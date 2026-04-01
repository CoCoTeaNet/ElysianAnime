#!/usr/bin/env bash
set -euo pipefail

# 用途：
# - 给 GitHub Actions 调用：在「push tag」时构建前端、后端、ZIP 包，并构建 Docker 镜像（不 push），导出为可下载的 tar.gz
# - 也可本地手动执行（需本机已安装：git、node/yarn、maven、docker）
#
# 运行位置：仓库根目录

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

# 版本号：优先使用 GitHub Actions 的 tag 名，其次使用参数，其次尝试 git tag
VERSION="${1:-${GITHUB_REF_NAME:-}}"
if [[ -z "${VERSION}" ]]; then
  VERSION="$(git describe --tags --abbrev=0 2>/dev/null || true)"
fi
if [[ -z "${VERSION}" ]]; then
  echo "ERROR: 无法确定 VERSION。请在 tag 环境运行，或执行：$0 v3.0.0" >&2
  exit 1
fi

echo "[release] VERSION=${VERSION}"

############################
# 1) 构建前端（Vue）
############################
echo "[release] Build frontend..."
pushd elysiananime-ui >/dev/null

# Node 16+ 通常自带 corepack；在 CI 里建议先 enable，避免 yarn 不存在
if command -v corepack >/dev/null 2>&1; then
  corepack enable || true
fi

yarn install --frozen-lockfile
VITE_API_URL="/api" yarn build
popd >/dev/null

############################
# 2) 构建后端（Java/Maven）
############################
echo "[release] Build backend..."
pushd elysiananime-server >/dev/null
mvn -B package -DskipTests
popd >/dev/null

############################
# 3) 打 ZIP 发布包（与现有 workflow 一致）
############################
echo "[release] Prepare release directory..."
rm -rf ElysianAnime-Release
mkdir -p ElysianAnime-Release/{backend,frontend}

cp elysiananime-server/elysiananime-api/target/*.jar ElysianAnime-Release/backend/
cp elysiananime-server/elysiananime-api/target/classes/app.yml ElysianAnime-Release/backend/
cp -r elysiananime-ui/dist/* ElysianAnime-Release/frontend/

cp doc/bin/start.bat ElysianAnime-Release/backend/
cp doc/bin/stop.bat ElysianAnime-Release/backend/
cp doc/bin/start.sh ElysianAnime-Release/backend/

cp README.md ElysianAnime-Release/ 2>/dev/null || true

ZIP_NAME="ElysianAnime-${VERSION}.zip"
rm -f "${ZIP_NAME}"
echo "[release] Create zip: ${ZIP_NAME}"
zip -r "${ZIP_NAME}" ElysianAnime-Release >/dev/null

############################
# 4) 构建 Docker 镜像（不 push，仅导出）
############################
echo "[release] Build docker image (no push)..."

IMAGE_REPO="elysiananime"
IMAGE_TAG="${IMAGE_REPO}:${VERSION}"

docker build -t "${IMAGE_TAG}" .

IMAGE_TAR="ElysianAnime-${VERSION}-docker-image.tar"
IMAGE_TGZ="${IMAGE_TAR}.gz"
rm -f "${IMAGE_TAR}" "${IMAGE_TGZ}"

echo "[release] Export docker image: ${IMAGE_TGZ}"
docker save "${IMAGE_TAG}" -o "${IMAGE_TAR}"
gzip -9 "${IMAGE_TAR}"

echo "[release] Done."
echo "[release] Outputs:"
echo "  - ${ZIP_NAME}"
echo "  - ${IMAGE_TGZ}"

