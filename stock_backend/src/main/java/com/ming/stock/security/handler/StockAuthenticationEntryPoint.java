package com.ming.stock.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.stock.vo.resp.R;
import com.ming.stock.vo.resp.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Ming
 * @Description 用户无权访问拒绝的处理器
 */
@Slf4j
public class StockAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /**
     * 没有认证的用户访问资源时拒绝处理
     * @param request that resulted in an <code>AuthenticationException</code>
     * @param response so that the user agent can begin authentication
     * @param authException that caused the invocation
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.warn("匿名用户访问资源拒绝 原因:{}",authException.getMessage());
        //响应数据格式json
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        //编码格式
        request.setCharacterEncoding("UTF-8");
        R<Object> error = R.error(ResponseCode.ANONMOUSE_NOT_PERMISSION);
        //响应
        response.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }
}
