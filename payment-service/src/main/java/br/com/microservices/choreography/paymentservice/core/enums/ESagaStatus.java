package br.com.microservices.choreography.paymentservice.core.enums;

import lombok.Getter;

@Getter
public enum ESagaStatus {

    SUCCESS,
    ROLLBACK_PENDING,
    FAIL
}
