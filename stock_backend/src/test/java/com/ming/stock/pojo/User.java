package com.ming.stock.pojo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: Ming
 * @Description TODO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@HeadRowHeight(value = 35) //表头行高
@ContentRowHeight(value = 25) //内容行高
@ColumnWidth(value = 50) //列宽
public class User implements Serializable {
    /**
     * 通过注解自定义表头名称 注解添加排序规则 值越大 越靠近右边
     */
    @ExcelProperty(value = {"用户基本信息","用户名"},index = 1)
    @ExcelIgnore
    private String userName;
    @ExcelProperty(value = {"用户基本信息","年龄"},index = 2)
    private Integer age;
    @ExcelProperty(value = {"用户基本信息","地址"},index = 4)
    private String address;
    @ExcelProperty(value = {"用户基本信息","生日"},index = 3)
    @DateTimeFormat("yyyy/MM/dd")
    private Date birthday;
}
