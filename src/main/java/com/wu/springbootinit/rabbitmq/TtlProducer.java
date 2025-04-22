package com.wu.springbootinit.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * 消息过期时间的生产者
 */
public class TtlProducer {
	// 定义队列名称为"ttl_queue"
    private final static String QUEUE_NAME = "ttl_queue";

    public static void main(String[] argv) throws Exception {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        // 建立连接、创建频道
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 消息虽然可以重复声明,必须指定相同的参数,在消费者的创建队列要指定过期时间,
            // 后面要放args,在生产者你又想重新创建队列，又不指定参数，那肯定会有问题，
            // 所以要把这里的创建队列注释掉。
            //如果队列不存在，RabbitMQ 会自动创建一个默认的队列，其名称与消息的目标队列一致。
            // channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            
            // 发送消息
            String message = "Hello World!";
        	// 设置消息过期时间为10秒,这是单个消息过期时间，如果要设置队列的过期时间，需要消费者在创建队列时设置
//            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
//                    .expiration("10000") // 设置消息过期时间为10秒
//            		.build();
//            channel.basicPublish("", QUEUE_NAME, properties, message.getBytes(StandardCharsets.UTF_8));
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
