package org.pitest.highwheel.util.base;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.fest.assertions.api.Assertions.assertThat;


public class OptionalTest {

    @Test(expected = AssertionError.class)
    public void ofShouldThrowAssertionErrorIfArgumentIsNull(){
        Optional.of(null);
    }

    @Test
    public void ofNullableShouldNotThrowAssertionErrorIfArgumentIsNull() {
        Optional.ofNullable(null);
    }

    @Test
    public void ofNullableOfNullShouldReturnEmpty() {
        assertThat(Optional.ofNullable(null) == Optional.empty()).isTrue();
    }

    @Test
    public void emptyShouldBeEqualToItself() {
        assertThat(Optional.empty()).isEqualTo(Optional.empty());
    }

    @Test
    public void emptyShouldBeReferentiallyIdenticalToItself() {
        assertThat(Optional.empty() == Optional.empty()).isTrue();
    }

    @Test(expected = AssertionError.class)
    public void getOnEmptyShouldThrowAssertionError() {
        Optional.empty().get();
    }

    @Test
    public void getOnNonEmptyShouldReturnValue() {
        assertThat(Optional.of(9).get()).isEqualTo(9);
    }

    @Test
    public void orElseShouldReturnDefaultOnEmpty() {
        assertThat(Optional.empty().orElse(9)).isEqualTo(9);
    }

    @Test
    public void orElseShouldReturnValueIfPresent() {
        assertThat(Optional.of(8).orElse(9)).isEqualTo(8);
    }

    @Test
    public void emptyShouldNotBePresent() {
        assertThat(Optional.empty().isPresent()).isFalse();
    }

    @Test
    public void presentShouldBePresent() {
        assertThat(Optional.of("hello").isPresent()).isTrue();
    }

    @Test
    public void mapShouldTransformValueIfPresent() {
        final Function<Integer,Integer> increase = new Function<Integer, Integer>() {
            public Integer apply(Integer argument) {
                return argument + 1;
            }
        };
        assertThat(Optional.of(9).map(increase).get()).isEqualTo(10);
    }

    @Test
    public void mapShouldKeepEmptyIfAbsent() {
        final Function<Integer,Integer> increase = new Function<Integer, Integer>() {
            public Integer apply(Integer argument) {
                return argument + 1;
            }
        };

        Optional<Integer> testee = Optional.empty();
        assertThat(testee.map(increase).isPresent()).isFalse();
    }

    @Test
    public void flatMapShouldTransformValueIfPresent() {
        final Function<Integer,Optional<Integer>> increase = new Function<Integer, Optional<Integer>>() {
            public Optional<Integer> apply(Integer argument) {
                return Optional.of(argument+1);
            }
        };

        Optional<Integer> testee = Optional.of(2);
        assertThat(testee.flatMap(increase).get()).isEqualTo(3);
    }

    @Test
    public void flatMapShouldKeepEmptyIfAbsent() {
        final Function<Integer,Optional<Integer>> increase = new Function<Integer, Optional<Integer>>() {
            public Optional<Integer> apply(Integer argument) {
                return Optional.of(argument+1);
            }
        };

        Optional<Integer> testee = Optional.empty();
        assertThat(testee.flatMap(increase).isPresent()).isFalse();
    }

    @Test
    public void shouldCallConsumerIfPresent() {
        final AtomicBoolean consumerCalled = new AtomicBoolean(false);
        final Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void consume(Integer value) {
                consumerCalled.set(true);
            }
        };

        final Optional<Integer> testee = Optional.of(10);
        testee.forEach(consumer);
        assertThat(consumerCalled.get()).isTrue();
    }

    @Test
    public void shouldNotCallConsumerIfAbsent() {
        final AtomicBoolean consumerCalled = new AtomicBoolean(false);
        final Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void consume(Integer value) {
                consumerCalled.set(true);
            }
        };

        final Optional<Integer> testee = Optional.empty();
        testee.forEach(consumer);
        assertThat(consumerCalled.get()).isFalse();
    }

    @Test
    public void orElseGetShouldCallSupplierIfEmpty() {
        final Supplier<Integer> supplier = new Supplier<Integer>() {
            @Override
            public Integer supply() {
                return 42;
            }
        };

        final Optional<Integer> testee = Optional.empty();

        assertThat(testee.orElseGet(supplier)).isEqualTo(42);
    }

    @Test
    public void orElseGetShouldNotCallSupplierIfDefined() {
        final AtomicBoolean consumerCalled = new AtomicBoolean(false);
        final Supplier<Integer> supplier = new Supplier<Integer>() {
            @Override
            public Integer supply() {
                consumerCalled.set(true);
                return 42;
            }
        };

        final Optional<Integer> testee = Optional.of(53);

        assertThat(testee.orElseGet(supplier)).isEqualTo(53);
        assertThat(consumerCalled.get()).isFalse();
    }

    @Test(expected = RuntimeException.class)
    public void orThrowShouldThrowExceptionOnEmpty() {
        Optional.<Object>empty().orThrow(new RuntimeException());
    }

    @Test
    public void orThrowShouldNotThrowExceptionOnPresent() {
        Optional.of("asdf").orThrow(new RuntimeException());
    }
}
