package org.xyp.functional.result.wrapper;

public class ValueHolder<T> {
    private T value;

    public static <T> ValueHolder<T> of(T value) {
        return new ValueHolder<>(value);
    }

    public ValueHolder() {
        this.value = null;
    }

    public ValueHolder(T t) {
        this.value = t;
    }

    public T value() {
        return value;
    }

    public ValueHolder<T> setValue(T value) {
        this.value = value;
        return this;
    }

    public boolean isEmpty() {
        return null == value;
    }
}
