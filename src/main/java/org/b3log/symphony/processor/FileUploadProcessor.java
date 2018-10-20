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
import jodd.io.upload.FileUpload;
import jodd.io.upload.MultipartStreamParser;
import jodd.io.upload.impl.MemoryFileUploadFactory;
import jodd.net.MimeTypes;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.SymphonyServletListener;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * File upload to local.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 2.0.0.3, Oct 20, 2018
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
                    LOGGER.log(Level.ERROR, "Init upload dir failed", ex);

                    System.exit(-1);
                }
            }

            LOGGER.info("Uses dir [" + file.getAbsolutePath() + "] for file uploading");
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
        final String etag = "\"" + DigestUtils.md5Hex(new String(data)) + "\"";

        resp.setHeader("Cache-Control", "public, max-age=31536000");
        resp.setHeader("ETag", etag);
        resp.setHeader("Server", "Sym File Server (v" + SymphonyServletListener.VERSION + ")");
        resp.setHeader("Access-Control-Allow-Origin", "*");
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
     * @param request  the specified request
     * @param response the specified response
     * @throws IOException io exception
     */
    @RequestProcessing(value = "/upload", method = HTTPRequestMethod.POST)
    public void uploadFile(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (QN_ENABLED) {
            return;
        }

        final int maxSize = Symphonys.getInt("upload.file.maxSize");
        final MultipartStreamParser parser = new MultipartStreamParser(new MemoryFileUploadFactory().setMaxFileSize(maxSize));
        parser.parseRequestStream(request.getInputStream(), "UTF-8");
        final FileUpload file = parser.getFiles("file")[0];
        String fileName = file.getHeader().getFileName();
        final String suffix = getSuffix(file);

        final String[] allowedSuffixArray = Symphonys.get("upload.suffix").split(",");
        if (!Strings.containsIgnoreCase(suffix, allowedSuffixArray)) {
            final JSONObject data = new JSONObject();
            data.put("code", 1);
            data.put("msg", "Invalid suffix [" + suffix + "], please compress this file and try again");
            data.put("key", Latkes.getServePath() + "/upload/" + fileName);
            data.put("name", fileName);
            response.setContentType("application/json");
            try (final PrintWriter writer = response.getWriter()) {
                writer.append(data.toString());
                writer.flush();
            }

            return;
        }

        final String name = StringUtils.substringBeforeLast(fileName, ".");
        final String processName = name.replaceAll("\\W", "");
        final String uuid = StringUtils.substring(UUID.randomUUID().toString().replaceAll("-", ""), 0, 8);
        fileName = processName + '-' + uuid + "." + suffix;
        final String date = DateFormatUtils.format(System.currentTimeMillis(), "yyyy/MM");
        fileName = date + "/" + fileName;
        final Path path = Paths.get(UPLOAD_DIR, fileName);
        path.getParent().toFile().mkdirs();
        try (final OutputStream output = new FileOutputStream(path.toFile());
             final InputStream input = file.getFileInputStream()) {
            IOUtils.copy(input, output);
        }

        final JSONObject data = new JSONObject();
        data.put("code", 0);
        data.put("key", Latkes.getServePath() + "/upload/" + fileName);
        data.put("name", fileName);
        response.setContentType("application/json");
        try (final PrintWriter writer = response.getWriter()) {
            writer.append(data.toString());
            writer.flush();
        }
    }

    private static String getSuffix(final FileUpload file) {
        final String fileName = file.getHeader().getFileName();
        String ret = StringUtils.substringAfterLast(fileName, ".");
        if (StringUtils.isNotBlank(ret)) {
            return ret;
        }

        final String contentType = file.getHeader().getContentType();
        final String[] exts = MimeTypes.findExtensionsByMimeTypes(contentType, false);
        if (null != exts && 0 < exts.length) {
            ret = exts[0];
        } else {
            ret = StringUtils.substringAfter(contentType, "/");
        }

        return ret;
    }
}
