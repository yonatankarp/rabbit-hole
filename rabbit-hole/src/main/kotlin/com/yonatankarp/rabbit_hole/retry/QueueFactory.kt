package com.yonatankarp.rabbit_hole.retry

import com.yonatankarp.rabbit_hole.exception.ExchangeException.Companion.invalidExchangeNameException
import com.yonatankarp.rabbit_hole.exception.QueueConfigException
import com.yonatankarp.rabbit_hole.exception.QueueConfigException.Companion.emptyConfigurationException
import com.yonatankarp.rabbit_hole.exception.QueueConfigException.Companion.mixedExchangeTypesException
import com.yonatankarp.rabbit_hole.retry.exchanges.topic.TopicQueueConfig
import com.yonatankarp.rabbit_hole.retry.exchanges.topic.TopicRetryBuilder
import com.yonatankarp.rabbit_hole.utils.ContextUtils
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import java.util.*

/** An interface represents a queue configuration object for RabbitMQ. */
interface QueueConfig

/**
 * The interface of the integrators with this library. This factory is the only entry point to the
 * library, and it should decide how to instantiate all beans according to the given queue
 * configurations it receives.
 *
 * @param contextUtils - application context utilities class to register beans.
 * @param connectionFactory - rabbitmq connection factory instance
 */
class QueueFactory constructor(
    private val contextUtils: ContextUtils,
    private val connectionFactory: ConnectionFactory,
) {
    private val typeBuilder = mutableMapOf<Class<out QueueConfig>, RetryBuilder>()

    init {
        buildTypeBuilderMapping()
    }

    private fun buildTypeBuilderMapping() {
        typeBuilder[TopicQueueConfig::class.java] = TopicRetryBuilder(contextUtils, connectionFactory)
    }

    /**
     * Creates the retry mechanism according to the given configs.
     *
     * @param exchangeName - the name of the main exchange to create
     * @param configs - a list of queues that needs to be created and associated with the exchange
     * @throws QueueConfigException - if the configuration list
     * is empty, null or mixed of multiple QueueConfig types
     */
    fun createQueues(exchangeName: String?, configs: List<QueueConfig>?) {
        exchangeName.validate()
        configs.validate()
        val clazz = typeBuilder[configs!!.first().javaClass]
        val retryBuilder = Optional
            .ofNullable(clazz)
            .orElseThrow { IllegalArgumentException("Required QueueConfig $clazz is not supported") }
        retryBuilder.createQueues(exchangeName!!, configs)
    }

    private fun String?.validate() =
        if (this.isNullOrBlank()) invalidExchangeNameException()
        else Unit

    private fun List<QueueConfig>?.validate() {
        if (this.isNullOrEmpty()) emptyConfigurationException()

        val nonMatchingConfigs = this!!.stream()
            .filter { this.first().javaClass != it.javaClass }
            .count()
        if (nonMatchingConfigs > 0) mixedExchangeTypesException()
    }
}
