package com.zhangzhankui.samples.common.web.aspect;

import com.zhangzhankui.samples.common.core.util.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Web请求日志切面
 */
@Slf4j
@Aspect
@Component
public class WebLogAspect {

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restControllerPointcut() {
    }

    @Pointcut("@within(org.springframework.stereotype.Controller)")
    public void controllerPointcut() {
    }

    @Around("restControllerPointcut() || controllerPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            return joinPoint.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // 请求日志
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String args = getArgs(joinPoint.getArgs());
        
        log.info("┌─────────────────────────────────────────────────────────");
        log.info("│ URL: {} {}", request.getMethod(), request.getRequestURI());
        log.info("│ Class: {}.{}", className, methodName);
        log.info("│ IP: {}", getClientIp(request));
        log.info("│ Args: {}", args);
        
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.info("│ Cost: {}ms", costTime);
            log.info("│ Exception: {}", e.getMessage());
            log.info("└─────────────────────────────────────────────────────────");
            throw e;
        }
        
        long costTime = System.currentTimeMillis() - startTime;
        log.info("│ Cost: {}ms", costTime);
        log.info("│ Response: {}", truncateResponse(JsonUtils.toJson(result)));
        log.info("└─────────────────────────────────────────────────────────");
        
        return result;
    }

    /**
     * 获取请求参数
     */
    private String getArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        List<Object> filteredArgs = Arrays.stream(args)
                .filter(arg -> !(arg instanceof HttpServletRequest))
                .filter(arg -> !(arg instanceof HttpServletResponse))
                .filter(arg -> !(arg instanceof MultipartFile))
                .collect(Collectors.toList());
        
        try {
            String json = JsonUtils.toJson(filteredArgs);
            return truncate(json, 500);
        } catch (Exception e) {
            return "[无法序列化]";
        }
    }

    /**
     * 截断响应
     */
    private String truncateResponse(String response) {
        return truncate(response, 1000);
    }

    /**
     * 字符串截断
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...(truncated)";
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
