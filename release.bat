@echo off

:: 设置项目的根路径和目标目录
set "PROJECT_ROOT=%~dp0"

:: 开始打包后端
call mvn -f "%PROJECT_ROOT%elysiananime-server\elysiananime-common\pom.xml" clean install -Dmaven.test.skip=true
if %errorlevel% neq 0 (
    echo Error occurred while installing elysiananime-common.
    exit /b %errorlevel%
)
echo elysiananime-common install finish.

call mvn -f "%PROJECT_ROOT%elysiananime-server\elysiananime-api\pom.xml" clean install -Dmaven.test.skip=true
if %errorlevel% neq 0 (
    echo Error occurred while installing elysiananime-api.
    exit /b %errorlevel%
)
echo elysiananime-api install finish.

:: 拷贝文件到release目录
xcopy /Y /F "%PROJECT_ROOT%elysiananime-server\elysiananime-api\target\elysiananime.jar" "%PROJECT_ROOT%release"
if %errorlevel% neq 0 (
    echo Failed to copy jar file.
    exit /b %error level%
)
echo xcopy copy jar file finish.

:: 切换到前端打包
cd "%PROJECT_ROOT%elysiananime-ui"

:: 加载 fnm 环境配置
call powershell -Command "fnm env --use-on-cd | Out-String | Invoke-Expression; fnm use --install-if-missing 22; npm -v; npm run build;"
if %errorlevel% neq 0 (
    echo Error occurred while ui package.
    exit /b %errorlevel%
)
echo ui package finish.

:: 拷贝文件到release目录（先删除历史的）
RD /S /Q "%PROJECT_ROOT%release\html"
MKDIR "%PROJECT_ROOT%release\html"
xcopy /Y /F "%PROJECT_ROOT%elysiananime-ui\dist\" "%PROJECT_ROOT%release\html" /E /I
if %errorlevel% neq 0 (
    echo Failed to copy jar file.
    exit /b %error level%
)
echo xcopy copy jar file finish.


pause