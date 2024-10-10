package br.com.microservices.orchestrated.paymentservice.core.service;

import br.com.microservices.orchestrated.paymentservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.paymentservice.core.dto.Event;
import br.com.microservices.orchestrated.paymentservice.core.dto.OrderProducts;
import br.com.microservices.orchestrated.paymentservice.core.model.Payment;
import br.com.microservices.orchestrated.paymentservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.paymentservice.core.repository.PaymentRepository;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    private static final Double REDUCE_SUM_VALUE = 0.0;

    private final JsonUtil jsonUtil;
    private final KafkaProducer kafkaProducer;

    private final PaymentRepository paymentRepository;

    public void realizePayment(Event event) {
        try {
            checkCurrentValidation(event);
            createPendingEvent(event);
        } catch (Exception ex) {
            log.error("Error trying to make payment: ", ex);
        }

        kafkaProducer.sendEvent(jsonUtil.toJson(event));
    }

    private void checkCurrentValidation(Event event) {

        if (paymentRepository.existsByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())) {
            throw new ValidationException("There's another transactionId for this validation!");
        }
    }

    private void createPendingEvent(Event event) {
        Double totalAmount = calculateTotalAmount(event);
        Integer totalItems = calculateTotalItems(event);

        Payment payment = Payment.builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();

        save(payment);
        setEventAmountItems(event, payment);
    }

    private Integer calculateTotalItems(Event event) {
        return event.getPayload()
                .getProducts()
                .stream()
                .map(OrderProducts::getQuantity)
                .reduce(REDUCE_SUM_VALUE.intValue(), Integer::sum);
    }

    private Double calculateTotalAmount(Event event) {
        return event.getPayload()
                .getProducts()
                .stream()
                .map(orderProducts -> orderProducts.getQuantity() * orderProducts.getProduct().getUnitValue())
                .reduce(REDUCE_SUM_VALUE, Double::sum);
    }

    private void setEventAmountItems(Event event, Payment payment) {
        event.getPayload().setTotalAmount(payment.getTotalAmount());
        event.getPayload().setTotalItems(payment.getTotalItems());
    }

    private void save(Payment payment) {
        paymentRepository.save(payment);
    }
}
