package com.zhangzhankui.seed.common.web.aspect;

import java.lang.reflect.Method;
import java.util.Collections;

import com.zhangzhankui.seed.common.core.annotation.RateLimiter;
import com.zhangzhankui.seed.common.core.constant.CacheConstants;
import com.zhangzhankui.seed.common.core.exception.ServiceException;
import com.zhangzhankui.seed.common.core.utils.ServletUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/** 限流切面 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimiterAspect {

  private final RedisTemplate<String, Object> redisTemplate;

  /** Lua 脚本：原子递增并设置过期时间 */
  private static final String RATE_LIMITER_LUA =
      "local current = redis.call('incr', KEYS[1])\n"
          + "if current == 1 then\n"
          + "  redis.call('expire', KEYS[1], ARGV[1])\n"
          + "end\n"
          + "return current";

  private static final DefaultRedisScript<Long> RATE_LIMITER_SCRIPT;

  static {
    RATE_LIMITER_SCRIPT = new DefaultRedisScript<>();
    RATE_LIMITER_SCRIPT.setScriptText(RATE_LIMITER_LUA);
    RATE_LIMITER_SCRIPT.setResultType(Long.class);
  }

  @Before("@annotation(rateLimiter)")
  public void doBefore(JoinPoint point, RateLimiter rateLimiter) {
    String key = getCombineKey(rateLimiter, point);
    long time = rateLimiter.timeUnit().toSeconds(rateLimiter.time());
    int count = rateLimiter.count();

    Long current =
        redisTemplate.execute(RATE_LIMITER_SCRIPT, Collections.singletonList(key), time);
    if (current == null) {
      throw new ServiceException("服务器繁忙，请稍后重试");
    }

    if (current > count) {
      log.warn("限流触发，key={}, count={}, current={}", key, count, current);
      throw new ServiceException(rateLimiter.message());
    }
  }

  private String getCombineKey(RateLimiter rateLimiter, JoinPoint point) {
    StringBuilder key = new StringBuilder(CacheConstants.RATE_LIMIT_KEY);

    // 添加自定义 key
    if (!rateLimiter.key().isEmpty()) {
      key.append(rateLimiter.key());
    } else {
      MethodSignature signature = (MethodSignature) point.getSignature();
      Method method = signature.getMethod();
      key.append(method.getDeclaringClass().getName()).append(':').append(method.getName());
    }

    // 根据限流类型添加后缀
    switch (rateLimiter.limitType()) {
      case IP -> key.append(':').append(ServletUtils.getClientIp());
      case USER -> {
        try {
          key.append(':')
              .append(com.zhangzhankui.seed.common.security.utils.SecurityUtils.getUserId());
        } catch (Exception ignored) {
          key.append(":anonymous");
        }
      }
      default -> {
        // DEFAULT 类型不需要额外的 key 后缀
      }
    }

    return key.toString();
  }
}
