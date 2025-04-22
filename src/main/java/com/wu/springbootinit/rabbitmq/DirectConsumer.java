package com.wu.springbootinit.rabbitmq;

import com.rabbitmq.client.*;

public class DirectConsumer {

  private static final String EXCHANGE_NAME = "direct";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();


    channel.exchangeDeclare(EXCHANGE_NAME, "direct");
    String queueName = "xiaowu_queue";
    channel.queueDeclare(queueName, true, false, false, null);

    String queueName2 = "xiaohai_queue";
    channel.queueDeclare(queueName2, true, false, false, null);

    String severity = "apple";
    String severity2 = "banana";
    channel.queueBind(queueName, EXCHANGE_NAME, severity);
    channel.queueBind(queueName2, EXCHANGE_NAME, severity2);

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [小五] Received '" +
            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
    DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [小海] Received '" +
                delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
  }
}