package com.wu.springbootinit.model.entity;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.wu.springbootinit.common.BaseResponse;
import com.wu.springbootinit.common.ErrorCode;
import com.wu.springbootinit.common.ResultUtils;
import com.wu.springbootinit.exception.BusinessException;
import com.wu.springbootinit.model.dto.chart.GenChartByAiRequest;
import com.wu.springbootinit.model.vo.BiResponseVO;
import com.wu.springbootinit.utils.ExcelUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.wu.springbootinit.exception.ThrowUtils.throwIf;

/**
 * 用户
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

