package com.xju.enrollment.modules.enrollment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xju.enrollment.common.BusinessException;
import com.xju.enrollment.common.PageResult;
import com.xju.enrollment.entity.Activity;
import com.xju.enrollment.entity.Enrollment;
import com.xju.enrollment.entity.User;
import com.xju.enrollment.mapper.ActivityMapper;
import com.xju.enrollment.mapper.EnrollmentMapper;
import com.xju.enrollment.mapper.UserMapper;
import com.xju.enrollment.modules.enrollment.dto.EnrollmentQuery;
import com.xju.enrollment.modules.enrollment.dto.EnrollmentRequest;
import com.xju.enrollment.modules.enrollment.dto.EnrollmentVO;
import com.xju.enrollment.modules.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentMapper enrollmentMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final UserService userService;

    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_APPROVING = "APPROVING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";

    @Transactional
    public EnrollmentVO submit(EnrollmentRequest req, Long userId) {
        // Validate activity exists and is accepting enrollments
        Activity activity = activityMapper.selectById(req.activityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        if (!"PUBLISHED".equals(activity.getStatus()) && !"ONGOING".equals(activity.getStatus())) {
            throw new BusinessException("该活动当前不可报名");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getEnrollStart() != null && now.isBefore(activity.getEnrollStart())) {
            throw new BusinessException("报名尚未开始");
        }
        if (activity.getEnrollEnd() != null && now.isAfter(activity.getEnrollEnd())) {
            throw new BusinessException("报名已截止");
        }

        // Validate user hasn't already enrolled (withdrawn enrollments can be re-submitted)
        Enrollment existed = enrollmentMapper.selectOne(
                new LambdaQueryWrapper<Enrollment>()
                        .eq(Enrollment::getUserId, userId)
                        .eq(Enrollment::getActivityId, req.activityId())
        );
        if (existed != null && !STATUS_WITHDRAWN.equals(existed.getStatus())) {
            throw new BusinessException("您已报名该活动，请勿重复报名");
        }

        // Fetch user info via UserService (auto-fills college, name, role)
        User user = userService.getById(userId);

        if (existed != null) {
            // Re-submit a withdrawn enrollment (table has unique key on user_id + activity_id)
            existed.setTargetSchool(req.targetSchool());
            existed.setCollegeId(user.getCollegeId());
            existed.setCollegeName(user.getCollegeName());
            existed.setStatus(STATUS_SUBMITTED);
            existed.setCurrentNode("college_review");
            existed.setSubmittedAt(LocalDateTime.now());
            existed.setApprovedAt(null);
            existed.setRejectReason(null);
            if (req.intro() != null || req.contact() != null) {
                existed.setFormData(buildFormDataJson(req.intro(), req.contact()));
            }
            enrollmentMapper.updateById(existed);
            return toVO(existed, activity.getTitle(), user.getName(), user.getRole());
        }

        // Build enrollment entity
        Enrollment enrollment = new Enrollment();
        enrollment.setActivityId(req.activityId());
        enrollment.setUserId(userId);
        enrollment.setTargetSchool(req.targetSchool());
        enrollment.setCollegeId(user.getCollegeId());
        enrollment.setCollegeName(user.getCollegeName());
        enrollment.setStatus(STATUS_SUBMITTED);
        enrollment.setCurrentNode("college_review");
        enrollment.setSubmittedAt(LocalDateTime.now());
        // Store intro and contact as JSON in formData if needed
        if (req.intro() != null || req.contact() != null) {
            enrollment.setFormData(buildFormDataJson(req.intro(), req.contact()));
        }

        enrollmentMapper.insert(enrollment);

        return toVO(enrollment, activity.getTitle(), user.getName(), user.getRole());
    }

    public PageResult<EnrollmentVO> listMy(Long userId, EnrollmentQuery q) {
        Page<Enrollment> page = new Page<>(q.page(), q.size());
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getUserId, userId)
                .eq(q.activityId() != null, Enrollment::getActivityId, q.activityId())
                .eq(q.status() != null, Enrollment::getStatus, q.status())
                .orderByDesc(Enrollment::getCreateTime);

        Page<Enrollment> result = enrollmentMapper.selectPage(page, wrapper);

        List<EnrollmentVO> voList = enrichWithActivityTitle(result.getRecords());
        return PageResult.of(voList, result.getTotal(), q.page(), q.size());
    }

    public PageResult<EnrollmentVO> listForCollege(EnrollmentQuery q, Long collegeId) {
        Page<Enrollment> page = new Page<>(q.page(), q.size());
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getCollegeId, collegeId)
                .eq(q.activityId() != null, Enrollment::getActivityId, q.activityId())
                .eq(q.status() != null, Enrollment::getStatus, q.status())
                .orderByDesc(Enrollment::getCreateTime);

        Page<Enrollment> result = enrollmentMapper.selectPage(page, wrapper);

        List<EnrollmentVO> voList = enrichWithUserInfo(enrichWithActivityTitle(result.getRecords()));
        return PageResult.of(voList, result.getTotal(), q.page(), q.size());
    }

    public PageResult<EnrollmentVO> listForSchool(EnrollmentQuery q) {
        Page<Enrollment> page = new Page<>(q.page(), q.size());
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<Enrollment>()
                .eq(q.activityId() != null, Enrollment::getActivityId, q.activityId())
                .eq(q.collegeId() != null, Enrollment::getCollegeId, q.collegeId())
                .eq(q.status() != null, Enrollment::getStatus, q.status())
                .orderByDesc(Enrollment::getCreateTime);

        Page<Enrollment> result = enrollmentMapper.selectPage(page, wrapper);

        List<EnrollmentVO> voList = enrichWithUserInfo(enrichWithActivityTitle(result.getRecords()));
        return PageResult.of(voList, result.getTotal(), q.page(), q.size());
    }

    @Transactional
    public void withdraw(Long enrollmentId, Long userId) {
        Enrollment enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!enrollment.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该报名记录");
        }
        if (!STATUS_SUBMITTED.equals(enrollment.getStatus())
                && !STATUS_APPROVING.equals(enrollment.getStatus())) {
            throw new BusinessException("当前状态不允许撤回");
        }
        enrollment.setStatus(STATUS_WITHDRAWN);
        enrollmentMapper.updateById(enrollment);
    }

    public EnrollmentVO getById(Long id) {
        Enrollment enrollment = enrollmentMapper.selectById(id);
        if (enrollment == null) {
            throw new BusinessException("报名记录不存在");
        }
        return enrichSingle(enrollment);
    }

    // ---- helper methods ----

    private EnrollmentVO toVO(Enrollment e, String activityTitle, String userName, String userRole) {
        return new EnrollmentVO(
                e.getId(),
                e.getActivityId(),
                activityTitle,
                e.getUserId(),
                userName,
                userRole,
                e.getStatus(),
                e.getTargetSchool(),
                e.getGroupName(),
                e.getRankInGroup(),
                e.getRejectReason(),
                e.getCollegeId(),
                e.getCollegeName(),
                e.getSubmittedAt(),
                e.getCreateTime()
        );
    }

    private List<EnrollmentVO> enrichWithActivityTitle(List<Enrollment> enrollments) {
        if (enrollments.isEmpty()) {
            return List.of();
        }
        List<Long> activityIds = enrollments.stream()
                .map(Enrollment::getActivityId)
                .distinct()
                .toList();
        Map<Long, String> titleMap = activityMapper.selectBatchIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, Activity::getTitle, (a, b) -> a));

        return enrollments.stream()
                .map(e -> {
                    EnrollmentVO vo = EnrollmentVO.from(e);
                    return new EnrollmentVO(
                            vo.id(), vo.activityId(),
                            titleMap.getOrDefault(e.getActivityId(), null),
                            vo.userId(), null, null,
                            vo.status(), vo.targetSchool(), vo.groupName(),
                            vo.rankInGroup(), vo.rejectReason(),
                            vo.collegeId(), vo.collegeName(),
                            vo.submittedAt(), vo.createTime()
                    );
                })
                .toList();
    }

    private List<EnrollmentVO> enrichWithUserInfo(List<EnrollmentVO> vos) {
        if (vos.isEmpty()) {
            return vos;
        }
        List<Long> userIds = vos.stream()
                .map(EnrollmentVO::userId)
                .distinct()
                .toList();
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        return vos.stream()
                .map(vo -> new EnrollmentVO(
                        vo.id(), vo.activityId(), vo.activityTitle(),
                        vo.userId(),
                        userMap.containsKey(vo.userId()) ? userMap.get(vo.userId()).getName() : null,
                        userMap.containsKey(vo.userId()) ? userMap.get(vo.userId()).getRole() : null,
                        vo.status(), vo.targetSchool(), vo.groupName(),
                        vo.rankInGroup(), vo.rejectReason(),
                        vo.collegeId(), vo.collegeName(),
                        vo.submittedAt(), vo.createTime()
                ))
                .toList();
    }

    private EnrollmentVO enrichSingle(Enrollment e) {
        String activityTitle = null;
        if (e.getActivityId() != null) {
            Activity activity = activityMapper.selectById(e.getActivityId());
            if (activity != null) {
                activityTitle = activity.getTitle();
            }
        }
        String userName = null;
        String userRole = null;
        if (e.getUserId() != null) {
            try {
                User user = userService.getById(e.getUserId());
                userName = user.getName();
                userRole = user.getRole();
            } catch (BusinessException ignored) {
            }
        }
        return toVO(e, activityTitle, userName, userRole);
    }

    private String buildFormDataJson(String intro, String contact) {
        StringBuilder sb = new StringBuilder("{");
        if (intro != null) {
            sb.append("\"intro\":\"").append(escapeJson(intro)).append("\"");
        }
        if (contact != null) {
            if (intro != null) sb.append(",");
            sb.append("\"contact\":\"").append(escapeJson(contact)).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
