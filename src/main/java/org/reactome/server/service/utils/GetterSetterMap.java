package org.reactome.server.service.utils;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GetterSetterMap {

    // Simple holder for getter + setter
    public static class Accessor<R, S, T> {
        public final Class<S> sourceClass;
        public final Function<S, R> sourceGetter;
        public final Function<T, R> targetGetter;
        public final BiConsumer<T, R> setter;

        Accessor(Function<S, R> sourceGetter, Function<T, R> targetGetter, BiConsumer<T, R> setter, Class<S> sourceClass) {
            this.sourceGetter = sourceGetter;
            this.targetGetter = targetGetter;
            this.setter = setter;
            this.sourceClass = sourceClass;
        }

        @Override
        public String toString() {
            return "Accessor{" +
                    "sourceClass=" + sourceClass +
                    ", getter=" + sourceGetter +
                    ", setter=" + setter +
                    '}';
        }
    }

    // Static helper for Map.ofEntries
    public static <R, S, T> Map.Entry<Function<S, R>, BiConsumer<T, R>> entry(
            Function<S, R> getter, BiConsumer<T, R> setter) {
        return Map.entry(getter, setter);
    }

    public static <R, S, T> Accessor<R, S, T> accessor(Function<S, R> sourceGetter, Function<T, R> targetGetter, BiConsumer<T, R> setter, Class<S> sourceClass) {
        return new Accessor<>(sourceGetter, targetGetter, setter, sourceClass);
    }

    // Unsafe, but unavoidable: cast to generic with helper method
    @SuppressWarnings("unchecked")
    public static <R, S, T extends S> Accessor<R, S, T> accessor(Function<S, R> getter, BiConsumer<T, R> setter, Class<S> sourceClass) {
        return new Accessor<>(getter, (Function<T, R>) getter, setter, sourceClass);
    }
}
