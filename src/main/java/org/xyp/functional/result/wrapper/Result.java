package org.xyp.functional.result.wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.*;

public interface Result<T, E extends Exception> {

    Logger log = LoggerFactory.getLogger(Result.class);

    boolean isSuccess();

    static <T, E extends Exception> Result<T, E> success(T t, StackStepInfo<T> stackStepInfo) {
        return new Success<>(t, stackStepInfo);
    }

    static <T, E extends Exception> Result<T, E> failure(E exception, StackStepInfo<T> stackStepInfo) {
        return new Failure<>(exception, stackStepInfo);
    }

    T get();

    E getError();

    <R extends RuntimeException>
    T getOrSpecError(Class<R> rteClass, Function<E, R> exceptionMapper);

    <R extends RuntimeException> T getOrSpecErrorBy(Class<R> rteClass, Function<Result<T, E>, R> exceptionMapper);

    Optional<T> getOption();

    Optional<T> getOptionEvenErr(Function<E, T> exceptionFallBack);

    <R extends RuntimeException> Optional<T>
    getOptionOrSpecError(Class<R> rteClass, Function<E, R> exceptionMapper);

    <R extends RuntimeException> Optional<T>
    getOptionOrSpecErrorBy(Class<R> rteClass, Function<Result<T, E>, R> exceptionMapper);

    Result<T, E> ifError(Consumer<E> consumer);

    T getOrFallBackForError(Function<E, T> exceptionMapper);

    Optional<StackStepInfo<T>> getStackStepInfo();

    default Result<T, E> doIf(Predicate<Result<T, E>> me, Consumer<Result<T, E>> consumer) {
        if (me.test(this)) {
            consumer.accept(this);
        }
        return this;
    }

    default Result<T, E> doIfError(Consumer<Result<T, E>> consumer) {
        return doIf(Failure.class::isInstance, consumer);
    }

    <W extends RuntimeException>
    Result<T, W> mapError(Class<W> target, Function<E, W> exceptionMapper);


    default Result<T, E> traceDebugOrError(
        BooleanSupplier needDebug, Consumer<String> debugLogger,
        BooleanSupplier needError, Consumer<String> errLogger
    ) {
        try {
            if (isSuccess() && needDebug.getAsBoolean()) {
                StackLogUtil.logTrace(debugLogger, getStackStepInfo().orElse(null));
            } else if (!isSuccess() && needError.getAsBoolean()) {
                StackLogUtil.logTrace(errLogger, getStackStepInfo().orElse(null));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return this;
    }
}