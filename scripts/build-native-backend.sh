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
  mvn clean package -Pnative -DskipTests
  popd >/dev/null
else
  echo "[WARN] 使用 native-image 命令行兜底构建（不走 Maven -Pnative）..."
  echo "[INFO] 先构建 JAR..."
  pushd "${SERVER_DIR}" >/dev/null
  mvn -f "${SERVER_DIR}/pom.xml" clean package -pl elysiananime-api -am -DskipTests -Dmaven.test.skip=true
  popd >/dev/null

  JAR_PATH="${API_DIR}/target/elysiananime.jar"
  if [[ ! -f "${JAR_PATH}" ]]; then
    echo "[ERROR] 未找到 JAR：${JAR_PATH}" >&2
    exit 1
  fi

  cp -f "${JAR_PATH}" "${OUT_DIR}/elysiananime.jar"

  echo "[INFO] 开始 native-image 构建（可能需要几分钟）..."
  pushd "${OUT_DIR}" >/dev/null
  native-image \
    --no-fallback \
    --allow-incomplete-classpath \
    --report-unsupported-elements-at-runtime \
    -H:+ReportExceptionStackTraces \
    -H:IncludeResources='.*\.xml$' \
    -H:IncludeResources='.*\.properties$' \
    -H:IncludeResources='.*\.json$' \
    -H:Name=elysiananime \
    -cp "elysiananime.jar" \
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

