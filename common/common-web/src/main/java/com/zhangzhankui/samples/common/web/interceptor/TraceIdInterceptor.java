package com.zhangzhankui.samples.common.web.interceptor;

import com.zhangzhankui.samples.common.core.constant.CommonConstants;
import com.zhangzhankui.samples.common.core.util.IdUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 追踪ID拦截器
 * <p>
 * 为每个请求生成唯一的追踪ID，用于日志追踪
 */
public class TraceIdInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 优先从请求头获取追踪ID
        String traceId = request.getHeader(CommonConstants.TRACE_ID_HEADER);
        
        // 如果没有则生成新的
        if (!StringUtils.hasText(traceId)) {
            traceId = IdUtils.traceId();
        }
        
        // 放入MDC
        MDC.put(TRACE_ID_KEY, traceId);
        
        // 添加到响应头
        response.setHeader(CommonConstants.TRACE_ID_HEADER, traceId);
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) {
        // Do nothing
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        // 清除MDC
        MDC.remove(TRACE_ID_KEY);
    }
}
