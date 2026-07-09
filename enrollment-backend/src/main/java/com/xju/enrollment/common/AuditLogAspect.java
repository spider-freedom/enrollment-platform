package com.xju.enrollment.common;

import com.xju.enrollment.entity.AuditLog;
import com.xju.enrollment.entity.User;
import com.xju.enrollment.mapper.AuditLogMapper;
import com.xju.enrollment.mapper.UserMapper;
import com.xju.enrollment.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogMapper auditLogMapper;
    private final UserMapper userMapper;
    private final HttpServletRequest request;

    private static final Map<String, String> ACTION_MAP = Map.ofEntries(
            Map.entry("approve", "APPROVE"),
            Map.entry("reject", "REJECT"),
            Map.entry("submit", "SUBMIT"),
            Map.entry("reply", "REPLY"),
            Map.entry("delete", "DELETE"),
            Map.entry("login", "LOGIN"),
            Map.entry("register", "REGISTER"),
            Map.entry("export", "EXPORT"),
            Map.entry("withdraw", "WITHDRAW"),
            Map.entry("batchApprove", "BATCH_APPROVE"),
            Map.entry("batch", "BATCH"),
            Map.entry("changePassword", "CHANGE_PASSWORD"),
            Map.entry("updateProfile", "UPDATE_PROFILE")
    );

    private static final Map<String, String> MODULE_MAP = Map.ofEntries(
            Map.entry("EnrollmentController", "ENROLLMENT"),
            Map.entry("FeedbackController", "FEEDBACK"),
            Map.entry("ApprovalController", "APPROVAL"),
            Map.entry("ActivityController", "ACTIVITY"),
            Map.entry("AuthController", "AUTH"),
            Map.entry("UserController", "USER"),
            Map.entry("CollegeUserController", "COLLEGE_USER"),
            Map.entry("AttachmentController", "ATTACHMENT"),
            Map.entry("StatisticsController", "STATISTICS"),
            Map.entry("AiController", "AI")
    );

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMappingMethods() {}

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMappingMethods() {}

    @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void deleteMappingMethods() {}

    @Around("postMappingMethods() || putMappingMethods() || deleteMappingMethods()")
    public Object auditLog(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            String userIdStr = SecurityUtils.getCurrentUserId();
            Long userId = null;
            String username = null;

            if (userIdStr != null) {
                try {
                    userId = Long.valueOf(userIdStr);
                    User user = userMapper.selectById(userId);
                    if (user != null) {
                        username = user.getUsername();
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid userId from SecurityUtils: {}", userIdStr);
                }
            }

            String methodName = joinPoint.getSignature().getName();
            String action = resolveAction(methodName);

            String className = joinPoint.getTarget().getClass().getSimpleName();
            String module = resolveModule(className);

            String target = request.getRequestURI();
            String ip = getClientIp(request);

            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setUsername(username);
            auditLog.setAction(action);
            auditLog.setModule(module);
            auditLog.setTarget(target);
            auditLog.setDetail(methodName);
            auditLog.setIp(ip);
            auditLog.setCreateTime(LocalDateTime.now());

            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }

        return result;
    }

    private String resolveAction(String methodName) {
        String lower = methodName.toLowerCase();
        for (Map.Entry<String, String> entry : ACTION_MAP.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return methodName.toUpperCase().replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }

    private String resolveModule(String className) {
        return MODULE_MAP.getOrDefault(className,
                className.replace("Controller", "").replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase());
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
