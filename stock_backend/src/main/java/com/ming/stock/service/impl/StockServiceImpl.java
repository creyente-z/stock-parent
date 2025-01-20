package com.ming.stock.service.impl;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ming.stock.mapper.*;
import com.ming.stock.pojo.domain.*;
import com.ming.stock.pojo.vo.StockInfoConfig;
import com.ming.stock.service.StockService;
import com.ming.stock.utils.DateTimeUtil;
import com.ming.stock.vo.resp.PageResult;
import com.ming.stock.vo.resp.R;
import com.ming.stock.vo.resp.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Ming
 * @Description TODO
 */
@Service
@Slf4j
public class StockServiceImpl implements StockService {
    @Autowired
    private StockInfoConfig stockInfoConfig;
    @Autowired
    private StockMarketIndexInfoMapper stockMarketIndexInfoMapper;
    @Autowired
    private StockBlockRtInfoMapper stockBlockRtInfoMapper;
    @Autowired
    private StockRtInfoMapper stockRtInfoMapper;
    @Autowired
    private StockOuterMarketIndexInfoMapper stockOuterMarketIndexInfoMapper;
    @Autowired
    private StockBusinessMapper stockBusinessMapper;
    @Autowired
    private Cache<String,Object> caffeineCache;

    @Override
    public R<List<InnerMarketDomain>> getInnerMarketInfo() {
        R<List<InnerMarketDomain>> result = (R<List<InnerMarketDomain>>) caffeineCache.get("innerMarketKey", key->{
            //1.获取股票的最新交易时间点(精确到分钟,秒和毫秒置为0)
            Date curDate = DateTimeUtil.getLastDate4Stock(DateTime.now()).toDate();
            //因为最新数据还没有获取 所以需要配mock data 等后期完成采集工程 在删除
            curDate = DateTime.parse("2022-12-28 09:31:00",
                    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();

            //2.获取大盘的编码集合
            List<String> mCodes = stockInfoConfig.getInner();
            //3.调用mapper查询数据
            List<InnerMarketDomain> data = stockMarketIndexInfoMapper.getMarketInfo(curDate,mCodes);
            //4.封装数据响应
            return R.ok(data);
        });

        return result;


    }
    /**
     *需求说明: 沪深两市板块分时行情数据查询，以交易时间和交易总金额降序查询，取前10条数据
     * @return
     */
    @Override
    public R<List<StockBlockDomain>> sectorAllLimit() {

        //获取股票最新交易时间点
        Date lastDate = DateTimeUtil.getLastDate4Stock(DateTime.now()).toDate();
        //mock数据,后续删除
        lastDate=DateTime.parse("2022-12-21 14:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        //1.调用mapper接口获取数据
        List<StockBlockDomain> infos=stockBlockRtInfoMapper.sectorAllLimit(lastDate);
        //2.组装数据
        if (CollectionUtils.isEmpty(infos)) {
            return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
        }
        return R.ok(infos);
    }
    /**
     * 分页查询股票最新数据 并按照涨幅排序查询
     * @param page  当前页
     * @param pageSize 每页大小
     * @return
     */
    @Override
    public R<PageResult<StockUpdownDomain>> getStockPageInfo(Integer page, Integer pageSize) {
        //1.获取股票的最新交易时间点(精确到分钟,秒和毫秒置为0)
        Date curDate = DateTimeUtil.getLastDate4Stock(DateTime.now()).toDate();
        //因为最新数据还没有获取 所以需要配mock data 等后期完成采集工程 在删除
        curDate = DateTime.parse("2022-12-30 09:42:00",
                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        //2.设置pagehelper分页参数
        PageHelper.startPage(page,pageSize);
        //3.调用mapper查询
        List<StockUpdownDomain> pageData = stockRtInfoMapper.getStockInfoByTime(curDate);
        //4.组装pageInfo对象 获取分页的具体信息 因为pageInfo包含了丰富的分页信息 很多信息前端不需要
        PageInfo<StockUpdownDomain> pageInfo = new PageInfo<>(pageData);
        PageResult<StockUpdownDomain> pageResult = new PageResult<>(pageInfo);
        //封装响应数据
        return R.ok(pageResult);
    }
    /**
     * 统计沪深两市个股最新交易数据，并按涨幅降序排序查询前4条数据
     * @return
     */
    @Override
    public R<List<StockUpdownDomain>> getTopStocksByIncrease() {
        //1.获取股票最新的交易时间点（精确到分钟，秒和毫秒置为0）
        Date curDate = DateTimeUtil.getLastDate4Stock(DateTime.now()).toDate();
        //因为是最新所有还没有数据 所以需要 mock data  等后续完成股票采集job工程 再将删除即可
        curDate = DateTime.parse("2022-12-30 09:42:00",
                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 调用仓库方法获取涨幅最大的股票
        List<StockUpdownDomain> topStocks = stockRtInfoMapper.findTopStocksByIncrease(curDate);
        // 返回成功响应和股票列表
        return R.ok(topStocks);
    }
    /**
     * 统计最新股票交易日内每分钟涨跌停的股票数量
     * @return
     */
    @Override
    public R<Map<String, List>> getStockUpdownCount() {
        //获取最新的交易时间范围
        //1.获取最新股票的交易时间
        DateTime curDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());
        //假数据
        curDateTime = DateTime.parse("2023-01-06 14:25:00",
                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        Date endDate = curDateTime.toDate();
        //2.获取最新交易点对应的开盘时间点
        Date startDate = DateTimeUtil.getOpenDate(curDateTime).toDate();
        //3.统计涨停数据 约定mapper中flag入参  1->涨停  0->跌停
        List<Map> upList  = stockRtInfoMapper.getStockUpdownCount(startDate,endDate,1);
        //4.统计跌停数据
        List<Map> downList = stockRtInfoMapper.getStockUpdownCount(startDate,endDate,0);
        //5.组装数据
        HashMap<String,List> info = new HashMap<>();
        info.put("upList",upList);
        info.put("downList",downList);
        return R.ok(info);
    }

    @Override
    public void exportStockUpDownInfo(Integer page, Integer pageSize, HttpServletResponse response) {
        //获取分页数据
        R<PageResult<StockUpdownDomain>> r = this.getStockPageInfo(page, pageSize);
        List<StockUpdownDomain> rows = r.getData().getRows();
        //将数据导出到excel
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        try {
            String fileName = URLEncoder.encode("测试", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), StockUpdownDomain.class).sheet("股票信息").doWrite(rows);
        } catch (IOException e) {
            log.error("当前页码:{},每页大小:{},当前时间:{},异常信息:{}",page,pageSize,DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),e.getMessage());
            //通知前端异常 稍后重试
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            R<Object> error = R.error(ResponseCode.ERROR);
            try {
                String jsonData = new ObjectMapper().writeValueAsString(error);
                response.getWriter().write(jsonData);
            } catch (IOException ex) {
                log.error("exportStockUpDownInfo:相应的错误信息失败,时间:{}",page,pageSize,DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
            }
        }
    }
    /**
     * 功能描述:统计A股大盘T日和T-1日成交量对比功能（成交量为沪深两市成交量之和） 大盘每分钟成交量数据
     *
     * @return
     */
    @Override
    public R<Map<String, List>> getComparedStockTradeAmt() {
        //1.获取T日和T-1日的开始时间和结束时间
        //1.1获取最新股票交易日的日期范围 T日时间范围
        DateTime tEndDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());//T日的截止时间点
        //mock data
        tEndDateTime = DateTime.parse(
                "2023-01-03 14:40:00",DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        //转化成java中的date 这样jdbc会默认识别
        Date tEndDate = tEndDateTime.toDate();
        //开盘的时间
        Date tStartDate = DateTimeUtil.getOpenDate(tEndDateTime).toDate();
        //2.获取T-1日的时间范围
        DateTime preTEndDateTime = DateTimeUtil.getPreviousTradingDay(tEndDateTime);
        //mock data
        preTEndDateTime = DateTime.parse(
                "2023-01-02 14:40:00",DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        //转化成java中的date 这样jdbc会默认识别
        Date preTEndDate = preTEndDateTime.toDate();
        //开盘时间
        Date tPreStartDate = DateTimeUtil.getOpenDate(preTEndDateTime).toDate();
        //3.调用mapper查询
        //3.1统计T日交易数据
        List<Map> tData = stockMarketIndexInfoMapper.
                getSumAmtInfo(tStartDate,tEndDate,stockInfoConfig.getInner());
        //3.2统计T-1日交易数据
        List<Map> preTData = stockMarketIndexInfoMapper.
                getSumAmtInfo(tPreStartDate,preTEndDate,stockInfoConfig.getInner());
        //4.组装数据
        HashMap<String,List> info = new HashMap<>();
        info.put("amtList",tData);
        info.put("yesAmtList",preTData);
        //5.响应数据
        return R.ok(info);
    }

    /*{
        "code": 1,
            "data": {
        "time": "2021-12-31 14:58:00",
                "infos": [
        {
            "count": 17,
                "title": "-3~0%"
        },
        {
            "count": 2,
                "title": "-5~-3%"
        },
        //省略......
        ]
    }
    }*/
    @Override
    public R<Map> getIncreaseRangeInfo() {
        //1.获取当前最新股票交易时间点
        DateTime curDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());
        //mock data
        curDateTime = DateTime.parse(
                "2023-01-06 09:55:00",DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        Date curDate = curDateTime.toDate();
        //2.调用mapper获取数据
        List<Map> infos = stockRtInfoMapper.getIncreaseRangeInfoByDate(curDate);
        //获取有序的涨幅区间标题集合
        List<String> upDownRange = stockInfoConfig.getUpDownRange();
        //将顺序的涨幅区间内的每个元素转化为map对象即可
//        List<Map> allInfos = new ArrayList<>();
//        for (String title : upDownRange) {
//            Map tmp = null;
//            for (Map info : infos) {
//                if (info.containsValue(title)){
//                    tmp = info;
//                    break;
//                }
//            }
//            if (tmp==null){
//                //不存在则补齐
//                tmp = new HashMap();
//                tmp.put("count",0);
//                tmp.put("title",title);
//            }
//            allInfos.add(tmp);
//        }
        //方式2:stream遍历循环
        List<Map> allInfos = upDownRange.stream().map(title->{
            Optional<Map> result = infos.stream().filter(map->
                    map.containsValue(title)).findFirst();
            //判断是否符合过滤条件的元素
            if (result.isPresent()){
                return result.get();
            }else {
                HashMap<String,Object> tmp = new HashMap<>();
                tmp.put("count",0);
                tmp.put("title",title);
                return tmp;
            }
        }).collect(Collectors.toList());
        //3.组装数据
        HashMap<String,Object> data = new HashMap<>();
        //获取指定日期格式字符串
        data.put("time",curDateTime.toString("yyyy-MM-dd HH:mm:ss"));
        //data.put("infos",infos);
        data.put("infos",allInfos);
        //4.返回数据
        return R.ok(data);
    }

    @Override
    public R<List<Stock4MinuteDomain>> getStockScreenTimeSharing(String stockCode) {
        //1.获取T日最新股票交易时间点 endTime
        DateTime endDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());
        endDateTime = DateTime.parse(
                "2022-12-30 14:30:00",DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        Date endDate = endDateTime.toDate();
        Date openDate = DateTimeUtil.getOpenDate(endDateTime).toDate();
        //2.查询
        List<Stock4MinuteDomain> data = stockRtInfoMapper.
                getStock4MinuteInfo(openDate,endDate,stockCode);
        //3.返回
        return R.ok(data);
    }

    /**
     * 单个个股日K 数据查询 ，可以根据时间区间查询数日的K线数据
     * @param stockCode 股票编码
     */
    @Override
    public R<List<Stock4EvrDayDomain>> getStockScreenDkLine(String stockCode) {
        //1.获取统计日k线的数据的时间范围
        //1.1获取截止时间(当前时间)
        DateTime endDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());
        //TODO MOCKDATA
        endDateTime = DateTime.parse("2023-06-06 14:25:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        Date endDate = endDateTime.toDate();
        //1.2 获取开始时间
        DateTime startDateTime = endDateTime.minusDays(10);
        //TODO MOCKDATA
        startDateTime = DateTime.parse("2023-01-01 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        Date startDate = startDateTime.toDate();
        //2.调用mapper获取指定日期范围内的日k线数据
        //List<Stock4EvrDayDomain> dkLineData = stockRtInfoMapper.getStock4DkLine(startDate,endDate,stockCode);
        //3.方案二:分步实现
        List<Date> mxTimes = stockRtInfoMapper.getMxTime4EvryDay(stockCode,startDate,endDate);
        List<Stock4EvrDayDomain> infos = stockRtInfoMapper.getStock4DkLine2(stockCode,mxTimes);
        if (CollectionUtils.isEmpty(infos)){
            return R.error(ResponseCode.NO_RESPONSE_DATA);
        }
        //3.返回
        return R.ok(infos);


    }

    /**
     * 获取外盘数据
     * @return
     */
    @Override
    public R<List<OutMarketDomain>> getOutMarket() {
        // 获取当前时间点
        Date curDate = DateTimeUtil.getLastDate4Stock(DateTime.now()).toDate();
        // 制造mock数据
        curDate = DateTime.parse("2022-12-01 10:57:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();

        // 从数据库中查询数据
        List<OutMarketDomain> data = stockOuterMarketIndexInfoMapper.getOutMarket(curDate);

        // 封装数据
        return R.ok(data);

    }

    /**
     * 股票Code联想推荐功能
     * @param searchStr
     * @return
     */
    @Override
    public R<List<CodeSearchDomain>> stockCodeSearch(String searchStr) {
        // 获取所有股票信息
        List<CodeSearchDomain> businessMsg =  stockBusinessMapper.getMsg(searchStr);

        return R.ok(businessMsg);


    }

    /**
     * 个股描述功能实现
     * @param code
     * @return
     */
    @Override
    public R<StockDescribeDomain> getStockDescribe(String code) {
        // 从数据库中获取数据
        StockDescribeDomain data = stockBusinessMapper.getStockDescribe(code);
        return R.ok(data);
    }

    @Override
    public R<List<WeekklineDomain>> getWeekkline(String code) {
        // 获取当前时间点
        DateTime curDate = DateTimeUtil.getLastDate4Stock(DateTime.now());
        // 制造mock数据
        curDate = DateTime.parse("2023-07-07 14:55:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));

        Date endDate = curDate.toDate();
        Date startDate = curDate.minusMonths(3).toDate();

        List<WeekklineDomain> date = stockRtInfoMapper.getWeekkline(startDate,endDate,code);

        return R.ok(date);
    }

    @Override
    public R<Stock4HourDetailsDomain> getStockHourDetails(String code) {
        //获取最新有效交易时间点
        DateTime lastDate = DateTimeUtil.getLastDate4Stock(DateTime.now());
        //转为Java中Date对象
        Date lastDateTime = lastDate.toDate();
        //TODO mock测试 后期通过第三方接口动态获取实时数据
        lastDateTime = DateTime.parse("2022-12-30 09:32:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        //调用mapper
        Stock4HourDetailsDomain stock4HourDetailsDomain = stockRtInfoMapper.getStockHourDetails(code,lastDateTime);
        return R.ok(stock4HourDetailsDomain);
    }



    @Override
    public R<List<Map<String, Object>>> getStockTradinStatement(String code) {
        if(StringUtils.isBlank(code)) {
            return R.error(ResponseCode.DATA_ERROR.getMessage());
        }
        List<Map<String,Object>> mapResult=stockRtInfoMapper.getByCodeInfo(code);
        return R.ok(mapResult);
    }



}
