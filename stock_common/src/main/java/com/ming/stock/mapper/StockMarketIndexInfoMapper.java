package com.ming.stock.mapper;

import com.ming.stock.pojo.domain.InnerMarketDomain;
import com.ming.stock.pojo.entity.StockMarketIndexInfo;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* @author 11519
* @description 针对表【stock_market_index_info(国内大盘数据详情表)】的数据库操作Mapper
* @createDate 2024-12-23 19:07:52
* @Entity com.ming.stock.pojo.entity.StockMarketIndexInfo
*/
public interface StockMarketIndexInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockMarketIndexInfo record);

    int insertSelective(StockMarketIndexInfo record);

    StockMarketIndexInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockMarketIndexInfo record);

    int updateByPrimaryKey(StockMarketIndexInfo record);


    List<InnerMarketDomain> getMarketInfo(@Param("curDate") Date curDate, @Param("marketCodes") List<String> marketCodes);

    /**
     * 统计指定日期范围内 指定大盘每分钟的成交量流水信息
     * @param tStartDate 起始时间 一般指开盘时间
     * @param tEndDate  截止时间 一般与tStartDate同一天
     * @param inner     大盘编码集合
     * @return
     */
    List<Map> getSumAmtInfo(
            @Param("tStartDate") Date tStartDate,
            @Param("tEndDate") Date tEndDate,
            @Param("inner") List<String> inner);

    /**
     * 批量插入大盘数据
     * @param entities 大盘实体对象集合
     * @return
     */
    int insertBatch(@Param("infos") ArrayList<StockMarketIndexInfo> entities);
}
