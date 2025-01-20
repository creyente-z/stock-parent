package com.ming.stock.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.stock.constant.StockConstant;
import com.ming.stock.security.user.LoginUserDetail;
import com.ming.stock.security.util.JwtTokenUtil;
import com.ming.stock.vo.req.LoginReqVo;
import com.ming.stock.vo.resp.LoginRespVo;
import com.ming.stock.vo.resp.R;
import com.ming.stock.vo.resp.ResponseCode;
import org.apache.commons.collections4.BagUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Ming
 * @Description 认证过滤器 核心作用:认证用户信息 并颁发jwt票据
 */
public class JwtLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 自定义构造器 传入登录认证的url地址
     * @param
     */
    public JwtLoginAuthenticationFilter(String loginUrl) {
        super(loginUrl);
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试去认证的方法
     * @param request from which to extract parameters and perform the authentication
     * @param response the response, which may be needed if the implementation has to do a
     * redirect as part of a multi-stage authentication process (such as OpenID).
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        // 请求必须是post请求
        if (!request.getMethod().equalsIgnoreCase("POST")
                || !(MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(request.getContentType())
                || MediaType.APPLICATION_JSON_UTF8_VALUE.equalsIgnoreCase(request.getContentType()))) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        //获取post请求ajax传入的数据流
        LoginReqVo reqVo = new ObjectMapper().readValue(request.getInputStream(), LoginReqVo.class);

        //校验给出的验证码是否正确
        //判断redis中保存的验证码与输入的验证码是否相同 (比较的时候忽略大小写)
        String redisCode = (String) redisTemplate.opsForValue().get(StockConstant.CHECK_PREFIX +reqVo.getSessionId());
        //响应数据格式json
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        if (redisCode==null || ! redisCode.equalsIgnoreCase(reqVo.getCode())) {
            //验证码输入错误
            R<Object> error = R.error(ResponseCode.CHECK_CODE_ERROR.getMessage());
            String jsonData = new ObjectMapper().writeValueAsString(error);
            //给前端响应错误的提示数据
            response.getWriter().write(jsonData);
            return null;
        }
        String username = reqVo.getUsername();
        username = (username != null) ? username : "";
        username = username.trim();
        String password = reqVo.getPassword();
        password = (password != null) ? password : "";
        //组装认证的票据对象
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        //交给认证管理器认证票据对象
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * 认证成功后执行的方法
     * @param request
     * @param response
     * @param chain   过滤器链
     * @param authResult 认证对象信息
     * method.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        LoginUserDetail principal = (LoginUserDetail) authResult.getPrincipal();
        String username = principal.getUsername();
        Collection<GrantedAuthority> authorities = principal.getAuthorities();//权限
        //生成票据["P5","ROLE_ADMIN"]
        String tokenStr = JwtTokenUtil.createToken(username, authorities.toString());

        //响应数据格式json
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        //编码格式
        request.setCharacterEncoding("UTF-8");
        //构建响应实体对象
        LoginRespVo respVo = new LoginRespVo();
        BeanUtils.copyProperties(principal,respVo);
        respVo.setAccessToken(tokenStr);
        R<LoginRespVo> ok = R.ok(respVo);
        //响应
        response.getWriter().write(new ObjectMapper().writeValueAsString(ok));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        //响应数据格式json
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        //编码格式
        response.setCharacterEncoding("UTF-8");
        R<Object> error = R.error(ResponseCode.ERROR);
        //响应
        response.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }
}
