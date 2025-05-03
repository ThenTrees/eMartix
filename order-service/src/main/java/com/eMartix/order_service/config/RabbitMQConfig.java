package com.eMartix.order_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_CREATE_EXCHANGE = "order.create.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String ORDER_ROUTING_KEY = "order.created";
    public static final String PAYMENT_QUEUE = "payment.created.queue";
    public static final String NOTIFICATION_QUEUE= "notification.created.queue";

    // Declare Exchange
    @Bean
    public DirectExchange orderCreateExchange() {
        return new DirectExchange(ORDER_CREATE_EXCHANGE, true, false); // Durable = true, không auto-delete
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE, true, false);
    }

    // Declare Queues
    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true, false, false);
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true, false, false);
    }

    // Bind Queues to Exchange with Routing Keys
    @Bean
    public Binding notificationBinding(@Qualifier("notificationQueue") Queue notificationQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(orderExchange)
                .with(ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding bindingPayment(@Qualifier("paymentQueue") Queue paymentQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentQueue)
                .to(paymentExchange)
                .with(ORDER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter(); // Dùng để convert object thành JSON
    }

    @Bean(name = "MailRequestDto")
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}

/**
 * bên phát sự kiện cần chuẩn bị exchange và queue và routing key và payload
 */

/**
 * bên nhận sự kiện cần nhận đúng queue,
 * queue phải đuojwc binding đúng routingkey và exchange
 * routing key có dạng [domain].[action]
 */