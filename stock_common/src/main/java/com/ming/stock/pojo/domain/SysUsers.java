package com.ming.stock.pojo.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户表
 * @TableName sys_user
 */
@Data
@ApiModel(description = "用户基本信息")
public class SysUsers implements Serializable {
    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private Long id;

    /**
     * 账户
     */
    @ApiModelProperty(value = "用户名")
    private String username;


    /**
     * 手机号码
     */
    @ApiModelProperty(value = "用户手机号")
    private String phone;

    /**
     * 昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String nickName;
    /**
     * 真实名称
     */
    @ApiModelProperty(value = "用户真实名字")
    private String realName;

    /**
     * 性别(1.男 2.女)
     */
    @ApiModelProperty(value = "用户性别")
    private Integer sex;

    /**
     * 账户状态(1.正常 2.锁定 )
     */
    @ApiModelProperty(value = "用户状态")
    private Integer status;

    /**
     * 邮箱(唯一)
     */
    @ApiModelProperty(value = "用户邮箱")
    private String email;

}
