package com.ming.stock.mapper;

import com.ming.stock.pojo.domain.CodeSearchDomain;
import com.ming.stock.pojo.domain.StockDescribeDomain;
import com.ming.stock.pojo.entity.StockBusiness;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 11519
* @description 针对表【stock_business(主营业务表)】的数据库操作Mapper
* @createDate 2024-12-23 19:07:52
* @Entity com.ming.stock.pojo.entity.StockBusiness
*/
public interface StockBusinessMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockBusiness record);

    int insertSelective(StockBusiness record);

    StockBusiness selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockBusiness record);

    int updateByPrimaryKey(StockBusiness record);

    /**
     * 获取所有的A股编码集合
     * @return
     */
    List<String> getStockIds();

    /**
     * 根据股票编码模糊查询
     * @param
     * @return
     */
    List<CodeSearchDomain> getMsg(@Param("searchStr") String searchStr);
    /**
     * 根据参数查询个股主营业务
     * @param code
     * @return
     */
    StockDescribeDomain getStockDescribe(@Param("code") String code);





}
