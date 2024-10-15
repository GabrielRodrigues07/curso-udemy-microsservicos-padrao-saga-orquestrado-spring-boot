package br.com.microservices.choreography.orderservice.core.dto;

import br.com.microservices.choreography.orderservice.core.document.OrderProducts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderRequest {

    private List<OrderProducts> products;
}
