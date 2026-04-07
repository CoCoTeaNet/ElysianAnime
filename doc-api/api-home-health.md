# 首页和健康检查接口

## 1. 系统健康检查

### 接口信息
- **接口名称**: 查看系统健康状态
- **请求方法**: `GET`
- **请求路径**: `/api/health`
- **接口描述**: 检查系统是否正常运行
- **权限要求**: 无需认证

### 请求参数
无

### 请求示例
```
GET /api/health
```

### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "OK"
}
```

### Postman 导入格式
```json
{
  "info": {
    "name": "系统健康检查",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "查看系统健康状态",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{baseUrl}}/api/health",
          "host": ["{{baseUrl}}"],
          "path": ["api", "health"]
        }
      },
      "response": []
    }
  ]
}
```

---

## 2. 默认首页

### 接口信息
- **接口名称**: 默认首页
- **请求方法**: `GET`
- **请求路径**: `/api/`
- **接口描述**: 访问系统默认首页，会转发到 index.html
- **权限要求**: 无需认证

### 请求参数
无

### 请求示例
```
GET /api/
```

### 响应说明
该接口会将请求转发到 `/index.html`，返回前端页面内容。

### Postman 导入格式
```json
{
  "info": {
    "name": "默认首页",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "默认首页",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{baseUrl}}/api/",
          "host": ["{{baseUrl}}"],
          "path": ["api", ""]
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
