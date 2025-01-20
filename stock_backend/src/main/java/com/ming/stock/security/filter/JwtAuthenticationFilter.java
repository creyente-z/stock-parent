package com.ming.stock.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.stock.security.util.JwtTokenUtil;
import com.ming.stock.vo.resp.R;
import com.ming.stock.vo.resp.ResponseCode;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Ming
 * @Description 定义授权过滤器 本质就是一切的请求 获取请求头的token 然后进行校验
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    /**
     * 过滤执行方法
     * @param request
     * @param response
     * @param filterChain 过滤器链
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //1.获取http请求中携带的jwt票据字符串(注意:如果用户尚未认证 则jwt票据自负床不存在)
        String jwtToken = request.getHeader(JwtTokenUtil.TOKEN_HEADER);
        //2.判断请求中的票据是否存在(合法性判断)
        if (StringUtils.isBlank(jwtToken)){
            filterChain.doFilter(request,response);
            return;
        }
        //3.校验票据是否合法
        Claims claims = JwtTokenUtil.checkJWT(jwtToken);
        //票据失效
        if (claims==null){
            //票据失效则提示则前端票据已失效 需要重新认证 过滤器链终止
            R<Object> error = R.error(ResponseCode.INVALID_TOKEN);
            //响应
            response.getWriter().write(new ObjectMapper().writeValueAsString(error));
            return;
        }
        //4.从合法的票据中获取用户名和权限信息 并组装UsernamePasswordAuthenticationToken对象
        //用户名
        String username = JwtTokenUtil.getUsername(jwtToken);
        //权限信息["P5",ROLE_ADMIN"]
        String roles = JwtTokenUtil.getUserRole(jwtToken);
        //将组合成的字符串转化为权限对象集合
        String comStr = StringUtils.strip(roles, "[]");
        List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(comStr);
        //5.组装认证成功的票据对象(认证成功时 密码位置null)
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        //6.将封装的认证票据存入security的安全上下文 这样后续的过滤器直接从安全上下文中获取相关的权限信息
        //以线程为维度 当前访问结束 那么线程回收 上下文凭证也会被回收
        SecurityContextHolder.getContext().setAuthentication(token);
        //7.发送请求后 执行的过滤器 比如认证过滤器发现如果上下文中存在token对象的话 无序认证
        filterChain.doFilter(request,response);

    }
}
