package org.xyp.functional.result.wrapper;


import org.xyp.functional.result.*;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * map 函数为递归 lazy 调用，可以保留链式调用的调用栈<br/>
 * 返回值一旦有null则调用链中断<br/>
 * 由于是 lazy 调用， 因此最后需要用 getXXX 函数做真正的调用
 *
 * @param <R>
 */
public class ResultOrError<R> {

    private final Supplier<StackStepInfo<R>> supplier;

    Supplier<StackStepInfo<R>> supplier() {
        return supplier;
    }

    ResultOrError(Supplier<StackStepInfo<R>> supplier) {
        this.supplier = supplier;
    }

    static StackWalker.StackFrame getStackStep() {
        return StackWalker.getInstance()
            .walk(stream -> stream.filter(s ->
                    !s.toStackTraceElement().getClassName().equals(ResultOrError.class.getName())
                        && !s.toStackTraceElement().getClassName().equals(WithCloseable.class.getName())
                )
                .findFirst())
            .orElse(null);
    }

    public static <T1> ResultOrError<T1> of(T1 t1) {
        final var frame = getStackStep();
        return new ResultOrError<>(() -> new StackStepInfo<>(frame, null, null, t1, null));
    }

    public static <R> ResultOrError<R> on(ExceptionalSupplier<R> supplier) {
        final var frame = getStackStep();
        return new ResultOrError<>(() -> {
            try {
                final var result = supplier.get();
                return new StackStepInfo<>(frame, null, null, result, null);
            } catch (Exception exception) {
                return new StackStepInfo<>(frame, null, null, null, exception);
            }
        });
    }

    public static ResultOrError<Void> doRun(ExceptionalRunnable runner) {
        final var frame = getStackStep();
        return new ResultOrError<>(() -> {
            try {
                runner.run();
                return new StackStepInfo<>(frame, null, null, null, null);
            } catch (Exception exception) {
                return new StackStepInfo<>(frame, null, null, null, exception);
            }
        }
        );
    }

    public ResultOrError<R> filter(Predicate<? super R> predicate) {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var prevStack = supplier.get();
                return getStackStepInfoByFilter(predicate, prevStack, frame);
            }
        );
    }

    static <R> StackStepInfo<R> getStackStepInfoByFilter(Predicate<? super R> predicate, StackStepInfo<R> prevStack, StackWalker.StackFrame frame) {
        if (prevStack.isError()) {
            return prevStack;
        }
        final var lastOutput = prevStack.output();
        try {
            if (null != lastOutput && predicate.test(lastOutput)) {
                return prevStack;
            } else {
                return new StackStepInfo<>(frame, prevStack, null, null, null);
            }
        } catch (Exception exception) {
            return new StackStepInfo<>(frame, prevStack, lastOutput, null, exception);
        }
    }

    public ResultOrError<R> fallbackForEmpty(Supplier<R> emptySupplier) {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var prevStack = supplier.get();
                final var lastOutput = prevStack.output();
                if (prevStack.isError()) {
                    return prevStack;
                }
                try {
                    if (null == lastOutput) {
                        var currentRes = emptySupplier.get();
                        return new StackStepInfo<>(frame, prevStack, null, currentRes, null);
                    } else {
                        return new StackStepInfo<>(frame, prevStack, lastOutput, lastOutput, null);
                    }
                } catch (Exception exception) {
                    return new StackStepInfo<>(frame, prevStack, lastOutput, null, exception);
                }
            }
        );
    }

    public ResultOrError<R> consume(ExceptionalConsumer<? super R> consumer) {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var prevStack = supplier.get();
                return getStackByConsume(consumer, prevStack, frame);
            }
        );
    }

    public ResultOrError<R> doOnError(ExceptionalConsumer<? super Exception> consumer) {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var prevStack = supplier.get();
                final var lastOutput = prevStack.output();
                try {
                    if (prevStack.isError()) {
                        consumer.accept(prevStack.exception());
                        return new StackStepInfo<>(frame, prevStack, lastOutput, null, prevStack.exception());
                    }
                    return prevStack;
                } catch (Exception exception) {
                    return new StackStepInfo<>(frame, prevStack, lastOutput, null, exception);
                }
            }
        );

    }

    static <R> StackStepInfo<R> getStackByConsume(ExceptionalConsumer<? super R> consumer, StackStepInfo<R> prevStack, StackWalker.StackFrame frame) {

        final var lastOutput = prevStack.output();
        if (prevStack.isError()) {
            return prevStack;
        } else if (null != lastOutput) {
            try {
                consumer.accept(lastOutput);
                return new StackStepInfo<>(frame, prevStack, lastOutput, lastOutput, null);
            } catch (Exception exception) {
                return new StackStepInfo<>(frame, prevStack, lastOutput, null, exception);
            }
        } else {
            return prevStack;
        }
    }

    public <U> ResultOrError<U> map(ExceptionalFunction<? super R, ? extends U> mapper) {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var previousStackInfo = supplier.get();
                return getStackStepInfoByMapper(mapper, previousStackInfo, frame);
            }
        );
    }

    public ResultOrError<R> mapOnError(ExceptionalFunction<Exception, ? extends R> mapper) {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var prevStack = supplier.get();
                try {
                    if (prevStack.isError()) {
                        final var newOutputForError = mapper.apply(prevStack.exception());
                        return new StackStepInfo<>(frame, prevStack, prevStack.exception(), newOutputForError, null);
                    }
                    return prevStack;
                } catch (Exception exception) {
                    final var lastOutput = prevStack.output();
                    return new StackStepInfo<>(frame, prevStack, lastOutput, null, exception);
                }
            }
        );
    }

    @SuppressWarnings("unchecked")
    static <R, U> StackStepInfo<U> getStackStepInfoByMapper(
        ExceptionalFunction<? super R, ? extends U> mapper,
        StackStepInfo<R> previousStackInfo,
        StackWalker.StackFrame frame
    ) {
        final var lastOutput = previousStackInfo.output();
        if (previousStackInfo.isError()) {
            return (StackStepInfo<U>) previousStackInfo;
        } else if (null != lastOutput) {
            try {
                final var mappedVal = mapper.apply(lastOutput);
                return new StackStepInfo<>(frame, previousStackInfo, lastOutput, mappedVal, null);
            } catch (Exception t) {
                return new StackStepInfo<>(frame, previousStackInfo, lastOutput, null, t);
            }
        } else {
            return (StackStepInfo<U>) previousStackInfo;
        }
    }

    public <U> ResultOrError<U> noExMap(Function<? super R, ? extends U> mapper) {
        return map(mapper::apply);
    }

    @SuppressWarnings("unchecked")
    public <U> ResultOrError<U> flatMap(Function<? super R, ResultOrError<U>> mapper) {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var previousStackInfo = supplier.get();
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return (StackStepInfo<U>) previousStackInfo;
                } else if (null != lastOutput) {
                    final var mapperROE = mapper.apply(lastOutput);
                    final var mappedResult = mapperROE.getResultInPackage(mapperROE.supplier);
                    final var childStack = mappedResult.getStackStepInfo();
                    if (mappedResult.isSuccess()) {
                        final var mappedVal = mappedResult.get();
                        return new StackStepInfo<>(frame, previousStackInfo, lastOutput, mappedVal, null, childStack.orElse(null));
                    } else {
                        return new StackStepInfo<>(frame, previousStackInfo, lastOutput, null, mappedResult.getError(), childStack.orElse(null));
                    }
                } else {
                    return (StackStepInfo<U>) previousStackInfo;
                }
            }
        );
    }

    @SuppressWarnings("unchecked")
    public ResultOrError<Optional<R>> continueWithOptional() {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var previousStackInfo = supplier.get();
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return (StackStepInfo<Optional<R>>) previousStackInfo;
                } else {
                    return new StackStepInfo<>(frame, previousStackInfo, lastOutput, Optional.ofNullable(lastOutput), null, null);
                }
            }
        );
    }

    public R get() {
        return getResult().get();
    }

    public Optional<R> getOption() {
        return Optional.ofNullable(get());
    }

    public <E extends RuntimeException> R getOrSpecError(Class<E> target, Function<Exception, E> exceptionMapper) {
        return getResult().getOrSpecError(target, exceptionMapper);
    }

    public <E extends RuntimeException> R getOrSpecErrorBy(Class<E> target, Function<Result<R, Exception>, E> exceptionMapper) {
        return getResult().getOrSpecErrorBy(target, exceptionMapper);
    }

    public <E extends RuntimeException> Optional<R> getOptionOrSpecError(Class<E> target, Function<Exception, E> exceptionMapper) {
        return getResult().getOptionOrSpecError(target, exceptionMapper);
    }

    public <E extends RuntimeException> Optional<R> getOptionOrSpecErrorBy(Class<E> target, Function<Result<R, Exception>, E> exceptionMapper) {
        return getResult().getOptionOrSpecErrorBy(target, exceptionMapper);
    }

    Result<R, Exception> getResultInPackage(Supplier<? extends StackStepInfo<R>> wrapped) {
        final var res = (wrapped.get());
        if (res.isError()) {
            return Result.failure(res.exception(), res);
        }
        return Result.success(res.output(), res);
    }

    public Result<R, Exception> getResult() {

        final var wrapped = (Supplier<? extends StackStepInfo<R>>) () -> {
            final var res = supplier.get();
            return new StackStepInfo<>(getStackStep(), res, res.input(), res.output(), res.exception());
        };

        return getResultInPackage(wrapped);
    }

    public <W extends RuntimeException>
    Result<R, W> getResult(Class<W> target, Function<Exception, W> exceptionMapper) {
        return getResult().mapError(target, exceptionMapper);
    }

}
