# 新疆大学招生宣传报名平台

基于 **Spring Boot + Vue 3** 的招生宣传报名管理系统，含公开招生网站 + 内部管理后台，支撑活动发布、报名、审批、反馈全流程，集成 AI 辅助分析。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端语言 | Java | 17 |
| 后端框架 | Spring Boot | 3.2.5 |
| 安全框架 | Spring Security + JWT (JJWT) | 0.12.5 |
| ORM | MyBatis Plus | 3.5.7 |
| AI 框架 | LangChain4j (DeepSeek) | 0.35.0 |
| API 文档 | Knife4j (Swagger) | 4.5.0 |
| Excel | Apache POI | 5.2.5 |
| 前端框架 | Vue 3 + TypeScript | 3.4+ / 5.4+ |
| UI 组件 | Element Plus | 2.7+ |
| 图表 | ECharts | 5.5+ |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | 7.x |

## 快速启动

### 1. 环境准备

- JDK 17+
- Node.js 18+
- MySQL 8.0（端口 3306，用户 `root`，密码 `123456`）
- Redis（端口 6379，密码 `123456`）
- Maven 3.8+

### 2. 初始化数据库

```bash
mysql -u root -p123456 < database/init.sql
```

### 3. 启动后端

```bash
cd enrollment-backend
mvn clean spring-boot:run
# 启动于 http://localhost:8080
```

### 4. 启动前端

```bash
cd enrollment-frontend
npm install
npm run dev
# 启动于 http://localhost:3000，API 代理至 :8080
```

### 5. 访问

| 服务 | 地址 |
|------|------|
| 公开首页 | http://localhost:3000 |
| 登录 | http://localhost:3000/login |
| 后端 API | http://localhost:8080 |
| 接口文档 | http://localhost:8080/doc.html |

## 项目结构

```
enrollment-platform/
├── database/
│   └── init.sql                         # 数据库初始化（7 张表 + 种子数据）
├── doc/
│   ├── architecture.md                  # 架构设计文档
│   └── sprint-plan.md                   # 团队开发计划
├── enrollment-backend/                  # Spring Boot 后端
│   └── src/main/java/com/xju/enrollment/
│       ├── ai/                          # AI 模块（LangChain4j + DeepSeek）
│       ├── common/                      # 通用类（响应/分页/异常/Excel）
│       ├── config/                      # 配置（CORS/MyBatisPlus/MetaHandler/WebMvc）
│       ├── controller/                  # 控制器（AI控制器含政策接口）
│       ├── entity/                      # 7 个实体类（含 Policy）
│       ├── mapper/                      # 7 个 Mapper 接口
│       ├── modules/
│       │   ├── activity/                # 活动管理（CRUD + 分类 + 级别 + Banner）
│       │   ├── enrollment/              # 报名管理（提交/撤回/导出）
│       │   ├── feedback/                # 反馈管理（提交/回复/附件/导出）
│       │   ├── statistics/              # 数据统计（仪表盘/学院分布/评分）
│       │   ├── system/                  # 用户管理 + 认证 + 注册
│       │   └── workflow/                # 审批流程（院级→校级两级审批）
│       └── security/                    # JWT + Spring Security
├── enrollment-frontend/                 # Vue 3 前端
│   └── src/
│       ├── api/                         # Axios 封装 + 12 个 API 模块
│       ├── data/                        # 政策 JSON 数据
│       ├── layouts/                     # PublicLayout(公开) + MainLayout(管理)
│       ├── router/                      # 路由（公开 + 4 角色 + 守卫）
│       ├── stores/                      # Pinia 用户状态
│       ├── styles/                      # 主题 CSS（XJU 红色系）
│       ├── types/                       # TS 接口
│       ├── utils/                       # 常量（状态映射 + 日期驱动）
│       └── views/                       # 38 个页面组件
└── start.bat
```

## 数据库设计

| 表 | 说明 | 关键字段 |
|------|------|------|
| `sys_user` | 用户表 | username, role(STUDENT/TEACHER/COLLEGE_ADMIN/SCHOOL_ADMIN), college_id |
| `activity` | 活动表 | type(ONLINE/OFFLINE), category(9种), level(院级/校级), college_id |
| `enrollment` | 报名表 | activity_id, user_id, status(SUBMITTED→APPROVING→APPROVED) |
| `feedback` | 反馈表 | content, rating(1-5), status(SUBMITTED/REPLIED/CLOSED) |
| `feedback_attachment` | 反馈附件表 | feedback_id, file_name, file_path |
| `activity_field_config` | 活动自定义字段 | field_name, field_type, required |
| `policy` | 招生政策表 | title, content, type(章程/简章/通知/办法/数据) |

## 种子数据

- **45 个用户**：11 名学生 + 5 名教师 + 12 名学院管理员 + 2 名学校管理员
- **24 个活动**：16 个院级（8 个学院）+ 8 个校级
- **21 条报名记录**：覆盖 SUBMITTED / APPROVING / APPROVED / REJECTED
- **7 条反馈**：含已回复和未回复
- **6 条政策**：2024-2026 年招生章程、简章、通知（新疆大学官网真实数据）

## 页面结构

### 公开前台（无需登录）

| 路径 | 页面 | 说明 |
|------|------|------|
| `/` | 首页 | Hero 大屏 + 统计数字 + 特色卡片 + CTA |
| `/about` | 学校概况 | 校史 + 三大校区 + 校训 |
| `/policy` | 招生政策 | 政策列表（数据库）+ 详情弹窗 + 历年录取分数（省份/年份/文理切换） |
| `/majors` | 专业查询 | 搜索 + 学院筛选 chips + 3 列卡片 |
| `/activities` | 活动报名 | 公开活动列表（浏览无需登录，报名需登录） |
| `/ai-assistant` | AI 咨询 | 聊天界面（气泡 + 建议 chips + 输入框） |
| `/login` | 登录 | 角色选择器（必须选身份）+ 密码显示切换 |
| `/register` | 注册 | 学生/教师注册 |

### 管理后台（需登录）

| 角色 | 路径 | 功能 |
|------|------|------|
| 学生 | `/student` | 个人主页（头像横幅 + 我的活动 + 我的反馈） |
| | `/student/activities` | 活动列表 + 报名 |
| | `/student/enrollments` | 已报名活动 + 撤回 |
| | `/student/my-feedback` | 我的反馈 + 查看回复 |
| | `/student/profile` | 个人信息 + 头像上传 + 密码修改 |
| 教师 | `/teacher/*` | 同学生端结构 |
| 学院管理员 | `/college/activities` | 本院院级活动 CRUD |
| | `/college/approvals` | 审批本院报名（通过/驳回/批量） |
| | `/college/feedbacks` | 本院反馈管理 + 回复 |
| | `/college/users` | 本院用户管理 + CSV 导入 |
| 学校管理员 | `/school/dashboard` | 数据大屏（统计卡片 + 反馈 + 热门活动） |
| | `/school/activities` | 全校活动 CRUD + 院级/校级 + Banner + 导出 |
| | `/school/approvals` | 全校审批（APPROVING 状态） |
| | `/school/feedbacks` | 全校反馈管理 + 回复 |
| | `/school/users` | 全校用户管理 + 权限设置 |

### 审批流程
```
学生提交 → SUBMITTED → 学院管理员审批 → APPROVING → 学校管理员审批 → APPROVED
              ↓ 驳回                    ↓ 驳回
          REJECTED                  REJECTED
```

### AI 功能（LangChain4j + DeepSeek，需配置 API Key）
- 学校名称 NLP 标准化 / 模糊匹配建议
- AI 审批建议（GPA + 目标学校 + 报名数据分析）
- 反馈情感分析 + 摘要提取

### 设计系统
- 主色：新疆大学红 `#A31F34` | 辅色：金 `#C9A96E` | 蓝 `#2E7FB9`
- 公开前台：红色顶栏 + 白色内容 + 深灰页脚
- 管理后台：白色侧边栏 + 角色色激活态 + 红色顶栏（日期显示）

## 测试账号

密码统一：`123456`

| 账号 | 角色 | 所属学院 |
|------|------|------|
| `stu01` | 学生 | 计算机科学与技术学院 |
| `stu02` | 学生 | 计算机科学与技术学院 |
| `stu03` | 学生 | 数学与系统科学学院 |
| `tch01` | 教师 | 计算机科学与技术学院 |
| `tch02` | 教师 | 数学与系统科学学院 |
| `col01` | 学院管理员 | 计算机科学与技术学院 |
| `col02` | 学院管理员 | 数学与系统科学学院 |
| `col_cs` | 学院管理员 | 计算机科学与技术学院 |
| `col_math` | 学院管理员 | 数学与系统科学学院 |
| `col_info` | 学院管理员 | 信息科学与工程学院 |
| `col_chem` | 学院管理员 | 化学化工学院 |
| `col_bio` | 学院管理员 | 生命科学与技术学院 |
| `col_phy` | 学院管理员 | 物理科学与技术学院 |
| `col_eng` | 学院管理员 | 外国语学院 |
| `col_marx` | 学院管理员 | 马克思主义学院 |
| `sch01` | 学校管理员 | 招生办公室 |