package com.zhangzhankui.seed.common.datasource.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** BaseEntity 测试 - 验证审计字段、扩展参数和逻辑删除 */
@DisplayName("BaseEntity测试")
class BaseEntityTest {

  /** 测试用的具体实现 */
  private static class TestEntity extends BaseEntity {}

  @Test
  @DisplayName("应实现Serializable接口")
  void shouldImplementSerializable() {
    BaseEntity entity = new TestEntity();
    assertThat(entity).isInstanceOf(java.io.Serializable.class);
  }

  @Test
  @DisplayName("应能设置和获取创建时间")
  void shouldSetAndGetCreateTime() {
    BaseEntity entity = new TestEntity();
    LocalDateTime now = LocalDateTime.now();

    entity.setCreateTime(now);
    assertThat(entity.getCreateTime()).isEqualTo(now);
  }

  @Test
  @DisplayName("应能设置和获取更新时间")
  void shouldSetAndGetUpdateTime() {
    BaseEntity entity = new TestEntity();
    LocalDateTime now = LocalDateTime.now();

    entity.setUpdateTime(now);
    assertThat(entity.getUpdateTime()).isEqualTo(now);
  }

  @Test
  @DisplayName("应能设置和获取创建者")
  void shouldSetAndGetCreateBy() {
    BaseEntity entity = new TestEntity();
    entity.setCreateBy("admin");
    assertThat(entity.getCreateBy()).isEqualTo("admin");
  }

  @Test
  @DisplayName("应能设置和获取更新者")
  void shouldSetAndGetUpdateBy() {
    BaseEntity entity = new TestEntity();
    entity.setUpdateBy("admin");
    assertThat(entity.getUpdateBy()).isEqualTo("admin");
  }

  @Test
  @DisplayName("应能设置和获取备注")
  void shouldSetAndGetRemark() {
    BaseEntity entity = new TestEntity();
    entity.setRemark("test remark");
    assertThat(entity.getRemark()).isEqualTo("test remark");
  }

  @Test
  @DisplayName("应能设置和获取逻辑删除标志")
  void shouldSetAndGetDeleted() {
    BaseEntity entity = new TestEntity();
    entity.setDeleted(0);
    assertThat(entity.getDeleted()).isEqualTo(0);

    entity.setDeleted(1);
    assertThat(entity.getDeleted()).isEqualTo(1);
  }

  @Test
  @DisplayName("应能设置和获取租户ID")
  void shouldSetAndGetTenantId() {
    BaseEntity entity = new TestEntity();
    entity.setTenantId("tenant001");
    assertThat(entity.getTenantId()).isEqualTo("tenant001");
  }

  @Test
  @DisplayName("getParams 应在 null 时自动初始化")
  void getParamsShouldAutoInitialize() {
    BaseEntity entity = new TestEntity();
    Map<String, Object> params = entity.getParams();
    assertThat(params).isNotNull().isEmpty();
  }

  @Test
  @DisplayName("addParam 应添加扩展参数")
  void addParamShouldAddExtensionParameter() {
    BaseEntity entity = new TestEntity();
    entity.addParam("key1", "value1");
    entity.addParam("key2", 42);

    assertThat(entity.getParams()).containsEntry("key1", "value1").containsEntry("key2", 42);
  }

  @Test
  @DisplayName("dataScopeSql 应能设置和获取")
  void shouldSetAndGetDataScopeSql() {
    BaseEntity entity = new TestEntity();
    entity.setDataScopeSql("AND dept_id = 1");
    assertThat(entity.getDataScopeSql()).isEqualTo("AND dept_id = 1");
  }
}
