package org.xyp.functional.result.wrapper;

public class ValidateWithStackException extends RuntimeException {
    private final StackStepInfo<?> stackStepInfo;

    public ValidateWithStackException(String message, StackStepInfo<?> stackStepInfo) {
        super(message);
        this.stackStepInfo = stackStepInfo;
    }


    public StackStepInfo<?> getStackStepInfo() {
        return stackStepInfo;
    }
}
