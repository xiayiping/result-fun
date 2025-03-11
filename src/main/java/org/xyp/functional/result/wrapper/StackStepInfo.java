package org.xyp.functional.result.wrapper;


import java.util.Optional;

public class StackStepInfo<T> {
    private final StackWalker.StackFrame stackFrame;
    private final StackStepInfo<?> previous;
    private final Object input;
    private final T output;
    private final Exception exception;
    private final StackStepInfo<T> child;

    public StackStepInfo(
        StackWalker.StackFrame stackFrame,
        StackStepInfo<?> previous,
        Object input,
        T output,
        Exception exception,
        StackStepInfo<T> child
    ) {
        this.stackFrame = stackFrame;
        this.previous = previous;
        this.input = input;
        this.output = output;
        this.exception = exception;
        this.child = child;
    }

    public boolean isError() {
        return null != exception;
    }

    public Optional<StackStepInfo<T>> getChild() {
        return Optional.ofNullable(child);
    }

    public StackStepInfo(
        StackWalker.StackFrame stackFrame,
        StackStepInfo<?> previous,
        Object input,
        T output,
        Exception exception
    ) {
        this(stackFrame, previous, input, output, exception, null);
    }

    public StackWalker.StackFrame stackFrame() {
        return stackFrame;
    }

    public StackStepInfo<?> previous() {
        return previous;
    }

    public Object input() {
        return input;
    }

    public T output() {
        return output;
    }

    public Exception exception() {
        return exception;
    }

    public StackStepInfo<T> child() {
        return child;
    }
}
