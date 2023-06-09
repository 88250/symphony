/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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

import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.io.FileUtil;
import jodd.net.MimeTypes;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.http.*;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.util.Strings;
import org.b3log.latke.util.URLs;
import org.b3log.symphony.Server;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.util.*;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.b3log.symphony.util.Symphonys.QN_ENABLED;

/**
 * File upload to local.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 3.0.1.0, Jun 9, 2023
 * @since 1.4.0
 */
@Singleton
public class FileUploadProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(FileUploadProcessor.class);

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Register request handlers.
     */
    public static void register() {
        final BeanManager beanManager = BeanManager.getInstance();

        final FileUploadProcessor fileUploadProcessor = beanManager.getReference(FileUploadProcessor.class);
        Dispatcher.get("/upload/{yyyy}/{MM}/{file}", fileUploadProcessor::getFile);
        Dispatcher.post("/upload", fileUploadProcessor::uploadFile);
        Dispatcher.post("/fetch-upload", fileUploadProcessor::fetchUpload);
    }

    /**
     * Gets file by the specified URL.
     *
     * @param context the specified context
     */
    public void getFile(final RequestContext context) {
        if (QN_ENABLED) {
            return;
        }

        final Response response = context.getResponse();

        final String uri = context.requestURI();
        String key = StringUtils.substringAfter(uri, "/upload/");
        key = StringUtils.substringBeforeLast(key, "?"); // Erase Qiniu template
        key = StringUtils.substringBeforeLast(key, "?"); // Erase Qiniu template

        String path = Symphonys.UPLOAD_LOCAL_DIR + key;
        path = URLs.decode(path);

        try {
            if (!FileUtil.isExistingFile(new File(path)) ||
                    !FileUtil.isExistingFolder(new File(Symphonys.UPLOAD_LOCAL_DIR)) ||
                    !Path.of(path).normalize().startsWith(Path.of(Symphonys.UPLOAD_LOCAL_DIR).normalize())) {
                context.sendError(404);
                return;
            }

            final byte[] data = IOUtils.toByteArray(new FileInputStream(path));

            final String ifNoneMatch = context.header("If-None-Match");
            final String etag = "\"" + DigestUtils.md5Hex(new String(data)) + "\"";

            context.setHeader("Cache-Control", "public, max-age=31536000");
            context.setHeader("ETag", etag);
            context.setHeader("Server", "Sym File Server (v" + Server.VERSION + ")");
            context.setHeader("Access-Control-Allow-Origin", "*");
            final String ext = StringUtils.substringAfterLast(path, ".");
            final String mimeType = MimeTypes.getMimeType(ext);
            context.addHeader("Content-Type", mimeType);

            if (etag.equals(ifNoneMatch)) {
                context.addHeader("If-None-Match", "false");
                context.setStatus(304);
            } else {
                context.addHeader("If-None-Match", "true");
            }

            response.sendBytes(data);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets a file failed", e);
        }
    }

    /**
     * Uploads file.
     *
     * @param context the specified context
     */
    public void uploadFile(final RequestContext context) {
        final JSONObject result = Results.newFail();
        context.renderJSONPretty(result);

        final Request request = context.getRequest();
        final int maxSize = (int) Symphonys.UPLOAD_FILE_MAX;

        final Map<String, String> succMap = new HashMap<>();
        final List<FileUpload> allFiles = request.getFileUploads("file[]");
        final List<FileUpload> files = new ArrayList<>();
        String fileName;

        Auth auth;
        UploadManager uploadManager = null;
        String uploadToken = null;
        if (QN_ENABLED) {
            auth = Auth.create(Symphonys.UPLOAD_QINIU_AK, Symphonys.UPLOAD_QINIU_SK);
            uploadToken = auth.uploadToken(Symphonys.UPLOAD_QINIU_BUCKET);
            uploadManager = new UploadManager(new Configuration());
        }

        final JSONObject data = new JSONObject();
        final List<String> errFiles = new ArrayList<>();

        boolean checkFailed = false;
        String suffix = "";
        final String[] allowedSuffixArray = Symphonys.UPLOAD_SUFFIX.split(",");
        for (final FileUpload file : allFiles) {
            suffix = Headers.getSuffix(file);
            if (!Strings.containsIgnoreCase(suffix, allowedSuffixArray)) {
                checkFailed = true;
                break;
            }

            if (maxSize < file.getData().length) {
                continue;
            }

            files.add(file);
        }

        if (checkFailed) {
            for (final FileUpload file : allFiles) {
                fileName = file.getFilename();
                errFiles.add(fileName);
            }

            data.put("errFiles", errFiles);
            data.put("succMap", succMap);
            result.put(Common.DATA, data);
            result.put(Keys.CODE, 1);
            String msg = langPropsService.get("invalidFileSuffixLabel");
            msg = StringUtils.replace(msg, "${suffix}", suffix);
            result.put(Keys.MSG, msg);
            return;
        }

        final List<byte[]> fileBytes = new ArrayList<>();
        if (Symphonys.QN_ENABLED) { // 文件上传性能优化 https://github.com/b3log/symphony/issues/866
            for (final FileUpload file : files) {
                final byte[] bytes = file.getData();
                fileBytes.add(bytes);
            }
        }

        final CountDownLatch countDownLatch = new CountDownLatch(files.size());
        for (int i = 0; i < files.size(); i++) {
            final FileUpload file = files.get(i);
            final String originalName = fileName = Escapes.sanitizeFilename(file.getFilename());
            try {
                String url;
                byte[] bytes;
                suffix = Headers.getSuffix(file);
                final String name = StringUtils.substringBeforeLast(fileName, ".");
                final String uuid = StringUtils.substring(UUID.randomUUID().toString().replaceAll("-", ""), 0, 8);
                fileName = name + '-' + uuid + "." + suffix;
                fileName = genFilePath(fileName);
                if (QN_ENABLED) {
                    bytes = fileBytes.get(i);
                    final String contentType = file.getContentType();
                    uploadManager.asyncPut(bytes, fileName, uploadToken, null, contentType, false, (key, r) -> {
                        LOGGER.log(Level.TRACE, "Uploaded [" + key + "], response [" + r.toString() + "]");
                        countDownLatch.countDown();
                    });
                    url = Symphonys.UPLOAD_QINIU_DOMAIN + "/" + fileName;
                    succMap.put(originalName, url);
                } else {
                    final Path path = Paths.get(Symphonys.UPLOAD_LOCAL_DIR, fileName);
                    path.getParent().toFile().mkdirs();
                    try (final OutputStream output = new FileOutputStream(path.toFile())) {
                        IOUtils.write(file.getData(), output);
                        countDownLatch.countDown();
                    }
                    url = Latkes.getServePath() + "/upload/" + fileName;
                    succMap.put(originalName, url);
                }
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Uploads file failed", e);

                errFiles.add(originalName);
            }
        }

        try {
            countDownLatch.await(1, TimeUnit.MINUTES);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Count down latch failed", e);
        }

        data.put("errFiles", errFiles);
        data.put("succMap", succMap);
        result.put(Common.DATA, data);
        result.put(Keys.CODE, StatusCodes.SUCC);
        result.put(Keys.MSG, "");
    }

    /**
     * Fetches the remote file and upload it.
     *
     * @param context the specified context
     */
    public void fetchUpload(final RequestContext context) {
        final JSONObject result = Results.newFail();
        context.renderJSONPretty(result);
        final JSONObject data = new JSONObject();

        final JSONObject requestJSONObject = context.requestJSON();
        final String originalURL = requestJSONObject.optString(Common.URL);
        if (!Strings.isURL(originalURL) || !StringUtils.startsWithIgnoreCase(originalURL, "http")) {
            return;
        }

        byte[] bytes;
        String contentType;
        try {
            final String host = new URL(originalURL).getHost();
            final String hostIp = InetAddress.getByName(host).getHostAddress();
            if (Networks.isInnerAddress(hostIp)) {
                return;
            }

            final HttpRequest req = HttpRequest.get(originalURL).header(Common.USER_AGENT, Symphonys.USER_AGENT_BOT);
            final HttpResponse res = req.connectionTimeout(3000).timeout(5000).send();
            res.close();
            if (200 != res.statusCode()) {
                return;
            }

            bytes = res.bodyBytes();
            contentType = res.contentType();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Fetch file [url=" + originalURL + "] failed", e);
            return;
        }

        final String suffix = Headers.getSuffix(contentType);
        final String[] allowedSuffixArray = Symphonys.UPLOAD_SUFFIX.split(",");
        if (!Strings.containsIgnoreCase(suffix, allowedSuffixArray)) {
            String msg = langPropsService.get("invalidFileSuffixLabel");
            msg = StringUtils.replace(msg, "${suffix}", suffix);
            result.put(Keys.MSG, msg);
            return;
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + suffix;

        if (Symphonys.QN_ENABLED) {
            final Auth auth = Auth.create(Symphonys.UPLOAD_QINIU_AK, Symphonys.UPLOAD_QINIU_SK);
            final UploadManager uploadManager = new UploadManager(new Configuration());

            try {
                uploadManager.put(bytes, "e/" + fileName, auth.uploadToken(Symphonys.UPLOAD_QINIU_BUCKET), null, contentType, false);
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Uploads to Qiniu failed", e);
            }

            data.put(Common.URL, Symphonys.UPLOAD_QINIU_DOMAIN + "/e/" + fileName);
            data.put("originalURL", originalURL);
        } else {
            fileName = FileUploadProcessor.genFilePath(fileName);
            final Path path = Paths.get(Symphonys.UPLOAD_LOCAL_DIR, fileName);
            path.getParent().toFile().mkdirs();
            try (final OutputStream output = new FileOutputStream(Symphonys.UPLOAD_LOCAL_DIR + fileName)) {
                IOUtils.write(bytes, output);
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Writes output stream failed", e);
            }

            data.put(Common.URL, Latkes.getServePath() + "/upload/" + fileName);
            data.put("originalURL", originalURL);
        }

        result.put(Common.DATA, data);
        result.put(Keys.CODE, StatusCodes.SUCC);
        result.put(Keys.MSG, "");
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
