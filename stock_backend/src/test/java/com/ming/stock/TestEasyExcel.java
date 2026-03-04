package com.ming.stock;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.ming.stock.pojo.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: Ming
 * @Description TODO
 */
public class TestEasyExcel {
    public List<User> init(){
        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setAddress("上海"+i);
            user.setUserName("张三"+i);
            user.setBirthday(new Date());
            user.setAge(10+i);
            users.add(user);
        }
        return users;
    }

    /**
     * 测试excel导出功能 直接导出之后 表头名称默认是是实体类的属性名称
     */
    @Test
    public void test01(){
        List<User> users = init();
        EasyExcel.write("C:\\Users\\11519\\Desktop\\test.xls", User.class)
                .sheet("用户信息").doWrite(users);
    }

    /**
     * 读取excel数据格式必须与实体类定义一样 否则读取不到
     */
    @Test
    public void readExcel(){
        ArrayList<User> users = new ArrayList<>();
        //读取数据 最后一个参数是解析器 读取一行回调一次invoke方法 都解析完 执行完成
        EasyExcel.read("C:\\Users\\11519\\Desktop\\test.xls", User.class, new AnalysisEventListener() {
            @Override
            public void invoke(Object o, AnalysisContext analysisContext) {
                System.out.println(o);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                System.out.println("完成.............");
            }
        }).sheet().doRead();
        System.out.println(users);
    }

}
