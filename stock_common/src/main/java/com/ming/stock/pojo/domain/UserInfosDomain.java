package com.ming.stock.pojo.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfosDomain {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id; // 用户ID
    private String username; // 用户名称
    private String phone; // 手机号
    private String nickName; // 昵称
    private String realName; // 真实名称
    private int sex; // 性别
    private int status; // 状态
    private String email; // 邮件
    private String password;
    private int deleted;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateId;
    private int createWhere;
    @JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;
    @JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updateTime;
    private String createUserName;
    private String updateUserName;

}
