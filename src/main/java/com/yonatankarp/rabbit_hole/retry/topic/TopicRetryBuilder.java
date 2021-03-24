package com.yonatankarp.rabbit_hole.retry.topic;

import com.yonatankarp.rabbit_hole.retry.QueueConfig;
import com.yonatankarp.rabbit_hole.retry.RetryBuilder;
import com.yonatankarp.rabbit_hole.utils.ContextUtils;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/** A RabbitMQ Topic exchange retry mechanism builder. */
@AllArgsConstructor
public class TopicRetryBuilder extends RetryBuilder {

    private final ContextUtils contextUtil;
    private final ConnectionFactory connectionFactory;

    @Override
    public void createQueues(
            final String exchangeName, final List<? extends QueueConfig> topicQueueConfigs) {
        final var mainExchange = createTopicExchange(exchangeName);
        contextUtil.registerTopicExchange(mainExchange);

        // Create the RabbitTemplate for messages that need to be discarded when retry exceeds
        final var deadLetterQueueRabbitTemplate =
                createDeadLetterQueueRabbitTemplate(mainExchange.getName());
        contextUtil.registerRabbitTemplate(
                DEAD_LETTER_RABBIT_TEMPLATE_BEAN_NAME, deadLetterQueueRabbitTemplate);

        topicQueueConfigs.forEach(
                topicQueueConfig ->
                        setupEventsRetryBeans(mainExchange, (TopicQueueConfig) topicQueueConfig));
    }

    private void setupEventsRetryBeans(
            final TopicExchange exchange, final TopicQueueConfig topicQueueConfig) {

        final var queueName = topicQueueConfig.getQueueName();
        final var routingKey = topicQueueConfig.getQueueRoutingKey();

        final var retryQueueName = queueName + RETRY_QUEUES_SUFFIX;
        final var deadLetterQueueName = queueName + DEAD_LETTER_QUEUES_SUFFIX;

        final var toRetryQueueRoutingKey =
                String.format(TO_RETRY_QUEUE_ROUTING_KEY_FORMAT, retryQueueName);
        final var sendToMainQueueRoutingKey =
                String.format(TO_MAIN_QUEUE_ROUTING_KEY_FORMAT, queueName);
        final var toDeadLetterQueueRoutingKey =
                String.format(TO_DEAD_LETTER_QUEUE_ROUTING_KEY_FORMAT, queueName);

        // Main listening queue with dead letter routing to retry queue.
        final var mainQueue = createQueue(queueName, exchange.getName(), toRetryQueueRoutingKey);
        contextUtil.registerQueue(mainQueue);

        // Binding in order that we receive the event coming from the exchange in the main queue
        contextUtil.registerBinding(
                queueName + INCOMING_MESSAGE_BINDING_SUFFIX,
                createBinding(mainQueue, exchange, routingKey));

        // Retry queue with dead letter routing back to main queue. The TTL cannot be changed without
        // recreating existing queues.
        final Queue retryQueue =
                createQueue(
                        retryQueueName,
                        exchange.getName(),
                        sendToMainQueueRoutingKey,
                        topicQueueConfig.getRetryTTL());
        contextUtil.registerQueue(retryQueue);

        // Binding in order to have the messages ending up in the retry queue.
        contextUtil.registerBinding(
                queueName + RETRY_MESSAGE_BINDING_SUFFIX,
                createBinding(retryQueue, exchange, toRetryQueueRoutingKey));

        // Binding in order to have the message ending up back in main queue to be retried
        contextUtil.registerBinding(
                queueName + TO_MAIN_QUEUE_BINDING_SUFFIX,
                createBinding(mainQueue, exchange, sendToMainQueueRoutingKey));

        // Create dead Letter Queue
        final var deadLetterQueue = createDeadLetterQueue(deadLetterQueueName);
        contextUtil.registerQueue(deadLetterQueue);

        // Create binding so message end up in the dead letter queue.
        contextUtil.registerBinding(
                queueName + DEAD_LETTER_BINDING_SUFFIX,
                createBinding(deadLetterQueue, exchange, toDeadLetterQueueRoutingKey));
    }

    private TopicExchange createTopicExchange(final String exchangeName) {
        return new TopicExchange(exchangeName);
    }

    private RabbitTemplate createDeadLetterQueueRabbitTemplate(final String exchangeName) {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setExchange(exchangeName);
        return template;
    }

    private Queue createQueue(
            final String name, final String retryExchangeName, final String deadLetterRoutingKey) {
        return createQueue(name, retryExchangeName, deadLetterRoutingKey, null);
    }

    private Queue createQueue(
            final String name,
            final String retryExchangeName,
            final String deadLetterRoutingKey,
            final Integer ttl) {
        final var queueBuilder =
                QueueBuilder.durable(name)
                        .withArgument(DEAD_LETTER_EXCHANGE_KEY, retryExchangeName)
                        .withArgument(DEAD_LETTER_ROUTING_KEY, deadLetterRoutingKey);

        if (ttl != null) {
            queueBuilder.withArgument(TIME_TO_LIVE_KEY, ttl);
        }

        return queueBuilder.build();
    }

    private Queue createDeadLetterQueue(final String name) {
        return new Queue(name);
    }

    private Binding createBinding(
            final Queue queue, final TopicExchange exchange, final String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}
