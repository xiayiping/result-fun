package org.xyp.functional.result;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Fun {

    private Fun() {
    }

    public static <T> ExceptionalFunction<T, T> updateSelf(ExceptionalConsumer<T> consumer) {
        return obj -> {
            consumer.accept(obj);
            return obj;
        };
    }

    public static <T> Function<T, T> consumeSelf(Consumer<T> consumer) {
        return obj -> {
            consumer.accept(obj);
            return obj;
        };
    }

    public static <T> Optional<T> checkAndCast(Object o, Class<T> target) {
        return Optional.ofNullable(o)
            .filter(target::isInstance)
            .map(target::cast);
    }

    public static <R> Function<Object, Optional<R>> cast(Class<R> target) {
        return o -> checkAndCast(o, target);
    }

    public static <E extends RuntimeException> E convertRte(Exception e, Class<E> target, Function<Exception, E> converter) {
        if (target.isAssignableFrom(e.getClass())) {
            return (target.cast(e));
        }
        return converter.apply(e);
    }
}
