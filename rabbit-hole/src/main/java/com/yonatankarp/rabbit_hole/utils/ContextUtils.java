package com.yonatankarp.rabbit_hole.utils;

import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.support.GenericApplicationContext;

/** A helper class to register objects into the application context */
@AllArgsConstructor
public class ContextUtils {

    private final GenericApplicationContext context;

    /**
    * Register a given TopicExchange into the context.
    *
    * @param exchange - the exchange to register.
    */
    public void registerTopicExchange(final TopicExchange exchange) {
        registerToContext(exchange.getName(), exchange, TopicExchange.class);
    }

    /**
    * Register a Queue into the context.
    *
    * @param queue - the queue to register.
    */
    public void registerQueue(final Queue queue) {
        registerToContext(queue.getName(), queue, Queue.class);
    }

    /**
    * Register a Binding into the context.
    *
    * @param beanName - name of the bean to register
    * @param binding - the binding to register.
    */
    public void registerBinding(final String beanName, final Binding binding) {
        registerToContext(beanName, binding, Binding.class);
    }

    /**
    * Register a RabbitTamplte into the context.
    *
    * @param beanName - name of the bean to register
    * @param rabbitTemplate - the template to register.
    */
    public void registerRabbitTemplate(final String beanName, final RabbitTemplate rabbitTemplate) {
        registerToContext(beanName, rabbitTemplate, RabbitTemplate.class);
    }

    private <T> void registerToContext(final String beanName, final T bean, final Class<T> clazz) {
        context.registerBean(beanName, clazz, () -> bean);
    }
}
