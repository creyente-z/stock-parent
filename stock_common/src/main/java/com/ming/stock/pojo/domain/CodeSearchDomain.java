package com.ming.stock.pojo.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Ming
 * @Description 股票Code联想推荐
 */
@ApiModel(description = "股票Code联想推荐")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeSearchDomain {
    @ApiModelProperty("股票编码")
    private String code; //股票编码
    @ApiModelProperty("股票名称")
    private String name; //股票名称
}
