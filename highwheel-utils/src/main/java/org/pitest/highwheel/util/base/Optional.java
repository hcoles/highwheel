package org.pitest.highwheel.util.base;

import org.pitest.highwheel.util.validation.Objects;

/**
 * Backport of the Optional type for Java 6 compatibility
 */
public final class Optional<T> {

    private final T value;

    @SuppressWarnings("unchecked")
    private final static Optional EMPTY = new Optional(null);

    private Optional(T value) {
        this.value = value;
    }

    public static <S> Optional<S> of(S value){
        Objects.requireNotNull(value);
        return new Optional<S>(value);
    }

    public static <S> Optional<S> ofNullable(S value) {
        if(value == null)
            return Optional.empty();
        else
            return new Optional<S>(value);
    }

    @SuppressWarnings("unchecked")
    public static <S> Optional<S> empty() {
        return (Optional<S>) EMPTY;
    }

    public T get() {
        Objects.requireNotNull(value);
        return value;
    }

    public T orElse(T defaultValue) {
        if(value == null)
            return defaultValue;
        else
            return value;
    }

    public boolean isPresent(){
        return value != null;
    }

    public <S> Optional<S> map(Function<? super T,? extends S> function) {
        if(value == null)
            return Optional.empty();
        else
            return new Optional<S>(function.apply(value));
    }

    public <S> Optional<S> flatMap(Function<? super T,Optional<S>> function) {
        if(value == null)
            return Optional.empty();
        else
            return function.apply(value);
    }

    public void forEach(Consumer<? super T> consumer) {
        if(value != null)
            consumer.consume(value);
    }

    public T orElseGet(Supplier<? extends T> supplier) {
        if(value == null)
            return supplier.supply();
        else
            return value;
    }
}
