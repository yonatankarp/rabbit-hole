package com.yonatankarp.rabbit_hole.exceptions;

/** An exception that is thrown when the given exchange is not valid. */
public class ExchangeException extends RuntimeException {
    private ExchangeException(final String message) {
        super(message);
    }

    /**
    * Throwing ExchangeException exception the proper message for invalid exchange name.
    *
    * @throws ExchangeException if the exchange name if null or empty
    */
    public static void invalidExchangeNameException() {
        throw new ExchangeException("Exchange name cannot be null or empty");
    }
}
