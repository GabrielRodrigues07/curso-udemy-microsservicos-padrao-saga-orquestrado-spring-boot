package br.com.microservices.choreography.paymentservice.core.service;

import br.com.microservices.choreography.paymentservice.config.exception.ValidationException;
import br.com.microservices.choreography.paymentservice.core.dto.Event;
import br.com.microservices.choreography.paymentservice.core.dto.History;
import br.com.microservices.choreography.paymentservice.core.dto.OrderProducts;
import br.com.microservices.choreography.paymentservice.core.enums.EPaymentStatus;
import br.com.microservices.choreography.paymentservice.core.model.Payment;
import br.com.microservices.choreography.paymentservice.core.producer.KafkaProducer;
import br.com.microservices.choreography.paymentservice.core.repository.PaymentRepository;
import br.com.microservices.choreography.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.microservices.choreography.paymentservice.core.enums.EPaymentStatus.REFUND;
import static br.com.microservices.choreography.paymentservice.core.enums.ESagaStatus.*;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    private static final Double REDUCE_SUM_VALUE = 0.0;
    private static final Double MIN_AMOUNT_AVAILABLE_VALUE = 0.1;

    private final JsonUtil jsonUtil;
    private final KafkaProducer kafkaProducer;

    private final PaymentRepository paymentRepository;

    public void realizePayment(Event event) {
        try {
            checkCurrentValidation(event);
            createPendingEvent(event);
            Payment payment = findByOrderIdAndTransactionId(event);
            validateAmount(payment.getTotalAmount());
            changePaymentToSuccess(payment);
            handleSuccess(event);

        } catch (Exception ex) {
            log.error("Error trying to make payment: ", ex);

            handleFailCurrentNotExecuted(event, ex.getMessage());
        }

        kafkaProducer.sendEvent(jsonUtil.toJson(event), "test");
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

    private void changePaymentToSuccess(Payment payment) {
        payment.setStatus(EPaymentStatus.SUCCESS);
        save(payment);
    }

    private void validateAmount(Double totalAmount) {
        if (totalAmount < MIN_AMOUNT_AVAILABLE_VALUE)
            throw new ValidationException("The minimum amount available is ".concat(MIN_AMOUNT_AVAILABLE_VALUE.toString()));
    }

    private void handleSuccess(Event event) {

        event.setStatus(SUCCESS);
        event.setSource(CURRENT_SOURCE);

        addHistory(event, "Payment realized successfully!");

    }

    private void addHistory(Event event, String message) {
        var history = History.builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        event.addToHistory(history);
    }

    public void realizeRefund(Event event) {
        event.setStatus(FAIL);
        event.setSource(CURRENT_SOURCE);
        try {
            changePaymentStatusToRefund(event);
            addHistory(event, "Rollback executed for payment!");
        } catch (Exception ex) {
            addHistory(event, "Rollback not executed for payment: ".concat(ex.getMessage()));
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(event), "test");
    }

    private void changePaymentStatusToRefund(Event event) {
        Payment payment = findByOrderIdAndTransactionId(event);
        payment.setStatus(REFUND);
        setEventAmountItems(event, payment);
        save(payment);
    }

    private void handleFailCurrentNotExecuted(Event event, String errorMessage) {

        event.setStatus(ROLLBACK_PENDING);
        event.setSource(CURRENT_SOURCE);

        addHistory(event, "Fail to realize payment: ".concat(errorMessage));
    }

    private Payment findByOrderIdAndTransactionId(Event event) {
        return paymentRepository.findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())
                .orElseThrow(() -> new ValidationException(String.format("Payment not found by orderId %s and TransactionId %s", event.getPayload().getId(), event.getTransactionId())));
    }

    private void save(Payment payment) {
        paymentRepository.save(payment);
    }
}
