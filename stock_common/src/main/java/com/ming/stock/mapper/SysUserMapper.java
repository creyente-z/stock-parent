package com.ming.stock.mapper;

import com.ming.stock.pojo.domain.SysUsers;
import com.ming.stock.pojo.domain.UserInfos;
import com.ming.stock.pojo.domain.UserInfosDomain;
import com.ming.stock.pojo.domain.UserQueryDomain;
import com.ming.stock.pojo.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
* @author 11519
* @description 针对表【sys_user(用户表)】的数据库操作Mapper
* @createDate 2024-12-23 19:07:52
* @Entity com.ming.stock.pojo.entity.SysUser
*/
public interface SysUserMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysUser record);

    int insertSelective(SysUser record);

    SysUser selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysUser record);

    int updateByPrimaryKey(SysUser record);

    SysUser findByUserName(@Param("userName") String userName);
    /**
     * 查询所有用户信息
     */
    List<SysUser> findAll();

    List<UserQueryDomain> pageUsers(@Param("userName") String userName, @Param("nickName") String nickName,
                                    @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 根据账户名称查询账户信息
     * @param username 账户名称
     * @return
     */
    SysUser findUserByUserName(@Param("userName")String username);

    void deleteUser(@Param("userIds") List<Long> userIds);

    SysUsers getUserInfosById(@Param("id") String id);

    HashMap<String, Object> getUserMsg(@Param("userId") Long userId);

    void updateInfosById(@Param("aa") SysUsers sysUsers,@Param("c") Integer createWhere);



}
