package com.yonatankarp.rabbit_hole.retry;

import java.util.List;

/** An interface of a retry mechanism builder. */
public abstract class RetryBuilder {

    /** The suffix attached the name of to each retry queue */
    public static final String RETRY_QUEUES_SUFFIX = ".retry";

    /** The suffix attached to the name of each dead letter queue */
    public static final String DEAD_LETTER_QUEUES_SUFFIX = ".dead-letter";

    /** The suffix attached to each binding between the exchange and the main queue */
    public static final String INCOMING_MESSAGE_BINDING_SUFFIX = "IncomingNewMessages";

    /** The suffix attached to each binding between the queue and the retry queue. */
    public static final String RETRY_MESSAGE_BINDING_SUFFIX = "MessageToRetryQueue";

    /** The suffix attached to each binding between the retry queue and the exchange. */
    public static final String TO_MAIN_QUEUE_BINDING_SUFFIX = "AfterRetryMessageBackToMainQueue";

    /** The suffix attached to each binding between the exchange and the dead letter queue. */
    public static final String DEAD_LETTER_BINDING_SUFFIX = "MessagesToDeadLetterQueue";

    /** The suffix of the RabbitTemplate bean name that will be created for the required exchange. */
    public static final String DEAD_LETTER_RABBIT_TEMPLATE_BEAN_NAME = "deadLetterRabbitTemplate";

    /** The queue key name to set the exchange. */
    public static final String DEAD_LETTER_EXCHANGE_KEY = "x-dead-letter-exchange";

    /** The queue key name to set the dead-letter queue. */
    public static final String DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    /** The queue key name to set the TTL of the queue. */
    public static final String TIME_TO_LIVE_KEY = "x-message-ttl";

    /** The routing key template for sending a message from the main queue to the retry queue. */
    public static final String TO_RETRY_QUEUE_ROUTING_KEY_FORMAT = "send-to-retry-queue.%s";

    /**
    * The routing key template for sending a message from the retry queue back to the main queue
    * after the TTL was passed.
    */
    public static final String TO_MAIN_QUEUE_ROUTING_KEY_FORMAT = "send-to-main-queue.%s";

    /**
    * The routing key template for sending message to the dead letter queue after the amount of
    * retries have reached.
    */
    public static final String TO_DEAD_LETTER_QUEUE_ROUTING_KEY_FORMAT =
            "send-to-dead-letter-queue.%s";

    /**
    * This method generates all the required beans to support the retry mechanism, assign them as
    * needed, and register them into the application context.
    *
    * @param exchangeName - the name of the exchange to build the queues to
    * @param queueConfigs - a list of 1 or more queues that should be bind to the exchange with the
    *     retry mechanism.
    * @throws com.yonatankarp.rabbit_hole.exceptions.QueueConfigException - if the configuration list
    *     is empty, null or mixed of multiple QueueConfig types
    */
    public abstract void createQueues(
            final String exchangeName, final List<? extends QueueConfig> queueConfigs);
}
