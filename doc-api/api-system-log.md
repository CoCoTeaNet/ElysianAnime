# 系统日志管理接口

## 1. 分页查询日志

### 接口信息
- **接口名称**: 列表分页查询
- **请求方法**: `POST`
- **请求路径**: `/api/system/log/listByPage`
- **接口描述**: 分页查询系统用户访问日志
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | 是 | 页码，从1开始 |
| pageSize | Integer | 是 | 每页条数 |
| sysLog | Object | 是 | 查询条件对象 |
| sysLog.username | String | 否 | 用户名（模糊查询） |
| sysLog.ip | String | 否 | IP地址（模糊查询） |
| sysLog.logType | String | 否 | 日志类型 |
| sysLog.logStatus | Integer | 否 | 日志状态：0成功 1失败 |
| sysLog.startTime | String | 否 | 开始时间 |
| sysLog.endTime | String | 否 | 结束时间 |

### 请求示例
```json
{
  "pageNo": 1,
  "pageSize": 10,
  "sysLog": {
    "username": "admin",
    "logStatus": 0
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
    "total": 1000,
    "pages": 100,
    "records": [
      {
        "id": 1,
        "username": "admin",
        "ip": "127.0.0.1",
        "logType": "LOGIN",
        "logStatus": 0,
        "requestUrl": "/api/system/login",
        "requestMethod": "POST",
        "requestParam": "{}",
        "responseResult": "{\"code\":200}",
        "costTime": 100,
        "createTime": "2024-01-01 12:00:00"
      }
    ]
  }
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "分页查询日志",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "列表分页查询",
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
          "raw": "{\n  \"pageNo\": 1,\n  \"pageSize\": 10,\n  \"sysLog\": {\n    \"username\": \"admin\",\n    \"logStatus\": 0\n  }\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/log/listByPage",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "log", "listByPage"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 2. 批量删除日志

### 接口信息
- **接口名称**: 批量删除日志
- **请求方法**: `POST`
- **请求路径**: `/api/system/log/deleteBatch`
- **接口描述**: 批量删除多个日志记录
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON Array)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| - | Array | 是 | 日志ID列表 |

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
    "name": "批量删除日志",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "批量删除日志",
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
          "raw": "{{baseUrl}}/api/system/log/deleteBatch",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "log", "deleteBatch"]
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
所有日志管理接口都需要管理员角色权限：
- `role:super:admin`: 超级管理员
- `role:simple:admin`: 普通管理员

### 日志类型说明
- `LOGIN`: 登录日志
- `OPERATION`: 操作日志
- `ERROR`: 错误日志
