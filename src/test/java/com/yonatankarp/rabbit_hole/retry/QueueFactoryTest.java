package com.yonatankarp.rabbit_hole.retry;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.yonatankarp.rabbit_hole.exceptions.ExchangeException;
import com.yonatankarp.rabbit_hole.exceptions.QueueConfigException;
import com.yonatankarp.rabbit_hole.retry.topic.TopicQueueConfig;
import com.yonatankarp.rabbit_hole.utils.ContextUtils;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

public class QueueFactoryTest {

    private static final String EXCHANGE_NAME = "testExchange";

    private ContextUtils contextUtils;
    private ConnectionFactory connectionFactory;
    private QueueFactory factory;

    @BeforeEach
    public void setUp() {
        contextUtils = mock(ContextUtils.class);
        connectionFactory = mock(ConnectionFactory.class);
        factory = new QueueFactory(contextUtils, connectionFactory);
    }

    @Test
    void testNullExchangeNameThrowsError() {
        // Given null exchange name and valid queue config
        final var configs =
                Collections.singletonList(new TopicQueueConfig("testQueue", "testRoutingKey", 1000));

        // When we cal the factory
        // Then we expect exception to be thrown
        assertThrows(ExchangeException.class, () -> factory.createQueues(null, configs));
    }

    @Test
    void testExchangeNameIsEmptyStringThrowsError() {
        // Given empty string as exchange name and valid queue config
        final var configs =
                Collections.singletonList(new TopicQueueConfig("testQueue", "testRoutingKey", 1000));

        // When we cal the factory
        // Then we expect exception to be thrown
        assertThrows(ExchangeException.class, () -> factory.createQueues("", configs));
    }

    @Test
    void testExchangeNameIsOnlySpacesStringThrowsError() {
        // Given only spaces as exchange name and valid queue config
        final var configs =
                Collections.singletonList(new TopicQueueConfig("testQueue", "testRoutingKey", 1000));

        // When we cal the factory
        // Then we expect exception to be thrown
        assertThrows(ExchangeException.class, () -> factory.createQueues("   ", configs));
    }

    @Test
    void testNullQueueConfigThrowsError() {
        // Given null queue config list and a valid exchange name
        // When we call the factory
        // Then we expect exception to be thrown
        assertThrows(QueueConfigException.class, () -> factory.createQueues(EXCHANGE_NAME, null));
    }

    @Test
    void testMixExchangeTypesThrowsError() {
        // Given mixed config list
        final var configs =
                Arrays.asList(
                        new TopicQueueConfig("testQueue", "testRoutingKey", 1000), new DummyQueueConfig());

        // When we call the factory
        // Then we expect exception to be thrown
        assertThrows(QueueConfigException.class, () -> factory.createQueues(EXCHANGE_NAME, configs));
    }

    @Test
    void testUnsupportedTypeThrowsError() {
        // Given unsupported queue config
        final var configs = Collections.singletonList(new DummyQueueConfig());

        // When we call the factory
        // Then we expect exception to be thrown
        assertThrows(
                IllegalArgumentException.class, () -> factory.createQueues(EXCHANGE_NAME, configs));
    }

    @Test
    void testTopicConfiguration() {
        // Given a topic queue configuration
        final var configs =
                Collections.singletonList(new TopicQueueConfig("testQueue", "testRoutingKey", 1000));

        // When we call the factory
        factory.createQueues(EXCHANGE_NAME, configs);

        // Then we expect the context utilities class to be called with the main topic exchange
        // and the retry topic exchange
        verify(contextUtils, times(1)).registerTopicExchange(any());
    }

    /** Dummy class that implements the QueueConfig interface. */
    static class DummyQueueConfig implements QueueConfig {}
}
