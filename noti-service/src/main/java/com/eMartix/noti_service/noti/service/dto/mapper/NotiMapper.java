package com.eMartix.noti_service.noti.service.dto.mapper;

import com.eMartix.noti_service.noti.service.dto.model.NotiDto;
import com.eMartix.noti_service.noti.service.dto.request.NotiRequestDto;
import com.eMartix.noti_service.noti.service.dto.response.NotificationResponse;
import com.eMartix.noti_service.noti.service.entity.Notification;
import lombok.AllArgsConstructor;
import org.mapstruct.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Mapper(componentModel = "spring")
public class NotiMapper {
    private ModelMapper mapper;

    public NotiDto mapToDto(Notification notification){
        return mapper.map(notification, NotiDto.class);
    }

    public Notification mapToNotiEntity(NotiRequestDto requestDto){
        return mapper.map(requestDto, Notification.class);
    }

    public NotificationResponse mapToResponseDto(Notification notification){
        return mapper.map(notification, NotificationResponse.class);
    }

    public Notification mapToResponseEntity(NotiRequestDto notiResponseDto){
        return mapper.map(notiResponseDto, Notification.class);
    }
}
