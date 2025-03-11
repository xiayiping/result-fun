package org.xyp.functional.result.wrapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xyp.functional.result.Fun;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

class ResultOrErrorTest {

    static Logger log = LoggerFactory.getLogger(ResultOrErrorTest.class);

    @Test
    void test1() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .getResult()
            .getOption();

        Assertions.assertThat(opt).isEmpty();
    }

    @Test
    void test2() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .fallbackForEmpty(() -> 996)
            .getResult()
            .getOption();

        Assertions.assertThat(opt).isNotEmpty();
        Assertions.assertThat(opt.get()).isEqualTo(996);
    }

    @Test
    void test3() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .noExMap(i -> i - 4)
            .filter(i -> i > 0)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .getOption();

        Assertions.assertThat(opt).isEmpty();
    }

    @Test
    void test4() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .noExMap(i -> i)
            .map(i -> i + 100)
            .fallbackForEmpty(() -> 996)
            .getOption();

        Assertions.assertThat(opt).isNotEmpty();
        Assertions.assertThat(opt.get()).isEqualTo(996);
    }

    @Test
    void test5() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .fallbackForEmpty(() -> 996)
            .map(i -> new Person("nn", i))
            .map(Fun.updateSelf(p -> p.setAge(77)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(77)))
            .map(Fun.updateSelf(p -> p.setAge(66)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(66)))
            .getOption();

        Assertions.assertThat(opt).isNotEmpty();
        Assertions.assertThat(opt.get().getName()).isEqualTo("nn");
    }

    @Test
    void test6() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .fallbackForEmpty(() -> 996)
            .map(i -> new Person("nn", i))
            .map(Fun.updateSelf(p -> p.setAge(77)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(77)))
            .map(Fun.updateSelf(p -> p.setAge(66)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(66)))
            .map(Fun.updateSelf(p -> p.setAge(66)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(66)))
            .map(r -> Fun.cast(Person.class).apply(r))
            .map(i -> i.orElse(null))
            .getOption();
        Assertions.assertThat(opt).isNotEmpty();
        Assertions.assertThat(opt.get().getName()).isEqualTo("nn");
    }

    @Test
    void test7() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .fallbackForEmpty(() -> 996)
            .map(i -> new Person("nn", i))
            .map(Fun.updateSelf(p -> p.setAge(77)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(77)))
            .map(Fun.updateSelf(p -> p.setAge(66)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(66)))
            .noExMap(Fun.cast(Integer.class))
            .map(i -> i.orElse(null))
            .getOption();
        Assertions.assertThat(opt).isEmpty();
    }

    @Test
    void test8() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .fallbackForEmpty(() -> 996)
            .map(i -> new Person("nn", i))
            .map(Fun.updateSelf(p -> p.setAge(77)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(77)))
            .map(Fun.updateSelf(p -> p.setAge(66)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(66)))
            .map(r -> Fun.cast(Object.class).apply(r))
            .map(i -> i.orElse(null))
            .getOption();
        Assertions.assertThat(opt).isNotEmpty();
        Assertions.assertThat(opt.get().getClass()).isEqualTo(Person.class);
    }

    @Test
    void test9() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .fallbackForEmpty(() -> 996);
        Assertions.assertThat(opt.getOption()).isNotEmpty();
        Assertions.assertThat(opt.get()).isEqualTo(996);
        Assertions.assertThat(opt.getOrSpecError(RuntimeException.class, ex -> new RuntimeException())).isEqualTo(996);
        Assertions.assertThat(opt.getResult().getOptionEvenErr(e -> null)).isNotEmpty();
        Assertions.assertThat(opt.getResult().getOptionEvenErr(e -> null).get()).isEqualTo(996);
    }

    @Test
    void test10() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .fallbackForEmpty(() -> 996);

        Assertions.assertThat(opt.getOption()).isNotEmpty();
        Assertions.assertThat(opt.getOptionOrSpecError(RuntimeException.class, ex -> new RuntimeException())).isNotEmpty();
        Assertions.assertThat(opt.get()).isEqualTo(996);
        ValueHolder<Integer> vh = new ValueHolder<>(0);
        Assertions.assertThat(
            opt.getResult().ifError(er -> vh.setValue(100))
                .isSuccess()).isTrue();
        Assertions.assertThat(vh.value()).isZero();
        Assertions.assertThat(opt.getResult().isSuccess()).isTrue();
        Assertions.assertThat(opt.getResult().getStackStepInfo()).isNotEmpty();
        opt.getResult()
            .getStackStepInfo().ifPresent(stack -> {
                int count = 0;
                StackStepInfo<?> curent = stack;
                while (curent != null) {
                    System.out.println(curent.stackFrame());
                    System.out.println(curent.input());
                    System.out.println(curent.output());
                    count += 1;
                    curent = curent.previous();
                }

                Assertions.assertThat(count).isEqualTo(5);
            });
    }

    @Test
    void test11() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .fallbackForEmpty(() -> 996)
            .map(o -> o / 0)
            .getResult();
        Assertions.assertThat(opt.isSuccess()).isFalse();
        Assertions.assertThatThrownBy(opt::getOption)
            .isInstanceOf(ArithmeticException.class);
        ValueHolder<Integer> vh = new ValueHolder<>(0);
        opt.ifError(er -> vh.setValue(100));
        Assertions.assertThat(vh.value()).isEqualTo(100);

        Assertions.assertThatThrownBy(() -> opt.getOptionOrSpecError(IllegalArgumentException.class, ex -> new IllegalArgumentException()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void test12() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .fallbackForEmpty(() -> 996)
            .map(o -> o / 0);
        Assertions.assertThat(opt.getResult().isSuccess()).isFalse();
        Assertions.assertThatThrownBy(() -> opt.getOrSpecError(ValidateException.class, ex -> new ValidateException(ex.getMessage())))
            .isInstanceOf(ValidateException.class);

        Assertions.assertThatThrownBy(opt::get)
            .isInstanceOf(RuntimeException.class)
        ;
    }

    @Test
    void test13() {
        final var opt = ResultOrError.of(1)
            .noExMap(i -> i)
            .filter(i -> i > 1000)
            .flatMap(ResultOrError::of)
            .fallbackForEmpty(() -> 1)
            .fallbackForEmpty(() -> 100)
            .get();
        Assertions.assertThat(opt).isOne();
    }

    @Test
    void test14() {
        final var lazy = ResultOrError.of(1)
            .noExMap(i -> i)
            .map(i -> i / 0);
        Assertions.assertThatThrownBy(lazy::get)
            .isInstanceOf(ArithmeticException.class);
        ;
        Assertions.assertThatThrownBy(() -> lazy.getOrSpecError(IllegalArgumentException.class, ex -> new IllegalArgumentException())
                .equals(88)
            )
            .isInstanceOf(IllegalArgumentException.class);
        ;
    }

    @Test
    void test15() {
        final var lazy = ResultOrError.of(1)
            .noExMap(i -> i)
            .map(i -> i / 0);
        Assertions.assertThatThrownBy(lazy::getOption)
            .isInstanceOf(ArithmeticException.class);
        Assertions.assertThatThrownBy(() -> lazy.getOptionOrSpecError(IllegalArgumentException.class, ex -> new IllegalArgumentException())
                .ifPresent(e -> {
                }))
            .isInstanceOf(IllegalArgumentException.class);

        Assertions.assertThat(lazy.getResult().getOptionEvenErr(e -> null)).isEmpty();
        Assertions.assertThat(lazy.getResult().getOptionEvenErr(e -> 71)).isEqualTo(Optional.of(71));
        ;
    }

    @Test
    void test16() {
        final var o = Stream.of(new Person("n", 2))
            .map(Fun.consumeSelf(p -> p.setAge(22)))
            .findFirst().get();
        Assertions.assertThat(o.getAge()).isEqualTo(22);

        final var castResult = Fun.checkAndCast(null, Object.class);
        Assertions.assertThat(castResult).isEmpty();
    }

    @Test
    void test18() {
        final var opt = ResultOrError.of(1)
            .noExMap(i -> i)
            .consume(i -> System.out.println())
            .fallbackForEmpty(() -> 100)
            .flatMap(i -> ResultOrError.on(() -> i / 0))
            .fallbackForEmpty(() -> 1);
        Assertions.assertThatThrownBy(opt::get).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void test19() {
        final var opt = ResultOrError.of(2)
            .noExMap(i -> i)
            .fallbackForEmpty(() -> 100)
            .flatMap(i -> ResultOrError.on(() -> i + 1)
                .map(t -> t + 2)
                .map(t -> t + 2)
                .map(t -> t + 2)
                .map(t -> t + 2)
            )
            .fallbackForEmpty(() -> 1);
        Assertions.assertThat(opt.get()).isEqualTo(11);
    }

    @Test
    void test20() {
        ValueHolder<Integer> vh = new ValueHolder<>(0);
        Assertions.assertThat(vh.value()).isZero();
        final var doRun = ResultOrError.doRun(() -> vh.setValue(1));
        Assertions.assertThat(vh.value()).isZero();
        doRun.getResult();
        Assertions.assertThat(vh.value()).isOne();

    }

    @Test
    void test21() {
        final var opt = ResultOrError.of(1)
            .noExMap(i -> i)
            .filter(i -> i > 1000)
            .flatMap(ResultOrError::of)
            .fallbackForEmpty(() -> 1 / 0)
            .fallbackForEmpty(() -> 100)
            .getResult();
        Assertions.assertThat(opt.isSuccess()).isFalse();
    }

    @Test
    void test22() {
        final var opt = ResultOrError.of(1)
            .map(i -> i + 1)
            .map(i -> i + 1)
            .map(i -> i + 1)
            .map(i -> i + 1)
            .map(i -> i + 1)
            .getResult();
        Assertions.assertThat(opt.isSuccess()).isTrue();
        Assertions.assertThat(opt.getStackStepInfo()).isNotEmpty();
        opt.getStackStepInfo().ifPresent(stack -> {
            StackStepInfo<?> current = stack;
            var num = 6;
            boolean first = true;
            while (current != null) {
                if (first) {
                    first = false;
                } else {
                    if (num > 1)
                        Assertions.assertThat(current.input()).isEqualTo(num - 1);
                    Assertions.assertThat(current.output()).isEqualTo(num);
                    num--;
                }
                current = current.previous();
            }
        });
    }

    @Test
    void test23() {
        final var opt = ResultOrError.of(1)
            .map(i -> i + 1)
            .map(i -> i + 1)
            .map(i -> i / 0)
            .filter(i -> i > 1000)
            .getResult();
        Assertions.assertThat(opt.isSuccess()).isFalse();
        Assertions.assertThat(opt.getStackStepInfo()).isNotEmpty();
        opt.getStackStepInfo().ifPresent(stack -> {
            StackStepInfo<?> current = stack;
            var num = 6;
            while (current != null) {
                if (num >= 5) {
                    Assertions.assertThat(current.exception()).isNotNull();
                } else {
                    Assertions.assertThat(current.exception()).isNull();
                }
                num--;
                current = current.previous();
            }
        });
    }

    @Test
    void test24() {
        final var opt = ResultOrError.of(1)
            .map(i -> i + 1)
            .map(i -> i + 1)
            .filter(i -> i > i / 0)
            .map(i -> i + 1)
            .getResult();
        Assertions.assertThat(opt.isSuccess()).isFalse();
        Assertions.assertThat(opt.getStackStepInfo()).isNotEmpty();
        opt.getStackStepInfo().ifPresent(stack -> {
            StackStepInfo<?> current = stack;
            var num = 6;
            while (current != null) {
                if (num >= 5) {
                    Assertions.assertThat(current.exception()).isNotNull();
                } else {
                    Assertions.assertThat(current.exception()).isNull();
                }
                num--;
                current = current.previous();
            }
        });
    }

    @Test
    void test25() {
        final var opt = ResultOrError.of(1)
            .map(i -> i + 1)
            .map(i -> i + 1)
            .consume(i -> {
                var t = i / 0;
                return;
            })
            .map(i -> i + 1)
            .getResult();
        Assertions.assertThat(opt.isSuccess()).isFalse();
        Assertions.assertThat(opt.getStackStepInfo()).isNotEmpty();
        opt.getStackStepInfo().ifPresent(stack -> {
            StackStepInfo<?> current = stack;
            var num = 6;
            while (current != null) {
                if (num >= 5) {
                    Assertions.assertThat(current.exception()).isNotNull();
                } else {
                    Assertions.assertThat(current.exception()).isNull();
                }
                num--;
                current = current.previous();
            }
        });
    }

    @Test
    void test26() {
        final var opt = ResultOrError.of(1)
            .map(i -> i + 1)
            .map(i -> i / 0)
            .consume(i -> {
                var t = i / 0;
                return;
            })
            .flatMap(i -> ResultOrError.on(() -> i / 1))
            .map(i -> i + 1)
            .getResult();
        Assertions.assertThat(opt.isSuccess()).isFalse();
        Assertions.assertThat(opt.getStackStepInfo()).isNotEmpty();
        opt.getStackStepInfo().ifPresent(stack -> {
            StackStepInfo<?> current = stack;
            var num = 6;
            while (current != null) {
                if (num >= 5) {
                    Assertions.assertThat(current.exception()).isNotNull();
                } else {
                    Assertions.assertThat(current.exception()).isNull();
                }
                num--;
                current = current.previous();
            }
        });
    }

    @Test
    void test27() {
        final var opt = ResultOrError.of(1)
            .map(i -> i + 1)
            .map(i -> i / 0)
            .consume(i -> {
                var t = i / 0;
                return;
            })
            .flatMap(i -> ResultOrError.on(() -> i / 1))
            .map(i -> i + 1);

        Assertions.assertThatThrownBy(() -> opt.getOrSpecError(IllegalStateException.class, IllegalStateException::new))
        ;
    }

    @Test
    void test28() {
        final var opt = ResultOrError.of(2)
            .noExMap(i -> i)
            .fallbackForEmpty(() -> 100)
            .flatMap(i -> ResultOrError.on(() -> i + 1)
                .map(t -> t + 2)
                .map(t -> t + 2)
                .map(t -> t + 2 / 0)
                .map(t -> t + 2)
            )
            .fallbackForEmpty(() -> 1);
        Assertions.assertThat(opt.getResult().isSuccess()).isFalse();
    }

    @Test
    void test29() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .fallbackForEmpty(() -> 996);
        Assertions.assertThat(opt.getOption()).isNotEmpty();
        Assertions.assertThat(opt.get()).isEqualTo(996);
        Assertions.assertThat(opt.getOrSpecErrorBy(RuntimeException.class, ex -> new RuntimeException())).isEqualTo(996);
        Assertions.assertThat(opt.getResult().getOptionEvenErr(e -> 13)).isNotEmpty();
        Assertions.assertThat(opt.getResult().getOptionEvenErr(e -> 13).get()).isEqualTo(996);
    }

    @Test
    void test30() {
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .fallbackForEmpty(() -> 996)
            .map(o -> o / 0)
            .getResult();
        Assertions.assertThat(opt.isSuccess()).isFalse();
        Assertions.assertThatThrownBy(() -> opt.getOrSpecErrorBy(
                ValidateWithStackException.class, result -> new ValidateWithStackException(
                    result.getError().getMessage(),
                    result.getStackStepInfo().orElse(null)))
            )
            .isInstanceOf(ValidateWithStackException.class)
        ;

        Assertions.assertThatThrownBy(opt::get)
            .isInstanceOf(RuntimeException.class)
        ;
    }

    @Test
    void test31() {
        final var opt = ResultOrError.of(1)
            .map(i -> i + 1)
            .map(i -> i / 0)
            .consume(i -> {
                var t = i / 0;
                return;
            })
            .flatMap(i -> ResultOrError.on(() -> i / 1))
            .map(i -> i + 1);

        Assertions.assertThatThrownBy(() -> opt.getOrSpecErrorBy(ValidateWithStackException.class,
                res -> new ValidateWithStackException(res.getError().getMessage(), res.getStackStepInfo().orElse(null))).equals(1)
            )
            .isInstanceOf(ValidateWithStackException.class)
            .matches(tr -> {
                return tr instanceof ValidateWithStackException vv && vv.getStackStepInfo() != null;
            })
        ;
    }

    @Test
    void test32() {
        final var lazy = ResultOrError.of(1)
            .noExMap(i -> i)
            .map(i -> i / 0);
        Assertions.assertThatThrownBy(lazy::getOption)
            .isInstanceOf(ArithmeticException.class);
        Assertions.assertThatThrownBy(() -> lazy.getOptionOrSpecErrorBy(
                    ValidateWithStackException.class,
                    ex -> new ValidateWithStackException(ex.getError().getMessage(), ex.getStackStepInfo().orElse(null)))
                .ifPresent(e -> {
                }))
            .isInstanceOf(ValidateWithStackException.class)
            .matches(tr -> {
                return tr instanceof ValidateWithStackException vv && vv.getStackStepInfo() != null;
            })
        ;

        ValueHolder<Integer> ss = new ValueHolder<>(0);
        lazy.getResult().doIfError(result -> {
            Assertions.assertThat(result.getError()).isNotNull();
            Assertions.assertThat(result.getStackStepInfo()).isNotNull();
            ss.setValue(1);
        }).equals(4);
        Assertions.assertThat(ss.value()).isOne();

        Assertions.assertThat(lazy.getResult().getOptionEvenErr(e -> null)).isEmpty();
        Assertions.assertThat(lazy.getResult().getOptionEvenErr(e -> 83)).isEqualTo(Optional.of(83));
        ;
    }

    @Test
    void test33() {
        final var lazy = ResultOrError.of(1)
            .map(i -> i / 0);
        Assertions.assertThatThrownBy(() -> lazy.getResult().getOrSpecError(ArithmeticException.class, ex -> {
                throw new RuntimeException();
            }))
            .isInstanceOf(ArithmeticException.class);
        Assertions.assertThatThrownBy(() -> lazy.getResult().getOrSpecErrorBy(ArithmeticException.class, res -> {
                throw new RuntimeException();
            }))
            .isInstanceOf(ArithmeticException.class);
        Assertions.assertThatThrownBy(() -> lazy.getResult().getOptionOrSpecError(ArithmeticException.class, ex -> {
                throw new RuntimeException();
            }))
            .isInstanceOf(ArithmeticException.class);
        Assertions.assertThatThrownBy(() -> lazy.getResult().getOptionOrSpecErrorBy(ArithmeticException.class, res -> {
                throw new RuntimeException();
            }))
            .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void test34() {

        final var lazy = ResultOrError.of(1)
            .map(i -> i / 1);

        Assertions.assertThat(lazy.getResult().getError()).isNull();
        Assertions.assertThat(lazy.getResult().getOptionOrSpecErrorBy(ArithmeticException.class, e -> new ArithmeticException()))
            .isNotEmpty();
    }

    @Test
    void test35() {
        final var opt = ResultOrError.of(Map.<String, Object>of())
            .map(i -> i)
            .map(i -> ((Number) i.get("s")).longValue())
            .getResult()
            .getOrFallBackForError(ex -> 999L);
        Assertions.assertThat(opt).isEqualTo(999);
    }

    @Test
    void test36() {
        ValueHolder<Boolean> continueOptionChecked = new ValueHolder<>(false);
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .continueWithOptional()
            .map(ww -> {
                Assertions.assertThat(ww).isEmpty();
                continueOptionChecked.setValue(true);
                return 1;
            })
            .getResult()
            .getOption();

        Assertions.assertThat(opt).isNotEmpty();
        Assertions.assertThat(opt.get()).isOne();
        Assertions.assertThat(continueOptionChecked.value()).isTrue();
    }

    @Test
    void test37() {
        final var opt = ResultOrError.of(Map.<String, Object>of())
            .map(i -> i)
            .map(i -> ((Number) i.get("s")).longValue())
            .getResult(ValidateException.class, e -> new ValidateException(e.getMessage()))
            .doIfError(System.out::println);
        Assertions.assertThat(opt.isSuccess()).isFalse();
        Assertions.assertThat(opt.getError()).isInstanceOf(ValidateException.class);

    }

    @Test
    void test38() {
        ValueHolder<Boolean> continueOptionChecked = new ValueHolder<>(false);
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .continueWithOptional()
            .map(ww -> {
                Assertions.assertThat(ww).isEmpty();
                continueOptionChecked.setValue(true);
                return 1;
            })
            .getResult()
            .mapError(ValidateException.class, e -> new ValidateException(e.getMessage()))
            .getOption();

        Assertions.assertThat(opt).isNotEmpty();
        Assertions.assertThat(opt.get()).isOne();
        Assertions.assertThat(continueOptionChecked.value()).isTrue();
    }

    @Test
    void test39() {
        ValueHolder<Boolean> continueOptionChecked = new ValueHolder<>(false);
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .map(i -> i / 0)
            .continueWithOptional()
            .map(ww -> {
                Assertions.assertThat(ww).isEmpty();
                continueOptionChecked.setValue(true);
                return 1;
            })
            .getResult()
            .mapError(ValidateException.class, e -> new ValidateException(e.getMessage()));

        Assertions.assertThat(opt.isSuccess()).isFalse();
        Assertions.assertThat(opt.getError()).isInstanceOf(ValidateException.class);
        Assertions.assertThat(continueOptionChecked.value()).isFalse();
    }

    @Test
    void test40() {
        ValueHolder<Boolean> continueOptionChecked = new ValueHolder<>(false);
        ValueHolder<Boolean> doOnErrorChecked = new ValueHolder<>(false);
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .map(i -> i / 0)
            .continueWithOptional()
            .doOnError((e) -> doOnErrorChecked.setValue(true))
            .map(ww -> {
                Assertions.assertThat(ww).isEmpty();
                continueOptionChecked.setValue(true);
                return 1;
            })
            .map(ww -> ww + 1)
            .map(ww -> ww + 1)
            .getResult()
            .mapError(ValidateException.class, e -> new ValidateException(e.getMessage()))

            .doIf(ignored -> true, res -> {
                res.traceDebugOrError(log::isDebugEnabled, System.out::println, () -> true, System.out::println);
            });

        Assertions.assertThat(opt.isSuccess()).isFalse();
        Assertions.assertThat(opt.getError()).isInstanceOf(ValidateException.class);
        Assertions.assertThat(continueOptionChecked.value()).isFalse();
        Assertions.assertThat(doOnErrorChecked.value()).isTrue();

        opt.getStackStepInfo().ifPresentOrElse(stack -> {
            StackStepInfo<?> current = stack;
            var num = 5;
            while (current != null) {
                num--;
                current = current.previous();
            }
            Assertions.assertThat(num).isZero();
        }, Assertions::assertThatException);
    }

    @Test
    void test41() {
        ValueHolder<Boolean> continueOptionChecked = new ValueHolder<>(false);
        ValueHolder<Boolean> doOnErrorChecked = new ValueHolder<>(false);
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .map(i -> i / 1)
            .continueWithOptional()
            .doOnError((e) -> doOnErrorChecked.setValue(true))
            .map(ww -> {
                continueOptionChecked.setValue(true);
                return 1;
            })
            .map(ww -> ww + 1)
            .map(ww -> ww + 1)
            .getResult()
            .mapError(ValidateException.class, e -> new ValidateException(e.getMessage()))

            .doIf(ignored -> true, res -> {
                res.traceDebugOrError(() -> true, System.out::println, () -> true, System.out::println);
            });

        Assertions.assertThat(opt.isSuccess()).isTrue();
        Assertions.assertThat(continueOptionChecked.value()).isTrue();
        Assertions.assertThat(doOnErrorChecked.value()).isFalse();

        opt.getStackStepInfo().ifPresentOrElse(stack -> {
            StackStepInfo<?> current = stack;
            var num = 8;
            while (current != null) {
                num--;
                current = current.previous();
            }
            Assertions.assertThat(num).isZero();
        }, Assertions::assertThatException);
    }

    @Test
    void test42() {
        ValueHolder<Boolean> doOnErrorChecked = new ValueHolder<>(false);
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .map(i -> i / 0)
            .mapOnError(e -> {
                doOnErrorChecked.setValue(true);
                return -999;
            })
            .map(ww -> ww + 1)
            .map(ww -> ww + 1)
            .getResult()
            .mapError(ValidateException.class, e -> new ValidateException(e.getMessage()))
            .doIf(ignored -> true, res -> {
                res.traceDebugOrError(() -> true, System.out::println, () -> true, System.out::println);
            });

        Assertions.assertThat(opt.isSuccess()).isTrue();
        Assertions.assertThat(opt.getError()).isNull();
        Assertions.assertThat(opt.get()).isEqualTo(-997);
        Assertions.assertThat(doOnErrorChecked.value()).isTrue();
    }

    @Test
    void test43() {
        ValueHolder<Boolean> doOnErrorChecked = new ValueHolder<>(false);
        final var opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .mapOnError(e -> {
                doOnErrorChecked.setValue(true);
                return -999;
            })
            .map(ww -> ww + 1)
            .map(ww -> ww + 1)
            .getResult()
            .mapError(ValidateException.class, e -> new ValidateException(e.getMessage()))
            .doIf(ignored -> true, res -> {
                res.traceDebugOrError(() -> true, System.out::println, () -> true, System.out::println);
            });

        Assertions.assertThat(opt.isSuccess()).isTrue();
        Assertions.assertThat(doOnErrorChecked.value()).isFalse();
        Assertions.assertThat(opt.get()).isEqualTo(-1);
    }


    static
    class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

}
