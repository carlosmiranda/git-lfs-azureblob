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

import com.google.common.collect.ImmutableMap;
import java.util.Properties;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Params}.
 * @author Carlos Miranda (miranda.cma+azureblob.gitlfs@gmail.com)
 */
public final class ParamsTest {

    /**
     * Params can retrieve stored parameters.
     * @throws Exception If something goes wrong
     */
    @Test
    public void retrievesParams() throws Exception {
        final Properties props = new Properties();
        props.putAll(
            ImmutableMap.<String, String>builder()
                .put("gitlfs.username", "lfsuser")
                .put("gitlfs.password", "lfspass")
                .put("gitlfs.realm", "realm")
                .put("gitlfs.path", "/test/repo.git")
                .put("gitlfs.port", "9090")
                .put("azure.account", "testaccount")
                .put("azure.key", "abcde")
                .put("azure.container", "test-container")
                .build()
        );
        final Params params = new Params(props);
        MatcherAssert.assertThat(
            params.username(), Matchers.is(props.getProperty("gitlfs.username"))
        );
        MatcherAssert.assertThat(
            params.password(), Matchers.is(props.getProperty("gitlfs.password"))
        );
        MatcherAssert.assertThat(
            params.realm(), Matchers.is(props.getProperty("gitlfs.realm"))
        );
        MatcherAssert.assertThat(
            params.path(), Matchers.is(props.getProperty("gitlfs.path"))
        );
        MatcherAssert.assertThat(
            params.port(),
            Matchers.is(Integer.parseInt(props.getProperty("gitlfs.port")))
        );
        MatcherAssert.assertThat(
            params.account(), Matchers.is(props.getProperty("azure.account"))
        );
        MatcherAssert.assertThat(
            params.key(), Matchers.is(props.getProperty("azure.key"))
        );
        MatcherAssert.assertThat(
            params.container(),
            Matchers.is(props.getProperty("azure.container"))
        );
    }

}

