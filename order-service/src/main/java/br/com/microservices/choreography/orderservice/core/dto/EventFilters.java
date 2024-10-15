package br.com.microservices.choreography.orderservice.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventFilters {

    private String orderId;
    private String transactionId;
}
