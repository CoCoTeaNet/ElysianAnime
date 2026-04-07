# 系统用户管理接口

## 1. 新增用户

### 接口信息
- **接口名称**: 新增用户
- **请求方法**: `POST`
- **请求路径**: `/api/system/user/add`
- **接口描述**: 创建新的系统用户
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| nickname | String | 否 | 昵称 |
| email | String | 否 | 邮箱 |
| phone | String | 否 | 手机号 |
| sex | Integer | 否 | 性别：0未知 1男 2女 |
| status | Integer | 否 | 状态：0正常 1禁用 |
| remark | String | 否 | 备注 |

### 请求示例
```json
{
  "username": "testuser",
  "password": "123456",
  "nickname": "测试用户",
  "email": "test@example.com",
  "phone": "13800138000",
  "sex": 1,
  "status": 0,
  "remark": "测试账号"
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
    "name": "新增用户",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "新增用户",
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
          "raw": "{\n  \"username\": \"testuser\",\n  \"password\": \"123456\",\n  \"nickname\": \"测试用户\",\n  \"email\": \"test@example.com\",\n  \"phone\": \"13800138000\",\n  \"sex\": 1,\n  \"status\": 0,\n  \"remark\": \"测试账号\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/user/add",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "user", "add"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 2. 更新用户信息

### 接口信息
- **接口名称**: 更新用户信息
- **请求方法**: `POST`
- **请求路径**: `/api/system/user/update`
- **接口描述**: 更新已有用户的信息
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | BigInteger | 是 | 用户ID |
| username | String | 否 | 用户名 |
| nickname | String | 否 | 昵称 |
| email | String | 否 | 邮箱 |
| phone | String | 否 | 手机号 |
| sex | Integer | 否 | 性别：0未知 1男 2女 |
| status | Integer | 否 | 状态：0正常 1禁用 |
| remark | String | 否 | 备注 |

### 请求示例
```json
{
  "id": 1,
  "nickname": "新昵称",
  "email": "newemail@example.com",
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
    "name": "更新用户信息",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "更新用户信息",
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
          "raw": "{\n  \"id\": 1,\n  \"nickname\": \"新昵称\",\n  \"email\": \"newemail@example.com\",\n  \"status\": 0\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/user/update",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "user", "update"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 3. 删除用户

### 接口信息
- **接口名称**: 删除用户
- **请求方法**: `POST`
- **请求路径**: `/api/system/user/delete/{id}`
- **接口描述**: 删除指定用户
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`

### 请求参数

**Path 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | BigInteger | 是 | 用户ID |

### 请求示例
```
POST /api/system/user/delete/1
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
    "name": "删除用户",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "删除用户",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/system/user/delete/1",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "user", "delete", "1"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 4. 批量删除用户

### 接口信息
- **接口名称**: 批量删除用户
- **请求方法**: `POST`
- **请求路径**: `/api/system/user/deleteBatch`
- **接口描述**: 批量删除多个用户
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON Array)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| - | Array | 是 | 用户ID列表 |

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
    "name": "批量删除用户",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "批量删除用户",
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
          "raw": "{{baseUrl}}/api/system/user/deleteBatch",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "user", "deleteBatch"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 5. 分页查询用户

### 接口信息
- **接口名称**: 分页查询用户
- **请求方法**: `POST`
- **请求路径**: `/api/system/user/listByPage`
- **接口描述**: 分页查询用户列表，支持条件筛选
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | 是 | 页码，从1开始 |
| pageSize | Integer | 是 | 每页条数 |
| sysUser | Object | 是 | 查询条件对象 |
| sysUser.username | String | 否 | 用户名（模糊查询） |
| sysUser.nickname | String | 否 | 昵称（模糊查询） |
| sysUser.email | String | 否 | 邮箱（模糊查询） |
| sysUser.phone | String | 否 | 手机号（模糊查询） |
| sysUser.status | Integer | 否 | 状态：0正常 1禁用 |

### 请求示例
```json
{
  "pageNo": 1,
  "pageSize": 10,
  "sysUser": {
    "username": "admin",
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
    "total": 100,
    "pages": 10,
    "records": [
      {
        "id": 1,
        "username": "admin",
        "nickname": "管理员",
        "email": "admin@example.com",
        "phone": "13800138000",
        "sex": 1,
        "status": 0,
        "avatar": "avatar.jpg",
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
    "name": "分页查询用户",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "分页查询用户",
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
          "raw": "{\n  \"pageNo\": 1,\n  \"pageSize\": 10,\n  \"sysUser\": {\n    \"username\": \"admin\",\n    \"status\": 0\n  }\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/user/listByPage",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "user", "listByPage"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 6. 获取用户详情

### 接口信息
- **接口名称**: 获取用户详细
- **请求方法**: `GET`
- **请求路径**: `/api/system/user/getDetail`
- **接口描述**: 获取当前登录用户的详细信息
- **权限要求**: 需要登录

### 请求参数
无

### 请求示例
```
GET /api/system/user/getDetail
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "sex": 1,
    "status": 0,
    "avatar": "avatar.jpg",
    "createTime": "2024-01-01 00:00:00"
  }
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "获取用户详情",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取用户详细",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/system/user/getDetail",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "user", "getDetail"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 7. 更新当前用户信息

### 接口信息
- **接口名称**: 更新登录用户信息
- **请求方法**: `POST`
- **请求路径**: `/api/system/user/updateByUser`
- **接口描述**: 当前登录用户更新自己的信息
- **权限要求**: 需要登录
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| nickname | String | 否 | 昵称 |
| email | String | 否 | 邮箱 |
| phone | String | 否 | 手机号 |
| sex | Integer | 否 | 性别：0未知 1男 2女 |

### 请求示例
```json
{
  "nickname": "新昵称",
  "email": "newemail@example.com"
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
    "name": "更新当前用户信息",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "更新登录用户信息",
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
          "raw": "{\n  \"nickname\": \"新昵称\",\n  \"email\": \"newemail@example.com\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/user/updateByUser",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "user", "updateByUser"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 8. 修改用户密码

### 接口信息
- **接口名称**: 修稿用户密码
- **请求方法**: `POST`
- **请求路径**: `/api/system/user/doModifyPassword`
- **接口描述**: 修改当前登录用户的密码
- **权限要求**: 需要登录
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码 |

### 请求示例
```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
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
    "name": "修改用户密码",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "修稿用户密码",
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
          "raw": "{\n  \"oldPassword\": \"123456\",\n  \"newPassword\": \"654321\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/user/doModifyPassword",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "user", "doModifyPassword"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 9. 上传用户头像

### 接口信息
- **接口名称**: 系统用户头像上传
- **请求方法**: `POST`
- **请求路径**: `/api/system/user/avatar/upload`
- **接口描述**: 上传当前用户的头像图片
- **权限要求**: 需要登录
- **Content-Type**: `multipart/form-data`

### 请求参数

**Form Data 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 头像图片文件 |

### 请求示例
```
POST /api/system/user/avatar/upload
Content-Type: multipart/form-data

file: [图片文件]
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
    "name": "上传用户头像",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "系统用户头像上传",
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
              "src": "/path/to/avatar.jpg"
            }
          ]
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/user/avatar/upload",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "user", "avatar", "upload"]
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
大部分用户管理接口需要管理员角色权限：
- `role:super:admin`: 超级管理员
- `role:simple:admin`: 普通管理员
