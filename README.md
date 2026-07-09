# 新疆大学招生宣传报名平台

新疆大学招生宣传报名管理系统，支撑招生宣传活动的发布、报名、审批、反馈全流程，集成 AI 辅助分析与工作流引擎。

## 技术栈

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=springboot)
![Vue](https://img.shields.io/badge/Vue-3.x-4FC08D?logo=vue.js)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?logo=typescript)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)
![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?logo=redis)
![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.7-1E90FF)
![Flowable](https://img.shields.io/badge/Flowable-7.0.1-007AFF)
![LangChain4j](https://img.shields.io/badge/LangChain4j-0.35.0-FF6F00)
![Knife4j](https://img.shields.io/badge/Knife4j-4.5.0-00B388)
![Element Plus](https://img.shields.io/badge/Element%20Plus-latest-409EFF?logo=elementplus)
![ECharts](https://img.shields.io/badge/ECharts-5.x-AA344D)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)

## 快速启动

### 1. 环境准备

- JDK 17+
- Node.js 18+
- MySQL 8.0
- Redis 7.x
- Maven 3.8+

### 2. 启动基础服务 (MySQL + Redis)

```bash
docker-compose up -d
```

### 3. 初始化数据库

数据库初始化脚本在 `database/init.sql`，Docker 启动时会自动执行。手动导入：

```bash
mysql -u root -p < database/init.sql
```

### 4. 启动后端

```bash
cd enrollment-backend
mvn spring-boot:run
```

或使用启动脚本：

```bash
./start-backend.bat    # Windows
```

### 5. 启动前端

```bash
cd enrollment-frontend
npm install
npm run dev
```

或使用启动脚本：

```bash
./start-frontend.bat   # Windows
```

### 6. 访问

| 服务     | 地址                                    |
| -------- | --------------------------------------- |
| 前端     | http://localhost:5173                    |
| 后端 API | http://localhost:8080                    |
| Swagger  | http://localhost:8080/doc.html           |
| Actuator | http://localhost:8080/actuator/health    |

## API 文档

启动后端后访问 Swagger/Knife4j 接口文档：

- **Knife4j 文档**: [http://localhost:8080/doc.html](http://localhost:8080/doc.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## 项目结构

```
enrollment-platform/
├── database/                     # 数据库初始化脚本
│   └── init.sql
├── doc/                          # 项目文档
├── docker-compose.yml            # Docker 编排文件
├── enrollment-backend/           # Spring Boot 后端
│   ├── src/main/java/com/xju/enrollment/
│   │   ├── ai/                   # AI 服务（LangChain4j）
│   │   ├── common/               # 通用类（响应、异常、分页）
│   │   ├── config/               # 配置（CORS、MyBatis Plus）
│   │   ├── controller/           # 旧版控制器
│   │   ├── entity/               # 实体类
│   │   ├── mapper/               # MyBatis Mapper
│   │   ├── modules/              # 功能模块
│   │   │   ├── activity/         # 活动管理
│   │   │   ├── enrollment/       # 报名管理
│   │   │   ├── feedback/         # 反馈管理
│   │   │   ├── statistics/       # 数据统计
│   │   │   ├── system/           # 系统管理（用户、认证）
│   │   │   └── workflow/         # 审批工作流（Flowable）
│   │   └── security/             # 安全（JWT）
│   └── src/main/resources/
│       ├── application.yml       # 应用配置
│       └── mapper/               # MyBatis XML 映射文件
├── enrollment-frontend/          # Vue 3 前端
│   └── src/
│       ├── api/                  # API 接口封装
│       ├── layouts/              # 布局组件
│       ├── router/               # 路由配置
│       ├── stores/               # Pinia 状态管理
│       ├── types/                # TypeScript 类型定义
│       └── views/                # 页面组件
├── start-backend.bat             # 后端一键启动
├── start-frontend.bat            # 前端一键启动
└── README.md
```

## 功能模块

- **活动管理**: 校级管理员创建/编辑招生宣传活动，支持线上/线下活动
- **报名管理**: 学生/教师报名参与活动，提交报名表单，支持自定义字段
- **审批工作流**: 学院管理员一审 + 校级管理员二审，支持批量审批
- **活动反馈**: 参与人员提交活动反馈，支持评分和附件上传
- **AI 辅助**: DeepSeek 驱动的学校名称标准化和反馈分析
- **数据大屏**: ECharts 可视化展示报名趋势、学院分布、评分统计
- **通知系统**: 审批结果、反馈回复自动推送站内通知

## 测试账号

| 角色           | 用户名     | 密码    | 说明                   |
| -------------- | ---------- | ------- | ---------------------- |
| 学生           | 2021001    | 123456  | 计算机科学与技术学院   |
| 学生           | 2021002    | 123456  | 计算机科学与技术学院   |
| 学生           | 2022001    | 123456  | 数学与系统科学学院     |
| 学生           | 2022002    | 123456  | 数学与系统科学学院     |
| 学生           | 2023001    | 123456  | 信息科学与工程学院     |
| 教师           | T2021001   | 123456  | 计算机科学与技术学院   |
| 教师           | T2022001   | 123456  | 数学与系统科学学院     |
| 学院管理员     | C2021001   | 123456  | 计算机科学与技术学院   |
| 校级管理员     | S2021001   | 123456  | 校级管理员             |
