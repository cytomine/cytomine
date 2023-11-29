package io.github.ya_b.registry.client.image;

import java.io.IOException;

@FunctionalInterface
public interface Supplier<T> {
    T get() throws IOException;
}
