# Result Wrapper

## Why this project

In java it is trivial to write code which introduces try-catch blocks, especially when you need to hold the return value from inside a try block.

Also java forces to catch 'unchecked' exception, which blocks the normal function call process.

you either extract the try-catch block to another method and make that method return the value and convert exception to RuntimeException, or write some tricky code like below:

```java

Object someMethod() {
    var value1 = ((Supplier<Object>)() -> {
        try {
            return someMethodThrowsIOException();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }).get();

    return doSomethingTo(value1);
}

```

This project aims to solve the problem , by borrowing the concept of Monad, wrapping the exception to a context called 'ResultOrError', just like other languages like scala's Success/Failure and Rust's Ok/Err.

## Target

1. zero dependency , no spring-boot, no others. Depends on nothing more than slf4j, which is a bridge for loggers.
2. Monad's mapper call chain(imaging the java stream map) is hard to debug, try to use lazy call and StackWalker to record the input/output of each mapping call.
3. You can specify which runtime exception should be thrown if exception throw in your function call chain.
4. Handle all Exceptions, but not Errors (Throwable), Errors (normally means a fatal problem) should still be explicitly handled.

## Example

```java
public static void main(String[] args) {
    String fileName = ResultOrError
        .on(() -> Files.createDirectories(Path.of("path")))
        .map(path -> path.getFileName().toString())
        .getOrSpecError(
            IllegalStateException.class,
            ex -> new IllegalStateException(ex.getMessage()));
    System.out.println("file name is " + fileName);
}
```

In the example, Files.createDirectories throws java.io.IOException, but can be caught inside the ResultOrError context, the returned fileName can be used by further process. There's no try-catch.


```java
// error log
public static void main(String[] args) {
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
/*
    the output:
begin -1, end -1, length 4
|--- org.xyp.functional.result.wrapper.Example.main(Example.java:14)
|---     ->: path
|---     <-: null
|---     [x] java.lang.StringIndexOutOfBoundsException: begin -1, end -1, length 4
|--- org.xyp.functional.result.wrapper.Example.main(Example.java:13)
|---     ->: path
|---     <-: null
|---     [x] java.lang.StringIndexOutOfBoundsException: begin -1, end -1, length 4
|--- org.xyp.functional.result.wrapper.Example.main(Example.java:11)
|---     ->: path
|---     <-: path
|--- org.xyp.functional.result.wrapper.Example.main(Example.java:10)
|---     ->: null
|---     <-: path 
    
 */
```


I will add more examples in Example.java, but for now, please refer to ResultOrErrorTest and WIthCloseableTest for more samples.