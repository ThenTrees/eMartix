package com.eMartix.noti_service.noti.service.service.impl;

import com.eMartix.noti_service.noti.service.dto.mapper.NotiMapper;
import com.eMartix.noti_service.noti.service.dto.model.NotiDto;
import com.eMartix.noti_service.noti.service.dto.request.NotiRequestDto;
import com.eMartix.noti_service.noti.service.dto.response.NotificationResponse;
import com.eMartix.noti_service.noti.service.dto.response.ObjectResponse;
import com.eMartix.noti_service.noti.service.entity.Notification;
import com.eMartix.noti_service.noti.service.repository.NotiRepository;
import com.eMartix.noti_service.noti.service.service.NotiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotiServiceImpl implements NotiService {
    private final NotiMapper notiMapper;
    private final NotiRepository notiRepository;
//    private WebClient webClient;
    private final JavaMailSender mailSender;

    @Override
    public NotiDto getNotiWithProduct(long productId) {
        return null;
    }

    @Override
    public ObjectResponse<NotiDto> getAllNotifications(int pageNo, int pageSize, String sortBy, String sortDir) {
        return null;
    }

    @Override
    public NotiDto getNotificationById(long id) {
        return null;
    }

    @Override
    public NotificationResponse createNotification(NotiRequestDto notiDto) {
        // Chuyển đổi NotiRequestDto thành Notification entity
        Notification notification = notiMapper.mapToNotiEntity(notiDto);
        // Lưu Notification entity vào cơ sở dữ liệu
        Notification savedNotification = notiRepository.save(notification); // chưa lưu được vào db
        return notiMapper.mapToResponseDto(savedNotification);
    }

    @Override
    public NotiDto updateNotification(long notiId, NotiRequestDto notiDto) {
        return null;
    }

    @Override
    public void deleteNotification(long id) {

    }

    @Override
    public ObjectResponse<NotiDto> searchNoti(String title, int pageNo, int pageSize, String sortBy, String sortDir) {
        return null;
    }

    @Override
    public void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("eMartixCo@emt.com"); // Không quan trọng với Mailtrap
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);

        log.info("Notification service - SendMail to {} successfully: ", to);

        NotiRequestDto notificationRequest = NotiRequestDto.builder()
                .title("Send mail to {}"+ to)
                .message("Send mail to {} successfully"+to)
                .build();
        createNotification(notificationRequest);
    }
}
