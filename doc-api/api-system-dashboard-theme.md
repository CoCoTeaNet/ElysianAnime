# 系统仪表盘和主题接口

## 1. 获取系统数据概览

### 接口信息
- **接口名称**: 获取系统数据概览
- **请求方法**: `GET`
- **请求路径**: `/api/system/dashboard/getCount`
- **接口描述**: 获取系统各项数据的统计概览
- **权限要求**: 需要登录

### 请求参数
无

### 请求示例
```
GET /api/system/dashboard/getCount
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "name": "用户总数",
      "value": 100,
      "icon": "user"
    },
    {
      "name": "角色总数",
      "value": 10,
      "icon": "role"
    },
    {
      "name": "菜单总数",
      "value": 50,
      "icon": "menu"
    },
    {
      "name": "番剧总数",
      "value": 200,
      "icon": "anime"
    }
  ]
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "获取系统数据概览",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取系统数据概览",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/system/dashboard/getCount",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "dashboard", "getCount"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 2. 更新用户主题信息

### 接口信息
- **接口名称**: 更新用户主题信息
- **请求方法**: `POST`
- **请求路径**: `/api/system/theme/updateByUser`
- **接口描述**: 当前登录用户更新自己的主题配置
- **权限要求**: 需要登录
- **Content-Type**: `application/json`

### 请求参数

**Body 参数 (JSON)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| themeType | String | 否 | 主题类型：light/dark |
| primaryColor | String | 否 | 主色调 |
| fontSize | Integer | 否 | 字体大小 |
| layoutMode | String | 否 | 布局模式 |

### 请求示例
```json
{
  "themeType": "dark",
  "primaryColor": "#1890ff",
  "fontSize": 14
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
    "name": "更新用户主题",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "更新用户主题信息",
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
          "raw": "{\n  \"themeType\": \"dark\",\n  \"primaryColor\": \"#1890ff\",\n  \"fontSize\": 14\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/system/theme/updateByUser",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "theme", "updateByUser"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 3. 获取用户主题信息

### 接口信息
- **接口名称**: 获取用户主题信息
- **请求方法**: `GET`
- **请求路径**: `/api/system/theme/loadByUser`
- **接口描述**: 获取当前登录用户的主题配置
- **权限要求**: 需要登录

### 请求参数
无

### 请求示例
```
GET /api/system/theme/loadByUser
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "themeType": "light",
    "primaryColor": "#1890ff",
    "fontSize": 14,
    "layoutMode": "side"
  }
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "获取用户主题",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取用户主题信息",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "sa-token",
            "value": "{{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/system/theme/loadByUser",
          "host": ["{{baseUrl}}"],
          "path": ["api", "system", "theme", "loadByUser"]
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

### 主题配置说明
- **themeType**: 主题类型
  - `light`: 浅色主题
  - `dark`: 深色主题
- **primaryColor**: 主色调，支持十六进制颜色值
- **fontSize**: 字体大小，单位px
- **layoutMode**: 布局模式
  - `side`: 侧边栏布局
  - `top`: 顶部布局
  - `mix`: 混合布局
