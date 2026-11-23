package ro.utcluj.cs.authservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.sync}")
    private String syncQueueName;

    @Value("${rabbitmq.exchange.sync}")
    private String syncExchangeName;

    @Bean
    public Queue syncQueue() {
        return new Queue(syncQueueName, true);
    }

    @Bean
    public FanoutExchange syncExchange() {
        return new FanoutExchange(syncExchangeName);
    }

    @Bean
    public Binding syncBinding(Queue syncQueue, FanoutExchange syncExchange) {
        return BindingBuilder.bind(syncQueue).to(syncExchange);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
