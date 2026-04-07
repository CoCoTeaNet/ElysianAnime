# 系统菜单管理接口

## 1. 新增菜单或权限

### 接口信息
- **接口名称**: 新增菜单或权限
- **请求方法**: `POST`
- **请求路径**: `/api/system/menu/add`
- **接口描述**: 创建新的系统菜单或权限
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| menuName | String | 是 | 菜单名称 |
| permissionCode | String | 否 | 权限编号 |
| routerPath | String | 否 | 路由地址 |
| parentId | BigInteger | 否 | 父级ID，0表示顶级 |
| menuType | Integer | 否 | 菜单类型：0目录 1菜单 2按钮 |
| isMenu | Integer | 否 | 是否菜单：0是 1否 |
| menuStatus | Integer | 否 | 菜单状态：0显示 1隐藏 |
| componentPath | String | 否 | 组件路径 |
| isExternalLink | Integer | 否 | 是否外链：0否 1是 |
| iconPath | String | 否 | 菜单图标 |
| sort | Integer | 否 | 显示顺序 |
| remark | String | 否 | 备注 |

### 请求示例
```json
{
  "menuName": "用户管理",
  "permissionCode": "system:user:list",
  "routerPath": "/system/user",
  "parentId": 0,
  "menuType": 1,
  "isMenu": 0,
  "menuStatus": 0,
  "componentPath": "system/user/index",
  "isExternalLink": 0,
  "iconPath": "user",
  "sort": 1,
  "remark": "用户管理菜单"
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
    "name": "新增菜单",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "新增菜单或权限",
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
          "raw": "{\n  \"menuName\": \"用户管理\",\n  \"permissionCode\": \"system:user:list\",\n  \"routerPath\": \"/system/user\",\n  \"parentId\": 0,\n  \"menuType\": 1,\n  \"isMenu\": 0,\n  \"menuStatus\": 0,\n  \"componentPath\": \"system/user/index\",\n  \"isExternalLink\": 0,\n  \"iconPath\": \"user\",\n  \"sort\": 1,\n  \"remark\": \"用户管理菜单\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/menu/add",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "menu", "add"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 2. 更新菜单或权限

### 接口信息
- **接口名称**: 更新菜单或权限信息
- **请求方法**: `POST`
- **请求路径**: `/api/system/menu/update`
- **接口描述**: 更新已有菜单或权限的信息
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | BigInteger | 是 | 菜单ID |
| menuName | String | 否 | 菜单名称 |
| permissionCode | String | 否 | 权限编号 |
| routerPath | String | 否 | 路由地址 |
| parentId | BigInteger | 否 | 父级ID |
| menuType | Integer | 否 | 菜单类型：0目录 1菜单 2按钮 |
| isMenu | Integer | 否 | 是否菜单：0是 1否 |
| menuStatus | Integer | 否 | 菜单状态：0显示 1隐藏 |
| componentPath | String | 否 | 组件路径 |
| isExternalLink | Integer | 否 | 是否外链：0否 1是 |
| iconPath | String | 否 | 菜单图标 |
| sort | Integer | 否 | 显示顺序 |
| remark | String | 否 | 备注 |

### 请求示例
```json
{
  "id": 1,
  "menuName": "新菜单名称",
  "sort": 2
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
    "name": "更新菜单",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "更新菜单或权限信息",
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
          "raw": "{\n  \"id\": 1,\n  \"menuName\": \"新菜单名称\",\n  \"sort\": 2\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/menu/update",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "menu", "update"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 3. 批量删除菜单或权限

### 接口信息
- **接口名称**: 批量删除菜单或权限
- **请求方法**: `POST`
- **请求路径**: `/api/system/menu/deleteBatch`
- **接口描述**: 批量删除多个菜单或权限
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON Array)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| - | Array | 是 | 菜单ID列表 |

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
    "name": "批量删除菜单",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "批量删除菜单或权限",
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
          "raw": "{{baseUrl}}/api/system/menu/deleteBatch",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "menu", "deleteBatch"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 4. 分页查询菜单或权限

### 接口信息
- **接口名称**: 分页查询菜单或权限
- **请求方法**: `POST`
- **请求路径**: `/api/system/menu/listByPage`
- **接口描述**: 分页查询菜单或权限列表，支持条件筛选
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | 是 | 页码，从1开始 |
| pageSize | Integer | 是 | 每页条数 |
| sysMenu | Object | 是 | 查询条件对象 |
| sysMenu.menuName | String | 否 | 菜单名称（模糊查询） |
| sysMenu.permissionCode | String | 否 | 权限编号（模糊查询） |
| sysMenu.menuType | Integer | 否 | 菜单类型 |
| sysMenu.menuStatus | Integer | 否 | 菜单状态 |

### 请求示例
```json
{
  "pageNo": 1,
  "pageSize": 10,
  "sysMenu": {
    "menuName": "用户",
    "menuType": 1
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
    "total": 30,
    "pages": 3,
    "records": [
      {
        "id": 1,
        "menuName": "用户管理",
        "permissionCode": "system:user:list",
        "routerPath": "/system/user",
        "parentId": 0,
        "menuType": 1,
        "menuStatus": 0,
        "sort": 1,
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
    "name": "分页查询菜单",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "分页查询菜单或权限",
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
          "raw": "{\n  \"pageNo\": 1,\n  \"pageSize\": 10,\n  \"sysMenu\": {\n    \"menuName\": \"用户\",\n    \"menuType\": 1\n  }\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/menu/listByPage",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "menu", "listByPage"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 5. 查询菜单树形数据

### 接口信息
- **接口名称**: 查询菜单或权限树形数据
- **请求方法**: `POST`
- **请求路径**: `/api/system/menu/listByTree`
- **接口描述**: 获取树形结构的菜单列表
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| menuName | String | 否 | 菜单名称（模糊查询） |
| menuStatus | Integer | 否 | 菜单状态 |

### 请求示例
```json
{
  "menuStatus": 0
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
      "id": 1,
      "menuName": "系统管理",
      "parentId": 0,
      "menuType": 0,
      "children": [
        {
          "id": 2,
          "menuName": "用户管理",
          "parentId": 1,
          "menuType": 1,
          "routerPath": "/system/user"
        }
      ]
    }
  ]
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "查询菜单树",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "查询菜单或权限树形数据",
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
          "raw": "{\n  \"menuStatus\": 0\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/menu/listByTree",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "menu", "listByTree"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 6. 查询菜单树（角色分配用）

### 接口信息
- **接口名称**: 通关角色获取菜单或权限
- **请求方法**: `POST`
- **请求路径**: `/api/system/menu/listByTreeAsRoleSelection`
- **接口描述**: 获取用于角色分配的菜单树形数据
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| menuName | String | 否 | 菜单名称（模糊查询） |

### 请求示例
```json
{}
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
      "menuName": "系统管理",
      "parentId": 0,
      "menuType": 0,
      "children": [
        {
          "id": 2,
          "menuName": "用户管理",
          "parentId": 1,
          "menuType": 1
        }
      ]
    }
  ]
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "查询菜单树(角色分配)",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "通关角色获取菜单或权限",
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
          "raw": "{}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/menu/listByTreeAsRoleSelection",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "menu", "listByTreeAsRoleSelection"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 7. 根据角色获取菜单

### 接口信息
- **接口名称**: 通关角色获取请所有菜单
- **请求方法**: `GET`
- **请求路径**: `/api/system/menu/listByRoleId/{roleId}`
- **接口描述**: 获取指定角色拥有的所有菜单
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`

### 请求参数

**Path 参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleId | String | 是 | 角色ID |

### 请求示例
```
GET /api/system/menu/listByRoleId/1
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
      "menuName": "系统管理",
      "permissionCode": "system:*:*",
      "menuType": 0
    },
    {
      "id": 2,
      "menuName": "用户管理",
      "permissionCode": "system:user:list",
      "menuType": 1
    }
  ]
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "根据角色获取菜单",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "通关角色获取请所有菜单",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/system/menu/listByRoleId/1",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "menu", "listByRoleId", "1"]
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
所有菜单管理接口都需要管理员角色权限：
- `role:super:admin`: 超级管理员
- `role:simple:admin`: 普通管理员

### 菜单类型说明
- `0`: 目录 - 用于组织菜单的容器
- `1`: 菜单 - 实际可访问的页面
- `2`: 按钮 - 页面内的操作按钮权限
