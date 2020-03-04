/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artpie.nuget.http;

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.http.Response;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;

/**
 * Package content route.
 * See <a href="https://docs.microsoft.com/en-us/nuget/api/package-base-address-resource">Package Content</a>
 *
 * @since 0.1
 */
public final class PackageContent implements Route {

    /**
     * Storage to read content from.
     */
    private final Storage storage;

    /**
     * Ctor.
     *
     * @param storage Storage to read content from.
     */
    public PackageContent(final Storage storage) {
        this.storage = storage;
    }

    @Override
    public String path() {
        return "/content";
    }

    @Override
    public Resource resource(final String path) {
        return new Value(path, this.storage);
    }

    /**
     * Package content resource.
     *
     * @since 0.1
     */
    private class Value implements Resource {

        /**
         * Resource path.
         */
        private final String path;

        /**
         * Storage to read content from.
         */
        private final Storage storage;

        /**
         * Ctor.
         *
         * @param path Resource path.
         * @param storage Storage to read content from.
         */
        Value(final String path, final Storage storage) {
            this.path = path;
            this.storage = storage;
        }

        @Override
        public Response get() {
            return connection -> this.existing()
                .thenCompose(
                    found -> {
                        final CompletionStage<Void> sent;
                        if (found.isPresent()) {
                            sent = this.storage.value(found.get()).thenCompose(
                                data -> connection.accept(
                                    RsStatus.OK,
                                    Collections.emptyList(),
                                    data
                                )
                            );
                        } else {
                            sent = new RsWithStatus(RsStatus.NOT_FOUND).send(connection);
                        }
                        return sent;
                    }
                );
        }

        @Override
        public Response put(final Publisher<ByteBuffer> body) {
            return new RsWithStatus(RsStatus.METHOD_NOT_ALLOWED);
        }

        /**
         * Try build key from path and check if it exists.
         *
         * @return Key to storage value, if value exists.
         */
        private CompletableFuture<Optional<Key>> existing() {
            return CompletableFuture.supplyAsync(this::key)
                .thenCompose(
                    parsed -> {
                        final CompletableFuture<Optional<Key>> found;
                        if (parsed.isPresent()) {
                            final Key key = parsed.get();
                            found = this.storage.exists(key).thenApply(
                                exists -> {
                                    final Optional<Key> existing;
                                    if (exists) {
                                        existing = Optional.of(key);
                                    } else {
                                        existing = Optional.empty();
                                    }
                                    return existing;
                                }
                            );
                        } else {
                            found = CompletableFuture.completedFuture(Optional.empty());
                        }
                        return found;
                    }
                );
        }

        /**
         * Tries to build key to storage value from path.
         *
         * @return Key to storage value, if there is one.
         */
        private Optional<Key> key() {
            final String base = String.format("%s/", path());
            final Optional<Key> parsed;
            if (this.path.startsWith(base)) {
                parsed = Optional.of(new Key.From(this.path.substring(base.length())));
            } else {
                parsed = Optional.empty();
            }
            return parsed;
        }
    }
}
