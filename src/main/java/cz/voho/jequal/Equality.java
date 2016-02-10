package cz.voho.jequal;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public interface Equality<T> {
    static <T> Equality.Builder<T> onType(final Class<T> type) {
        return new Builder<T>(type);
    }

    boolean equals(final T first, final Object second);

    int hashCode(final T first);

    class Builder<T> {
        private final Class<T> type;
        private final List<Function<T, ?>> valueExtractors;
        private boolean allowSubTypes;

        public Builder(final Class<T> type) {
            this.type = type;
            valueExtractors = new LinkedList<>();
            allowSubTypes = false;
        }

        public Builder<T> orAnySubType() {
            allowSubTypes = true;
            return this;
        }

        public Builder<T> checkEquality(final Function<T, ?> valueExtractor) {
            valueExtractors.add(valueExtractor);
            return this;
        }

        public Equality<T> define() {
            return new Equality<T>() {
                public boolean equals(final T first, final Object second) {
                    if (first == second) {
                        return true;
                    }
                    if (first == null || second == null) {
                        return false;
                    }
                    if (allowSubTypes) {
                        if (!type.isInstance(first) || !type.isInstance(second)) {
                            return false;
                        }
                    } else {
                        if (type != first.getClass() || type != second.getClass()) {
                            return false;
                        }
                    }
                    final T secondTyped = (T) second;

                    for (final Function<T, ?> valueExtractor : valueExtractors) {
                        final Object firstValue = valueExtractor.apply(first);
                        final Object secondValue = valueExtractor.apply(secondTyped);

                        if (!Objects.equals(firstValue, secondValue)) {
                            return false;
                        }
                    }

                    return true;
                }

                public int hashCode(final T first) {
                    if (first == null) {
                        return 0;
                    }

                    int result = 1;

                    for (final Function<T, ?> valueExtractor : valueExtractors) {
                        final Object firstValue = valueExtractor.apply(first);
                        result = 31 * result + Objects.hashCode(firstValue);
                    }

                    return result;
                }
            };
        }
    }
}
