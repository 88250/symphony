/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.processor;

import jodd.io.FileUtil;
import jodd.io.upload.MultipartRequestInputStream;
import jodd.util.net.MimeTypes;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.MD5;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.SymphonyServletListener;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.UUID;

/**
 * File upload to local.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 2.0.0.2, Apr 28, 2018
 * @since 1.4.0
 */
@RequestProcessor
public class FileUploadProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FileUploadProcessor.class);

    /**
     * Upload directory.
     */
    private static final String UPLOAD_DIR = Symphonys.get("upload.dir");

    /**
     * Qiniu enabled.
     */
    private static final Boolean QN_ENABLED = Symphonys.getBoolean("qiniu.enabled");

    static {
        if (!QN_ENABLED) {
            final File file = new File(UPLOAD_DIR);
            if (!FileUtil.isExistingFolder(file)) {
                try {
                    FileUtil.mkdirs(UPLOAD_DIR);
                } catch (IOException ex) {
                    LOGGER.log(Level.ERROR, "Init upload dir error", ex);

                    System.exit(-1);
                }
            }

            LOGGER.info("Uses dir [" + file.getAbsolutePath() + "] for saving files uploaded");
        }
    }

    /**
     * Gets file by the specified URL.
     *
     * @param req  the specified request
     * @param resp the specified response
     * @throws IOException io exception
     */
    @RequestProcessing(value = "/upload/*", method = HTTPRequestMethod.GET)
    public void getFile(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        if (QN_ENABLED) {
            return;
        }

        final String uri = req.getRequestURI();
        String key = StringUtils.substringAfter(uri, "/upload/");
        key = StringUtils.substringBeforeLast(key, "?"); // Erase Qiniu template
        key = StringUtils.substringBeforeLast(key, "?"); // Erase Qiniu template

        String path = UPLOAD_DIR + key;
        path = URLDecoder.decode(path, "UTF-8");

        if (!FileUtil.isExistingFile(new File(path)) ||
                !FileUtil.isExistingFolder(new File(UPLOAD_DIR)) ||
                !new File(path).getCanonicalPath().startsWith(new File(UPLOAD_DIR).getCanonicalPath())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final byte[] data = IOUtils.toByteArray(new FileInputStream(path));

        final String ifNoneMatch = req.getHeader("If-None-Match");
        final String etag = "\"" + MD5.hash(new String(data)) + "\"";

        resp.addHeader("Cache-Control", "public, max-age=31536000");
        resp.addHeader("ETag", etag);
        resp.setHeader("Server", "Latke Static Server (v" + SymphonyServletListener.VERSION + ")");
        final String ext = StringUtils.substringAfterLast(path, ".");
        final String mimeType = MimeTypes.getMimeType(ext);
        resp.addHeader("Content-Type", mimeType);

        if (etag.equals(ifNoneMatch)) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

            return;
        }

        try (final OutputStream output = resp.getOutputStream()) {
            IOUtils.write(data, output);
            output.flush();
        }
    }

    /**
     * Uploads file.
     *
     * @param req  the specified reuqest
     * @param resp the specified response
     * @throws IOException io exception
     */
    @RequestProcessing(value = "/upload", method = HTTPRequestMethod.POST)
    public void uploadFile(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        if (QN_ENABLED) {
            return;
        }

        String fileName;
        try (final MultipartRequestInputStream multipartRequestInputStream = new MultipartRequestInputStream(req.getInputStream())) {
            multipartRequestInputStream.readBoundary();
            multipartRequestInputStream.readDataHeader("UTF-8");

            fileName = multipartRequestInputStream.getLastHeader().getFileName();
            String suffix = StringUtils.substringAfterLast(fileName, ".");
            if (StringUtils.isBlank(suffix)) {
                final String mimeType = multipartRequestInputStream.getLastHeader().getContentType();
                String[] exts = MimeTypes.findExtensionsByMimeTypes(mimeType, false);

                if (null != exts && 0 < exts.length) {
                    suffix = exts[0];
                } else {
                    suffix = StringUtils.substringAfter(mimeType, "/");
                }
            }

            final String[] allowedSuffixArray = Symphonys.get("upload.suffix").split(",");
            if (!Strings.containsIgnoreCase(suffix, allowedSuffixArray)) {
                final JSONObject data = new JSONObject();
                data.put("code", 1);
                data.put("msg", "Invalid suffix [" + suffix + "], please compress this file and try again");
                data.put("key", Latkes.getServePath() + "/upload/" + fileName);
                data.put("name", fileName);
                resp.setContentType("application/json");
                try (final PrintWriter writer = resp.getWriter()) {
                    writer.append(data.toString());
                    writer.flush();
                }

                return;
            }

            final String name = StringUtils.substringBeforeLast(fileName, ".");
            final String processName = name.replaceAll("\\W", "");
            final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            fileName = uuid + '_' + processName + "." + suffix;

            try (final OutputStream output = new FileOutputStream(UPLOAD_DIR + fileName)) {
                IOUtils.copy(multipartRequestInputStream, output);
            }
        }

        final JSONObject data = new JSONObject();
        data.put("code", 0);
        data.put("key", Latkes.getServePath() + "/upload/" + fileName);
        data.put("name", fileName);
        resp.setContentType("application/json");
        try (final PrintWriter writer = resp.getWriter()) {
            writer.append(data.toString());
            writer.flush();
        }
    }
}
