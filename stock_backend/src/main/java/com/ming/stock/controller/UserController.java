package com.ming.stock.controller;

import com.ming.stock.pojo.domain.*;
import com.ming.stock.service.UserService;
import com.ming.stock.pojo.entity.SysUser;
import com.ming.stock.vo.req.*;
import com.ming.stock.vo.resp.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Ming
 * @Description 定义用户服务接口
 */
@RestController
@RequestMapping("/api")
@Api(tags = "用户相关接口处理器")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 根据用户名查询用户信息
     *
     * @param userName
     * @return
     */
    @ApiOperation(value = "根据用户名查询用户信息")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "name",
                    value = "用户名", dataType = "string", required = true, type = "path")})
    @GetMapping("/user/{userName}")
    public SysUser getUserByUserName(@PathVariable("userName") String userName) {
        return userService.getUserByUserName(userName);
    }

    /**
     * 用户登录功能
     *
     * @param vo
     * @return
     */
//    @ApiOperation(value = "用户登录功能")
//    @PostMapping("/login")
//    public R<LoginRespVo> login(@RequestBody LoginReqVo vo) {
//        return userService.login(vo);
//    }

    /**
     * 生成验证码的功能
     *
     * @return
     */
    @ApiOperation(value = "生成验证码的功能")
    @GetMapping("/captcha")
    public R<Map> getCaptchaCode() {
        return userService.getCaptchaCode();

    }

    //多条件综合查询用户分页信息

    @ApiOperation(value = "多条件综合查询用户分页信息", notes = "多条件综合查询用户分页信息", httpMethod = "POST")
    @PostMapping("/users")
    public R<PageResult> getUserInfos(@RequestBody UserPageDomain userPageDomain) {
        return userService.getUserInfos(userPageDomain);
    }

    //添加用户信息
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "UserInfosDomain", name = "userInfosDomain", value = "", required = true)
    })
    @ApiOperation(value = "添加用户信息", notes = "添加用户信息", httpMethod = "POST")
    @PostMapping("/user")
    public R<String> addUserInfos(@RequestBody UserInfosDomain userInfosDomain) {
        return userService.addUserInfos(userInfosDomain);
    }

    /**
     * 获取用户具有的角色信息
     */
    @ApiOperation(value = "获取用户具有的角色信息")
    @GetMapping("/user/roles/{userId}")
    public R<Map<String, Object>> getRole(@PathVariable String userId) {
        return userService.getRole(userId);
    }


    /**
     * 更新用户角色信息
     * @param vo
     * @return
     */
    @PutMapping("/user/roles")
    public R<String> updateUserOwnRoles(@RequestBody UserOwnRoleReqVo vo){
        return userService.updateUserOwnRoles(vo);
    }

    //批量删除用户信息
    @DeleteMapping("/user")
    public R deleteUser(@RequestBody List<Long> userIds) {
        return userService.deleteUser(userIds);
    }

    /**
     * 根据用户id查询用户信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/user/info/{userId}")
    public R<HashMap<String, Object>> getUserMsg(@PathVariable String userId) {
        return userService.getUserMsg(userId);

    }

    //根据id更新用户基本信息
    @PutMapping("/user")
    public R updateInfosById(@RequestBody SysUsers sysUsers) {
        return userService.updateInfosById(sysUsers);
    }


}
