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

import java.io.IOException;
import java.nio.charset.Charset;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import org.eclipse.jetty.http.HttpHeader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.server.ContentManager;
import ru.bozaro.gitlfs.server.ForbiddenError;
import ru.bozaro.gitlfs.server.UnauthorizedError;

/**
 * Decorating ContentManager providing HTTP Basic authentication functionality.
 * @author Carlos Miranda (miranda.cma+azureblob.gitlfs@gmail.com)
 */
final class AuthenticatedStorage implements ContentManager {

    /**
     * Backing Storage.
     */
    private final ContentManager storage;

    /**
     * Basic auth string to match.
     */
    private final String auth;

    /**
     * Basic authentication realm.
     */
    private final String realm;

    /**
     * Ctor.
     * @param user Username.
     * @param pass Password.
     * @param realm Authentication realm.
     * @param storage Backing storage.
     * @checkstyle ParameterNumber (4 lines)
     */
    AuthenticatedStorage(final String user, final String pass,
        final String realm, final ContentManager storage) {
        this.storage = storage;
        this.realm = String.format("Basic realm=\"%s\"", realm);
        this.auth = String.format(
            "Basic %s",
            DatatypeConverter.printBase64Binary(
                String.format("%s:%s", user, pass)
                    .getBytes(Charset.defaultCharset())
            )
        );
    }

    @Override
    @NotNull
    public Downloader checkDownloadAccess(
        @NotNull final HttpServletRequest request)
        throws IOException, ForbiddenError, UnauthorizedError {
        this.checkAuthorization(request);
        return this.storage.checkDownloadAccess(request);
    }

    @Override
    @NotNull
    public Uploader checkUploadAccess(@NotNull final HttpServletRequest request)
        throws IOException, ForbiddenError, UnauthorizedError {
        this.checkAuthorization(request);
        return this.storage.checkUploadAccess(request);
    }

    @Override
    @Nullable
    public Meta getMetadata(@NotNull final String hash)
        throws IOException {
        return this.storage.getMetadata(hash);
    }

    /**
     * Check authorization.
     * @param request HTTP Servlet Request.
     * @throws UnauthorizedError If authorization header does not match.
     */
    private void checkAuthorization(final HttpServletRequest request)
        throws UnauthorizedError {
        if (!this.auth.equalsIgnoreCase(
            request.getHeader(HttpHeader.AUTHORIZATION.asString())
        )) {
            throw new UnauthorizedError(this.realm);
        }
    }

}
