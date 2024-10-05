package br.com.microservices.orchestrated.orchestratorservice.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Order {

    private String id;
    private List<OrderProducts> products;
    private LocalDateTime createAt;
    private String transactionId;
    private Double totalAmount;
    private Integer totalItems;
}
