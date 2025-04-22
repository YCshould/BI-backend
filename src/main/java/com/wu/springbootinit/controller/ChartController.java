package com.wu.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wu.springbootinit.annotation.AuthCheck;
import com.wu.springbootinit.bizmq.BiProducer;
import com.wu.springbootinit.common.BaseResponse;
import com.wu.springbootinit.common.DeleteRequest;
import com.wu.springbootinit.common.ErrorCode;
import com.wu.springbootinit.common.ResultUtils;
import com.wu.springbootinit.constant.CommonConstant;
import com.wu.springbootinit.constant.FileConstant;
import com.wu.springbootinit.constant.UserConstant;
import com.wu.springbootinit.exception.BusinessException;
import com.wu.springbootinit.manager.DeepseekManger;
import com.wu.springbootinit.manager.RedisLimitManager;
import com.wu.springbootinit.manager.YuAiApiManger;
import com.wu.springbootinit.model.dto.chart.*;
import com.wu.springbootinit.model.entity.Chart;
import com.wu.springbootinit.model.entity.User;

import com.wu.springbootinit.model.vo.BiResponseVO;
import com.wu.springbootinit.service.ChartService;
import com.wu.springbootinit.service.UserService;
import com.wu.springbootinit.utils.ExcelUtils;
import com.wu.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.wu.springbootinit.exception.ThrowUtils.throwIf;

/**
 * 帖子接口
 *
 *
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private YuAiApiManger yuAiApiManger;

    @Resource
    private DeepseekManger   deepseekManger;

    @Resource
    private RedisLimitManager  redisLimitManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiProducer biProducer;

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Chart>> listChartVoByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 同步请求
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     * @throws FileNotFoundException
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponseVO> genchartbyai(@RequestPart("file") MultipartFile multipartFile,
                                                   GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) throws FileNotFoundException {
        String chartType = genChartByAiRequest.getChartType();
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();

        throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR,"图表类型不能为空");
        throwIf(StringUtils.isNotBlank(name)&&name.length()>200, ErrorCode.PARAMS_ERROR,"图表名称不能超过200个字符");

        String originalFilename = multipartFile.getOriginalFilename();
        long size = multipartFile.getSize();
        // 限制文件大小
        //文件最大长度
        final long maxFileSize = 1024 * 1024L; // 1M
        throwIf(size>maxFileSize,ErrorCode.PARAMS_ERROR,"文件大小不能超过1M");

        // 限制文件类型
        final List<String> allowTypes = Arrays.asList( "xls", "xlsx");
        String suffix = FileUtil.getSuffix(originalFilename); // 获取文件后缀
        throwIf(!allowTypes.contains(suffix), ErrorCode.PARAMS_ERROR,"不支持的文件类型");

        //todo 检验文件的合规性，腾讯云的图片审核接口，用的也是腾讯云COS

        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"请先登录");
        }

        //压缩输入数据
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求；").append("\n");
        userInput.append(goal).append("\n");
        userInput.append("图标类型；").append("\n");
        userInput.append(chartType).append("\n");
        userInput.append("原始数据：").append("\n");

        String exceltocsv = ExcelUtils.exceltocsv(multipartFile);
        userInput.append(exceltocsv).append("\n");

        Long modelid=1651468516836098050L;
        //在调用ai接口前，先调用redission的接口限制进行流量限制
        String key="genchartbyai_"+loginUser.getId();
        redisLimitManager.limit(key);
        //调ai接口
        String msgChat = deepseekManger.toChat(modelid, userInput.toString());
        //处理ai接口返回的数据
        String[] split = msgChat.split("【【【【【");
        if(split.length<3){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"输出数据有误");
        }
        String genChart = split[1].trim();
        String genResult = split[2].trim();

        Chart chart = new Chart();
        chart.setUserId(loginUser.getId());
        chart.setChartType(chartType);
        chart.setChartData(exceltocsv);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setStatus("success");
        chart.setGoal(goal);
        chart.setName(name);
        boolean save = chartService.save(chart);
        if(!save){
            Chart chart1 = new Chart();
            chart1.setStatus("fail");
            chart1.setChartMsg("图表保存失败");
            chart1.setId(chart.getId());
            boolean b = chartService.updateById(chart1);
            throwIf(!b,ErrorCode.OPERATION_ERROR,"更新图表失败状态失败");
        }
        throwIf(!save,ErrorCode.OPERATION_ERROR,"保存图表失败");
        //将ai生成的图表总结数据和生成图表前端代码保存到数据库
        BiResponseVO biResponseVO = new BiResponseVO();
        biResponseVO.setGenChart(genChart);
        biResponseVO.setGenResult(genResult);
        biResponseVO.setId(chart.getId());
        return ResultUtils.success(biResponseVO);
    }

    /**
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     * @throws FileNotFoundException
     */
    //异步请求，没设计提交genchart和genresult到前端，在我的图标里面可以看到（数据库有）
    @PostMapping("/genbyasync")
    public BaseResponse<BiResponseVO> genchartbyaiasync(@RequestPart("file") MultipartFile multipartFile,
                                                   GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) throws FileNotFoundException {
        String chartType = genChartByAiRequest.getChartType();
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();

        throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR,"图表类型不能为空");
        throwIf(StringUtils.isNotBlank(name)&&name.length()>200, ErrorCode.PARAMS_ERROR,"图表名称不能超过200个字符");

        String originalFilename = multipartFile.getOriginalFilename();
        long size = multipartFile.getSize();
        // 限制文件大小
        //文件最大长度
        final long maxFileSize = 1024 * 1024L; // 1M
        throwIf(size>maxFileSize,ErrorCode.PARAMS_ERROR,"文件大小不能超过1M");

        // 限制文件类型
        final List<String> allowTypes = Arrays.asList( "xls", "xlsx");
        String suffix = FileUtil.getSuffix(originalFilename); // 获取文件后缀
        throwIf(!allowTypes.contains(suffix), ErrorCode.PARAMS_ERROR,"不支持的文件类型");

        //todo 检验文件的合规性，腾讯云的图片审核接口，用的也是腾讯云COS

        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"请先登录");
        }


        String exceltocsv = ExcelUtils.exceltocsv(multipartFile);

        Chart chart = new Chart();
        chart.setUserId(loginUser.getId());
        chart.setChartType(chartType);
        chart.setChartData(exceltocsv);
        chart.setStatus("wait");
        chart.setGoal(goal);
        chart.setName(name);
        boolean save = chartService.save(chart);
        throwIf(!save,ErrorCode.OPERATION_ERROR,"保存图表失败");
        long chartid = chart.getId();
        biProducer.sendMessage(String.valueOf(chartid));

        //将ai生成的图表总结数据和生成图表前端代码保存到数据库
        BiResponseVO biResponseVO = new BiResponseVO();
        biResponseVO.setId(chart.getId());
        return ResultUtils.success(biResponseVO);
    }

    private void updatefailstatus(String msg,Long id){
        Chart chart3 = new Chart();
        chart3.setStatus("fail");
        chart3.setChartMsg(msg);
        chart3.setId(id);
        boolean d = chartService.updateById(chart3);
        throwIf(!d, ErrorCode.NOT_FOUND_ERROR,"图表更新失败状态失败");

    }

    /**
     * 线程池本地队列
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     * @throws FileNotFoundException
     */
    @PostMapping("/genbyasync/mq")
    public BaseResponse<BiResponseVO> genchartbyaiasyncmq(@RequestPart("file") MultipartFile multipartFile,
                                                        GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) throws FileNotFoundException {
        String chartType = genChartByAiRequest.getChartType();
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();

        throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR,"图表类型不能为空");
        throwIf(StringUtils.isNotBlank(name)&&name.length()>200, ErrorCode.PARAMS_ERROR,"图表名称不能超过200个字符");

        String originalFilename = multipartFile.getOriginalFilename();
        long size = multipartFile.getSize();
        // 限制文件大小
        //文件最大长度
        final long maxFileSize = 1024 * 1024L; // 1M
        throwIf(size>maxFileSize,ErrorCode.PARAMS_ERROR,"文件大小不能超过1M");

        // 限制文件类型
        final List<String> allowTypes = Arrays.asList( "xls", "xlsx");
        String suffix = FileUtil.getSuffix(originalFilename); // 获取文件后缀
        throwIf(!allowTypes.contains(suffix), ErrorCode.PARAMS_ERROR,"不支持的文件类型");

        //todo 检验文件的合规性，腾讯云的图片审核接口，用的也是腾讯云COS

        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"请先登录");
        }

        //压缩输入数据
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求；").append("\n");
        userInput.append(goal).append("\n");
        userInput.append("图标类型；").append("\n");
        userInput.append(chartType).append("\n");
        userInput.append("原始数据：").append("\n");

        String exceltocsv = ExcelUtils.exceltocsv(multipartFile);
        userInput.append(exceltocsv).append("\n");

        //在调用ai接口前，先调用redission的接口限制进行流量限制
        String key="genchartbyai_"+loginUser.getId();
        redisLimitManager.limit(key);

        Chart chart = new Chart();
        chart.setUserId(loginUser.getId());
        chart.setChartType(chartType);
        chart.setChartData(exceltocsv);
        chart.setStatus("wait");
        chart.setGoal(goal);
        chart.setName(name);
        boolean save = chartService.save(chart);
        throwIf(!save,ErrorCode.OPERATION_ERROR,"保存图表失败");

        Long modelid=1651468516836098050L;
        CompletableFuture.runAsync(()->{
            //调用ai接口前先更新图表状态为running
            Chart chart1 = new Chart();
            chart1.setStatus("running");
            chart1.setId(chart.getId());
            boolean b = chartService.updateById(chart1);
            if(!b){
                updatefailstatus("图表更新运行中状态失败",chart.getId());
                return;
            }

            String msgChat = deepseekManger.toChat(modelid, userInput.toString());
            String[] split = msgChat.split("【【【【【");
            if(split.length<3){
                updatefailstatus("图表更新失败",chart.getId());
                return;
            }
            String genChart = split[1].trim();
            String genResult = split[2].trim();

            //调用ai接口前先更新图表状态为success并且更新genChart，genResult
            Chart chart2 = new Chart();
            chart2.setStatus("success");
            chart2.setId(chart.getId());
            chart2.setGenChart(genChart);
            chart2.setGenResult(genResult);
            boolean c = chartService.updateById(chart1);
            if(!c){
                updatefailstatus("图表更新成功状态失败",chart.getId());
            }
        },threadPoolExecutor);

        //将ai生成的图表总结数据和生成图表前端代码保存到数据库
        BiResponseVO biResponseVO = new BiResponseVO();
        biResponseVO.setId(chart.getId());
        return ResultUtils.success(biResponseVO);
    }
    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        String chartType = chartQueryRequest.getChartType();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        Long id = chartQueryRequest.getId();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(id!=null&&id>0,"id",id);
        queryWrapper.ne(ObjectUtils.isNotEmpty(goal), "goal", goal);
        queryWrapper.like(ObjectUtils.isNotEmpty(name), "name", name);
        queryWrapper.eq(ObjectUtils.isNotEmpty(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDeleted",false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }



}
