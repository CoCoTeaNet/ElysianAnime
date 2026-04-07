# 番剧RSS订阅接口

## 1. 提交番剧订阅

### 接口信息
- **接口名称**: 提交番剧订阅
- **请求方法**: `POST`
- **请求路径**: `/api/anime/rss/subscribe`
- **接口描述**: 为指定番剧添加RSS订阅，自动下载更新
- **权限要求**: 需要角色 `role:super:admin`、`role:simple:admin` 或 `bangumi:rss:subscriber`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| opusId | BigInteger | 是 | 作品ID |
| rssUrl | String | 是 | RSS订阅地址 |
| renameRule | String | 否 | 重命名规则 |
| exclusion | String | 否 | 排除关键词 |

### 请求示例
```json
{
  "opusId": 1,
  "rssUrl": "https://mikanani.me/RSS/Bangumi?bangumiId=123456",
  "renameRule": "{title}_{episode}",
  "exclusion": "720P,BIG5"
}
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": true
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "提交番剧订阅",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "提交番剧订阅",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"opusId\": 1,\n  \"rssUrl\": \"https://mikanani.me/RSS/Bangumi?bangumiId=123456\",\n  \"renameRule\": \"{title}_{episode}\",\n  \"exclusion\": \"720P,BIG5\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/anime/rss/subscribe",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "rss", "subscribe"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 2. 关闭番剧订阅

### 接口信息
- **接口名称**: 关闭番剧订阅
- **请求方法**: `POST`
- **请求路径**: `/api/anime/rss/{opusId}/closeSubscribe`
- **接口描述**: 关闭指定番剧的RSS订阅
- **权限要求**: 需要角色 `role:super:admin`、`role:simple:admin` 或 `bangumi:rss:subscriber`

### 请求参数

**Path 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| opusId | BigInteger | 是 | 作品ID |

### 请求示例
```
POST /api/anime/rss/1/closeSubscribe
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": true
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "关闭番剧订阅",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "关闭番剧订阅",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/anime/rss/1/closeSubscribe",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "rss", "1", "closeSubscribe"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 3. 获取RSS资源详细信息

### 接口信息
- **接口名称**: 获取资源信息详细信息
- **请求方法**: `GET`
- **请求路径**: `/api/anime/rss/getMkXmlDetail`
- **接口描述**: 解析RSS XML文件，获取资源详细信息
- **权限要求**: 需要角色 `role:super:admin`、`role:simple:admin` 或 `bangumi:rss:subscriber`

### 请求参数

**Query 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| rssUrl | String | 是 | RSS订阅地址 |

### 请求示例
```
GET /api/anime/rss/getMkXmlDetail?rssUrl=https://mikanani.me/RSS/Bangumi?bangumiId=123456
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "title": "测试动画",
    "episodes": [
      {
        "episode": 1,
        "title": "测试动画 第01话",
        "link": "magnet:?xt=urn:btih:xxx",
        "size": "500MB",
        "publishDate": "2024-01-01"
      }
    ]
  }
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "获取RSS资源详细信息",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取资源信息详细信息",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/anime/rss/getMkXmlDetail?rssUrl=https://mikanani.me/RSS/Bangumi?bangumiId=123456",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "rss", "getMkXmlDetail"],
          "query": [
            {
              "key": "rssUrl",
              "value": "https://mikanani.me/RSS/Bangumi?bangumiId=123456"
            }
          ]
        }
      },
      "response": []
    }
  ]
}
```

---

## 4. 获取RSS执行状态

### 接口信息
- **接口名称**: 获取RSS执行状态
- **请求方法**: `GET`
- **请求路径**: `/api/anime/rss/getRssWorkStatus`
- **接口描述**: 获取RSS订阅任务的执行状态和统计信息
- **权限要求**: 需要登录

### 请求参数
无

### 请求示例
```
GET /api/anime/rss/getRssWorkStatus
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "totalSubscriptions": 10,
    "activeSubscriptions": 8,
    "lastRunTime": "2024-01-01 12:00:00",
    "nextRunTime": "2024-01-01 13:00:00",
    "status": "running"
  }
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "获取RSS执行状态",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取RSS执行状态",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/anime/rss/getRssWorkStatus",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "rss", "getRssWorkStatus"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 5. 获取重命名结果预览

### 接口信息
- **接口名称**: 根据配置的规则获取重命名结果
- **请求方法**: `POST`
- **请求路径**: `/api/anime/rss/getRenames`
- **接口描述**: 根据RSS规则预览文件重命名结果
- **权限要求**: 需要登录
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| opusId | BigInteger | 是 | 作品ID |
| rssUrl | String | 是 | RSS订阅地址 |
| renameRule | String | 否 | 重命名规则 |

### 请求示例
```json
{
  "opusId": 1,
  "rssUrl": "https://mikanani.me/RSS/Bangumi?bangumiId=123456",
  "renameRule": "{title}_{episode}"
}
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "originalName": "测试动画 第01话 [1080P].mp4",
      "newName": "测试动画_01.mp4",
      "episode": 1
    }
  ]
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "获取重命名结果预览",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "根据配置的规则获取重命名结果",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"opusId\": 1,\n  \"rssUrl\": \"https://mikanani.me/RSS/Bangumi?bangumiId=123456\",\n  \"renameRule\": \"{title}_{episode}\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/anime/rss/getRenames",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "rss", "getRenames"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 6. 获取默认排除选项

### 接口信息
- **接口名称**: 获取默认排除的选项
- **请求方法**: `GET`
- **请求路径**: `/api/anime/rss/defaultExclusions`
- **接口描述**: 获取系统默认的RSS排除关键词列表
- **权限要求**: 需要登录

### 请求参数
无

### 请求示例
```
GET /api/anime/rss/defaultExclusions
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    "720P",
    "BIG5",
    "GB",
    "简繁"
  ]
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "获取默认排除选项",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取默认排除的选项",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/anime/rss/defaultExclusions",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "rss", "defaultExclusions"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 7. 添加作品种子

### 接口信息
- **接口名称**: 添加作品种子
- **请求方法**: `POST`
- **请求路径**: `/api/anime/rss/addOpusTorrent`
- **接口描述**: 手动添加种子到作品目录进行下载
- **权限要求**: 需要登录
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| opusId | BigInteger | 是 | 作品ID |
| torrentUrl | String | 是 | 种子链接（magnet或http） |
| episode | Integer | 否 | 集数 |

### 请求示例
```json
{
  "opusId": 1,
  "torrentUrl": "magnet:?xt=urn:btih:xxx",
  "episode": 1
}
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "种子添加成功"
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "添加作品种子",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "添加作品种子（将资源下载到作品目录）",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"opusId\": 1,\n  \"torrentUrl\": \"magnet:?xt=urn:btih:xxx\",\n  \"episode\": 1\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/anime/rss/addOpusTorrent",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "rss", "addOpusTorrent"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 8. 获取RSS统计情况

### 接口信息
- **接口名称**: 获取RSS统计情况
- **请求方法**: `GET`
- **请求路径**: `/api/anime/rss/getCounts`
- **接口描述**: 获取RSS订阅的统计数据
- **权限要求**: 需要登录

### 请求参数
无

### 请求示例
```
GET /api/anime/rss/getCounts
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "totalSubscriptions": 10,
    "activeSubscriptions": 8,
    "downloadedEpisodes": 50,
    "pendingEpisodes": 5
  }
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "获取RSS统计情况",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取RSS统计情况",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/anime/rss/getCounts",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "rss", "getCounts"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 通用说明

### 响应数据结构
所有接口统一返回以下结构：
```json
{
  "code": 200,      // 响应码，200表示成功
  "msg": "操作成功", // 响应消息
  "data": {}        // 响应数据
}
```

### 常见响应码
- `200`: 操作成功
- `401`: 未登录或 token 失效
- `403`: 无权限访问
- `500`: 服务器内部错误

### 环境变量
在 Postman 中设置环境变量：
- `baseUrl`: 后端服务地址，例如 `http://localhost:8080`
- `token`: 登录后获取的 token 值

### 权限说明
RSS订阅相关接口的权限要求：
- `role:super:admin`: 超级管理员
- `role:simple:admin`: 普通管理员
- `bangumi:rss:subscriber`: RSS订阅者

### RSS重命名规则说明
支持的变量：
- `{title}`: 作品标题
- `{episode}`: 集数
- `{original}`: 原始文件名

示例：
- `{title}_{episode}` → "测试动画_01.mp4"
- `{title}_EP{episode}` → "测试动画_EP01.mp4"
