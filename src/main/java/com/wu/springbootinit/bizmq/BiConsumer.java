package com.wu.springbootinit.bizmq;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rabbitmq.client.Channel;
import com.wu.springbootinit.common.ErrorCode;
import com.wu.springbootinit.exception.BusinessException;
import com.wu.springbootinit.manager.DeepseekManger;
import com.wu.springbootinit.manager.RedisLimitManager;
import com.wu.springbootinit.model.entity.Chart;
import com.wu.springbootinit.service.ChartService;
import com.wu.springbootinit.utils.ExcelUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.io.IOException;

import static com.wu.springbootinit.exception.ThrowUtils.throwIf;

// 使用@Component注解标记该类为一个组件，让Spring框架能够扫描并将其纳入管理
@Component
// 使用@Slf4j注解生成日志记录器
@Slf4j
public class BiConsumer {


    @Resource
    private ChartService chartService;

    @Resource
    private DeepseekManger deepseekManger;

    /**
     * 接收消息的方法
     *
     * @param message     接收到的消息内容，是一个字符串类型
     * @param channel     消息所在的通道，可以通过该通道与 RabbitMQ 进行交互，例如手动确认消息、拒绝消息等
     * @param deliveryTag 消息的投递标签，用于唯一标识一条消息
     */
    // 使用@SneakyThrows注解简化异常处理
    @SneakyThrows
    // 使用@RabbitListener注解指定要监听的队列名称为"code_queue"，并设置消息的确认机制为手动确认
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    // @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag是一个方法参数注解,用于从消息头中获取投递标签(deliveryTag),
    // 在RabbitMQ中,每条消息都会被分配一个唯一的投递标签，用于标识该消息在通道中的投递状态和顺序。通过使用@Header(AmqpHeaders.DELIVERY_TAG)注解,可以从消息头中提取出该投递标签,并将其赋值给long deliveryTag参数。
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        // 使用日志记录器打印接收到的消息内容
        log.info("receiveMessage message = {}", message);
        if(StringUtils.isBlank(message)){
            channel.basicNack(deliveryTag,false,false);
            return;
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if(chart == null){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图表不存在");
        }
        Long modelid = 1651468516836098050L;
        // 投递标签是一个数字标识,它在消息消费者接收到消息后用于向RabbitMQ确认消息的处理状态。通过将投递标签传递给channel.basicAck(deliveryTag, false)方法,可以告知RabbitMQ该消息已经成功处理,可以进行确认和从队列中删除。
        // 手动确认消息的接收，向RabbitMQ发送确认消息
        //调用ai接口前先更新图表状态为running
        Chart chart1 = new Chart();
        chart1.setStatus("running");
        chart1.setId(chart.getId());
        boolean b = chartService.updateById(chart1);
        if (!b) {
            updatefailstatus("图表更新运行中状态失败", chart.getId());
            channel.basicNack(deliveryTag,false,false);
            return;
        }
        String msgChat = deepseekManger.toChat(modelid, getUserInput(chart));
        String[] split = msgChat.split("【【【【【");
        if (split.length < 3) {
            updatefailstatus("图表更新失败", chart.getId());
            channel.basicNack(deliveryTag,false,false);
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
        if (!c) {
            updatefailstatus("图表更新成功状态失败", chart.getId());
            channel.basicNack(deliveryTag,false,false);
            return;
        }
        channel.basicAck(deliveryTag, false);

    }

//    /**
//     * 监听普通队列的消息
//     */
//    @RabbitListener(queues = {"normal_queue"}, ackMode = "MANUAL")
//    public void receiveNormalLetterMessage(String message,Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
//        channel.basicNack(deliveryTag,false,false);
//    }
//
//    /**
//     * 监听死信队列的消息
//     */
//    @RabbitListener(queues = {"die_queue"})
//    public void receiveDieLetterMessage(String message,Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
//        log.info("从死信队列收到消息: {}", message);
//        System.out.println("从死信队列收到消息: " + message);
//    }

    private void updatefailstatus(String msg,Long id){
        Chart chart3 = new Chart();
        chart3.setStatus("fail");
        chart3.setChartMsg(msg);
        chart3.setId(id);
        boolean d = chartService.updateById(chart3);
        throwIf(!d, ErrorCode.NOT_FOUND_ERROR,"图表更新失败状态失败");

    }

    private String getUserInput(Chart chart) {
        String chartData = chart.getChartData();
        String chartType = chart.getChartType();
        String goal = chart.getGoal();
        //压缩输入数据
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求；").append("\n");
        userInput.append(goal).append("\n");
        userInput.append("图标类型；").append("\n");
        userInput.append(chartType).append("\n");
        userInput.append("原始数据：").append("\n");

        userInput.append(chartData).append("\n");
        return userInput.toString();

    }
}
