package com.ming.stock.service;

import com.ming.stock.vo.req.LogPageReqVo;
import com.ming.stock.vo.resp.PageResult;
import com.ming.stock.vo.resp.R;

import java.util.List;

/**
 * @author by Ming
 * @Description 操纵日志服务接口
 */
public interface LogService {
    /**
     * 日志信息分页综合查询
     * @param vo
     * @return
     */
    R<PageResult> logPageQuery(LogPageReqVo vo);

    /**
     * 删除日志信息
     * @param logIds
     * @return
     */
    R<String> deleteBatch(List<Long> logIds);
}