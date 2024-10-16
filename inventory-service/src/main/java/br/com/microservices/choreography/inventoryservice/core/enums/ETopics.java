package br.com.microservices.choreography.inventoryservice.core.enums;

import lombok.Getter;

@Getter
public enum ETopics {

    INVENTORY_FAIL("inventory-fail"),
    PAYMENT_FAIL("payment-fail"),
    NOTIFY_ENDING("notify-ending");

    private final String topic;

    ETopics(String topic) {
        this.topic = topic;
    }
}
