package com.zhangzhankui.samples.service.converter;

import com.zhangzhankui.samples.api.dto.UserCreateDTO;
import com.zhangzhankui.samples.api.dto.UserUpdateDTO;
import com.zhangzhankui.samples.api.vo.UserVO;
import com.zhangzhankui.samples.common.core.enums.UserStatus;
import com.zhangzhankui.samples.db.entity.User;
import org.mapstruct.*;

import java.util.List;

/**
 * 用户对象转换器
 */
@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserConverter {

    /**
     * Entity -> VO
     * <p>
     * UserStatus 枚举会通过 @JsonValue 自动序列化为 Integer
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToInteger")
    UserVO toVO(User entity);

    /**
     * Entity List -> VO List
     */
    List<UserVO> toVOList(List<User> entities);

    /**
     * CreateDTO -> Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "status", expression = "java(com.zhangzhankui.samples.common.core.enums.UserStatus.ENABLED)")
    @Mapping(target = "avatar", ignore = true)
    User toEntity(UserCreateDTO dto);

    /**
     * UpdateDTO -> Entity (更新非空字段)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(UserUpdateDTO dto, @MappingTarget User entity);

    /**
     * UserStatus 枚举转 Integer
     */
    @Named("statusToInteger")
    default Integer statusToInteger(UserStatus status) {
        return status != null ? status.getValue() : null;
    }
}
