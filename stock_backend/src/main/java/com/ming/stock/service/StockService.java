package com.ming.stock.service;

import com.ming.stock.pojo.domain.*;
import com.ming.stock.vo.resp.PageResult;
import com.ming.stock.vo.resp.R;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author: Ming
 * @Description TODO
 */
public interface StockService {
    /**
     * 获取国内大盘的最新数据
     * @return
     */
    R<List<InnerMarketDomain>> getInnerMarketInfo();
    /**
     * 需求说明: 获取沪深两市板块最新数据，以交易总金额降序查询，取前10条数据
     * @return
     */
    R<List<StockBlockDomain>> sectorAllLimit();
    /**
     * 分页查询股票最新数据 并按照涨幅排序查询
     * @param page  当前页
     * @param pageSize 每页大小
     * @return
     */
    R<PageResult<StockUpdownDomain>> getStockPageInfo(Integer page, Integer pageSize);
    /**
     * 统计沪深两市个股最新交易数据，并按涨幅降序排序查询前4条数据
     * @return
     */
    R<List<StockUpdownDomain>> getTopStocksByIncrease();
    /**
     * 统计最新股票交易日内每分钟涨跌停的股票数量
     * @return
     */
    R<Map<String, List>> getStockUpdownCount();


    void exportStockUpDownInfo(Integer page, Integer pageSize, HttpServletResponse response);
    /**
     * 功能描述:统计A股大盘T日和T-1日成交量对比功能（成交量为沪深两市成交量之和） 大盘每分钟成交量数据
     * @return
     */
    R<Map<String, List>> getComparedStockTradeAmt();
    /**
     * 统计最新交易时间点下的股票(A股) 在各个涨幅区间的数量
     * @return
     */
    R<Map> getIncreaseRangeInfo();
    /**
     * 功能描述:查询单个个股的分时行情数据 也就是统计股票T日每分钟的交易数据
     * 如果当前日期不在有效时间范围内 则以最近的一个股票交易时间作为查询时间点
     * 总结:获取指定股票T日分时数据
     * @param stockCode 股票编码
     * @return
     */
    R<List<Stock4MinuteDomain>> getStockScreenTimeSharing(String stockCode);
    /**
     * 单个个股日K 数据查询 可以根据区间查询数日的k线数据
     * @param stockCode 股票编码
     * @return
     */
    R<List<Stock4EvrDayDomain>> getStockScreenDkLine(String stockCode);
    R<List<OutMarketDomain>> getOutMarket();

    R<List<CodeSearchDomain>> stockCodeSearch(String searchStr);

    R<StockDescribeDomain> getStockDescribe(String code);

    /**
     * 功能描述：统计每周内的股票数据信息，信息包含：
     * 股票ID、 一周内最高价、 一周内最低价 、周1开盘价、周5的收盘价、
     * 整周均价、以及一周内最大交易日期（一般是周五所对应日期）
     *
     * @param code
     * @return
     */
    R<List<WeekklineDomain>> getWeekkline(String code);

    /**
     * 获取个股最新分时行情数据，主要包含：
     * 开盘价、前收盘价、最新价、最高价、最低价、成交金额和成交量、交易时间信息
     * @param code
     * @return
     */
    R<Stock4HourDetailsDomain> getStockHourDetails(String code);


    R<List<Map<String, Object>>> getStockTradinStatement(String code);



}
