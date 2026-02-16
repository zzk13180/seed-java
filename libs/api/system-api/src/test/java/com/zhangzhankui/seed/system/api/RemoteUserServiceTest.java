package com.zhangzhankui.seed.system.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.zhangzhankui.seed.common.core.domain.ApiResult;
import com.zhangzhankui.seed.common.core.domain.LoginUser;
import com.zhangzhankui.seed.system.api.dto.SysUserDTO;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 远程用户服务接口契约测试
 *
 * <p>验证方法签名、参数类型和返回类型匹配消费者期望，防止接口变更破坏调用方。
 */
@DisplayName("远程用户服务接口契约测试")
class RemoteUserServiceTest {

  @Test
  @DisplayName("HttpExchange 应指向 /system/user 路径")
  void httpExchangeShouldPointToSystemUserPath() {
    HttpExchange annotation = RemoteUserService.class.getAnnotation(HttpExchange.class);
    assertThat(annotation).isNotNull();
    assertThat(annotation.value()).isEqualTo("/system/user");
  }

  @Test
  @DisplayName("getUserInfo 应接受 String 参数并返回 ApiResult<LoginUser>")
  void getUserInfoSignature() throws NoSuchMethodException {
    Method method = RemoteUserService.class.getMethod("getUserInfo", String.class);
    assertThat(method.getAnnotation(GetExchange.class)).isNotNull();

    Type returnType = method.getGenericReturnType();
    assertThat(returnType).isInstanceOf(ParameterizedType.class);
    ParameterizedType pt = (ParameterizedType) returnType;
    assertThat(pt.getRawType()).isEqualTo(ApiResult.class);
    assertThat(pt.getActualTypeArguments()[0]).isEqualTo(LoginUser.class);
  }

  @Test
  @DisplayName("getUserInfoById 应接受 Long 参数并返回 ApiResult<LoginUser>")
  void getUserInfoByIdSignature() throws NoSuchMethodException {
    Method method = RemoteUserService.class.getMethod("getUserInfoById", Long.class);
    assertThat(method.getAnnotation(GetExchange.class)).isNotNull();

    Type returnType = method.getGenericReturnType();
    assertThat(returnType).isInstanceOf(ParameterizedType.class);
    ParameterizedType pt = (ParameterizedType) returnType;
    assertThat(pt.getRawType()).isEqualTo(ApiResult.class);
    assertThat(pt.getActualTypeArguments()[0]).isEqualTo(LoginUser.class);
  }

  @Test
  @DisplayName("createUser 应接受 SysUserDTO 并返回 ApiResult<Void>")
  void createUserSignature() throws NoSuchMethodException {
    Method method = RemoteUserService.class.getMethod("createUser", SysUserDTO.class);
    assertThat(method.getAnnotation(PostExchange.class)).isNotNull();

    Type returnType = method.getGenericReturnType();
    assertThat(returnType).isInstanceOf(ParameterizedType.class);
    ParameterizedType pt = (ParameterizedType) returnType;
    assertThat(pt.getRawType()).isEqualTo(ApiResult.class);
    assertThat(pt.getActualTypeArguments()[0]).isEqualTo(Void.class);
  }

  @Test
  @DisplayName("createOAuth2User 应接受 SysUserDTO 并返回 ApiResult<Void>")
  void createOAuth2UserSignature() throws NoSuchMethodException {
    Method method = RemoteUserService.class.getMethod("createOAuth2User", SysUserDTO.class);
    PostExchange annotation = method.getAnnotation(PostExchange.class);
    assertThat(annotation).isNotNull();
    assertThat(annotation.value()).isEqualTo("/oauth2");
  }
}
