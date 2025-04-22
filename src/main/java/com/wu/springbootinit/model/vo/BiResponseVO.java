package com.wu.springbootinit.model.vo;

import lombok.Data;

/**
 * ai提示信息返回类
 */
@Data
public class BiResponseVO {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 生成图表
     */
    private String genChart;


    /**
     * 分析图表结论
     */
    private String genResult;


}
