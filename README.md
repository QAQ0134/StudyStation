# StudyStation 学习交流平台

一个前后端分离的学习作品分享社区。用户可以发布学习作品，按学科和知识点归类，互相点赞、收藏、评论；首页根据热度推荐作品；管理员可以在后台管理用户和查看操作日志。

## 功能

**用户端**

- 注册、登录、退出
- 编辑个人资料（昵称、头像、签名、地区、兴趣爱好）
- 发布作品（标题、正文、封面图、选择学科与知识点）
- 浏览作品、按学科筛选、查看详情
- 点赞、收藏、评论
- 查看自己的作品、自己的收藏

**管理端**

- 用户分页列表，支持按用户名、注册时间、状态多条件搜索
- 封禁 / 解封用户
- 操作日志查询（操作人、模块、IP、操作结果）
- 学科与知识点的增删改查

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端框架 | Spring Boot 4.0.9、Spring MVC、Java 17 |
| 持久层 | MyBatis、PageHelper 分页 |
| 数据库 | MySQL 8 |
| 缓存 | Redis（ZSet 实现热度排行榜） |
| 认证 | JWT（jjwt）、HandlerInterceptor 拦截器 |
| 其他 | AOP + 自定义注解记录操作日志、Lombok |
| 前端 | Vue 3、Element Plus、Vue Router、Vite、axios |

## 目录结构

```
StudyStation/
├── pom.xml
├── src/
│   ├── main/java/com/studystation/
│   │   ├── annotation/     自定义 @Log 注解
│   │   ├── aspect/         日志切面
│   │   ├── config/         Web 配置（拦截器、静态资源映射）
│   │   ├── controller/     REST 接口
│   │   ├── interceptor/    登录、管理员拦截器
│   │   ├── mapper/         MyBatis Mapper
│   │   ├── pojo/           实体与返回结果
│   │   ├── service/        业务逻辑
│   │   └── utils/          JWT 工具
│   └── main/resources/
│       └── application.yml 配置文件
└── frontend/                前端工程
    └── src/
        ├── views/           页面
        ├── components/      组件
        ├── router/          路由与守卫
        └── utils/           axios 封装
```

## 本地运行

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0
- Redis 5+
- Node.js 16+

### 2. 初始化数据库

```sql
CREATE DATABASE studystation DEFAULT CHARACTER SET utf8mb4;
```

建表语句：根据 `src/main/java/com/studystation/mapper/` 下各 Mapper 中的 SQL 创建对应的表
（`user`、`work`、`comment`、`subject_area`、`knowledge_point`、`work_subject`、
`work_knowledge_point`、`user_work_like`、`user_work_collect`、`user_hobby`、`operation_log`）。

### 3. 修改配置

编辑 `src/main/resources/application.yml`，把数据库、Redis 和上传目录改成自己机器的：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/studystation
    username: root
    password: 你的密码
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password:

studystation:
  upload:
    path: D:/StudyStation_Uploads/    # 头像等文件的本地保存目录
```

### 4. 启动后端

```bash
mvn spring-boot:run
```

默认监听 `http://localhost:8080`。

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

Vite 开发服务器会把 `/api` 开头的请求代理到 `http://localhost:8080`。

## 主要接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/user/register` | 注册 |
| POST | `/user/login` | 登录，返回 token |
| GET | `/user/info` | 获取当前用户信息 |
| PUT | `/user/info` | 更新个人信息 |
| POST | `/work/publish` | 发布作品 |
| GET | `/work/recommend` | 热度推荐列表 |
| GET | `/work/{id}` | 作品详情 |
| POST | `/work/{id}/like` | 点赞 / 取消 |
| POST | `/work/{id}/collect` | 收藏 / 取消 |
| POST | `/comment` | 发表评论 |
| GET | `/admin/log` | 操作日志查询（管理员） |

登录后前端会把 token 放在请求头 `token` 中，后端由拦截器统一校验。

## 说明

- 热度排行榜使用 Redis ZSet：发布时间作为初始分，浏览 +1、评论 +2、点赞 +5、收藏 +10，取前 10 名作为首页推荐。
- 操作日志通过 AOP 拦截带 `@Log` 注解的方法，自动记录操作人、模块、IP 和操作结果，业务代码无需改动。
