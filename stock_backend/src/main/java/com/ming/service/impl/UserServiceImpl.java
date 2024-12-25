package com.ming.service.impl;

import com.ming.service.UserService;
import com.ming.stock.mapper.SysUserMapper;
import com.ming.stock.pojo.entity.SysUser;
import com.ming.vo.req.LoginReqVo;
import com.ming.vo.resp.LoginRespVo;
import com.ming.vo.resp.R;
import com.ming.vo.resp.ResponseCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @Author: Ming
 * @Description 定义用户服务实现
 */
@Service("userService")
public class UserServiceImpl implements UserService {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 根据用户名称查询用户信息
     * @return
     */
    @Override
    public SysUser getUserByUserName(String userName) {
        SysUser user = sysUserMapper.findByUserName(userName);
        return user;
    }

    /**
     * 用户登录功能
     * @param vo
     * @return
     */
    @Override
    public R<LoginRespVo> login(LoginReqVo vo) {
        //判断参数是否合法
        if (vo == null || StringUtils.isBlank(vo.getUsername()) || StringUtils.isBlank(vo.getPassword()) || StringUtils.isBlank(vo.getCode())){
            return R.error(ResponseCode.DATA_ERROR);
        }
        //根据用户名去数据空查询用户信息 获取密码加密后的密文
        SysUser user = sysUserMapper.findByUserName(vo.getUsername());
        //判断用户是否存在
        if (user == null){
            //用户不存在
            return R.error(ResponseCode.ACCOUNT_NOT_EXISTS);
        }
        //调用密码匹配器输入明文密码和数据库的密文密码
        if (!passwordEncoder.matches(vo.getPassword(),user.getPassword())){
            return R.error(ResponseCode.USERNAME_OR_PASSWORD_ERROR);
        }
        //响应
        LoginRespVo respVo = new LoginRespVo();
//        respVo.setUsername(user.getUsername());
//        respVo.setUsername(user.getNickName());
//        respVo.setUsername(user.getPhone());
//        respVo.setUsername(user.getId());
        //LoginRespVo和SysUsUser 对象的属性名和类型一致
        BeanUtils.copyProperties(user,respVo);
        return R.ok(respVo);
    }
}
