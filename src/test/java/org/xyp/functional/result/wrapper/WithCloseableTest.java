package org.xyp.functional.result.wrapper;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xyp.functional.result.Fun;

class WithCloseableTest {
    @Test
    void test1() {
        ValueHolder<MockCloseable> holder = ValueHolder.of(null);
        Assertions.assertThat(holder.isEmpty()).isTrue();
        var lazy = WithCloseable.open(MockCloseable::new)
            .map(Fun.updateSelf(holder::setValue))
            .map(c -> 1);

        Assertions.assertThat(lazy.closeAndGet()).isOne();
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isTrue();
        Assertions.assertThat(lazy.closeAndGetResult().getOrFallBackForError(ex -> 2)).isOne();
        Assertions.assertThat(lazy
            .closeAndGetResult().isSuccess()).isTrue();

        Assertions.assertThat(lazy.closeAndGet()).isOne();
        Assertions.assertThat(lazy.closeAndGet(IllegalArgumentException.class, IllegalArgumentException::new)).isOne();

        Assertions.assertThat(holder.value().isClosed()).isTrue();
    }

    @Test
    void test2() {
        var lazy = WithCloseable.open(MockCloseable::new)
            .map(c -> 1)
            .map(c -> c / 0);

        Assertions.assertThatThrownBy(lazy::closeAndGet).isInstanceOf(ArithmeticException.class);
        Assertions.assertThatThrownBy(() -> lazy.closeAndGet(IllegalArgumentException.class, IllegalArgumentException::new))
            .isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isFalse();
    }

    @Test
    void test3() {
        var lazy = WithCloseable.open(MockCloseableErr::new)
            .map(c -> 1);

        Assertions.assertThatThrownBy(lazy::closeAndGet).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isFalse();
    }

    @Test
    void test4() {
        ValueHolder<MockCloseable> holder = new ValueHolder<>(null);
        var lazy = WithCloseable.open(MockCloseable::new)
            .map(Fun.updateSelf(holder::setValue))
            .map(c -> 1)
            .consume(i -> {
            })
            .map(c -> null)
            .consume(i -> {
            })
            .mapWithCloseable((c, i) -> i)
            .map(c -> 11);
        ;
        Assertions.assertThat(lazy.closeAndGet()).isNull();
    }

    @Test
    void test5() {
        var lazy = WithCloseable.open(MockCloseableErr2::new)
            .map(c -> 1);

        Assertions.assertThatThrownBy(lazy::closeAndGet).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isFalse();
        Assertions.assertThat(lazy.closeAndGetResult().getOrFallBackForError(ex -> 55)).isEqualTo(55);
    }

    @Test
    void test6() {
        ValueHolder<MockCloseable> holder = new ValueHolder<>(null);
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        ValueHolder<Integer> finallyHolder = new ValueHolder<>(0);
        final var lazy = WithCloseable.open(
                () -> new MockCloseable(() -> finallyHolder.setValue(1)),
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1)
            )
            .map(Fun.updateSelf(holder::setValue))
            .mapWithCloseable((c, v) -> {
                holder.setValue(c);
                Assertions.assertThat(holder.value().isClosed()).isFalse();
                return v;
            })
            .map(c -> 1)
            .fallBackEmpty(c -> 49)
            .consume(i -> {
            })
            .map(c -> null)
            .consume(i -> {
            })
            .mapWithCloseable((c, i) -> i)
            .map(c -> 11)
            .fallBackEmpty(c -> 44);
        ;
        Assertions.assertThat(lazy.closeAndGet()).isEqualTo(44);
        Assertions.assertThat(errorHolder.value()).isZero();
        Assertions.assertThat(finallyHolder.value()).isOne();
        Assertions.assertThat(holder.value().isClosed()).isTrue();
    }

    @Test
    void test7() {
        ValueHolder<MockCloseable> holder = new ValueHolder<>();
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        ValueHolder<Integer> finallyHolder = new ValueHolder<>(0);
        final var lazy = WithCloseable.open(
                () -> new MockCloseable(() -> finallyHolder.setValue(1)),
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1)/*,
                closeable -> finallyHolder.setValue(finallyHolder.value() + 1)*/
            )
            .map(Fun.updateSelf(holder::setValue))
            .mapWithCloseable((c, v) -> {
                holder.setValue(c);
                Assertions.assertThat(holder.value().isClosed()).isFalse();
                return v;
            })
            .map(c -> 1)
            .fallBackEmpty(c -> 49)
            .consume(i -> {
                Assertions.assertThat(i).isOne();
                throw new RuntimeException("run time e");
            })
            .map(i -> i)
            .fallBackEmpty(c -> 44);

        Assertions.assertThat(errorHolder.value()).isZero();
        final var result = lazy.closeAndGetResult();
        Assertions.assertThat(result).matches(r -> !r.isSuccess());
        Assertions.assertThat(errorHolder.value()).isOne();
        Assertions.assertThat(finallyHolder.value()).isOne();
        Assertions.assertThat(holder.value().isClosed()).isTrue();

        final ValueHolder<Integer> stackSize = new ValueHolder<>(0);
        var stackOpt = result.getStackStepInfo();
        System.out.println("--------");
        System.out.println("--------");
        Assertions.assertThat(stackOpt).isNotEmpty();
        stackOpt.ifPresent(stack -> {
            StackStepInfo<?> current = stack;
            while (null != current) {
                stackSize.setValue(stackSize.value() + 1);
                System.out.println(current.stackFrame());
                System.out.println("    " + current.input());
                System.out.println("    " + current.output());
                current = current.previous();
            }
        });
        Assertions.assertThat(stackSize.value()).isGreaterThan(4);
    }

    @Test
    void test08() {
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        final var lazy = WithCloseable.open(
                MockCloseable::new,
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1)/*,
                closeable -> finallyHolder.setValue(finallyHolder.value() + 1)*/
            )
            .map(c -> 1)
            .map(i -> i + 1)
            .filter(i -> i > 100)
            .continueWithOptional()
            .map(opt -> {
                Assertions.assertThat(opt).isEmpty();
                return null;
            })
            .fallBackEmpty(c -> 49 / 0)
            .map(i -> 99)
            .closeAndGetResult()
            //
            ;
        Assertions.assertThat(lazy.isSuccess()).isFalse();
        Assertions.assertThat(lazy.getStackStepInfo()).isNotEmpty();
        lazy.getStackStepInfo().ifPresent(stack -> {
            StackStepInfo<?> current = stack;
            var num = 6;
            while (current != null) {
                if (num >= 4)
                    Assertions.assertThat(current.exception()).isNotNull();
                else
                    Assertions.assertThat(current.exception()).isNull();
                num--;
                current = current.previous();
            }
        });
    }

    @Test
    void test09() {
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        final var lazy = WithCloseable.open(
                MockCloseable::new,
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1)/*,
                closeable -> finallyHolder.setValue(finallyHolder.value() + 1)*/
            )
            .map(c -> 1)
            .map(i -> i + 1)
            .mapWithCloseable((c, i) -> i / 0)
            .mapWithCloseable((c, i) -> i / 0)
            .filter(i -> i > 100)
            .fallBackEmpty(c -> 49 / 0)
            .map(i -> 99)
            .closeAndGetResult()
            //
            ;
        Assertions.assertThat(lazy.isSuccess()).isFalse();
        Assertions.assertThat(lazy.getStackStepInfo()).isNotEmpty();
        lazy.getStackStepInfo().ifPresent(stack -> {
            StackStepInfo<?> current = stack;
            var num = 6;
            while (current != null) {
                if (num >= 4)
                    Assertions.assertThat(current.exception()).isNotNull();
                else
                    Assertions.assertThat(current.exception()).isNull();
                num--;
                current = current.previous();
            }
        });
    }

    @Test
    void test10() {
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        final var lazy = WithCloseable.open(
                MockCloseable::new,
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1)/*,
                closeable -> finallyHolder.setValue(finallyHolder.value() + 1)*/
            )
            .map(c -> 1)
            .map(i -> i + 1)
            .flatMap(i -> ResultOrError.on(() -> i + 2)
                .map(t -> t + 3)
                .map(t -> t + 3)
                .map(t -> t + 3)
                .map(t -> t + 3)
            )
            .filter(i -> i > 100)
            .fallBackEmpty(c -> 49 / 0)
            .map(i -> 99)
            .closeAndGetResult()
            //
            ;
        Assertions.assertThat(lazy.isSuccess()).isFalse();
        Assertions.assertThat(lazy.getStackStepInfo()).isNotEmpty();
    }

    @Test
    void test11() {
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        final var lazy = WithCloseable.open(
                MockCloseable::new,
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1)/*,
                closeable -> finallyHolder.setValue(finallyHolder.value() + 1)*/
            )
            .map(c -> 1)
            .map(i -> i + 1)
            .flatMap(i -> ResultOrError.on(() -> i + 2)
                .map(t -> t + 3)
                .map(t -> t + 3)
                .map(t -> t + 3 / 0)
                .map(t -> t + 3)
            )
            .filter(i -> i > 100)
            .map(i -> 99)
            .closeAndGetResult()
            .traceDebugOrError(() -> true, System.out::println, () -> true, System.out::println);
        //
        ;
        Assertions.assertThat(lazy.isSuccess()).isFalse();
        Assertions.assertThat(lazy.getStackStepInfo()).isNotEmpty();
    }

    @Test
    void test12() {
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        ValueHolder<Integer> originalExceptionChecked = new ValueHolder<>(0);
        final var lazy = WithCloseable.open(
                MockCloseable::new,
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1)/*,
                closeable -> finallyHolder.setValue(finallyHolder.value() + 1)*/
            )
            .map(c -> 1)
            .map(i -> i + 1)
            .map(t -> t + 3 / 0)
            .flatMap(i -> ResultOrError.on(() -> i + 2)
                .map(t -> t + 3)
                .map(t -> t + 3)
                .map(t -> t + 3 / 0)
                .map(t -> t + 3)
            )
            .filter(i -> i > 100)
            .map(i -> 99)
            .closeAndGetResult()
            .doIfError(res -> {
                Assertions.assertThat(res.getError()).isInstanceOf(ArithmeticException.class);
                originalExceptionChecked.setValue(11);
            })
            .mapError(IllegalArgumentException.class, IllegalArgumentException::new)
            //
            ;
        Assertions.assertThat(lazy.isSuccess()).isFalse();
        Assertions.assertThat(lazy.getError()).isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThat(lazy.getStackStepInfo()).isNotEmpty();
        Assertions.assertThat(originalExceptionChecked.value()).isEqualTo(11);
    }

    @Test
    void test13() {
        var lazy = WithCloseable.open(MockCloseableErr2::new)
            .map(c -> 1);
        Assertions.assertThat(lazy.closeAndGetResult(IllegalArgumentException.class, IllegalArgumentException::new).getError())
            .isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThat(lazy.closeAndGetResult().getError())
            .isInstanceOf(Exception.class);
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isFalse();
        Assertions.assertThat(lazy.closeAndGetResult().getOrFallBackForError(ex -> 55)).isEqualTo(55);
    }

    @Test
    void test14() {
        var lazy = WithCloseable.open(MockCloseable::new)
            .map(c -> 1)
            .map(c -> null)
            .fallBackEmpty(e -> null)
            .flatMap(o -> ResultOrError.on(() -> 3));
        Assertions.assertThat(lazy.closeAndGetResult(IllegalArgumentException.class, IllegalArgumentException::new).getError())
            .isNull();
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isTrue();
        Assertions.assertThat(lazy.closeAndGetResult().getOption()).isEmpty();
    }

    @Test
    void test15() {
        var lazy = WithCloseable.open(MockCloseable::new)
            .map(c -> 1)
            .map(c -> c / 0)
            .continueWithOptional()
            .flatMap(o -> ResultOrError.on(() -> 3));
        Assertions.assertThat(lazy.closeAndGetResult(IllegalArgumentException.class, IllegalArgumentException::new).getError())
            .isNotNull();
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isFalse();
        Assertions.assertThat(lazy.closeAndGetResult().getError()).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void test16() {
        final var holder = ValueHolder.of(0);
        var lazy = WithCloseable.open(MockCloseable::new)
            .map(c -> 1)
            .map(c -> c / 0)
            .map(c -> c + 1)
            .continueWithOptional()
            .doOnError(((mockCloseable, exception) -> {
                Assertions.assertThat(mockCloseable).isNotNull();
                holder.setValue(holder.value() + 1);
            }))
            .flatMap(o -> ResultOrError.on(() -> {
                holder.setValue(holder.value() + 1);
                return 3;
            }));
        Assertions.assertThat(lazy.closeAndGetResult(IllegalArgumentException.class, IllegalArgumentException::new).getError())
            .isNotNull();
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isFalse();
        Assertions.assertThat(lazy.closeAndGetResult().getError()).isInstanceOf(ArithmeticException.class);

        Assertions.assertThat(holder.value()).isEqualTo(3);

        lazy.closeAndGetResult().doIf(ignored -> true, res -> {
            res.traceDebugOrError(() -> true, System.out::println, () -> true, System.out::println);
        });
        lazy.closeAndGetResult().getStackStepInfo().ifPresentOrElse(stack -> {
            StackStepInfo<?> current = stack;
            var num = 6;
            while (current != null) {
                num--;
                current = current.previous();
            }
            Assertions.assertThat(num).isZero();
        }, Assertions::assertThatException);
    }

    @Test
    void test17() {
        final var holder = ValueHolder.of(0);
        var lazy = WithCloseable.open(MockCloseable::new)
            .map(c -> 1)
            .map(c -> c + 1)
            .continueWithOptional()
            .doOnError(((mockCloseable, exception) -> {
                Assertions.assertThat(mockCloseable).isNotNull();
                holder.setValue(holder.value() + 1);
            }))
            .flatMap(o -> ResultOrError.on(() -> {
                holder.setValue(holder.value() -1);
                return 3;
            }));
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isTrue();
        lazy.closeAndGetResult().doIf(ignored -> true, res -> {
            res.traceDebugOrError(() -> true, System.out::println, () -> true, System.out::println);
        });

        Assertions.assertThat(holder.value()).isEqualTo(-2);

        lazy.closeAndGetResult().getStackStepInfo().ifPresentOrElse(stack -> {
            StackStepInfo<?> current = stack;
            var num = 6;
            while (current != null) {
                num--;
                current = current.previous();
            }
            Assertions.assertThat(num).isZero();
        }, Assertions::assertThatException);
    }

    @Test
    void test18() {
        final var holder = ValueHolder.of(0);
        var lazy = WithCloseable.open(MockCloseable::new)
            .map(c -> 1)
            .map(c -> c / 0)
            .map(c -> c + 1)
            .mapOnError(((mockCloseable, exception) -> {
                Assertions.assertThat(mockCloseable).isNotNull();
                holder.setValue(holder.value() + 1);
                return 999;
            }))
            .flatMap(o -> ResultOrError.on(() -> {
                holder.setValue(holder.value() + 1);
                return o;
            }));
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isTrue();
        Assertions.assertThat(lazy.closeAndGetResult().get()).isEqualTo(999);

        Assertions.assertThat(holder.value()).isEqualTo(4);

        lazy.closeAndGetResult().doIf(ignored -> true, res -> {
            res.traceDebugOrError(() -> true, System.out::println, () -> true, System.out::println);
        });
    }

    @Test
    void test19() {
        final var holder = ValueHolder.of(0);
        var lazy = WithCloseable.open(MockCloseable::new)
            .map(c -> 1)
            .map(c -> c + 1)
            .mapOnError(((mockCloseable, exception) -> {
                Assertions.assertThat(mockCloseable).isNotNull();
                holder.setValue(holder.value() + 1);
                return 999;
            }))
            .flatMap(o -> ResultOrError.on(() -> {
                holder.setValue(holder.value() + 1);
                return o;
            }));
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isTrue();
        Assertions.assertThat(lazy.closeAndGetResult().get()).isEqualTo(2);

        Assertions.assertThat(holder.value()).isEqualTo(2);

        lazy.closeAndGetResult().doIf(ignored -> true, res -> {
            res.traceDebugOrError(() -> true, System.out::println, () -> true, System.out::println);
        });
    }

    static class MockCloseable implements AutoCloseable {
        boolean closed = false;

        Runnable additional = () -> {

        };

        public boolean isClosed() {
            return closed;
        }

        @Override
        public void close() throws Exception {
            this.closed = true;
            this.additional.run();
        }

        MockCloseable(Runnable additional) {
            this.additional = additional;
        }

        MockCloseable() {
        }
    }

    static class MockCloseableErr implements AutoCloseable {

        MockCloseableErr() {
            throw new RuntimeException();
        }

        @Override
        public void close() throws Exception {
        }
    }

    static class MockCloseableErr2 implements AutoCloseable {

        MockCloseableErr2() {
        }

        @Override
        public void close() throws Exception {
            throw new Exception();
        }
    }
}
