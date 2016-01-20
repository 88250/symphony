/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jodd.io.FileUtil;
import jodd.upload.MultipartRequestInputStream;
import jodd.util.MimeTypes;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Logger;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * File upload.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jan 20, 2016
 * @since 1.4.0
 */
@WebServlet(urlPatterns = {"/upload", "/upload/*"}, loadOnStartup = 2)
public class FileUploadServlet extends HttpServlet {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FileUploadServlet.class);

    /**
     * Upload directory.
     */
    private static final String UPLOAD_DIR = Symphonys.get("upload.dir");

    /**
     * Qiniu enabled.
     */
    private static final Boolean QN_ENABLED = Symphonys.getBoolean("qiniu.enabled");

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        if (QN_ENABLED) {
            return;
        }

        final String uri = req.getRequestURI();
        final String key = uri.substring("/upload/".length());

        String path = UPLOAD_DIR + key;
        path = StringUtils.substringBeforeLast(path, "-64.jpg"); // Erase Qiniu template
        path = StringUtils.substringBeforeLast(path, "-260.jpg"); // Erase Qiniu template

        if (!FileUtil.isExistingFile(new File(path))) {
            return;
        }

        final OutputStream output = resp.getOutputStream();
        final byte[] data = IOUtils.toByteArray(new FileInputStream(path));

        IOUtils.write(data, output);
        output.flush();

        IOUtils.closeQuietly(output);
    }

    @Override
    public void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        if (QN_ENABLED) {
            return;
        }

        final MultipartRequestInputStream multipartRequestInputStream = new MultipartRequestInputStream(req.getInputStream());
        multipartRequestInputStream.readBoundary();
        multipartRequestInputStream.readDataHeader("UTF-8");

        final String mimeType = multipartRequestInputStream.getLastHeader().getContentType();

        String suffix;
        String[] exts = MimeTypes.findExtensionsByMimeTypes(mimeType, false);
        if (null == exts || 0 == exts.length) {
            suffix = StringUtils.substringAfterLast(multipartRequestInputStream.getLastHeader().getFileName(), ".");
        } else {
            suffix = exts[0];
        }

        final String fileName = UUID.randomUUID().toString() + "." + suffix;

        final OutputStream output = new FileOutputStream(UPLOAD_DIR + fileName);
        IOUtils.copy(multipartRequestInputStream, output);

        IOUtils.closeQuietly(multipartRequestInputStream);
        IOUtils.closeQuietly(output);

        final JSONObject data = new JSONObject();
        data.put("key", Latkes.getServePath() + "/upload/" + fileName);

        resp.setContentType("application/json");

        final PrintWriter writer = resp.getWriter();
        writer.append(data.toString());
        writer.flush();
        writer.close();
    }
}
