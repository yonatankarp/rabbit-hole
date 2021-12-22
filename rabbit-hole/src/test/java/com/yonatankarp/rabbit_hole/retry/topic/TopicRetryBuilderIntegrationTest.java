package com.yonatankarp.rabbit_hole.retry.topic;

import java.util.Collections;
import com.yonatankarp.rabbit_hole.configs.RabbitHoleConfig;
import com.yonatankarp.rabbit_hole.config.ComponentTestConfig;
import com.yonatankarp.rabbit_hole.utils.ContextUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.yonatankarp.rabbit_hole.retry.RetryBuilder.DEAD_LETTER_BINDING_SUFFIX;
import static com.yonatankarp.rabbit_hole.retry.RetryBuilder.DEAD_LETTER_EXCHANGE_KEY;
import static com.yonatankarp.rabbit_hole.retry.RetryBuilder.DEAD_LETTER_QUEUES_SUFFIX;
import static com.yonatankarp.rabbit_hole.retry.RetryBuilder.DEAD_LETTER_RABBIT_TEMPLATE_BEAN_NAME;
import static com.yonatankarp.rabbit_hole.retry.RetryBuilder.DEAD_LETTER_ROUTING_KEY;
import static com.yonatankarp.rabbit_hole.retry.RetryBuilder.INCOMING_MESSAGE_BINDING_SUFFIX;
import static com.yonatankarp.rabbit_hole.retry.RetryBuilder.RETRY_MESSAGE_BINDING_SUFFIX;
import static com.yonatankarp.rabbit_hole.retry.RetryBuilder.RETRY_QUEUES_SUFFIX;
import static com.yonatankarp.rabbit_hole.retry.RetryBuilder.TO_DEAD_LETTER_QUEUE_ROUTING_KEY_FORMAT;
import static com.yonatankarp.rabbit_hole.retry.RetryBuilder.TO_MAIN_QUEUE_BINDING_SUFFIX;
import static com.yonatankarp.rabbit_hole.retry.RetryBuilder.TO_MAIN_QUEUE_ROUTING_KEY_FORMAT;
import static com.yonatankarp.rabbit_hole.retry.RetryBuilder.TO_RETRY_QUEUE_ROUTING_KEY_FORMAT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RabbitHoleConfig.class, ComponentTestConfig.class})
public class TopicRetryBuilderIntegrationTest {

    private static final String EXCHANGE_NAME = "myExchange";
    private static final String QUEUE_NAME = "myQueue";
    private static final String ROUTING_KEY = "my.routing.key";

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ContextUtils contextUtil;

    @Autowired
    private ConnectionFactory connectionFactory;

    private TopicRetryBuilder retryBuilder;

    @BeforeEach
    void setUp() {
        retryBuilder = new TopicRetryBuilder(contextUtil, connectionFactory);
    }

    @Test
    void testAllBeansAreCorrectlyCreated() {
        // Given a valid exchange name and topic queue configuration
        final var configs =
                Collections.singletonList(new TopicQueueConfig(QUEUE_NAME, ROUTING_KEY, 1000));

        // When we call create method
        retryBuilder.createQueues(EXCHANGE_NAME, configs);

        // Then we expect the context contains main exchange and its correctly configured
        final var mainExchange = context.getBean(EXCHANGE_NAME, TopicExchange.class);
        assertNotNull(mainExchange);
        assertEquals(mainExchange.getType(), ExchangeTypes.TOPIC);

        // And we expect that the context contains the rabbit template bean and its correctly configured
        final var deadLetterTemplate =
                context.getBean(DEAD_LETTER_RABBIT_TEMPLATE_BEAN_NAME, RabbitTemplate.class);
        assertNotNull(deadLetterTemplate);
        assertEquals(EXCHANGE_NAME, deadLetterTemplate.getExchange());

        // And we expect that the context contains the main queue and its correctly configured
        final var mainQueue = context.getBean(QUEUE_NAME, Queue.class);
        final var mainQueueRoutingKey =
                String.format(TO_RETRY_QUEUE_ROUTING_KEY_FORMAT, QUEUE_NAME + RETRY_QUEUES_SUFFIX);
        assertQueue(mainQueue, QUEUE_NAME, mainQueueRoutingKey);

        // And we expect that the context contains the main queue binding and its correctly configured
        final var mainQueueBinding =
                context.getBean(QUEUE_NAME + INCOMING_MESSAGE_BINDING_SUFFIX, Binding.class);
        assertBinding(mainQueueBinding, ROUTING_KEY, QUEUE_NAME);

        // And we expect that the context contains the retry queue and its correctly configured
        final var retryQueue = context.getBean(QUEUE_NAME + RETRY_QUEUES_SUFFIX, Queue.class);
        final var retryQueueRoutingKey = String.format(TO_MAIN_QUEUE_ROUTING_KEY_FORMAT, QUEUE_NAME);
        assertQueue(retryQueue, QUEUE_NAME + RETRY_QUEUES_SUFFIX, retryQueueRoutingKey);

        // And we expect that the context contains the main queue to retry queue binding and its
        // correctly configured
        final var toRetryQueueBinding =
                context.getBean(QUEUE_NAME + RETRY_MESSAGE_BINDING_SUFFIX, Binding.class);
        assertBinding(
                toRetryQueueBinding,
                String.format(TO_RETRY_QUEUE_ROUTING_KEY_FORMAT, QUEUE_NAME + RETRY_QUEUES_SUFFIX),
                QUEUE_NAME + RETRY_QUEUES_SUFFIX);

        // And we expect that the context contains the retry queue to main queue binding and its
        // correctly configured
        final var toMainQueueBinding =
                context.getBean(QUEUE_NAME + TO_MAIN_QUEUE_BINDING_SUFFIX, Binding.class);
        assertBinding(
                toMainQueueBinding,
                String.format(TO_MAIN_QUEUE_ROUTING_KEY_FORMAT, QUEUE_NAME),
                QUEUE_NAME);

        // And we expect that the context contains the dead letter queue and it's correctly configured
        final var deadLetterQueue =
                context.getBean(QUEUE_NAME + DEAD_LETTER_QUEUES_SUFFIX, Queue.class);
        assertDeadLetterQueue(deadLetterQueue);

        // And we expect that the context contains the binding to dead letter queue and its correctly
        // configured
        final var toDeadLetterQueueBinding =
                context.getBean(QUEUE_NAME + DEAD_LETTER_BINDING_SUFFIX, Binding.class);
        assertBinding(
                toDeadLetterQueueBinding,
                String.format(TO_DEAD_LETTER_QUEUE_ROUTING_KEY_FORMAT, QUEUE_NAME),
                QUEUE_NAME + DEAD_LETTER_QUEUES_SUFFIX);
    }

    private void assertQueue(
            final Queue actualQueue, final String queueName, final String routingKey) {
        assertNotNull(actualQueue);
        assertEquals(queueName, actualQueue.getActualName());
        assertEquals(EXCHANGE_NAME, actualQueue.getArguments().get(DEAD_LETTER_EXCHANGE_KEY));
        assertEquals(routingKey, actualQueue.getArguments().get(DEAD_LETTER_ROUTING_KEY));
    }

    private void assertDeadLetterQueue(final Queue actualQueue) {
        assertNotNull(actualQueue);
        assertEquals("myQueue.dead-letter", actualQueue.getActualName());
    }

    private void assertBinding(
            final Binding actualBinding, final String routingKey, final String queueName) {
        assertNotNull(actualBinding);
        assertEquals(EXCHANGE_NAME, actualBinding.getExchange());
        assertEquals(routingKey, actualBinding.getRoutingKey());
        assertEquals(queueName, actualBinding.getDestination());
        assertEquals(Binding.DestinationType.QUEUE, actualBinding.getDestinationType());
    }
}
