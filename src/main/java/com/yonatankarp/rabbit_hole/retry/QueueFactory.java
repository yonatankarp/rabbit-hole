package com.yonatankarp.rabbit_hole.retry;

import com.yonatankarp.rabbit_hole.exceptions.ExchangeException;
import com.yonatankarp.rabbit_hole.exceptions.QueueConfigException;
import com.yonatankarp.rabbit_hole.retry.topic.TopicQueueConfig;
import com.yonatankarp.rabbit_hole.retry.topic.TopicRetryBuilder;
import com.yonatankarp.rabbit_hole.utils.ContextUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
* The interface of the integrators with this library. This factory is the only entry point to the
* library and it should decide how to instantiate all beans according to the given queue
* configurations it receives.
*/
public class QueueFactory {

    private final ContextUtils contextUtils;
    private final ConnectionFactory connectionFactory;
    private final Map<Class<? extends QueueConfig>, RetryBuilder> typeBuilder = new HashMap<>();

    /**
    * Creates a new QueueFactory object.
    *
    * @param contextUtils - application context utilities class to register beans.
    * @param connectionFactory - rabbitmq connection factory instance
    */
    @Autowired
    public QueueFactory(final ContextUtils contextUtils, final ConnectionFactory connectionFactory) {
        this.contextUtils = contextUtils;
        this.connectionFactory = connectionFactory;
        buildTypeBuilderMapping();
    }

    private void buildTypeBuilderMapping() {
        this.typeBuilder.put(
                TopicQueueConfig.class, new TopicRetryBuilder(contextUtils, connectionFactory));
    }

    /**
    * Creates the retry mechanism according to the given configs.
    *
    * @param exchangeName - the name of the main exchange to create
    * @param configs - a list of queues that needs to be created and associated with the exchange
    * @throws com.yonatankarp.rabbit_hole.exceptions.QueueConfigException - if the configuration list
    *     is empty, null or mixed of multiple QueueConfig types
    */
    public void createQueues(final String exchangeName, final List<? extends QueueConfig> configs) {
        validateExchangeName(exchangeName);
        validateConfigs(configs);

        final var clazz = typeBuilder.get(configs.get(0).getClass());

        final var retryBuilder =
                Optional.ofNullable(clazz)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                String.format("Required QueueConfig %s is not supported", clazz)));

        retryBuilder.createQueues(exchangeName, configs);
    }

    private void validateExchangeName(final String exchangeName) {
        if (exchangeName == null || exchangeName.isBlank()) {
            ExchangeException.invalidExchangeNameException();
        }
    }

    private void validateConfigs(final List<? extends QueueConfig> configs) {
        checkIfNullOrEmpty(configs);
        checkAllConfigsOfSameType(configs);
    }

    private void checkIfNullOrEmpty(final List<? extends QueueConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            QueueConfigException.emptyConfigurationException();
        }
    }

    private void checkAllConfigsOfSameType(final List<? extends QueueConfig> configs) {
        // We do not check for NPE as we assume that checkIfNullOrEmpty() was called beforehand
        final var firstQueueType = configs.get(0).getClass();
        final var nonMatchingConfigs =
                configs.stream()
                        .filter(queueConfig -> !firstQueueType.equals(queueConfig.getClass()))
                        .count();

        if (nonMatchingConfigs > 0) {
            QueueConfigException.mixedExchangeTypesException();
        }
    }
}
