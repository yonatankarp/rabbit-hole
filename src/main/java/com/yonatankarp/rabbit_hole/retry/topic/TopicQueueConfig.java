package com.yonatankarp.rabbit_hole.retry.topic;

import com.yonatankarp.rabbit_hole.retry.QueueConfig;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Value;

/**
* A class that holds the configuration per queue that needs to be created with a retry mechanism
* attached to it.
*/
@Value
@ToString
@AllArgsConstructor
public class TopicQueueConfig implements QueueConfig {
    /** The name of the main queue to create. */
    String queueName;

    /** The routing key for the main queue. */
    String queueRoutingKey;

    /**
    * The TTL (time to live) that the message will wait in the retry queue before returning to the
    * main queue in milliseconds. Once this value is set it cannot be changed.
    */
    int retryTTL;
}
