package com.ming.stock.sharding;

import com.google.common.collect.Range;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: Ming
 * @Description 定义公共个股流水表的分表算法类: 覆盖个股表
 */
public class CommonAlg4Tb implements PreciseShardingAlgorithm<Date>, RangeShardingAlgorithm<Date> {
    /**
     * 精准查询
     * @param tbNames
     * @param shardingValue 封装了逻辑表 分片列名称 条件值等
     * @return 返回具体一个数据源
     */
    @Override
    public String doSharding(Collection<String> tbNames, PreciseShardingValue<Date> shardingValue) {
        //获取逻辑表名称 t_user
        String logicTableName = shardingValue.getLogicTableName();
        //获取数据库指定的分片键名称：cur_time
        String columnName = shardingValue.getColumnName();
        //获取此时查询时的片键值  条件值
        Date curTime = shardingValue.getValue();
        //获取条件值对应的年月 然后 tb集合中过滤除以年月结尾的表即可
        String yearMonth = new DateTime(curTime).toString("yyyyMM");

        Optional<String> result = tbNames.stream().filter(tbName -> tbName.endsWith(yearMonth)).findFirst();
        if (result.isPresent()){
            return result.get();
        }
        return null;
    }
    /**
     * 根据片键的范围匹配数据库
     * @param tbNames
     * @param shardingValue 封装了逻辑表 分片列名称 条件值等
     * @return 当前数据库查询名称的数据库集合
     *
     */
    @Override
    public Collection<String> doSharding(Collection<String> tbNames, RangeShardingValue<Date> shardingValue) {
        //获取逻辑表名称
        String logicTableName = shardingValue.getLogicTableName();
        //获取数据库指定的分片键名称：cur_time
        String columnName = shardingValue.getColumnName();
        //对范围数据的封装
        Range<Date> valueRange = shardingValue.getValueRange();
        //理论上应该让范围值参与运算，然后根据某种算法规则获取需要操作的数据库
        //判断是否有范围查询的起始值
        if (valueRange.hasLowerBound()) {
            //获取起始值
            Date startTime = valueRange.lowerEndpoint();
            int startYearMonth = Integer.parseInt(new DateTime(startTime).toString("yyyyMM"));
            //过滤除数据源中年份大于等于startYear
            tbNames = tbNames.stream().filter(
                            tbName->Integer.parseInt(
                                    tbName.substring(tbName.lastIndexOf("_")+1))>=startYearMonth)
                    .collect(Collectors.toList());


        }
        //判断是否有上限值
        if (valueRange.hasUpperBound()) {
            Date endTime = valueRange.upperEndpoint();
            //获取条件所属年份
            int endYearMonth = Integer.parseInt(new DateTime(endTime).toString("yyyyMM"));
            tbNames = tbNames.stream().filter(
                            tbName->Integer.parseInt(
                                    tbName.substring(tbName.lastIndexOf("_")+1))<=endYearMonth)
                    .collect(Collectors.toList());
        }
        //一般会根据start和end值找符合条件的数据源集合
        return tbNames;
    }
}
