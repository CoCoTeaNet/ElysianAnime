# ElysianAnime使用文档


## release包下载
[前往下载](https://github.com/CoCoTeaNet/ElysianAnime/releases)


## 前置条件

- JDK >= 17
- MySQL >= 8.0
- Redis
- qBittorrent >= 4.2.x


## 配置
1. 配置jdk环境变量 || 将jre放到项目根目录
2. 配置app.yml，根据注释的选项按需配置
备注：mysql、qb、redis以及文件路径都得配置
3. 配置启动脚本，xxx.bat是window环境使用，ani.sh是在Linux系统使用


## 启动
1. 确保mysql已启动并且导入数据（./sql/ddl-sql.sql  data-sql.sql）
2. 确保redis已启动
3. 确保qb已启动
4. 执行启动脚本
```shell
# linux
# 启动
ani.sh start
# 停止 
ani.sh stop

# window
# 启动
./start.bat
# 停止
./stop.bat
```
