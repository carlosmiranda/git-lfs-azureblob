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

import java.nio.charset.Charset;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.Test;
import org.mockito.Mockito;
import ru.bozaro.gitlfs.server.ContentManager;
import ru.bozaro.gitlfs.server.UnauthorizedError;

/**
 * Test case for {@link AuthenticatedStorage}.
 *
 * @author Carlos Miranda (miranda.cma+azureblob.gitlfs@gmail.com)
 */
public final class AuthenticatedStorageTest {

    /**
     * AuthenticatedStorage can delegate authorized download to underlying
     * storage.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void delegatesDownloadToUnderlyingStorage() throws Exception {
        final ContentManager storage = Mockito.mock(ContentManager.class);
        final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        final String user = "downuser";
        final String pass = "downpass";
        Mockito.when(req.getHeader(HttpHeader.AUTHORIZATION.toString()))
            .thenReturn(AuthenticatedStorageTest.authorization(user, pass));
        new AuthenticatedStorage(user, pass, "down", storage)
            .checkDownloadAccess(req);
        Mockito.verify(storage).checkDownloadAccess(req);
    }

    /**
     * AuthenticatedStorage can delegate authorized upload to underlying
     * storage.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void delegatesUploadToUnderlyingStorage() throws Exception {
        final ContentManager storage = Mockito.mock(ContentManager.class);
        final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        final String user = "upuser";
        final String pass = "uppass";
        Mockito.when(req.getHeader(HttpHeader.AUTHORIZATION.toString()))
            .thenReturn(AuthenticatedStorageTest.authorization(user, pass));
        new AuthenticatedStorage(user, pass, "up", storage)
            .checkUploadAccess(req);
        Mockito.verify(storage).checkUploadAccess(req);
    }

    /**
     * AuthenticatedStorage can throw UnauthorizedError for non-authenticated
     * downloads.
     * @throws Exception If something goes wrong.
     */
    @Test(expected = UnauthorizedError.class)
    public void throwsUnauthorizedErrorForDownloadWithNoAuth()
        throws Exception {
        new AuthenticatedStorage(
            "blah", "bleh", "fail1", Mockito.mock(ContentManager.class)
        ).checkDownloadAccess(Mockito.mock(HttpServletRequest.class));
    }

    /**
     * AuthenticatedStorage can throw UnauthorizedError for non-authenticated
     * uploads.
     * @throws Exception If something goes wrong.
     */
    @Test(expected = UnauthorizedError.class)
    public void throwsUnauthorizedErrorForUploadWithNoAuth() throws Exception {
        new AuthenticatedStorage(
            "blah2", "bleh2", "fail2", Mockito.mock(ContentManager.class)
        ).checkUploadAccess(Mockito.mock(HttpServletRequest.class));
    }

    /**
     * AuthenticatedStorage can delegate checkMetadata to underlying storage.
     * @throws Exception If something goes wrong
     */
    @Test
    public void delegatesCheckMetadataToUnderlyingStorage() throws Exception {
        final ContentManager storage = Mockito.mock(ContentManager.class);
        final String hash = "abcde";
        new AuthenticatedStorage("blah3", "bleh3", "meta", storage)
            .getMetadata(hash);
        Mockito.verify(storage).getMetadata(hash);
    }

    /**
     * Get authorization header value.
     * @param user Username
     * @param pass Password
     * @return Authorization header value.
     */
    private static String authorization(final String user, final String pass) {
        return String.format(
            "Basic %s",
            DatatypeConverter.printBase64Binary(
                String.format("%s:%s", user, pass)
                    .getBytes(Charset.defaultCharset())
            )
        );
    }

}
