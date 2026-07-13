# 新疆大学招生宣传报名平台 团队开发计划

> 团队规模：4 人 | 预计工期：4 周 | 2026-07-13

---

## 分工总览

| 成员 | 负责模块 | 技术栈 | 工作量 |
|------|---------|--------|--------|
| A | 后端核心 | Java + Spring Boot + Security + JWT + MySQL | 数据库、认证、用户管理 |
| B | 后端业务 | Java + Spring Boot + MyBatis-Plus + Redis | 活动、报名、审批、反馈、统计 |
| C | 前端用户端 | Vue 3 + Element Plus + Axios | 登录注册、学生端、教师端 |
| D | 前端管理端 | Vue 3 + Element Plus + ECharts | 学院管理、学校管理、数据大屏 |

---

## A 成员：后端核心

负责搭建后端工程骨架、数据库设计、认证授权体系、用户管理模块。

### 史诗 1：项目初始化与数据库设计

**故事 1.1：创建 Spring Boot 项目骨架**
- 描述：用 Maven 创建 Spring Boot 3.2.5 项目，配置 pom.xml 依赖（Web、Security、MyBatis-Plus、Redis、JWT、Knife4j、POI、Hutool）。配置 application.yml（数据源、Redis、JWT 密钥、文件上传路径）。
- 验收：项目能成功编译启动，/actuator/health 返回 UP。

**故事 1.2：设计数据库表结构**
- 描述：编写 init.sql，建 6 张表：sys_user、activity、enrollment、feedback、feedback_attachment、activity_field_config。包含索引、外键、默认值、注释。编写种子数据（45 个用户、24 个活动、21 条报名、7 条反馈）。
- 验收：执行 init.sql 后数据库完整可用。

**故事 1.3：创建实体类和 Mapper 接口**
- 描述：创建 User、Activity、Enrollment、Feedback、FeedbackAttachment、ActivityFieldConfig 六 个实体类，用 @TableName 映射表名，@TableId 标注主键，@TableField 标注自动填充字段。创建对应的 6 个 Mapper 接口继承 BaseMapper。
- 验收：MyBatis-Plus 能正确映射所有表和字段，selectById 能查到数据。

**故事 1.4：配置 MyBatis-Plus 和自动填充**
- 描述：配 置 MybatisPlusConfig（分页插件 MySQL 方言），配 置 MetaObjectHandlerConfig（插入和更新时自动填充 createTime 和 updateTime）。在实体类字段上加 @TableField(fill = ...) 注解。
- 验收：插入记录时 createTime 和 updateTime 自动填入当前时间，更新记录时 updateTime 自动刷新。

### 史诗 2：认证与授权

**故事 2.1：实现 JWT 工具类**
- 描述：创建 JwtUtils，支持生成 Token（载荷含 userId、role、sub）、验证 Token 有效性、从 Token 中提取 userId 和 role。使用 HMAC-SHA384 签名算法，Token 有效期 24 小时（从 application.yml 读取配置）。
- 验收：调用 generateToken 能得到合法 Token，调用 validateToken 能正确判断有效/过期/伪造。

**故事 2.2：实现 JWT 认证过滤器**
- 描述：创建 JwtAuthFilter，从请求头 Authorization 中提取 Bearer Token，验证后设置 SecurityContext（包含 userId 和角色权限）。放行 /api/auth/**、/doc.html、/v3/api-docs/** 路径。
- 验收：不带 Token 访问 /api/user/profile 返回 401，带合法 Token 能正常访问。

**故事 2.3：配置 Spring Security**
- 描述：创建 SecurityConfig，禁用 CSRF，设置无状态会话。配置 URL 权限：/api/auth/** 公开，/api/college/** 需要 COLLEGE_ADMIN 角色，/api/school/** 需要 SCHOOL_ADMIN 角色，其余 /api/** 需登录。
- 验收：学生 Token 访问 /api/college/users/list 返回 403，学院管理员 Token 能正常访问。

**故事 2.4：实现密码加密和通用安全工具**
- 描述：在 SecurityConfig 中注册 BCryptPasswordEncoder Bean。创建 SecurityUtils 工具类，提供 getCurrentUserId() 静态方法从 SecurityContext 中获取当前登录用户 ID。
- 验收：注册用户时密码 BCrypt 加密存储，登录时能正确验证。

### 史诗 3：用户管理

**故事 3.1：实现登录和注册接口**
- 描述：创建 AuthController 和 UserService.login()、UserService.register()。登录：根据 username 查用户，BCrypt 验证密码，检查是否被禁用，返回 JWT Token + 用户信息（id、角色、学院）。注册：校验用户名唯一，仅允许学生和教师角色，加密密码后插入。
- 验收：用正确的用户名密码登录返回 Token，用错误密码登录提示"账号与密码不匹配"。

**故事 3.2：实现个人信息管理接口**
- 描述：创建 UserController，实现 getProfile（获取当前用户完整信息），updateProfile（更新姓名、学院、专业、年级、GPA、邮箱、手机、头像），changePassword（验证旧密码后更新为新密码加密值），uploadAvatar（接收 MultipartFile，保存到 ./uploads/avatars/，更新 user.avatar 字段）。
- 验收：能查看和修改个人信息，上传头像后能通过 URL 访问。

**故事 3.3：实现学院用户管理接口**
- 描述：创建 CollegeUserController，实现 listUsers（当前学院管理员所在学院的所有用户）、listAdmins（当前学院的管理员列表）、promote（教师提升为学院管理员）、demote（学院管理员降为教师）、resetPassword（重置密码为 123456）、toggleStatus（启用/禁用用户）、deleteUser（软删除，设为 DISABLED）、importUsers（CSV 文件批量导入）。
- 验收：学院管理员能管理本院用户，无法操作其他学院用户。

**故事 3.4：实现学校用户管理接口**
- 描述：创建 SchoolUserController，实现 listAll（全校所有用户）、promoteToCollegeAdmin（提升为学院管理员）、promoteToSchoolAdmin（提升为学校管理员）、demoteToTeacher（降为教师）、toggleStatus、resetPassword、deleteUser。
- 验收：学校管理员能管理所有学院的用户。

---

## B 成员：后端业务

负责活动管理、报名流程、审批流程、反馈管理、数据统计、AI 集成。

### 史诗 1：活动管理

**故事 1.1：实现活动增删改查接口**
- 描述：创建 ActivityController 和 ActivityService。createActivity：创建活动，默认状态 DRAFT。updateActivity：更新活动字段。deleteActivity：物理删除活动。getById：查活动详情（含创建人姓名）。toggleBanner：切换活动是否为轮播图。getBanners：查询所有轮播图活动（isBanner=1 且 PUBLISHED/ONGOING）。
- 验收：能创建、编辑、删除活动，能设置/取消轮播图。

**故事 1.2：实现活动列表接口（分角色）**
- 描述：实现 listForStudent（过滤 targetAudience=1或3 + 状态 PUBLISHED/ONGOING），listForTeacher（过滤 targetAudience=2或3 + 状态 PUBLISHED/ONGOING），listForCollege（院级活动 + 本院 collegeId），listForSchool（所有活动）。均支持 keyword 搜索、type 筛选、category 筛选、status 筛选、分页。
- 验收：各角色看到的活动列表符合权限规则。

**故事 1.3：实现活动 Excel 导出**
- 描述：创建 ExcelExportUtil 工具类，使用 Apache POI 生成 .xlsx 文件。ActivityController.export() 接收筛选条件，查询全量数据（最多 10000 条），输出标题、类型、状态、时间、地点等列。
- 验收：点击导出能下载包含活动数据的 Excel 文件。

### 史诗 2：报名管理

**故事 2.1：实现报名提交接口**
- 描述：创建 EnrollmentController 和 EnrollmentService.submit()。校验：活动存在且状态为 PUBLISHED/ONGOING，当前时间在报名时间窗口内，名额未满，用户未重复报名。通过后创建报名记录（状态=SUBMITTED），关联活动、用户、学院、目标学校信息。
- 验收：满足条件能成功报名，不满足条件返回明确错误提示。

**故事 2.2：实现报名列表和撤回接口**
- 描述：实现 listMy（当前用户的报名，支持分页，含活动标题），listForCollege（学院视角，筛选本院报名），listForSchool（学校视角，全校报名）。withdraw：撤回报名（仅 SUBMITTED 和 APPROVING 状态可撤回），状态改为 WITHDRAWN。
- 验收：学生能看到自己的报名列表和状态，能撤回未审批的报名。

**故事 2.3：实现报名 Excel 导出**
- 描述：按筛选条件导出报名数据到 Excel。
- 验收：点击导出能下载包含报名信息的 Excel 文件。

### 史诗 3：审批流程

**故事 3.1：实现学院审批接口**
- 描述：创建 ApprovalController 和 ApprovalService。getPendingListForCollege：查询当前学院管理员所在学院的 SUBMITTED 状态报名。approve：学院管理员审批通过 → 状态改为 APPROVING。reject：驳回 → 状态改为 REJECTED 并记录驳回原因。
- 验收：学院管理员能看到本院待审批报名，能通过或驳回。

**故事 3.2：实现学校审批接口**
- 描述：getPendingListForSchool：查询所有 APPROVING 状态报名。approve：学校管理员审批通过 → 状态改为 APPROVED，填入 approvedAt 时间，自动分组排名。reject：驳回。batchApprove：批量通过多条报名。
- 验收：学校管理员能看到所有待学校审批的报名，支持批量操作。

### 史诗 4：反馈管理

**故事 4.1：实现反馈提交接口**
- 描述：创建 FeedbackController 和 FeedbackService。submitStudent：校验学生已报名且状态为 APPROVED，未重复提交，创建反馈（含评分 1-5、内容、联系方式）。submitTeacher：同上，教师角色额外记录院系信息。
- 验收：已通过报名的学生/教师能提交反馈，未通过或已提交过的给出明确提示。

**故事 4.2：实现反馈管理和回复接口**
- 描述：listMy（当前用户反馈列表），listForCollege（学院管理员查看本院反馈），listForSchool（学校管理员查看全校反馈）。reply：管理员回复反馈 → 状态改为 REPLIED，记录回复人和时间。getDetail：查看单条反馈详情（含附件数量）。
- 验收：管理员能查看反馈列表和详情，能回复反馈。

**故事 4.3：实现附件上传下载接口**
- 描述：创建 AttachmentController 和 AttachmentService。upload：接收文件保存到 ./uploads/，记录 FeedbackAttachment 到数据库。download：根据附件 ID 流式读取文件并返回给客户端。
- 验收：能上传附件（支持常见格式），能下载。

### 史诗 5：数据统计

**故事 5.1：实现仪表盘统计接口**
- 描述：创建 StatisticsController 和 StatisticsService。getDashboard：统计活动总数、报名总数、审批通过率、反馈覆盖率、平均评分。getCollegeDistribution：按学院统计报名分布。getRatingDistribution：按 1-5 分统计评分分布。
- 验收：调用接口返回正确的统计数据。

### 史诗 6：AI 集成

**故事 6.1：配置 LangChain4j 对接 DeepSeek**
- 描述：创建 AiConfig，配置 ChatLanguageModel Bean（Base URL、API Key、模型名 deepseek-chat、temperature 0.3、maxTokens 4096）。
- 验收：能正常调用 DeepSeek API 并获得回复。

**故事 6.2：实现 AI 辅助功能**
- 描述：创建 SchoolNameNormalizer（学校名称标准化和模糊匹配建议），FeedbackAnalyzer（反馈情感分析和关键词提取），AiController（暴露 suggestSchool、normalizeSchool、analyzeFeedback、summarizeFeedback、approvalSuggest 五个端点）。
- 验收：输入非标准学校名能返回标准化结果 + 相似建议，输入反馈文本能返回情感倾向。

---

## C 成员：前端用户端

负责登录注册、学生端全部页面、教师端全部页面、个人信息页面。

### 史诗 1：项目初始化

**故事 1.1：搭建 Vue 3 项目骨架**
- 描述：用 Vite 创建项目，安装 Element Plus、Vue Router、Pinia、Axios、ECharts、Sass。配置 tsconfig.json（@ 别名），配置 vite.config.ts（/api 代理到 localhost:8080）。在 main.ts 中全局注册 Element Plus（中文语言包）和所有图标。
- 验收：npm run dev 能正常启动，访问 localhost:3000 能看到页面。

**故事 1.2：搭建路由和状态管理**
- 描述：创建 router/index.ts，配置 5 组路由：/login、/register（公开）、/student、/teacher、/college、/school（需登录）。实现 beforeEach 路由守卫：检查 token 是否存在，恢复用户信息，校验角色前缀匹配。创建 stores/user.ts（Pinia），管理 token、userInfo、login、logout、fetchProfile。
- 验收：未登录自动跳转登录页，不同角色只能访问自己的页面。

**故事 1.3：封装网络请求和 API 模块**
- 描述：创建 api/request.ts（axios 实例，baseURL=/api，30 秒超时，请求拦截器自动附加 Token，响应拦截器 401 跳转登录）。创建 api/index.ts，封装 11 个 API 模块：authApi、userApi、activityApi、enrollmentApi、approvalApi、feedbackApi、statisticsApi、collegeUserApi、schoolUserApi、aiApi。创建 types/index.ts（Activity、Enrollment、Feedback、User、PageResult 接口）。
- 验收：能正常调用后端接口，Token 过期自动跳登录。

**故事 1.4：创建公共组件和常量工具**
- 描述：创建 MainLayout.vue（深色侧边栏 + 顶部导航 + 通知铃铛 + 用户头像 + 退出按钮）。创建 utils/constants.ts（活动类型/状态/报名状态/反馈状态映射表，角色中文映射，活动分类选项，日期驱动的 getDisplayStatus 和 canEnroll 函数）。
- 验收：布局正常显示，侧边栏菜单根据角色动态切换。

### 史诗 2：认证页面

**故事 2.1：实现登录页面**
- 描述：创建 LoginView.vue。学号/工号输入框 + 密码输入框 + 登录按钮。登录成功后根据角色跳转对应首页。显示错误提示（账号不存在、密码错误、已禁用）。底部链接跳转注册页。
- 验收：输入正确账号密码能登录并跳转到正确首页。

**故事 2.2：实现注册页面**
- 描述：创建 RegisterView.vue。选择角色（学生/教师），填写用户名、密码、姓名、学院、专业、年级等信息。提交后自动登录跳转。
- 验收：注册成功后能用新账号登录。

### 史诗 3：学生端

**故事 3.1：实现活动列表页**
- 描述：创建 StudentActivityList.vue。顶部 Banner 轮播图，分类筛选下拉框（宣讲会/开放日/夏令营/咨询会等），线上/线下标签切换，搜索框，活动卡片网格（显示分类标签、状态标签、日期、地点、报名进度条），分页。状态标签根据日期自动计算（getDisplayStatus）。
- 验收：能看到所有可报名的活动，能按分类和类型筛选，能搜索，日期驱动状态显示正确。

**故事 3.2：实现活动详情和报名**
- 描述：创建 StudentActivityDetail.vue。Hero 头部（分类标签 + 状态标签 + 标题 + 日期 + 地点 + 已报名人数），活动详情描述，名额信息（学生名额进度条、教师名额、每校限额），报名按钮（根据 canEnroll 判断可报名/已报名/已截止/已结束）。报名弹窗：显示我的学院（自动填入不可编辑），填写招生目标学校（带 AI 联想），填写个人简介。
- 验收：能在活动详情页点击报名，填写信息后成功提交。

**故事 3.3：实现已报名活动页**
- 描述：创建 StudentEnrollments.vue。表格显示：活动名称、学院、招生学校、状态标签（待审核/审批中/已通过/已驳回/已撤回）、报名时间。操作按钮：查看活动详情、已通过的可提交反馈、待审核/审批中的可撤回报名。分页。
- 验收：能看到自己的报名列表和状态，能撤回报名。

**故事 3.4：实现反馈页面**
- 描述：创建 StudentFeedbackSubmit.vue（评分组件 + 内容输入 + 联系方式，提交按钮），创建 StudentMyFeedback.vue（反馈列表：活动名、评分、内容、状态（待回复/已回复）、管理员回复内容）。
- 验收：能提交反馈，能在我的反馈中看到管理员回复。

### 史诗 4：教师端

**故事 4.1：实现教师活动列表和详情**
- 描述：创建 TeacherActivityList.vue（与学生端类似，targetAudience 过滤不同），TeacherActivityDetail.vue（与学生端类似，名额显示侧重教师名额）。
- 验收：教师能看到面向教师的活动，能报名。

**故事 4.2：实现教师报名和反馈页面**
- 描述：创建 TeacherEnrollments.vue、TeacherFeedbackSubmit.vue（增加院系信息字段）、TeacherMyFeedback.vue。与学生端逻辑一致，适配教师角色。
- 验收：教师能正常报名、查看报名状态、提交反馈。

### 史诗 5：个人信息

**故事 5.1：实现个人信息页面**
- 描述：创建 ProfileView.vue。展示区：用户名、姓名、角色标签、学院、专业、年级、GPA、邮箱、手机。编辑区：可修改姓名、学院、专业、年级、邮箱、手机（角色和用户名不可修改、GPA 只读）。头像区：显示当前头像，点击更换按钮上传本地图片。修改密码区：填入旧密码和新密码提交。
- 验收：所有角色都能查看和编辑个人信息，能上传头像，能修改密码。

---

## D 成员：前端管理端

负责学院管理员全部页面、学校管理员全部页面、数据大屏。

### 史诗 1：学院管理员端

**故事 1.1：实现学院活动管理页**
- 描述：创建 CollegeActivityList.vue。顶部统计卡片（活动总数、进行中、报名中、已结束），筛选栏（搜索 + 类型 + 状态），活动表格（ID、名称、类型、状态、报名人数进度条、地点、时间、操作列）。操作列：编辑按钮（跳转到编辑页 /college/activities/:id）、删除按钮（确认弹窗）。创建活动按钮（跳转到 /college/activities/create，表单自动设为院级 + 本院 collegeId）。
- 验收：学院管理员能看到本院院级活动，能创建、编辑、删除。

**故事 1.2：实现学院审批页**
- 描述：创建 CollegeApprovalList.vue。统计卡片（待审核、已通过、已驳回、通过率），筛选栏（搜索申请人或招生学校、按活动筛选、按状态筛选），审批表格（多选 + 申请人 + 角色标签 + 活动名称 + 招生学校 + GPA + 提交时间 + 审核状态）。操作列：通过按钮、拒绝按钮（弹窗填写原因）。支持批量通过/拒绝。修复字段名匹配（applicantName、enrollmentId、currentStatus）。
- 验收：能审批本院学生的报名申请。

**故事 1.3：实现学院反馈管理页**
- 描述：创建 CollegeFeedbackList.vue。统计卡片（待回复、已回复），筛选栏（活动 + 状态），反馈表格（提交人 + 角色 + 内容摘要 + 评分 + 状态标签 + 提交时间）。操作列：状态为 SUBMITTED 时显示回复按钮，点击弹窗回复内容和上传附件。
- 验收：能查看本院反馈列表，能回复反馈、上传附件。

**故事 1.4：实现学院用户管理页**
- 描述：创建 CollegeUserManagement.vue。统计卡片（用户总数、管理员、教师、学生），筛选（搜索用户名/姓名/邮箱 + 角色），用户表格（用户名 + 姓名 + 角色标签 + 专业 + 年级 + 邮箱 + 状态开关 + 创建时间）。操作下拉菜单：提升为管理员、降为教师、重置密码、删除。导入 CSV 按钮。底部分页条（10 条/页）。
- 验收：能管理本院用户，能批量导入，能分页浏览。

### 史诗 2：学校管理员端

**故事 2.1：实现数据大屏页**
- 描述：创建 SchoolDashboard.vue。4 个统计卡片（活动总数、报名总数、通过率、平均评分），带数字动画效果。3 个图表：活动分类统计柱状图（各分类活动数量）、学院报名分布柱状图（各学院报名人数）、评分分布饼图（1-5 分占比）。最近反馈列表。数据从 statisticsApi 实时获取，加载失败显示错误状态而非假数据。
- 验收：能看到真实的统计数据图表。

**故事 2.2：实现学校活动管理页**
- 描述：创建 SchoolActivityList.vue（表格：名称、类型、分类、级别、状态、创建人、报名人数、Banner 开关、时间，操作列：编辑/删除/导出）。创建 SchoolActivityCreate.vue（新建/编辑活动表单：标题、描述、类型 ONLINE/OFFLINE、分类下拉 9 种、级别院级/校级、状态、招生对象、开始/结束时间、报名开始/截止时间、地点、名额、每校限额、审批流程、Banner 图链接）。
- 验收：能创建/编辑/删除所有活动，能设置院级/校级。

**故事 2.3：实现学校审批页**
- 描述：创建 SchoolApprovalList.vue。筛选栏（按学院 + 活动 + 状态），审批表格（申请人 + 角色 + 学院 + 活动 + 招生学校 + GPA + 学院审批状态 + 当前状态 + 申请时间）。通过/拒绝按钮 + AI 建议按钮。批量通过/拒绝。学校审批只处理 APPROVING 状态的报名。
- 验收：能审批已通过学院审批的报名。

**故事 2.4：实现学校反馈管理页**
- 描述：创建 SchoolFeedbackList.vue。统计卡片（待回复、已回复），反馈表格（提交人 + 角色 + 学院 + 活动 + 评分 + 状态标签 + 回复人 + 提交时间），查看详情弹窗（含回复内容），回复弹窗。操作列：仅 SUBMITTED 状态显示回复按钮，不显示多余的"已回复"标签。
- 验收：能查看全校反馈，能回复。

**故事 2.5：实现学校用户管理页**
- 描述：创建 SchoolUserManagement.vue。全校用户表格（角色标签：学校管理员红色、学院管理员橙色、教师绿色、学生蓝色），操作下拉菜单（提升为学院管理员、提升为学校管理员、降为教师、重置密码、删除）。分页（10 条/页）。
- 验收：能管理全校用户，能设置权限。

---

## 四周 Sprint 计划

### 第一周：基础搭建
| 成员 | 任务 |
|------|------|
| A | 项目骨架 + 数据库建表 + 实体类 + Mapper + MyBatis-Plus 配置 |
| B | LangChain4j 配置 + AI 模块 + 统计模块 |
| C | Vue 项目骨架 + 路由 + 状态管理 + API 封装 + 登录注册页 |
| D | 布局组件 + 公共常量 + 类型定义 |

### 第二周：核心功能
| 成员 | 任务 |
|------|------|
| A | JWT 认证 + Spring Security + 用户管理全部接口 |
| B | 活动管理全部接口 + 报名管理全部接口 |
| C | 学生端全部页面（活动列表、详情、报名、已报名、反馈） |
| D | 学院管理全部页面（活动、审批、反馈、用户） |

### 第三周：完善功能
| 成员 | 任务 |
|------|------|
| A | 接口联调 + bug 修复 + 安全加固 |
| B | 审批流程全部接口 + 反馈管理全部接口 |
| C | 教师端全部页面 + 个人信息页面 |
| D | 学校管理全部页面（数据大屏、活动、审批、反馈、用户） |

### 第四周：收尾测试
| 成员 | 任务 |
|------|------|
| A | 代码审查 + 性能优化 + @Transactional 完善 |
| B | Excel 导出 + 附件上传下载 + AI 接口 |
| C | 端到端测试 + UI 优化 + 中文化 + 日期驱动状态 |
| D | 端到端测试 + 字段名对齐 + 响应解析兼容 + 文档 |