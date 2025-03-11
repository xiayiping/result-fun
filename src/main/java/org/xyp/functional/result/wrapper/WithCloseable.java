package org.xyp.functional.result.wrapper;

import org.xyp.functional.result.*;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class WithCloseable<C extends AutoCloseable, T> {

    static StackWalker.StackFrame getStackStep() {
        return StackWalker.getInstance()
            .walk(stream -> stream.filter(s ->
                    !s.toStackTraceElement().getClassName().equals(WithCloseable.class.getName())
                        && !s.toStackTraceElement().getClassName().equals(ResultOrError.class.getName())
                )
                .findFirst())
            .orElse(null);
    }

    private static <L extends AutoCloseable> StackStepInfoWithCloseable<L, L>
    openStackStepInfoWithCloseable(ExceptionalSupplier<L> open, StackWalker.StackFrame frame) {
        try {
            final var closeable = open.get();
            return new StackStepInfoWithCloseable<>(frame, null, closeable, null, closeable, null);
        } catch (Exception t) {
            return new StackStepInfoWithCloseable<>(frame, null, null, null, null, t);
        }
    }

    @SuppressWarnings("unchecked")
    static <R, U, C extends AutoCloseable> StackStepInfoWithCloseable<C, U>
    getStackStepInfoByMapper(
        ExceptionalFunction<? super R, ? extends U> mapper,
        StackStepInfoWithCloseable<C, R> previousStackInfo,
        StackWalker.StackFrame frame
    ) {
        final var lastOutput = previousStackInfo.output();
        if (previousStackInfo.isError()) {
            return (StackStepInfoWithCloseable<C, U>) previousStackInfo;
        } else if (null != lastOutput) {
            final var closeable = previousStackInfo.closeable();
            try {
                final var mappedVal = mapper.apply(lastOutput);
                return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, mappedVal, null);
            } catch (Exception t) {
                return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, null, t);
            }
        } else {
            return (StackStepInfoWithCloseable<C, U>) previousStackInfo;
        }
    }

    static <R, C extends AutoCloseable> StackStepInfoWithCloseable<C, R>
    getStackByConsume(
        ExceptionalConsumer<? super R> consumer,
        StackStepInfoWithCloseable<C, R> prevStack,
        StackWalker.StackFrame frame
    ) {
        final var lastOutput = prevStack.output();
        if (prevStack.isError()) {
            return prevStack;
        } else if (null != lastOutput) {
            final var closeable = prevStack.closeable();
            try {
                consumer.accept(lastOutput);
                return new StackStepInfoWithCloseable<>(frame, prevStack, closeable, lastOutput, lastOutput, null);
            } catch (Exception exception) {
                return new StackStepInfoWithCloseable<>(frame, prevStack, closeable, lastOutput, null, exception);
            }
        } else {
            return prevStack;
        }
    }

    static <C extends AutoCloseable, R> StackStepInfoWithCloseable<C, R> getStackStepInfoByFilter(
        Predicate<? super R> predicate,
        StackStepInfoWithCloseable<C, R> prevStack,
        StackWalker.StackFrame frame
    ) {
        if (prevStack.isError()) {
            return prevStack;
        }
        final var lastOutput = prevStack.output();
        final var closeable = prevStack.closeable();
        try {
            if (null != lastOutput && predicate.test(lastOutput)) {
                return prevStack;
            } else {
                return new StackStepInfoWithCloseable<>(frame, prevStack, closeable, null, null, null);
            }
        } catch (Exception exception) {
            return new StackStepInfoWithCloseable<>(frame, prevStack, closeable, lastOutput, null, exception);
        }
    }

    public static <L extends AutoCloseable> WithCloseable<L, L> open(ExceptionalSupplier<L> open) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> openStackStepInfoWithCloseable(open, frame),
            // (closeable, exception) onException
            (__, ___) -> {
            }
        );
    }

    public static <L extends AutoCloseable> WithCloseable<L, L> open(
        ExceptionalSupplier<L> open,
        BiConsumer<L, Exception> exceptionConsumer
    ) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> openStackStepInfoWithCloseable(open, frame),
            exceptionConsumer
        );
    }

    final Supplier<StackStepInfoWithCloseable<C, T>> closeableSupplier;
    final BiConsumer<C, Exception> exceptionConsumer;

    private WithCloseable(
        Supplier<StackStepInfoWithCloseable<C, T>> closeableSupplier,
        BiConsumer<C, Exception> exceptionConsumer
    ) {
        this.closeableSupplier = closeableSupplier;
        this.exceptionConsumer = exceptionConsumer;
    }

    public <U> WithCloseable<C, U> map(ExceptionalFunction<? super T, ? extends U> function) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                return getStackStepInfoByMapper(function, previousStackInfo, frame);
            },
            exceptionConsumer
        );
    }

    public WithCloseable<C, T> fallBackEmpty(Function<C, T> emptySupplier) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return previousStackInfo;
                } else if (null == lastOutput) {
                    final var closeable = previousStackInfo.closeable();
                    try {
                        final var mappedVal = emptySupplier.apply(closeable);
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, null, mappedVal, null);
                    } catch (Exception t) {
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, null, null, t);
                    }
                } else {
                    return previousStackInfo;
                }
            },
            exceptionConsumer
        );
    }

    public WithCloseable<C, T> consume(ExceptionalConsumer<? super T> consumer) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                return getStackByConsume(consumer, previousStackInfo, frame);
            },
            this.exceptionConsumer
        );
    }

    public WithCloseable<C, T> doOnError(ExceptionalBiConsumer<C, Exception> consumer) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                final var lastOutput = previousStackInfo.output();
                try {
                    if (previousStackInfo.isError()) {
                        consumer.accept(previousStackInfo.closeable(), previousStackInfo.exception());
                        final var closeable = previousStackInfo.closeable();
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, null, previousStackInfo.exception());
                    } else {
                        return previousStackInfo;
                    }
                } catch (Exception t) {
                    final var closeable = previousStackInfo.closeable();
                    return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, null, t);
                }
            },
            this.exceptionConsumer
        );
    }

    public WithCloseable<C, T> mapOnError(ExceptionalBiFunction<C, Exception, T> consumer) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                final var lastOutput = previousStackInfo.output();
                try {
                    if (previousStackInfo.isError()) {
                        final var newValueForError = consumer.apply(previousStackInfo.closeable(), previousStackInfo.exception());
                        final var closeable = previousStackInfo.closeable();
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, previousStackInfo.exception(), newValueForError, null);
                    } else {
                        return previousStackInfo;
                    }
                } catch (Exception t) {
                    final var closeable = previousStackInfo.closeable();
                    return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, null, t);
                }
            },
            this.exceptionConsumer
        );
    }


    @SuppressWarnings("unchecked")
    public <U> WithCloseable<C, U> mapWithCloseable(ExceptionalBiFunction<? super C, ? super T, ? extends U> biFunction) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return (StackStepInfoWithCloseable<C, U>) previousStackInfo;
                } else {
                    final var closeable = previousStackInfo.closeable();
                    try {
                        final var mappedVal = biFunction.apply(closeable, lastOutput);
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, mappedVal, null);
                    } catch (Exception t) {
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, null, t);
                    }
                }
            },
            this.exceptionConsumer
        );
    }

    public WithCloseable<C, T> filter(Predicate<? super T> predicate) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var prevStack = closeableSupplier.get();
                return getStackStepInfoByFilter(predicate, prevStack, frame);
            },
            this.exceptionConsumer
        );
    }

    @SuppressWarnings("unchecked")
    public <U> WithCloseable<C, U> flatMap(Function<? super T, ResultOrError<U>> mapper) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return (StackStepInfoWithCloseable<C, U>) previousStackInfo;
                } else if (null != lastOutput) {
                    final var mapperROE = mapper.apply(lastOutput);
                    final var mappedResult = mapperROE.getResultInPackage(mapperROE.supplier());
                    final var childStack = mappedResult.getStackStepInfo();
                    final var closeable = previousStackInfo.closeable();
                    if (mappedResult.isSuccess()) {
                        final var mappedVal = mappedResult.get();
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, mappedVal, null, childStack.orElse(null));
                    } else {
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, null, mappedResult.getError(), childStack.orElse(null));
                    }
                } else {
                    return (StackStepInfoWithCloseable<C, U>) previousStackInfo;
                }
            },
            this.exceptionConsumer
        );
    }

    @SuppressWarnings("unchecked")
    public WithCloseable<C, Optional<T>> continueWithOptional() {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return (StackStepInfoWithCloseable<C, Optional<T>>) previousStackInfo;
                } else {
                    return new StackStepInfoWithCloseable<>(frame, previousStackInfo, previousStackInfo.closeable(), lastOutput, Optional.ofNullable(lastOutput), null, null);
                }
            },
            this.exceptionConsumer
        );
    }

    public T closeAndGet() {
        return closeAndGetResult().get();
    }

    public <E extends RuntimeException> T closeAndGet(
        Class<E> target,
        Function<Exception, E> exceptionMapper
    ) {
        return closeAndGetResult().getOrSpecError(target, exceptionMapper);
    }

    public Result<T, Exception> closeAndGetResult() {
        return convertToResult().getResult();
    }


    public <W extends RuntimeException>
    Result<T, W> closeAndGetResult(Class<W> target, Function<Exception, W> exceptionMapper) {
        return closeAndGetResult().mapError(target, exceptionMapper);
    }

    private ResultOrError<T> convertToResult() {

        final var frame = getStackStep();
        final var wrapped = (Supplier<StackStepInfo<T>>) () -> {
            C localCloseable = null;
            StackStepInfoWithCloseable<C, T> localRes = null;
            try (var res = closeableSupplier.get()) {
                localCloseable = res.closeable();
                localRes = res;
                if (res.isError()) {
                    this.exceptionConsumer.accept(localCloseable, res.exception());
                    localRes = new StackStepInfoWithCloseable<>(frame, res, localCloseable, res.output(), res.output(), res.exception());
                }
            } catch (Exception e) {
                localRes = new StackStepInfoWithCloseable<>(
                    StackWalker.getInstance().walk(Stream::findFirst).orElse(localRes.stackFrame()),
                    new StackStepInfoWithCloseable<>(
                        frame,
                        localRes,
                        localCloseable,
                        localRes.output(),
                        localRes.output(),
                        localRes.exception()
                    ),
                    localCloseable,
                    localRes.output(),
                    localRes.output(),
                    e
                );
            }
            return localRes;
        };

        return new ResultOrError<>(wrapped);
    }
}
