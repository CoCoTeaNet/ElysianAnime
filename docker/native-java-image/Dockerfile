# 构建 native-java-image 基础镜像
# 用法：docker build -t native-java-image:latest .

FROM docker.m.daocloud.io/library/node:20-bookworm AS base

LABEL maintainer="CoCoTea" \
      description="ElysianAnime Build Environment - Node.js + GraalVM Native Java + Maven" \
      version="1.0.0"

# 设置时区
ENV TZ=Asia/Shanghai

# 安装必要的系统工具
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        bash \
        curl \
        git \
        wget \
        unzip \
        tar \
        gzip \
        fontconfig \
        fonts-dejavu-core && \
    rm -rf /var/lib/apt/lists/*

# 配置 Debian 使用阿里云镜像源
RUN sed -i 's/deb.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list.d/debian.sources && \
    sed -i 's/security.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list.d/debian.sources

# 安装 GraalVM 21（用于原生镜像构建）
# 使用 Oracle 官方发布的 GraalVM CE for Java 21
RUN set -eux; \
    arch=$(uname -m); \
    if [ "$arch" = "x86_64" ]; then \
        GRAALVM_URL="http://192.168.0.234:3000/graalvm-jdk-21_linux-x64_bin.tar.gz"; \
    elif [ "$arch" = "aarch64" ]; then \
        GRAALVM_URL="http://192.168.0.234:3000/graalvm-jdk-21_linux-aarch64_bin.tar.gz"; \
    else \
        echo "Unsupported architecture: $arch"; exit 1; \
    fi; \
    echo "=== Downloading GraalVM ==="; \
    echo "URL: $GRAALVM_URL"; \
    echo "Architecture: $arch"; \
    wget --progress=dot:giga "$GRAALVM_URL" -O /tmp/graalvm.tar.gz; \
    echo "=== Download completed, file size ==="; \
    ls -lh /tmp/graalvm.tar.gz; \
    echo "=== Extracting to /opt/graalvm ==="; \
    mkdir -p /opt/graalvm; \
    tar -xzf /tmp/graalvm.tar.gz -C /opt/graalvm --strip-components=1; \
    echo "=== Contents of /opt/graalvm ==="; \
    ls -la /opt/graalvm; \
    echo "=== Checking java binary ==="; \
    ls -la /opt/graalvm/bin/java; \
    /opt/graalvm/bin/java -version; \
    rm -f /tmp/graalvm.tar.gz; \
    echo "GraalVM installed successfully"

# 设置 GRAALVM_HOME 和 JAVA_HOME 环境变量
ENV GRAALVM_HOME=/opt/graalvm
ENV JAVA_HOME=/opt/graalvm
# 注意：MAVEN_HOME 将在后面定义，这里先设置 GraalVM 路径
ENV PATH=$GRAALVM_HOME/bin:$PATH

# 验证 GraalVM 版本
RUN java -version && \
    native-image --version

# 安装 Maven 3.9.14（最新稳定版本）
RUN wget -q http://192.168.0.234:3000/apache-maven-3.9.14-bin.tar.gz && \
    tar -xzf apache-maven-3.9.14-bin.tar.gz -C /opt && \
    mv /opt/apache-maven-3.9.14 /opt/maven && \
    rm apache-maven-3.9.14-bin.tar.gz

# 设置 Maven 环境变量
ENV MAVEN_HOME=/opt/maven
# 更新 PATH，将 Maven 添加到路径末尾
ENV PATH=$PATH:$MAVEN_HOME/bin

# 配置 Maven 使用阿里云镜像加速
RUN mkdir -p ~/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?> \
    <settings xmlns="http://maven.apache.org/SETTINGS/1.2.0" \
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
              xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 http://maven.apache.org/xsd/settings-1.2.0.xsd"> \
      <mirrors> \
        <mirror> \
          <id>aliyunmaven</id> \
          <mirrorOf>*</mirrorOf> \
          <name>Aliyun Maven</name> \
          <url>https://maven.aliyun.com/repository/public</url> \
        </mirror> \
      </mirrors> \
    </settings>' > ~/.m2/settings.xml

# 验证 Maven 安装
RUN mvn -version

# 安装 Node.js 和 pnpm
RUN node -v && \
    npm install -g pnpm && \
    pnpm -v

# 配置 pnpm 使用淘宝镜像源
RUN pnpm config set registry https://registry.npmmirror.com

# 创建工作目录
WORKDIR /workspace

# 输出环境信息
RUN echo "=== ElysianAnime Build Environment Ready ===" && \
    echo "Java Version:" && java -version && \
    echo "Maven Version:" && mvn -version && \
    echo "Node.js Version:" && node -v && \
    echo "pnpm Version:" && pnpm -v
