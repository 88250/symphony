/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import jodd.util.URLDecoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.util.MD5;
import org.b3log.symphony.SymphonyServletListener;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * File upload to local.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.1.4.3, Dec 19, 2016
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

    static {
        if (!FileUtil.isExistingFolder(new File(UPLOAD_DIR))) {
            try {
                FileUtil.mkdirs(UPLOAD_DIR);
            } catch (IOException ex) {
                LOGGER.log(Level.ERROR, "Init upload dir error", ex);
            }
        }
    }

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
        String key = StringUtils.substringAfter(uri, "/upload/");
        key = StringUtils.substringBeforeLast(key, "?"); // Erase Qiniu template
        key = StringUtils.substringBeforeLast(key, "?"); // Erase Qiniu template

        String path = UPLOAD_DIR + key;
        path = URLDecoder.decode(path, "UTF-8");

        if (!FileUtil.isExistingFile(new File(path))) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final byte[] data = IOUtils.toByteArray(new FileInputStream(path));

        final String ifNoneMatch = req.getHeader("If-None-Match");
        final String etag = "\"" + MD5.hash(new String(data)) + "\"";

        resp.addHeader("Cache-Control", "public, max-age=31536000");
        resp.addHeader("ETag", etag);
        resp.setHeader("Server", "Latke Static Server (v" + SymphonyServletListener.VERSION + ")");

        if (etag.equals(ifNoneMatch)) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

            return;
        }

        final OutputStream output = resp.getOutputStream();
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

        String fileName = multipartRequestInputStream.getLastHeader().getFileName();

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

        final String name = StringUtils.substringBeforeLast(fileName, ".");
        final String processName = name.replaceAll("\\W", "");
        final String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        if (StringUtils.isBlank(processName)) {
            fileName = uuid + "." + suffix;
        } else {
            fileName = uuid + '_' + processName + "." + suffix;
        }

        final OutputStream output = new FileOutputStream(UPLOAD_DIR + fileName);
        IOUtils.copy(multipartRequestInputStream, output);

        IOUtils.closeQuietly(multipartRequestInputStream);
        IOUtils.closeQuietly(output);

        final JSONObject data = new JSONObject();
        data.put("key", Latkes.getServePath() + "/upload/" + fileName);
        data.put("name", fileName);

        resp.setContentType("application/json");

        final PrintWriter writer = resp.getWriter();
        writer.append(data.toString());
        writer.flush();
        writer.close();
    }
}
