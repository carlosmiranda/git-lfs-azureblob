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
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assume;
import org.junit.rules.ExternalResource;

/**
 * Azure Storage container resource.
 *
 * <p> This resource can use either a real Azure Storage account or emulated
 * storage.
 *
 * <p>To use a real storage account, you must specify your account name and key.
 * This can be done by specifying the environment variables
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
 * @author Carlos Miranda (miranda.cma+azureblob.gitlfs@gmail.com)
 */
public final class AzureStorageContainer extends ExternalResource {
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
     * Storage container.
     */
    private static final boolean EMULATED_STORAGE =
        Boolean.valueOf(System.getProperty("storage.emulated"));

    /**
     * Name of storage container to test.
     */
    private final String cont;

    /**
     * Ctor.
     */
    public AzureStorageContainer() {
        this.cont = String.format(
            // @checkstyle MagicNumber (2 lines)
            "%s-%s", "gitlfs-test",
            RandomStringUtils.randomAlphabetic(20).toLowerCase()
        );
    }

    @Override
    protected void before() throws Throwable {
        Assume.assumeTrue(AzureStorageContainer.testShouldExecute());
        final CloudBlobContainer container = this.container();
        container.createIfNotExists();
    }

    @Override
    protected void after() {
        if (AzureStorageContainer.testShouldExecute()) {
            try {
                this.container().deleteIfExists();
            // @checkstyle IllegalCatch (1 line)
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Should this test execute?
     * @return true, if proper environment variables are specified
     */
    private static boolean testShouldExecute() {
        return AzureStorageContainer.EMULATED_STORAGE
            || AzureStorageContainer.STORAGE_ACCOUNT != null
            && AzureStorageContainer.STORAGE_KEY != null;
    }

    /**
     * Get Azure Blob Container.
     * @return Azure Blob Container.
     * @throws Exception If something goes wrong
     */
    public CloudBlobContainer container() throws Exception {
        final CloudStorageAccount account = AzureStorageContainer.account();
        return account.createCloudBlobClient().
            getContainerReference(this.cont);
    }

    /**
     * Get cloud storage account.
     * @return Cloud storage account.
     * @throws Exception if something goes wrong
     */
    private static CloudStorageAccount account() throws Exception {
        final CloudStorageAccount account;
        if (AzureStorageContainer.EMULATED_STORAGE) {
            account = CloudStorageAccount.getDevelopmentStorageAccount();
        } else {
            account = CloudStorageAccount.parse(
                new AzureStorageCredentials(
                    AzureStorageContainer.STORAGE_ACCOUNT,
                    AzureStorageContainer.STORAGE_KEY,
                    false
                ).connectionString()
            );
        }
        return account;
    }

}
