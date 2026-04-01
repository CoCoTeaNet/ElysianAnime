# 多阶段构建 - 前端编译阶段
# 使用 native-java-image 基础镜像（已包含 Node.js、Maven、GraalVM）
FROM native-java-image:latest AS frontend-builder

# 设置工作目录
WORKDIR /frontend

# 复制前端项目文件
COPY elysiananime-ui/package.json ./

# 安装前端依赖（使用阿里云镜像源）
RUN echo "=== [FRONTEND] Installing dependencies ===" && \
    npm config set registry https://registry.npmmirror.com && \
    npm install && \
    echo "=== [FRONTEND] Dependencies installed successfully ==="

# 复制前端源代码
COPY elysiananime-ui/ ./

# 执行前端构建
RUN echo "=== [FRONTEND] Building frontend application ===" && \
    npm run build && \
    echo "=== [FRONTEND] Build completed successfully ===" && \
    ls -la dist/

# 多阶段构建 - 后端编译阶段
# 使用 native-java-image 基础镜像进行原生镜像构建
FROM native-java-image:latest AS builder

# 设置工作目录
WORKDIR /build

# 复制 pom.xml 文件以利用 Docker 缓存层
RUN echo "=== [BACKEND] Copying POM files ==="
COPY elysiananime-server/pom.xml ./pom.xml
COPY elysiananime-server/elysiananime-api/pom.xml ./elysiananime-api/pom.xml
COPY elysiananime-server/elysiananime-common/pom.xml ./elysiananime-common/pom.xml
RUN echo "=== [BACKEND] POM files copied ==="

# 下载依赖（利用缓存，Maven 配置已在基础镜像中完成）
RUN echo "=== [BACKEND] Downloading Maven dependencies ===" && \
    mvn dependency:go-offline -B && \
    echo "=== [BACKEND] Dependencies downloaded ===" || \
    (echo "=== [BACKEND] Warning: Some dependencies may have failed to download ===" && exit 0)

# 复制源代码
COPY elysiananime-server/elysiananime-common/src ./elysiananime-common/src
COPY elysiananime-server/elysiananime-api/src ./elysiananime-api/src
RUN echo "=== [BACKEND] Source code copied ==="

# 从前端构建阶段复制 dist 文件到后端静态资源目录
RUN echo "=== [BACKEND] Copying frontend dist to static resources ==="
COPY --from=frontend-builder /frontend/dist ./elysiananime-api/src/main/resources/static
RUN echo "=== [BACKEND] Frontend resources copied ===" && \
    ls -la ./elysiananime-api/src/main/resources/static/

# 执行 Maven 构建（先安装 common 模块，再打包 api 模块）
RUN echo "=== [BACKEND] Building common module ===" && \
    mvn clean install -pl elysiananime-common -am -DskipTests -Dmaven.test.skip=true -B && \
    echo "=== [BACKEND] Common module built successfully ===" && \
    echo "=== [BACKEND] Building API module ===" && \
    mvn package -pl elysiananime-api -am -DskipTests -Dmaven.test.skip=true -B && \
    echo "=== [BACKEND] API module packaged successfully ===" && \
    ls -la ./elysiananime-api/target/*.jar

# 构建 GraalVM 原生镜像
RUN echo "=========================================" && \
    echo "=== [NATIVE] Starting native image build ===" && \
    echo "=========================================" && \
    export JAVA_HOME=/opt/graalvm && \
    export PATH=$JAVA_HOME/bin:$PATH && \
    echo "=== [NATIVE] JAVA_HOME: $JAVA_HOME ===" && \
    echo "=== [NATIVE] PATH: $PATH ===" && \
    echo "=== [NATIVE] Native Image Version ===" && \
    native-image --version && \
    echo "=== [NATIVE] JAR files in target directory ===" && \
    # 打包成native可执行程序
    mvn clean native:compile -f elysiananime-api -P native -DskipTests && \
    echo "=========================================" && \
    echo "=== [NATIVE] Native image build completed! ===" && \
    echo "=========================================" && \
    ls -lh elysiananime-api/target

# 运行阶段 - 使用 Alpine 作为基础镜像
# 使用国内镜像源
FROM docker.m.daocloud.io/library/alpine:3.19 AS runner
RUN apk add --no-cache gcompat

LABEL maintainer="CoCoTea" \
      description="ElysianAnime - Anime Management System" \
      version="3.0.0"

# 配置 APK 使用阿里云镜像源
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories

# 更新包索引
RUN apk update

# 安装必要的工具（原生镜像不需要 JRE）
RUN apk add --no-cache curl \
    && apk add --no-cache fontconfig ttf-dejavu

# 设置环境变量
ENV TZ=Asia/Shanghai

# 设置工作目录
WORKDIR /app

# 从构建阶段复制原生可执行文件
COPY --from=builder /build/elysiananime-api/target/elysiananime-api ./elysiananime

# 赋予执行权限
RUN chmod +x ./elysiananime

# 创建数据卷挂载点（用于日志和文件存储）
VOLUME ["/app/logs", "/app/files"]

# 暴露端口（根据 app.yml 配置为 8088）
EXPOSE 8088

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8088/api/system/dashboard/health || exit 1

# 启动应用
ENTRYPOINT ["./elysiananime"]
