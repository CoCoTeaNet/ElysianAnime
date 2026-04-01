# 多阶段构建 - 前端编译阶段
# 使用国内镜像源加速
FROM docker.m.daocloud.io/library/node:20-alpine AS frontend-builder

# 设置工作目录
WORKDIR /frontend

# 复制前端项目文件
COPY elysiananime-ui/package.json ./

# 安装前端依赖（使用阿里云镜像源）
RUN npm config set registry https://registry.npmmirror.com && \
    npm install

# 复制前端源代码
COPY elysiananime-ui/ ./

# 执行前端构建
RUN npm run build

# 多阶段构建 - 后端编译阶段
# 使用 GraalVM 进行原生镜像构建
FROM ghcr.io/graalvm/jdk-community:21 AS builder

# 设置工作目录
WORKDIR /build

# 先安装 gzip 和解压工具
RUN microdnf install -y gzip tar 2>/dev/null || dnf install -y gzip tar

# 复制本地 Maven 压缩包
COPY docker/apache-maven-3.9.14-bin.tar.gz /tmp/

# 解压并配置 Maven
RUN tar -xzf /tmp/apache-maven-3.9.14-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.14/bin/mvn /usr/bin/mvn && \
    rm /tmp/apache-maven-3.9.14-bin.tar.gz

# 从前端构建阶段复制 dist 文件到后端静态资源目录
COPY --from=frontend-builder /frontend/dist ./elysiananime-api/src/main/resources/static

# 复制 pom.xml 文件以利用 Docker 缓存层
COPY elysiananime-server/pom.xml ./pom.xml
COPY elysiananime-server/elysiananime-api/pom.xml ./elysiananime-api/pom.xml
COPY elysiananime-server/elysiananime-common/pom.xml ./elysiananime-common/pom.xml

# 配置 Maven 使用腾讯镜像源
RUN mkdir -p ~/.m2 && \
    echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \
    <settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" \
              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \
              xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd\"> \
      <mirrors> \
            <mirror> \
                <id>tencent-cloud</id> \
                <mirrorOf>central</mirrorOf> \
                <url>http://mirrors.cloud.tencent.com/nexus/repository/maven-public/</url> \
            </mirror> \
      </mirrors> \
    </settings>" > ~/.m2/settings.xml

# 下载依赖（利用缓存）
RUN mvn dependency:go-offline -B || true

# 复制源代码
COPY elysiananime-server/elysiananime-common/src ./elysiananime-common/src
COPY elysiananime-server/elysiananime-api/src ./elysiananime-api/src

# 执行 Maven 构建（先安装 common 模块）
RUN mvn clean install -pl elysiananime-common -am -DskipTests -Dmaven.test.skip=true -B && \
    mvn package -pl elysiananime-api -am -DskipTests -Dmaven.test.skip=true -B

# 构建 GraalVM 原生镜像
RUN export JAVA_HOME=/usr/lib/jvm/java-21-graalvm && \
    export PATH=$JAVA_HOME/bin:$PATH && \
    cd elysiananime-api/target && \
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

# 运行阶段 - 使用 Alpine 作为基础镜像
# 使用国内镜像源
FROM docker.m.daocloud.io/library/alpine:latest AS runner

LABEL maintainer="CoCoTea" \
      description="ElysianAnime - Anime Management System" \
      version="3.0.0"

# 配置 APK 使用阿里云镜像源
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories

# 更新包索引
RUN apk update

# 安装 OpenJDK 21 JRE 和其他必要工具
RUN apk add --no-cache openjdk21-jre \
    && apk add --no-cache curl \
    && apk add --no-cache fontconfig ttf-dejavu

# 设置 JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV TZ=Asia/Shanghai

# 设置工作目录
WORKDIR /app

# 从构建阶段复制原生可执行文件
COPY --from=builder /build/elysiananime-api/target/elysiananime ./elysiananime

# 创建数据卷挂载点（用于日志和文件存储）
VOLUME ["/app/logs", "/app/files"]

# 暴露端口（根据 app.yml 配置为 8088）
EXPOSE 8088

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8088/api/system/health || exit 1

# 启动应用
ENTRYPOINT ["./elysiananime"]
