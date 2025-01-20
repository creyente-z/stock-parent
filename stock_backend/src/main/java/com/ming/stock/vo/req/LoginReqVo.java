package com.ming.stock.vo.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Ming
 * @Description 登录请求vo
 */
@ApiModel(description = "登录请求vo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginReqVo {
    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String username;
    /**
     * 密码
     */
    @ApiModelProperty("密码")
    private String password;
    /**
     * 验证码
     */
    @ApiModelProperty("验证码")
    private String code;
    /**
     * 会话ID
     */
    @ApiModelProperty("会话ID")
    private String sessionId;
}
