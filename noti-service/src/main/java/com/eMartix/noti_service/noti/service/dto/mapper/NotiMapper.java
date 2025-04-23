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
        NotiDto notiDto = mapper.map(notification, NotiDto.class);
        return notiDto;
    }

    public Notification mapToNotiEntity(NotiRequestDto requestDto){
        Notification notification = mapper.map(requestDto, Notification.class);
        return notification;
    }

    public NotificationResponse mapToResponseDto(Notification notification){
        NotificationResponse notiResponseDto = mapper.map(notification, NotificationResponse.class);
        return notiResponseDto;
    }

    public Notification mapToResponseEntity(NotiRequestDto notiResponseDto){
        Notification notification = mapper.map(notiResponseDto, Notification.class);
        return notification;
    }
}
