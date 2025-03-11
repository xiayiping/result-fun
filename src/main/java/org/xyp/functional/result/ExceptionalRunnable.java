package org.xyp.functional.result;

@FunctionalInterface
public interface ExceptionalRunnable {
    void run() throws Exception;
}
