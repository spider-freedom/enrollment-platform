# 新疆大学招生宣传报名平台 架构设计文档

> 2026-07-13 | [github.com/spider-freedom/enrollment-platform](https://github.com/spider-freedom/enrollment-platform)

---

## 一、系统架构总览

```
                    +------------------+
                    |   浏览器客户端    |
                    |  localhost:3000  |
                    +--------+---------+
                             |
                    +--------+---------+
                    |   Vite Dev Proxy  |
                    |   /api -> :8080   |
                    +--------+---------+
                             |
              +--------------+--------------+
              |                             |
    +---------+----------+       +----------+---------+
    |   Vue 3.4 前端      |       |  Spring Boot 后端  |
    |   Element Plus 2.7  |       |  3.2.5  :8080     |
    |   ECharts 5.5       |       |  Java 17          |
    +--------------------+       +----------+---------+
                                            |
                          +----------------+----------------+
                          |                |                 |
                   +------+------+  +------+------+  +------+------+
                   |   MySQL 8.0  |  |  Redis 7.x  |  |  本地磁盘    |
                   |  6 张业务表  |  |  缓存/Session|  |  ./uploads/  |
                   +-------------+  +-------------+  +-------------+
```

### 分层说明

| 层级 | 技术 | 职责 |
|------|------|------|
| 客户端 | 浏览器 | 用户交互界面，SPA 单页应用 |
| 前端 | Vue 3.4 + Element Plus + ECharts | 31 个页面组件，4 种角色视图 |
| 网关 | Vite Dev Proxy | 开发环境 /api 代理到 localhost:8080 |
| 后端 | Spring Boot 3.2.5 + Java 17 | REST API 服务，11 个控制器，40+ 接口 |
| 安全 | Spring Security + JWT (JJWT 0.12.5) | 无状态 Token 认证，角色权限控制 |
| ORM | MyBatis-Plus 3.5.7 | 数据库映射，分页查询，自动填充 |
| 数据库 | MySQL 8.0 | 主数据存储，6 张业务表 |
| 缓存 | Redis 7.x | 会话共享，热点数据缓存 |
| 文件 | 本地磁盘 | 头像上传，附件存储 |

---

## 二、前端技术架构

```
main.ts (应用入口)
  |
  +-- createApp(Vue 3.4)
  +-- use(ElementPlus, { locale: zhCn })
  +-- use(Pinia)  -- 全局状态管理
  +-- use(Router) -- 路由导航
  |
  +-- App.vue --> <router-view/>
       |
       +-- MainLayout.vue (布局壳)
       |     |-- 深色侧边栏 (220px)
       |     |-- 顶部导航 (通知铃铛 + 角色标签 + 头像 + 退出)
       |     +-- <router-view/> (内容区)
       |
       +-- 路由守卫 (beforeEach)
       |     校验 Token 存在
       |     恢复用户信息
       |     角色前缀匹配 (student/teacher/college/school)
       |
       +-- 31 个页面组件
       |     Login.vue          Register.vue
       |     StudentActivityList.vue   StudentActivityDetail.vue
       |     StudentEnrollments.vue    StudentFeedbackSubmit.vue
       |     StudentMyFeedback.vue     ProfileView.vue
       |     TeacherActivityList.vue   TeacherActivityDetail.vue
       |     TeacherEnrollments.vue    TeacherFeedbackSubmit.vue
       |     TeacherMyFeedback.vue
       |     CollegeActivityList.vue   CollegeApprovalList.vue
       |     CollegeFeedbackList.vue   CollegeUserManagement.vue
       |     SchoolDashboard.vue       SchoolActivityList.vue
       |     SchoolActivityCreate.vue   SchoolApprovalList.vue
       |     SchoolFeedbackList.vue    SchoolUserManagement.vue
       |
       +-- 共享工具
             src/api/request.ts    axios 实例 + Token 拦截器
             src/api/index.ts      11 个 API 模块
             src/stores/user.ts    Pinia 用户状态
             src/router/index.ts   路由配置 + 守卫
             src/utils/constants.ts 状态映射 + 日期驱动 + 分类选项
             src/types/index.ts    TypeScript 接口
```

### 前端技术栈明细

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4 | 渐进式 JavaScript 框架，Composition API + `<script setup>` 语法 |
| TypeScript | 5.4 | 类型检查，接口定义 (Activity/Enrollment/Feedback/User/PageResult) |
| Element Plus | 2.7 | 60+ UI 组件：Table Form Dialog Tag Badge Avatar Rate Pagination |
| Element Plus Icons | 2.3 | 20+ SVG 图标：Bell Search Calendar Location User ArrowDown |
| Vue Router | 4.3 | 5 组嵌套路由，beforeEach 角色守卫，路由级代码分割 |
| Pinia | 2.1 | userStore: token + userInfo + login/logout/fetchProfile |
| Axios | 1.7 | HTTP 客户端，baseURL=/api，30s 超时，请求自动附 Token，响应兼容 list/records |
| ECharts | 5.5 | SchoolDashboard 数据大屏：柱状图(分类统计) + 饼图(评分/类型) + 折线图 |
| Sass | 1.77 | CSS 预处理，嵌套规则，变量，混合宏 |
| Vite | 5.3 | 开发服务器 HMR 热更新，生产构建 Tree Shaking，/api 代理 |
| unplugin-auto-import | 0.17 | 自动导入 Vue Composition API (ref reactive computed watch) |
| unplugin-vue-components | 0.27 | 按需导入 Element Plus 组件，减小打包体积 |

---

## 三、后端技术架构

```
Spring Boot 3.2.5 (Java 17)
|
+-- Security 安全层
|     SecurityConfig     (URL 角色授权)
|     JwtAuthFilter      (Bearer Token 拦截)
|     JwtUtils           (HS256 签名/验证/解析)
|     SecurityUtils      (获取当前用户 ID)
|
+-- Controller 控制器层 (11 个)
|     AuthController          POST login, register
|     UserController          GET/PUT profile, PUT password, POST avatar
|     CollegeUserController   GET users, POST promote/demote/import/reset
|     SchoolUserController    GET users, POST promote/demote/reset
|     ActivityController      POST create, PUT update, DELETE delete
|                              POST banner, GET list(student/teacher/college/school)
|                              GET banners, GET export
|     EnrollmentController    POST submit, GET my/college/school, POST withdraw, GET export
|     FeedbackController      POST student/teacher, GET college/school/my
|                              POST reply, GET export
|     ApprovalController      GET college/school, POST approve/reject/batch
|     StatisticsController    GET dashboard/trend/college/rating
|     AttachmentController    POST upload, GET download
|     AiController            GET school/suggest, POST normalize/analyze/summarize/approval
|
+-- Service 服务层 (7 个)
|     UserService         login, register, getProfile, updateProfile, changePassword
|                          promoteToAdmin, demoteAdmin, deleteUser, toggleStatus
|                          resetPassword, importUsers (CSV 解析), promoteToRole
|     ActivityService     createActivity, updateActivity, deleteActivity
|                          listForStudent (过滤 targetAudience=1,3 + PUBLISHED/ONGOING)
|                          listForTeacher (过滤 targetAudience=2,3 + PUBLISHED/ONGOING)
|                          listForCollege (院级 + 本院), listForSchool (全部)
|                          toggleBanner, getBanners
|     EnrollmentService   submit (校验: 状态/时间/名额/重复), listMy, listForCollege
|                          listForSchool, withdraw, getById, Excel 导出
|     FeedbackService     submitStudent, submitTeacher (校验: 已报名且已通过), listMy
|                          listForCollege (按院系筛选), listForSchool, reply, getDetail
|     ApprovalService     getPendingForCollege (collegeId + SUBMITTED)
|                          getPendingForSchool (APPROVING), approve (两段流转)
|                          reject, batchApprove
|     StatisticsService   getDashboard (活动总数/报名数/通过率/反馈率/均分)
|                          getMonthlyTrend (近 7 月按月统计), getCollegeDistribution
|                          getRatingDistribution
|     AttachmentService   upload (MultipartFile 存储), download (流式读取)
|
+-- AI 模块
|     AiConfig            ChatLanguageModel Bean (DeepSeek API, temperature 0.3, max 4096)
|     SchoolNameNormalizer 学校名 NLP 标准化
|     FeedbackAnalyzer     反馈情感分析 + 关键词提取
|
+-- Common 通用层
|     ApiResponse         统一响应包装 {code, message, data}
|     PageResult          分页结果 {list, total, page, size}
|     BusinessException   业务异常 (code + message)
|     GlobalExceptionHandler 全局异常处理 (@RestControllerAdvice)
|     ExcelExportUtil     Apache POI Excel 导出
|
+-- Config 配置层
|     CorsConfig          跨域配置 (允许所有来源)
|     MybatisPlusConfig   分页插件 + MySQL 方言
|     MetaObjectHandler   createTime/updateTime 自动填充
|     WebMvcConfig        静态资源 /uploads/** 映射
```

### 后端技术栈明细

| 技术 | 版本 | 用途 | 关键类/配置 |
|------|------|------|------------|
| Java | 17 LTS | Record 类型 DTO，String.isBlank，List.of，Stream API | 全部 DTO 使用 Record |
| Spring Boot | 3.2.5 | IoC 容器，自动配置，内嵌 Tomcat，Actuator 健康检查 | application.yml |
| Spring Security | 6.1 | 过滤器链，URL 授权，无状态会话 | SecurityConfig.java |
| Spring Data Redis | 3.2.5 | Redis 自动配置，连接池 | application.yml |
| Spring AOP | 3.2.5 | @Transactional 事务管理 | UserService ActivityService |
| Spring Validation | 3.2.5 | @Valid @NotBlank @NotNull DTO 校验 | 全部 Controller |
| JJWT | 0.12.5 | HMAC-SHA384 签名，Claims 存储 userId+role，24h 过期 | JwtUtils.java |
| MyBatis-Plus | 3.5.7 | BaseMapper CRUD，LambdaQueryWrapper 类型安全查询，Page 分页 | 6 个 Mapper 接口 |
| LangChain4j | 0.35.0 | Spring Boot Starter 自动配置，OpenAI 兼容客户端 | AiConfig.java |
| Knife4j | 4.5.0 | OpenAPI 3.0 文档生成，Swagger UI /doc.html | 自动扫描 |
| Apache POI | 5.2.5 | XSSFWorkbook 生成 .xlsx，SXSSFWorkbook 流式导出 | ExcelExportUtil.java |
| Hutool | 5.8.28 | StrUtil 字符串工具，DateUtil 日期处理 | 全局 |
| Lombok | latest | @Data @RequiredArgsConstructor 减少样板代码 | 全部实体和 Service |
| MySQL Connector | runtime | JDBC Type 4 驱动，UTF-8 编码 | pom.xml |
| H2 | runtime | 内存数据库 (备用) | pom.xml |

---

## 四、数据库设计

### 核心实体关系图

```
  sys_user                 activity                 enrollment
  (用户表)                 (活动表)                  (报名表)
+-------------+         +----------------+        +-----------------+
| id (PK)     |         | id (PK)        |        | id (PK)         |
| username    |----+    | title          |    +---| activity_id(FK) |
| password    |    |    | description    |    |   | user_id(FK)     |
| name        |    |    | type           |    |   | status          |
| role        |    +--->| category       |    |   | target_school   |
| college_id  |    |    | level          |    |   | college_id      |
| college_name|    |    | status         |<---+   | college_name    |
| major       |    |    | target_audience|        | reject_reason   |
| grade       |    |    | start_time     |        | submitted_at    |
| gpa         |    |    | end_time       |        | approved_at     |
| email       |    |    | enroll_start   |        | create_time     |
| phone       |    |    | enroll_end     |        | update_time     |
| avatar      |    |    | location       |        +-----------------+
| status      |    |    | max_students   |
| create_time |    |    | max_teachers   |         feedback
| update_time |    |    | max_per_school |        (反馈表)
+-------------+    |    | banner_url     |       +-----------------+
                   |    | is_banner      |       | id (PK)         |
  activity_field   |    | college_id     |   +---| activity_id(FK) |
  _config          |    | college_name   |   |   | user_id(FK)     |
+-------------+    |    | group_rule     |   |   | content         |
| id (PK)     |    |    | rank_rule      |   |   | rating          |
| activity_id |<- -+    | workflow_key   |   |   | status          |
| field_name  |         | creator_id     |---+   | reply           |
| field_label |         | create_time    |       | reply_user_id   |
| field_type  |         | update_time    |       | reply_time      |
| required    |         +----------------+       | create_time     |
| options     |                                   | update_time     |
+-------------+         feedback_attachment       +-----------------+
                        (附件表)
                       +-----------------+
                       | id (PK)         |
                       | feedback_id(FK) |
                       | file_name       |
                       | file_path       |
                       | file_size       |
                       | file_type       |
                       | create_time     |
                       +-----------------+
```

### 数据规模

| 表名 | 记录数 | 说明 |
|------|--------|------|
| sys_user | 45 | 学生 11 + 教师 5 + 学院管理员 12 + 学校管理员 2 + 旧数据 |
| activity | 24 | 院级 16 (8 个学院各 1-2 个) + 校级 8 |
| enrollment | 21 | SUBMITTED 5 / APPROVING 3 / APPROVED 10 / REJECTED 1 / WITHDRAWN 0 |
| feedback | 7 | SUBMITTED 4 / REPLIED 3 |
| feedback_attachment | 0 | 暂无附件记录 |
| activity_field_config | 3 | 目标学校 + 个人简介 + 个人简历 |

### 关键字段约束

| 表 | 约束 | 说明 |
|------|------|------|
| sys_user | UNIQUE(username) | 学号/工号唯一 |
| enrollment | UNIQUE(user_id, activity_id) | 同一用户对同一活动只能报名一次 |
| feedback | UNIQUE(user_id, activity_id) | 同一用户对同一活动只能反馈一次 |
| activity | INDEX(status), INDEX(type), INDEX(creator_id) | 按状态/类型/创建人查询优化 |
| enrollment | INDEX(activity_id), INDEX(user_id), INDEX(status) | 按活动/用户/状态查询优化 |
| feedback | INDEX(activity_id), INDEX(user_id), INDEX(status) | 同上 |

---

## 五、审批流程

```
  学生提交报名
       |
       v
+--------------+
|   SUBMITTED  |  (已提交，等待学院审批)
|   状态: 待审核 |
+------+-------+
       |
       | 学院管理员: 通过 / 驳回
       |
   +---+---+
   |       |
   v       v
+-------+  +----------+
|APPROVING|  | REJECTED |  (驳回，流程结束)
|待学校审批|  | 已驳回    |
+---+---+  +----------+
    |
    | 学校管理员: 通过 / 驳回
    |
+---+---+
|       |
v       v
+-------+  +----------+
|APPROVED|  | REJECTED |
|已通过   |  | 已驳回    |
+-------+  +----------+
    |
    v
 (流程结束，可提交反馈)
```

### 权限矩阵

| 操作 | STUDENT | TEACHER | COLLEGE_ADMIN | SCHOOL_ADMIN |
|------|---------|---------|---------------|--------------|
| 浏览活动列表 | 学生+师生 | 教师+师生 | 本院院级 | 全部 |
| 报名活动 | 可报 | 可报 | -- | -- |
| 撤回报名 | 自己的 | 自己的 | -- | -- |
| 学院审批 | -- | -- | 本院 SUBMITTED | -- |
| 学校审批 | -- | -- | -- | 全部 APPROVING |
| 创建活动 | -- | -- | 院级(本院) | 院级+校级 |
| 编辑活动 | -- | -- | 本院院级 | 全部 |
| 删除活动 | -- | -- | 本院院级 | 全部 |
| 反馈管理 | -- | -- | 本院 | 全部 |
| 用户管理 | -- | -- | 本院 | 全校 |

---

## 六、前端状态显示逻辑

### 活动状态由日期驱动（非数据库静态值）

| 条件 | 显示状态 | 标签颜色 | 能否报名 |
|------|----------|----------|----------|
| 当前时间 < 报名开始时间 | 未开始 | 灰色 info | 否 |
| 报名开始 <= 当前 <= 报名截止 | 报名中 | 绿色 success | 是 |
| 报名截止 < 当前 < 活动开始 | 报名已截止 | 红色 danger | 否 |
| 活动开始 <= 当前 <= 活动结束 | 进行中 | 橙色 warning | 否 |
| 当前 > 活动结束 | 已结束 | 灰色 info | 否 |
| 状态为 DRAFT | 草稿 | 灰色 info | 否 |

### 报名状态流转

| 数据库状态 | 中文显示 | 标签颜色 |
|-----------|----------|----------|
| SUBMITTED | 待审核 | 蓝色 info |
| APPROVING | 审批中 | 橙色 warning |
| APPROVED | 已通过 | 绿色 success |
| REJECTED | 已驳回 | 红色 danger |
| WITHDRAWN | 已撤回 | 灰色 info |

### 反馈状态

| 数据库状态 | 中文显示 | 标签颜色 |
|-----------|----------|----------|
| SUBMITTED | 待回复 | 橙色 warning |
| REPLIED | 已回复 | 绿色 success |
| CLOSED | 已关闭 | 灰色 info |

---

## 七、项目文件统计

```
enrollment-platform/
|
+-- database/
|     +-- init.sql                      280 行   数据库初始化脚本
|
+-- enrollment-backend/                  66 个 Java 文件
|     +-- pom.xml                                Maven 项目配置
|     +-- src/main/resources/
|     |     +-- application.yml                  全局配置 (数据源 Redis JWT AI 文件)
|     +-- src/main/java/com/xju/enrollment/
|           +-- ai/                       3 文件  AI 配置 + 分析器
|           +-- common/                   5 文件  响应/分页/异常/Excel
|           +-- config/                   4 文件  CORS/MyBatis/MetaHandler/WebMvc
|           +-- controller/              1 文件  AiController
|           +-- entity/                   6 文件  JPA 实体映射
|           +-- mapper/                   6 文件  MyBatis-Plus Mapper
|           +-- modules/
|           |     +-- activity/           7 文件  控制器 服务 DTO
|           |     +-- enrollment/         7 文件  控制器 服务 DTO
|           |     +-- feedback/           10 文件 控制器 服务 DTO 附件
|           |     +-- statistics/         5 文件  控制器 服务 DTO
|           |     +-- system/             10 文件 控制器 服务 DTO (用户/认证)
|           |     +-- workflow/           5 文件  控制器 服务 DTO (审批)
|           +-- security/                 4 文件  SecurityConfig JWT 过滤器 工具
|
+-- enrollment-frontend/                  38 个源文件
|     +-- package.json                          NPM 依赖配置
|     +-- vite.config.ts                        Vite 配置 (/api 代理)
|     +-- tsconfig.json                         TypeScript 配置 (@ 别名)
|     +-- index.html                            入口 HTML
|     +-- src/
|           +-- api/                     2 文件  axios 实例 + 11 个 API 模块
|           +-- layouts/                 1 文件  MainLayout 主布局
|           +-- router/                  1 文件  路由配置 + 守卫
|           +-- stores/                  1 文件  Pinia 用户状态
|           +-- types/                   1 文件  TS 接口定义
|           +-- utils/                   1 文件  常量映射 + 日期逻辑
|           +-- views/                   31 文件 页面组件
|
+-- doc/
      +-- architecture.md                      本架构文档
```

---

## 八、API 接口汇总

### 认证模块 (2 个接口)
```
POST /api/auth/login     公开   用户名+密码登录，返回 JWT Token
POST /api/auth/register  公开   学生/教师注册
```

### 用户模块 (16 个接口)
```
GET    /api/user/profile          获取个人信息
PUT    /api/user/profile          更新个人信息
PUT    /api/user/password         修改密码
POST   /api/user/avatar           上传头像 (MultipartFile, <=2MB)
GET    /api/college/users/list    学院管理员: 本院用户列表
GET    /api/college/users/admins  学院管理员: 本院管理员列表
POST   /api/college/users/{id}/promote    提升为学院管理员
POST   /api/college/users/{id}/demote     降为教师
POST   /api/college/users/{id}/reset-password  重置密码为123456
POST   /api/college/users/{id}/status    切换启用/禁用
DELETE /api/college/users/{id}           软删除 (status=DISABLED)
POST   /api/college/users/import         CSV 批量导入
GET    /api/school/users/list            学校管理员: 全校用户列表
POST   /api/school/users/{id}/promote-college  提升为学院管理员
POST   /api/school/users/{id}/promote-school   提升为学校管理员
POST   /api/school/users/{id}/demote           降为教师
```

### 活动模块 (10 个接口)
```
POST /api/activity/create         创建活动 (默认 DRAFT)
PUT  /api/activity/update/{id}    更新活动
DELETE /api/activity/delete/{id}  删除活动 (物理删除)
POST /api/activity/{id}/banner    切换 Banner 轮播状态
GET  /api/activity/list/student   学生端列表 (targetAudience=1或3, PUBLISHED+ONGOING)
GET  /api/activity/list/teacher   教师端列表 (targetAudience=2或3, PUBLISHED+ONGOING)
GET  /api/activity/list/college   学院端列表 (院级, 本院)
GET  /api/activity/list/school    学校端列表 (全部)
GET  /api/activity/banners        Banner 轮播图列表
GET  /api/activity/export         Excel 导出
GET  /api/activity/{id}           活动详情
```

### 报名模块 (6 个接口)
```
POST /api/enrollment/submit       提交报名 (校验状态/时间/名额/重复)
GET  /api/enrollment/my           我的报名列表
GET  /api/enrollment/college      学院报名列表
GET  /api/enrollment/school       全校报名列表
POST /api/enrollment/{id}/withdraw 撤回报名 (仅 SUBMITTED/APPROVING)
GET  /api/enrollment/export       Excel 导出
```

### 审批模块 (5 个接口)
```
GET  /api/approval/college        学院审批列表 (collegeId + SUBMITTED)
GET  /api/approval/school         学校审批列表 (APPROVING)
POST /api/approval/approve        审批通过 (学院: SUBMITTED->APPROVING, 学校: APPROVING->APPROVED)
POST /api/approval/reject         驳回 (status->REJECTED + rejectReason)
POST /api/approval/batch          批量审批
```

### 反馈模块 (9 个接口)
```
POST /api/feedback/student        学生提交反馈 (需已报名且已通过)
POST /api/feedback/teacher        教师提交反馈
GET  /api/feedback/college        学院反馈列表
GET  /api/feedback/school         全校反馈列表
GET  /api/feedback/my             我的反馈列表
GET  /api/feedback/{id}           反馈详情
POST /api/feedback/{id}/reply     回复反馈
POST /api/feedback/attachment/upload  上传附件
GET  /api/feedback/attachment/download/{id}  下载附件
GET  /api/feedback/export         Excel 导出
```

### 统计模块 (4 个接口)
```
GET /api/statistics/dashboard     仪表盘 (活动总数/报名数/通过率/反馈率/均分)
GET /api/statistics/trend         月度报名趋势 (近 7 月)
GET /api/statistics/college       学院报名分布
GET /api/statistics/rating        评分分布 (1-5 分)
```

### AI 模块 (5 个接口)
```
GET  /api/ai/school/suggest       学校名称联想 (keyword 模糊匹配)
POST /api/ai/school/normalize     学校名称标准化
POST /api/ai/feedback/analyze     反馈情感分析
POST /api/ai/feedback/summarize   多条反馈摘要
POST /api/ai/approval/suggest     审批建议 (GPA+目标学校+报名数据分析)
```