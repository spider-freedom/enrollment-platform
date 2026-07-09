package com.xju.enrollment.modules.statistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xju.enrollment.entity.Activity;
import com.xju.enrollment.entity.Enrollment;
import com.xju.enrollment.entity.Feedback;
import com.xju.enrollment.mapper.ActivityMapper;
import com.xju.enrollment.mapper.EnrollmentMapper;
import com.xju.enrollment.mapper.FeedbackMapper;
import com.xju.enrollment.modules.statistics.dto.CollegeStatVO;
import com.xju.enrollment.modules.statistics.dto.DashboardVO;
import com.xju.enrollment.modules.statistics.dto.TrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ActivityMapper activityMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final FeedbackMapper feedbackMapper;

    public DashboardVO getDashboard() {
        long totalActivities = activityMapper.selectCount(null);

        List<Enrollment> allEnrollments = enrollmentMapper.selectList(null);
        long totalEnrollments = allEnrollments.size();

        long approvedCount = allEnrollments.stream()
                .filter(e -> "APPROVED".equals(e.getStatus()))
                .count();

        double approvalRate = totalEnrollments > 0
                ? (double) approvedCount / totalEnrollments * 100
                : 0.0;

        List<Feedback> allFeedbacks = feedbackMapper.selectList(null);

        double feedbackRate = approvedCount > 0
                ? (double) allFeedbacks.size() / approvedCount * 100
                : 0.0;

        double avgRating = allFeedbacks.stream()
                .filter(f -> f.getRating() != null)
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        return new DashboardVO(totalActivities, totalEnrollments,
                Math.round(approvalRate * 100.0) / 100.0,
                Math.round(feedbackRate * 100.0) / 100.0,
                Math.round(avgRating * 100.0) / 100.0);
    }

    public TrendVO getMonthlyTrend() {
        List<String> months = new ArrayList<>();
        List<Long> studentCounts = new ArrayList<>();
        List<Long> teacherCounts = new ArrayList<>();

        YearMonth current = YearMonth.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 6; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            months.add(ym.format(fmt));

            LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = ym.plusMonths(1).atDay(1).atStartOfDay();

            long studentCount = enrollmentMapper.selectCount(
                    new LambdaQueryWrapper<Enrollment>()
                            .ge(Enrollment::getCreateTime, monthStart)
                            .lt(Enrollment::getCreateTime, monthEnd)
                            .eq(Enrollment::getStatus, "APPROVED")
            );

            long teacherCount = enrollmentMapper.selectCount(
                    new LambdaQueryWrapper<Enrollment>()
                            .ge(Enrollment::getCreateTime, monthStart)
                            .lt(Enrollment::getCreateTime, monthEnd)
            );

            studentCounts.add(studentCount);
            teacherCounts.add(teacherCount);
        }

        return new TrendVO(months, studentCounts, teacherCounts);
    }

    public List<CollegeStatVO> getCollegeDistribution() {
        List<Enrollment> allEnrollments = enrollmentMapper.selectList(
                new LambdaQueryWrapper<Enrollment>()
                        .isNotNull(Enrollment::getCollegeName)
        );

        Map<String, Long> collegeCountMap = allEnrollments.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCollegeName() != null ? e.getCollegeName() : "未知学院",
                        Collectors.counting()
                ));

        return collegeCountMap.entrySet().stream()
                .map(e -> new CollegeStatVO(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.enrollmentCount(), a.enrollmentCount()))
                .collect(Collectors.toList());
    }

    public Map<Integer, Long> getRatingDistribution() {
        List<Feedback> allFeedbacks = feedbackMapper.selectList(
                new LambdaQueryWrapper<Feedback>()
                        .isNotNull(Feedback::getRating)
        );

        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }

        allFeedbacks.stream()
                .filter(f -> f.getRating() != null && f.getRating() >= 1 && f.getRating() <= 5)
                .forEach(f -> distribution.merge(f.getRating(), 1L, Long::sum));

        return distribution;
    }
}
