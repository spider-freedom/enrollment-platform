# 新疆大学招生宣传报名平台 架构设计文档

> 2026-07-13 | [github.com/spider-freedom/enrollment-platform](https://github.com/spider-freedom/enrollment-platform)

用 VS Code 打开本文档，安装插件 **Markdown Preview Mermaid**，按 `Ctrl+Shift+V` 即可看到图表渲染效果。GitHub 上也能直接渲染。

---

## 一、系统架构总览

```mermaid
flowchart TB
    Browser["浏览器 localhost:3000"]
    Vite["Vite 前端服务器"]
    Backend["Spring Boot 后端 localhost:8080"]
    MySQL[("MySQL 数据库")]
    Redis[("Redis 缓存")]
    Disk[("本地文件存储")]

    Browser --> Vite
    Vite -->|"/api 代理"| Backend
    Backend --> MySQL
    Backend --> Redis
    Backend --> Disk
```

**说明：** 前端跑在 3000 端口，通过 Vite 把 `/api` 请求转发到后端 8080。后端连 MySQL 存数据，连 Redis 做缓存，文件（头像、附件）存本地磁盘。

---

## 二、前端架构

```mermaid
flowchart TB
    subgraph A["页面层 31 个页面"]
        direction LR
        S["学生端 6 页"]
        T["教师端 6 页"]
        C["学院管理 5 页"]
        H["学校管理 8 页"]
        P["公共 3 页"]
    end

    subgraph B["工具层"]
        direction LR
        R["Vue Router 路由"]
        ST["Pinia 状态管理"]
        AX["Axios 网络请求"]
    end

    subgraph D["UI 框架"]
        direction LR
        EL["Element Plus 组件库"]
        EC["ECharts 图表库"]
    end

    A --> B
    A --> D
    B --> Backend["后端 API"]
```

**用什么做的：**

| 技术 | 干什么用 |
|------|---------|
| Vue 3.4 | 前端框架，搭页面 |
| TypeScript 5.4 | 给 JavaScript 加类型检查，减少 bug |
| Element Plus 2.7 | 现成的 UI 组件：表格、表单、按钮、弹窗、标签 |
| ECharts 5.5 | 数据大屏画图表：柱状图、饼图 |
| Vue Router 4.3 | 页面跳转，自动检查登录状态 |
| Pinia 2.1 | 全局存用户信息和 token |
| Axios 1.7 | 发 HTTP 请求，自动带 token，token 过期自动跳登录 |
| Vite 5.3 | 开发时启动服务器，打包时压缩代码 |

---

## 三、后端架构

```mermaid
flowchart TB
    subgraph A["安全层"]
        JWT["JWT 登录验证"]
        SEC["角色权限控制"]
    end

    subgraph B["控制器层 11 个 Controller"]
        Auth["登录注册"]
        User["用户管理"]
        Act["活动管理"]
        Enr["报名管理"]
        Fb["反馈管理"]
        Appr["审批管理"]
        Stat["数据统计"]
        AI["AI 辅助"]
    end

    subgraph C["服务层 7 个 Service"]
        US["用户服务"]
        AS["活动服务"]
        ES["报名服务"]
        FS["反馈服务"]
        APS["审批服务"]
        SS["统计服务"]
        ATS["附件服务"]
    end

    subgraph D["数据层"]
        MP["MyBatis-Plus 数据库操作"]
        MySQL2[("MySQL 8.0")]
    end

    A --> B
    B --> C
    C --> MP
    MP --> MySQL2
```

**用什么做的：**

| 技术 | 干什么用 |
|------|---------|
| Java 17 | 编程语言 |
| Spring Boot 3.2.5 | 后端框架，提供 REST API |
| Spring Security | 登录验证、权限控制 |
| JWT (JJWT) | 生成登录令牌，24 小时有效 |
| MyBatis-Plus 3.5.7 | 操作数据库，写 SQL 查询，自动分页 |
| LangChain4j | 对接 DeepSeek 大模型，做 AI 分析 |
| Knife4j | 自动生成接口文档网页 |
| Apache POI | 导出 Excel 表格 |
| Redis | 缓存热点数据 |

---

## 四、数据库设计

```mermaid
erDiagram
    USER["用户表 sys_user"] {
        bigint id PK
        string username
        string password
        string name
        string role
        bigint college_id
        string college_name
        string major
        string grade
        double gpa
        string email
        string phone
        string avatar
    }

    ACTIVITY["活动表 activity"] {
        bigint id PK
        string title
        string type
        string category
        string level
        string status
        int target_audience
        datetime start_time
        datetime end_time
        datetime enroll_start
        datetime enroll_end
        string location
        int max_students
        int max_teachers
        int max_per_school
        bigint college_id
    }

    ENROLLMENT["报名表 enrollment"] {
        bigint id PK
        bigint activity_id FK
        bigint user_id FK
        string status
        string target_school
        bigint college_id
        string college_name
        string reject_reason
    }

    FEEDBACK["反馈表 feedback"] {
        bigint id PK
        bigint activity_id FK
        bigint user_id FK
        string user_role
        text content
        int rating
        string status
        text reply
        bigint reply_user_id
    }

    ATTACHMENT["附件表 feedback_attachment"] {
        bigint id PK
        bigint feedback_id FK
        string file_name
        string file_path
        bigint file_size
    }

    USER ||--o{ ENROLLMENT : "报名"
    USER ||--o{ FEEDBACK : "反馈"
    ACTIVITY ||--o{ ENROLLMENT : "包含"
    ACTIVITY ||--o{ FEEDBACK : "包含"
    FEEDBACK ||--o{ ATTACHMENT : "附件"
```

**当前数据量：** 45 个用户、24 个活动、21 条报名、7 条反馈。

---

## 五、审批流程

```mermaid
stateDiagram-v2
    direction LR

    A1: 学生提交报名
    A2: SUBMITTED 待学院审批
    A3: APPROVING 待学校审批
    A4: APPROVED 审批通过
    A5: REJECTED 已驳回

    [*] --> A1
    A1 --> A2
    A2 --> A3: 学院管理员通过
    A2 --> A5: 学院管理员驳回
    A3 --> A4: 学校管理员通过
    A3 --> A5: 学校管理员驳回
    A4 --> [*]
    A5 --> [*]
```

**谁有权限做什么：**

| 角色 | 能做什么 |
|------|---------|
| 学生 | 看活动、报名、撤回自己的报名、提交反馈 |
| 教师 | 同学生 |
| 学院管理员 | 审批本院报名（第一步）、管理本院活动、管理本院用户 |
| 学校管理员 | 审批所有报名（第二步）、管理所有活动、管理所有用户、看数据大屏 |

---

## 六、活动状态怎么显示

不按数据库存的状态显示，而是根据当前日期自动算：

| 当前日期在哪个阶段 | 显示什么 | 能报名吗 |
|-------------------|---------|---------|
| 还没到报名开始时间 | 未开始 | 不能 |
| 报名开始 ~ 报名截止 | 报名中 | 能 |
| 报名截止后，活动开始前 | 报名已截止 | 不能 |
| 活动开始 ~ 活动结束 | 进行中 | 不能 |
| 活动结束后 | 已结束 | 不能 |

---

## 七、报名和反馈怎么流转

```mermaid
flowchart LR
    subgraph A["报名状态"]
        direction LR
        E1["SUBMITTED 待审核"] --> E2["APPROVING 审批中"]
        E2 --> E3["APPROVED 已通过"]
        E1 --> E4["REJECTED 已驳回"]
        E2 --> E4
    end
```

```mermaid
flowchart LR
    subgraph B["反馈状态"]
        direction LR
        F1["SUBMITTED 待回复"] --> F2["REPLIED 已回复"]
        F2 --> F3["CLOSED 已关闭"]
    end
```

---

## 八、API 接口一览

**认证 (2 个)**
```
POST /api/auth/login        登录，返回 token
POST /api/auth/register     注册
```

**用户 (16 个)**
```
GET    /api/user/profile           看自己的信息
PUT    /api/user/profile           改自己的信息
PUT    /api/user/password          改密码
POST   /api/user/avatar            换头像
GET    /api/college/users/list     看本院用户
POST   /api/college/users/import   批量导入用户 (CSV)
POST   /api/college/users/{id}/promote   升为管理员
POST   /api/college/users/{id}/demote    降为教师
POST   /api/college/users/{id}/reset-password  重置密码
GET    /api/school/users/list      看全校用户
```

**活动 (11 个)**
```
POST   /api/activity/create        创建活动
PUT    /api/activity/update/{id}   编辑活动
DELETE /api/activity/delete/{id}   删除活动
GET    /api/activity/list/student  学生看的活动列表
GET    /api/activity/list/teacher  教师看的活动列表
GET    /api/activity/list/college  学院管理员看的活动列表
GET    /api/activity/list/school   学校管理员看的活动列表
GET    /api/activity/banners       轮播图列表
GET    /api/activity/export        导出 Excel
GET    /api/activity/{id}          活动详情
```

**报名 (6 个)**
```
POST /api/enrollment/submit        提交报名
GET  /api/enrollment/my            我的报名
GET  /api/enrollment/college       学院的报名
GET  /api/enrollment/school        全校的报名
POST /api/enrollment/{id}/withdraw 撤回报名
GET  /api/enrollment/export        导出 Excel
```

**审批 (5 个)**
```
GET  /api/approval/college         待学院审批的
GET  /api/approval/school          待学校审批的
POST /api/approval/approve         通过
POST /api/approval/reject          驳回
POST /api/approval/batch           批量操作
```

**反馈 (7 个)**
```
POST /api/feedback/student         学生提交反馈
POST /api/feedback/teacher         教师提交反馈
GET  /api/feedback/college         学院的反馈
GET  /api/feedback/school          全校的反馈
GET  /api/feedback/my              我的反馈
POST /api/feedback/{id}/reply      管理员回复
GET  /api/feedback/export          导出 Excel
```

**统计 (4 个)**
```
GET /api/statistics/dashboard      仪表盘数据
GET /api/statistics/trend          趋势图数据
GET /api/statistics/college        学院分布
GET /api/statistics/rating         评分分布
```

**AI (5 个)**
```
GET  /api/ai/school/suggest        搜学校名
POST /api/ai/school/normalize      标准化学校名
POST /api/ai/feedback/analyze      分析反馈情感
POST /api/ai/approval/suggest      审批建议
```