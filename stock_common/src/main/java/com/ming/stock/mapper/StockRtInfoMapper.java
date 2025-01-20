package com.ming.stock.mapper;

import com.ming.stock.pojo.domain.*;
import com.ming.stock.pojo.entity.StockBlockRtInfo;
import com.ming.stock.pojo.entity.StockRtInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* @author 11519
* @description 针对表【stock_rt_info(个股详情信息表)】的数据库操作Mapper
* @createDate 2024-12-23 19:07:52
* @Entity com.ming.stock.pojo.entity.StockRtInfo
*/
public interface StockRtInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockRtInfo record);

    int insertSelective(StockRtInfo record);

    StockRtInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockRtInfo record);

    int updateByPrimaryKey(StockRtInfo record);

    List<StockUpdownDomain> getStockInfoByTime(@Param("curDate") Date curDate);
    List<StockUpdownDomain> findTopStocksByIncrease(@Param("curDate")Date curDate);
    List<Map> getStockUpdownCount(@Param("startDate") Date startDate,
                                  @Param("endDate") Date endDate,
                                  @Param("flag") int flag);


    List<Map> getIncreaseRangeInfoByDate(@Param("curDate") Date curDate);

    List<Stock4MinuteDomain> getStock4MinuteInfo(
            @Param("openDate") Date openDate,
            @Param("endDate") Date endDate,
            @Param("stockCode") String stockCode);

    List<Stock4EvrDayDomain> getStock4DkLine(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("stockCode") String stockCode);


    List<WeekklineDomain> getWeekkline(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("code") String code);

    /**
     * 批量插入个股数据
     * @param list 个股数据
     * @return
     */
    int insertBatch(@Param("list") List<StockRtInfo> list);



    List<Map<String, Object>> getByCodeInfo(String code);

    /**
     * 获取个股最新分时行情数据，主要包含：
     * 开盘价、前收盘价、最新价、最高价、最低价、成交金额和成交量、交易时间信息
     * @param code
     * @param curDate
     * @return
     */
    Stock4HourDetailsDomain getStockHourDetails(@Param("code") String code, @Param("curDate") Date curDate);

    List<Date> getMxTime4EvryDay(@Param("stockCode") String stockCode, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    List<Stock4EvrDayDomain> getStock4DkLine2(@Param("stockCode") String stockCode, @Param("mxTimes") List<Date> mxTimes);
}
