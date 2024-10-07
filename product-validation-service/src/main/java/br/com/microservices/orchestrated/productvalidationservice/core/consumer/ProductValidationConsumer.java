package br.com.microservices.orchestrated.productvalidationservice.core.consumer;

import br.com.microservices.orchestrated.productvalidationservice.core.dto.Event;
import br.com.microservices.orchestrated.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ProductValidationConsumer {

    private final JsonUtil jsonUtil;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.product-validation-success}")
    public void consumerSuccessEvent(String payload) {
        log.info("Receiving success notification event {} from product-validation-success topic", payload);

        Event event = jsonUtil.toEvent(payload);

        log.info(event.toString());
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.product-validation-fail}")
    public void consumerFailEvent(String payload) {
        log.info("Receiving rollback notification event {} from product-validation-fail topic", payload);

        Event event = jsonUtil.toEvent(payload);

        log.info(event.toString());
    }
}
