package com.xju.enrollment.modules.workflow.dto;

/**
 * 审批统计：基于数据库真实数据（不含已撤回）
 */
public record ApprovalStatsVO(
        long submitted,
        long approving,
        long approved,
        long rejected,
        long total,
        int approvalRate
) {
}