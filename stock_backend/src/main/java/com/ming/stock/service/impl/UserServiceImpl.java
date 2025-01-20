package com.ming.stock.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import com.ming.stock.exception.BusinessException;
import com.ming.stock.mapper.SysUserRoleMapper;
import com.ming.stock.pojo.domain.*;
import com.ming.stock.pojo.entity.SysPermission;
import com.ming.stock.pojo.entity.SysRole;
import com.ming.stock.pojo.entity.SysUserRole;
import com.ming.stock.service.PermissionService;
import com.ming.stock.service.UserService;
import com.ming.stock.constant.StockConstant;
import com.ming.stock.mapper.SysUserMapper;
import com.ming.stock.pojo.entity.SysUser;
import com.ming.stock.utils.IdWorker;
import com.ming.stock.vo.req.*;
import com.ming.stock.vo.resp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: Ming
 * @Description 定义用户服务实现
 */
@Service("userService")
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private PermissionService permissionService;

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
    /**
     * 用户登录功能
     * @param vo
     * @return
     */
    @Override
    public R<LoginRespVo> login(LoginReqVo vo) {
        //判断参数是否合法
        if (vo == null || Strings.isNullOrEmpty(vo.getUsername())
                || Strings.isNullOrEmpty(vo.getPassword())
                || Strings.isNullOrEmpty(vo.getSessionId())
                || Strings.isNullOrEmpty(vo.getCode())) {
            return R.error(ResponseCode.USERNAME_OR_PASSWORD_ERROR.getMessage());
        }
        //从程序执行的效率看，先进行校验码的校验，成本较低
        //补充：根据传入的rkye 从redis中获取校验码
        //校验验证码和sessionId是否有效
        String rCheckCode = (String) redisTemplate.opsForValue().get(StockConstant.CHECK_PREFIX +vo.getSessionId());
        if (rCheckCode==null || ! rCheckCode.equalsIgnoreCase(vo.getCode())) {
            //响应验证码输入错误
            return R.error(ResponseCode.CHECK_CODE_ERROR.getMessage());
        }
        //是否需要添加手动淘汰redis缓存的数据，如果想快速淘汰，则可手动删除
        redisTemplate.delete("CK:" + vo.getSessionId());
        //2.根据用户名查询用户信息
        SysUser user = sysUserMapper.findByUserName(vo.getUsername());
        //3.判断用户是否存在
        if (user == null) {
            //用户不存在
            return R.error(ResponseCode.ACCOUNT_NOT_EXISTS.getMessage());
        }
        //4.调用密码匹配器匹配输入的明文密码和数据库的密文密码
        boolean isSuccess = passwordEncoder.matches(vo.getPassword(), user.getPassword());
        //4.1如果密码匹配不成功
        if (! isSuccess){
            return R.error(ResponseCode.ACCOUNT_NOT_EXISTS.getMessage());
        }
        //4.2成功则返回用户的正常信息
        LoginRespVo respVo = new LoginRespVo();
//        respVo.setUsername(user.getUsername());
//        respVo.setNickName(user.getNickName());
//        respVo.setPhone(user.getPhone());
//        respVo.setId(user.getId());
        //发现LoginRespVo与SysUser对象属性名称和类型一致
        //属性名称与类型必须相同，否则属性值无法copy
        BeanUtils.copyProperties(user, respVo);
        //获取指定用户的权限集合 添加获取侧边样数据和按钮权限的结合信息
        List<SysPermission> permissions = permissionService.getPermissionByUserId(user.getId());
        //获取树状权限菜单数据
        List<PermissionRespNodeVo> tree = permissionService.getTree(permissions,"0",true);
        //获取菜单按钮集合
        List<String> authBthPerms = permissions.stream().filter(per -> !Strings.isNullOrEmpty(per.getCode()) && per.getType() == 3)
                .map(per -> per.getCode()).collect(Collectors.toList());
        respVo.setMenus(tree);
        respVo.setPermissions(authBthPerms);
        //后期 用jwt生成token
        respVo.setAccessToken(user.getId()+":"+user.getUsername());

        return R.ok(respVo);
    }
    /**
     * 生成验证码的功能
     * @return
     */
    @Override
    public R<Map> getCaptchaCode() {
        //生成图片验证码 Params:
        //width – 图片宽 height – 图片高 codeCount – 字符个数 lineCount – 干扰线条数
        LineCaptcha captcha =
                CaptchaUtil.createLineCaptcha(250, 40, 4, 5);
        //社背景颜色青灰色
        captcha.setBackground(Color.LIGHT_GRAY);
        //获取图片中的验证码 默认人省的验证码包含字母数字 长度为4
        String checkCode = captcha.getCode();
        //获取经过base64编码处理的图片数据
        String imageData = captcha.getImageBase64();
        //生成sessionId转化成String 避免前端精度丢失
        String sessionId = String.valueOf(idWorker.nextId());
        log.info("当生成的图片校验码:{},会话id:{}",checkCode,sessionId);
        //将sessionId做为key 校验码作为value保存在redis下 (使用redis模拟session的行为 通过过期时间设置)
        redisTemplate.opsForValue().set(StockConstant.CHECK_PREFIX+sessionId,checkCode,5, TimeUnit.MINUTES);
        //组装响应数据
        HashMap<String,String> data = new HashMap<>();
        data.put("imageData",imageData);
        data.put("sessionId",sessionId);
        //响应
        return R.ok(data);
    }
    @Override
    public R<PageResult> getUserInfos(UserPageDomain userPageDomain) {
        //组装分页数据
        PageHelper.startPage(userPageDomain.getPageNum(),userPageDomain.getPageSize());
        //设置查询条件
        List<UserQueryDomain> users= sysUserMapper.pageUsers(userPageDomain.getUsername(),userPageDomain.getNickName(),userPageDomain.getStartTime(),userPageDomain.getEndTime());
        if (CollectionUtils.isEmpty(users)) {
            return R.error("没有数据");
        }
        PageResult<UserQueryDomain> pageResult = new PageResult<>(new PageInfo<>(users));
        return R.ok(pageResult);

    }

    @Override
    public R<String> addUserInfos(UserInfosDomain userInfosDomain) {
        //1.判断当前账户username是否已被使用
        SysUser dbUser= sysUserMapper.findUserByUserName(userInfosDomain.getUsername());
        if (dbUser!=null) {
            //抛出业务异常 等待全局异常处理器统一处理
            throw new BusinessException(ResponseCode.ACCOUNT_EXISTS_ERROR.getMessage());
        }
        //2.否则添加
        //封装用户信息
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userInfosDomain,user);
        //设置用户id
        user.setId(idWorker.nextId()+"");
        //密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        //设置添加时间和更新时间
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        //是否删除
        user.setDeleted(1);
        //TODO 获取当前操作用户的id
        int count = this.sysUserMapper.insert(user);
        if (count!=1) {
            throw new BusinessException(ResponseCode.ERROR.getMessage());
        }
        return R.ok(ResponseCode.SUCCESS.getMessage());
    }

    /**
     * 更新用户角色信息
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> updateUserOwnRoles(UserOwnRoleReqVo vo) {
        //1.判断用户id是否存在
        if (vo.getUserId()==null) {
            throw new BusinessException(ResponseCode.DATA_ERROR.getMessage());
        }
        //2.删除用户原来所拥有的角色id
        sysUserRoleMapper.deleteByUserId(vo.getUserId());
        //如果对应集合为空，则说明用户将所有角色都清除了
        if (CollectionUtils.isEmpty(vo.getRoleIds())) {
            return R.ok(ResponseCode.SUCCESS.getMessage());
        }
        //封装用户角色对象集合

        List<SysUserRole> list = vo.getRoleIds().stream().map(roleId -> {
            SysUserRole userRole = SysUserRole.builder().
                    userId(vo.getUserId()).roleId(roleId).
                    createTime(new Date()).id(idWorker.nextId()+"").build();
            return userRole;
        }).collect(Collectors.toList());
        //批量插入
        int count= sysUserRoleMapper.insertBatch(list);
        if (count==0) {
            throw new BusinessException(ResponseCode.ERROR.getMessage());
        }
        return R.ok(ResponseCode.SUCCESS.getMessage());
    }


    @Override
    public R deleteUser(List<Long> userIds) {
        sysUserMapper.deleteUser(userIds);
        return R.ok();
    }

    @Override
    public R<SysUsers> getUserInfosById(String id) {
        SysUsers sysUsers = sysUserMapper.getUserInfosById(id);
        return R.ok(sysUsers);
    }

    @Override
    public R<HashMap<String, Object>> getUserMsg(String userId) {
        Long user_id = Long.valueOf(userId);

        // 查询用户数据
        HashMap<String,Object> user_data= sysUserMapper.getUserMsg(user_id);
        return R.ok(user_data);

    }

    @Override
    public R updateInfosById(SysUsers sysUsers) {
        Integer createWhere = 1;
        sysUserMapper.updateInfosById(sysUsers,createWhere);
        return R.ok();
    }


    @Override
    public R<Map<String, Object>> getRole(String userId) {
        Long id = Long.valueOf(userId);
        // 查询获得用户角色
        List<Long> userRoleList =  sysUserRoleMapper.getRole(id);

        // 查询获得所有角色信息
        ArrayList<SysRole> allRoleList = sysUserRoleMapper.getAllRole();

        // 封装数据
        HashMap<String, Object> data = new HashMap<>();

        data.put("ownRoleIds" , userRoleList);
        data.put("allRole",allRoleList);

        return R.ok(data);
    }


}
