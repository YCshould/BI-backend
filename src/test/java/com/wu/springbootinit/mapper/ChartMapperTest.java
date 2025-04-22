package com.wu.springbootinit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChartMapperTest {

    @Resource
    private ChartMapper chartMapper;

    @Test
    void createTable() {
        String tableName = "chart"+1899294075014647810L;
        List<String> columns = Arrays.asList( "id INT AUTO_INCREMENT PRIMARY KEY", "日期 VARCHAR(255)", "用户数 INT");
        chartMapper.createTable(tableName, columns);
    }

    @Test
    void queryChartData() {
        String chartId = "1899294075014647810";
        String querySql = String.format("select * from chart%s", chartId);
        List<Map<String, Object>> resultData = chartMapper.queryChartData(querySql);
        StringBuffer sb = new StringBuffer();
        sb.append("日期,用户数\n");
        for (Map<String, Object> resultDatum : resultData) {
            sb.append(resultDatum.get("日期")).append(",").append(resultDatum.get("用户数")).append("\n");
        }
        String result = sb.toString();
        System.out.println(result);
        System.out.println(resultData);
    }

}