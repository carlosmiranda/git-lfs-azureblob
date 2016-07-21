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
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import ru.bozaro.gitlfs.client.Client;
import ru.bozaro.gitlfs.client.io.StringStreamProvider;
import ru.bozaro.gitlfs.common.data.BatchReq;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;

/**
 * Integration test case for {@link LfsServer}.
 *
 * @author Carlos Miranda (miranda.cma+azureblob.gitlfs@gmail.com)
 */
public final class LfsServerITCase {

    /**
     * Container resource.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @ClassRule
    public static AzureStorageContainer container = new AzureStorageContainer();

    /**
     * LfsServer can upload and download files.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void uploadsAndDownloadsFiles() throws Exception {
        final AzureBlobStorage storage =
            new AzureBlobStorage(LfsServerITCase.container.container());
        final String path = "/foo/bar.git";
        try (LfsServer server = new LfsServer(path, storage).start()) {
            final Client client =
                new Client(new SimpleAuthProvider(path, server.port()));
            final StringStreamProvider streamProvider =
                new StringStreamProvider("Test LFS server upload.");
            final Meta meta = Client.generateMeta(streamProvider);
            client.postBatch(
                new BatchReq(
                    Operation.Download, Collections.singletonList(meta)
                )
            );
            // Can upload.
            Assert.assertTrue(client.putObject(streamProvider, meta));
            // Can download uploaded file.
            final byte[] content =
                client.getObject(meta.getOid(), ByteStreams::toByteArray);
            Assert.assertTrue(
                Arrays.equals(
                    content, ByteStreams.toByteArray(streamProvider.getStream())
                )
            );
            // Should return false since we already uploaded it.
            Assert.assertFalse(client.putObject(streamProvider, meta));
        }
    }

    /**
     * LfsServer can throw an exception if the file does not exist.
     * @throws Exception If something goes wrong.
     */
    @Test(expected = FileNotFoundException.class)
    public void throwsExceptionIfFileDoesNotExist() throws Exception {
        final AzureBlobStorage storage =
            new AzureBlobStorage(LfsServerITCase.container.container());
        final String path = "/foo/baz.git";
        try (LfsServer server = new LfsServer(path, storage).start()) {
            final StringStreamProvider streamProvider =
                new StringStreamProvider("Test nonexistent file download.");
            final Meta meta = Client.generateMeta(streamProvider);
            new Client(new SimpleAuthProvider(path, server.port()))
                .getObject(meta.getOid(), ByteStreams::toByteArray);
        }
    }

}
