<#
用途：
  在本地一键创建并推送 Git Tag，用于触发 GitHub Actions（push tag）工作流，从而自动构建并发布 Release。

推荐用法（最安全，要求工作区干净）：
  pwsh ./scripts/publish-tag.ps1 v3.0.0

如果你希望脚本自动提交当前改动（会 git add -A 并 commit），再打 tag：
  pwsh ./scripts/publish-tag.ps1 v3.0.0 -AutoCommit -Message "release: v3.0.0"

可选：同时推送当前分支（一般不需要，tag push 就会触发 workflow）
  pwsh ./scripts/publish-tag.ps1 v3.0.0 -PushBranch

注意：
  - 你的 GitHub Actions workflow 是按 “push tag” 触发的，所以关键动作是：git push origin <tag>
  - 默认不做任何“危险操作”（不自动提交、不自动删 tag、不强推），避免误操作
#>

[CmdletBinding()]
param(
  # 例如：v3.0.0 或 3.0.0（取决于你的 workflow tag 规则）
  [Parameter(Mandatory = $true, Position = 0)]
  [string]$Tag,

  # 远端名称，默认 origin
  [string]$Remote = "origin",

  # 是否自动提交当前改动（git add -A && git commit）
  [switch]$AutoCommit,

  # 自动提交时使用的提交信息
  [string]$Message = "",

  # 是否同时推送当前分支
  [switch]$PushBranch
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

Assert-Command "git"

# 确认在 git 仓库内
& git rev-parse --is-inside-work-tree *> $null
if ($LASTEXITCODE -ne 0) { Fail "当前目录不是 Git 仓库根目录/子目录。请在仓库内运行该脚本。" }

# 基础信息
$branch = (& git rev-parse --abbrev-ref HEAD).Trim()
if (-not $branch) { Fail "无法获取当前分支名。" }
Info "当前分支：$branch"
Info "远端：$Remote"
Info "Tag：$Tag"

# 可选：简单校验 tag（按需放宽）
# 你的 workflow 支持 v?x.y.z；这里仅做提醒，不强制拦截
if ($Tag -notmatch '^v?\d+\.\d+\.\d+([\-+].+)?$') {
  Warn "Tag 看起来不是常见 SemVer（vX.Y.Z）。如果你的 workflow 只匹配特定格式，可能不会触发。"
}

# 拉取远端信息，确保 tag 冲突能提前发现
Info "git fetch --prune --tags $Remote"
& git fetch --prune --tags $Remote

# 检查 tag 是否已存在（本地或远端）
& git show-ref --tags --verify --quiet "refs/tags/$Tag"
if ($LASTEXITCODE -eq 0) { Fail "本地已存在 tag：$Tag（请更换 tag 或手动删除后再试）" }

# 检查远端是否已有同名 tag
$remoteTag = (& git ls-remote --tags $Remote "refs/tags/$Tag" 2>$null)
if ($remoteTag) { Fail "远端 $Remote 已存在 tag：$Tag（请更换 tag）" }

# 检查工作区状态
$status = (& git status --porcelain).Trim()
if ($status) {
  if (-not $AutoCommit) {
    Fail "检测到未提交改动。请先提交/暂存，或使用 -AutoCommit 让脚本自动提交。"
  }

  if (-not $Message) {
    $Message = "release: $Tag"
  }

  Info "自动提交改动：git add -A"
  & git add -A

  Info "git commit -m `"$Message`""
  & git commit -m $Message
  if ($LASTEXITCODE -ne 0) {
    Fail "git commit 失败。请检查是否没有实际改动可提交，或存在 pre-commit hook 报错。"
  }
} else {
  Info "工作区干净：无未提交改动"
}

if ($PushBranch) {
  Info "推送当前分支到远端：git push $Remote $branch"
  & git push $Remote $branch
}

# 创建注解 tag（推荐；GitHub Release 通常更友好）
Info "创建注解 tag：git tag -a $Tag -m `"Release $Tag`""
& git tag -a $Tag -m "Release $Tag"

Info "推送 tag：git push $Remote $Tag"
& git push $Remote $Tag

Info "完成。现在 GitHub Actions 应该已被 tag push 触发。"
Info "你可以到 GitHub 仓库的 Actions 页面查看运行情况，成功后会在 Releases 里看到产物。"

