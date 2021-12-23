package com.yonatankarp.rabbit_hole.demo_app

import com.yonatankarp.rabbit_hole.retry.QueueFactory
import com.yonatankarp.rabbit_hole.retry.topic.TopicQueueConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration


@Configuration
class DemoConfig {
    @Autowired
    fun createRetryQueues(factory: QueueFactory) {
        // TODO: add named parameters when possible:
        val config = listOf(TopicQueueConfig(
                QUEUE_NAME,
                ROUTING_KEY,
                RETRY_TTL_IN_MILLISECONDS
            ))
        factory.createQueues(EXCHANGE_NAME, config)
    }

    companion object {
        const val EXCHANGE_NAME = "myExchange"
        const val QUEUE_NAME = "myQueue"
        const val ROUTING_KEY = "my.routing.key"

        // This value can be set only on the creating of the queue, in order to change it later on there's a need to delete
        // the queue completely and re-run the project
        private const val RETRY_TTL_IN_MILLISECONDS = 5000
    }
}
