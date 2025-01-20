package com.ming.stock.service;

import com.ming.stock.pojo.domain.*;
import com.ming.stock.pojo.entity.SysUser;
import com.ming.stock.vo.req.*;
import com.ming.stock.vo.resp.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Ming
 * @Description 定义用户服务接口
 */
public interface UserService {
    /**
     * 根据用户名称查询用户信息
     * @return
     */
    SysUser getUserByUserName(String userName);

    /**
     * 用户登录功能
     * @param vo
     * @return
     */
    R<LoginRespVo> login(LoginReqVo vo);
    /**
     * 生成验证码的功能
     * @return
     */
    R<Map> getCaptchaCode();

    /**
     * 多条件综合查询用户分页信息，条件包含：分页信息 用户创建日期范围
     * @param userPageDomain
     * @return
     */
    R<PageResult> getUserInfos(UserPageDomain userPageDomain);


    R<Map<String, Object>> getRole(String userId);

    R<String> addUserInfos(UserInfosDomain userInfosDomain);
    /**
     * 更新用户角色信息
     * @param vo
     * @return
     */
    R<String> updateUserOwnRoles(UserOwnRoleReqVo vo);
    R deleteUser(List<Long> userIds);

    R<SysUsers> getUserInfosById(String id);

    R<HashMap<String, Object>> getUserMsg(String userId);

    R updateInfosById(SysUsers sysUsers);



}
