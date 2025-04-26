package com.eMartix.noti_service.noti.service.consumer;

import com.eMartix.noti_service.noti.service.config.RabbitMQConfig;
import com.eMartix.noti_service.noti.service.dto.model.MailRequestDto;
import com.eMartix.noti_service.noti.service.service.NotiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final NotiService notiService;

    @RabbitListener(
            queues = RabbitMQConfig.EMAIL_QUEUE,
            containerFactory = "rabbitListenerContainerFactoryCustom" // RẤT QUAN TRỌNG
    )
    public void consumeSendEmail(MailRequestDto mailRequestDto) {
        notiService.sendEmail(mailRequestDto.getTo(), mailRequestDto.getSubject(), mailRequestDto.getBody());
        log.info("Email sent to: {} successfully!", mailRequestDto.getTo());
    }
}
