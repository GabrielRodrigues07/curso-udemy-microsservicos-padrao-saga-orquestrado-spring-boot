package br.com.microservices.choreography.paymentservice.core.consumer;

import br.com.microservices.choreography.paymentservice.core.dto.Event;
import br.com.microservices.choreography.paymentservice.core.service.PaymentService;
import br.com.microservices.choreography.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class PaymentValidationConsumer {

    private final JsonUtil jsonUtil;
    private final PaymentService paymentService;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.payment-success}")
    public void consumerSuccessEvent(String payload) {
        log.info("Receiving success notification event {} from payment-success topic", payload);

        Event event = jsonUtil.toEvent(payload);

        paymentService.realizePayment(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.payment-fail}")
    public void consumerFailEvent(String payload) {
        log.info("Receiving rollback notification event {} from payment-fail topic", payload);

        Event event = jsonUtil.toEvent(payload);

        paymentService.realizeRefund(event);
    }
}
