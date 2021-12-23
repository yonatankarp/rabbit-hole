package com.yonatankarp.rabbit_hole.retry

/**
 * An abstract implementation of a retry mechanism. This class would be implemented by each
 * of the different exchanges that are going to be supported by the library
 * (e.g. topic exchange, fanout exchange)
 */
abstract class RetryBuilder {
    /**
     * This method generates all the required beans to support the retry mechanism, assign them as
     * needed, and register them into the application context.
     *
     * @param exchangeName - the name of the exchange to build the queues to
     * @param queueConfigs - a list of 1 or more queues that should be bind to the exchange with the
     * retry mechanism.
     * @throws com.yonatankarp.rabbit_hole.exception.QueueConfigException - if the configuration list
     * is empty, null or mixed of multiple QueueConfig types
     */
    abstract fun createQueues(exchangeName: String, queueConfigs: List<QueueConfig>)

    companion object {

        /** The suffix attached the name of to each retry queue */
        const val RETRY_QUEUES_SUFFIX = ".retry"

        /** The suffix attached to the name of each dead letter queue */
        const val DEAD_LETTER_QUEUES_SUFFIX = ".dead-letter"

        /** The suffix attached to each binding between the exchange and the main queue */
        const val INCOMING_MESSAGE_BINDING_SUFFIX = "IncomingNewMessages"

        /** The suffix attached to each binding between the queue and the retry queue. */
        const val RETRY_MESSAGE_BINDING_SUFFIX = "MessageToRetryQueue"

        /** The suffix attached to each binding between the retry queue and the exchange. */
        const val TO_MAIN_QUEUE_BINDING_SUFFIX = "AfterRetryMessageBackToMainQueue"

        /** The suffix attached to each binding between the exchange and the dead letter queue. */
        const val DEAD_LETTER_BINDING_SUFFIX = "MessagesToDeadLetterQueue"

        /** The suffix of the RabbitTemplate bean name that will be created for the required exchange. */
        const val DEAD_LETTER_RABBIT_TEMPLATE_BEAN_NAME = "deadLetterRabbitTemplate"

        /** The queue key name to set the exchange. */
        const val DEAD_LETTER_EXCHANGE_KEY = "x-dead-letter-exchange"

        /** The queue key name to set the dead-letter queue. */
        const val DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key"

        /** The queue key name to set the TTL of the queue. */
        const val TIME_TO_LIVE_KEY = "x-message-ttl"

        /** The routing key template for sending a message from the main queue to the retry queue. */
        const val TO_RETRY_QUEUE_ROUTING_KEY_FORMAT = "send-to-retry-queue.%s"

        /**
         * The routing key template for sending a message from the retry queue back to the main queue
         * after the TTL was passed.
         */
        const val TO_MAIN_QUEUE_ROUTING_KEY_FORMAT = "send-to-main-queue.%s"

        /**
         * The routing key template for sending message to the dead letter queue after the amount of
         * retries has reached.
         */
        const val TO_DEAD_LETTER_QUEUE_ROUTING_KEY_FORMAT = "send-to-dead-letter-queue.%s"
    }
}
