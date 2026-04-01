#!/bin/bash

# 设置项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="${PROJECT_ROOT}/elysiananime-server"
UI_DIR="${PROJECT_ROOT}/elysiananime-ui"
RELEASE_DIR="${PROJECT_ROOT}/release-native"

# 检查 GRAALVM_HOME 环境变量
if [ -z "$GRAALVM_HOME" ]; then
    echo "[错误] 未检测到 GRAALVM_HOME 环境变量"
    echo "请确保已安装 GraalVM 并设置 GRAALVM_HOME 环境变量"
    echo ""
    echo "Linux 设置示例:"
    echo "export GRAALVM_HOME=/usr/lib/jvm/graalvm-jdk-21"
    echo "或添加到 ~/.bashrc 或 ~/.zshrc"
    exit 1
fi

echo "========================================"
echo "ElysianAnime 原生镜像打包 - Linux"
echo "========================================"
echo "GRAALVM_HOME: $GRAALVM_HOME"
echo ""

# 1. 打包后端 - 普通 JAR
echo "[步骤 1/4] 编译后端 JAR..."
mvn -f "${SERVER_DIR}/pom.xml" clean package -DskipTests -Dmaven.test.skip=true
if [ $? -ne 0 ]; then
    echo "[错误] 后端 JAR 打包失败"
    exit 1
fi
echo "[成功] 后端 JAR 打包完成"
echo ""

# 2. 打包前端
echo "[步骤 2/4] 编译前端资源..."
cd "${UI_DIR}" || exit 1

# 检查是否使用 fnm
if command -v fnm &> /dev/null; then
    eval "$(fnm env --use-on-cd)"
    fnm use --install-if-missing 22
fi

npm run build
if [ $? -ne 0 ]; then
    echo "[错误] 前端打包失败"
    exit 1
fi
echo "[成功] 前端打包完成"
echo ""

# 3. 创建 release 目录
echo "[步骤 3/4] 准备发布目录..."
rm -rf "${RELEASE_DIR}"
mkdir -p "${RELEASE_DIR}"
mkdir -p "${RELEASE_DIR}/html"
mkdir -p "${RELEASE_DIR}/logs"
mkdir -p "${RELEASE_DIR}/files"
mkdir -p "${RELEASE_DIR}/config"

# 复制 JAR 文件（用于 Native Image 构建）
cp -f "${SERVER_DIR}/elysiananime-api/target/elysiananime.jar" "${RELEASE_DIR}/elysiananime.jar"
if [ $? -ne 0 ]; then
    echo "[错误] 复制 JAR 文件失败"
    exit 1
fi

# 复制前端资源
cp -rf "${UI_DIR}/dist/"* "${RELEASE_DIR}/html/"
if [ $? -ne 0 ]; then
    echo "[错误] 复制前端资源失败"
    exit 1
fi

# 复制配置文件
if [ -f "${PROJECT_ROOT}/release/config/app.yml" ]; then
    cp -f "${PROJECT_ROOT}/release/config/app.yml" "${RELEASE_DIR}/config/"
else
    echo "[提示] 未找到 app.yml 配置文件，将使用默认配置"
fi

# 复制启动脚本
if [ -f "${PROJECT_ROOT}/doc/bin/start.sh" ]; then
    cp -f "${PROJECT_ROOT}/doc/bin/start.sh" "${RELEASE_DIR}/"
    chmod +x "${RELEASE_DIR}/start.sh"
fi
if [ -f "${PROJECT_ROOT}/doc/bin/stop.sh" ]; then
    cp -f "${PROJECT_ROOT}/doc/bin/stop.sh" "${RELEASE_DIR}/"
    chmod +x "${RELEASE_DIR}/stop.sh"
fi
echo "[成功] 文件复制完成"
echo ""

# 4. 构建 Native Image
echo "[步骤 4/4] 开始构建 Native Image (这可能需要几分钟)..."
cd "${RELEASE_DIR}" || exit 1

# 检查 native-image 命令是否可用
if ! command -v native-image &> /dev/null; then
    echo "[错误] 未找到 native-image 命令"
    echo "请确保 GraalVM 的 bin 目录已添加到 PATH 环境变量"
    echo "或设置 PATH=\$GRAALVM_HOME/bin"
    exit 1
fi

# 执行 Native Image 构建
echo "正在使用 native-image 构建可执行文件..."
native-image \
    --no-fallback \
    --allow-incomplete-classpath \
    --report-unsupported-elements-at-runtime \
    -H:+ReportExceptionStackTraces \
    -H:IncludeResources=.*\.xml$ \
    -H:IncludeResources=.*\.properties$ \
    -H:IncludeResources=.*\.json$ \
    -H:Name=elysiananime \
    -cp elysiananime.jar \
    net.cocotea.elysiananime.Launcher

if [ $? -ne 0 ]; then
    echo "[错误] Native Image 构建失败"
    echo "请检查上方的错误信息"
    exit 1
fi

echo ""
echo "========================================"
echo "[成功] Native Image 构建完成!"
echo "========================================"
echo ""
echo "输出文件：${RELEASE_DIR}/elysiananime"
echo ""
echo "发布目录结构:"
echo "  ${RELEASE_DIR}/"
echo "    ├── elysiananime          (原生可执行文件)"
echo "    ├── elysiananime.jar      (JAR 包备份)"
echo "    ├── html/                 (前端静态资源)"
echo "    ├── logs/                 (日志目录)"
echo "    ├── files/                (文件存储目录)"
echo "    └── config/               (配置文件目录)"
echo ""
echo "启动方式:"
echo "  Linux: ./elysiananime"
echo "  或使用：./start.sh"
echo ""
