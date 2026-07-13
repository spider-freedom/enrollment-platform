# 新疆大学招生宣传报名平台

基于 **Spring Boot + Vue 3** 的招生宣传报名管理系统，支撑活动发布、报名、审批、反馈全流程，集成 AI 辅助分析。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端语言 | Java | 17 |
| 后端框架 | Spring Boot | 3.2.5 |
| 安全框架 | Spring Security + JWT | — |
| ORM | MyBatis Plus | 3.5.7 |
| AI 框架 | LangChain4j (DeepSeek) | 0.35.0 |
| API 文档 | Knife4j (Swagger) | 4.5.0 |
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
mvn spring-boot:run
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
| 前端界面 | http://localhost:3000 |
| 后端 API | http://localhost:8080 |
| 接口文档 | http://localhost:8080/doc.html |
| 健康检查 | http://localhost:8080/actuator/health |

## 项目结构

```
enrollment-platform/
├── database/
│   └── init.sql                       # 数据库初始化（6 张表 + 种子数据）
├── enrollment-backend/                # Spring Boot 后端
│   └── src/main/java/com/xju/enrollment/
│       ├── ai/                        # AI 模块（LangChain4j + DeepSeek）
│       ├── common/                    # 通用类（ApiResponse / PageResult / 异常处理）
│       ├── config/                    # 配置（CORS / MyBatisPlus / MetaObjectHandler）
│       ├── controller/                # AI 控制器
│       ├── entity/                    # 6 个实体类
│       ├── mapper/                    # MyBatis-Plus Mapper 接口
│       ├── modules/
│       │   ├── activity/              # 活动管理（CRUD + 分类 + 级别 + Banner）
│       │   ├── enrollment/            # 报名管理（提交 / 撤回 / 导出）
│       │   ├── feedback/              # 反馈管理（提交 / 回复 / 附件 / 导出）
│       │   ├── statistics/            # 数据大屏（仪表盘 / 学院分布 / 评分）
│       │   ├── system/                # 用户管理 + 认证 + 注册
│       │   └── workflow/              # 审批流程（院级 → 校级两级审批）
│       └── security/                  # JWT + Spring Security（无状态会话）
├── enrollment-frontend/               # Vue 3 前端
│   └── src/
│       ├── api/                       # Axios 封装 + 11 个 API 模块
│       ├── layouts/                   # MainLayout（侧边栏 / 导航 / 通知）
│       ├── router/                    # 路由 + 角色守卫
│       ├── stores/                    # Pinia 用户状态管理
│       ├── types/                     # TypeScript 接口定义
│       ├── utils/                     # 常量工具（状态 / 类型 / 角色映射）
│       └── views/                     # 31 个页面组件
└── doc/                               # 文档资源
```

## 数据库设计

| 表 | 说明 | 关键字段 |
|------|------|------|
| `sys_user` | 用户表 | username, role(STUDENT/TEACHER/COLLEGE_ADMIN/SCHOOL_ADMIN), college_id |
| `activity` | 活动表 | type(ONLINE/OFFLINE), category(宣讲会/开放日/…), level(院级/校级), status(DRAFT/PUBLISHED/ONGOING/ENDED), college_id |
| `activity_field_config` | 活动自定义字段 | field_name, field_type, required |
| `enrollment` | 报名表 | activity_id, user_id, status(SUBMITTED/APPROVING/APPROVED/REJECTED/WITHDRAWN), target_school |
| `feedback` | 反馈表 | activity_id, user_id, content, rating(1-5), status(SUBMITTED/REPLIED/CLOSED) |
| `feedback_attachment` | 反馈附件表 | feedback_id, file_name, file_path |

## 种子数据

- **45 个用户**：8 名学生 + 3 名教师 + 10 名学院管理员 + 2 名学校管理员（+ 旧数据 22 个）
- **24 个活动**：16 个院级活动（覆盖 8 个学院）+ 8 个校级活动
- **21 条报名记录**：覆盖 SUBMITTED / APPROVING / APPROVED / REJECTED 各状态
- **7 条反馈**：含已回复和未回复

## 功能模块

### 学生端 / 教师端
- 活动列表（Banner 轮播 + 分类筛选 + 搜索 + 分页）
- 活动详情（名额进度条 + 每校限额 + 活动级别）
- 报名提交（目标学院 + 个人简介 + AI 学校名建议）
- 已报名活动（查看审批进度 + 撤回报名）
- 提交反馈（评分 + 内容 + 院系信息）
- 我的反馈（查看管理员回复）

### 学院管理员端
- **活动管理**：创建 / 编辑 / 删除本院院级活动（自动挂载学院归属）
- **报名审批**：审批本院学生报名（通过 / 驳回 + 批量操作 + AI 建议）
- **反馈管理**：查看本院反馈 + 回复 + 附件上传
- **用户管理**：本院用户增删改查 + 提升 / 降级管理员 + 状态开关 + CSV 导入 + 分页

### 学校管理员端（招生办）
- **数据大屏**：统计卡片 + 活动分类统计 + 学院报名分布 + 评分分布
- **活动管理**：全校活动 CRUD + 院级/校级设置 + Banner 轮播 + Excel 导出
- **报名审批**：全校范围审批（院级审批通过后流转至校级）+ 按学院筛选
- **反馈管理**：全校反馈查看 + 回复 + AI 情感分析
- **用户管理**：全校用户 + 权限提升（学院/学校管理员）+ 分页

### AI 功能（LangChain4j + DeepSeek API）
- 学校名称 NLP 标准化 / 模糊匹配建议
- AI 审批建议（分析 GPA + 目标学校 + 报名数据给出建议 / 理由 / 风险）
- 反馈情感分析 + 摘要提取

### 审批流程
```
学生提交 → SUBMITTED → 学院管理员审批 → APPROVING → 学校管理员审批 → APPROVED
              ↓ 驳回                    ↓ 驳回
          REJECTED                  REJECTED
```

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
| `col_math` | 学院管理员 | 数学与系统科学学院 |
| `col_info` | 学院管理员 | 信息科学与工程学院 |
| `col_chem` | 学院管理员 | 化学化工学院 |
| `col_bio` | 学院管理员 | 生命科学与技术学院 |
| `col_phy` | 学院管理员 | 物理科学与技术学院 |
| `col_eng` | 学院管理员 | 外国语学院 |
| `col_marx` | 学院管理员 | 马克思主义学院 |
| `sch01` | 学校管理员 | 招生办公室 |