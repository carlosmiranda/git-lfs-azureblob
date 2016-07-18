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
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import ru.bozaro.gitlfs.client.Client;
import ru.bozaro.gitlfs.client.io.StringStreamProvider;
import ru.bozaro.gitlfs.common.data.Meta;

/**
 * Integration test for {@link AzureBlobStorage}.
 *
 * <p> This integration test can use either a real Azure Storage account or
 * emulated storage.
 *
 * <p>To use Emulated Storage, you must specify your account name and key. This
 * can be done by specifying the environment variables
 * <code>storage.name</code> and <code>storage.key</code> respectively, with the
 * appropriate values.
 *
 * <p>To use Emulated Storage, you must specify the environment variable
 * <code>storage.emulated</code> with a value of <code>true</code>. Note that if
 * specified, use of emulated storage will take precedence over the real Azure
 * storage account details.
 *
 * The IT case will create and delete a container named
 * <code>gitlfs-test-[suffix]</code>, where [suffix] is a random string with 20
 * lowercase characters.
 *
 * @author Carlos Miranda (miranda.cma+azureblob.gitlfs@gmail.com)
 */
public final class AzureBlobStorageITCase {
    /**
     * Storage account.
     */
    private static final String STORAGE_ACCOUNT =
        System.getProperty("storage.account");

    /**
     * Storage key.
     */
    private static final String STORAGE_KEY = System.getProperty("storage.key");

    /**
     * Storage container name.
     */
    private static final String STORAGE_CONTAINER = String.format(
        // @checkstyle MagicNumber (2 lines)
        "%s-%s", "gitlfs-test",
        RandomStringUtils.randomAlphabetic(20).toLowerCase()
    );

    /**
     * Storage container.
     */
    private static final boolean EMULATED_STORAGE =
        Boolean.valueOf(System.getProperty("storage.emulated"));

    /**
     * Set up container.
     * @throws Exception If something goes wrong
     */
    @BeforeClass
    public static void createContainer() throws Exception {
        Assume.assumeTrue(AzureBlobStorageITCase.testShouldExecute());
        final CloudBlobContainer container = AzureBlobStorageITCase.container();
        container.createIfNotExists();
    }

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
            new AzureBlobStorage(AzureBlobStorageITCase.container());
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
            new AzureBlobStorage(AzureBlobStorageITCase.container());
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
            new AzureBlobStorage(AzureBlobStorageITCase.container());
        MatcherAssert.assertThat(
            storage.getMetadata(meta.getOid()), Matchers.nullValue()
        );
    }

    /**
     * Remove container afterwards.
     * @throws Exception If something goes wrong.
     */
    @AfterClass
    public static void removeContainer() throws Exception {
        if (AzureBlobStorageITCase.testShouldExecute()) {
            AzureBlobStorageITCase.container().deleteIfExists();
        }
    }

    /**
     * Get Azure Blob Container.
     * @return Azure Blob Container.
     * @throws Exception If something goes wrong
     */
    private static CloudBlobContainer container() throws Exception {
        final CloudStorageAccount account = AzureBlobStorageITCase.account();
        return account.createCloudBlobClient().
            getContainerReference(AzureBlobStorageITCase.STORAGE_CONTAINER);
    }

    /**
     * Get cloud storage account.
     * @return Cloud storage account.
     * @throws Exception if something goes wrong
     */
    private static CloudStorageAccount account() throws Exception {
        final CloudStorageAccount account;
        if (AzureBlobStorageITCase.EMULATED_STORAGE) {
            account = CloudStorageAccount.getDevelopmentStorageAccount();
        } else {
            account = CloudStorageAccount.parse(
                new AzureStorageCredentials(
                    AzureBlobStorageITCase.STORAGE_ACCOUNT,
                    AzureBlobStorageITCase.STORAGE_KEY,
                    false
                ).connectionString()
            );
        }
        return account;
    }

    /**
     * Should this test execute?
     * @return true, if proper environment variables are specified
     */
    private static boolean testShouldExecute() {
        return AzureBlobStorageITCase.EMULATED_STORAGE
            || AzureBlobStorageITCase.STORAGE_ACCOUNT != null
            && AzureBlobStorageITCase.STORAGE_KEY != null;
    }

}
