package com.zhangzhankui.seed.system.converter;

import com.zhangzhankui.seed.system.api.dto.SysUserDTO;
import com.zhangzhankui.seed.system.api.vo.SysUserVO;
import com.zhangzhankui.seed.system.domain.SysDept;
import com.zhangzhankui.seed.system.domain.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 用户对象转换器 - 使用 MapStruct 在编译期自动生成字段映射代码。
 */
@Mapper
public interface SysUserConverter {

  SysUserConverter INSTANCE = Mappers.getMapper(SysUserConverter.class);

  /**
   * DTO 转 Entity（新增用户时使用）。
   *
   * <p>自动映射同名字段（username、nickname、email 等），忽略 password 及审计字段，
   * 这些字段需在业务层单独赋值。
   *
   * @param dto 用户 DTO
   * @return 用户实体（不含密码，密码需单独加密处理）
   */
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "createBy", ignore = true)
  @Mapping(target = "createTime", ignore = true)
  @Mapping(target = "updateBy", ignore = true)
  @Mapping(target = "updateTime", ignore = true)
  @Mapping(target = "tenantId", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(target = "dataScopeSql", ignore = true)
  @Mapping(target = "params", ignore = true)
  SysUser toEntity(SysUserDTO dto);

  /**
   * Entity 转 VO（查询返回时使用）。
   *
   * <p>手机号通过 {@code DesensitizationUtils.desensitizePhone} 脱敏（例: 138****5678），
   * deptName 置为 null。
   *
   * @param entity 用户实体
   * @return 用户 VO（不含敏感信息）
   */
  @Mapping(target = "deptName", ignore = true)
  @Mapping(
      target = "phone",
      expression =
          "java(com.zhangzhankui.seed.common.core.utils.DesensitizationUtils"
              + ".desensitizePhone(entity.getPhone()))")
  SysUserVO toVO(SysUser entity);

  /**
   * Entity 转 VO（包含部门名称）。
   *
   * <p>两个参数存在同名字段（如 phone、email），因此需显式指定 source 以消除歧义。
   * 手机号同样会脱敏处理，deptName 取自 {@code dept.deptName}。
   *
   * @param entity 用户实体
   * @param dept 部门实体（可为 null）
   * @return 用户 VO
   */
  @Mapping(target = "deptName", source = "dept.deptName")
  @Mapping(target = "userId", source = "entity.userId")
  @Mapping(target = "username", source = "entity.username")
  @Mapping(target = "nickname", source = "entity.nickname")
  @Mapping(target = "email", source = "entity.email")
  @Mapping(
      target = "phone",
      expression =
          "java(com.zhangzhankui.seed.common.core.utils.DesensitizationUtils"
              + ".desensitizePhone(entity.getPhone()))")
  @Mapping(target = "sex", source = "entity.sex")
  @Mapping(target = "avatar", source = "entity.avatar")
  @Mapping(target = "deptId", source = "entity.deptId")
  @Mapping(target = "status", source = "entity.status")
  @Mapping(target = "remark", source = "entity.remark")
  @Mapping(target = "createTime", source = "entity.createTime")
  @Mapping(target = "updateTime", source = "entity.updateTime")
  SysUserVO toVO(SysUser entity, SysDept dept);
}
