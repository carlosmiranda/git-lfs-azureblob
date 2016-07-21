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

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.server.ContentManager;
import ru.bozaro.gitlfs.server.ContentServlet;
import ru.bozaro.gitlfs.server.PointerServlet;

/**
 * Azure Server for Git LFS.
 * @author Carlos Miranda (miranda.cma+azureblob.gitlfs@gmail.com)
 */
final class LfsServer implements AutoCloseable {
    /**
     * Server.
     */
    @NotNull
    private final Server server;
    /**
     * Server connector.
     */
    @NotNull
    private final ServerConnector http;
    /**
     * Servlet handler.
     */
    @NotNull
    private final ServletHandler handler;

    /**
     * Ctor. Creates server on random port (for testing).
     * @param path Server path.
     * @param storage Storage container.
     */
    LfsServer(final String path, final ContentManager storage) {
        this(path, storage, 0);
    }

    /**
     * Ctor.
     * @param path Server path.
     * @param storage Storage container.
     * @param port Server port
     */
    LfsServer(final String path, final ContentManager storage,
        final int port) {
        this.server = new Server();
        this.http =
            new ServerConnector(this.server, new HttpConnectionFactory());
        this.http.setPort(port);
        // @checkstyle MagicNumber (1 line)
        this.http.setIdleTimeout(30000);
        this.server.addConnector(this.http);
        this.handler = new ServletHandler();
        this.server.setHandler(this.handler);
        this.handler.addServletWithMapping(
            new ServletHolder(
                new PointerServlet(
                    storage, String.format("%s/info/lfs/storage/", path)
                )
            ),
            String.format("%s/info/lfs/objects/*", path)
        );
        this.handler.addServletWithMapping(
            new ServletHolder(new ContentServlet(storage)),
            String.format("%s/info/lfs/storage/*", path)
        );
    }

    /**
     * Start the server.
     * @return This same instance.
     * @throws Exception If something goes wrong.
     */
    public LfsServer start() throws Exception {
        this.server.start();
        return this;
    }

    /**
     * Port this server is listening to.
     * @return Port that server is listening to.
     */
    public int port() {
        return this.http.getLocalPort();
    }

    @Override
    public void close() throws Exception {
        this.server.stop();
        this.server.join();
    }

}
