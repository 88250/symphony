/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.Strings;
import org.b3log.latke.util.URLs;
import org.b3log.symphony.SymphonyServletListener;
import org.b3log.symphony.util.Escapes;
import org.b3log.symphony.util.Headers;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * File upload to local.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 2.0.1.6, Jan 31, 2019
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
    public static final String UPLOAD_DIR = Symphonys.get("upload.dir");

    /**
     * Qiniu enabled.
     */
    public static final Boolean QN_ENABLED = Symphonys.getBoolean("qiniu.enabled");

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
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Gets file by the specified URL.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/upload/{yyyy}/{MM}/{file}", method = HttpMethod.GET)
    public void getFile(final RequestContext context) {
        if (QN_ENABLED) {
            return;
        }

        final HttpServletResponse response = context.getResponse();

        final String uri = context.requestURI();
        String key = StringUtils.substringAfter(uri, "/upload/");
        key = StringUtils.substringBeforeLast(key, "?"); // Erase Qiniu template
        key = StringUtils.substringBeforeLast(key, "?"); // Erase Qiniu template

        String path = UPLOAD_DIR + key;
        path = URLs.decode(path);

        try {
            if (!FileUtil.isExistingFile(new File(path)) ||
                    !FileUtil.isExistingFolder(new File(UPLOAD_DIR)) ||
                    !new File(path).getCanonicalPath().startsWith(new File(UPLOAD_DIR).getCanonicalPath())) {
                context.sendError(HttpServletResponse.SC_NOT_FOUND);

                return;
            }

            final byte[] data = IOUtils.toByteArray(new FileInputStream(path));

            final String ifNoneMatch = context.header("If-None-Match");
            final String etag = "\"" + DigestUtils.md5Hex(new String(data)) + "\"";

            context.setHeader("Cache-Control", "public, max-age=31536000");
            context.setHeader("ETag", etag);
            context.setHeader("Server", "Sym File Server (v" + SymphonyServletListener.VERSION + ")");
            context.setHeader("Access-Control-Allow-Origin", "*");
            final String ext = StringUtils.substringAfterLast(path, ".");
            final String mimeType = MimeTypes.getMimeType(ext);
            context.addHeader("Content-Type", mimeType);

            if (etag.equals(ifNoneMatch)) {
                context.addHeader("If-None-Match", "false");
                context.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            } else {
                context.addHeader("If-None-Match", "true");
            }

            try (final OutputStream output = response.getOutputStream()) {
                IOUtils.write(data, output);
                output.flush();
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets a file failed", e);
        }
    }

    /**
     * Uploads file.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/upload", method = HttpMethod.POST)
    public void uploadFile(final RequestContext context) {
        if (QN_ENABLED) {
            return;
        }

        final HttpServletRequest request = context.getRequest();
        final int maxSize = Symphonys.getInt("upload.file.maxSize");
        final MultipartStreamParser parser = new MultipartStreamParser(new MemoryFileUploadFactory().setMaxFileSize(maxSize));
        try {
            parser.parseRequestStream(request.getInputStream(), "UTF-8");
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Parses request stream failed", e);
        }
        final FileUpload file = parser.getFiles("file")[0];
        String fileName = file.getHeader().getFileName();
        fileName = Escapes.sanitizeFilename(fileName);
        final String suffix = Headers.getSuffix(file);

        final HttpServletResponse response = context.getResponse();

        final String[] allowedSuffixArray = Symphonys.get("upload.suffix").split(",");
        if (!Strings.containsIgnoreCase(suffix, allowedSuffixArray)) {
            final JSONObject data = new JSONObject();
            data.put("code", 1);
            String msg = langPropsService.get("invalidFileSuffixLabel");
            msg = StringUtils.replace(msg, "${suffix}", suffix);
            data.put("msg", msg);
            data.put("key", Latkes.getServePath() + "/upload/" + fileName);
            data.put("name", fileName);
            response.setContentType("application/json");
            try (final PrintWriter writer = response.getWriter()) {
                writer.append(data.toString());
                writer.flush();
            } catch (final Exception e) {
                // ignored
            }

            return;
        }

        try {
            final String name = StringUtils.substringBeforeLast(fileName, ".");
            final String uuid = StringUtils.substring(UUID.randomUUID().toString().replaceAll("-", ""), 0, 8);
            fileName = name + '-' + uuid + "." + suffix;
            fileName = genFilePath(fileName);
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
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Uploads a file failed", e);
        }
    }

    /**
     * Generates upload file path for the specified file name.
     *
     * @param fileName the specified file name
     * @return "yyyy/MM/fileName"
     */
    public static String genFilePath(final String fileName) {
        final String date = DateFormatUtils.format(System.currentTimeMillis(), "yyyy/MM");

        return date + "/" + fileName;
    }
}
