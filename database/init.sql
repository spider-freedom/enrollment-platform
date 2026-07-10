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
-- 初始数据
-- ============================================================
-- 密码均为 123456 的 BCrypt 加密值
INSERT INTO sys_user (username, password, name, role, college_id, college_name, major, grade, gpa, email, phone) VALUES
('2021001', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '艾克拜尔·买买提', 'STUDENT', 1, '计算机科学与技术学院', '软件工程', '2021级', 3.7, 'aikebai@xju.edu.cn', '13800001111'),
('2021002', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '古丽娜尔·阿不都', 'STUDENT', 1, '计算机科学与技术学院', '计算机科学', '2021级', 3.5, 'gulinaer@xju.edu.cn', '13800002222'),
('2022001', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '迪丽娜尔', 'STUDENT', 2, '数学与系统科学学院', '数学与应用数学', '2022级', 3.9, 'dilinaer@xju.edu.cn', '13800003333'),
('T2021001', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '李老师', 'TEACHER', 1, '计算机科学与技术学院', '副教授', NULL, NULL, 'lilaoshi@xju.edu.cn', '13900002222'),
('C2021001', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '赵老师', 'COLLEGE_ADMIN', 1, '计算机科学与技术学院', NULL, NULL, NULL, 'zhaolaoshi@xju.edu.cn', '13700003333'),
('S2021001', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '陈老师', 'SCHOOL_ADMIN', NULL, NULL, NULL, NULL, NULL, 'chenlaoshi@xju.edu.cn', '13600004444'),

-- 新增用户
('2022002', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '阿依古丽·吐尔逊', 'STUDENT', 2, '数学与系统科学学院', '统计学', '2022级', 3.6, 'ayiguli@xju.edu.cn', '13800005555'),
('2023001', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '买买提·艾力', 'STUDENT', 3, '信息科学与工程学院', '电子信息工程', '2023级', 3.4, 'maimaiti@xju.edu.cn', '13800006666'),
('T2022001', '$2b$12$fbKC6xEPicrekDy8wB5a0.koj95WTlXvFLg59sdafHQmNytLpdWpO', '王老师', 'TEACHER', 2, '数学与系统科学学院', '教授', NULL, NULL, 'wanglaoshi@xju.edu.cn', '13900005555');

INSERT INTO activity (title, description, type, status, target_audience, start_time, end_time, enroll_start, enroll_end, location, max_students, max_teachers, max_per_school, workflow_key, creator_id) VALUES
('2026年寒假招生宣传活动', '面向全校的寒假招生宣传活动，前往新疆各地州重点高中开展宣讲，与考生和家长面对面交流，介绍新疆大学的办学特色、专业设置和招生政策。', 'OFFLINE', 'PUBLISHED', 3, '2026-01-15 09:00:00', '2026-01-20 17:00:00', '2026-01-01 00:00:00', '2026-01-10 23:59:59', '新疆各地州重点高中', 20, 5, 3, 'college_school_approval', 6),
('优秀学子寒假回访高中母校', '以个人或团队形式回访高中母校，分享大学学习与生活经历，帮助学弟学妹了解新疆大学。', 'OFFLINE', 'PUBLISHED', 1, '2026-01-20 09:00:00', '2026-02-10 17:00:00', '2026-01-05 00:00:00', '2026-01-15 23:59:59', '全国各高中', 60, 0, 5, 'college_school_approval', 6),
('线上招生政策宣讲与答疑', '通过腾讯会议开展线上招生政策宣讲，为考生和家长解答报考疑问，分享专业选择经验。', 'ONLINE', 'PUBLISHED', 3, '2026-03-05 14:00:00', '2026-03-08 17:00:00', '2026-02-20 00:00:00', '2026-03-01 23:59:59', '线上(腾讯会议)', 200, 15, 0, 'school_approval', 6);

INSERT INTO activity_field_config (activity_id, field_name, field_label, field_type, required, sort_order, enable_ai_normalize) VALUES
(1, 'target_school', '招生对象学校', 'TEXT', 1, 1, 1),
(1, 'intro', '个人简介', 'TEXT', 1, 2, 0),
(1, 'resume', '个人简历', 'FILE', 0, 3, 0);

INSERT INTO enrollment (activity_id, user_id, status, target_school, college_id, college_name, submitted_at) VALUES
(1, 1, 'APPROVED', '乌鲁木齐市第一中学', 1, '计算机科学与技术学院', '2026-01-05 14:30:00'),
(1, 2, 'SUBMITTED', '兵团二中', 1, '计算机科学与技术学院', '2026-01-05 15:10:00'),
(1, 4, 'APPROVED', '乌鲁木齐市第一中学', 1, '计算机科学与技术学院', '2026-01-03 09:20:00'),

-- 新增报名数据
(1, 3, 'APPROVING', '石河子第一中学', 2, '数学与系统科学学院', '2026-01-06 11:00:00'),
(1, 7, 'REJECTED', '昌吉州第一中学', 2, '数学与系统科学学院', '2026-01-05 16:30:00'),
(1, 8, 'SUBMITTED', '克拉玛依市第一中学', 3, '信息科学与工程学院', '2026-01-07 09:15:00'),
(1, 9, 'APPROVED', '伊宁市第三中学', 2, '数学与系统科学学院', '2026-01-04 10:00:00'),
(2, 4, 'APPROVING', '乌鲁木齐市第八中学', 1, '计算机科学与技术学院', '2026-01-08 08:30:00');

INSERT INTO feedback (activity_id, user_id, user_role, content, rating, contact, department, status, reply, reply_user_id, reply_time, create_time) VALUES
(1, 1, 'STUDENT', '活动组织得很好，宣讲过程中与考生和家长交流非常充分。学到了很多沟通技巧，对学校招生政策也有了更深入的理解。', 5, '13800001111', '计算机科学与技术学院', 'SUBMITTED', NULL, NULL, NULL, '2026-01-25 10:30:00'),
(1, 4, 'TEACHER', '作为带队教师，整体流程顺畅。建议下次增加更多的宣传物料，比如学校宣传视频播放在宣讲开始前。', 4, '13900002222', '计算机科学与技术学院', 'SUBMITTED', NULL, NULL, NULL, '2026-01-26 14:00:00'),
(1, 2, 'STUDENT', '活动整体不错，但现场组织有些混乱，部分学校的对接不够及时。希望下次能提前与学校做好沟通。', 3, '13800002222', '计算机科学与技术学院', 'REPLIED', '感谢您的反馈！我们会在下次活动中加强校方对接工作，提前一周与各学校确认宣讲安排。', 6, '2026-01-28 09:00:00', '2026-01-27 16:30:00');

