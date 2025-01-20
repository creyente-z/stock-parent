package com.ming.stock.vo.req;

import lombok.Data;

import java.util.List;

/**
 * @author by Ming
 * @Description 角色添加VO
 */
@Data
public class RoleAddVo {
    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色描述
     */
    private String description;

    /**
     * 权限ID集合
     */
    private List<String> permissionsIds;
}
