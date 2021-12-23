package com.yonatankarp.rabbit_hole.exception

/** An exception that is thrown when the given exchange is not valid.  */
class ExchangeException private constructor(message: String) : RuntimeException(message) {
    companion object {
        /**
         * Throwing ExchangeException exception the proper message for invalid exchange name.
         *
         * @throws ExchangeException if the exchange name if null or empty
         */
        @JvmStatic
        fun invalidExchangeNameException() {
            throw ExchangeException("Exchange name cannot be null or empty")
        }
    }
}

/** An exception caused by misconfiguration of the queues.  */
class QueueConfigException private constructor(message: String) : RuntimeException(message) {
    companion object {
        /** An exception that should be thrown if no queue config was passed to the library  */
        @JvmStatic
        fun emptyConfigurationException() {
            throw QueueConfigException("Queue configurations list must not be null or empty.")
        }

        /**
         * An exception that should be thrown when the queue configurations mixing multiple types of
         * exchanges. (e.g. Topic and Direct)
         */
        @JvmStatic
        fun mixedExchangeTypesException() {
            throw QueueConfigException(
                "Queue configurations list must include only 1 configuration type."
            )
        }
    }
}
