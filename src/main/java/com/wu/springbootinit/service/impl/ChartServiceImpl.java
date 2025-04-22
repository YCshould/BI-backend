package com.wu.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.wu.springbootinit.mapper.ChartMapper;
import com.wu.springbootinit.model.entity.Chart;
import com.wu.springbootinit.service.ChartService;
import org.springframework.stereotype.Service;

/**
* @author WuXHa
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2025-03-09 10:35:16
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

}




