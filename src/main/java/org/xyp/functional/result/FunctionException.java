package org.xyp.functional.result;

public class FunctionException extends RuntimeException {
    public FunctionException() {
    }

    public FunctionException(String message) {
        super(message);
    }

    public FunctionException(String message, Exception cause) {
        super(message, cause);
    }

    public FunctionException(Exception cause) {
        super(cause);
    }
}
