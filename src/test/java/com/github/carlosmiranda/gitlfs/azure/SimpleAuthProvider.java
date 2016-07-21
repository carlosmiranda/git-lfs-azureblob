/**
 * Copyright (C) 2016 Carlos Miranda
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.carlosmiranda.gitlfs.azure;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.auth.CachedAuthProvider;
import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Operation;

/**
 * Simple Auth Provider implementation for testing.
 * @author Carlos Miranda (miranda.cma+azureblob.gitlfs@gmail.com)
 */
public final class SimpleAuthProvider extends CachedAuthProvider {

    /**
     * Server URI.
     */
    private final URI uri;

    /**
     * Ctor.
     * @param path Path to Git repo.
     * @param port Port of server.
     * @throws URISyntaxException If there is a URI syntax error.
     */
    public SimpleAuthProvider(final String path, final int port)
        throws URISyntaxException {
        this.uri = new URI(
            String.format("http://localhost:%d/%s", port, path)
        ).resolve(String.format("%s/info/lfs", path));
    }

    @Override
    @NotNull
    protected Link getAuthUncached(
        @NotNull final Operation operation) throws IOException,
        InterruptedException {
        return new Link(this.uri, Collections.emptyMap(), null);
    }
}
