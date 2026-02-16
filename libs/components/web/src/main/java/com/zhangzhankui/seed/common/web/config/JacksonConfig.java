package com.zhangzhankui.seed.common.web.config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/** Jackson 配置 */
@AutoConfiguration
public class JacksonConfig {

  private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
  private static final String DATE_PATTERN = "yyyy-MM-dd";
  private static final String TIME_PATTERN = "HH:mm:ss";

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer customizer() {
    return builder -> {
      // 时区
      builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));

      // Long 类型转 String，避免前端精度丢失
      builder.serializerByType(Long.class, ToStringSerializer.instance);
      builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
      builder.serializerByType(BigInteger.class, ToStringSerializer.instance);
      builder.serializerByType(BigDecimal.class, ToStringSerializer.instance);

      // Java 8 时间类型
      JavaTimeModule timeModule = new JavaTimeModule();

      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
      DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN);

      timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
      timeModule.addDeserializer(
          LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

      timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
      timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

      timeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
      timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));

      builder.modules(timeModule);
    };
  }
}
