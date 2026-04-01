# ElysianAnime Docker 初始化脚本
# 用于将 SQL 文件复制到 Docker 初始化目录

$ErrorActionPreference = "Stop"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "ElysianAnime Docker 数据库初始化准备" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

# 获取脚本所在目录
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$sqlSourceDir = Join-Path $scriptDir "doc\sql"
$dockerInitDir = Join-Path $scriptDir "docker\mysql\init"

# 检查源目录是否存在
if (-not (Test-Path $sqlSourceDir)) {
    Write-Host "错误：找不到 SQL 文件目录：$sqlSourceDir" -ForegroundColor Red
    exit 1
}

# 创建 Docker 初始化目录（如果不存在）
if (-not (Test-Path $dockerInitDir)) {
    Write-Host "创建 Docker 初始化目录：$dockerInitDir" -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path $dockerInitDir | Out-Null
}

# 复制 SQL 文件
Write-Host "`n正在复制 SQL 文件..." -ForegroundColor Green

$files = @(
    "ddl-sql.sql",
    "ddl-update-sql.sql",
    "data-sql.sql"
)

foreach ($file in $files) {
    $sourceFile = Join-Path $sqlSourceDir $file
    $destFile = Join-Path $dockerInitDir $file
    
    if (Test-Path $sourceFile) {
        Copy-Item -Path $sourceFile -Destination $destFile -Force
        Write-Host "  ✓ 已复制：$file" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 文件不存在：$file" -ForegroundColor Red
    }
}

Write-Host "`n=====================================" -ForegroundColor Cyan
Write-Host "初始化准备完成！" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "`n下一步操作：" -ForegroundColor Yellow
Write-Host "1. 确保配置文件已就绪：" -ForegroundColor White
Write-Host "   - 复制 elysiananime-server\src\main\resources\app.yml 到 docker\app\config\app.yml" -ForegroundColor Gray
Write-Host "   - 修改 app.yml 中的数据库连接为：jdbc:mysql://mysql:3306/elysiananime" -ForegroundColor Gray
Write-Host ""
Write-Host "2. 启动 Docker 容器：" -ForegroundColor White
Write-Host "   docker-compose up -d" -ForegroundColor Gray
Write-Host ""
Write-Host "3. 查看启动日志：" -ForegroundColor White
Write-Host "   docker-compose logs -f mysql" -ForegroundColor Gray
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
