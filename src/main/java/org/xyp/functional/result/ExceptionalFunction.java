package org.xyp.functional.result;

@FunctionalInterface
public interface ExceptionalFunction<T, R> {
    R apply(T t) throws Exception;
}
