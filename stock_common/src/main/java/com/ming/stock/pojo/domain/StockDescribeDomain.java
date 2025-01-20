package com.ming.stock.pojo.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: Ming
 * @Description 个股主营业务查询
 */
@ApiModel(description = "个股主营业务查询")
@Data
public class StockDescribeDomain {
    @ApiModelProperty("股票编码")
    private String code;//股票编码
    @ApiModelProperty("行业，也就是行业板块名称")
    private String trade;//行业，也就是行业板块名称
    @ApiModelProperty("公司主营业务")
    private String business;//公司主营业务
    @ApiModelProperty("公司名称")
    private String name;//公司名称
}
