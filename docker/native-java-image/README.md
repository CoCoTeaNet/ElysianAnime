# native-java-image 基础镜像

ElysianAnime 项目的打包基础镜像，包含前后端构建所需的所有环境。

## 已安装环境

- **Node.js**: 20.x (前端构建)
- **npm**: 最新版本 (包管理)
- **OpenJDK**: 21.x (Java 运行环境)
- **Maven**: 3.9.9 (后端构建)
- **系统工具**: bash, curl, git, wget, unzip, tar, gzip, fontconfig, ttf-dejavu

## 快速开始

### 方式一：使用 PowerShell 脚本（推荐）

```powershell
cd docker\native-java-image
.\build-base-image.ps1
```

### 方式二：使用 CMD 脚本

```cmd
cd docker\native-java-image
build-base-image.bat
```

### 方式三：直接使用 Docker 命令

```bash
docker build -t native-java-image:latest .
```

## 在 IDEA 中运行

### 方法 1：使用 IDEA 的 Docker 功能

1. **打开 Docker 工具窗口**
   - View → Tool Windows → Docker

2. **添加 Docker 连接**（如果还没有）
   - 点击 `+` 号
   - 选择 Docker for Windows

3. **构建镜像**
   - 右键点击 `Dockerfile` 文件
   - 选择 "Build Image..."
   - 输入镜像标签：`native-java-image:latest`
   - 点击 Build

### 方法 2：使用 Terminal

1. **在 IDEA 中打开 Terminal**
   - View → Tool Windows → Terminal

2. **执行构建命令**
   ```bash
   cd docker\native-java-image
   docker build -t native-java-image:latest .
   ```

### 方法 3：配置 Run/Debug Configuration

1. **创建新的配置**
   - Run → Edit Configurations
   - 点击 `+` 号
   - 选择 "Docker" → "Docker Image"

2. **配置参数**
   - Name: `Build native-java-image`
   - Image tag: `native-java-image:latest`
   - Dockerfile: `docker\native-java-image\Dockerfile`

3. **运行配置**
   - 点击 Run 按钮

## 验证镜像

```bash
# 查看镜像
docker images | findstr native-java-image

# 测试 Java
docker run --rm native-java-image:latest java -version

# 测试 Maven
docker run --rm native-java-image:latest mvn -version

# 测试 Node.js
docker run --rm native-java-image:latest node -v
```

## 常见问题

### Q1: IDEA 无法识别 Dockerfile？

**解决方案：**
1. 确保已安装 Docker Desktop
2. 在 IDEA 设置中配置 Docker：File → Settings → Build, Execution, Deployment → Docker
3. 勾选 "Enable Docker integration"

### Q2: 构建时提示找不到文件？

**解决方案：**
确保在正确的目录执行命令：
```bash
cd D:\CodeLife\ElysianAnime\docker\native-java-image
docker build -t native-java-image:latest .
```

注意最后的 `.` 表示当前目录！

### Q3: 构建速度慢？

**解决方案：**
1. 首次构建需要下载依赖，后续会使用缓存
2. 检查网络连接
3. 确认使用了国内镜像源（已在 Dockerfile 中配置）

### Q4: 如何在 IDEA 中看到构建输出？

**解决方案：**
- 使用 Terminal 窗口执行命令可以看到实时输出
- 或者在 Docker 工具窗口中查看构建日志

## 使用示例

构建完成后，在主 Dockerfile 中使用：

```dockerfile
FROM native-java-image:latest AS frontend-builder
# ... 前端构建步骤

FROM native-java-image:latest AS builder
# ... 后端构建步骤
```

## 清理镜像

```bash
# 删除镜像
docker rmi native-java-image:latest

# 删除所有悬空镜像
docker image prune -f
```

## 相关文档

- [完整构建指南](../BUILD-GUIDE.md)
- [快速开始](../QUICKSTART.md)
- [主项目文档](../../readme.md)
