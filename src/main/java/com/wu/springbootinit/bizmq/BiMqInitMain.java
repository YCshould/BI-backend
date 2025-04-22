package com.wu.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于创建测试程序用到的交换机和队列（只用在程序启动前执行一次）
 */
public class BiMqInitMain {

    public static void main(String[] args) {
        try {
            // 创建连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            // 创建连接
            Connection connection = factory.newConnection();
            // 创建通道
            Channel channel = connection.createChannel();
            // 定义交换机的名称为"code_exchange"
            String EXCHANGE_NAME = BiMqConstant.BI_EXCHANGE_NAME;
            // 声明交换机，指定交换机类型为 direct
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 创建队列，随机分配一个队列名称
            String queueName = BiMqConstant.BI_QUEUE_NAME;
            // 声明队列，设置队列持久化、非独占、非自动删除，并传入额外的参数为 null
            channel.queueDeclare(queueName, true, false, false, null);
            // 将队列绑定到交换机，指定路由键为 "my_routingKey"
            channel.queueBind(queueName, EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY);
        } catch (Exception e) {
        	// 异常处理
        }

//        try {
//            // 创建连接工厂
//            ConnectionFactory factory = new ConnectionFactory();
//            factory.setHost("localhost");
//            // 创建连接
//            Connection connection = factory.newConnection();
//            // 创建通道
//            Channel channel = connection.createChannel();
//            // 定义普通交换机和死信交换机的名称
//            String NORMAL_EXCHANGE_NAME = "normal_exchange";
//            String DIE_EXCHANGE_NAME = "die_exchange";
//            // 声明交换机，指定交换机类型为 direct
//            channel.exchangeDeclare(NORMAL_EXCHANGE_NAME, "direct");
//            channel.exchangeDeclare(DIE_EXCHANGE_NAME, "direct");
//
//
//            Map<String, Object> params = new HashMap<>();
//            // 将要创建的队列绑定到指定的交换机，并设置死信队列的参数
//            params.put("x-dead-letter-exchange", DIE_EXCHANGE_NAME);
//            // 指定死信要转发到外包死信队列
//            params.put("x-dead-letter-routing-key", "die_key");
//
//            // 创建普通队列，随机分配一个队列名称
//            String queueName = "normal_queue";
//            // 声明队列，设置队列持久化、非独占、非自动删除，并传入额外的参数为 null
//            channel.queueDeclare(queueName, true, false, false, params);
//            // 将队列绑定到交换机，指定路由键为 "normal_key"
//            channel.queueBind(queueName, NORMAL_EXCHANGE_NAME, "normal_key");
//
//            // 创建死信队列，随机分配一个队列名称
//            String dieQueueName = "die_queue";
//            // 声明队列，设置队列持久化、非独占、非自动删除，并传入额外的参数为 null
//            channel.queueDeclare(dieQueueName, true, false, false, null);
//            // 将死信队列绑定到死信交换机，指定路由键为 "die_key"
//            channel.queueBind(dieQueueName, DIE_EXCHANGE_NAME, "die_key");
//        } catch (Exception e) {
//            // 异常处理
//        }
    }
}
