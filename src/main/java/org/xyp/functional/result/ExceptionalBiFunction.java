package org.xyp.functional.result;

@FunctionalInterface
public interface ExceptionalBiFunction<T, U, R> {
    R apply(T t, U u) throws Exception;
}
