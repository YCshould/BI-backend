package com.wu.springbootinit.manager;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.lkeap.v20240522.LkeapClient;
import com.tencentcloudapi.lkeap.v20240522.models.ChatCompletionsRequest;
import com.tencentcloudapi.lkeap.v20240522.models.ChatCompletionsResponse;
import com.tencentcloudapi.lkeap.v20240522.models.Message;
import com.wu.springbootinit.common.ErrorCode;
import com.wu.springbootinit.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class DeepseekManger {

    @Resource
    private LkeapClient deepseekClient;

    public  String toChat(Long modelId,String msg) {

        final String systemMsg = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                "分析需求：\n" +
                "{数据分析的需求或者目标}\n" +
                "原始数据：\n" +
                "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
                "【【【【【\n" +
                "{前端 Echarts V5 的 option 配置对象js代码(输出json格式)，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
                "【【【【【\n" +
                "{明确的数据分析结论、越详细越好，不要生成多余的注释}\n" +
                "【【【【【\n";

        try {
            ChatCompletionsRequest req = new ChatCompletionsRequest();
            req.setModel("deepseek-r1");
            req.setStream(false);

            //message0是提示Ai系统的消息告诉他的身份，我要的输入输出格式，message1是用户消息也就是要ai做的事
            Message[] messages = new Message[2];
            Message message0 = new Message();
            Message message1 = new Message();
            message0.setRole("system");
            message0.setContent(systemMsg);
            messages[0] = message0;

            message1.setRole("user");
            message1.setContent(msg);
            messages[1] = message1;

            req.setMessages(messages);

            // 返回的resp是一个ChatCompletionsResponse的实例，与请求对象对应
            ChatCompletionsResponse resp = deepseekClient.ChatCompletions(req);

            String content = resp.getChoices()[0].getMessage().getContent();
            // 输出json格式的字符串回包
            return content;
        } catch (
                TencentCloudSDKException e) {
            log.error("ai对话请求失败",e);
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"ai对话请求失败");
        }

    }
}
