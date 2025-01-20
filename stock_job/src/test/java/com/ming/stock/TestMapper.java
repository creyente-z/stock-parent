package com.ming.stock;

import com.google.common.collect.Lists;
import com.ming.stock.mapper.StockBusinessMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Ming
 * @Description TODO
 */
@SpringBootTest
public class TestMapper {
    @Autowired
    StockBusinessMapper stockBusinessMapper;

    /**
     * 获取A股个股编码集合
     */
    @Test
    public void test01(){
        List<String> stockIds = stockBusinessMapper.getStockIds();
        System.out.println(stockIds);
        //添加大盘业务前缀 sh sz
        stockIds = stockIds.stream()
                .map(code->code.startsWith("6") ? "sh" +code:"sz"+code)
                .collect(Collectors.toList());
        System.out.println(stockIds);
        //将所有个股编码组成大的的集合拆分成若干个小集合 40---->15  15  10
        Lists.partition(stockIds,15).forEach(codes->{
            System.out.println("size"+codes.size()+":"+codes);
        });
    }

}
