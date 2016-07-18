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

/**
 * Credentials for Azure Blob Storage.
 *
 * @author Carlos Miranda (miranda.cma+azureblob.gitlfs@gmail.com)
 */
final class AzureStorageCredentials {
    /**
     * Account name.
     */
    private final String account;
    /**
     * Account key.
     */
    private final String key;

    /**
     * Should we use HTTPS protocol?
     */
    private final boolean https;

    /**
     * Ctor.
     * @param acct Storage account name
     * @param key Storage account key
     * @param https Should we use https protocol?
     */
    AzureStorageCredentials(final String acct, final String key,
        final boolean https) {
        this.account = acct;
        this.key = key;
        this.https = https;
    }

    /**
     * Connection string for this account.
     * @return Azure Blob connection string.
     */
    public String connectionString() {
        final String protocol;
        if (this.https) {
            protocol = "DefaultEndpointsProtocol=https";
        } else {
            protocol = "DefaultEndpointsProtocol=http";
        }
        return Joiner.on(';').join(
            protocol,
            String.format("AccountName=%s", this.account),
            String.format("AccountKey=%s", this.key)
        );
    }

}
