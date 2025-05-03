package com.eMartix.order_service.publisher;

import com.eMartix.order_service.config.RabbitMQConfig;
import com.eMartix.order_service.dto.request.OrderEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OrderEventPublisher {

//    private final RabbitTemplate rabbitTemplate;
    private final AmqpTemplate amqpTemplate;
    public OrderEventPublisher( @Qualifier("MailRequestDto")AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void publishOrderPaid(OrderEventDto event) {
        amqpTemplate.convertAndSend(
                RabbitMQConfig.ORDER_CREATE_EXCHANGE,   // Exchange name
                RabbitMQConfig.ORDER_ROUTING_KEY,       // Routing key
                event               // Payload (message)
        );
    }

    public void publishOrderCreate(OrderEventDto event) {
        amqpTemplate.convertAndSend(
                RabbitMQConfig.ORDER_CREATE_EXCHANGE,   // Exchange name
                RabbitMQConfig.ORDER_ROUTING_KEY,   // Routing key
                event               // Payload (message)
        );
    }

}
