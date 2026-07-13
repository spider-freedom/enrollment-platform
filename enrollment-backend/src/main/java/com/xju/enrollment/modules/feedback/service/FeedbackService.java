package com.xju.enrollment.modules.feedback.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xju.enrollment.common.BusinessException;
import com.xju.enrollment.common.PageResult;
import com.xju.enrollment.entity.Activity;
import com.xju.enrollment.entity.Enrollment;
import com.xju.enrollment.entity.Feedback;
import com.xju.enrollment.entity.FeedbackAttachment;
import com.xju.enrollment.entity.User;
import com.xju.enrollment.mapper.ActivityMapper;
import com.xju.enrollment.mapper.EnrollmentMapper;
import com.xju.enrollment.mapper.FeedbackAttachmentMapper;
import com.xju.enrollment.mapper.FeedbackMapper;
import com.xju.enrollment.mapper.UserMapper;
import com.xju.enrollment.modules.feedback.dto.FeedbackQuery;
import com.xju.enrollment.modules.feedback.dto.FeedbackReplyRequest;
import com.xju.enrollment.modules.feedback.dto.FeedbackSubmitRequest;
import com.xju.enrollment.modules.feedback.dto.FeedbackVO;
import com.xju.enrollment.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final FeedbackAttachmentMapper feedbackAttachmentMapper;
    private final ActivityMapper activityMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final UserMapper userMapper;

    @Transactional
    public FeedbackVO submitStudent(Long userId, FeedbackSubmitRequest req) {
        Activity activity = activityMapper.selectById(req.activityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        Enrollment enrollment = enrollmentMapper.selectOne(
                new LambdaQueryWrapper<Enrollment>()
                        .eq(Enrollment::getActivityId, req.activityId())
                        .eq(Enrollment::getUserId, userId)
        );
        if (enrollment == null) {
            throw new BusinessException("您未报名该活动，无法提交反馈");
        }

        if (!"APPROVED".equals(enrollment.getStatus())) {
            throw new BusinessException("报名未通过审核，无法提交反馈");
        }

        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        Integer audience = activity.getTargetAudience();
        if (audience != null) {
            boolean isStudent = "STUDENT".equals(user.getRole());
            if (audience == 1 && !isStudent) {
                throw new BusinessException("该活动仅面向学生，您无法提交反馈");
            }
            if (audience == 2 && isStudent) {
                throw new BusinessException("该活动仅面向教师，您无法提交反馈");
            }
        }

        Long existed = feedbackMapper.selectCount(
                new LambdaQueryWrapper<Feedback>()
                        .eq(Feedback::getActivityId, req.activityId())
                        .eq(Feedback::getUserId, userId)
        );
        if (existed != null && existed > 0) {
            throw new BusinessException("您已提交过反馈，请勿重复提交");
        }

        Feedback feedback = new Feedback();
        feedback.setActivityId(req.activityId());
        feedback.setUserId(userId);
        feedback.setUserRole("STUDENT");
        feedback.setContent(req.content());
        feedback.setRating(req.rating());
        feedback.setContact(req.contact());
        feedback.setStatus("SUBMITTED");
        feedbackMapper.insert(feedback);

        return FeedbackVO.from(feedback);
    }

    @Transactional
    public FeedbackVO submitTeacher(Long userId, FeedbackSubmitRequest req) {
        Activity activity = activityMapper.selectById(req.activityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        Enrollment enrollment = enrollmentMapper.selectOne(
                new LambdaQueryWrapper<Enrollment>()
                        .eq(Enrollment::getActivityId, req.activityId())
                        .eq(Enrollment::getUserId, userId)
        );
        if (enrollment == null) {
            throw new BusinessException("您未报名该活动，无法提交反馈");
        }

        if (!"APPROVED".equals(enrollment.getStatus())) {
            throw new BusinessException("报名未通过审核，无法提交反馈");
        }

        User user = userMapper.selectById(userId);
        Integer audience = activity.getTargetAudience();
        if (audience != null) {
            boolean isTeacher = "TEACHER".equals(user.getRole());
            if (audience == 1 && isTeacher) {
                throw new BusinessException("该活动仅面向学生，您无法提交反馈");
            }
            if (audience == 2 && !isTeacher) {
                throw new BusinessException("该活动仅面向教师，您无法提交反馈");
            }
        }

        Long existed = feedbackMapper.selectCount(
                new LambdaQueryWrapper<Feedback>()
                        .eq(Feedback::getActivityId, req.activityId())
                        .eq(Feedback::getUserId, userId)
        );
        if (existed != null && existed > 0) {
            throw new BusinessException("您已提交过反馈，请勿重复提交");
        }

        Feedback feedback = new Feedback();
        feedback.setActivityId(req.activityId());
        feedback.setUserId(userId);
        feedback.setUserRole("TEACHER");
        feedback.setContent(req.content());
        feedback.setRating(req.rating());
        feedback.setContact(req.contact());
        feedback.setDepartment(req.department());
        feedback.setStatus("SUBMITTED");
        feedbackMapper.insert(feedback);

        return FeedbackVO.from(feedback);
    }

    public PageResult<FeedbackVO> listForCollege(FeedbackQuery q) {
        String currentUserIdStr = SecurityUtils.getCurrentUserId();
        Long currentUserId = Long.parseLong(currentUserIdStr);
        User currentUser = userMapper.selectById(currentUserId);
        Long collegeId = currentUser.getCollegeId();

        return listFeedbacks(q, collegeId);
    }

    public PageResult<FeedbackVO> listForSchool(FeedbackQuery q) {
        return listFeedbacks(q, q.collegeId());
    }

    public PageResult<FeedbackVO> listMy(Long userId, FeedbackQuery q) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getUserId, userId);
        wrapper.orderByDesc(Feedback::getCreateTime);

        Page<Feedback> page = new Page<>(q.page(), q.size());
        Page<Feedback> resultPage = feedbackMapper.selectPage(page, wrapper);

        Map<Long, String> userNameCache = new HashMap<>();
        Map<Long, String> userCollegeCache = new HashMap<>();

        List<FeedbackVO> voList = resultPage.getRecords().stream()
                .map(f -> {
                    String userName = userNameCache.computeIfAbsent(f.getUserId(), uid -> {
                        User u = userMapper.selectById(uid);
                        return u != null ? u.getName() : "未知用户";
                    });
                    String college = userCollegeCache.computeIfAbsent(f.getUserId(), uid -> {
                        User u = userMapper.selectById(uid);
                        return u != null ? u.getCollegeName() : null;
                    });
                    String replyUserName = null;
                    if (f.getReplyUserId() != null) {
                        replyUserName = userNameCache.computeIfAbsent(f.getReplyUserId(), rid -> {
                            User u = userMapper.selectById(rid);
                            return u != null ? u.getName() : "未知用户";
                        });
                    }

                    int attachmentCount = (int) (long) feedbackAttachmentMapper.selectCount(
                            new LambdaQueryWrapper<FeedbackAttachment>()
                                    .eq(FeedbackAttachment::getFeedbackId, f.getId())
                    );

                    return new FeedbackVO(
                            f.getId(),
                            f.getUserId(),
                            userName,
                            f.getUserRole(),
                            college,
                            f.getContent(),
                            f.getRating(),
                            f.getStatus(),
                            f.getReply(),
                            replyUserName,
                            f.getReplyTime(),
                            attachmentCount,
                            f.getCreateTime()
                    );
                })
                .collect(Collectors.toList());

        return PageResult.of(voList, resultPage.getTotal(), q.page(), q.size());
    }

    public FeedbackVO getDetail(Long feedbackId) {
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }

        long attachmentCount = feedbackAttachmentMapper.selectCount(
                new LambdaQueryWrapper<FeedbackAttachment>()
                        .eq(FeedbackAttachment::getFeedbackId, feedbackId)
        );

        FeedbackVO vo = FeedbackVO.from(feedback);
        FeedbackVO enriched = enrichWithUserNames(vo, feedback);

        return new FeedbackVO(
                enriched.feedbackId(),
                enriched.userId(),
                enriched.userName(),
                enriched.userRole(),
                enriched.college(),
                enriched.content(),
                enriched.rating(),
                enriched.status(),
                enriched.reply(),
                enriched.replyUserName(),
                enriched.replyTime(),
                (int) attachmentCount,
                enriched.createTime()
        );
    }

    @Transactional
    public void reply(Long feedbackId, FeedbackReplyRequest req, Long replyUserId) {
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }

        if ("CLOSED".equals(feedback.getStatus())) {
            throw new BusinessException("反馈已关闭，无法回复");
        }

        User replyUser = userMapper.selectById(replyUserId);
        if ("COLLEGE_ADMIN".equals(replyUser.getRole())) {
            Enrollment enrollment = enrollmentMapper.selectOne(
                    new LambdaQueryWrapper<Enrollment>()
                            .eq(Enrollment::getActivityId, feedback.getActivityId())
                            .eq(Enrollment::getUserId, feedback.getUserId())
            );
            if (enrollment == null || !Objects.equals(enrollment.getCollegeId(), replyUser.getCollegeId())) {
                throw new BusinessException("您只能回复本院系的反馈");
            }
        }

        feedback.setReply(req.replyContent());
        feedback.setReplyUserId(replyUserId);
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setStatus("REPLIED");
        feedbackMapper.updateById(feedback);

        if (req.attachmentIds() != null && !req.attachmentIds().isEmpty()) {
            for (Long attachmentId : req.attachmentIds()) {
                FeedbackAttachment attachment = feedbackAttachmentMapper.selectById(attachmentId);
                if (attachment != null) {
                    attachment.setFeedbackId(feedbackId);
                    feedbackAttachmentMapper.updateById(attachment);
                }
            }
        }
    }

    private PageResult<FeedbackVO> listFeedbacks(FeedbackQuery q, Long collegeId) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();

        if (q.activityId() != null) {
            wrapper.eq(Feedback::getActivityId, q.activityId());
        }

        if (q.status() != null && !q.status().isBlank()) {
            wrapper.eq(Feedback::getStatus, q.status());
        }

        if (collegeId != null) {
            List<Long> userIdsInCollege = enrollmentMapper.selectList(
                            new LambdaQueryWrapper<Enrollment>()
                                    .eq(Enrollment::getCollegeId, collegeId)
                                    .select(Enrollment::getUserId)
                    ).stream()
                    .map(Enrollment::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            if (userIdsInCollege.isEmpty()) {
                return PageResult.of(List.of(), 0, q.page(), q.size());
            }

            wrapper.in(Feedback::getUserId, userIdsInCollege);
        }

        wrapper.orderByDesc(Feedback::getCreateTime);

        Page<Feedback> page = new Page<>(q.page(), q.size());
        Page<Feedback> resultPage = feedbackMapper.selectPage(page, wrapper);

        Map<Long, String> userNameCache = new HashMap<>();
        Map<Long, String> userCollegeCache = new HashMap<>();

        List<FeedbackVO> voList = resultPage.getRecords().stream()
                .map(f -> {
                    String userName = userNameCache.computeIfAbsent(f.getUserId(), uid -> {
                        User u = userMapper.selectById(uid);
                        return u != null ? u.getName() : "未知用户";
                    });
                    String college = userCollegeCache.computeIfAbsent(f.getUserId(), uid -> {
                        User u = userMapper.selectById(uid);
                        return u != null ? u.getCollegeName() : null;
                    });
                    String replyUserName = null;
                    if (f.getReplyUserId() != null) {
                        replyUserName = userNameCache.computeIfAbsent(f.getReplyUserId(), rid -> {
                            User u = userMapper.selectById(rid);
                            return u != null ? u.getName() : "未知用户";
                        });
                    }

                    int attachmentCount = (int) (long) feedbackAttachmentMapper.selectCount(
                            new LambdaQueryWrapper<FeedbackAttachment>()
                                    .eq(FeedbackAttachment::getFeedbackId, f.getId())
                    );

                    return new FeedbackVO(
                            f.getId(),
                            f.getUserId(),
                            userName,
                            f.getUserRole(),
                            college,
                            f.getContent(),
                            f.getRating(),
                            f.getStatus(),
                            f.getReply(),
                            replyUserName,
                            f.getReplyTime(),
                            attachmentCount,
                            f.getCreateTime()
                    );
                })
                .collect(Collectors.toList());

        return PageResult.of(voList, resultPage.getTotal(), q.page(), q.size());
    }

    private FeedbackVO enrichWithUserNames(FeedbackVO vo, Feedback feedback) {
        User submitUser = userMapper.selectById(feedback.getUserId());
        String userName = submitUser != null ? submitUser.getName() : "未知用户";
        String college = submitUser != null ? submitUser.getCollegeName() : null;

        String replyUserName = null;
        if (feedback.getReplyUserId() != null) {
            User replyUser = userMapper.selectById(feedback.getReplyUserId());
            replyUserName = replyUser != null ? replyUser.getName() : "未知用户";
        }

        return new FeedbackVO(
                vo.feedbackId(),
                vo.userId(),
                userName,
                vo.userRole(),
                college,
                vo.content(),
                vo.rating(),
                vo.status(),
                vo.reply(),
                replyUserName,
                vo.replyTime(),
                vo.attachmentCount(),
                vo.createTime()
        );
    }
}
