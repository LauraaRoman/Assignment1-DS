package ro.utcluj.cs.monitoringservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.sync.host}")
    private String syncHost;
    @Value("${rabbitmq.sync.port}")
    private int syncPort;
    @Value("${rabbitmq.sync.username}")
    private String syncUser;
    @Value("${rabbitmq.sync.password}")
    private String syncPass;

    @Value("${rabbitmq.data.host}")
    private String dataHost;
    @Value("${rabbitmq.data.port}")
    private int dataPort;
    @Value("${rabbitmq.data.username}")
    private String dataUser;
    @Value("${rabbitmq.data.password}")
    private String dataPass;

    @Value("${rabbitmq.queue.sync}")
    private String syncQueue;
    @Value("${rabbitmq.queue.data}")
    private String dataQueue;
    @Value("${rabbitmq.exchange.sync}")
    private String syncExchange;
    @Value("${rabbitmq.exchange.data}")
    private String dataExchange;

    // ── Sync broker (rabbitmq-sync) ──────────────────────────────────────────

    @Primary
    @Bean(name = "syncConnectionFactory")
    public ConnectionFactory syncConnectionFactory() {
        CachingConnectionFactory cf = new CachingConnectionFactory(syncHost, syncPort);
        cf.setUsername(syncUser);
        cf.setPassword(syncPass);
        return cf;
    }

    @Bean(name = "syncRabbitAdmin")
    public RabbitAdmin syncRabbitAdmin(@org.springframework.beans.factory.annotation.Qualifier("syncConnectionFactory") ConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }

    @Bean
    public FanoutExchange syncFanoutExchange() {
        return new FanoutExchange(syncExchange);
    }

    @Bean
    public Queue syncQueueBean() {
        return QueueBuilder.durable(syncQueue).build();
    }

    @Bean
    public Binding syncBinding(Queue syncQueueBean, FanoutExchange syncFanoutExchange) {
        return BindingBuilder.bind(syncQueueBean).to(syncFanoutExchange);
    }

    @Bean(name = "syncListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory syncListenerContainerFactory(
            @org.springframework.beans.factory.annotation.Qualifier("syncConnectionFactory") ConnectionFactory cf) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    // ── Data broker (rabbitmq-data) ──────────────────────────────────────────

    @Bean(name = "dataConnectionFactory")
    public ConnectionFactory dataConnectionFactory() {
        CachingConnectionFactory cf = new CachingConnectionFactory(dataHost, dataPort);
        cf.setUsername(dataUser);
        cf.setPassword(dataPass);
        return cf;
    }

    @Bean(name = "dataRabbitAdmin")
    public RabbitAdmin dataRabbitAdmin(@org.springframework.beans.factory.annotation.Qualifier("dataConnectionFactory") ConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }

    @Bean
    public FanoutExchange dataFanoutExchange() {
        return new FanoutExchange(dataExchange);
    }

    @Bean
    public Queue dataQueueBean() {
        return QueueBuilder.durable(dataQueue).build();
    }

    @Bean
    public Binding dataBinding(Queue dataQueueBean, FanoutExchange dataFanoutExchange) {
        return BindingBuilder.bind(dataQueueBean).to(dataFanoutExchange);
    }

    @Bean(name = "dataListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory dataListenerContainerFactory(
            @org.springframework.beans.factory.annotation.Qualifier("dataConnectionFactory") ConnectionFactory cf) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    // ── Shared ───────────────────────────────────────────────────────────────

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            @org.springframework.beans.factory.annotation.Qualifier("syncConnectionFactory") ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
