package com.ming.stock.vo.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: Ming
 * @Description TODO
 */
@ApiModel
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRespVo {
    /**
     * 用户id
     * 将Long类型进行json转化的时候 转成String格式类型
     */
    @ApiModelProperty("用户id 将Long类型进行json转化的时候 转成String格式类型")
    @JsonSerialize
    private Long id;
    /**
     * 电话
     */
    @ApiModelProperty("电话")
    private String phone;
    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String username;
    /**
     * 昵称
     */
    @ApiModelProperty("昵称")
    private String nickName;
    /**
     * 菜单树
     */
    private List<PermissionRespNodeVo> menus;

    private List<String> permissions;

    /**
     * 认证成功后响应的jwtToken
     */
    private String accessToken;
}
