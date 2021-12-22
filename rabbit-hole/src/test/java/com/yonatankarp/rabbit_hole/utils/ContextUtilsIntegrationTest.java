package com.yonatankarp.rabbit_hole.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
public class ContextUtilsIntegrationTest {

    @Autowired private ApplicationContext context;

    @Autowired private ContextUtils contextUtils;
    @Autowired private ConnectionFactory connectionFactory;

    @Test
    void testRegisterQueue() {
        // Given a queue
        final var queue = new Queue("testQueue");

        // When we call registry method
        contextUtils.registerQueue(queue);

        // Then we expect the queue to be available in the context
        final var actual = context.getBean("testQueue", Queue.class);
        assertNotNull(actual);
        assertEquals(queue.getName(), actual.getName());
    }

    @Test
    void testRegisterTopicExchange() {
        // Given a topic exchange
        final var exchange = new TopicExchange("testExchange");

        // When we call registry method
        contextUtils.registerTopicExchange(exchange);

        // Then we expect the correct exchange to be available in the context
        final var actual = context.getBean("testExchange", TopicExchange.class);
        assertNotNull(actual);
        assertEquals(exchange.getName(), actual.getName());
        assertEquals(exchange.getType(), actual.getType());
    }

    @Test
    void testRegisterBinding() {
        // Given a valid exchange, queue and binding
        final var queue = new Queue("test");
        final var exchange = new TopicExchange("testExchange");
        final var routingKey = "routing-key";

        final var binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);

        // When we call registry method
        contextUtils.registerBinding("testBinding", binding);

        // Then we expect the queue to be available in the context
        final var actual = context.getBean("testBinding", Binding.class);
        assertNotNull(actual);
        assertEquals(binding.getExchange(), exchange.getName());
        assertEquals(binding.getRoutingKey(), routingKey);
    }

    @Test
    void testRegisterRabbitTemplate() {
        // Given a rabbit template
        final var rabbitTemplate = new RabbitTemplate(connectionFactory);

        // When we call registry method
        contextUtils.registerRabbitTemplate("myRabbitTemplate", rabbitTemplate);

        // Then we expect the queue to be available in the context
        final var actual = context.getBean("myRabbitTemplate", RabbitTemplate.class);
        assertNotNull(actual);
        assertEquals(rabbitTemplate, actual);
    }
}
