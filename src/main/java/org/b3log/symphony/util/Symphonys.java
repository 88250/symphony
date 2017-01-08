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
package org.b3log.symphony.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.io.IOUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeMode;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.Lifecycle;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.LangPropsServiceImpl;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.SymphonyServletListener;
import org.json.JSONObject;

/**
 * Symphony utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.7.0.7, Jan 8, 2017
 * @since 0.1.0
 */
public final class Symphonys {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Symphonys.class);

    /**
     * Configurations.
     */
    public static final ResourceBundle CFG = ResourceBundle.getBundle("symphony");

    /**
     * HacPai bot User-Agent.
     */
    public static final String USER_AGENT_BOT = "Mozilla/5.0 (compatible; HacPai/1.1; +https://hacpai.com)";

    /**
     * Reserved tags.
     */
    public static final String[] RESERVED_TAGS;

    /**
     * White list - tags.
     */
    public static final String[] WHITE_LIST_TAGS;

    /**
     * Reserved user names.
     */
    public static final String[] RESERVED_USER_NAMES;

    /**
     * Thread pool.
     */
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(50);

    static {
        // Loads reserved tags
        final String reservedTags = CFG.getString("reservedTags");
        final String[] tags = reservedTags.split(",");
        RESERVED_TAGS = new String[tags.length];

        for (int i = 0; i < tags.length; i++) {
            final String tag = tags[i];

            RESERVED_TAGS[i] = tag.trim();
        }

        // Loads white list tags
        final String whiteListTags = CFG.getString("whitelist.tags");
        final String[] wlTags = whiteListTags.split(",");
        WHITE_LIST_TAGS = new String[wlTags.length];

        for (int i = 0; i < wlTags.length; i++) {
            final String tag = wlTags[i];

            WHITE_LIST_TAGS[i] = tag.trim();
        }

        // Loads reserved usernames
        final String reservedUserNames = CFG.getString("reservedUserNames");
        final String[] userNames = reservedUserNames.split(",");
        RESERVED_USER_NAMES = new String[userNames.length];

        for (int i = 0; i < userNames.length; i++) {
            final String userName = userNames[i];

            RESERVED_USER_NAMES[i] = userName.trim();
        }
    }

    static {
        try {
            final SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) {
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }}, new SecureRandom());
            SSLContext.setDefault(ctx);
        } catch (final Exception e) {
            // ignore
        }

        // Reports status to Rhythm, I hope that everyone will be able to join in the SymHub plan :p
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                final String symURL = Latkes.getServePath();
                if (Networks.isIPv4(symURL)) {
                    return;
                }

                HttpURLConnection httpConn = null;
                OutputStream outputStream = null;

                try {
                    final LatkeBeanManager beanManager = Lifecycle.getBeanManager();
                    final LangPropsService langPropsService = beanManager.getReference(LangPropsServiceImpl.class);

                    httpConn = (HttpURLConnection) new URL("https://rhythm.b3log.org/sym").openConnection();
                    httpConn.setConnectTimeout(10000);
                    httpConn.setReadTimeout(10000);
                    httpConn.setDoOutput(true);
                    httpConn.setRequestMethod("POST");
                    httpConn.setRequestProperty("User-Agent", "B3log Symphony/" + SymphonyServletListener.VERSION);

                    httpConn.connect();

                    outputStream = httpConn.getOutputStream();
                    final JSONObject sym = new JSONObject();
                    sym.put("symURL", symURL);
                    sym.put("symTitle", langPropsService.get("symphonyLabel", Latkes.getLocale()));

                    IOUtils.write(sym.toString(), outputStream, "UTF-8");
                    outputStream.flush();

                    httpConn.getResponseCode();
                } catch (final Exception e) {
                    // ignore
                } finally {
                    IOUtils.closeQuietly(outputStream);

                    if (null != httpConn) {
                        try {
                            httpConn.disconnect();
                        } catch (final Exception e) {
                            // ignore
                        }
                    }
                }
            }
        }, 1000 * 60 * 60 * 2, 1000 * 60 * 60 * 2);
    }

    /**
     * Gets all symphonies.
     *
     * @return a list of symphonies
     */
    public static List<JSONObject> getSyms() {
        HttpURLConnection httpConn = null;
        InputStream inputStream = null;

        try {
            httpConn = (HttpURLConnection) new URL("https://rhythm.b3log.org/syms").openConnection();
            httpConn.setConnectTimeout(10000);
            httpConn.setReadTimeout(10000);
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("User-Agent", "B3log Symphony/" + SymphonyServletListener.VERSION);

            httpConn.connect();

            inputStream = httpConn.getInputStream();
            final String data = IOUtils.toString(inputStream, "UTF-8");
            final JSONObject result = new JSONObject(data);
            if (!result.optBoolean(Keys.STATUS_CODE)) {
                return Collections.emptyList();
            }

            return CollectionUtils.jsonArrayToList(result.optJSONArray("syms"));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets syms from Rhythm failed", e);

            return Collections.emptyList();
        } finally {
            IOUtils.closeQuietly(inputStream);

            if (null != httpConn) {
                try {
                    httpConn.disconnect();
                } catch (final Exception e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Does Symphony runs on development environment?
     *
     * @return {@code true} if it runs on development environment, {@code false} otherwise
     */
    public static boolean runsOnDevEnv() {
        return RuntimeMode.DEVELOPMENT == Latkes.getRuntimeMode();
    }

    /**
     * Gets a configuration string property with the specified key.
     *
     * @param key the specified key
     * @return string property value corresponding to the specified key, returns {@code null} if not found
     */
    public static String get(final String key) {
        return CFG.getString(key);
    }

    /**
     * Gets a configuration boolean property with the specified key.
     *
     * @param key the specified key
     * @return boolean property value corresponding to the specified key, returns {@code null} if not found
     */
    public static Boolean getBoolean(final String key) {
        final String stringValue = get(key);

        if (null == stringValue) {
            return null;
        }

        return Boolean.valueOf(stringValue);
    }

    /**
     * Gets a configuration float property with the specified key.
     *
     * @param key the specified key
     * @return float property value corresponding to the specified key, returns {@code null} if not found
     */
    public static Float getFloat(final String key) {
        final String stringValue = get(key);
        if (null == stringValue) {
            return null;
        }

        return Float.valueOf(stringValue);
    }

    /**
     * Gets a configuration integer property with the specified key.
     *
     * @param key the specified key
     * @return integer property value corresponding to the specified key, returns {@code null} if not found
     */
    public static Integer getInt(final String key) {
        final String stringValue = get(key);
        if (null == stringValue) {
            return null;
        }

        return Integer.valueOf(stringValue);
    }

    /**
     * Gets a configuration long property with the specified key.
     *
     * @param key the specified key
     * @return long property value corresponding to the specified key, returns {@code null} if not found
     */
    public static Long getLong(final String key) {
        final String stringValue = get(key);
        if (null == stringValue) {
            return null;
        }

        return Long.valueOf(stringValue);
    }

    /**
     * Private default constructor.
     */
    private Symphonys() {
    }
}
