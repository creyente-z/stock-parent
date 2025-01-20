package com.ming.stock.vo.req;

import lombok.Data;

import java.util.List;

@Data
public class UserOwnRoleReqVo {

    private String userId;

    private List<String> roleIds;
}