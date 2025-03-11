package org.xyp.functional.result;

@FunctionalInterface
public interface ExceptionalConsumer<T> {
    void accept(T t) throws Exception;
}
