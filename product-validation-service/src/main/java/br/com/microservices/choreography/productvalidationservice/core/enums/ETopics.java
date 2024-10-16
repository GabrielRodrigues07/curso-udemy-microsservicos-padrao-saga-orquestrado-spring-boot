package br.com.microservices.choreography.productvalidationservice.core.enums;

import lombok.Getter;

@Getter
public enum ETopics {

    PRODUCT_VALIDATION_FAIL("product-validation-fail"),
    NOTIFY_ENDING("notify-ending"),
    PAYMENT_START("payment-start");

    private final String topic;

    ETopics(String topic) {
        this.topic = topic;
    }
}
