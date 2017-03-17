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
package org.b3log.symphony.service;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Audio management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 17, 2017
 * @since 2.1.0
 */
@Service
public class AudioMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AudioMgmtService.class);

    private static final Set<String> DOING = new ConcurrentSkipListSet<>();

    private static final String BAIDU_API_KEY = Symphonys.get("baidu.yuyin.apiKey");
    private static final String BAIDU_SECRET_KEY = Symphonys.get("baidu.yuyin.secretKey");
    private static String BAIDU_ACCESS_TOKEN;
    private static long BAIDU_ACCESS_TOKEN_TIME;

    private static void refreshBaiduAccessToken() {
        if (null == BAIDU_API_KEY || null == BAIDU_SECRET_KEY) {
            return;
        }

        final long now = System.currentTimeMillis();
        if (StringUtils.isNotBlank(BAIDU_ACCESS_TOKEN) && now - 1000 * 60 * 60 * 6 < BAIDU_ACCESS_TOKEN_TIME) {
            return;
        }

        try {
            final URL url = new URL("https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials"
                    + "&client_id=" + BAIDU_API_KEY + "&client_secret=" + BAIDU_SECRET_KEY);

            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            final InputStream inputStream = conn.getInputStream();
            final String content = IOUtils.toString(inputStream);
            IOUtils.closeQuietly(inputStream);

            conn.disconnect();

            final JSONObject result = new JSONObject(content);
            BAIDU_ACCESS_TOKEN = result.optString("access_token", null);
            BAIDU_ACCESS_TOKEN_TIME = now;

            return;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Requires Baidu Yuyin access token failed", e);
        }
    }

    private static byte[] baiduTTS(final String text, final String uid) {
        if (null == BAIDU_ACCESS_TOKEN) {
            refreshBaiduAccessToken();
        }

        if (null == BAIDU_ACCESS_TOKEN) {
            LOGGER.warn("Please configure [baidu.yuyin.*] in symphony.properties");

            return null;
        }

        try {
            final URL url = new URL("http://tsn.baidu.com/text2audio?grant_type=client_credentials"
                    + "&client_id=" + BAIDU_API_KEY + "&client_secret=" + BAIDU_SECRET_KEY);

            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            final OutputStream outputStream = conn.getOutputStream();
            IOUtils.write("tex=" + StringUtils.substring(text, 0, 1024) + "&lan=zh&cuid=" + uid + "&ctp=1&tok=" + BAIDU_ACCESS_TOKEN, outputStream);
            IOUtils.closeQuietly(outputStream);

            final InputStream inputStream = conn.getInputStream();

            final int responseCode = conn.getResponseCode();
            final String contentType = conn.getContentType();

            if (HttpServletResponse.SC_OK != responseCode || !"audio/mp3".equals(contentType)) {
                final String msg = IOUtils.toString(inputStream);
                LOGGER.warn("Baidu Yuyin TTS failed: " + msg.toString());
                IOUtils.closeQuietly(inputStream);
                conn.disconnect();

                return null;
            }

            final byte[] ret = IOUtils.toByteArray(inputStream);
            IOUtils.closeQuietly(inputStream);
            conn.disconnect();

            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Requires Baidu Yuyin access token failed", e);
        }

        return null;
    }

    /**
     * Text to speech.
     *
     * @param text   the specified text
     * @param type   specified type, "article"/"comment"
     * @param textId the specified id of text, article id or comment id
     * @param uid    the specified user id
     * @return speech URL
     */
    public String tts(final String text, final String type, final String textId, final String uid) {
        final byte[] bytes = baiduTTS(text, uid);
        if (null == bytes) {
            return null;
        }

        String ret;

        DOING.add(textId);
        try {
            if (Symphonys.getBoolean("qiniu.enabled")) {
                final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
                final UploadManager uploadManager = new UploadManager();

                final int seq = RandomUtils.nextInt(10);
                uploadManager.put(bytes, "audio/" + type + "/" + textId + seq + ".mp3", auth.uploadToken(Symphonys.get("qiniu.bucket")),
                        null, "audio/mp3", false);
                ret = Symphonys.get("qiniu.domain") + "/audio/" + type + "/" + textId + seq + ".mp3";
            } else {
                final String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".mp3";
                final OutputStream output = new FileOutputStream(Symphonys.get("upload.dir") + fileName);
                IOUtils.write(bytes, output);
                IOUtils.closeQuietly(output);

                ret = Latkes.getServePath() + "/upload/" + fileName;
            }

            return ret;
        } catch (final Exception e) {
            if (e instanceof QiniuException) {
                try {
                    LOGGER.log(Level.ERROR, "Uploads audio failed [type=" + type + ", textId=" + textId + "]",
                            ((QiniuException) e).response.bodyString());
                } catch (final Exception qe) {
                    LOGGER.log(Level.ERROR, "Uploads audio and parse result exception", qe);
                }
            } else {
                LOGGER.log(Level.ERROR, "Uploads audio failed [type=" + type + ", textId=" + textId + "]", e);
            }
        } finally {
            DOING.remove(textId);
        }

        return null;
    }
}
