package com.eMartix.authservice.producer;

import com.eMartix.authservice.configuration.RabbitMQConfig;
import com.eMartix.authservice.dto.request.MailRequestDto;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MailProducer {


    private final AmqpTemplate amqpTemplate;

    public MailProducer( @Qualifier("MailRequestDto")AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendMail(MailRequestDto mailRequestDto) {
        amqpTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                mailRequestDto
        );
    }
}
