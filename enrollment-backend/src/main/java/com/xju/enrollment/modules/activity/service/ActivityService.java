package com.xju.enrollment.modules.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xju.enrollment.common.BusinessException;
import com.xju.enrollment.common.PageResult;
import com.xju.enrollment.entity.Activity;
import com.xju.enrollment.entity.User;
import com.xju.enrollment.mapper.ActivityMapper;
import com.xju.enrollment.mapper.EnrollmentMapper;
import com.xju.enrollment.modules.activity.dto.ActivityListQuery;
import com.xju.enrollment.modules.activity.dto.ActivityRequest;
import com.xju.enrollment.modules.activity.dto.ActivityVO;
import com.xju.enrollment.modules.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityMapper activityMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final UserService userService;

    public ActivityVO createActivity(ActivityRequest request, Long creatorId) {
        Activity activity = new Activity();
        applyRequestToActivity(request, activity);
        activity.setCreatorId(creatorId);
        if (activity.getStatus() == null) {
            activity.setStatus("DRAFT");
        }
        activityMapper.insert(activity);

        User creator = userService.getById(creatorId);
        return ActivityVO.from(activity, creator.getName());
    }

    public void updateActivity(Long id, ActivityRequest request) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        applyRequestToActivity(request, activity);
        activityMapper.updateById(activity);
    }

    public ActivityVO getById(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        String creatorName = null;
        if (activity.getCreatorId() != null) {
            try {
                User creator = userService.getById(activity.getCreatorId());
                creatorName = creator.getName();
            } catch (BusinessException ignored) {
            }
        }
        int[] counts = countEnrollments(List.of(id)).getOrDefault(id, new int[2]);
        return ActivityVO.from(activity, creatorName, counts[0], counts[1]);
    }

    public PageResult<ActivityVO> listForStudent(ActivityListQuery query, Long userId) {
        LambdaQueryWrapper<Activity> wrapper = buildBaseQuery(query);
        // Filter by targetAudience: students see activities targeting audience 1 or 3
        wrapper.and(w -> w.eq(Activity::getTargetAudience, 1).or().eq(Activity::getTargetAudience, 3));
        // Only PUBLISHED and ONGOING
        wrapper.in(Activity::getStatus, List.of("PUBLISHED", "ONGOING"));
        return doPageQuery(query, wrapper);
    }

    public PageResult<ActivityVO> listForTeacher(ActivityListQuery query, Long userId) {
        LambdaQueryWrapper<Activity> wrapper = buildBaseQuery(query);
        // Filter by targetAudience: teachers see activities targeting audience 2 or 3
        wrapper.and(w -> w.eq(Activity::getTargetAudience, 2).or().eq(Activity::getTargetAudience, 3));
        // Only PUBLISHED and ONGOING
        wrapper.in(Activity::getStatus, List.of("PUBLISHED", "ONGOING"));
        return doPageQuery(query, wrapper);
    }

    public PageResult<ActivityVO> listForCollege(ActivityListQuery query, Long userId) {
        LambdaQueryWrapper<Activity> wrapper = buildBaseQuery(query);
        User collegeAdmin = userService.getById(userId);
        // College admin sees only college-level activities in their own college
        wrapper.eq(Activity::getLevel, "院级");
        if (collegeAdmin.getCollegeId() != null) {
            wrapper.eq(Activity::getCollegeId, collegeAdmin.getCollegeId());
        }
        return doPageQuery(query, wrapper);
    }

    public PageResult<ActivityVO> listForSchool(ActivityListQuery query) {
        LambdaQueryWrapper<Activity> wrapper = buildBaseQuery(query);
        return doPageQuery(query, wrapper);
    }

    public void toggleBanner(Long id, Integer isBanner) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) throw new BusinessException("活动不存在");
        activity.setIsBanner(isBanner != null ? isBanner : 0);
        activityMapper.updateById(activity);
    }

    public List<ActivityVO> getBanners() {
        LambdaQueryWrapper<Activity> w = new LambdaQueryWrapper<>();
        w.eq(Activity::getIsBanner, 1);
        w.and(w2 -> w2.eq(Activity::getStatus, "PUBLISHED").or().eq(Activity::getStatus, "ONGOING"));
        w.orderByAsc(Activity::getId);
        return activityMapper.selectList(w).stream().map(a -> ActivityVO.from(a, null)).toList();
    }

    public void deleteActivity(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        activityMapper.deleteById(id);
    }

    private LambdaQueryWrapper<Activity> buildBaseQuery(ActivityListQuery query) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.keyword())) {
            wrapper.and(w -> w.like(Activity::getTitle, query.keyword())
                    .or()
                    .like(Activity::getDescription, query.keyword()));
        }
        if (StringUtils.isNotBlank(query.type())) {
            wrapper.eq(Activity::getType, query.type());
        }
        if (StringUtils.isNotBlank(query.category())) {
            wrapper.eq(Activity::getCategory, query.category());
        }
        if (StringUtils.isNotBlank(query.status())) {
            wrapper.eq(Activity::getStatus, query.status());
        }
        wrapper.orderByDesc(Activity::getCreateTime);
        return wrapper;
    }

    private PageResult<ActivityVO> doPageQuery(ActivityListQuery query, LambdaQueryWrapper<Activity> wrapper) {
        IPage<Activity> page = new Page<>(query.page(), query.size());
        IPage<Activity> result = activityMapper.selectPage(page, wrapper);

        // Batch fetch creator names with caching
        Map<Long, String> creatorNameMap = new HashMap<>();
        for (Activity activity : result.getRecords()) {
            Long creatorId = activity.getCreatorId();
            if (creatorId != null && !creatorNameMap.containsKey(creatorId)) {
                try {
                    User creator = userService.getById(creatorId);
                    creatorNameMap.put(creatorId, creator.getName());
                } catch (BusinessException ignored) {
                    creatorNameMap.put(creatorId, null);
                }
            }
        }

        // Batch count enrollments per activity (students / teachers)
        List<Long> activityIds = result.getRecords().stream().map(Activity::getId).toList();
        Map<Long, int[]> enrollCountMap = countEnrollments(activityIds);

        List<ActivityVO> voList = result.getRecords().stream()
                .map(a -> {
                    int[] counts = enrollCountMap.getOrDefault(a.getId(), new int[2]);
                    return ActivityVO.from(a, creatorNameMap.get(a.getCreatorId()), counts[0], counts[1]);
                })
                .toList();

        return PageResult.of(voList, result.getTotal(), query.page(), query.size());
    }

    /**
     * 批量统计各活动的有效报名人数（不含已撤回/已拒绝），按角色区分
     * 返回: activityId -> [学生人数, 教师人数]
     */
    private Map<Long, int[]> countEnrollments(List<Long> activityIds) {
        Map<Long, int[]> map = new HashMap<>();
        if (activityIds == null || activityIds.isEmpty()) {
            return map;
        }
        for (Map<String, Object> row : enrollmentMapper.countByActivityAndRole(activityIds)) {
            Long activityId = ((Number) row.get("activityId")).longValue();
            String role = (String) row.get("role");
            int cnt = ((Number) row.get("cnt")).intValue();
            int[] counts = map.computeIfAbsent(activityId, k -> new int[2]);
            if ("STUDENT".equals(role)) {
                counts[0] += cnt;
            } else if ("TEACHER".equals(role)) {
                counts[1] += cnt;
            }
        }
        return map;
    }

    private void applyRequestToActivity(ActivityRequest request, Activity activity) {
        activity.setTitle(request.title());
        activity.setDescription(request.description());
        activity.setType(request.type());
        activity.setCategory(request.category());
        activity.setLevel(request.level());
        if (request.status() != null) {
            activity.setStatus(request.status());
        }
        activity.setTargetAudience(request.targetAudience());
        activity.setStartTime(request.startTime());
        activity.setEndTime(request.endTime());
        activity.setEnrollStart(request.enrollStart());
        activity.setEnrollEnd(request.enrollEnd());
        activity.setLocation(request.location());
        activity.setMaxStudents(request.maxStudents());
        activity.setMaxTeachers(request.maxTeachers());
        activity.setMaxPerSchool(request.maxPerSchool());
        activity.setBannerUrl(request.bannerUrl());
        activity.setBannerLink(request.bannerLink());
        activity.setGroupRule(request.groupRule());
        activity.setRankRule(request.rankRule());
        activity.setCollegeId(request.collegeId());
        activity.setCollegeName(request.collegeName());
        activity.setWorkflowKey(request.workflowKey());
    }
}
