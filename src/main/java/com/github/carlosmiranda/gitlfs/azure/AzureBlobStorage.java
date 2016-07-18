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

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.server.ContentManager;
import ru.bozaro.gitlfs.server.ForbiddenError;
import ru.bozaro.gitlfs.server.UnauthorizedError;

/**
 * Git LFS Storage backed by Azure Blob Storage.
 *
 * @author Carlos Miranda (miranda.cma+azureblob.gitlfs@gmail.com)
 */
public final class AzureBlobStorage implements ContentManager {
    /**
     * Cloud blob client.
     */
    private final CloudBlobContainer container;

    /**
     * Downloader of files from Azure Storage.
     */
    private final Downloader downloader;

    /**
     * Get instance using with the specified container.
     * @param container CloudBlob Container
     */
    AzureBlobStorage(final CloudBlobContainer container) {
        this.container = container;
        this.downloader = new AzureDownloader(container);
    }

    @Override
    public Meta getMetadata(final String hash) throws IOException {
        try {
            final CloudBlockBlob blob =
                this.container.getBlockBlobReference(hash);
            final boolean exists = blob.exists();
            final Meta meta;
            if (exists){
                meta = new Meta(hash, blob.getProperties().getLength());
            } else {
                meta = null;
            }
            return meta;
        } catch (final StorageException | URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Downloader checkDownloadAccess(final HttpServletRequest request)
        throws IOException, ForbiddenError, UnauthorizedError {
        return this.downloader;
    }

    @Override
    public Uploader checkUploadAccess(final HttpServletRequest request)
        throws IOException, ForbiddenError, UnauthorizedError {
        return this::upload;
    }

    /**
     * Upload blob.
     * @param meta Metadata
     * @param content Contents as stream
     * @throws IOException If an IO Exception occurs.
     */
    private void upload(@NotNull final Meta meta,
        @NotNull final InputStream content) throws IOException {
        try {
            this.container.getBlockBlobReference(meta.getOid())
                .upload(content, meta.getSize());
        } catch (final StorageException | URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Azure implementation of Downloader.
     */
    private static final class AzureDownloader implements Downloader {
        /**
         * Cloud blob client.
         */
        private final CloudBlobContainer container;

        /**
         * Get instance using with the specified container.
         * @param container CloudBlob Container
         */
        AzureDownloader(final CloudBlobContainer container) {
            this.container = container;
        }

        @Override
        @NotNull
        public InputStream openObject(@NotNull final String hash)
            throws IOException {
            try {
                return this.container
                    .getBlockBlobReference(hash).openInputStream();
            } catch (StorageException | URISyntaxException ex) {
                throw new IOException(ex);
            }
        }
        @Override
        @Nullable
        public InputStream openObjectGzipped(@NotNull final String hash)
                throws IOException {
            return null;
        }
    }

}
