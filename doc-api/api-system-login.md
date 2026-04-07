# 系统登录相关接口

## 1. 用户登录

### 接口信息
- **接口名称**: 后台系统用户登录
- **请求方法**: `POST`
- **请求路径**: `/api/system/login`
- **接口描述**: 用户登录系统，获取访问令牌
- **权限要求**: 无需认证
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| captcha | String | 否 | 验证码（如果启用） |

### 请求示例
```json
{
  "username": "admin",
  "password": "123456"
}
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
}
```

**失败响应**
```json
{
  "code": 500,
  "msg": "用户名或密码错误",
  "data": null
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "用户登录",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "后台系统用户登录",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"username\": \"admin\",\n  \"password\": \"123456\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/login",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "login"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 2. 用户登出

### 接口信息
- **接口名称**: 后台系统用户退出登录
- **请求方法**: `POST`
- **请求路径**: `/api/system/logout`
- **接口描述**: 用户退出登录，清除会话和缓存
- **权限要求**: 需要登录
- **Headers**: 需要携带 `sa-token`

### 请求参数
无

### 请求示例
```
POST /api/system/logout
Headers:
  sa-token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
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
    "name": "用户登出",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "后台系统用户退出登录",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/system/logout",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "logout"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 3. 获取登录用户信息

### 接口信息
- **接口名称**: 获取用户登录信息
- **请求方法**: `GET`
- **请求路径**: `/api/system/loginInfo`
- **接口描述**: 获取当前登录用户的详细信息
- **权限要求**: 需要登录
- **Headers**: 需要携带 `sa-token`

### 请求参数
无

### 请求示例
```
GET /api/system/loginInfo
Headers:
  sa-token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 1,
    "username": "admin",
    "nickname": "管理员",
    "avatar": "avatar.jpg",
    "email": "admin@example.com",
    "phone": "13800138000",
    "roles": ["role:super:admin"],
    "permissions": ["system:user:list", "system:role:list"]
  }
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "获取登录用户信息",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取用户登录信息",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/system/loginInfo",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "loginInfo"]
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

### 认证说明
除登录接口外，其他接口都需要在请求头中携带 `sa-token`：
```
sa-token: {{token}}
```

登录成功后，建议将返回的 token 保存到 Postman 的环境变量中，后续请求自动携带。
