# ElysianAnime Docker 快速部署

## 环境要求

- Docker 20.10+
- Docker Compose 2.0+

## 快速开始（3 步部署）

### 步骤 1：初始化数据库 SQL 文件

**Windows (PowerShell):**
```powershell
.\init-docker-db.ps1
```

**Linux/Mac:**
```bash
chmod +x init-docker-db.sh
./init-docker-db.sh
```

### 步骤 2：配置应用

复制并修改配置文件：

**Windows:**
```powershell
Copy-Item elysiananime-server\src\main\resources\app.yml docker\app\config\app.yml
```

**Linux/Mac:**
```bash
cp elysiananime-server/src/main/resources/app.yml docker/app/config/app.yml
```

修改 `docker/app/config/app.yml` 中的数据库连接：

```yaml
myapp:
  db1:
    jdbcUrl: jdbc:mysql://mysql:3306/elysiananime?...
  rd1:
    server: redis:6379
```

### 步骤 3：启动容器

```bash
docker-compose up -d
```

查看日志：
```bash
docker-compose logs -f
```

访问服务：http://localhost:8088

默认账号：admin / admin123456

## 常用命令

```bash
# 查看容器状态
docker-compose ps

# 停止所有服务
docker-compose down

# 重启应用
docker-compose restart elysiananime

# 查看实时日志
docker-compose logs -f elysiananime

# 进入 MySQL 容器
docker exec -it elysiananime-mysql mysql -u root -p

# 进入 Redis 容器
docker exec -it elysiananime-redis redis-cli

# 备份数据库
docker exec elysiananime-mysql mysqldump -u root -proot elysiananime > backup.sql

# 恢复数据库
docker exec -i elysiananime-mysql mysql -u root -proot elysiananime < backup.sql
```

## 数据持久化

所有数据保存在 `docker/` 目录：

- `docker/mysql/data/` - MySQL 数据文件
- `docker/redis/data/` - Redis 数据文件
- `docker/app/logs/` - 应用日志
- `docker/app/files/` - 上传的文件

## 故障排查

### 应用无法启动

1. 检查数据库和 Redis 是否正常
   ```bash
   docker-compose ps
   ```

2. 查看应用日志
   ```bash
   docker-compose logs elysiananime
   ```

3. 确认配置文件中的数据库地址使用 `mysql` 而不是 `localhost`

### 数据库连接失败

确保 `app.yml` 配置正确：
```yaml
jdbcUrl: jdbc:mysql://mysql:3306/elysiananime
```

### 内存不足

修改 `docker-compose.yml` 中的 JVM 参数：
```yaml
environment:
  JAVA_OPTS: "-Xms256m -Xmx512m"
```

## 生产环境建议

1. 修改默认密码（MySQL、Redis）
2. 启用 Redis 密码认证
3. 关闭不必要的端口暴露
4. 调整 JVM 内存参数
5. 使用外部数据库服务（可选）

详细文档请参考项目 wiki。
