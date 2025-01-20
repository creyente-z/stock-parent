package com.ming.stock.pojo.domain;

import com.ming.stock.pojo.entity.SysUser;
import lombok.Data;

/**
 * @Author: Ming
 * @Description TODO
 */
@Data
public class UserQueryDomain extends SysUser {
    private String createUserName;
    private String updateUserName;
}
