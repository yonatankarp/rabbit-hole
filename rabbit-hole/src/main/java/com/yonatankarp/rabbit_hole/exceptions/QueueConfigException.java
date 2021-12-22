package com.yonatankarp.rabbit_hole.exceptions;

/** An exception caused by misconfiguration of the queues. */
public class QueueConfigException extends RuntimeException {

    private QueueConfigException(final String message) {
        super(message);
    }

    /** An exception that should be thrown if no queue config was passed to the library */
    public static void emptyConfigurationException() {
        throw new QueueConfigException("Queue configurations list must not be null or empty.");
    }

    /**
    * An exception that should be thrown when the queue configurations mixing multiple types of
    * exchanges. (e.g. Topic and Direct)
    */
    public static void mixedExchangeTypesException() {
        throw new QueueConfigException(
                "Queue configurations list must include only 1 configuration type.");
    }
}
