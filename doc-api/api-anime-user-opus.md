# 用户番剧追番接口

## 1. 追番/取消追番

### 接口信息
- **接口名称**: 追番/取消追番
- **请求方法**: `POST`
- **请求路径**: `/api/anime/userOpus/{opusId}/follow`
- **接口描述**: 关注或取消关注指定番剧作品
- **权限要求**: 需要登录

### 请求参数

**Path 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| opusId | BigInteger | 是 | 作品ID |

### 请求示例
```
POST /api/anime/userOpus/1/follow
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
    "name": "追番/取消追番",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "追番/取消追番",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/anime/userOpus/1/follow",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "userOpus", "1", "follow"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 2. 查询用户追番列表

### 接口信息
- **接口名称**: 查询用户追番列表
- **请求方法**: `POST`
- **请求路径**: `/api/anime/userOpus/listByUser`
- **接口描述**: 获取当前用户的追番列表
- **权限要求**: 需要登录
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | 是 | 页码，从1开始 |
| pageSize | Integer | 是 | 每页条数 |
| status | String | 否 | 观看状态：NOT_READ/READING/IS_READ |

### 请求示例
```json
{
  "pageNo": 1,
  "pageSize": 10,
  "status": "READING"
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
        "opusId": 1,
        "opusName": "测试动画",
        "coverImage": "cover.jpg",
        "status": "READING",
        "progress": 5,
        "totalEpisodes": 12,
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
    "name": "查询用户追番列表",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "查询用户追番列表",
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
          "raw": "{\n  \"pageNo\": 1,\n  \"pageSize\": 10,\n  \"status\": \"READING\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/anime/userOpus/listByUser",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "userOpus", "listByUser"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 3. 更新追番信息

### 接口信息
- **接口名称**: 更新追番信息
- **请求方法**: `POST`
- **请求路径**: `/api/anime/userOpus/update`
- **接口描述**: 更新用户对番剧的观看状态等信息
- **权限要求**: 需要登录
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| opusId | BigInteger | 是 | 作品ID |
| status | String | 否 | 观看状态：NOT_READ/READING/IS_READ |
| score | Integer | 否 | 评分（1-10） |
| comment | String | 否 | 评论 |

### 请求示例
```json
{
  "opusId": 1,
  "status": "IS_READ",
  "score": 9,
  "comment": "非常好看的动画！"
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
    "name": "更新追番信息",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "更新追番信息",
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
          "raw": "{\n  \"opusId\": 1,\n  \"status\": \"IS_READ\",\n  \"score\": 9,\n  \"comment\": \"非常好看的动画！\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/anime/userOpus/update",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "userOpus", "update"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 4. 更新观看进度

### 接口信息
- **接口名称**: 更新观看进度
- **请求方法**: `POST`
- **请求路径**: `/api/anime/userOpus/updateProgress`
- **接口描述**: 更新用户对番剧的观看进度
- **权限要求**: 需要登录
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| opusId | BigInteger | 是 | 作品ID |
| progress | Integer | 是 | 观看进度（集数） |

### 请求示例
```json
{
  "opusId": 1,
  "progress": 5
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
    "name": "更新观看进度",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "更新观看进度",
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
          "raw": "{\n  \"opusId\": 1,\n  \"progress\": 5\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/anime/userOpus/updateProgress",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "userOpus", "updateProgress"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 5. 分享追番作品

### 接口信息
- **接口名称**: 分享自己追番的作品
- **请求方法**: `POST`
- **请求路径**: `/api/anime/userOpus/share/{opusId}`
- **接口描述**: 将自己追番的作品分享给其他用户
- **权限要求**: 需要登录

### 请求参数

**Path 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| opusId | BigInteger | 是 | 作品ID |

### 请求示例
```
POST /api/anime/userOpus/share/1
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
    "name": "分享追番作品",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "分享自己追番的作品",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/anime/userOpus/share/1",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "userOpus", "share", "1"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 6. 获取用户分享的番剧列表

### 接口信息
- **接口名称**: 获取用户分享的番剧
- **请求方法**: `GET`
- **请求路径**: `/api/anime/userOpus/shares/list`
- **接口描述**: 获取用户分享的番剧列表
- **权限要求**: 需要登录

### 请求参数

**Query 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limits | Integer | 是 | 限制返回条数（1-1000） |

### 请求示例
```
GET /api/anime/userOpus/shares/list?limits=10
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "opusId": 1,
      "opusName": "测试动画",
      "coverImage": "cover.jpg",
      "username": "admin",
      "score": 9,
      "comment": "非常好看！",
      "shareTime": "2024-01-01 12:00:00"
    }
  ]
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "获取用户分享的番剧列表",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取用户分享的番剧",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/anime/userOpus/shares/list?limits=10",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "userOpus", "shares", "list"],
          "query": [
            {
              "key": "limits",
              "value": "10"
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

### 观看状态说明
- `NOT_READ`: 未观看
- `READING`: 观看中
- `IS_READ`: 已看完

### 评分说明
- 评分范围：1-10分
- 10分为满分
