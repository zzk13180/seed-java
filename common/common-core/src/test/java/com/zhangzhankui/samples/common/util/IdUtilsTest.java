package com.zhangzhankui.samples.common.util;

import com.zhangzhankui.samples.common.core.util.IdUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IdUtils 工具类测试
 * 
 * @author zhangzhankui
 */
@DisplayName("IdUtils ID生成工具测试")
class IdUtilsTest {

    @Test
    @DisplayName("生成UUID - 格式正确")
    void uuid_GeneratesValidUUID() {
        String uuid = IdUtils.uuid();
        
        assertThat(uuid).isNotNull();
        assertThat(uuid).hasSize(32); // 移除横线后32位
        assertThat(uuid).matches("[a-f0-9]{32}"); // 只包含小写字母和数字
    }

    @Test
    @DisplayName("生成UUID - 唯一性")
    void uuid_GeneratesUniqueValues() {
        Set<String> uuids = new HashSet<>();
        int count = 1000;
        
        for (int i = 0; i < count; i++) {
            uuids.add(IdUtils.uuid());
        }
        
        assertThat(uuids).hasSize(count);
    }

    @Test
    @DisplayName("生成UUID带连字符 - 格式正确")
    void uuidWithHyphen_GeneratesValidUUID() {
        String uuid = IdUtils.uuidWithHyphen();
        
        assertThat(uuid).isNotNull();
        assertThat(uuid).hasSize(36); // 带连字符的标准格式36位
        assertThat(uuid).matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
    }

    @Test
    @DisplayName("生成TraceId - 16位")
    void traceId_Generates16CharacterId() {
        String traceId = IdUtils.traceId();
        
        assertThat(traceId).isNotNull();
        assertThat(traceId).hasSize(16);
        assertThat(traceId).matches("[a-f0-9]{16}");
    }
}
