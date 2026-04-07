# 系统字典管理接口

## 1. 新增字典

### 接口信息
- **接口名称**: 新增字典
- **请求方法**: `POST`
- **请求路径**: `/api/system/dictionary/add`
- **接口描述**: 创建新的系统字典
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictionaryName | String | 是 | 字典名称 |
| dictionaryValue | String | 是 | 字典值 |
| parentId | BigInteger | 否 | 父级ID，0表示顶级 |
| sort | Integer | 否 | 显示顺序 |
| status | Integer | 否 | 状态：0正常 1禁用 |
| remark | String | 否 | 备注 |

### 请求示例
```json
{
  "dictionaryName": "启用",
  "dictionaryValue": "0",
  "parentId": 0,
  "sort": 1,
  "status": 0,
  "remark": "状态字典"
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
    "name": "新增字典",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "新增字典",
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
          "raw": "{\n  \"dictionaryName\": \"启用\",\n  \"dictionaryValue\": \"0\",\n  \"parentId\": 0,\n  \"sort\": 1,\n  \"status\": 0,\n  \"remark\": \"状态字典\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/dictionary/add",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "dictionary", "add"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 2. 更新字典信息

### 接口信息
- **接口名称**: 更新字典信息
- **请求方法**: `POST`
- **请求路径**: `/api/system/dictionary/update`
- **接口描述**: 更新已有字典的信息
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | BigInteger | 是 | 字典ID |
| dictionaryName | String | 否 | 字典名称 |
| dictionaryValue | String | 否 | 字典值 |
| parentId | BigInteger | 否 | 父级ID |
| sort | Integer | 否 | 显示顺序 |
| status | Integer | 否 | 状态：0正常 1禁用 |
| remark | String | 否 | 备注 |

### 请求示例
```json
{
  "id": 1,
  "dictionaryName": "新字典名称",
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
    "name": "更新字典",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "更新字典信息",
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
          "raw": "{\n  \"id\": 1,\n  \"dictionaryName\": \"新字典名称\",\n  \"sort\": 2\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/dictionary/update",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "dictionary", "update"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 3. 批量删除字典

### 接口信息
- **接口名称**: 批量删除字典
- **请求方法**: `POST`
- **请求路径**: `/api/system/dictionary/deleteBatch`
- **接口描述**: 批量删除多个字典
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON Array)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| - | Array | 是 | 字典ID列表 |

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
    "name": "批量删除字典",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "批量删除字典",
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
          "raw": "{{baseUrl}}/api/system/dictionary/deleteBatch",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "dictionary", "deleteBatch"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 4. 分页获取字典树形列表

### 接口信息
- **接口名称**: 分页获取字典树形列表
- **请求方法**: `POST`
- **请求路径**: `/api/system/dictionary/listByTree`
- **接口描述**: 获取树形结构的字典列表
- **权限要求**: 需要角色 `role:super:admin` 或 `role:simple:admin`
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictionaryName | String | 否 | 字典名称（模糊查询） |
| status | Integer | 否 | 状态 |

### 请求示例
```json
{
  "status": 0
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
      "dictionaryName": "状态",
      "parentId": 0,
      "children": [
        {
          "id": 2,
          "dictionaryName": "启用",
          "dictionaryValue": "0",
          "parentId": 1
        },
        {
          "id": 3,
          "dictionaryName": "禁用",
          "dictionaryValue": "1",
          "parentId": 1
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
    "name": "获取字典树",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "分页获取字典树形列表",
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
          "raw": "{\n  \"status\": 0\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/dictionary/listByTree",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "dictionary", "listByTree"]
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
所有字典管理接口都需要管理员角色权限：
- `role:super:admin`: 超级管理员
- `role:simple:admin`: 普通管理员
