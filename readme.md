<div align="center">

  <h1>🌸 ElysianAnime</h1>
  
  <p>
    <strong>一个专注动漫番剧管理的私人媒体服务器</strong>
  </p>
  <p>
    RSS 订阅 • 自动下载 • 智能追番 • 私有化部署
  </p>

  <p>
    <a href="https://github.com/CoCoTeaNet/ElysianAnime/blob/master/LICENSE">
      <img src="https://img.shields.io/github/license/CoCoTeaNet/ElysianAnime?color=blue&style=flat-square" alt="License">
    </a>
    <a href="https://github.com/CoCoTeaNet/ElysianAnime/releases">
      <img src="https://img.shields.io/github/v/release/CoCoTeaNet/ElysianAnime?style=flat-square&color=orange" alt="Release">
    </a>
    <img src="https://img.shields.io/badge/Java-17+-007396?style=flat-square&logo=java&logoColor=white" alt="Java">
    <img src="https://img.shields.io/badge/Vue.js-3.0-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white" alt="Vue">
    <img src="https://img.shields.io/badge/Database-MySQL_8.0+-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL">
  </p>

  <p>
    <a href="#-项目简介">项目简介</a> •
    <a href="#-功能特性">功能特性</a> •
    <a href="#-界面预览">界面预览</a> •
    <a href="#-快速开始">快速开始</a> •
    <a href="#-关联项目">关联项目</a>
  </p>
</div>

---

## 📖 项目简介

ElysianAnime 是一个运行在本地环境的番剧媒体库工具。它主要用于解决 RSS 订阅、自动下载以及本地媒体文件的整理展示问题，辅助用户更方便地管理 NAS 中的动漫资源。

## ✨ 功能特性

* **📡 RSS 订阅管理**：支持解析 RSS 源，根据设定规则筛选并匹配番剧资源。
* **🔗 下载器对接**：对接 qBittorrent，将匹配到的资源自动推送至下载队列。
* **🖥️ Web 可视化**：提供基于 Web 的管理后台，以海报墙形式展示已订阅或入库的媒体信息。
* **🏠 本地部署**：适合部署在 NAS 或个人服务器上，通过 Docker 或源码运行。
* **🧩 客户端支持**：可配合配套的桌面端播放器使用，支持调用本地播放器打开资源。

## 📸 界面预览

<table border="0">
  <tr>
    <td align="center" width="50%">
      <img src="https://youke1.picui.cn/s1/2025/11/20/691ed93f55ac0.png" alt="首页海报墙" style="max-width: 100%;">
      <br/>
    </td>
    <td align="center" width="50%">
      <img src="https://youke1.picui.cn/s1/2025/11/20/691ed93f27332.png" alt="详情页信息" style="max-width: 100%;">
      <br/>
    </td>
  </tr>
  <tr>
    <td align="center" width="50%">
      <img src="https://youke1.picui.cn/s1/2025/11/20/691ed93f003d8.png" alt="订阅与资源管理" style="max-width: 100%;">
      <br/>
    </td>
    <td align="center" width="50%">
      <img src="https://youke1.picui.cn/s1/2025/11/20/691ed93e37870.png" alt="播放与设置" style="max-width: 100%;">
      <br/>
    </td>
  </tr>
</table>

## 🛠️ 技术栈

* **后端**: Java 17 (Spring Boot)
* **前端**: Vue 3 + TypeScript
* **数据库**: MySQL 8.0+
* **缓存**: Redis

## 🚀 快速开始

### 环境要求

在开始之前，请确保您的环境满足以下要求：

* **JDK**: 17+
* **Node.js**: 14+
* **MySQL**: 8.0+
* **Redis**: 运行中
* **qBittorrent**: 运行中且开启 Web UI

### 部署步骤

1.  **数据库初始化**
    导入项目提供的 SQL 脚本到您的 MySQL 数据库中。

2.  **后端启动**
    修改配置文件（数据库连接、Redis配置、qBittorrent配置），然后启动 Spring Boot 服务。

3.  **前端启动**
    ```bash
    cd elysiananime-ui
    npm install
    npm run dev
    ```

4.  **访问系统**
    * 访问地址：`http://localhost:8086`
    * 默认账号：`admin`
    * 默认密码：`admin123456`

## 🔗 关联项目

为了获得更好的播放体验（如支持内封字幕、特殊格式），推荐配合我们的桌面端播放器使用：

* 🖥️ **[ElysianAnime-Viewer](https://github.com/CoCoTeaNet/ElysianAnime-Viewer)**: 基于 MPV 内核的桌面客户端。

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！如果您有好的想法或发现了 Bug，请随时告知我们。

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

## 📄 开源协议

本项目采用 [GPL-2.0](LICENSE) 协议开源。

---

<div align="center">
  Made with ❤️ by <a href="https://github.com/CoCoTeaNet">CoCoTeaNet</a>
</div>
