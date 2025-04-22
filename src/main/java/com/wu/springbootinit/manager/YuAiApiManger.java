package com.wu.springbootinit.manager;

import com.wu.springbootinit.common.ErrorCode;
import com.wu.springbootinit.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class YuAiApiManger {

    @Resource
    private  YuCongMingClient client;

    public  String toChat(Long modelId,String msg) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(msg);
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        if(response==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"ai接口调用失败");
        }
        System.out.println(response.getData());
        return response.getData().getContent();
    }
}
