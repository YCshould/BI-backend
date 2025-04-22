package com.wu.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wu.springbootinit.model.entity.Chart;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
* @author WuXHa
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2025-03-09 10:35:16
* @Entity generator.domain.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {

    /**
     * 建表语句，要注意sql注入风险where 1==1
     * @param tableName
     * @param columns
     */
    void createTable(@Param("tableName") String tableName, @Param("columns") List<String> columns);


    /**
     * 查询图表数据,要注意sql注入风险where 1==1
     * @param querySql
     * @return
     */
    List<Map<String, Object>> queryChartData(@Param("querySql") String querySql);


}




