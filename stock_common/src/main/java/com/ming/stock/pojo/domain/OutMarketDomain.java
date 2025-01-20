package com.ming.stock.pojo.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: Ming
 * @Description 定义外盘的领域对象
 */
@ApiModel(description = "定义外盘的领域对象")
@Data
public class OutMarketDomain {

    @ApiModelProperty("大盘名称")
    private String name; //大盘名称
    @ApiModelProperty("当前大盘点")
    private BigDecimal curPoint;//当前大盘点
    @ApiModelProperty("涨跌值")
    private BigDecimal upDown;//涨跌值
    @ApiModelProperty("涨幅")
    private BigDecimal rose;  //涨幅
    @ApiModelProperty("当前时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date curTime;//当前时间


}
