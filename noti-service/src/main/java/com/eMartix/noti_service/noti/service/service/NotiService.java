package com.eMartix.noti_service.noti.service.service;

import com.eMartix.noti_service.noti.service.dto.model.NotiDto;
import com.eMartix.noti_service.noti.service.dto.request.NotiRequestDto;
import com.eMartix.noti_service.noti.service.dto.response.NotificationResponse;
import com.eMartix.noti_service.noti.service.dto.response.ObjectResponse;

public interface NotiService {
    NotiDto getNotiWithProduct(long productId);
    ObjectResponse<NotiDto> getAllNotifications(int pageNo, int pageSize, String sortBy, String sortDir);
    NotiDto getNotificationById(long id);
    NotificationResponse createNotification(NotiRequestDto notiDto);
    NotiDto updateNotification(long notiId, NotiRequestDto notiDto);
    void deleteNotification(long id);
    ObjectResponse<NotiDto> searchNoti(String title, int pageNo, int pageSize, String sortBy, String sortDir);
    void sendEmail(String to, String subject, String content);
}
