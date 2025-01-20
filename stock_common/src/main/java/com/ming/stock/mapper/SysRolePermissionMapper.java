package com.ming.stock.mapper;

import com.ming.stock.pojo.entity.SysRolePermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
* @author 11519
* @description 针对表【sys_role_permission(角色权限表)】的数据库操作Mapper
* @createDate 2024-12-23 19:07:52
* @Entity com.ming.stock.pojo.entity.SysRolePermission
*/
public interface SysRolePermissionMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysRolePermission record);

    int insertSelective(SysRolePermission record);

    SysRolePermission selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysRolePermission record);

    int updateByPrimaryKey(SysRolePermission record);

    int insertPerms(@Param("perms") List<SysRolePermission> list);

    List<Long> getPermsByRoleId(@Param("roleId") String roleId);

    int deleteByRoleId(@Param("id") String id);

    /**
     * 根据权限id删除关联的角色信息
     * @param permissionId
     * @return
     */
    int deleteByPermissionId(@Param("permissionId") String permissionId);

    /**
     * 批量添加用户角色集合
     * @param rps
     * @return
     */
    int addRolePermissionBatch(@Param("rps") List<SysRolePermission> rps);

    /**
     * 根据角色id查询对应的权限id集合
     * @param roleId 角色id
     * @return
     */
    Set<String> getPermissionIdsByRoleId(@Param("roleId") Long roleId);
}
