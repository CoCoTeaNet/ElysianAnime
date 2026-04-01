@echo off
setlocal enabledelayedexpansion

:: 设置项目根目录
set "PROJECT_ROOT=%~dp0"
set "SERVER_DIR=%PROJECT_ROOT%elysiananime-server"
set "UI_DIR=%PROJECT_ROOT%elysiananime-ui"
set "RELEASE_DIR=%PROJECT_ROOT%release-native"

:: 检查 GRAALVM_HOME 环境变量
if "%GRAALVM_HOME%"=="" (
    echo [ERROR] 未检测到 GRAALVM_HOME 环境变量
    echo 请确保已安装 GraalVM 并设置 GRAALVM_HOME 环境变量
    echo.
    echo Windows 设置示例:
    echo setx GRAALVM_HOME "C:\Program Files\Java\graalvm-jdk-21"
    echo.
    pause
    exit /b 1
)

echo ========================================
echo ElysianAnime 原生镜像打包 - Windows
echo ========================================
echo GRAALVM_HOME: %GRAALVM_HOME%
echo.

:: 1. 打包后端 - 普通 JAR
echo [步骤 1/4] 编译后端 JAR...
call mvn -f "%SERVER_DIR%\pom.xml" clean package -DskipTests -Dmaven.test.skip=true
if %errorlevel% neq 0 (
    echo [错误] 后端 JAR 打包失败
    exit /b %errorlevel%
)
echo [成功] 后端 JAR 打包完成
echo.

:: 2. 打包前端
echo [步骤 2/4] 编译前端资源...
cd /d "%UI_DIR%"
call powershell -Command "fnm env --use-on-cd | Out-String | Invoke-Expression; fnm use --install-if-missing 22; npm -v; npm run build;"
if %errorlevel% neq 0 (
    echo [错误] 前端打包失败
    exit /b %errorlevel%
)
echo [成功] 前端打包完成
echo.

:: 3. 创建 release 目录
echo [步骤 3/4] 准备发布目录...
if exist "%RELEASE_DIR%" (
    RD /S /Q "%RELEASE_DIR%"
)
MKDIR "%RELEASE_DIR%"
MKDIR "%RELEASE_DIR%\html"
MKDIR "%RELEASE_DIR%\logs"
MKDIR "%RELEASE_DIR%\files"
MKDIR "%RELEASE_DIR%\config"

:: 复制 JAR 文件（用于 Native Image 构建）
copy /Y "%SERVER_DIR%\elysiananime-api\target\elysiananime.jar" "%RELEASE_DIR%\elysiananime.jar"
if %errorlevel% neq 0 (
    echo [错误] 复制 JAR 文件失败
    exit /b %errorlevel%
)

:: 复制前端资源
xcopy /Y /F /E /I "%UI_DIR%\dist\" "%RELEASE_DIR%\html"
if %errorlevel% neq 0 (
    echo [错误] 复制前端资源失败
    exit /b %errorlevel%
)

:: 复制配置文件
copy /Y "%PROJECT_ROOT%release\config\app.yml" "%RELEASE_DIR%\config\" 2>nul
if %errorlevel% neq 0 (
    echo [提示] 未找到 app.yml 配置文件，将使用默认配置
)

:: 复制启动脚本
copy /Y "%PROJECT_ROOT%doc\bin\start.bat" "%RELEASE_DIR%\" 2>nul
copy /Y "%PROJECT_ROOT%doc\bin\stop.bat" "%RELEASE_DIR%\" 2>nul
echo [成功] 文件复制完成
echo.

:: 4. 构建 Native Image
echo [步骤 4/4] 开始构建 Native Image (这可能需要几分钟)...
cd /d "%RELEASE_DIR%"

:: 检查 native-image 命令是否可用
where native-image >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未找到 native-image 命令
    echo 请确保 GraalVM 的 bin 目录已添加到 PATH 环境变量
    echo 或设置 PATH=%%GRAALVM_HOME%%\bin
    pause
    exit /b 1
)

:: 执行 Native Image 构建
echo 正在使用 native-image 构建可执行文件...
native-image ^
    --no-fallback ^
    --allow-incomplete-classpath ^
    --report-unsupported-elements-at-runtime ^
    -H:+ReportExceptionStackTraces ^
    -H:IncludeResources=.*\.xml$ ^
    -H:IncludeResources=.*\.properties$ ^
    -H:IncludeResources=.*\.json$ ^
    -H:Name=elysiananime ^
    -cp elysiananime.jar ^
    net.cocotea.elysiananime.Launcher

if %errorlevel% neq 0 (
    echo [错误] Native Image 构建失败
    echo 请检查上方的错误信息
    pause
    exit /b %errorlevel%
)

echo.
echo ========================================
echo [成功] Native Image 构建完成!
echo ========================================
echo.
echo 输出文件：%RELEASE_DIR%\elysiananime.exe
echo.
echo 发布目录结构:
echo   %RELEASE_DIR%\
echo     ├── elysiananime.exe      (原生可执行文件)
echo     ├── elysiananime.jar      (JAR 包备份)
echo     ├── html/                 (前端静态资源)
echo     ├── logs/                 (日志目录)
echo     ├── files/                (文件存储目录)
echo     └── config/               (配置文件目录)
echo.
echo 启动方式:
echo   Windows: .\elysiananime.exe
echo   或使用：start.bat
echo.

pause
