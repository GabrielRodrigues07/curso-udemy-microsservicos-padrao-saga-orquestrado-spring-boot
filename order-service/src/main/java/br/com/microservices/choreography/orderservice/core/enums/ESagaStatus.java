package br.com.microservices.choreography.orderservice.core.enums;

import lombok.Getter;

@Getter
public enum ESagaStatus {

    SUCCESS,
    ROLLBACK_PENDING,
    FAIL
}
