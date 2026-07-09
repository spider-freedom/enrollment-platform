# 新疆大学招生宣传报名平台

招生宣传报名管理系统，支撑活动发布、报名、审批、反馈全流程，集成 AI 辅助分析。

## 技术栈

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=springboot)
![Vue](https://img.shields.io/badge/Vue-3.x-4FC08D?logo=vue.js)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?logo=typescript)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)
![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.7-1E90FF)
![LangChain4j](https://img.shields.io/badge/LangChain4j-0.35.0-FF6F00)
![Knife4j](https://img.shields.io/badge/Knife4j-4.5.0-00B388)
![Element Plus](https://img.shields.io/badge/Element%20Plus-latest-409EFF?logo=elementplus)
![ECharts](https://img.shields.io/badge/ECharts-5.x-AA344D)

## 快速启动

### 1. 环境准备

- JDK 17+
- Node.js 18+
- MySQL 8.0
- Maven 3.8+

### 2. 初始化数据库

```bash
mysql -u root -p < database/init.sql
```

### 3. 启动后端

```bash
cd enrollment-backend
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
| 前端 | http://localhost:3000 |
| 后端 API | http://localhost:8080 |
| Swagger 文档 | http://localhost:8080/doc.html |

## 项目结构

```
enrollment-platform/
├── database/init.sql              # 数据库初始化（6 张表 + 初始数据）
├── enrollment-backend/            # Spring Boot 后端（65 Java 文件）
│   └── src/main/java/com/xju/enrollment/
│       ├── ai/                    # AI 模块（LangChain4j）
│       ├── common/                # 通用类
│       ├── config/                # 配置
│       ├── entity/                # 实体类
│       ├── mapper/                # MyBatis Mapper
│       ├── modules/
│       │   ├── activity/          # 活动管理
│       │   ├── enrollment/        # 报名管理
│       │   ├── feedback/          # 反馈管理
│       │   ├── statistics/        # 数据统计
│       │   ├── system/            # 用户管理 + 认证
│       │   └── workflow/          # 审批流程
│       └── security/              # JWT 安全
├── enrollment-frontend/           # Vue 3 前端（22 Vue 文件）
│   └── src/
│       ├── api/                   # API 封装
│       ├── layouts/               # 布局
│       ├── router/                # 路由
│       ├── stores/                # 状态管理
│       └── views/                 # 页面（18 个业务页面）
└── start.bat                      # 一键启动脚本
```

## 数据库（6 张业务表）

| 表 | 说明 |
|------|------|
| `sys_user` | 用户表 |
| `activity` | 活动表 |
| `activity_field_config` | 活动自定义字段 |
| `enrollment` | 报名表 |
| `feedback` | 反馈表 |
| `feedback_attachment` | 反馈附件表 |

## 功能模块

### 学生端 / 教师端
- 活动列表（轮播图 + 搜索 + 筛选 + 分页）
- 活动详情 → 报名提交
- 已报名活动（查看审批进度 + 退出报名）
- 提交反馈（评分 + 内容 + 院系）
- 我的反馈（查看管理员回复）

### 学院管理员端
- 活动列表查看
- 报名审批（通过/驳回 + 批量操作 + AI 建议）
- 反馈管理（查看 + 回复 + 附件上传）
- 用户管理（本院用户增删改查 + 提升/降级管理员 + 状态开关 + CSV 导入 + 重置密码）

### 学校管理员端（招生办）
- 数据大屏（报名趋势 + 学院分布 + 评分统计）
- 活动管理（创建/编辑/删除 + 设为轮播图 + Excel 导出 + 查看活动详情）
- 报名审批（全校范围 + 按学院筛选）
- 反馈管理（全校范围 + AI 分析）
- 用户管理（全校用户 + 权限设置 + 提升为学校/学院管理员）

### AI 功能（LangChain4j + DeepSeek）
- 学校名称 NLP 标准化
- AI 审批建议（分析报名申请给出建议/理由/风险）
- 反馈情感分析 + 关键词提取

## 测试账号

密码统一：`123456`

| 账号 | 角色 |
|------|------|
| `student1` | 学生 |
| `student2` | 学生 |
| `student3` | 学生（软件学院） |
| `teacher1` | 教师 |
| `teacher2` | 教师（软件学院） |
| `college1` | 学院管理员 |
| `college2` | 学院管理员（软件学院） |
| `school1` | 学校管理员 |
