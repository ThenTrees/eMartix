package com.eMartix.noti_service.noti.service.entity;

import com.eMartix.commons.id.GeneratedID;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedID
    @Column(name = "id", nullable = false)
    private Long id;
    private String title;
    private LocalDateTime timestamp;
    private String message;

}
