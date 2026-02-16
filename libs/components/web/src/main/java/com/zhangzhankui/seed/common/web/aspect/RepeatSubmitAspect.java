package com.zhangzhankui.seed.common.web.aspect;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangzhankui.seed.common.core.annotation.RepeatSubmit;
import com.zhangzhankui.seed.common.core.constant.CacheConstants;
import com.zhangzhankui.seed.common.core.exception.ServiceException;
import com.zhangzhankui.seed.common.core.utils.ServletUtils;
import com.zhangzhankui.seed.common.redis.utils.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/** 防重复提交切面 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RepeatSubmitAspect {

  private final RedisUtils redisUtils;
  private final ObjectMapper objectMapper;

  @Before("@annotation(repeatSubmit)")
  public void doBefore(JoinPoint point, RepeatSubmit repeatSubmit) throws Exception {
    HttpServletRequest request = ServletUtils.getRequest();
    if (request == null) {
      return;
    }

    String key = generateKey(request, point);
    long interval = repeatSubmit.timeUnit().toMillis(repeatSubmit.interval());

    if (!redisUtils.setIfAbsent(key, "1", interval, TimeUnit.MILLISECONDS)) {
      log.warn("重复提交拦截，key={}", key);
      throw new ServiceException(repeatSubmit.message());
    }
  }

  private String generateKey(HttpServletRequest request, JoinPoint point) throws Exception {
    String uri = request.getRequestURI();
    String token = request.getHeader("Authorization");
    if (token == null) {
      token = ServletUtils.getClientIp();
    }
    String params = objectMapper.writeValueAsString(point.getArgs());

    String hash = md5(uri + token + params);
    return CacheConstants.REPEAT_SUBMIT_KEY + hash;
  }

  /** 计算 MD5 哈希值 */
  private String md5(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 algorithm not available", e);
    }
  }
}
