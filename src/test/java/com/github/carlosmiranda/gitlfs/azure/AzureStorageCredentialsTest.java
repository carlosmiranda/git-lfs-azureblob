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

import com.google.common.base.Joiner;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link AzureStorageCredentials}.
 *
 * @author Carlos Miranda (miranda.cma+azureblob.gitlfs@gmail.com)
 */
public final class AzureStorageCredentialsTest {

    /**
     * AzureStorageCredentials can return credentials with http endpoint.
     */
    @Test
    public void returnsHttpCredentials() {
        final String acct = "foo";
        final String key = "bar";
        MatcherAssert.assertThat(
            new AzureStorageCredentials(acct, key, false).connectionString(),
            Matchers.is(
                Joiner.on(';').join(
                    "DefaultEndpointsProtocol=http",
                    String.format("AccountName=%s", acct),
                    String.format("AccountKey=%s", key)
                )
            )
        );
    }

    /**
     * AzureStorageCredentials can return credentials with https endpoint.
     */
    @Test
    public void returnsHttpsCredentials() {
        final String acct = "fou";
        final String key = "bas";
        MatcherAssert.assertThat(
            new AzureStorageCredentials(acct, key, true).connectionString(),
            Matchers.is(
                Joiner.on(';').join(
                    "DefaultEndpointsProtocol=https",
                    String.format("AccountName=%s", acct),
                    String.format("AccountKey=%s", key)
                )
            )
        );
    }

}
