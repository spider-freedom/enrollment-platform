-- ============================================================
-- 新疆大学招生宣传报名平台 — 数据库初始化脚本
-- ============================================================
CREATE DATABASE IF NOT EXISTS enrollment_platform
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE enrollment_platform;

-- 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名(学号/工号)',
    password VARCHAR(255) NOT NULL COMMENT '加密密码',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    role VARCHAR(20) NOT NULL COMMENT 'STUDENT/TEACHER/COLLEGE_ADMIN/SCHOOL_ADMIN',
    college_id BIGINT COMMENT '所属学院ID',
    college_name VARCHAR(100) COMMENT '所属学院名称',
    major VARCHAR(100) COMMENT '专业',
    grade VARCHAR(20) COMMENT '年级',
    gpa DOUBLE COMMENT '绩点',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role (role),
    INDEX idx_college (college_id)
) COMMENT '用户表';

-- 活动表
CREATE TABLE activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    description TEXT COMMENT '活动简介(富文本)',
    type VARCHAR(20) NOT NULL COMMENT 'ONLINE/OFFLINE',
    category VARCHAR(30) DEFAULT '宣讲会' COMMENT '活动分类: 宣讲会/开放日/夏令营/咨询会/回访母校/线上直播/走访宣讲/招生宣传/其他',
    level VARCHAR(10) DEFAULT '校级' COMMENT '院级/校级',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/ONGOING/ENDED',
    target_audience TINYINT NOT NULL DEFAULT 3 COMMENT '1=仅学生 2=仅教师 3=学生+教师',
    start_time DATETIME COMMENT '活动开始时间',
    end_time DATETIME COMMENT '活动结束时间',
    enroll_start DATETIME COMMENT '报名开始时间',
    enroll_end DATETIME COMMENT '报名截止时间',
    location VARCHAR(200) COMMENT '活动地点',
    max_students INT DEFAULT 0 COMMENT '学生名额上限',
    max_teachers INT DEFAULT 0 COMMENT '教师名额上限',
    max_per_school INT DEFAULT 3 COMMENT '每校最大人数',
    banner_url VARCHAR(500) COMMENT 'Banner图片URL',
    banner_link VARCHAR(500) COMMENT 'Banner跳转链接',
    is_banner TINYINT DEFAULT 0 COMMENT '是否轮播 0=否 1=是',
    group_rule VARCHAR(200) COMMENT '分组规则',
    rank_rule VARCHAR(200) COMMENT '排名规则',
    workflow_key VARCHAR(100) COMMENT '审批流程Key',
    creator_id BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_status (status),
    INDEX idx_type (type),
    INDEX idx_creator (creator_id)
) COMMENT '活动表';

-- 活动自定义字段配置
CREATE TABLE activity_field_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    field_name VARCHAR(100) NOT NULL COMMENT '字段名',
    field_label VARCHAR(100) NOT NULL COMMENT '字段标签',
    field_type VARCHAR(20) DEFAULT 'TEXT' COMMENT 'TEXT/SELECT/DATE/FILE',
    required TINYINT DEFAULT 0 COMMENT '0=选填 1=必填',
    sort_order INT DEFAULT 0 COMMENT '排序',
    options TEXT COMMENT '下拉选项JSON',
    enable_ai_normalize TINYINT DEFAULT 0 COMMENT '是否启用AI标准化',
    INDEX idx_activity (activity_id)
) COMMENT '活动自定义字段配置';

-- 报名表
CREATE TABLE enrollment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '报名人ID',
    status VARCHAR(20) DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED/APPROVING/APPROVED/REJECTED/WITHDRAWN',
    form_data JSON COMMENT '表单数据',
    current_node VARCHAR(100) COMMENT '当前审批节点',
    college_id BIGINT COMMENT '学院ID',
    college_name VARCHAR(100) COMMENT '学院名称',
    target_school VARCHAR(100) COMMENT '招生对象学校',
    rank_in_group INT COMMENT '组内排名',
    group_name VARCHAR(200) COMMENT '分组名称',
    reject_reason VARCHAR(500) COMMENT '驳回原因',
    submitted_at DATETIME COMMENT '提交时间',
    approved_at DATETIME COMMENT '审批通过时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_activity (user_id, activity_id),
    INDEX idx_activity (activity_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_college (college_id)
) COMMENT '报名表';

-- 反馈表
CREATE TABLE feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '提交人ID',
    user_role VARCHAR(20) NOT NULL COMMENT '提交时角色 STUDENT/TEACHER',
    content TEXT NOT NULL COMMENT '反馈内容',
    rating TINYINT COMMENT '评分1-5',
    contact VARCHAR(100) COMMENT '联系方式',
    department VARCHAR(100) COMMENT '所属院系(教师)',
    status VARCHAR(20) DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED/REPLIED/CLOSED',
    reply TEXT COMMENT '回复内容',
    reply_user_id BIGINT COMMENT '回复人ID',
    reply_time DATETIME COMMENT '回复时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_activity (user_id, activity_id),
    INDEX idx_activity (activity_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) COMMENT '活动反馈表';

-- 反馈附件表
CREATE TABLE feedback_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    feedback_id BIGINT NOT NULL COMMENT '关联反馈ID',
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '存储路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    file_type VARCHAR(50) COMMENT 'MIME类型',
    upload_user_id BIGINT NOT NULL COMMENT '上传人ID',
    attachment_type VARCHAR(20) DEFAULT 'REPLY' COMMENT 'FEEDBACK/REPLY',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_feedback (feedback_id),
    FOREIGN KEY (feedback_id) REFERENCES feedback(id) ON DELETE CASCADE
) COMMENT '反馈附件表';

-- ============================================================
-- 初始数据 (密码均为 123456 的 BCrypt 加密值)
-- ============================================================
INSERT INTO sys_user (username, password, name, role, college_id, college_name, major, grade, gpa, email, phone) VALUES
-- 学生 (简单用户名: stu01~stu08)
('stu01', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '艾克拜尔', 'STUDENT', 1, '计算机科学与技术学院', '软件工程', '2021级', 3.7, 'stu01@xju.edu.cn', '13800001111'),
('stu02', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '古丽娜尔', 'STUDENT', 1, '计算机科学与技术学院', '计算机科学', '2022级', 3.5, 'stu02@xju.edu.cn', '13800002222'),
('stu03', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '迪丽努尔', 'STUDENT', 2, '数学与系统科学学院', '数学与应用数学', '2021级', 3.9, 'stu03@xju.edu.cn', '13800003333'),
('stu04', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '买买提江', 'STUDENT', 1, '计算机科学与技术学院', '网络工程', '2023级', 3.2, 'stu04@xju.edu.cn', '13800004444'),
('stu05', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '阿依古丽', 'STUDENT', 2, '数学与系统科学学院', '统计学', '2022级', 3.6, 'stu05@xju.edu.cn', '13800005555'),
('stu06', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '热合曼', 'STUDENT', 3, '信息科学与工程学院', '电子信息工程', '2023级', 3.4, 'stu06@xju.edu.cn', '13800006666'),
('stu07', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '帕提古丽', 'STUDENT', 4, '化学化工学院', '应用化学', '2021级', 3.8, 'stu07@xju.edu.cn', '13800007777'),
('stu08', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '吐尔逊江', 'STUDENT', 5, '生命科学与技术学院', '生物技术', '2022级', 3.1, 'stu08@xju.edu.cn', '13800008888'),
-- 教师 (tch01~tch03)
('tch01', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '李教授', 'TEACHER', 1, '计算机科学与技术学院', '副教授', NULL, NULL, 'tch01@xju.edu.cn', '13900001111'),
('tch02', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '王教授', 'TEACHER', 2, '数学与系统科学学院', '教授', NULL, NULL, 'tch02@xju.edu.cn', '13900002222'),
('tch03', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '张老师', 'TEACHER', 3, '信息科学与工程学院', '讲师', NULL, NULL, 'tch03@xju.edu.cn', '13900003333'),
-- 学院管理员 (col01~col02)
('col01', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '赵主任', 'COLLEGE_ADMIN', 1, '计算机科学与技术学院', NULL, NULL, NULL, 'col01@xju.edu.cn', '13700001111'),
('col02', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '钱主任', 'COLLEGE_ADMIN', 2, '数学与系统科学学院', NULL, NULL, NULL, 'col02@xju.edu.cn', '13700002222'),
-- 学校管理员 (sch01~sch02)
('sch01', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '陈处长', 'SCHOOL_ADMIN', NULL, '招生办公室', NULL, NULL, NULL, 'sch01@xju.edu.cn', '13600001111'),
('sch02', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '刘副处长', 'SCHOOL_ADMIN', NULL, '招生办公室', NULL, NULL, NULL, 'sch02@xju.edu.cn', '13600002222');

INSERT INTO activity (title, description, type, category, level, status, target_audience, start_time, end_time, enroll_start, enroll_end, location, max_students, max_teachers, max_per_school, workflow_key, creator_id, is_banner) VALUES
('2026年寒假招生宣传活动', '面向全校的寒假招生宣传活动，前往新疆各地州重点高中开展宣讲，与考生和家长面对面交流。', 'OFFLINE', '宣讲会', '校级', 'PUBLISHED', 3, '2026-01-15 09:00:00', '2026-01-20 17:00:00', '2026-01-01 00:00:00', '2026-01-10 23:59:59', '新疆各地州重点高中', 20, 5, 3, 'college_school_approval', 15, 1),
('优秀学子寒假回访高中母校', '以个人或团队形式回访高中母校，分享大学学习与生活经历，帮助学弟学妹了解新疆大学。', 'OFFLINE', '回访母校', '校级', 'PUBLISHED', 1, '2026-01-20 09:00:00', '2026-02-10 17:00:00', '2026-01-05 00:00:00', '2026-01-15 23:59:59', '全国各高中', 60, 0, 5, 'college_school_approval', 15, 0),
('线上招生政策宣讲与答疑', '通过腾讯会议开展线上招生政策宣讲，为考生和家长解答报考疑问，分享专业选择经验。', 'ONLINE', '咨询会', '校级', 'PUBLISHED', 3, '2026-03-05 14:00:00', '2026-03-08 17:00:00', '2026-02-20 00:00:00', '2026-03-01 23:59:59', '线上(腾讯会议)', 200, 15, 0, 'school_approval', 15, 0),
('2026年春季校园开放日', '邀请全疆各中学师生来校参观，体验大学校园生活，了解新疆大学学科特色和招生政策。', 'OFFLINE', '开放日', '校级', 'PUBLISHED', 3, '2026-04-15 09:00:00', '2026-04-15 17:00:00', '2026-03-20 00:00:00', '2026-04-10 23:59:59', '新疆大学红湖校区', 100, 20, 5, 'college_school_approval', 15, 1),
('2026年暑期夏令营招生宣讲', '面向全国高三毕业生，包含学科体验、校园参观、名师讲座等活动。', 'OFFLINE', '夏令营', '校级', 'PUBLISHED', 1, '2026-07-10 09:00:00', '2026-07-15 17:00:00', '2026-06-01 00:00:00', '2026-06-30 23:59:59', '新疆大学博达校区', 200, 0, 10, 'school_approval', 15, 1),
('计算机学院专业介绍直播', '计算机科学与技术学院线上专业介绍，讲解各专业方向、课程设置、就业前景。', 'ONLINE', '线上直播', '院级', 'ONGOING', 3, '2026-07-05 15:00:00', '2026-07-05 17:00:00', '2026-06-20 00:00:00', '2026-07-04 23:59:59', '线上(腾讯会议)', 500, 30, 0, 'school_approval', 14, 0),
('数学学院招生答疑专场', '数学与系统科学学院招生政策解读与答疑，帮助考生了解数学各专业方向。', 'ONLINE', '咨询会', '院级', 'PUBLISHED', 3, '2026-07-20 14:00:00', '2026-07-20 16:00:00', '2026-07-01 00:00:00', '2026-07-19 23:59:59', '线上(Zoom)', 300, 20, 0, 'school_approval', 14, 0),
('喀什地区重点高中走访宣讲', '前往喀什地区重点高中进行面对面招生宣传，深度对接优质生源。', 'OFFLINE', '走访宣讲', '校级', 'DRAFT', 3, '2026-09-01 09:00:00', '2026-09-05 17:00:00', '2026-08-01 00:00:00', '2026-08-25 23:59:59', '喀什地区各重点高中', 15, 5, 3, 'college_school_approval', 15, 1),
('伊犁地区招生宣传行', '组织招生宣传团队赴伊犁地区，覆盖伊宁市、奎屯市等重点高中。', 'OFFLINE', '招生宣传', '校级', 'ENDED', 3, '2026-03-10 09:00:00', '2026-03-15 17:00:00', '2026-02-15 00:00:00', '2026-03-05 23:59:59', '伊犁地区各重点高中', 20, 5, 3, 'college_school_approval', 15, 0),
('线上招生政策全解读', '招生办老师线上全面解读2026年招生政策、录取规则、志愿填报技巧。', 'ONLINE', '线上直播', '校级', 'PUBLISHED', 3, '2026-07-25 19:00:00', '2026-07-25 21:00:00', '2026-07-10 00:00:00', '2026-07-24 23:59:59', '线上(腾讯会议+直播)', 1000, 50, 0, 'school_approval', 15, 1);

INSERT INTO activity_field_config (activity_id, field_name, field_label, field_type, required, sort_order, enable_ai_normalize) VALUES
(1, 'target_school', '招生对象学校', 'TEXT', 1, 1, 1),
(1, 'intro', '个人简介', 'TEXT', 1, 2, 0),
(1, 'resume', '个人简历', 'FILE', 0, 3, 0);

INSERT INTO enrollment (activity_id, user_id, status, target_school, college_id, college_name, submitted_at, approved_at) VALUES
(1, 1, 'APPROVED', '乌鲁木齐市第一中学', 1, '计算机科学与技术学院', '2026-01-05 14:30:00', '2026-01-06 09:00:00'),
(1, 2, 'APPROVED', '兵团二中', 1, '计算机科学与技术学院', '2026-01-05 15:10:00', '2026-01-06 10:00:00'),
(1, 3, 'APPROVED', '石河子第一中学', 2, '数学与系统科学学院', '2026-01-06 11:00:00', '2026-01-07 10:00:00'),
(1, 9, 'APPROVED', '乌鲁木齐市第一中学', 1, '计算机科学与技术学院', '2026-01-04 10:00:00', '2026-01-05 08:00:00'),
(4, 1, 'APPROVED', '乌鲁木齐市第一中学', 1, '计算机科学与技术学院', '2026-03-25 10:00:00', '2026-03-26 09:00:00'),
(4, 2, 'APPROVED', '兵团二中', 1, '计算机科学与技术学院', '2026-03-25 14:00:00', '2026-03-26 10:00:00'),
(4, 3, 'APPROVED', '石河子第一中学', 2, '数学与系统科学学院', '2026-03-26 09:00:00', '2026-03-27 10:00:00'),
(4, 6, 'APPROVING', '克拉玛依市第一中学', 3, '信息科学与工程学院', '2026-03-28 11:00:00', NULL),
(4, 9, 'APPROVED', '乌鲁木齐市第一中学', 1, '计算机科学与技术学院', '2026-03-24 08:00:00', '2026-03-25 08:00:00'),
(5, 4, 'APPROVED', '乌鲁木齐市第八中学', 1, '计算机科学与技术学院', '2026-06-05 10:00:00', '2026-06-06 09:00:00'),
(5, 5, 'APPROVING', '昌吉州第一中学', 2, '数学与系统科学学院', '2026-06-10 15:00:00', NULL),
(5, 7, 'SUBMITTED', '阿克苏地区第二中学', 4, '化学化工学院', '2026-06-15 16:00:00', NULL),
(5, 8, 'REJECTED', '哈密市第一中学', 5, '生命科学与技术学院', '2026-06-12 09:00:00', NULL),
(6, 1, 'APPROVED', '乌鲁木齐市第一中学', 1, '计算机科学与技术学院', '2026-06-25 10:00:00', '2026-06-25 15:00:00'),
(6, 2, 'SUBMITTED', '兵团二中', 1, '计算机科学与技术学院', '2026-06-28 14:00:00', NULL),
(6, 6, 'SUBMITTED', '克拉玛依市第一中学', 3, '信息科学与工程学院', '2026-06-29 17:00:00', NULL),
(7, 3, 'SUBMITTED', '石河子第一中学', 2, '数学与系统科学学院', '2026-07-12 10:00:00', NULL),
(7, 5, 'SUBMITTED', '昌吉州第一中学', 2, '数学与系统科学学院', '2026-07-13 15:00:00', NULL),
(9, 4, 'APPROVED', '伊宁市第三中学', 1, '计算机科学与技术学院', '2026-02-20 09:00:00', '2026-02-22 10:00:00'),
(9, 7, 'APPROVED', '奎屯市第一中学', 4, '化学化工学院', '2026-02-21 11:00:00', '2026-02-23 08:00:00'),
(9, 9, 'APPROVED', '伊宁市第三中学', 1, '计算机科学与技术学院', '2026-02-18 08:00:00', '2026-02-20 08:00:00');

INSERT INTO feedback (activity_id, user_id, user_role, content, rating, contact, department, status, reply, reply_user_id, reply_time, create_time) VALUES
(1, 1, 'STUDENT', '活动组织得很好，宣讲过程中与考生和家长交流非常充分。', 5, '13800001111', '计算机科学与技术学院', 'SUBMITTED', NULL, NULL, NULL, '2026-01-25 10:30:00'),
(1, 9, 'TEACHER', '作为带队教师，整体流程顺畅。建议下次增加更多的宣传物料。', 4, '13900001111', '计算机科学与技术学院', 'SUBMITTED', NULL, NULL, NULL, '2026-01-26 14:00:00'),
(4, 1, 'STUDENT', '校园开放日组织得非常成功！高中学生参观后对新疆大学有了全新认识。', 5, '13800001111', '计算机科学与技术学院', 'REPLIED', '感谢反馈！继续努力做好招生宣传工作。', 14, '2026-04-20 09:00:00', '2026-04-18 10:00:00'),
(4, 9, 'TEACHER', '活动流程顺畅，后勤保障到位。建议下次增加更多互动体验环节。', 4, '13900001111', '计算机科学与技术学院', 'SUBMITTED', NULL, NULL, NULL, '2026-04-19 14:00:00'),
(9, 4, 'STUDENT', '伊犁之行收获很大，深入了解了当地生源情况，也锻炼了沟通能力。', 4, '13800004444', '计算机科学与技术学院', 'REPLIED', '伊犁地区生源质量不错，希望继续保持对接。', 15, '2026-03-20 10:00:00', '2026-03-18 16:00:00'),
(9, 7, 'STUDENT', '首次参加外出招生宣传，学到了很多，建议多配一些宣传手册。', 3, '13800007777', '化学化工学院', 'SUBMITTED', NULL, NULL, NULL, '2026-03-19 11:00:00'),
(6, 1, 'STUDENT', '线上宣讲效果很好，直播流畅，答疑环节解答了很多考生关心的问题。', 5, '13800001111', '计算机科学与技术学院', 'SUBMITTED', NULL, NULL, NULL, '2026-07-06 18:00:00');

-- ============================================================
-- 扩展数据: 新增学院管理员 + 院级活动 (各学院1-2个活动)
-- ============================================================
INSERT INTO sys_user (username, password, name, role, college_id, college_name, email, phone) VALUES
('col_cs', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '孙主任', 'COLLEGE_ADMIN', 1, '计算机科学与技术学院', 'col_cs@xju.edu.cn', '13700000001'),
('col_math', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '周主任', 'COLLEGE_ADMIN', 2, '数学与系统科学学院', 'col_math@xju.edu.cn', '13700000002'),
('col_info', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '吴主任', 'COLLEGE_ADMIN', 3, '信息科学与工程学院', 'col_info@xju.edu.cn', '13700000003'),
('col_info2', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '郑老师', 'COLLEGE_ADMIN', 3, '信息科学与工程学院', 'col_info2@xju.edu.cn', '13700000004'),
('col_chem', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '冯主任', 'COLLEGE_ADMIN', 4, '化学化工学院', 'col_chem@xju.edu.cn', '13700000005'),
('col_bio', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '褚主任', 'COLLEGE_ADMIN', 5, '生命科学与技术学院', 'col_bio@xju.edu.cn', '13700000006'),
('col_phy', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '蒋主任', 'COLLEGE_ADMIN', 6, '物理科学与技术学院', 'col_phy@xju.edu.cn', '13700000007'),
('col_phy2', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '韩老师', 'COLLEGE_ADMIN', 6, '物理科学与技术学院', 'col_phy2@xju.edu.cn', '13700000008'),
('col_eng', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '杨主任', 'COLLEGE_ADMIN', 7, '外国语学院', 'col_eng@xju.edu.cn', '13700000009'),
('col_marx', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '朱主任', 'COLLEGE_ADMIN', 8, '马克思主义学院', 'col_marx@xju.edu.cn', '13700000010');

INSERT INTO sys_user (username, password, name, role, college_id, college_name, major, grade, gpa, email, phone) VALUES
('stu_phy', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '阿迪力', 'STUDENT', 6, '物理科学与技术学院', '物理学', '2022级', 3.5, 'stu_phy@xju.edu.cn', '13800010001'),
('stu_eng', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '米娜瓦尔', 'STUDENT', 7, '外国语学院', '英语', '2023级', 3.8, 'stu_eng@xju.edu.cn', '13800010002'),
('stu_marx', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '艾山江', 'STUDENT', 8, '马克思主义学院', '思想政治教育', '2021级', 3.6, 'stu_marx@xju.edu.cn', '13800010003');

INSERT INTO activity (title, description, type, category, level, status, target_audience, start_time, end_time, enroll_start, enroll_end, location, max_students, max_teachers, max_per_school, workflow_key, creator_id, college_id, college_name, is_banner) VALUES
('计算机学院AI前沿讲座', '邀请国内AI领域专家，围绕人工智能最新进展开展系列讲座。', 'OFFLINE', '宣讲会', '院级', 'PUBLISHED', 3, '2026-08-10 14:00:00', '2026-08-10 17:00:00', '2026-07-20 00:00:00', '2026-08-05 23:59:59', '计算机学院学术报告厅', 150, 15, 0, 'school_approval', 14, 1, '计算机科学与技术学院', 1),
('计算机学院优秀学长经验分享', '邀请已毕业优秀学长回校分享考研、就业经验。', 'ONLINE', '线上直播', '院级', 'PUBLISHED', 1, '2026-09-05 19:00:00', '2026-09-05 21:00:00', '2026-08-15 00:00:00', '2026-09-04 23:59:59', '线上(腾讯会议)', 300, 0, 0, 'school_approval', 14, 1, '计算机科学与技术学院', 0),
('数学建模竞赛宣讲', '全国大学生数学建模竞赛赛前动员与经验分享。', 'OFFLINE', '宣讲会', '院级', 'PUBLISHED', 1, '2026-08-20 10:00:00', '2026-08-20 12:00:00', '2026-08-01 00:00:00', '2026-08-18 23:59:59', '数学学院多媒体教室', 80, 0, 0, 'school_approval', 15, 2, '数学与系统科学学院', 0),
('数学之美公众开放日', '面向全校师生展示数学的趣味与应用，包括数学游戏、趣味竞赛。', 'OFFLINE', '开放日', '院级', 'PUBLISHED', 3, '2026-09-15 09:00:00', '2026-09-15 17:00:00', '2026-08-25 00:00:00', '2026-09-12 23:59:59', '数学学院大楼', 120, 10, 0, 'school_approval', 15, 2, '数学与系统科学学院', 1),
('信息学院校企合作宣讲', '邀请华为、中兴等企业HR来院宣讲，介绍实习与就业机会。', 'OFFLINE', '宣讲会', '院级', 'PUBLISHED', 3, '2026-08-25 14:00:00', '2026-08-25 17:00:00', '2026-08-10 00:00:00', '2026-08-23 23:59:59', '信息学院报告厅', 200, 10, 0, 'school_approval', 14, 3, '信息科学与工程学院', 1),
('5G通信技术科普日', '面向中学生开展5G通信技术科普体验活动。', 'OFFLINE', '开放日', '院级', 'DRAFT', 1, '2026-10-10 09:00:00', '2026-10-10 17:00:00', '2026-09-20 00:00:00', '2026-10-08 23:59:59', '信息学院实验中心', 60, 5, 3, 'college_school_approval', 14, 3, '信息科学与工程学院', 0),
('化学实验开放体验日', '开放化学实验室，让中学生亲身体验趣味化学实验。', 'OFFLINE', '开放日', '院级', 'PUBLISHED', 1, '2026-09-20 09:00:00', '2026-09-20 16:00:00', '2026-09-01 00:00:00', '2026-09-18 23:59:59', '化学化工学院实验楼', 50, 5, 3, 'college_school_approval', 15, 4, '化学化工学院', 0),
('绿色化学前沿论坛', '国内外绿色化学领域专家线上分享最新研究成果。', 'ONLINE', '线上直播', '院级', 'ONGOING', 3, '2026-07-15 14:00:00', '2026-07-15 17:00:00', '2026-07-01 00:00:00', '2026-07-14 23:59:59', '线上(腾讯会议)', 500, 20, 0, 'school_approval', 15, 4, '化学化工学院', 0),
('生物标本馆参观活动', '带领中学生参观生物标本馆，了解新疆特有动植物资源。', 'OFFLINE', '开放日', '院级', 'PUBLISHED', 1, '2026-09-25 09:00:00', '2026-09-25 17:00:00', '2026-09-10 00:00:00', '2026-09-23 23:59:59', '生命科学学院标本馆', 40, 5, 5, 'college_school_approval', 14, 5, '生命科学与技术学院', 0),
('物理趣味实验展', '通过趣味物理实验展示，激发中学生对物理学科的兴趣。', 'OFFLINE', '开放日', '院级', 'PUBLISHED', 1, '2026-10-05 09:00:00', '2026-10-05 16:00:00', '2026-09-15 00:00:00', '2026-10-03 23:59:59', '物理学院实验中心', 50, 5, 3, 'college_school_approval', 14, 6, '物理科学与技术学院', 1),
('天体物理科普讲座', '邀请天文台专家线上讲解黑洞、引力波等天体物理前沿话题。', 'ONLINE', '宣讲会', '院级', 'PUBLISHED', 3, '2026-10-15 19:00:00', '2026-10-15 21:00:00', '2026-09-25 00:00:00', '2026-10-13 23:59:59', '线上(腾讯会议)', 400, 15, 0, 'school_approval', 14, 6, '物理科学与技术学院', 0),
('外语文化节开幕式', '一年一度的外语文化节，展示各国文化风情。', 'OFFLINE', '开放日', '院级', 'PUBLISHED', 3, '2026-10-20 09:00:00', '2026-10-20 17:00:00', '2026-10-01 00:00:00', '2026-10-18 23:59:59', '外国语学院文化广场', 200, 10, 0, 'school_approval', 15, 7, '外国语学院', 1),
('英语演讲比赛宣讲', '面向全校学生开展英语演讲比赛，提升语言表达能力。', 'OFFLINE', '宣讲会', '院级', 'DRAFT', 1, '2026-11-01 14:00:00', '2026-11-01 17:00:00', '2026-10-15 00:00:00', '2026-10-30 23:59:59', '外国语学院报告厅', 100, 5, 0, 'school_approval', 15, 7, '外国语学院', 0),
('思政教育实践基地参观', '组织中学生参观思政教育实践基地。', 'OFFLINE', '开放日', '院级', 'PUBLISHED', 1, '2026-10-25 09:00:00', '2026-10-25 12:00:00', '2026-10-10 00:00:00', '2026-10-23 23:59:59', '马克思主义学院实践基地', 60, 5, 5, 'college_school_approval', 14, 8, '马克思主义学院', 0);

