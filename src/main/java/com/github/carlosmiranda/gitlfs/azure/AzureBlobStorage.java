/**
 *
 */
package com.github.carlosmiranda.gitlfs.azure;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.server.ContentManager;
import ru.bozaro.gitlfs.server.ForbiddenError;
import ru.bozaro.gitlfs.server.UnauthorizedError;

/**
 * @author cmiranda
 */
public class AzureBlobStorage implements ContentManager {

    @Override
    public Meta getMetadata(final String hash) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Downloader checkDownloadAccess(final HttpServletRequest request)
        throws IOException, ForbiddenError, UnauthorizedError {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uploader checkUploadAccess(final HttpServletRequest request)
        throws IOException, ForbiddenError, UnauthorizedError {
        // TODO Auto-generated method stub
        return null;
    }

}
