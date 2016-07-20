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

import com.google.common.io.ByteStreams;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import ru.bozaro.gitlfs.client.Client;
import ru.bozaro.gitlfs.client.io.StringStreamProvider;
import ru.bozaro.gitlfs.common.data.Meta;

/**
 * Integration test for {@link AzureBlobStorage}.
 *
 * @author Carlos Miranda (miranda.cma+azureblob.gitlfs@gmail.com)
 */
public final class AzureBlobStorageITCase {

    /**
     * Container resource.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @ClassRule
    public static AzureStorageContainer container = new AzureStorageContainer();

    /**
     * AzureBlobStorage can upload and download data.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void uploadsAndDownloadsData() throws Exception {
        final StringStreamProvider provider =
            new StringStreamProvider("Hello, world");
        final Meta meta = Client.generateMeta(provider);
        final AzureBlobStorage storage =
            new AzureBlobStorage(AzureBlobStorageITCase.container.container());
        try (final InputStream stream = provider.getStream()) {
            storage.checkUploadAccess(Mockito.mock(HttpServletRequest.class))
                .saveObject(meta, stream);
        }
        final byte[] downloaded = ByteStreams.toByteArray(
            storage.checkDownloadAccess(Mockito.mock(HttpServletRequest.class))
                .openObject(meta.getOid())
        );
        MatcherAssert.assertThat(
            downloaded,
            Matchers.is(ByteStreams.toByteArray(provider.getStream()))
        );
    }

    /**
     * AzureBlobStorage can get object metadata.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void getsObjectMetadata() throws Exception {
        final StringStreamProvider provider =
            new StringStreamProvider("Test object metadata");
        final Meta meta = Client.generateMeta(provider);
        final AzureBlobStorage storage =
            new AzureBlobStorage(AzureBlobStorageITCase.container.container());
        try (final InputStream stream = provider.getStream()) {
            storage.checkUploadAccess(Mockito.mock(HttpServletRequest.class))
                .saveObject(meta, stream);
        }
        final Meta result = storage.getMetadata(meta.getOid());
        MatcherAssert.assertThat(
            result.getOid(), Matchers.is(meta.getOid())
        );
        MatcherAssert.assertThat(
            result.getSize(), Matchers.is(meta.getSize())
        );
    }

    /**
     * AzureBlobStorage can return null metadata for nonexistent object.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void returnsNoMetadataForNonexistentObject() throws Exception {
        final StringStreamProvider provider =
            new StringStreamProvider("Nonexistent");
        final Meta meta = Client.generateMeta(provider);
        final AzureBlobStorage storage =
            new AzureBlobStorage(AzureBlobStorageITCase.container.container());
        MatcherAssert.assertThat(
            storage.getMetadata(meta.getOid()), Matchers.nullValue()
        );
    }

}
