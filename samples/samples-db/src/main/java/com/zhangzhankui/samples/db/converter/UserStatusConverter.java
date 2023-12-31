package com.zhangzhankui.samples.db.converter;

import com.zhangzhankui.samples.common.core.enums.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * UserStatus 枚举与数据库 Integer 类型的转换器
 * <p>
 * 使用 JPA AttributeConverter 实现枚举与数据库值的自动转换
 */
@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(UserStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public UserStatus convertToEntityAttribute(Integer dbData) {
        return UserStatus.fromValue(dbData);
    }
}
