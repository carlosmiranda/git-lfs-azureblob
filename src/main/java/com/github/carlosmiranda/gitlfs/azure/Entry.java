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

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Properties;
import joptsimple.OptionParser;
import joptsimple.OptionSpec;
import ru.bozaro.gitlfs.server.ContentManager;

/**
 * Entry point to launch LFS Server.
 * @author Carlos Miranda (miranda.cma+azureblob.gitlfs@gmail.com)
 * @checkstyle ClassDataAbstractionCoupling (2 lines)
 */
public final class Entry {
    /**
     * Storage.
     */
    private final ContentManager storage;
    /**
     * Git LFS context path.
     */
    private final String path;
    /**
     * Server port.
     */
    private final int port;
    /**
     * Exit criteria.
     */
    private final Exit exit;

    /**
     * Ctor.
     * @param params Parameters
     * @param exit Exit criteria
     */
    public Entry(final Params params, final Exit exit) {
        try {
            final CloudBlobContainer container = CloudStorageAccount.parse(
                new AzureStorageCredentials(
                    params.account(), params.key(), true
                ).connectionString()
            ).createCloudBlobClient().getContainerReference(params.container());
            container.createIfNotExists();
            this.storage = new AuthenticatedStorage(
                params.username(), params.password(), params.realm(),
                new AzureBlobStorage(container)
            );
        } catch (final InvalidKeyException | URISyntaxException
                | StorageException e) {
            throw new IllegalStateException(e);
        }
        this.path = params.path();
        this.port = params.port();
        this.exit = exit;
    }

    /**
     * Main entry point.
     * @param args Command line arguments.
     * @throws Exception If something goes wrong.
     */
    public static void main(final String[] args) throws Exception {
        final OptionParser parser = new OptionParser();
        final OptionSpec<String> props = parser.accepts("properties")
            .withRequiredArg();
        final String path = parser.parse(args).valueOf(props);
        final Properties properties = new Properties();
        try (final FileInputStream file = new FileInputStream(path)) {
            properties.load(file);
        }
        new Entry(new Params(properties), Exit.NEVER).start();
    }

    /**
     * Start the server.
     */
    public void start() {
        try (
            LfsServer server = new LfsServer(this.path, this.storage, this.port)
        ) {
            server.start();
            while (!this.exit.exit()) {
                // @checkstyle MagicNumber (1 line)
                Thread.sleep(10000);
            }
            // @checkstyle IllegalCatch (1 line)
        } catch (final Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Exit criteria.
     */
    interface Exit {
        /**
         * Never exit (keep running forever).
         */
        Exit NEVER = () -> {
            return false;
        };

        /**
         * Exit?
         * @return Exit
         */
        boolean exit();
    }

}
