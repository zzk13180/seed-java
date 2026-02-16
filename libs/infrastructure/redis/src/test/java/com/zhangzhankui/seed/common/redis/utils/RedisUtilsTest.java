package com.zhangzhankui.seed.common.redis.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

/**
 * RedisUtils 单元测试
 *
 * <p>验证 RedisUtils 正确委托给 RedisTemplate。
 * verify() 调用证明委托正确；去除 tautological return-value 断言。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RedisUtils 单元测试")
class RedisUtilsTest {

  @Mock private RedisTemplate<String, Object> redisTemplate;

  @Mock private ValueOperations<String, Object> valueOperations;

  @Mock private HashOperations<String, Object, Object> hashOperations;

  @Mock private ListOperations<String, Object> listOperations;

  @Mock private SetOperations<String, Object> setOperations;

  @InjectMocks private RedisUtils redisUtils;

  @BeforeEach
  void setUp() {
    lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    lenient().when(redisTemplate.opsForList()).thenReturn(listOperations);
    lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
  }

  @Nested
  @DisplayName("通用操作测试")
  class CommonOperationsTest {

    @Test
    @DisplayName("expire 应委托给 redisTemplate.expire")
    void shouldDelegateExpire() {
      given(redisTemplate.expire("testKey", 3600L, TimeUnit.SECONDS)).willReturn(true);

      redisUtils.expire("testKey", 3600L, TimeUnit.SECONDS);

      verify(redisTemplate).expire("testKey", 3600L, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("getExpire 应委托给 redisTemplate.getExpire")
    void shouldDelegateGetExpire() {
      given(redisTemplate.getExpire("testKey", TimeUnit.SECONDS)).willReturn(3600L);

      redisUtils.getExpire("testKey", TimeUnit.SECONDS);

      verify(redisTemplate).getExpire("testKey", TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("hasKey 应委托给 redisTemplate.hasKey")
    void shouldDelegateHasKey() {
      given(redisTemplate.hasKey("testKey")).willReturn(true);

      redisUtils.hasKey("testKey");

      verify(redisTemplate).hasKey("testKey");
    }

    @Test
    @DisplayName("delete(String) 应委托给 redisTemplate.delete")
    void shouldDelegateDeleteSingle() {
      given(redisTemplate.delete("testKey")).willReturn(true);

      redisUtils.delete("testKey");

      verify(redisTemplate).delete("testKey");
    }

    @Test
    @DisplayName("delete(Collection) 应委托给 redisTemplate.delete")
    void shouldDelegateDeleteMultiple() {
      List<String> keys = Arrays.asList("key1", "key2", "key3");

      redisUtils.delete(keys);

      verify(redisTemplate).delete(keys);
    }
  }

  @Nested
  @DisplayName("字符串操作测试")
  class StringOperationsTest {

    @Test
    @DisplayName("set 应委托给 valueOperations.set")
    void shouldDelegateSet() {
      redisUtils.set("testKey", "testValue");

      verify(valueOperations).set("testKey", "testValue");
    }

    @Test
    @DisplayName("set 带过期时间应委托给 valueOperations.set")
    void shouldDelegateSetWithExpire() {
      redisUtils.set("testKey", "testValue", 3600L, TimeUnit.SECONDS);

      verify(valueOperations).set("testKey", "testValue", 3600L, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("setIfAbsent 应委托给 valueOperations.setIfAbsent")
    void shouldDelegateSetIfAbsent() {
      given(valueOperations.setIfAbsent("testKey", "testValue", 3600L, TimeUnit.SECONDS))
          .willReturn(true);

      redisUtils.setIfAbsent("testKey", "testValue", 3600L, TimeUnit.SECONDS);

      verify(valueOperations).setIfAbsent("testKey", "testValue", 3600L, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("get 应委托给 valueOperations.get")
    void shouldDelegateGet() {
      given(valueOperations.get("testKey")).willReturn("testValue");

      redisUtils.get("testKey");

      verify(valueOperations).get("testKey");
    }

    @Test
    @DisplayName("increment 应委托给 valueOperations.increment")
    void shouldDelegateIncrement() {
      redisUtils.increment("counter");

      verify(valueOperations).increment("counter");
    }

    @Test
    @DisplayName("increment 带步长应委托给 valueOperations.increment")
    void shouldDelegateIncrementByStep() {
      redisUtils.increment("counter", 3L);

      verify(valueOperations).increment("counter", 3L);
    }

    @Test
    @DisplayName("decrement 应委托给 valueOperations.decrement")
    void shouldDelegateDecrement() {
      redisUtils.decrement("counter");

      verify(valueOperations).decrement("counter");
    }
  }

  @Nested
  @DisplayName("哈希操作测试")
  class HashOperationsTest {

    @Test
    @DisplayName("hSet 应委托给 hashOperations.put")
    void shouldDelegateHSet() {
      redisUtils.hSet("hashKey", "field", "value");

      verify(hashOperations).put("hashKey", "field", "value");
    }

    @Test
    @DisplayName("hGet 应委托给 hashOperations.get")
    void shouldDelegateHGet() {
      given(hashOperations.get("hashKey", "field")).willReturn("value");

      redisUtils.hGet("hashKey", "field");

      verify(hashOperations).get("hashKey", "field");
    }

    @Test
    @DisplayName("hSetAll 应委托给 hashOperations.putAll")
    void shouldDelegateHSetAll() {
      Map<String, Object> map = new HashMap<>();
      map.put("field1", "value1");
      map.put("field2", "value2");

      redisUtils.hSetAll("hashKey", map);

      verify(hashOperations).putAll("hashKey", map);
    }

    @Test
    @DisplayName("hGetAll 应委托给 hashOperations.entries")
    void shouldDelegateHGetAll() {
      Map<Object, Object> expectedMap = new HashMap<>();
      expectedMap.put("field1", "value1");
      given(hashOperations.entries("hashKey")).willReturn(expectedMap);

      redisUtils.hGetAll("hashKey");

      verify(hashOperations).entries("hashKey");
    }
  }

  @Nested
  @DisplayName("列表操作测试")
  class ListOperationsTest {

    @Test
    @DisplayName("lRightPush 应委托给 listOperations.rightPush")
    void shouldDelegateLRightPush() {
      redisUtils.lRightPush("listKey", "value");

      verify(listOperations).rightPush("listKey", "value");
    }

    @Test
    @DisplayName("lLeftPop 应委托给 listOperations.leftPop")
    void shouldDelegateLLeftPop() {
      redisUtils.lLeftPop("listKey");

      verify(listOperations).leftPop("listKey");
    }

    @Test
    @DisplayName("lSize 应委托给 listOperations.size")
    void shouldDelegateLSize() {
      redisUtils.lSize("listKey");

      verify(listOperations).size("listKey");
    }

    @Test
    @DisplayName("lRange 应委托给 listOperations.range")
    void shouldDelegateLRange() {
      redisUtils.lRange("listKey", 0, -1);

      verify(listOperations).range("listKey", 0, -1);
    }
  }

  @Nested
  @DisplayName("集合操作测试")
  class SetOperationsTest {

    @Test
    @DisplayName("sAdd 应委托给 setOperations.add")
    void shouldDelegateSAdd() {
      redisUtils.sAdd("setKey", "value1", "value2");

      verify(setOperations).add("setKey", "value1", "value2");
    }

    @Test
    @DisplayName("sIsMember 应委托给 setOperations.isMember")
    void shouldDelegateSIsMember() {
      given(setOperations.isMember("setKey", "value")).willReturn(true);

      redisUtils.sIsMember("setKey", "value");

      verify(setOperations).isMember("setKey", "value");
    }

    @Test
    @DisplayName("sMembers 应委托给 setOperations.members")
    void shouldDelegateSMembers() {
      redisUtils.sMembers("setKey");

      verify(setOperations).members("setKey");
    }

    @Test
    @DisplayName("sRemove 应委托给 setOperations.remove")
    void shouldDelegateSRemove() {
      redisUtils.sRemove("setKey", "v1", "v2");

      verify(setOperations).remove("setKey", "v1", "v2");
    }
  }

  @Nested
  @DisplayName("边界条件和异常测试")
  class BoundaryAndExceptionTests {

    @Test
    @DisplayName("set null 键应抛出异常")
    void shouldThrowExceptionWhenSettingNullKey() {
      assertThatThrownBy(() -> redisUtils.set(null, "value"))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("delete null 键列表应抛出异常")
    void shouldHandleNullKeysList() {
      assertThatThrownBy(() -> redisUtils.delete((Collection<String>) null))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("hSet null 键应抛出异常")
    void shouldHandleNullHashKey() {
      assertThatThrownBy(() -> redisUtils.hSet(null, "field", "value"))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("lRightPush null 键应抛出异常")
    void shouldHandleNullListKey() {
      assertThatThrownBy(() -> redisUtils.lRightPush(null, "value"))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("sAdd null 键应抛出异常")
    void shouldHandleNullSetKey() {
      assertThatThrownBy(() -> redisUtils.sAdd(null, "value"))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("sAdd null 值数组应抛出异常")
    void shouldHandleNullSetValues() {
      assertThatThrownBy(() -> redisUtils.sAdd("key", (Object[]) null))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("set 空字符串键应委托正常")
    void shouldHandleEmptyKey() {
      redisUtils.set("", "value");
      verify(valueOperations).set("", "value");
    }

    @Test
    @DisplayName("delete 空键列表应委托正常")
    void shouldHandleEmptyKeysList() {
      List<String> emptyList = List.of();
      redisUtils.delete(emptyList);
      verify(redisTemplate).delete(emptyList);
    }
  }
}
