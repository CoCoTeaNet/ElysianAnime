```markdown
# ElysianAnime 镜像构建与发布

## 变量定义（执行前必须填写）

| 变量 | 值 | 说明 |
|------|------|------|
| `IMAGE_NAME` | `cocoteanet/elysiananime` | 镜像名称 |
| `VERSION` | `<填入版本号，如 v3.1.2>` | 本次发布版本 |
| `REGISTRY` | `<填入仓库域名，Docker Hub 留空>` | 仓库地址 |

## 执行步骤

### Step 1: 设置变量

```bash
IMAGE_NAME="cocoteanet/elysiananime"
VERSION="<填入版本号>"
REGISTRY="<填入仓库域名，Docker Hub 留空>"
FULL_IMAGE="${REGISTRY:+${REGISTRY}/}${IMAGE_NAME}"
```

### Step 2: 构建镜像

```bash
docker build \
  --build-arg APP_VERSION=${VERSION} \
  -t ${FULL_IMAGE}:${VERSION} \
  -t ${FULL_IMAGE}:latest \
  .
```

### Step 3: 验证构建结果

```bash
docker images ${FULL_IMAGE} --format "table {{.Tag}}\t{{.ID}}"
```

✅ 确认 `VERSION` 和 `latest` 两个标签的 IMAGE ID 相同。

### Step 4: 推送

```bash
docker push --all-tags ${FULL_IMAGE}
```

### Step 5: 验证推送

```bash
docker pull ${FULL_IMAGE}:${VERSION}
```

## 异常处理

- **构建失败**：检查 Dockerfile 语法和依赖文件是否存在。
- **推送失败**：确认已执行 `docker login`。
- **遗漏标签**：执行以下命令补打并推送：

```bash
docker tag ${FULL_IMAGE}:${VERSION} ${FULL_IMAGE}:latest
docker push ${FULL_IMAGE}:latest
```
```