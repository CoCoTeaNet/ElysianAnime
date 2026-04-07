# 系统通知接口

## 1. 按类型获取通知列表

### 接口信息
- **接口名称**: 按类型获取通知列表
- **请求方法**: `GET`
- **请求路径**: `/api/anime/notify/listByType`
- **接口描述**: 根据通知类型获取通知列表
- **权限要求**: 需要登录

### 请求参数

**Query 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | String | 是 | 通知类型 |

### 请求示例
```
GET /api/anime/notify/listByType?type=SYSTEM
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
      "title": "系统通知",
      "content": "系统维护通知",
      "type": "SYSTEM",
      "level": 1,
      "isRead": 0,
      "jumpUrl": "/system/notice",
      "notifyTime": "2024-01-01 12:00:00"
    }
  ]
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "按类型获取通知列表",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "按类型获取通知列表",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/anime/notify/listByType?type=SYSTEM",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "notify", "listByType"],
          "query": [
            {
              "key": "type",
              "value": "SYSTEM"
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

## 2. 标记通知为已读

### 接口信息
- **接口名称**: 标记通知为已读
- **请求方法**: `POST`
- **请求路径**: `/api/anime/notify/read/{id}`
- **接口描述**: 将指定通知标记为已读状态
- **权限要求**: 需要登录

### 请求参数

**Path 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | BigInteger | 是 | 通知ID |

### 请求示例
```
POST /api/anime/notify/read/1
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "标记通知为已读",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "标记通知为已读",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/anime/notify/read/1",
          "host": ["{{baseUrl}}"],
          "path": ["api", "anime", "notify", "read", "1"]
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

### 通知类型说明
- `SYSTEM`: 系统通知
- `UPDATE`: 更新通知
- `RSS`: RSS订阅通知
- `OTHER`: 其他通知

### 通知等级说明
- `1`: 普通
- `2`: 重要
- `3`: 紧急

### 已读状态说明
- `0`: 未读
- `1`: 已读
