package br.com.microservices.orchestrated.inventoryservice.core.consumer;

import br.com.microservices.orchestrated.inventoryservice.core.dto.Event;
import br.com.microservices.orchestrated.inventoryservice.core.service.InventoryService;
import br.com.microservices.orchestrated.inventoryservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class InventoryValidationConsumer {

    private final JsonUtil jsonUtil;

    private final InventoryService inventoryService;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.inventory-success}")
    public void consumerSuccessEvent(String payload) {
        log.info("Receiving success notification event {} from inventory-success topic", payload);

        Event event = jsonUtil.toEvent(payload);

        inventoryService.updateInventory(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.inventory-fail}")
    public void consumerFailEvent(String payload) {
        log.info("Receiving rollback notification event {} from inventory-fail topic", payload);

        Event event = jsonUtil.toEvent(payload);

        inventoryService.rollbackInventory(event);
    }
}
