package com.ming.stock.vo.resp;

import lombok.Data;

/**
 * @author by Ming
 * @Description 角色列表查询参数封装
 */
@Data
public class RolePageReqVo {
    /**
     * 当前页
     */
    private Integer pageNum=1;
    /**
     * 每页大小
     */
    private Integer pageSize=10;
}
