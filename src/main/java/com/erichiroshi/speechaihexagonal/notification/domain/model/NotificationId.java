package com.erichiroshi.speechaihexagonal.notification.domain.model;

import java.util.UUID;

public record NotificationId(UUID id) {
    public NotificationId() {
        this(UUID.randomUUID());
    }
}
