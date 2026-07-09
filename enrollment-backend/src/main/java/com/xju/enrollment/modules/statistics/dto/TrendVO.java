package com.xju.enrollment.modules.statistics.dto;

import java.util.List;

public record TrendVO(
        List<String> months,
        List<Long> studentCounts,
        List<Long> teacherCounts
) {}
