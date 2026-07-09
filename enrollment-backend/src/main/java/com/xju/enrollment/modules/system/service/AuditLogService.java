package com.xju.enrollment.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xju.enrollment.common.PageResult;
import com.xju.enrollment.entity.AuditLog;
import com.xju.enrollment.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    public PageResult<AuditLog> queryLogs(int page, int size, String action, String module) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(action != null && !action.isEmpty(), AuditLog::getAction, action);
        wrapper.eq(module != null && !module.isEmpty(), AuditLog::getModule, module);
        wrapper.orderByDesc(AuditLog::getCreateTime);

        Page<AuditLog> p = auditLogMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(p.getRecords(), p.getTotal(), page, size);
    }
}
