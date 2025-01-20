package com.ming.stock.security.service;

import com.google.common.base.Strings;
import com.ming.stock.mapper.SysRoleMapper;
import com.ming.stock.mapper.SysUserMapper;
import com.ming.stock.pojo.entity.SysPermission;
import com.ming.stock.pojo.entity.SysRole;
import com.ming.stock.pojo.entity.SysUser;
import com.ming.stock.security.user.LoginUserDetail;
import com.ming.stock.service.PermissionService;
import com.ming.stock.service.RoleService;
import com.ming.stock.vo.resp.PermissionRespNodeVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Ming
 * @Description 定义用户认证的时候获取认证用户的详情信息的服务bean
 */
@Service("userDetailsService")
public class LoginUserDetailService implements UserDetailsService {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private SysRoleMapper sysRoleMapper;

    /**
     * 根据认证时传入用户名去指定资源库下获取用户相关的详情信息:用户名 密文密码  权限对象集合
     * @param username the username identifying the user whose data is required.
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //1.根据用户名去数据库获取用户基本信息
        SysUser dbUser = sysUserMapper.findByUserName(username);
        //1.1判断查询的用户信息
        if (dbUser==null){
            throw new UsernameNotFoundException("当前用户存在");
        }

        //2组装UserDetail对象
        //2.1 用户名 密文密码 权限集合
        //2.2其他字段:id email phone menus permissions
        //获取指定用户的权限集合 添加获取侧边样数据和按钮权限的结合信息
        List<SysPermission> permissions = permissionService.getPermissionByUserId(dbUser.getId());
        List<SysRole> roles = sysRoleMapper.getRoleByUserId(dbUser.getId());
        //获取树状权限菜单数据
        List<PermissionRespNodeVo> menus = permissionService.getTree(permissions,"0",true);
        //获取菜单按钮集合
        List<String> authBthPerms = permissions.stream().filter(per -> !Strings.isNullOrEmpty(per.getCode()) && per.getType() == 3)
                .map(per -> per.getCode()).collect(Collectors.toList());
        //获取SpringSecurity的权限标识 ROLE_角色名称+权限自身标识
        ArrayList<String> ps = new ArrayList<>();
        List<String> pers = permissions.stream()
                .filter(per-> StringUtils.isNotBlank(per.getPerms()))
                .map(per->per.getPerms())
                .collect(Collectors.toList());
        ps.addAll(pers);
        List<String> rs = roles.stream().map(r -> "ROLE_" + r.getName()).collect(Collectors.toList());
        ps.addAll(rs);
        //将用户的权限标识表示为权限对象
        String[] psArray = ps.toArray(new String[pers.size()]);
        List<GrantedAuthority> authorityList = AuthorityUtils.createAuthorityList(psArray);
        //构建用户的详情服务对象
        LoginUserDetail userDetail = new LoginUserDetail();
        BeanUtils.copyProperties(dbUser,userDetail);
        userDetail.setMenus(menus);
        userDetail.setPermissions(authBthPerms);
        userDetail.setAuthorities(authorityList);
        return userDetail;
    }


}
