# ============================================
# ElysianAnime 运行镜像
# 前置条件：release/elysiananime.jar 已构建完成（内置前端静态资源）
# 配置文件通过 docker-compose volumes 挂载到 /app/conf
# ============================================
FROM docker.1ms.run/library/eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

ENV TZ=Asia/Shanghai
WORKDIR /app

COPY release/elysiananime.jar ./elysiananime.jar

VOLUME ["/app/logs", "/app/files", "/app/conf"]
EXPOSE 8088

CMD ["java", "-jar", "./elysiananime.jar", "--config.add=./conf/app.yml"]
