package com.ming.stock.mapper;

import com.ming.stock.pojo.domain.UserRoles;
import com.ming.stock.pojo.entity.SysRole;
import com.ming.stock.pojo.entity.SysUserRole;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

/**
* @author 11519
* @description 针对表【sys_user_role(用户角色表)】的数据库操作Mapper
* @createDate 2024-12-23 19:07:52
* @Entity com.ming.stock.pojo.entity.SysUserRole
*/
public interface SysUserRoleMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysUserRole record);

    int insertSelective(SysUserRole record);

    SysUserRole selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysUserRole record);

    int updateByPrimaryKey(SysUserRole record);

    List<Long> getRole(@Param("id") Long id);

    ArrayList<SysRole> getAllRole();


    int delUser(@Param("rolesList") ArrayList rolesList);


    void deleteByUserId(UserRoles userRoles);


    void insertUserRole(@Param("roleId") String roleId, @Param("userId") String userId);
    /**
     * 根据用户id删除关联的角色
     * @param userId
     * @return
     */
    int deleteByUserId(@Param("userId") String userId);
    /**
     * 批量插入信息
     * @param list
     * @return
     */
    int insertBatch(@Param("urs") List<SysUserRole> list);


}
