package org.xyp.functional.result.wrapper;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record Success<T, E extends Exception>(T value, StackStepInfo<T> stackStepInfo) implements Result<T, E> {

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public E getError() {
        return null;
    }

    @Override
    public <R extends RuntimeException> T getOrSpecError(Class<R> rteClass, Function<E, R> exceptionMapper) {
        return value;
    }

    @Override
    public <R extends RuntimeException> T getOrSpecErrorBy(Class<R> rteClass, Function<Result<T, E>, R> exceptionMapper) {
        return value;
    }

    @Override
    public Optional<T> getOption() {
        return Optional.ofNullable(value);
    }

    @Override
    public Optional<T> getOptionEvenErr(Function<E, T> exceptionFallBack) {
        return Optional.ofNullable(value);
    }

    @Override
    public <R extends RuntimeException> Optional<T> getOptionOrSpecError(Class<R> rteClass, Function<E, R> exceptionMapper) {
        return Optional.ofNullable(value);
    }

    @Override
    public <R extends RuntimeException> Optional<T>
    getOptionOrSpecErrorBy(Class<R> rteClass, Function<Result<T, E>, R> exceptionMapper) {
        return Optional.ofNullable(value);
    }

    @Override
    public Result<T, E> ifError(Consumer<E> consumer) {
        return this;
    }

    @Override
    public T getOrFallBackForError(Function<E, T> exceptionMapper) {
        return value;
    }

    @Override
    public Optional<StackStepInfo<T>> getStackStepInfo() {
        return Optional.ofNullable(stackStepInfo);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <W extends RuntimeException>
    Result<T, W> mapError(Class<W> target, Function<E, W> exceptionMapper) {
        return (Result<T, W>) this;
    }
}
