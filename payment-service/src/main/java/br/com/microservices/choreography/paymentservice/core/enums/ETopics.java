package br.com.microservices.choreography.paymentservice.core.enums;

import lombok.Getter;

@Getter
public enum ETopics {

    PAYMENT_FAIL("payment-fail"),
    PRODUCT_VALIDATION_FAIL("product-validation-fail"),
    INVENTORY_START("inventory-start");

    private final String topic;

    ETopics(String topic) {
        this.topic = topic;
    }
}
