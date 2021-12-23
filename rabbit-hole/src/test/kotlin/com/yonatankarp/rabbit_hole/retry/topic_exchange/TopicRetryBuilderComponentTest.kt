package com.yonatankarp.rabbit_hole.retry.topic_exchange

import com.yonatankarp.rabbit_hole.config.TestConfig
import com.yonatankarp.rabbit_hole.configs.RabbitHoleConfig
import com.yonatankarp.rabbit_hole.retry.RetryBuilder
import com.yonatankarp.rabbit_hole.retry.topic.TopicQueueConfig
import com.yonatankarp.rabbit_hole.retry.topic.TopicRetryBuilder
import com.yonatankarp.rabbit_hole.utils.ContextUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.Exchange
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [RabbitHoleConfig::class, TestConfig::class])
class TopicRetryBuilderComponentTest(
    @Autowired private val context: ApplicationContext,
    @Autowired private val contextUtil: ContextUtils,
    @Autowired private val connectionFactory: ConnectionFactory
) {
    private lateinit var retryBuilder: TopicRetryBuilder

    @BeforeEach
    fun init() {
        retryBuilder = TopicRetryBuilder(contextUtil, connectionFactory)
    }

    @Test
    fun `should create all beans correctly`() {
        // Given a valid exchange name and topic queue configuration
        val configs = listOf(TopicQueueConfig(QUEUE_NAME, ROUTING_KEY, 1000))

        // When we call create method
        retryBuilder.createQueues(EXCHANGE_NAME, configs)

        // Then we expect the context contains main exchange and its correctly configured
        val mainExchange = context.getBean(EXCHANGE_NAME, TopicExchange::class.java)
        assertIsTopicExchange(mainExchange)

        // And we expect that the context contains the rabbit template bean and its correctly configured
        val deadLetterTemplate =
            context.getBean(RetryBuilder.DEAD_LETTER_RABBIT_TEMPLATE_BEAN_NAME, RabbitTemplate::class.java)
        assertRabbitTemplate(deadLetterTemplate)

        // And we expect that the context contains the main queue and its correctly configured
        val mainQueue = context.getBean(QUEUE_NAME, Queue::class.java)
        assertQueue(mainQueue, QUEUE_NAME, "send-to-retry-queue.${QUEUE_NAME}${RetryBuilder.RETRY_QUEUES_SUFFIX}")

        // And we expect that the context contains the main queue binding and its correctly configured
        val mainQueueBinding =
            context.getBean(QUEUE_NAME + RetryBuilder.INCOMING_MESSAGE_BINDING_SUFFIX, Binding::class.java)
        assertBinding(mainQueueBinding, ROUTING_KEY, QUEUE_NAME)

        // And we expect that the context contains the retry queue and its correctly configured
        val retryQueue = context.getBean(QUEUE_NAME + RetryBuilder.RETRY_QUEUES_SUFFIX, Queue::class.java)
        assertQueue(
            retryQueue, QUEUE_NAME + RetryBuilder.RETRY_QUEUES_SUFFIX, "send-to-main-queue.$QUEUE_NAME"
        )

        // And we expect that the context contains the main queue to retry queue binding and its
        // correctly configured
        val toRetryQueueBinding = context.getBean(
            QUEUE_NAME + RetryBuilder.RETRY_MESSAGE_BINDING_SUFFIX, Binding::class.java
        )
        assertBinding(
            toRetryQueueBinding, "send-to-retry-queue.$QUEUE_NAME${RetryBuilder.RETRY_QUEUES_SUFFIX}",
            QUEUE_NAME + RetryBuilder.RETRY_QUEUES_SUFFIX
        )

        // And we expect that the context contains the retry queue to main queue binding and its
        // correctly configured
        val toMainQueueBinding =
            context.getBean(QUEUE_NAME + RetryBuilder.TO_MAIN_QUEUE_BINDING_SUFFIX, Binding::class.java)
        assertBinding(toMainQueueBinding, "send-to-main-queue.$QUEUE_NAME", QUEUE_NAME)

        // And we expect that the context contains the dead letter queue, and it's correctly configured
        val deadLetterQueue = context.getBean(QUEUE_NAME + RetryBuilder.DEAD_LETTER_QUEUES_SUFFIX, Queue::class.java)
        assertDeadLetterQueue(deadLetterQueue)

        // And we expect that the context contains the binding to dead letter queue and its correctly
        // configured
        val toDeadLetterQueueBinding =
            context.getBean(QUEUE_NAME + RetryBuilder.DEAD_LETTER_BINDING_SUFFIX, Binding::class.java)
        assertBinding(
            toDeadLetterQueueBinding,
            "send-to-dead-letter-queue.$QUEUE_NAME",
            QUEUE_NAME + RetryBuilder.DEAD_LETTER_QUEUES_SUFFIX
        )
    }

    private fun assertIsTopicExchange(exchange: Exchange) {
        assertNotNull(exchange)
        assertEquals(exchange.type, ExchangeTypes.TOPIC)
    }

    private fun assertQueue(
        actualQueue: Queue,
        queueName: String,
        routingKey: String
    ) {
        assertNotNull(actualQueue)
        assertEquals(queueName, actualQueue.actualName)
        assertEquals(EXCHANGE_NAME, actualQueue.arguments[RetryBuilder.DEAD_LETTER_EXCHANGE_KEY])
        assertEquals(routingKey, actualQueue.arguments[RetryBuilder.DEAD_LETTER_ROUTING_KEY])
    }

    private fun assertDeadLetterQueue(actualQueue: Queue) {
        assertNotNull(actualQueue)
        assertEquals("myQueue.dead-letter", actualQueue.actualName)
    }

    private fun assertBinding(
        actualBinding: Binding,
        routingKey: String,
        queueName: String
    ) {
        assertNotNull(actualBinding)
        assertEquals(EXCHANGE_NAME, actualBinding.exchange)
        assertEquals(routingKey, actualBinding.routingKey)
        assertEquals(queueName, actualBinding.destination)
        assertEquals(Binding.DestinationType.QUEUE, actualBinding.destinationType)
    }

    private fun assertRabbitTemplate(template: RabbitTemplate) {
        assertNotNull(template)
        assertEquals(EXCHANGE_NAME, template.exchange)
    }

    companion object {
        private const val EXCHANGE_NAME = "myExchange"
        private const val QUEUE_NAME = "myQueue"
        private const val ROUTING_KEY = "my.routing.key"
    }
}
