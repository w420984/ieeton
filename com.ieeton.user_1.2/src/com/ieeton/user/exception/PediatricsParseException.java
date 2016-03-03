package com.ieeton.user.exception;

/**
 * Thrown when there were problems parsing the response to an API call,
 * either because the response was empty, or it was malformed.
 */
public class PediatricsParseException extends Exception {

    private static final long serialVersionUID = 3132128578218204998L;

    public PediatricsParseException() {
        super();
    }

    public PediatricsParseException(String detailMessage) {
        super(detailMessage);
    }

    public PediatricsParseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PediatricsParseException(Throwable throwable) {
        super(throwable);
    }

}
