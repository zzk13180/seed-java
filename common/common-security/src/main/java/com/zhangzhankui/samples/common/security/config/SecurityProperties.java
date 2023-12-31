package com.zhangzhankui.samples.common.security.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 安全配置属性
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private final Environment environment;

    /**
     * JWT密钥最小长度(Base64解码后的字节数)
     */
    private static final int MIN_SECRET_KEY_LENGTH = 32;

    /**
     * 最小熵值要求 (bits per character)
     * 安全的随机密钥熵值通常 > 4.0
     */
    private static final double MIN_ENTROPY = 3.5;

    /**
     * 不安全的密钥模式（正则表达式）
     */
    private static final List<Pattern> INSECURE_PATTERNS = List.of(
            Pattern.compile("^(.)\\1+$"),                          // 重复字符: aaaa, 1111
            Pattern.compile("^(012|123|234|345|456|567|678|789)+"), // 连续数字
            Pattern.compile("^(abc|bcd|cde|def|efg)+", Pattern.CASE_INSENSITIVE), // 连续字母
            Pattern.compile("^(password|secret|key|token|jwt)", Pattern.CASE_INSENSITIVE), // 常见弱词
            Pattern.compile("^[a-z]+$"),                           // 纯小写字母
            Pattern.compile("^[0-9]+$")                            // 纯数字
    );

    public SecurityProperties(Environment environment) {
        this.environment = environment;
    }

    /**
     * JWT密钥
     * 
     * ⚠️ 安全警告：默认值仅供开发测试使用！
     * 生产环境必须通过环境变量 JWT_SECRET 设置一个安全的随机密钥。
     * 推荐使用以下命令生成：openssl rand -base64 64
     */
    private String jwtSecret = "dev-only-default-jwt-secret-key-please-change-in-production-environment-12345";

    /**
     * JWT过期时间 (秒) - 默认2小时
     */
    private long jwtExpiration = 7200;

    /**
     * 刷新Token过期时间 (秒) - 默认7天
     */
    private long refreshTokenExpiration = 604800;

    /**
     * Token请求头名称
     */
    private String tokenHeader = "Authorization";

    /**
     * Token前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * 忽略认证的路径
     */
    private List<String> ignorePaths = new ArrayList<>();

    /**
     * 是否启用安全
     */
    private boolean enabled = true;

    // Getters and Setters
    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }

    public void setJwtExpiration(long jwtExpiration) {
        this.jwtExpiration = jwtExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(long refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String getTokenHeader() {
        return tokenHeader;
    }

    public void setTokenHeader(String tokenHeader) {
        this.tokenHeader = tokenHeader;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public List<String> getIgnorePaths() {
        return ignorePaths != null ? Collections.unmodifiableList(ignorePaths) : Collections.emptyList();
    }

    public void setIgnorePaths(List<String> ignorePaths) {
        this.ignorePaths = ignorePaths != null ? new ArrayList<>(ignorePaths) : new ArrayList<>();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 启动时校验JWT密钥安全性
     */
    @PostConstruct
    public void validateJwtSecret() {
        if (!enabled) {
            log.info("Security is disabled, skipping JWT secret validation");
            return;
        }

        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");

        // 检查JWT密钥是否设置
        if (jwtSecret == null || jwtSecret.isBlank()) {
            String message = "JWT_SECRET environment variable is not set! Please set a secure JWT secret.";
            throw new IllegalStateException("[FATAL] " + message);
        }

        // 检查是否使用默认密钥
        if (jwtSecret.contains("dev-only-default")) {
            if (isProd) {
                throw new IllegalStateException("[FATAL] Cannot use default JWT secret in production! Set JWT_SECRET environment variable.");
            } else {
                log.warn("[SECURITY WARNING] Using default JWT secret. This is acceptable for development only.");
            }
        }

        // 检查是否匹配不安全模式
        for (Pattern pattern : INSECURE_PATTERNS) {
            if (pattern.matcher(jwtSecret).find()) {
                handleSecurityWarning("JWT secret matches insecure pattern: " + pattern.pattern(), isProd);
                break;
            }
        }

        // 检查熵值（密钥随机性）
        double entropy = calculateEntropy(jwtSecret);
        if (entropy < MIN_ENTROPY) {
            handleSecurityWarning(
                    String.format("JWT secret entropy (%.2f) is below minimum (%.2f). Use a more random key.", 
                            entropy, MIN_ENTROPY), 
                    isProd);
        }

        // 检查密钥长度
        try {
            byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
            if (decodedKey.length < MIN_SECRET_KEY_LENGTH) {
                String message = String.format("JWT secret key length (%d bytes) is less than minimum required (%d bytes)",
                        decodedKey.length, MIN_SECRET_KEY_LENGTH);
                if (isProd) {
                    throw new IllegalStateException("[FATAL] " + message);
                } else {
                    log.warn("[SECURITY WARNING] {}", message);
                }
            }
        } catch (IllegalArgumentException e) {
            // 不是有效的Base64，检查原始长度
            if (jwtSecret.length() < MIN_SECRET_KEY_LENGTH) {
                String message = String.format("JWT secret length (%d chars) is less than minimum required (%d chars)",
                        jwtSecret.length(), MIN_SECRET_KEY_LENGTH);
                if (isProd) {
                    throw new IllegalStateException("[FATAL] " + message);
                } else {
                    log.warn("[SECURITY WARNING] {}", message);
                }
            }
        }

        if (isProd) {
            log.info("JWT secret validation passed for production environment");
        }
    }

    /**
     * 计算字符串的香农熵 (Shannon Entropy)
     * 熵值越高表示随机性越强，安全性越好
     * 
     * @param input 输入字符串
     * @return 熵值 (bits per character)
     */
    private double calculateEntropy(String input) {
        if (input == null || input.isEmpty()) {
            return 0.0;
        }

        // 统计每个字符出现的频率
        int[] charCounts = new int[256];
        for (char c : input.toCharArray()) {
            if (c < 256) {
                charCounts[c]++;
            }
        }

        // 计算香农熵
        double entropy = 0.0;
        int length = input.length();
        for (int count : charCounts) {
            if (count > 0) {
                double probability = (double) count / length;
                entropy -= probability * (Math.log(probability) / Math.log(2));
            }
        }

        return entropy;
    }

    /**
     * 处理安全警告
     * 生产环境抛出异常，开发环境仅记录警告
     */
    private void handleSecurityWarning(String message, boolean isProd) {
        if (isProd) {
            throw new IllegalStateException("[FATAL] " + message);
        } else {
            log.warn("[SECURITY WARNING] {} This is acceptable for development only.", message);
        }
    }
}
