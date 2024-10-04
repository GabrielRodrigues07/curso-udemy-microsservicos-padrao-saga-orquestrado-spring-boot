package br.com.microservices.orchestrated.orderservice.config.kafka;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "spring.kafka")
@Getter
@Setter
public class KafkaProperties {

    //TODO usar essa classe em todos os serviços e substituir os atributos atuais

    private String bootStrapServers;
    private Map<String, String> topic;
    private Map<String, String> consumer;
}
