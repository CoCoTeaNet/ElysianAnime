#!/bin/bash

# ElysianAnime Docker 初始化脚本
# 用于将 SQL 文件复制到 Docker 初始化目录

set -e

echo "====================================="
echo "ElysianAnime Docker 数据库初始化准备"
echo "====================================="

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SQL_SOURCE_DIR="$SCRIPT_DIR/doc/sql"
DOCKER_INIT_DIR="$SCRIPT_DIR/docker/mysql/init"

# 检查源目录是否存在
if [ ! -d "$SQL_SOURCE_DIR" ]; then
    echo "错误：找不到 SQL 文件目录：$SQL_SOURCE_DIR"
    exit 1
fi

# 创建 Docker 初始化目录（如果不存在）
if [ ! -d "$DOCKER_INIT_DIR" ]; then
    echo "创建 Docker 初始化目录：$DOCKER_INIT_DIR"
    mkdir -p "$DOCKER_INIT_DIR"
fi

# 复制 SQL 文件
echo ""
echo "正在复制 SQL 文件..."

files=("ddl-sql.sql" "ddl-update-sql.sql" "data-sql.sql")

for file in "${files[@]}"; do
    source_file="$SQL_SOURCE_DIR/$file"
    dest_file="$DOCKER_INIT_DIR/$file"
    
    if [ -f "$source_file" ]; then
        cp "$source_file" "$dest_file"
        echo "  ✓ 已复制：$file"
    else
        echo "  ✗ 文件不存在：$file"
    fi
done

echo ""
echo "====================================="
echo "初始化准备完成！"
echo "====================================="
echo ""
echo "下一步操作："
echo "1. 确保配置文件已就绪："
echo "   - 复制 elysiananime-server/src/main/resources/app.yml 到 docker/app/config/app.yml"
echo "   - 修改 app.yml 中的数据库连接为：jdbc:mysql://mysql:3306/elysiananime"
echo ""
echo "2. 启动 Docker 容器："
echo "   docker-compose up -d"
echo ""
echo "3. 查看启动日志："
echo "   docker-compose logs -f mysql"
echo ""
echo "====================================="
