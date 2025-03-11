package org.xyp.functional.result.wrapper;


import java.nio.file.Files;
import java.nio.file.Path;


public class Example {
    void example01() {
        String fileName = ResultOrError
            .on(() -> Files.createDirectories(Path.of("path")))
            .map(path -> path.getFileName().toString())
            // force exception
            .map(name -> name.substring(-1, name.lastIndexOf('.')))
            .getResult()
            .doIf(res -> true,
                res -> res.traceDebugOrError(
                    () -> true,
                    System.out::println,
                    () -> true,
                    System.out::println))
            .getOrSpecError(
                IllegalStateException.class,
                ex -> new IllegalStateException(ex.getMessage()));
        System.out.println("file name is " + fileName);
    }
}
