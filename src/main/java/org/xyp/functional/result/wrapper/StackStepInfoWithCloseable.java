package org.xyp.functional.result.wrapper;

public class StackStepInfoWithCloseable<C extends AutoCloseable, T>
    extends StackStepInfo<T>
    implements AutoCloseable {

    private final C closeable;

    public StackStepInfoWithCloseable(
        StackWalker.StackFrame stackFrame,
        StackStepInfoWithCloseable<C, ?> previous,
        C closeable,
        Object input,
        T output,
        Exception exception
    ) {
        super(stackFrame, previous, input, output, exception, null);
        this.closeable = closeable;
    }

    public StackStepInfoWithCloseable(
        StackWalker.StackFrame stackFrame,
        StackStepInfo<?> previous,
        C closeable,
        Object input,
        T output,
        Exception exception,
        StackStepInfo<T> child
    ) {
        super(stackFrame, previous, input, output, exception, child);
        this.closeable = closeable;
    }

    @Override
    public void close() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    public C closeable() {
        return closeable;
    }

}
