package com.eMartix.noti_service.noti.service.subscribe;

import com.eMartix.noti_service.noti.service.config.RabbitMQConfig;
import com.eMartix.noti_service.noti.service.dto.model.MailRequestDto;
import com.eMartix.noti_service.noti.service.dto.model.OrderEventDto;
import com.eMartix.noti_service.noti.service.service.NotiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSubscribe {

    private final NotiService notiService;

    @RabbitListener(
            queues = RabbitMQConfig.EMAIL_QUEUE,
            containerFactory = "rabbitListenerContainerFactoryCustom" // RẤT QUAN TRỌNG
    )
    public void consumeSendEmail(MailRequestDto mailRequestDto) {
        notiService.sendEmail(mailRequestDto.getTo(), mailRequestDto.getSubject(), mailRequestDto.getBody());
        log.info("Email sent to: {} successfully!", mailRequestDto.getTo());
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeOrderPaid(OrderEventDto orderEventDto) {
        notiService.sendEmail(orderEventDto.getEmail(), "order status is successfully", "Your order with ID: " + orderEventDto.getOrderId() + " has been paid successfully.");
        log.info("Order paid to: {} successfully!", orderEventDto.getOrderId());
    }
}
