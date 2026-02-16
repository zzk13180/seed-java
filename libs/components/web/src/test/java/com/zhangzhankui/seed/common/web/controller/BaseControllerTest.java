package com.zhangzhankui.seed.common.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.zhangzhankui.seed.common.core.domain.ApiResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * BaseController 单元测试
 *
 * <p>测试基础控制器的响应封装和便捷方法
 */
@DisplayName("BaseController 单元测试")
class BaseControllerTest {

  private final TestBaseController controller = new TestBaseController();

  @Test
  @DisplayName("创建成功响应")
  void shouldCreateSuccessResult() {
    ApiResult<String> result = controller.success("test data", "操作成功");

    assertThat(result.getCode()).isEqualTo(200);
    assertThat(result.getMessage()).isEqualTo("操作成功");
    assertThat(result.getData()).isEqualTo("test data");
  }

  @Test
  @DisplayName("创建成功响应（无数据）")
  void shouldCreateSuccessResultWithoutData() {
    ApiResult<Void> result = controller.success();

    assertThat(result.getCode()).isEqualTo(200);
    assertThat(result.getMessage()).isEqualTo("操作成功");
    assertThat(result.getData()).isNull();
  }

  @Test
  @DisplayName("创建带数据的成功响应")
  void shouldCreateSuccessResultWithData() {
    ApiResult<Integer> result = controller.success(42);

    assertThat(result.getCode()).isEqualTo(200);
    assertThat(result.getData()).isEqualTo(42);
  }

  @Test
  @DisplayName("创建带消息的成功响应")
  void shouldCreateSuccessResultWithMessage() {
    ApiResult<String> result = controller.success("自定义消息");

    assertThat(result.getCode()).isEqualTo(200);
    assertThat(result.getMessage()).isEqualTo("自定义消息");
  }

  @Test
  @DisplayName("创建错误响应")
  void shouldCreateErrorResult() {
    ApiResult<Void> result = controller.fail("测试错误");

    assertThat(result.getCode()).isEqualTo(500);
    assertThat(result.getMessage()).isEqualTo("测试错误");
    assertThat(result.getData()).isNull();
  }

  @Test
  @DisplayName("创建错误响应（无消息）")
  void shouldCreateErrorResultWithoutMessage() {
    ApiResult<Void> result = controller.fail();

    assertThat(result.getCode()).isEqualTo(500);
    assertThat(result.getMessage()).isEqualTo("操作失败");
    assertThat(result.getData()).isNull();
  }

  @Test
  @DisplayName("toAjax - boolean true 应返回成功")
  void toAjaxBooleanTrueShouldReturnSuccess() {
    ApiResult<Void> result = controller.toAjax(true);

    assertThat(result.getCode()).isEqualTo(200);
  }

  @Test
  @DisplayName("toAjax - boolean false 应返回失败")
  void toAjaxBooleanFalseShouldReturnFail() {
    ApiResult<Void> result = controller.toAjax(false);

    assertThat(result.getCode()).isEqualTo(500);
    assertThat(result.getMessage()).isEqualTo("操作失败");
  }

  @Test
  @DisplayName("toAjax - 正数行数应返回成功")
  void toAjaxPositiveRowsShouldReturnSuccess() {
    ApiResult<Void> result = controller.toAjax(1);

    assertThat(result.getCode()).isEqualTo(200);
  }

  @Test
  @DisplayName("toAjax - 零行数应返回失败")
  void toAjaxZeroRowsShouldReturnFail() {
    ApiResult<Void> result = controller.toAjax(0);

    assertThat(result.getCode()).isEqualTo(500);
    assertThat(result.getMessage()).isEqualTo("操作失败");
  }

  @Test
  @DisplayName("无 RequestContext 时 getRequest 应返回 null")
  void getRequestShouldReturnNullWithoutContext() {
    assertThat(controller.getRequest()).isNull();
  }

  @Test
  @DisplayName("无 RequestContext 时 getParameter 应返回 null")
  void getParameterShouldReturnNullWithoutContext() {
    assertThat(controller.getParameter("key")).isNull();
  }

  @Test
  @DisplayName("无 RequestContext 时 getParameter 带默认值应返回默认值")
  void getParameterWithDefaultShouldReturnDefault() {
    assertThat(controller.getParameter("key", "default")).isEqualTo("default");
  }

  @Test
  @DisplayName("无 RequestContext 时 getParameterInt 应返回 null")
  void getParameterIntShouldReturnNullWithoutContext() {
    assertThat(controller.getParameterInt("key")).isNull();
  }

  @Test
  @DisplayName("无 RequestContext 时 getParameterInt 带默认值应返回默认值")
  void getParameterIntWithDefaultShouldReturnDefault() {
    assertThat(controller.getParameterInt("key", 10)).isEqualTo(10);
  }

  /** 测试用的具体 BaseController 实现类 - 暴露 protected 方法 */
  private static class TestBaseController extends BaseController {

    // Re-expose protected methods as public for testing

    @Override
    public <T> ApiResult<T> success() {
      return super.success();
    }

    @Override
    public <T> ApiResult<T> success(T data) {
      return super.success(data);
    }

    @Override
    public <T> ApiResult<T> success(String msg) {
      return super.success(msg);
    }

    @Override
    public <T> ApiResult<T> success(T data, String msg) {
      return super.success(data, msg);
    }

    @Override
    public <T> ApiResult<T> fail() {
      return super.fail();
    }

    @Override
    public <T> ApiResult<T> fail(String msg) {
      return super.fail(msg);
    }

    @Override
    public ApiResult<Void> toAjax(boolean result) {
      return super.toAjax(result);
    }

    @Override
    public ApiResult<Void> toAjax(int rows) {
      return super.toAjax(rows);
    }

    @Override
    public String getParameter(String name) {
      return super.getParameter(name);
    }

    @Override
    public String getParameter(String name, String defaultValue) {
      return super.getParameter(name, defaultValue);
    }

    @Override
    public Integer getParameterInt(String name) {
      return super.getParameterInt(name);
    }

    @Override
    public Integer getParameterInt(String name, Integer defaultValue) {
      return super.getParameterInt(name, defaultValue);
    }
  }
}
