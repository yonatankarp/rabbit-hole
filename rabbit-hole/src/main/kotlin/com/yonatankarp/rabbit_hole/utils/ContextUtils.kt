package com.yonatankarp.rabbit_hole.utils

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.support.GenericApplicationContext
import java.util.function.Supplier

class ContextUtils(private val context: GenericApplicationContext) {
    /**
     * Register a given TopicExchange into the context.
     *
     * @param exchange - the exchange to register.
     */
    fun registerTopicExchange(exchange: TopicExchange) =
        registerToContext(exchange.name, exchange, TopicExchange::class.java)

    /**
     * Register a Queue into the context.
     *
     * @param queue - the queue to register.
     */
    fun registerQueue(queue: Queue) = registerToContext(queue.name, queue, Queue::class.java)

    /**
     * Register a Binding into the context.
     *
     * @param beanName - name of the bean to register
     * @param binding - the binding to register.
     */
    fun registerBinding(beanName: String, binding: Binding) = registerToContext(beanName, binding, Binding::class.java)


    /**
     * Register a RabbitTemplate into the context.
     *
     * @param beanName - name of the bean to register
     * @param rabbitTemplate - the template to register.
     */
    fun registerRabbitTemplate(beanName: String, rabbitTemplate: RabbitTemplate) =
        registerToContext(beanName, rabbitTemplate, RabbitTemplate::class.java)

    private fun <T> registerToContext(beanName: String, bean: T, clazz: Class<T>) =
        context.registerBean(beanName, clazz, Supplier { bean })
}
