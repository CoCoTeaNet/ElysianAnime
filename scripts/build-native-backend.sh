#!/usr/bin/env bash
set -euo pipefail

# 用途：将本项目 Java 后端（elysiananime-server/elysiananime-api）打包为 Linux 原生可执行文件（GraalVM Native Image）。
#
# 默认：走 Maven Profile native（文档中提到的：mvn clean package -Pnative -DskipTests）
# 兜底：如果 Maven native profile 不可用，可加 --cli 改走 native-image 命令行（参考仓库自带 build-native-linux.sh 思路）
#
# 前置要求（Linux）：
# 1) 安装 GraalVM JDK 21，并设置：
#    export GRAALVM_HOME=/path/to/graalvm-jdk-21
#    export PATH=$GRAALVM_HOME/bin:$PATH
# 2) native-image 可用：native-image --version
# 3) Maven 可用：mvn -v
#
# 用法：
#   chmod +x ./scripts/build-native-backend.sh
#   ./scripts/build-native-backend.sh                 # 默认输出到 release-native-backend/
#   ./scripts/build-native-backend.sh -o dist-native  # 自定义输出目录
#   ./scripts/build-native-backend.sh --cli           # 走 native-image 命令行兜底

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SERVER_DIR="${ROOT_DIR}/elysiananime-server"
API_DIR="${SERVER_DIR}/elysiananime-api"

OUT_DIR="${ROOT_DIR}/release-native-backend"
USE_CLI="0"

usage() {
  cat <<'EOF'
用法：
  ./scripts/build-native-backend.sh [options]

Options:
  -o, --output <dir>   输出目录（相对仓库根目录或绝对路径），默认：release-native-backend
  --cli                使用 native-image 命令行兜底（不走 mvn -Pnative）
  -h, --help           显示帮助
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -o|--output)
      OUT_DIR="$2"
      shift 2
      ;;
    --cli)
      USE_CLI="1"
      shift 1
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "[ERROR] 未知参数：$1" >&2
      usage
      exit 2
      ;;
  esac
done

if [[ ! -d "${API_DIR}" ]]; then
  echo "[ERROR] 未找到后端模块目录：${API_DIR}" >&2
  exit 1
fi

if [[ -z "${GRAALVM_HOME:-}" ]]; then
  echo "[ERROR] 未检测到 GRAALVM_HOME 环境变量，请先安装 GraalVM JDK 21 并设置 GRAALVM_HOME。" >&2
  exit 1
fi

export JAVA_HOME="${GRAALVM_HOME}"
export PATH="${GRAALVM_HOME}/bin:${PATH}"

command -v mvn >/dev/null 2>&1 || { echo "[ERROR] 未找到 mvn，请先安装 Maven。" >&2; exit 1; }
command -v native-image >/dev/null 2>&1 || { echo "[ERROR] 未找到 native-image，请确认 GraalVM 安装与 PATH 设置正确。" >&2; exit 1; }

# 为避免某些环境下 ~/.m2/settings.xml 配置了 http 镜像导致 Maven 3.8+ 默认拦截，
# 这里生成一个最小 settings.xml 并强制 mvn 使用它。
MAVEN_SETTINGS="${OUT_DIR}/maven-settings.xml"
mkdir -p "${OUT_DIR}"
cat > "${MAVEN_SETTINGS}" <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
</settings>
EOF

# 规范化输出路径
if [[ "${OUT_DIR}" != /* ]]; then
  OUT_DIR="${ROOT_DIR}/${OUT_DIR}"
fi

echo "========================================"
echo "ElysianAnime 后端原生打包（Linux）"
echo "========================================"
echo "[INFO] ROOT_DIR     : ${ROOT_DIR}"
echo "[INFO] OUT_DIR      : ${OUT_DIR}"
echo "[INFO] GRAALVM_HOME : ${GRAALVM_HOME}"
echo "[INFO] MODE         : $([[ "${USE_CLI}" == "1" ]] && echo "native-image CLI" || echo "mvn -Pnative")"
echo

rm -rf "${OUT_DIR}"
mkdir -p "${OUT_DIR}/"{config,logs,files}

if [[ "${USE_CLI}" == "0" ]]; then
  echo "[INFO] 使用 Maven Profile 'native' 构建（推荐）..."
  pushd "${API_DIR}" >/dev/null
  mvn -s "${MAVEN_SETTINGS}" clean package -Pnative -DskipTests -B
  popd >/dev/null
else
  echo "[WARN] 使用 native-image 命令行兜底构建（不走 Maven -Pnative）..."
  echo "[INFO] 先编译后端（生成 target/classes 与依赖）..."
  pushd "${SERVER_DIR}" >/dev/null
  mvn -s "${MAVEN_SETTINGS}" -f "${SERVER_DIR}/pom.xml" clean package -pl elysiananime-api -am -DskipTests -B
  popd >/dev/null

  # 关键修复：
  # 某些 fat-jar 会把 classes 放在 BOOT-INF/classes/，native-image 直接 -cp app.jar 会找不到主类。
  # 因此这里改为使用 target/classes + runtime 依赖 classpath。
  CLASS_DIR="${API_DIR}/target/classes"
  if [[ ! -f "${CLASS_DIR}/net/cocotea/elysiananime/Launcher.class" ]]; then
    echo "[ERROR] 未找到主类 class：${CLASS_DIR}/net/cocotea/elysiananime/Launcher.class" >&2
    exit 1
  fi

  CP_FILE="${OUT_DIR}/classpath.txt"
  pushd "${API_DIR}" >/dev/null
  mvn -s "${MAVEN_SETTINGS}" -q -B dependency:build-classpath -DincludeScope=runtime -DskipTests -Dmdep.outputFile="${CP_FILE}"
  popd >/dev/null

  DEP_CP=""
  if [[ -f "${CP_FILE}" ]]; then
    DEP_CP="$(tr -d '\r\n' < "${CP_FILE}")"
  fi
  FULL_CP="${CLASS_DIR}"
  if [[ -n "${DEP_CP}" ]]; then
    FULL_CP="${CLASS_DIR}:${DEP_CP}"
  fi

  echo "[INFO] 开始 native-image 构建（可能需要几分钟）..."
  pushd "${OUT_DIR}" >/dev/null
  native-image \
    --no-fallback \
    --report-unsupported-elements-at-runtime \
    -H:+UnlockExperimentalVMOptions \
    -H:+ReportExceptionStackTraces \
    -H:IncludeResources='.*\.xml$' \
    -H:IncludeResources='.*\.properties$' \
    -H:IncludeResources='.*\.json$' \
    -H:Name=elysiananime \
    -cp "${FULL_CP}" \
    net.cocotea.elysiananime.Launcher
  popd >/dev/null
fi

# 复制产物：优先从 Maven 输出目录找，其次看 OUT_DIR
BIN_CANDIDATE_1="${API_DIR}/target/elysiananime"
BIN_CANDIDATE_2="${API_DIR}/target/elysiananime-api"

if [[ -f "${BIN_CANDIDATE_1}" ]]; then
  cp -f "${BIN_CANDIDATE_1}" "${OUT_DIR}/elysiananime"
elif [[ -f "${OUT_DIR}/elysiananime" ]]; then
  : # 已在 OUT_DIR 生成
else
  echo "[WARN] 没有找到可执行文件："
  echo "  - ${BIN_CANDIDATE_1}"
  echo "  - ${OUT_DIR}/elysiananime"
  echo "[WARN] 如果你使用 mvn -Pnative：请检查 native 构建的输出位置/插件配置。"
  echo "[WARN] 你也可以尝试加 --cli 兜底：./scripts/build-native-backend.sh --cli"
  exit 2
fi

chmod +x "${OUT_DIR}/elysiananime" || true

# 拷贝配置（如存在）
APP_YML="${ROOT_DIR}/release/config/app.yml"
if [[ -f "${APP_YML}" ]]; then
  cp -f "${APP_YML}" "${OUT_DIR}/config/app.yml"
else
  echo "[WARN] 未找到 ${APP_YML}，将使用程序默认配置（如有）。"
fi

echo
echo "========================================"
echo "[SUCCESS] 后端原生打包完成"
echo "========================================"
echo "输出目录：${OUT_DIR}"
echo "可执行文件：${OUT_DIR}/elysiananime"
echo
echo "运行方式："
echo "  cd \"${OUT_DIR}\""
echo "  ./elysiananime"
