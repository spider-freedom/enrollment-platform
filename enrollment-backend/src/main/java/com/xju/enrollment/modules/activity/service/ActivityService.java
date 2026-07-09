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
import com.xju.enrollment.modules.activity.dto.ActivityListQuery;
import com.xju.enrollment.modules.activity.dto.ActivityRequest;
import com.xju.enrollment.modules.activity.dto.ActivityVO;
import com.xju.enrollment.modules.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityMapper activityMapper;
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
        return ActivityVO.from(activity, creatorName);
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

    public PageResult<ActivityVO> listForCollege(ActivityListQuery query) {
        LambdaQueryWrapper<Activity> wrapper = buildBaseQuery(query);
        return doPageQuery(query, wrapper);
    }

    public PageResult<ActivityVO> listForSchool(ActivityListQuery query) {
        LambdaQueryWrapper<Activity> wrapper = buildBaseQuery(query);
        return doPageQuery(query, wrapper);
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

        List<ActivityVO> voList = result.getRecords().stream()
                .map(a -> ActivityVO.from(a, creatorNameMap.get(a.getCreatorId())))
                .toList();

        return PageResult.of(voList, result.getTotal(), query.page(), query.size());
    }

    private void applyRequestToActivity(ActivityRequest request, Activity activity) {
        activity.setTitle(request.title());
        activity.setDescription(request.description());
        activity.setType(request.type());
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
        activity.setWorkflowKey(request.workflowKey());
    }
}
