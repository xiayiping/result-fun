package org.xyp.functional.result.wrapper;


import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class StackLogUtil {
    private StackLogUtil() {
    }

    public static final String TRACE_LOG_INDENT = "|--- ";
    private static final int MAX_SIZE = 150;

    public static void logTrace(
        Consumer<String> logger,
        StackStepInfo<?> stackInfo
    ) {
        logTrace(logger, stackInfo, () -> true);
    }

    public static void logTrace(
        Consumer<String> logger,
        StackStepInfo<?> stackInfo,
        BooleanSupplier shouldLog
    ) {
        logTrace(logger, stackInfo, shouldLog, StackLogUtil::getLogForSingleStack);
    }

    public static void logTrace(
        Consumer<String> logger,
        StackStepInfo<?> stackInfo,
        BooleanSupplier shouldLog,
        BiFunction<String, StackStepInfo<?>, String> logGenerator
    ) {
        if (!shouldLog.getAsBoolean() || stackInfo == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (stackInfo.isError()) {
            sb.append(stackInfo.exception().getMessage()).append(System.lineSeparator());
        } else {
            sb.append("stackInfo.output() ::").append(System.lineSeparator());
        }
        logTrace(sb, stackInfo, TRACE_LOG_INDENT, logGenerator);
        logger.accept(sb.toString());
    }

    private static void logTrace(
        StringBuilder sb,
        StackStepInfo<?> stackInfo,
        String prefix,
        BiFunction<String, StackStepInfo<?>, String> logGenerator
    ) {
        var currentStackInfo = stackInfo;
        while (null != currentStackInfo) {
            extractLogString(sb, prefix, currentStackInfo, logGenerator);

            currentStackInfo.getChild().ifPresent(child ->
                logTrace(sb, child, prefix + TRACE_LOG_INDENT, logGenerator)
            );

            currentStackInfo = currentStackInfo.previous();
        }
    }

    private static void extractLogString(
        StringBuilder sb,
        String prefix,
        StackStepInfo<?> currentStackInfo,
        BiFunction<String, StackStepInfo<?>, String> logGenerator
    ) {
        final var log2 = logGenerator.apply(prefix, currentStackInfo);
        sb.append(log2).append(System.lineSeparator());
    }

    private static String getLogForSingleStack(String prefix, StackStepInfo<?> currentStackInfo) {
        final var frame = currentStackInfo.stackFrame().toString();

        final var input = Optional.of(Objects.toString(currentStackInfo.input()))
            .map(s -> {
                if (s.length() > MAX_SIZE) {
                    return s.substring(0, MAX_SIZE) + " ...";
                }
                return s;
            }).orElse(null);
        final var output = Optional.of(Objects.toString(currentStackInfo.output()))
            .map(s -> {
                if (s.length() > MAX_SIZE) {
                    return s.substring(0, MAX_SIZE) + " ...";
                }
                return s;
            }).orElse(null);

        if (currentStackInfo.isError()) {
            return String.format("%s%s\n%s    ->: %s\n%s    <-: %s\n%s    [x] %s",
                prefix, frame,
                prefix, input,
                prefix, output,
                prefix, Optional.ofNullable(currentStackInfo.exception()).map(Object::toString).orElse("")
            );
        } else {
            return String.format("%s%s\n%s    ->: %s\n%s    <-: %s",
                prefix, frame,
                prefix, input,
                prefix, output
            );
        }
    }
}
