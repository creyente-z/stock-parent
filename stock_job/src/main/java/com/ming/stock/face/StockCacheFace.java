package com.ming.stock.face;

import com.ming.stock.pojo.entity.StockBusiness;
import org.checkerframework.checker.units.qual.A;

import java.util.List;

/**
 * @Author: Ming
 * @Description 定义股票的缓存层
 */
public interface StockCacheFace {
    /**
     * 获取所有股票的编码 并添加上证或者深证的股票的前缀编号:sh  sz
     * @return
     */
    List<String> getAllStockCodeWithPredix();

    /**
     * 根据id更新 股票的信息
     * @param info
     */
    void updateStockInfoById(StockBusiness info);
}
