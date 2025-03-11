package org.xyp.functional.result;

@FunctionalInterface
public interface ExceptionalBiConsumer<S, T> {
    void accept(S s, T t) throws Exception;
}

