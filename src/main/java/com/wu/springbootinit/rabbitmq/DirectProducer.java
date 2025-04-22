package com.wu.springbootinit.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

public class DirectProducer {

  private static final String EXCHANGE_NAME = "direct";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");

    Scanner scanner = new Scanner(System.in);

    while(scanner.hasNext()){
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            //severity是交换机和队列的绑定键，可以有很多个
            String severity = "apple";
            String severity2 = "banana";
            String message = scanner.nextLine();

            channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes("UTF-8"));
            channel.basicPublish(EXCHANGE_NAME, severity2, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + severity + "':'" + message + "'");
            System.out.println(" [x] Sent '" + severity2 + "':'" + message + "'");
        }
    }
    }

  //..
}