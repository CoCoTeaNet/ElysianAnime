# ElysianAnime 原生镜像打包指南

本文档说明如何使用 GraalVM Native Image 将应用打包为 Windows 和 Linux 原生可执行文件。

## 环境要求

### 1. GraalVM 安装

#### Windows
1. 下载 GraalVM JDK 21: https://www.graalvm.org/downloads/
2. 解压到目录，例如：`C:\Program Files\Java\graalvm-jdk-21`
3. 设置环境变量：
   ```cmd
   setx GRAALVM_HOME "C:\Program Files\Java\graalvm-jdk-21"
   setx PATH "%GRAALVM_HOME%\bin;%PATH%"
   ```
4. 验证安装：
   ```cmd
   native-image --version
   ```

#### Linux
1. 下载 GraalVM JDK 21: https://www.graalvm.org/downloads/
2. 解压到目录，例如：`/usr/lib/jvm/graalvm-jdk-21`
3. 设置环境变量（添加到 `~/.bashrc` 或 `~/.zshrc`）：
   ```bash
   export GRAALVM_HOME=/usr/lib/jvm/graalvm-jdk-21
   export PATH=$GRAALVM_HOME/bin:$PATH
   ```
4. 使配置生效：
   ```bash
   source ~/.bashrc  # 或 source ~/.zshrc
   ```
5. 验证安装：
   ```bash
   native-image --version
   ```

### 2. 其他依赖

- **Maven 3.6+**: 用于构建 Java 项目
- **Node.js 22+**: 用于构建前端资源（使用 fnm 管理）
- **Visual Studio Build Tools (Windows)**: 用于编译原生镜像
  - 安装时选择 "使用 C++ 的桌面开发"
  - 或使用：`choco install visualstudio2022buildtools`

## 打包方法

### 方法一：使用 release.bat (Windows)

#### 普通打包（JAR 方式）
```cmd
release.bat
```

#### 原生打包（Native Image）
```cmd
release.bat --native
```
或
```cmd
release.bat -n
```

### 方法二：使用专用脚本

#### Windows
```cmd
build-native-windows.bat
```

#### Linux
```bash
chmod +x build-native-linux.sh
./build-native-linux.sh
```

### 方法三：使用 Maven 命令

```bash
cd elysiananime-server/elysiananime-api
mvn clean package -Pnative -DskipTests
```

## 输出目录结构

打包完成后，会在 `release-native` 目录生成以下文件：

```
release-native/
├── elysiananime.exe      (Windows) / elysiananime (Linux)  # 原生可执行文件
├── elysiananime.jar      (JAR 包备份)
├── html/                 (前端静态资源)
├── logs/                 (日志目录)
├── files/                (文件存储目录)
└── config/               (配置文件目录)
    └── app.yml          (应用配置文件)
```

## 运行应用

### Windows
```cmd
cd release-native
.\elysiananime.exe
```

或使用启动脚本：
```cmd
start.bat
```

### Linux
```bash
cd release-native
./elysiananime
```

或使用启动脚本：
```bash
./start.sh
```

## 优势对比

| 特性 | JAR 方式 | Native Image |
|------|----------|--------------|
| 启动时间 | ~3-5 秒 | ~0.1-0.5 秒 |
| 内存占用 | ~200-300MB | ~50-100MB |
| 需要 JVM | 是 | 否 |
| 文件大小 | ~50MB | ~100-150MB |
| 跨平台 | 是 | 否（需分平台编译）|
| 部署复杂度 | 中 | 低 |

## 常见问题

### Q1: native-image 命令未找到
**解决**: 确保已设置 `GRAALVM_HOME` 并将 `%GRAALVM_HOME%\bin` 添加到 PATH

### Q2: 编译时提示缺少 C/C++ 编译器 (Windows)
**解决**: 安装 Visual Studio Build Tools，并确保安装了 "使用 C++ 的桌面开发" 组件

### Q3: 编译时内存不足
**解决**: 增加系统内存或调整 JVM 参数：
```bash
export MAVEN_OPTS="-Xmx4g"
```

### Q4: 原生镜像启动时报类找不到
**解决**: 这是正常的，因为使用了 `--allow-incomplete-classpath` 参数。如果影响功能，需要在 pom.xml 中添加反射配置。

## 自定义配置

如需调整 Native Image 编译参数，编辑 `elysiananime-api/pom.xml`:

```xml
<plugin>
    <groupId>org.graalvm.buildtools</groupId>
    <artifactId>native-maven-plugin</artifactId>
    <configuration>
        <buildArgs>
            <!-- 在这里添加或修改参数 -->
            <arg>--no-fallback</arg>
            <arg>-H:+ReportExceptionStackTraces</arg>
        </buildArgs>
    </configuration>
</plugin>
```

## 参考资料

- [GraalVM 官方文档](https://www.graalvm.org/latest/docs/)
- [Native Image 性能优化](https://www.graalvm.org/latest/reference-manual/native-image/PerformanceOptions/)
