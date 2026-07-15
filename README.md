# 新疆大学招生宣传报名平台

基于 **Spring Boot + Vue 3** 的全栈招生管理系统，包含公开招生网站 + 内部管理后台，覆盖活动发布、报名、审批、反馈全流程，集成百炼 AI 大模型辅助分析。

---

## 项目截图

### 公开前台

| 首页 | 登录 |
|------|------|
| ![首页](screenshots/home.png) | ![登录](screenshots/login.png) |

| 招生政策 | 专业查询 |
|----------|----------|
| ![政策](screenshots/policy.png) | ![专业](screenshots/majors.png) |

| 学校概况 | AI 咨询 |
|----------|---------|
| ![概况](screenshots/about.png) | ![AI](screenshots/ai.png) |

### 管理后台

| 学生主页 | 活动列表 |
|----------|----------|
| ![学生](screenshots/student.png) | ![活动](screenshots/activities.png) |

| 学院审批 | 数据大屏 |
|----------|----------|
| ![审批](screenshots/approvals.png) | ![大屏](screenshots/dashboard.png) |

| 用户管理 | 个人信息 |
|----------|----------|
| ![用户](screenshots/users.png) | ![个人](screenshots/profile.png) |

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端语言 | Java | 17 |
| 后端框架 | Spring Boot | 3.2.5 |
| 安全框架 | Spring Security + JWT (JJWT) | 0.12.5 |
| ORM | MyBatis Plus | 3.5.7 |
| AI 框架 | LangChain4j（阿里云百炼 qwen-plus） | 0.35.0 |
| API 文档 | Knife4j (Swagger) | 4.5.0 |
| Excel | Apache POI | 5.2.5 |
| 前端框架 | Vue 3 + TypeScript | 3.4 / 5.4 |
| UI 组件 | Element Plus | 2.7 |
| 图表 | ECharts | 5.5 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | 7.x |

---

## 快速启动

### 1. 环境准备

- JDK 17+
- Node.js 18+
- MySQL 8.0（端口 3306，用户 `root`，密码 `123456`）
- Redis（端口 6379，密码 `123456`）
- Maven 3.8+

### 2. 初始化数据库

```bash
mysql -u root -p123456 --default-character-set=utf8mb4 < database/init.sql
```

### 3. 启动后端

```bash
cd enrollment-backend

# 需要配置 AI API Key（可选，不配则 AI 功能不可用）
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-DDEEPSEEK_API_KEY=你的百炼API密钥"

# 不需要 AI 功能直接启动
mvn spring-boot:run
```

### 4. 启动前端

```bash
cd enrollment-frontend
npm install
npm run dev
```

### 5. 访问

| 服务 | 地址 |
|------|------|
| 公开首页 | http://localhost:3000 |
| 登录 | http://localhost:3000/login |
| 后端 API | http://localhost:8080 |
| 接口文档 | http://localhost:8080/doc.html |
| 健康检查 | http://localhost:8080/actuator/health |

---

## 项目结构

```
enrollment-platform/
├── database/
│   └── init.sql                     # 数据库初始化（7张表 + 种子数据）
├── doc/
│   ├── architecture.md              # 架构设计文档
│   ├── sprint-plan.md               # 团队开发计划
│   └── enrollment-subtasks.md       # 分工子任务清单
├── enrollment-backend/              # Spring Boot 后端（70+ Java 文件）
│   └── src/main/java/com/xju/enrollment/
│       ├── ai/                      # AI 模块（LangChain4j + 百炼）
│       ├── common/                  # 通用组件
│       ├── config/                  # 配置类
│       ├── controller/              # 控制器
│       ├── entity/                  # 7个实体类
│       ├── mapper/                  # 7个Mapper接口
│       ├── modules/
│       │   ├── activity/            # 活动管理
│       │   ├── enrollment/          # 报名管理
│       │   ├── feedback/            # 反馈管理
│       │   ├── statistics/          # 数据统计
│       │   ├── system/              # 用户管理 + 认证
│       │   └── workflow/            # 审批流程
│       └── security/                # JWT + Spring Security
├── enrollment-frontend/             # Vue 3 前端（40+ 源文件）
│   └── src/
│       ├── api/                     # Axios + 12个API模块
│       ├── data/                    # JSON 数据
│       ├── layouts/                 # PublicLayout + MainLayout
│       ├── router/                  # 路由 + 角色守卫
│       ├── stores/                  # Pinia 用户状态
│       ├── styles/                  # 主题样式
│       ├── types/                   # TS 接口
│       ├── utils/                   # 常量工具
│       └── views/                   # 40 个页面组件
│       └── public/images/           # 校园照片素材
└── start.bat
```

---

## 数据库设计

| 表名 | 记录 | 说明 |
|------|------|------|
| `sys_user` | 45 | 用户表：学生/教师/学院管理员/学校管理员 |
| `activity` | 25 | 活动表：ONLINE/OFFLINE，9种分类，院级/校级 |
| `enrollment` | 29 | 报名表：SUBMITTED→APPROVING→APPROVED 两级审批 |
| `feedback` | 7 | 反馈表：评分1-5，状态：待回复/已回复/已关闭 |
| `feedback_attachment` | 0 | 附件表 |
| `activity_field_config` | 3 | 活动自定义字段配置 |
| `policy` | 4 | 招生政策表 |

---

## 功能模块

### 公开前台（无需登录）
| 页面 | 说明 |
|------|------|
| 首页 `/` | Hero大屏 + 统计数字 + 特色卡片 + CTA横幅（真实校园照片背景） |
| 学校概况 `/about` | 校史 + 三大校区 + 校训 |
| 招生政策 `/policy` | 政策列表（数据库存取）+ 详情弹窗 + 历年录取分数（省份/年份/专业筛选） |
| 专业查询 `/majors` | 搜索 + 学院筛选 + 3列专业卡片 |
| 活动报名 `/activities` | 公开浏览活动列表（报名需登录） |
| AI咨询 `/ai-assistant` | 百炼大模型对话 + 建议快捷提问 |
| 登录 `/login` | 角色选择器（必选身份）+ 身份校验 + 照片背景 |
| 注册 `/register` | 学生/教师注册 |

### 学生端
- 活动列表 + 报名（我的学院自动填入 + 个人简介）
- 已报名活动（查看进度：等待学院审批→等待学校审批→已通过）+ 撤回
- 提交反馈（评分 + 内容）+ 查看回复
- 个人信息（头像上传 + 信息编辑 + 密码修改）

### 教师端
- 同学生端结构，报名弹窗适配教师角色

### 学院管理员端
- 活动管理（本院院级活动 CRUD）
- 报名审批（通过/驳回/批量操作 + AI 建议）
- 反馈管理（查看/回复本院反馈）
- 用户管理（增删改查 + CSV导入 + 角色管理 + 分页）

### 学校管理员端
- 数据大屏（统计卡片 + 图表 + 热门活动）
- 活动管理（全校活动 CRUD + 院级/校级 + 导出）
- 报名审批（全校 + 学院/活动/状态筛选）
- 反馈管理（全校 + 回复）
- 用户管理（全校 + 权限设置 + 分页）

### 审批流程
```
学生提交 → SUBMITTED → 学院管理员审批 → APPROVING → 学校管理员审批 → APPROVED
              ↓ 驳回                     ↓ 驳回
          REJECTED                   REJECTED
              ↓ 撤回                     ↓ 撤回
          WITHDRAWN                  WITHDRAWN
```

### 设计系统
- 主色：新疆大学红 `#A31F34`
- 辅色：金 `#C9A96E` / 蓝 `#2E7FB9`
- 公开前台：红色顶栏 + 真实校园照片背景 + 深灰页脚
- 管理后台：白色侧边栏 + 角色色激活态 + 红色顶栏

---

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
| `col_cs` | 学院管理员 | 计算机科学与技术学院 |
| `col_math` | 学院管理员 | 数学与系统科学学院 |
| `col_info` | 学院管理员 | 信息科学与工程学院 |
| `col_chem` | 学院管理员 | 化学化工学院 |
| `col_bio` | 学院管理员 | 生命科学与技术学院 |
| `col_phy` | 学院管理员 | 物理科学与技术学院 |
| `col_eng` | 学院管理员 | 外国语学院 |
| `col_marx` | 学院管理员 | 马克思主义学院 |
| `sch01` | 学校管理员 | 招生办公室 |