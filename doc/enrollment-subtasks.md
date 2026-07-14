# 个人分工子任务清单

> 2026-07-14

---

## 1. 报名表单页

- [ ] **报名弹窗 UI** — el-dialog 弹窗 + el-form 表单，包含"我的学院"（自动填入、只读）和"个人简介"（textarea，最多 500 字）
- [ ] **日期驱动报名按钮** — 根据活动报名开始/截止时间动态显示"立即报名"/"报名已截止"/"未开始"，名额满显示"已满"
- [ ] **报名提交逻辑** — 点击确认报名 → 调 enrollApi.submit() → 后端校验（活动存在 + 在时间窗口 + 未超名额 + 未重复报名）
- [ ] **报名后刷新** — 报名成功后刷新活动详情（更新已报名人数）+ 按钮变为"已报名"

---

## 2. 我的报名

- [ ] **报名列表页** — el-table 展示（活动名称、学院、状态标签、报名时间），分页 10 条/页
- [ ] **状态标签中文化** — SUBMITTED→待审核、APPROVING→审批中、APPROVED→已通过、REJECTED→已驳回、WITHDRAWN→已撤回
- [ ] **撤回报名** — 仅 SUBMITTED/APPROVING 状态可撤回，确认弹窗后调 withdraw API
- [ ] **反馈入口** — 已通过状态显示"反馈"按钮，点击跳转反馈页

---

## 3. 报名审核页

- [ ] **学院审批列表** — 查询本院 SUBMITTED 状态报名，显示申请人、角色、活动、GPA、提交时间
- [ ] **学校审批列表** — 查询全校 APPROVING 状态报名
- [ ] **审批通过** — 点击通过 → API approve → 学院: SUBMITTED→APPROVING / 学校: APPROVING→APPROVED
- [ ] **审批驳回** — 弹窗填写驳回原因 → API reject → 状态变为 REJECTED
- [ ] **批量审批** — el-table 多选 → 批量通过/批量驳回
- [ ] **AI 审批建议** — 点击"AI建议" → 调 /api/ai/approval/suggest → 显示建议/理由/风险

---

## 4. 活动反馈收集模块

- [ ] **学生反馈提交** — 评分（el-rate 1-5）+ 内容 + 联系方式 → 校验已报名且已通过才能提交
- [ ] **教师反馈提交** — 同上，额外填写院系信息
- [ ] **重复提交拦截** — 同一活动已反馈过的提示"请勿重复提交"

---

## 5. 反馈管理页

- [ ] **学院反馈列表** — 查看本院用户提交的反馈，按活动和状态筛选
- [ ] **学校反馈列表** — 查看全校反馈
- [ ] **统计卡片** — 待回复数量、已回复数量
- [ ] **中文化状态** — SUBMITTED→待回复、REPLIED→已回复、CLOSED→已关闭

---

## 6. 回复反馈页

- [ ] **回复弹窗** — el-dialog + textarea，管理员填写回复内容
- [ ] **回复提交** — 更新 feedback 表（reply + replyUserId + replyTime + status→REPLIED）
- [ ] **权限控制** — 学院管理员只能回复本院反馈，学校管理员可回复所有
- [ ] **学生端同步** — 回复后学生"我的反馈"立即显示回复内容

---

## 7. 附件上传与下载

- [ ] **附件上传** — POST /api/feedback/attachment/upload + multipart/form-data，保存到 ./uploads/
- [ ] **文件存储** — 记录 feedback_attachment 表（文件名、路径、大小、类型）
- [ ] **附件下载** — GET /api/feedback/attachment/download/{id} → 流式返回文件
- [ ] **静态资源映射** — WebMvcConfig 配置 /uploads/** 路径，文件可通过 URL 直接访问

---

## 8. 活动统计页

- [ ] **统计接口** — GET /api/statistics/dashboard → 活动总数、报名总数、通过率、反馈率、平均评分
- [ ] **学院分布** — GET /api/statistics/college → 各学院报名人数分布
- [ ] **评分分布** — GET /api/statistics/rating → 1-5 分各多少人
- [ ] **SQL 优化** — 用 selectCount() 替代 selectList()，避免全表加载到内存

---

## 9. 数据可视化

- [ ] **统计卡片** — 4 个卡片（活动总数、报名总数、通过率、平均评分），数字从 0 动画滚动到目标值
- [ ] **活动分类柱状图** — ECharts createBarOption，展示各分类活动数量
- [ ] **学院报名柱状图** — ECharts createBarOption，展示各学院报名人数
- [ ] **评分分布饼图** — ECharts createPieOption，展示 1-5 分占比
- [ ] **响应式适配** — 窗口 resize 时图表自动调整大小

---

## 10. AI 分析

- [ ] **LangChain4j 配置** — AiConfig 创建 ChatLanguageModel Bean（DeepSeek API，temperature=0.3）
- [ ] **学校名称标准化** — SchoolNameNormalizer，输入不标准校名 → AI 返回规范名称 + 联想建议
- [ ] **反馈情感分析** — FeedbackAnalyzer，输入反馈文本 → 返回情感倾向 + 关键词
- [ ] **审批建议** — 传入 GPA + 目标学校 + 报名数据 → AI 给出通过/驳回建议 + 理由 + 风险评估

---

## 公共依赖

- [ ] **响应解析兼容** — 所有 API 调用同时兼容 `res.data.list` 和 `res.data.records` 两种格式
- [ ] **中文化状态映射** — constants.ts 统一维护活动状态、报名状态、反馈状态的中英文映射
- [ ] **日期驱动状态** — `getDisplayStatus(activity)` 根据 enrollStart/enrollEnd/startTime/endTime 动态计算显示状态
- [ ] **字段名校验** — 所有表格 prop 对齐 API 返回字段（applicantName、currentStatus、enrollmentId）