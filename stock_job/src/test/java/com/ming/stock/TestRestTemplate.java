package com.ming.stock;

import com.ming.stock.pojo.Account;
import com.ming.stock.service.StockTimerTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * @Author: Ming
 * @Description TODO
 */
@SpringBootTest
public class TestRestTemplate {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private StockTimerTaskService stockTimerTaskService;


    @Test
    public void test01(){
        String url="http://localhost:6767/account/getByUserNameAndAddress?userName=zhangsan&address=sh";
        /*
          参数1：url请求地址
          参数2：请求返回的数据类型
         */
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        //获取响应头
        HttpHeaders headers = result.getHeaders();
        System.out.println(headers);
        //获取响应状态码 200
        int statusCode = result.getStatusCodeValue();
        System.out.println(statusCode);
        //获取响应数据
        String respData = result.getBody();
        System.out.println(respData);
    }

    /**
     * 测试响应数据 自动封装到vo对象中
     */
    @Test
    public void test02(){
        String url="http://localhost:6767/account/getByUserNameAndAddress?userName=zhangsan&address=sh";
        Account account = restTemplate.getForObject(url, Account.class);
        System.out.println(account);
    }
    /*
    * 请求头设置参数 访问指定接口
    * */
    @Test
    public void test03(){
        String url = "http://localhost:6767/account/getHeader";
        //设置请求头参数
        HttpHeaders headers = new HttpHeaders();
        headers.add("userName","zhangsan");
        //请求头填充到请求对象下
        HttpEntity<Map> entity = new HttpEntity<>(headers);
        //发送请求
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String result = responseEntity.getBody();
        System.out.println(result);

    }
    /*
    * post模拟form表单提交数据
    * */
    @Test
    public void test04(){
        String url = "http://localhost:6767/account/addAccount";
        //设置请求头 指定数据请求方式
        HttpHeaders headers = new HttpHeaders();
        //告知被调用方法 请求方式是form表单提交 这样对解析数据时 就会按照form表单的方式解析处理
        headers.add("Content-type","application/x-www-form-urlencoded");
        //组装模拟form表单提交数据，内部元素相当于form表单的input框
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("id","10");
        map.add("userName","ming");
        map.add("address","shanghai");
        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(map, headers);
        /*
            参数1：请求url地址
            参数2：请求方式 POST
            参数3：请求体对象，携带了请求头和请求体相关的参数
            参数4：响应数据类型
         */
        ResponseEntity<Account> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Account.class);
        Account body = exchange.getBody();
        System.out.println(body);
    }
    @Test
    public void test05(){
        String url = "http://localhost:6767/account/updateAccount";
        //设置请求头 指定数据请求方式
        HttpHeaders headers = new HttpHeaders();
        //告知被调用方法 请求方式是form表单提交 这样对解析数据时 就会按照form表单的方式解析处理
        headers.add("Content-type","application/json; charset=utf-8");
        //组装json格式数据
        String jsonReq="{\"address\":\"上海\",\"id\":\"1\",\"userName\":\"zhangsan\"}";
        //构建请求对象
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonReq,headers);
        ResponseEntity<Account> responseEntity =
                restTemplate.exchange(url, HttpMethod.POST, httpEntity, Account.class);
        Account body = responseEntity.getBody();
        System.out.println(body);

    }
    @Test
    public void test06(){
        String url = "http://localhost:6767/account/getCookie";
        ResponseEntity<String> result = restTemplate.getForEntity(url,String.class);
        //获取cookie
        List<String> cookies = result.getHeaders().get("Set-Cookie");
        //获取响应数据
        String resStr = result.getBody();
        System.out.println(resStr);
        System.out.println(cookies);
    }

    /**
     * 测试采集国内大盘数据
     */
    @Test
    public void testInnerGetMarketInfo() throws InterruptedException {
        //stockTimerTaskService.getInnerMarketInfo();
        stockTimerTaskService.getStockRtIndex();
        Thread.sleep(5000);
    }
    /**
     * 获取板块数据测试
     */
//    @Test
//    public void testStockSectorRtIndex(){
//        stockTimerTaskService.getStockSectorRtIndex();
//    }


}
