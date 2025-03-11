package org.xyp.functional.result.wrapper;

import org.xyp.functional.result.Fun;
import org.xyp.functional.result.FunctionException;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record Failure<T, E extends Exception>(
    E exception,
    StackStepInfo<T> stackStepInfo
) implements Result<T, E> {
    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public T get() {
        throw Fun.convertRte(exception, RuntimeException.class, FunctionException::new);
    }

    @Override
    public E getError() {
        return exception;
    }

    @Override
    public <R extends RuntimeException> T getOrSpecError(Class<R> rteClass, Function<E, R> exceptionMapper) {
        if (rteClass.isAssignableFrom(exception.getClass())) {
            throw rteClass.cast(exception);
        }
        throw exceptionMapper.apply(exception);
    }

    @Override
    public <R extends RuntimeException> T getOrSpecErrorBy(Class<R> rteClass, Function<Result<T, E>, R> exceptionMapper) {
        if (rteClass.isAssignableFrom(exception.getClass())) {
            throw rteClass.cast(exception);
        }
        throw exceptionMapper.apply(this);
    }

    @Override
    public Optional<T> getOption() {
        throw Fun.convertRte(exception, RuntimeException.class, FunctionException::new);
    }

    @Override
    public Optional<T> getOptionEvenErr(Function<E, T> exceptionFallBack) {
        return Optional.ofNullable(exceptionFallBack.apply(exception));
    }

    @Override
    public <R extends RuntimeException> Optional<T> getOptionOrSpecError(Class<R> rteClass, Function<E, R> exceptionMapper) {
        if (rteClass.isAssignableFrom(exception.getClass())) {
            throw rteClass.cast(exception);
        }
        throw exceptionMapper.apply(exception);
    }

    @Override
    public <R extends RuntimeException> Optional<T>
    getOptionOrSpecErrorBy(Class<R> rteClass, Function<Result<T, E>, R> exceptionMapper) {
        if (rteClass.isAssignableFrom(exception.getClass())) {
            throw rteClass.cast(exception);
        }
        throw exceptionMapper.apply(this);
    }

    @Override
    public Result<T, E> ifError(Consumer<E> consumer) {
        consumer.accept(exception);
        return this;
    }

    @Override
    public T getOrFallBackForError(Function<E, T> exceptionMapper) {
        return exceptionMapper.apply(exception);
    }

    @Override
    public Optional<StackStepInfo<T>> getStackStepInfo() {
        return Optional.ofNullable(stackStepInfo);
    }

    @Override
    public <W extends RuntimeException>
    Result<T, W> mapError(Class<W> target, Function<E, W> exceptionMapper) {
        return new Failure<>(
            exceptionMapper.apply(this.exception),
            this.stackStepInfo
        );
    }
}
