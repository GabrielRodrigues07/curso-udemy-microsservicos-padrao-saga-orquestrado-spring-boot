package br.com.microservices.orchestrated.orchestratorservice.core.enums;

import lombok.Getter;

@Getter
public enum EEventSource {

    ORCHESTRATOR,
    PRODUCT_VALIDATION_SERVICE,
    PAYMENT_SERVICE,
    INVENTORY_SERVICE
}
