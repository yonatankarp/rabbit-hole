package com.yonatankarp.rabbit_hole.retry.exchanges.topic

import com.yonatankarp.rabbit_hole.retry.QueueConfig
import com.yonatankarp.rabbit_hole.retry.RetryBuilder
import com.yonatankarp.rabbit_hole.utils.ContextUtils
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate

/**
 * A RabbitMQ Topic exchange retry mechanism builder.
 */
class TopicRetryBuilder constructor(
    private val contextUtil: ContextUtils,
    private val connectionFactory: ConnectionFactory
) : RetryBuilder() {

    override fun createQueues(exchangeName: String, queueConfigs: List<QueueConfig>) {
        val mainExchange = createTopicExchange(exchangeName)
        contextUtil.registerTopicExchange(mainExchange)

        val deadLetterQueueRabbitTemplate = createDeadLetterQueueRabbitTemplate(mainExchange.name)
        contextUtil.registerRabbitTemplate(DEAD_LETTER_RABBIT_TEMPLATE_BEAN_NAME, deadLetterQueueRabbitTemplate)

        queueConfigs.forEach { setupEventsRetryBeans(mainExchange, (it as TopicQueueConfig)) }
    }

    private fun setupEventsRetryBeans(exchange: TopicExchange, topicQueueConfig: TopicQueueConfig) {
        val queueName = topicQueueConfig.queueName
        val routingKey = topicQueueConfig.queueRoutingKey
        val retryQueueName = queueName + RETRY_QUEUES_SUFFIX
        val deadLetterQueueName = queueName + DEAD_LETTER_QUEUES_SUFFIX
        val toRetryQueueRoutingKey = "send-to-retry-queue.$retryQueueName"
        val sendToMainQueueRoutingKey = "send-to-main-queue.$queueName"
        val toDeadLetterQueueRoutingKey = "send-to-dead-letter-queue.$queueName"

        // Main listening queue with dead letter routing to retry queue.
        val mainQueue = createQueue(queueName, exchange.name, toRetryQueueRoutingKey)
        contextUtil.registerQueue(mainQueue)

        // Binding in order that we receive the event coming from the exchange in the main queue
        contextUtil.registerBinding(
            queueName + INCOMING_MESSAGE_BINDING_SUFFIX,
            createBinding(mainQueue, exchange, routingKey)
        )

        // Retry queue with dead letter routing back to main queue. The TTL cannot be changed without
        // recreating existing queues.
        val retryQueue =
            createQueue(retryQueueName, exchange.name, sendToMainQueueRoutingKey, topicQueueConfig.retryTTL)
        contextUtil.registerQueue(retryQueue)

        // Binding in order to have the messages ending up in the retry queue.
        contextUtil.registerBinding(
            queueName + RETRY_MESSAGE_BINDING_SUFFIX,
            createBinding(retryQueue, exchange, toRetryQueueRoutingKey)
        )

        // Binding in order to have the message ending up back in main queue to be retried
        contextUtil.registerBinding(
            queueName + TO_MAIN_QUEUE_BINDING_SUFFIX,
            createBinding(mainQueue, exchange, sendToMainQueueRoutingKey)
        )

        // Create dead Letter Queue
        val deadLetterQueue = createDeadLetterQueue(deadLetterQueueName)
        contextUtil.registerQueue(deadLetterQueue)

        // Create binding so message end up in the dead letter queue.
        contextUtil.registerBinding(
            queueName + DEAD_LETTER_BINDING_SUFFIX,
            createBinding(deadLetterQueue, exchange, toDeadLetterQueueRoutingKey)
        )
    }

    private fun createTopicExchange(exchangeName: String) = TopicExchange(exchangeName)

    private fun createDeadLetterQueueRabbitTemplate(exchangeName: String) =
        RabbitTemplate(connectionFactory).let {
            it.exchange = exchangeName
            it
        }

    private fun createQueue(
        name: String,
        retryExchangeName: String,
        deadLetterRoutingKey: String,
        ttl: Int? = null
    ) = QueueBuilder.durable(name)
        .withArgument(DEAD_LETTER_EXCHANGE_KEY, retryExchangeName)
        .withArgument(DEAD_LETTER_ROUTING_KEY, deadLetterRoutingKey)
        .let {
            if (ttl != null) it.withArgument(TIME_TO_LIVE_KEY, ttl)
            it // We're returning it, so we can call the builder bellow
        }.build()

    private fun createDeadLetterQueue(name: String) = Queue(name)

    private fun createBinding(queue: Queue, exchange: TopicExchange, routingKey: String) =
        BindingBuilder.bind(queue).to(exchange).with(routingKey)
}

/**
 * A class that holds the configuration per queue that needs to be created with a retry mechanism
 * attached to it.
 */
data class TopicQueueConfig(
    /** The name of the main queue to create. */
    val queueName: String,
    /** The routing key for the main queue. */
    val queueRoutingKey: String,
    /**
     * The TTL (time to live) that the message will wait in the retry queue before returning to the
     * main queue in milliseconds. Once this value is set it cannot be changed.
     */
    val retryTTL: Int
) : QueueConfig
