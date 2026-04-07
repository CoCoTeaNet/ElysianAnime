# 系统角色管理接口

## 1. 新增角色

### 接口信息
- **接口名称**: 新增角色
- **请求方法**: `POST`
- **请求路径**: `/api/system/role/add`
- **接口描述**: 创建新的系统角色
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleName | String | 是 | 角色名称 |
| roleKey | String | 是 | 角色权限字符串 |
| roleSort | Integer | 否 | 显示顺序 |
| status | Integer | 否 | 状态：0正常 1禁用 |
| remark | String | 否 | 备注 |

### 请求示例
```json
{
  "roleName": "测试角色",
  "roleKey": "test:role",
  "roleSort": 1,
  "status": 0,
  "remark": "测试角色"
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
    "name": "新增角色",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "新增角色",
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
          "raw": "{\n  \"roleName\": \"测试角色\",\n  \"roleKey\": \"test:role\",\n  \"roleSort\": 1,\n  \"status\": 0,\n  \"remark\": \"测试角色\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/role/add",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "role", "add"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 2. 更新角色信息

### 接口信息
- **接口名称**: 更新角色信息
- **请求方法**: `POST`
- **请求路径**: `/api/system/role/update`
- **接口描述**: 更新已有角色的信息
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | BigInteger | 是 | 角色ID |
| roleName | String | 否 | 角色名称 |
| roleKey | String | 否 | 角色权限字符串 |
| roleSort | Integer | 否 | 显示顺序 |
| status | Integer | 否 | 状态：0正常 1禁用 |
| remark | String | 否 | 备注 |

### 请求示例
```json
{
  "id": 1,
  "roleName": "新角色名称",
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
    "name": "更新角色信息",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "更新角色信息",
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
          "raw": "{\n  \"id\": 1,\n  \"roleName\": \"新角色名称\",\n  \"status\": 0\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/role/update",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "role", "update"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 3. 删除角色

### 接口信息
- **接口名称**: 删除角色
- **请求方法**: `POST`
- **请求路径**: `/api/system/role/delete/{id}`
- **接口描述**: 删除指定角色
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`

### 请求参数

**Path 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | BigInteger | 是 | 角色ID |

### 请求示例
```
POST /api/system/role/delete/1
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
    "name": "删除角色",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "删除角色",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/system/role/delete/1",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "role", "delete", "1"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 4. 批量删除角色

### 接口信息
- **接口名称**: 批量删除角色
- **请求方法**: `POST`
- **请求路径**: `/api/system/role/deleteBatch`
- **接口描述**: 批量删除多个角色
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON Array)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| - | Array | 是 | 角色ID列表 |

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
    "name": "批量删除角色",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "批量删除角色",
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
          "raw": "{{baseUrl}}/api/system/role/deleteBatch",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "role", "deleteBatch"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 5. 给角色赋予权限

### 接口信息
- **接口名称**: 给角色赋予权限
- **请求方法**: `POST`
- **请求路径**: `/api/system/role/grantPermissionsByRoleId`
- **接口描述**: 为指定角色分配菜单和权限
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON Array)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| - | Array | 是 | 角色菜单关联列表 |
| [].roleId | BigInteger | 是 | 角色ID |
| [].menuId | BigInteger | 是 | 菜单ID |

### 请求示例
```json
[
  {
    "roleId": 1,
    "menuId": 1
  },
  {
    "roleId": 1,
    "menuId": 2
  }
]
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
    "name": "给角色赋予权限",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "给角色赋予权限",
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
          "raw": "[\n  {\n    \"roleId\": 1,\n    \"menuId\": 1\n  },\n  {\n    \"roleId\": 1,\n    \"menuId\": 2\n  }\n]"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/role/grantPermissionsByRoleId",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "role", "grantPermissionsByRoleId"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 6. 分页查询角色

### 接口信息
- **接口名称**: 分页查询角色
- **请求方法**: `POST`
- **请求路径**: `/api/system/role/listByPage`
- **接口描述**: 分页查询角色列表，支持条件筛选
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | 是 | 页码，从1开始 |
| pageSize | Integer | 是 | 每页条数 |
| sysRole | Object | 是 | 查询条件对象 |
| sysRole.roleName | String | 否 | 角色名称（模糊查询） |
| sysRole.roleKey | String | 否 | 角色权限字符串（模糊查询） |
| sysRole.remark | String | 否 | 备注（模糊查询） |

### 请求示例
```json
{
  "pageNo": 1,
  "pageSize": 10,
  "sysRole": {
    "roleName": "管理员"
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
        "roleName": "超级管理员",
        "roleKey": "role:super:admin",
        "roleSort": 1,
        "status": 0,
        "remark": "超级管理员",
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
    "name": "分页查询角色",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "分页查询角色",
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
          "raw": "{\n  \"pageNo\": 1,\n  \"pageSize\": 10,\n  \"sysRole\": {\n    \"roleName\": \"管理员\"\n  }\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/role/listByPage",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "role", "listByPage"]
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
所有角色管理接口都需要管理员角色权限：
- `role:super:admin`: 超级管理员
- `role:simple:admin`: 普通管理员
