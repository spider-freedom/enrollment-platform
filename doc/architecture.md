# 新疆大学招生宣传报名平台 架构设计文档

> 2026-07-13 | [github.com/spider-freedom/enrollment-platform](https://github.com/spider-freedom/enrollment-platform)

---

## 一、系统架构总览

```mermaid
graph TB
    Browser["浏览器客户端<br/>localhost:3000"]

    subgraph Frontend["前端展示层"]
        Vue["Vue 3.4 + TypeScript 5.4"]
        Element["Element Plus 2.7"]
        ECharts["ECharts 5.5"]
        ViteProxy["Vite 代理 /api → :8080"]
    end

    subgraph Gateway["安全网关层"]
        SpringSec["Spring Security 6.1"]
        JWTFilter["JWT 认证过滤器"]
        CorsFilter["CORS 跨域过滤器"]
    end

    subgraph Business["业务服务层 :8080"]
        AuthMod["认证模块<br/>登录 注册 用户管理"]
        ActMod["活动模块<br/>CRUD 分类 级别 Banner"]
        EnrollMod["报名模块<br/>提交 撤回 导出"]
        FeedbackMod["反馈模块<br/>提交 回复 附件"]
        ApprovalMod["审批模块<br/>院级→校级 两级审批"]
        StatsMod["统计模块<br/>仪表盘 分布 评分"]
        AIMod["AI 模块<br/>LangChain4j + DeepSeek"]
    end

    subgraph Data["数据存储层"]
        MySQL["MySQL 8.0<br/>6 张业务表"]
        Redis["Redis 7.x<br/>缓存 / Session"]
        DiskStorage["本地文件系统<br/>./uploads/ 头像 附件"]
    end

    Browser --> ViteProxy
    ViteProxy --> SpringSec
    SpringSec --> JWTFilter
    JWTFilter --> CorsFilter
    CorsFilter --> Business
    AuthMod --> MySQL
    ActMod --> MySQL
    EnrollMod --> MySQL
    FeedbackMod --> MySQL
    FeedbackMod --> DiskStorage
    ApprovalMod --> MySQL
    StatsMod --> MySQL
    AIMod --> Redis
    AuthMod --> Redis
```

---

## 二、前端技术架构

```mermaid
graph TB
    subgraph Entry["应用入口"]
        MainTS["main.ts"]
        AppVue["App.vue"]
    end

    subgraph StateRouter["状态管理与路由"]
        Pinia["Pinia 2.1<br/>userStore<br/>token + userInfo<br/>login / logout / fetchProfile"]
        Router["Vue Router 4.3<br/>5 组角色路由<br/>beforeEach 守卫<br/>角色前缀校验"]
    end

    subgraph NetLayer["网络通信层"]
        AxiosInst["axios 实例<br/>baseURL=/api<br/>timeout=30s"]
        ReqInt["请求拦截器<br/>自动附加 Bearer Token"]
        RespInt["响应拦截器<br/>401 自动跳转登录<br/>兼容 data.list / data.records"]
        APIMods["11 个 API 模块<br/>auth user activity enrollment<br/>approval feedback statistics<br/>collegeUser schoolUser ai"]
    end

    subgraph ViewLayer["页面视图层 31 个组件"]
        StudentViews["学生端 6 页<br/>ActivityList / Detail / Enrollments<br/>FeedbackSubmit / MyFeedback / Profile"]
        TeacherViews["教师端 6 页<br/>同上结构 角色适配"]
        CollegeViews["学院管理端 5 页<br/>ActivityList / Approvals<br/>Feedbacks / Users / Profile"]
        SchoolViews["学校管理端 8 页<br/>Dashboard / ActivityList / Create<br/>Approvals / Feedbacks / Users / Profile"]
        PublicViews["公共页 3 页<br/>Login / Register / Profile"]
    end

    subgraph SharedLayer["共享组件与工具"]
        MainLayout["MainLayout.vue<br/>深色侧边栏 通知铃铛<br/>角色标签 头像 退出按钮"]
        Constants["constants.ts<br/>状态类型角色映射<br/>日期驱动状态计算<br/>canEnroll 报名判断"]
        Types["types/index.ts<br/>TypeScript 类型定义<br/>Activity Enrollment Feedback User"]
    end

    MainTS --> AppVue
    AppVue --> Pinia
    AppVue --> Router
    Router --> ViewLayer
    ViewLayer --> SharedLayer
    ViewLayer --> APIMods
    APIMods --> AxiosInst
    AxiosInst --> ReqInt
    AxiosInst --> RespInt
    NetLayer --> BackendTarget["后端 API :8080"]
```

---

## 三、后端模块详解

```mermaid
graph TB
    subgraph SecurityLayer["安全模块"]
        JwtUtils["JwtUtils<br/>HS256 签名<br/>生成 验证 解析 Token<br/>载荷: userId role sub exp"]
        JwtFilter["JwtAuthFilter<br/>提取 Authorization Header<br/>跳过 /auth 和 /doc.html<br/>设置 SecurityContext"]
        SecConfig["SecurityConfig<br/>无状态会话<br/>CSRF 禁用<br/>URL 角色授权<br/>college→COLLEGE_ADMIN<br/>school→SCHOOL_ADMIN<br/>其余→authenticated"]
    end

    subgraph ControllerLayer["控制器层 11 个"]
        AuthCtrl["AuthController<br/>POST /auth/login<br/>POST /auth/register"]
        UserCtrl["UserController<br/>GET/PUT /user/profile<br/>PUT /user/password<br/>POST /user/avatar 上传"]
        CollegeUserCtrl["CollegeUserController<br/>GET /college/users/list<br/>POST promote demote<br/>POST import CSV<br/>DELETE resetPassword"]
        SchoolUserCtrl["SchoolUserController<br/>GET /school/users/list<br/>POST promoteCollege<br/>POST promoteSchool<br/>POST demoteToTeacher"]
        ActivityCtrl["ActivityController<br/>POST create / PUT update<br/>DELETE delete / POST banner<br/>GET list student teacher<br/>GET list college school<br/>GET banners export"]
        EnrollmentCtrl["EnrollmentController<br/>POST submit<br/>GET my college school<br/>POST withdraw<br/>GET export"]
        FeedbackCtrl["FeedbackController<br/>POST student teacher<br/>GET college school my<br/>POST reply / GET export"]
        ApprovalCtrl["ApprovalController<br/>GET college school<br/>POST approve reject<br/>POST batch"]
        StatsCtrl["StatisticsController<br/>GET dashboard trend<br/>GET college rating"]
        AttachCtrl["AttachmentController<br/>POST upload<br/>GET download"]
        AICtrl["AiController<br/>GET school/suggest<br/>POST school/normalize<br/>POST feedback/analyze<br/>POST approval/suggest"]
    end

    subgraph ServiceLayer["服务层 7 个"]
        UserSvc["UserService<br/>@Transactional 类级<br/>login register profile<br/>promote demote import<br/>resetPassword toggleStatus"]
        ActivitySvc["ActivityService<br/>@Transactional 类级<br/>create update delete<br/>listFor 4 种角色<br/>toggleBanner getBanners"]
        EnrollmentSvc["EnrollmentService<br/>@Transactional 提交 撤回<br/>listMy listForCollege<br/>listForSchool 导出"]
        FeedbackSvc["FeedbackService<br/>@Transactional 提交 回复<br/>listMy listForCollege<br/>listForSchool 附件关联"]
        ApprovalSvc["ApprovalService<br/>@Transactional 审批 驳回<br/>getPendingForCollege<br/>getPendingForSchool<br/>batchApprove 批量"]
        StatsSvc["StatisticsService<br/>dashboard 仪表盘<br/>monthlyTrend 月度趋势<br/>collegeDist 学院分布<br/>ratingDist 评分分布"]
        AISvc["LangChain4j 集成<br/>SchoolNameNormalizer<br/>FeedbackAnalyzer<br/>DeepSeek API 调用"]
    end

    subgraph DataLayer["数据访问层"]
        MyBatisConfig["MyBatis-Plus 3.5.7<br/>BaseMapper 通用 CRUD<br/>分页插件 MySQL 方言<br/>map-underscore-to-camel-case"]
        MetaHandler["MetaObjectHandler<br/>createTime 自动填充<br/>updateTime 自动填充"]
        EntityList["6 个实体类<br/>User Activity Enrollment<br/>Feedback FeedbackAttachment<br/>ActivityFieldConfig"]
        MapperList["6 个 Mapper 接口<br/>继承 BaseMapper<br/>自定义 LambdaWrapper"]
    end

    JwtFilter --> JwtUtils
    JwtFilter --> SecConfig
    ControllerLayer --> ServiceLayer
    ServiceLayer --> DataLayer
    DataLayer --> MySQL[("MySQL 8.0")]
```

---

## 四、数据库 ER 图

```mermaid
erDiagram
    sys_user {
        bigint id PK "自增主键"
        varchar username UK "学号工号"
        varchar password "BCrypt 加密"
        varchar name "姓名"
        varchar role "STUDENT TEACHER COLLEGE_ADMIN SCHOOL_ADMIN"
        bigint college_id "所属学院 ID"
        varchar college_name "学院名称"
        varchar major "专业 学生"
        varchar grade "年级 学生"
        double gpa "绩点 学生"
        varchar email "邮箱"
        varchar phone "手机"
        varchar avatar "头像 URL"
        varchar status "ACTIVE DISABLED"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
    }

    activity {
        bigint id PK
        varchar title "活动标题"
        text description "活动详情 富文本"
        varchar type "ONLINE OFFLINE"
        varchar category "宣讲会 开放日 夏令营 咨询会 回访母校 线上直播 走访宣讲 招生宣传 其他"
        varchar level "院级 校级"
        varchar status "DRAFT PUBLISHED ONGOING ENDED"
        tinyint target_audience "1仅学生 2仅教师 3学生+教师"
        datetime start_time "活动开始"
        datetime end_time "活动结束"
        datetime enroll_start "报名开始"
        datetime enroll_end "报名截止"
        varchar location "活动地点"
        int max_students "学生名额"
        int max_teachers "教师名额"
        int max_per_school "每校限额"
        varchar banner_url "Banner 图"
        varchar banner_link "跳转链接"
        tinyint is_banner "是否轮播"
        bigint college_id FK "归属学院"
        varchar college_name "学院名称"
        varchar group_rule "分组规则"
        varchar rank_rule "排名规则"
        varchar workflow_key "审批流程 key"
        bigint creator_id FK "创建人"
        datetime create_time
        datetime update_time
    }

    enrollment {
        bigint id PK
        bigint activity_id FK "活动"
        bigint user_id FK "报名人"
        varchar status "SUBMITTED APPROVING APPROVED REJECTED WITHDRAWN"
        text form_data "表单 JSON"
        varchar current_node "当前审批节点"
        varchar target_school "招生学校"
        bigint college_id "学院 ID"
        varchar college_name "学院名称"
        int rank_in_group "组内排名"
        varchar group_name "分组名"
        varchar reject_reason "驳回原因"
        datetime submitted_at "提交时间"
        datetime approved_at "通过时间"
        datetime create_time
        datetime update_time
    }

    feedback {
        bigint id PK
        bigint activity_id FK
        bigint user_id FK
        varchar user_role "提交时角色"
        text content "反馈内容"
        int rating "评分 1-5"
        varchar contact "联系方式"
        varchar department "院系 教师"
        varchar status "SUBMITTED REPLIED CLOSED"
        text reply "回复内容"
        bigint reply_user_id "回复人"
        datetime reply_time "回复时间"
        datetime create_time
        datetime update_time
    }

    feedback_attachment {
        bigint id PK
        bigint feedback_id FK
        varchar file_name "文件名"
        varchar file_path "存储路径"
        bigint file_size "文件大小"
        varchar file_type "MIME 类型"
        bigint upload_user_id "上传人"
        varchar attachment_type "FEEDBACK REPLY"
        datetime create_time
    }

    activity_field_config {
        bigint id PK
        bigint activity_id FK
        varchar field_name "字段名"
        varchar field_label "标签"
        varchar field_type "TEXT SELECT DATE FILE"
        tinyint required "0选填 1必填"
        int sort_order "排序"
        text options "下拉选项 JSON"
        tinyint enable_ai_normalize "AI 标准化"
    }

    sys_user ||--o{ enrollment : "报名参加"
    sys_user ||--o{ feedback : "提交反馈"
    sys_user ||--o{ activity : "创建活动"
    activity ||--o{ enrollment : "包含报名"
    activity ||--o{ feedback : "包含反馈"
    activity ||--o{ activity_field_config : "自定义字段"
    feedback ||--o{ feedback_attachment : "附件"
```

---

## 五、审批流程状态机

```mermaid
stateDiagram-v2
    direction LR

    [*] --> 已提交 : 学生提交报名<br/>POST /enrollment/submit

    state 已提交 {
        [*] --> SUBMITTED : enrollment.status = SUBMITTED
    }

    state 学院审批 {
        学院管理员审批通过 : POST /approval/approve<br/>college admin
        学院管理员驳回 : POST /approval/reject<br/>with reason
    }

    state 学校审批 {
        学校管理员审批通过 : POST /approval/approve<br/>school admin
        学校管理员驳回 : POST /approval/reject<br/>with reason
    }

    已提交 --> 学院审批通过 : status → APPROVING
    已提交 --> 学院管理员驳回 : status → REJECTED

    学院审批通过 --> 学校审批通过 : status → APPROVED
    学院审批通过 --> 学校管理员驳回 : status → REJECTED

    已提交 --> 已撤回 : POST /enrollment/{id}/withdraw
    学院审批通过 --> 已撤回 : POST /enrollment/{id}/withdraw

    学校审批通过 --> 已完成 : 审批流程结束
    学院管理员驳回 --> 已结束 : 驳回结束
    学校管理员驳回 --> 已结束 : 驳回结束
    已撤回 --> 已结束 : 撤回结束
```

---

## 六、数据流 报名全链路

```mermaid
sequenceDiagram
    participant Student as 学生
    participant Backend as 后端 API
    participant MySQL as MySQL 数据库
    participant ColAdmin as 学院管理员
    participant SchAdmin as 学校管理员

    Student->>Backend: GET /activity/list/student
    Backend->>MySQL: SELECT 筛选 PUBLISHED+ONGOING<br/>targetAudience IN (1,3)
    MySQL-->>Backend: 活动列表
    Backend-->>Student: 返回可报名活动

    Student->>Backend: POST /enrollment/submit<br/>{activityId, targetSchool, intro}
    Backend->>MySQL: 校验: 状态 时间 名额 重复
    Backend->>MySQL: INSERT enrollment status=SUBMITTED
    MySQL-->>Backend: 插入成功
    Backend-->>Student: 报名成功

    ColAdmin->>Backend: GET /approval/college
    Backend->>MySQL: SELECT WHERE collegeId=本院<br/>AND status=SUBMITTED
    MySQL-->>Backend: 待审批列表
    Backend-->>ColAdmin: 返回待审批

    ColAdmin->>Backend: POST /approval/approve<br/>{enrollmentId, action:APPROVE}
    Backend->>MySQL: UPDATE status=APPROVING
    Backend-->>ColAdmin: 审批完成

    SchAdmin->>Backend: GET /approval/school
    Backend->>MySQL: SELECT WHERE status=APPROVING
    MySQL-->>Backend: 待校级审批
    Backend-->>SchAdmin: 返回待审批

    SchAdmin->>Backend: POST /approval/approve<br/>{enrollmentId, action:APPROVE}
    Backend->>MySQL: UPDATE status=APPROVED<br/>SET approvedAt=NOW()
    Backend-->>SchAdmin: 审批完成

    Student->>Backend: GET /enrollment/my
    Backend->>MySQL: SELECT WHERE userId=当前用户
    MySQL-->>Backend: 报名列表含状态
    Backend-->>Student: 状态更新为 APPROVED
```

---

## 七、技术栈明细

### 后端技术栈

| 类别 | 技术 | 版本 | 用途 | 应用模块 |
|------|------|------|------|----------|
| 语言 | Java | 17 LTS | 主编程语言 | 全部 |
| 框架 | Spring Boot | 3.2.5 | IoC DI MVC 自动配置 | 全部 |
| 安全 | Spring Security | 6.1 | 认证授权框架 | security/ |
| 令牌 | JJWT | 0.12.5 | JWT 生成 验证 解析 | security/JwtUtils |
| ORM | MyBatis-Plus | 3.5.7 | 数据库映射 分页 CRUD | mapper/ entity/ |
| 连接池 | HikariCP | Spring Boot 内置 | 数据库连接池 | 自动配置 |
| 校验 | Jakarta Validation | 3.0 | DTO 参数校验 @Valid | 全部 Controller |
| AI | LangChain4j | 0.35.0 | LLM 集成框架 | ai/ |
| AI | DeepSeek API | deepseek-chat | 大语言模型 | ai/AiConfig |
| 文档 | Knife4j | 4.5.0 | Swagger UI 接口文档 | 自动扫描 |
| Excel | Apache POI | 5.2.5 | Excel 导入导出 | common/ExcelExportUtil |
| 工具 | Hutool | 5.8.28 | 通用工具类 | 全局 |
| JSON | Jackson | Spring Boot 内置 | JSON 序列化 | 自动配置 |
| 日志 | SLF4J Logback | Spring Boot 内置 | 日志输出 | 自动配置 |
| 测试 | JUnit 5 | Spring Boot 内置 | 单元测试 | test/ |

### 前端技术栈

| 类别 | 技术 | 版本 | 用途 | 应用模块 |
|------|------|------|------|----------|
| 框架 | Vue 3 | 3.4 | 响应式 UI 框架 Composition API | 全部 .vue |
| 语言 | TypeScript | 5.4 | 类型安全 | 全部 .ts |
| UI 库 | Element Plus | 2.7 | 组件库 表格 表单 弹窗 | 全部视图 |
| 图标 | Element Plus Icons | 2.3 | SVG 图标集 | 全部视图 |
| 路由 | Vue Router | 4.3 | SPA 路由 导航守卫 | router/ |
| 状态 | Pinia | 2.1 | 全局状态管理 userStore | stores/ |
| HTTP | Axios | 1.7 | HTTP 客户端 拦截器 | api/ |
| 图表 | ECharts | 5.5 | 数据大屏 统计图表 | SchoolDashboard |
| CSS | Sass | 1.77 | CSS 预处理 | 全部 .vue |
| 构建 | Vite | 5.3 | 开发服务器 HMR 打包 | vite.config.ts |
| 类型 | Vue TSC | 2.0 | TypeScript 类型检查 | 构建时 |
| 工具 | unplugin-auto-import | 0.17 | 自动导入 Vue API | 自动配置 |
| 工具 | unplugin-vue-components | 0.27 | 自动导入 Element Plus | 自动配置 |

### 基础设施

| 类别 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 数据库 | MySQL | 8.0 | 主数据存储 6 张表 |
| 缓存 | Redis | 7.x | 缓存 Session 共享 |
| 构建 | Maven | 3.8+ | 后端依赖管理 构建 |
| 运行时 | JDK | 17 | Java 运行环境 |
| 运行时 | Node.js | 18+ | 前端开发运行环境 |
| 容器 | Docker Compose | 3.8 | 可选 容器化部署 MySQL Redis |