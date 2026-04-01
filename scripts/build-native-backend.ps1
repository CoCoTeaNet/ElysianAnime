<# 
用途：将本项目的 Java 后端（elysiananime-server/elysiananime-api）打包为 Windows Native EXE。

默认使用 Maven 的 native Profile（文档中提到的：mvn clean package -Pnative）。
如果你的环境/插件不支持，也可以用 -UseCliNativeImage 走 native-image 命令行兜底（与 build-native-windows.bat 同思路）。

前置要求（Windows）：
1) 安装 GraalVM JDK 21，并设置环境变量：
   - GRAALVM_HOME=C:\Program Files\Java\graalvm-jdk-21
   - PATH=%GRAALVM_HOME%\bin;%PATH%
2) 确保 native-image 可用：native-image --version
3) 安装 VS Build Tools（C++ 工具链），否则 native-image 会报缺少编译器
4) 安装 Maven，并确保 mvn 可用

用法示例：
  pwsh ./scripts/build-native-backend.ps1
  pwsh ./scripts/build-native-backend.ps1 -OutputDir release-native-backend
  pwsh ./scripts/build-native-backend.ps1 -UseCliNativeImage
#>

[CmdletBinding()]
param(
  # 输出目录（相对仓库根目录，或绝对路径）
  [string]$OutputDir = "release-native-backend",

  # 如果 Maven -Pnative 不可用，使用 native-image 命令行兜底
  [switch]$UseCliNativeImage
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Info([string]$msg) { Write-Host "[INFO]  $msg" -ForegroundColor Cyan }
function Warn([string]$msg) { Write-Host "[WARN]  $msg" -ForegroundColor Yellow }
function Fail([string]$msg) { Write-Host "[ERROR] $msg" -ForegroundColor Red; throw $msg }

function Assert-Command([string]$name) {
  if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
    Fail "未找到命令：$name。请先安装/配置后再运行。"
  }
}

# 计算仓库根目录：假设脚本位于 <repo>/scripts/ 下
$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$ServerDir = Join-Path $RepoRoot "elysiananime-server"
$ApiDir    = Join-Path $ServerDir "elysiananime-api"
$ApiPom    = Join-Path $ApiDir "pom.xml"

if (-not (Test-Path $ApiPom)) {
  Fail "未找到后端模块 pom.xml：$ApiPom（请确认脚本位于仓库 scripts 目录下）"
}

Assert-Command "mvn"

if (-not $env:GRAALVM_HOME) {
  Fail "未检测到 GRAALVM_HOME 环境变量。请安装 GraalVM JDK 21 并设置 GRAALVM_HOME。"
}

# 优先让本次执行使用 GraalVM 作为 JAVA_HOME，并把其 bin 放到 PATH 头部
$env:JAVA_HOME = $env:GRAALVM_HOME
$graalBin = Join-Path $env:GRAALVM_HOME "bin"
if (Test-Path $graalBin) {
  $env:PATH = "$graalBin;$env:PATH"
}

Assert-Command "java"
Assert-Command "native-image"

Info "RepoRoot      : $RepoRoot"
Info "GRAALVM_HOME  : $env:GRAALVM_HOME"
Info "JAVA_HOME     : $env:JAVA_HOME"

# 计算输出目录路径
$OutPath = $OutputDir
if (-not [System.IO.Path]::IsPathRooted($OutPath)) {
  $OutPath = Join-Path $RepoRoot $OutputDir
}

# 清理并创建输出目录结构（仅后端所需）
if (Test-Path $OutPath) { Remove-Item -Recurse -Force $OutPath }
New-Item -ItemType Directory -Force -Path $OutPath | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $OutPath "config") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $OutPath "logs")   | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $OutPath "files")  | Out-Null

try {
  if (-not $UseCliNativeImage) {
    Info "使用 Maven Profile 'native' 构建（推荐）..."
    Push-Location $ApiDir
    # 说明：NATIVE-BUILD-GUIDE.md 提到可以这样打包：
    # mvn clean package -Pnative -DskipTests
    & mvn clean package -Pnative -DskipTests
    Pop-Location
  } else {
    Warn "使用 native-image 命令行兜底构建（不走 Maven -Pnative）..."
    # 1) 先构建 jar（目标：elysiananime-server/elysiananime-api/target/elysiananime.jar）
    Push-Location $ServerDir
    & mvn -f (Join-Path $ServerDir "pom.xml") clean package -pl elysiananime-api -am -DskipTests -Dmaven.test.skip=true
    Pop-Location

    $JarPath = Join-Path $ApiDir "target/elysiananime.jar"
    if (-not (Test-Path $JarPath)) {
      Fail "未找到构建产物 JAR：$JarPath"
    }

    Copy-Item -Force $JarPath (Join-Path $OutPath "elysiananime.jar")

    Push-Location $OutPath
    Info "native-image 构建中（可能需要几分钟）..."
    & native-image `
      --no-fallback `
      --allow-incomplete-classpath `
      --report-unsupported-elements-at-runtime `
      "-H:+ReportExceptionStackTraces" `
      "-H:IncludeResources=.*\.xml$" `
      "-H:IncludeResources=.*\.properties$" `
      "-H:IncludeResources=.*\.json$" `
      "-H:Name=elysiananime" `
      -cp "elysiananime.jar" `
      "net.cocotea.elysiananime.Launcher"
    Pop-Location
  }
}
catch {
  Fail "构建失败：$($_.Exception.Message)"
}

# 找到 exe（优先在 target 下找；找不到则在输出目录找）
$TargetDir = Join-Path $ApiDir "target"
$exe = $null

if (Test-Path $TargetDir) {
  $exe = Get-ChildItem -Path $TargetDir -Filter "*.exe" -Recurse -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
}

if (-not $exe -and (Test-Path $OutPath)) {
  $exe = Get-ChildItem -Path $OutPath -Filter "*.exe" -Recurse -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
}

if (-not $exe) {
  Warn "没有在 target/ 或输出目录中找到 *.exe。"
  Warn "如果你使用的是 Maven -Pnative：请检查 native 插件配置/输出路径。"
  Warn "你也可以尝试：pwsh ./scripts/build-native-backend.ps1 -UseCliNativeImage"
  exit 2
}

Copy-Item -Force $exe.FullName (Join-Path $OutPath "elysiananime.exe")

# 拷贝配置（如果存在）
$AppYml = Join-Path $RepoRoot "release/config/app.yml"
if (Test-Path $AppYml) {
  Copy-Item -Force $AppYml (Join-Path $OutPath "config/app.yml")
} else {
  Warn "未找到 release/config/app.yml，将使用默认配置（如程序内置）。"
}

Info "构建完成："
Info "  EXE   -> $(Join-Path $OutPath 'elysiananime.exe')"
Info "  配置  -> $(Join-Path $OutPath 'config/app.yml')（如存在）"
Info "运行方式（示例）："
Info "  cd `"$OutPath`""
Info "  .\\elysiananime.exe"

