package com.ming.stock.controller;

import com.ming.stock.pojo.domain.*;
import com.ming.stock.service.StockService;
import com.ming.stock.vo.resp.PageResult;
import com.ming.stock.vo.resp.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


@Api(value = "/api/quot", tags = {"TODO"})
@RestController
@RequestMapping("/api/quot")
public class StockController {
    @Autowired
    private StockService stockService;

    /**
     * 获取国内大盘的最新数据
     * @return
     */
    @ApiOperation(value = "获取国内大盘的最新数据", notes = "获取国内大盘的最新数据", httpMethod = "GET")
    @GetMapping("/index/all")
    public R<List<InnerMarketDomain>> getInnerMarketInfo(){
        return stockService.getInnerMarketInfo();
    }
    /**
     *需求说明: 获取沪深两市板块最新数据，以交易总金额降序查询，取前10条数据
     * @return
     */
    @ApiOperation(value = "需求说明: 获取沪深两市板块最新数据，以交易总金额降序查询，取前10条数据", notes = "需求说明: 获取沪深两市板块最新数据，以交易总金额降序查询，取前10条数据", httpMethod = "GET")
    @GetMapping("/sector/all")
    public R<List<StockBlockDomain>> sectorAll(){
        return stockService.sectorAllLimit();
    }

    /**
     * 分页查询股票最新数据 并按照涨幅排序查询
     * @param page  当前页
     * @param pageSize 每页大小
     * @return
     */

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page", value = "当前页"),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize", value = "每页大小")
    })
    @ApiOperation(value = "分页查询股票最新数据 并按照涨幅排序查询", notes = "分页查询股票最新数据 并按照涨幅排序查询", httpMethod = "GET")
    @GetMapping("/stock/all")
    public R<PageResult<StockUpdownDomain>> getStockPageInfo(
            @RequestParam(name = "page",required = false,defaultValue = "1") Integer page,
            @RequestParam(name = "pageSize",required = false,defaultValue = "20") Integer pageSize){
        return stockService.getStockPageInfo(page,pageSize);
    }

    /**
     * 统计沪深两市个股最新交易数据，并按涨幅降序排序查询前4条数据
     * @return
     */
    @ApiOperation(value = "统计沪深两市个股最新交易数据，并按涨幅降序排序查询前4条数据", notes = "无请求参数", httpMethod = "GET")
    @GetMapping("/stock/increase")
    @ResponseBody
    public R<List<StockUpdownDomain>> getTopStocksByIncrease() {
        return stockService.getTopStocksByIncrease();
    }

    /**
     * 统计最新股票交易日内每分钟涨跌停的股票数量
     * @return
     */
    @ApiOperation(value = "统计最新股票交易日内每分钟涨跌停的股票数量", notes = "统计最新股票交易日内每分钟涨跌停的股票数量", httpMethod = "GET")
    @GetMapping("/stock/updown/count")
    public R<Map<String,List>> getStockUpdownCount(){
        return stockService.getStockUpdownCount();
    }

    /**
     * 将指定页的股票数据导出到excel表下
     * @param page 当前页
     * @param pageSize 每页大小
     * @param response
     */
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page", value = "当前页"),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize", value = "每页大小")
    })
    @ApiOperation(value = "将指定页的股票数据导出到excel表下", notes = "将指定页的股票数据导出到excel表下", httpMethod = "GET")
    @GetMapping("/stock/export")
    public void exportStockUpDownInfo(
            @RequestParam(name="page",required = false,defaultValue = "1") Integer page,
            @RequestParam(name = "pageSize",required = false,defaultValue = "20") Integer pageSize,
            HttpServletResponse response){
        stockService.exportStockUpDownInfo(page,pageSize,response);

    }

    /**
     * 功能描述:统计A股大盘T日和T-1日成交量对比功能（成交量为沪深两市成交量之和） 大盘每分钟成交量数据
     * @return
     */
    @ApiOperation(value = "功能描述:统计A股大盘T日和T-1日成交量对比功能（成交量为沪深两市成交量之和） 大盘每分钟成交量数据", notes = "功能描述:统计A股大盘T日和T-1日成交量对比功能（成交量为沪深两市成交量之和） 大盘每分钟成交量数据", httpMethod = "GET")
    @GetMapping("/stock/tradeAmt")
    public R<Map<String,List>> getComparedStockTradeAmt(){
        return stockService.getComparedStockTradeAmt();
    }

    /**
     * 统计最新交易时间点下的股票(A股) 在各个涨幅区间的数量
     * @return
     */
    @ApiOperation(value = "统计最新交易时间点下的股票(A股) 在各个涨幅区间的数量", notes = "统计最新交易时间点下的股票(A股) 在各个涨幅区间的数量", httpMethod = "GET")
    @GetMapping("/stock/updown")
    public R<Map> getIncreaseRangeInfo(){
        return stockService.getIncreaseRangeInfo();
    }

    /**
     * 功能描述:查询单个个股的分时行情数据 也就是统计股票T日每分钟的交易数据
     * 如果当前日期不在有效时间范围内 则以最近的一个股票交易时间作为查询时间点
     * 总结:获取指定股票T日分时数据
     * @param stockCode 股票编码
     * @return
     */
    @GetMapping("/stock/screen/time-sharing")
    public R<List<Stock4MinuteDomain>> getStockScreenTimeSharing(
            @RequestParam(value = "code",required = true) String stockCode){
        return stockService.getStockScreenTimeSharing(stockCode);

    }

    /**
     * 单个个股日K 数据查询 可以根据区间查询数日的k线数据
     * @param stockCode 股票编码
     * @return
     */
    @GetMapping("/stock/screen/dkline")
    public R<List<Stock4EvrDayDomain>> getStockScreenDkLine(
            @RequestParam(value = "code",required = true) String stockCode){
        return stockService.getStockScreenDkLine(stockCode);
    }

    /**
     * 外盘数据展示
     * @return
     */
    @ApiOperation(value = "外盘数据", notes = "外盘数据", httpMethod = "GET")
    @GetMapping("/external/index")
    public R<List<OutMarketDomain>> getOutMarket(){
        return stockService.getOutMarket();
    }
    /**
     * 股票Code联想推荐功能
     * @param searchStr
     * @return
     */
    @ApiOperation(value = "股票Code联想推荐功能", notes = "股票Code联想推荐功能", httpMethod = "GET")
    @GetMapping("/stock/search")
    public R<List<CodeSearchDomain>> codeSearch(@RequestParam(name = "searchStr" , required = false ,defaultValue = "0") String searchStr){
        return stockService.stockCodeSearch(searchStr);
    }
    /**
     * 个股描述功能实现
     * @param code
     * @return
     */
    @ApiOperation(value = "个股描述功能实现", notes = "个股描述功能实现", httpMethod = "GET")
    @GetMapping("stock/describe")
    public R<StockDescribeDomain> getStockDescribe(@RequestParam(name = "code" , required = true) String code){
        return stockService.getStockDescribe(code);
    }
    /**
     * 功能描述：统计每周内的股票数据信息，信息包含：
     * 股票ID、 一周内最高价、 一周内最低价 、周1开盘价、周5的收盘价、
     * 整周均价、以及一周内最大交易日期（一般是周五所对应日期）
     *
     * @param code
     * @return
     */
    @ApiOperation(value = "个股周K线功能", notes = "个股周K线功能", httpMethod = "GET")
    @GetMapping("/stock/screen/weekkline")
    public R<List<WeekklineDomain>> getWeekkline(@RequestParam String code){
        return stockService.getWeekkline(code);
    }
    /**
     * 获取个股最新分时行情数据，主要包含：
     * 	开盘价、前收盘价、最新价、最高价、最低价、成交金额和成交量、交易时间信息
     * @param code
     * @return
     */
    @GetMapping("/stock/screen/second/detail")
    public R<Stock4HourDetailsDomain> getStockHourDetails(String code){
        return stockService.getStockHourDetails(code)   ;

    }
    /**
     * 个股交易流水行情数据查询--查询最新交易流水，按照交易时间降序取前10
     * @param code
     * @return
     */
    @GetMapping("/stock/screen/second")
    public R<List<Map<String, Object>>> getStockTradinStatement(@RequestParam("code") String code) {
        return stockService.getStockTradinStatement(code);
    }

}
