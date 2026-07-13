# 新疆大学招生宣传报名平台 — 架构设计文档

> 自动生成于 2026-07-13 | 项目: [spider-freedom/enrollment-platform](https://github.com/spider-freedom/enrollment-platform)

---

## 一、系统架构总览

```mermaid
graph TB
    subgraph 客户端["🖥️ 客户端"]
        Browser["浏览器 :3000<br/>Vue3 + Element Plus"]
    end

    subgraph 网关["⚡ 网关层"]
        Vite["Vite Dev Proxy<br/>localhost:3000 → :8080"]
    end

    subgraph 后端["☕ 后端服务 :8080"]
        Security["Spring Security<br/>JWT 无状态认证"]
        Controllers["REST Controllers<br/>11 个控制器 40+ API"]
        Services["Service Layer<br/>@Transactional 事务管理"]
        AI["AI Module<br/>LangChain4j + DeepSeek"]
        ORM["MyBatis-Plus 3.5.7<br/>ORM + 分页"]
    end

    subgraph 存储["💾 数据层"]
        MySQL[("MySQL 8.0<br/>enrollment_platform")]
        Redis[("Redis 7<br/>缓存/Session")]
        Disk[("本地磁盘<br/>./uploads/ 文件存储")]
    end

    Browser --> Vite
    Vite --> Security
    Security --> Controllers
    Controllers --> Services
    Services --> ORM
    Services --> AI
    ORM --> MySQL
    Services --> Redis
    Services --> Disk
```

---

## 二、前端架构

```mermaid
graph TB
    subgraph Entry["入口"]
        Main["main.ts<br/>createApp + ElementPlus + Router"]
    end

    subgraph Core["核心层"]
        Store["Pinia Store<br/>user.ts<br/>token + userInfo + login/logout"]
        Router["Vue Router<br/>5 个角色路由组<br/>守卫: 角色前缀校验"]
        API["api/request.ts<br/>axios 实例<br/>JWT 拦截器<br/>data.list/data.records 兼容"]
    end

    subgraph Views["页面层 31 Views"]
        Student["学生端 6 Pages<br/>ActivityList | Detail<br/>Enrollments | Feedback"]
        Teacher["教师端 6 Pages<br/>同上结构<br/>角色适配"]
        College["学院管理 6 Pages<br/>Activities CRUD | Approvals<br/>Feedbacks | Users"]
        School["学校管理 8 Pages<br/>Dashboard | Activities<br/>Approvals | Feedbacks | Users"]
        Common["公共 3 Pages<br/>Login | Register | Profile"]
    end

    subgraph Shared["共享层"]
        Layout["MainLayout.vue<br/>深色侧边栏 | 通知 | 头像"]
        Utils["constants.ts<br/>状态映射 | 日期计算<br/>canEnroll | getDisplayStatus"]
        Types["types/index.ts<br/>TS 接口定义"]
    end

    Main --> Store
    Main --> Router
    Store --> API
    Router --> Views
    Views --> Shared
    Views --> API
    API --> Backend["后端 :8080"]
```

---

## 三、后端模块结构

```mermaid
graph LR
    subgraph security["🔐 security/"]
        JWT["JwtUtils<br/>生成/验证/解析"]
        Filter["JwtAuthFilter<br/>Bearer Token 拦截"]
        Config["SecurityConfig<br/>URL 角色授权"]
    end

    subgraph modules["📦 modules/"]
        direction TB
        Auth["system/<br/>登录/注册/用户CRUD<br/>10 管理员/学生/教师"]
        Activity["activity/<br/>活动CRUD<br/>分类/级别/Banner"]
        Enrollment["enrollment/<br/>报名提交/撤回<br/>列表/导出"]
        Feedback["feedback/<br/>反馈提交/回复<br/>附件上传/下载"]
        Approval["workflow/<br/>院→校 两级审批<br/>批量操作"]
        Stats["statistics/<br/>仪表盘/趋势<br/>学院分布/评分"]
    end

    subgraph common["🔧 common/"]
        Response["ApiResponse<T><br/>统一响应包装"]
        Page["PageResult<T><br/>分页: list/total/page/size"]
        Excel["ExcelExportUtil<br/>POI 导出"]
        Exception["GlobalExceptionHandler<br/>BusinessException<br/>Validation 错误"]
    end

    subgraph infra["🏗️ infra"]
        Cors["CorsConfig"]
        MyBatis["MybatisPlusConfig<br/>分页插件"]
        Meta["MetaObjectHandler<br/>createTime/updateTime<br/>自动填充"]
        WebMvc["WebMvcConfig<br/>静态资源 /uploads/**"]
    end

    Filter --> JWT
    Filter --> Config
    Modules --> common
    Modules --> infra
```

---

## 四、数据库 ER 图

```mermaid
erDiagram
    sys_user {
        bigint id PK
        varchar username UK
        varchar password
        varchar name
        varchar role "STUDENT|TEACHER|COLLEGE_ADMIN|SCHOOL_ADMIN"
        bigint college_id FK
        varchar college_name
        varchar major
        varchar grade
        double gpa
        varchar email
        varchar phone
        varchar avatar
        varchar status "ACTIVE|DISABLED"
        datetime create_time
        datetime update_time
    }

    activity {
        bigint id PK
        varchar title
        text description
        varchar type "ONLINE|OFFLINE"
        varchar category "宣讲会|开放日|夏令营|咨询会|回访母校|线上直播|走访宣讲|招生宣传|其他"
        varchar level "院级|校级"
        varchar status "DRAFT|PUBLISHED|ONGOING|ENDED"
        tinyint target_audience "1学生|2教师|3学生+教师"
        datetime start_time
        datetime end_time
        datetime enroll_start
        datetime enroll_end
        varchar location
        int max_students
        int max_teachers
        int max_per_school
        varchar banner_url
        varchar banner_link
        tinyint is_banner
        bigint college_id FK
        varchar college_name
        bigint creator_id FK
        datetime create_time
        datetime update_time
    }

    enrollment {
        bigint id PK
        bigint activity_id FK
        bigint user_id FK
        varchar status "SUBMITTED|APPROVING|APPROVED|REJECTED|WITHDRAWN"
        text form_data
        varchar target_school
        bigint college_id
        varchar college_name
        varchar reject_reason
        datetime submitted_at
        datetime approved_at
        datetime create_time
        datetime update_time
    }

    feedback {
        bigint id PK
        bigint activity_id FK
        bigint user_id FK
        varchar user_role
        text content
        int rating "1-5"
        varchar contact
        varchar department
        varchar status "SUBMITTED|REPLIED|CLOSED"
        text reply
        bigint reply_user_id
        datetime reply_time
        datetime create_time
        datetime update_time
    }

    feedback_attachment {
        bigint id PK
        bigint feedback_id FK
        varchar file_name
        varchar file_path
        bigint file_size
        varchar file_type
        bigint upload_user_id
        varchar attachment_type
        datetime create_time
    }

    activity_field_config {
        bigint id PK
        bigint activity_id FK
        varchar field_name
        varchar field_label
        varchar field_type
        tinyint required
        int sort_order
        text options
        tinyint enable_ai_normalize
    }

    sys_user ||--o{ enrollment : "报名"
    sys_user ||--o{ feedback : "反馈"
    sys_user ||--o{ activity : "创建"
    activity ||--o{ enrollment : "包含"
    activity ||--o{ feedback : "包含"
    activity ||--o{ activity_field_config : "配置"
    feedback ||--o{ feedback_attachment : "附件"
```

---

## 五、审批流程

```mermaid
stateDiagram-v2
    [*] --> SUBMITTED: 学生提交报名

    SUBMITTED --> APPROVING: 学院管理员审批通过
    SUBMITTED --> REJECTED: 学院管理员驳回

    APPROVING --> APPROVED: 学校管理员审批通过
    APPROVING --> REJECTED: 学校管理员驳回

    SUBMITTED --> WITHDRAWN: 学生撤回
    APPROVING --> WITHDRAWN: 学生撤回

    APPROVED --> [*]: 完成
    REJECTED --> [*]: 结束
    WITHDRAWN --> [*]: 结束
```

---

## 六、数据流 — 报名全链路

```mermaid
sequenceDiagram
    actor Student as 学生
    actor ColAdmin as 学院管理员
    actor SchAdmin as 学校管理员

    Student->>Backend: GET /activity/list/student
    Backend-->>Student: 活动列表(PUBLISHED+ONGOING, targetAudience=1或3)

    Student->>Backend: POST /enrollment/submit
    Note over Backend: 校验: 状态=报名中<br/>未超名额 未重复报名
    Backend->>MySQL: INSERT enrollment (status=SUBMITTED)
    Backend-->>Student: 报名成功

    ColAdmin->>Backend: GET /approval/college
    Note over Backend: 筛选: collegeId=本院<br/>status=SUBMITTED
    Backend-->>ColAdmin: 待审批列表

    ColAdmin->>Backend: POST /approval/approve
    Backend->>MySQL: UPDATE status=APPROVING
    Backend-->>ColAdmin: 审批通过

    SchAdmin->>Backend: GET /approval/school
    Note over Backend: 筛选: status=APPROVING
    Backend-->>SchAdmin: 待校级审批列表

    SchAdmin->>Backend: POST /approval/approve
    Backend->>MySQL: UPDATE status=APPROVED
    Backend-->>SchAdmin: 审批通过

    Student->>Backend: GET /enrollment/my
    Backend-->>Student: 状态已更新为 APPROVED
```

---

## 七、技术栈详情

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 语言 | Java | 17 | 后端 |
| 框架 | Spring Boot | 3.2.5 | REST API |
| 安全 | Spring Security + JJWT | 0.12.5 | JWT 无状态认证 |
| ORM | MyBatis-Plus | 3.5.7 | 数据库操作 + 分页 |
| AI | LangChain4j | 0.35.0 | DeepSeek 集成 |
| 文档 | Knife4j | 4.5.0 | Swagger UI |
| 语言 | TypeScript | 5.4 | 前端 |
| 框架 | Vue 3 | 3.4 | SPA |
| UI | Element Plus | 2.7 | 组件库 |
| 图表 | ECharts | 5.5 | 数据大屏 |
| 构建 | Vite | 5.3 | 前端构建 |
| 数据库 | MySQL | 8.0 | 主存储 |
| 缓存 | Redis | 7 | 缓存 |