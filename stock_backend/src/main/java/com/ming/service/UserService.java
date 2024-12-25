package com.ming.service;

import com.ming.stock.pojo.entity.SysUser;
import com.ming.vo.req.LoginReqVo;
import com.ming.vo.resp.LoginRespVo;
import com.ming.vo.resp.R;

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
}
