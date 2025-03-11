package org.xyp.functional.result;

@FunctionalInterface
public interface ExceptionalSupplier<R> {
    R get() throws Exception;
}
