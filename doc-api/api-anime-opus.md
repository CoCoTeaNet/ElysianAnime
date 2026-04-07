# 番剧作品管理接口

## 1. 上传作品资源

### 接口信息
- **接口名称**: 上传作品资源
- **请求方法**: `POST`
- **请求路径**: `/api/anime/opus/{opusId}/uploadRes`
- **接口描述**: 上传番剧作品的视频资源文件
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `multipart/form-data`

### 请求参数

**Path 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| opusId | BigInteger | 是 | 作品ID |

**Form Data 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 视频文件 |

### 请求示例
```
POST /api/anime/opus/1/uploadRes
Content-Type: multipart/form-data

file: [视频文件]
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "episode_01.mp4"
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "上传作品资源",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "上传作品资源",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "body": {
          "mode": "formdata",
          "formdata": [
            {
              "key": "file",
              "type": "file",
              "src": "/path/to/video.mp4"
            }
          ]
        },
        "url": {
          "raw": "{{baseUrl}}/api/anime/opus/1/uploadRes",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "opus", "1", "uploadRes"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 2. 从Bangumi添加作品

### 接口信息
- **接口名称**: 添加作品信息,来源是Bangumi
- **请求方法**: `POST`
- **请求路径**: `/api/anime/opus/addOpusFromBangumi`
- **接口描述**: 从 Bangumi 网站抓取番剧信息并添加到系统
- **权限要求**: 需要登录
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| bgmUrl | String | 是 | Bangumi 作品页面URL |
| isCover | Integer | 否 | 是否覆盖已有作品：0否 1是 |

### 请求示例
```json
{
  "bgmUrl": "https://bgm.tv/subject/123456",
  "isCover": 0
}
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": 1
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "从Bangumi添加作品",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "添加作品信息,来源是Bangumi",
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
          "raw": "{\n  \"bgmUrl\": \"https://bgm.tv/subject/123456\",\n  \"isCover\": 0\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/anime/opus/addOpusFromBangumi",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "opus", "addOpusFromBangumi"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 3. 新增作品

### 接口信息
- **接口名称**: 新增作品
- **请求方法**: `POST`
- **请求路径**: `/api/anime/opus/add`
- **接口描述**: 手动创建新的番剧作品
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| opusName | String | 是 | 作品名称 |
| opusNameCn | String | 否 | 中文名称 |
| coverImage | String | 否 | 封面图片 |
| introduction | String | 否 | 简介 |
| airTime | String | 否 | 播出时间 |
| episodeCount | Integer | 否 | 总集数 |
| status | Integer | 否 | 状态：0连载中 1已完结 |

### 请求示例
```json
{
  "opusName": "Test Anime",
  "opusNameCn": "测试动画",
  "introduction": "这是一部测试动画",
  "airTime": "2024-01-01",
  "episodeCount": 12,
  "status": 0
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
    "name": "新增作品",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "新增作品",
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
          "raw": "{\n  \"opusName\": \"Test Anime\",\n  \"opusNameCn\": \"测试动画\",\n  \"introduction\": \"这是一部测试动画\",\n  \"airTime\": \"2024-01-01\",\n  \"episodeCount\": 12,\n  \"status\": 0\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/anime/opus/add",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "opus", "add"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 4. 批量删除作品

### 接口信息
- **接口名称**: 批量删除作品
- **请求方法**: `POST`
- **请求路径**: `/api/anime/opus/deleteBatch`
- **接口描述**: 批量删除多个番剧作品
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON Array)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| - | Array | 是 | 作品ID列表 |

### 请求示例
```json
[1, 2, 3]
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
    "name": "批量删除作品",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "批量删除作品",
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
          "raw": "[1, 2, 3]"
        },
        "url": {
          "raw": "{{baseUrl}}/api/anime/opus/deleteBatch",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "opus", "deleteBatch"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 5. 更新作品信息

### 接口信息
- **接口名称**: 更新作品信息
- **请求方法**: `POST`
- **请求路径**: `/api/anime/opus/update`
- **接口描述**: 更新已有番剧作品的信息
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | BigInteger | 是 | 作品ID |
| opusName | String | 否 | 作品名称 |
| opusNameCn | String | 否 | 中文名称 |
| coverImage | String | 否 | 封面图片 |
| introduction | String | 否 | 简介 |
| airTime | String | 否 | 播出时间 |
| episodeCount | Integer | 否 | 总集数 |
| status | Integer | 否 | 状态：0连载中 1已完结 |

### 请求示例
```json
{
  "id": 1,
  "opusName": "新作品名称",
  "status": 1
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
    "name": "更新作品",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "更新作品信息",
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
          "raw": "{\n  \"id\": 1,\n  \"opusName\": \"新作品名称\",\n  \"status\": 1\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/anime/opus/update",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "opus", "update"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 6. 分页查询作品

### 接口信息
- **接口名称**: 分页查询作品信息
- **请求方法**: `POST`
- **请求路径**: `/api/anime/opus/listByPage`
- **接口描述**: 分页查询番剧作品列表，支持条件筛选
- **权限要求**: 需要角色 `role:super:admin`、`role:simple:admin` 或 `bangumi:rss:subscriber`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | 是 | 页码，从1开始 |
| pageSize | Integer | 是 | 每页条数 |
| aniOpus | Object | 是 | 查询条件对象 |
| aniOpus.opusName | String | 否 | 作品名称（模糊查询） |
| aniOpus.status | Integer | 否 | 状态：0连载中 1已完结 |

### 请求示例
```json
{
  "pageNo": 1,
  "pageSize": 10,
  "aniOpus": {
    "opusName": "测试",
    "status": 0
  }
}
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNo": 1,
    "pageSize": 10,
    "total": 50,
    "pages": 5,
    "records": [
      {
        "id": 1,
        "opusName": "Test Anime",
        "opusNameCn": "测试动画",
        "coverImage": "cover.jpg",
        "episodeCount": 12,
        "status": 0,
        "createTime": "2024-01-01 00:00:00"
      }
    ]
  }
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "分页查询作品",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "分页查询作品信息",
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
          "raw": "{\n  \"pageNo\": 1,\n  \"pageSize\": 10,\n  \"aniOpus\": {\n    \"opusName\": \"测试\",\n    \"status\": 0\n  }\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/anime/opus/listByPage",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "opus", "listByPage"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 7. 查询用户作品列表

### 接口信息
- **接口名称**: 查找作品，并关联用户
- **请求方法**: `POST`
- **请求路径**: `/api/anime/opus/listByUser`
- **接口描述**: 查询当前用户的番剧列表，按观看状态筛选
- **权限要求**: 需要登录
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | 是 | 页码，从1开始 |
| pageSize | Integer | 是 | 每页条数 |
| status | Array | 是 | 观看状态列表：NOT_READ/READING/IS_READ |

### 请求示例
```json
{
  "pageNo": 1,
  "pageSize": 10,
  "status": ["READING", "NOT_READ"]
}
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNo": 1,
    "pageSize": 10,
    "total": 20,
    "pages": 2,
    "records": [
      {
        "id": 1,
        "opusName": "Test Anime",
        "coverImage": "cover.jpg",
        "userStatus": "READING",
        "progress": 5,
        "totalEpisodes": 12
      }
    ]
  }
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "查询用户作品列表",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "查找作品，并关联用户",
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
          "raw": "{\n  \"pageNo\": 1,\n  \"pageSize\": 10,\n  \"status\": [\"READING\", \"NOT_READ\"]\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/anime/opus/listByUser",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "opus", "listByUser"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 8. 获取作品媒体信息

### 接口信息
- **接口名称**: 获取作品详情
- **请求方法**: `GET`
- **请求路径**: `/api/anime/opus/getOpusMedia/{opusId}`
- **接口描述**: 获取指定作品的视频资源信息
- **权限要求**: 需要登录

### 请求参数

**Path 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| opusId | BigInteger | 是 | 作品ID |

### 请求示例
```
GET /api/anime/opus/getOpusMedia/1
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "opusId": 1,
    "opusName": "Test Anime",
    "videos": [
      {
        "episode": 1,
        "fileName": "episode_01.mp4",
        "duration": 1440
      }
    ]
  }
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "获取作品媒体信息",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取作品详情",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/anime/opus/getOpusMedia/1",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "opus", "getOpusMedia", "1"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 9. 获取作品媒体流

### 接口信息
- **接口名称**: 获取资源媒体流
- **请求方法**: `GET`
- **请求路径**: `/api/anime/opus/media/{opusId}`
- **接口描述**: 获取作品视频文件的媒体流，用于在线播放
- **权限要求**: 需要登录

### 请求参数

**Path 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| opusId | BigInteger | 是 | 作品ID |

**Query 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| resName | String | 是 | 资源文件名 |

### 请求示例
```
GET /api/anime/opus/media/1?resName=episode_01.mp4
```

### 响应说明
返回视频文件的二进制流，可直接用于视频播放器。

### Postman 导入格式
```json
{
  "info": {
    "name": "获取作品媒体流",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取资源媒体流",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/anime/opus/media/1?resName=episode_01.mp4",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "opus", "media", "1"],
          "query": [
            {
              "key": "resName",
              "value": "episode_01.mp4"
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

## 10. 获取作品封面

### 接口信息
- **接口名称**: 获取作品封面
- **请求方法**: `GET`
- **请求路径**: `/api/anime/opus/cover`
- **接口描述**: 获取作品封面图片
- **权限要求**: 无需认证

### 请求参数

**Query 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| resName | String | 是 | 封面文件名 |
| w | Integer | 否 | 宽度（可选，用于缩放） |
| h | Integer | 否 | 高度（可选，用于缩放） |

### 请求示例
```
GET /api/anime/opus/cover?resName=cover.jpg&w=300&h=400
```

### 响应说明
返回图片文件的二进制流。

### Postman 导入格式
```json
{
  "info": {
    "name": "获取作品封面",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取作品封面",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{baseUrl}}/api/anime/opus/cover?resName=cover.jpg&w=300&h=400",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "opus", "cover"],
          "query": [
            {
              "key": "resName",
              "value": "cover.jpg"
            },
            {
              "key": "w",
              "value": "300"
            },
            {
              "key": "h",
              "value": "400"
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

## 11. 获取背景图片

### 接口信息
- **接口名称**: 获取背景图片
- **请求方法**: `GET`
- **请求路径**: `/api/anime/opus/getBackground`
- **接口描述**: 获取系统背景图片，可指定或随机
- **权限要求**: 无需认证

### 请求参数

**Query 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| resName | String | 否 | 背景文件名（不传则随机返回） |

### 请求示例
```
GET /api/anime/opus/getBackground
GET /api/anime/opus/getBackground?resName=bg1.jpg
```

### 响应说明
返回图片文件的二进制流。

### Postman 导入格式
```json
{
  "info": {
    "name": "获取背景图片",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取背景图片",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{baseUrl}}/api/anime/opus/getBackground?resName=bg1.jpg",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "opus", "getBackground"],
          "query": [
            {
              "key": "resName",
              "value": "bg1.jpg"
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
作品管理相关接口的权限要求：
- `role:super:admin`: 超级管理员
- `role:simple:admin`: 普通管理员
- `bangumi:rss:subscriber`: RSS订阅者
