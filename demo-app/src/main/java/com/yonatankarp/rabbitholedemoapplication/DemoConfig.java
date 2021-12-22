package com.yonatankarp.rabbitholedemoapplication;

import java.util.Collections;
import com.yonatankarp.rabbit_hole.retry.QueueFactory;
import com.yonatankarp.rabbit_hole.retry.topic.TopicQueueConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoConfig {


    public static final String EXCHANGE_NAME = "myExchange";
    public static final String QUEUE_NAME = "myQueue";
    public static final String ROUTING_KEY = "my.routing.key";
    // This value can be set only on the creating of the queue, in order to change it later on there's a need to delete
    // the queue completely and re-run the project
    private static final int RETRY_TTL_IN_MILLISECONDS = 5000;

    @Autowired
    public void createRetryQueues(final QueueFactory factory) {
        final var config = Collections.singletonList(
                new TopicQueueConfig(QUEUE_NAME, ROUTING_KEY, RETRY_TTL_IN_MILLISECONDS)
                // Multiple queues can be added here per exchange
        );
        factory.createQueues(EXCHANGE_NAME, config);
    }
}
