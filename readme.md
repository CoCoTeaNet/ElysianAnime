# ElysianAnime

## 项目简介

这是一个专注动漫番剧管理服务，主要用于部署在私人服务器上，订阅并下载RSS内容，然后自动追番~~~

- version: v2.2.2
- [Changed Log](release-note.md)

## 图片演示

<table>
    <tr>
        <td><img src="https://i.postimg.cc/k6K1L5xW/11.png" alt="ys_1"/></td>
        <td><img src="https://i.postimg.cc/tYBjBrRp/22.png" alt="ys_2"/></td>
    </tr>
    <tr>
        <td><img src="https://i.postimg.cc/5XjMTwjv/44.png" alt="ys_4"/></td>
        <td><img src="https://i.postimg.cc/tYqbt0jB/33.png" alt="ys_3"/></td>
    </tr>
</table>

## 启动说明

### 步骤

1. 运行数据库脚本
2. 启动本地Redis并运行后端服务
3. 安装前端依赖并运行前端项目

### 访问

* 地址：http://localhost:8086
* 账号：admin admin123456

## 环境要求

- JDK：17+
- Nodejs：14+
- MySQL：8.0+
- Redis
- qBittorrent

## 拓展项目

- [ElysianAnime-Viewer](https://github.com/CoCoTeaNet/ElysianAnime-Viewer) 桌面端应用（嵌入Mpv,播放体验更好）
