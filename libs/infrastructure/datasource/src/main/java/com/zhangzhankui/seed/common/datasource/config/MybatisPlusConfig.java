package com.zhangzhankui.seed.common.datasource.config;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zhangzhankui.seed.common.core.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/** MyBatis Plus 配置 */
@AutoConfiguration
@RequiredArgsConstructor
public class MybatisPlusConfig implements MetaObjectHandler {

  private final UserContextHolder userContextHolder;

  /** 数据库类型，通过配置注入，默认 PostgreSQL */
  @Value("${mybatis-plus.db-type:postgresql}")
  private String dbType;

  /** 分页插件 */
  @Bean
  public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

    // 分页插件 - 通过配置注入数据库类型
    PaginationInnerInterceptor paginationInterceptor =
        new PaginationInnerInterceptor(DbType.getDbType(dbType));
    paginationInterceptor.setMaxLimit(500L);
    interceptor.addInnerInterceptor(paginationInterceptor);

    // 乐观锁插件
    interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

    return interceptor;
  }

  /** 插入时自动填充 */
  @Override
  public void insertFill(MetaObject metaObject) {
    this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
    this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

    // 获取当前用户（通过抽象接口，解耦认证模块）
    if (userContextHolder.isLogin()) {
      String username = userContextHolder.getUsername();
      String tenantId = userContextHolder.getTenantId();

      this.strictInsertFill(metaObject, "createBy", String.class, username);
      this.strictInsertFill(metaObject, "updateBy", String.class, username);
      this.strictInsertFill(metaObject, "tenantId", String.class, tenantId);
    }
  }

  /** 更新时自动填充 */
  @Override
  public void updateFill(MetaObject metaObject) {
    this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

    if (userContextHolder.isLogin()) {
      String username = userContextHolder.getUsername();
      this.strictUpdateFill(metaObject, "updateBy", String.class, username);
    }
  }
}
