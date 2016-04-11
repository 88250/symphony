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
package org.b3log.symphony.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.symphony.model.Common;
import org.json.JSONObject;

/**
 * Geography utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.2.1, Apr 11, 2016
 * @since 1.3.0
 */
public final class Geos {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Geos.class.getName());

    /**
     * Gets country, province and city of the specified IP.
     *
     * @param ip the specified IP
     * @return address info, for example      <pre>
     * {
     *     "country": "",
     *     "province": "",
     *     "city": ""
     * }
     * </pre>, returns {@code null} if not found
     */
    public static JSONObject getAddress(final String ip) {
        final String ak = Symphonys.get("baidu.lbs.ak");

        if (StringUtils.isBlank(ak) || !Networks.isIPv4(ip)) {
            return null;
        }

        HttpURLConnection conn = null;

        try {
            final URL url = new URL("http://api.map.baidu.com/location/ip?ip=" + ip
                    + "&ak=" + ak);

            conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);

            final BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line = null;

            final StringBuilder sb = new StringBuilder();
            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
            }

            final JSONObject data = new JSONObject(sb.toString());
            if (0 != data.optInt("status")) {
                return getAddressSina(ip); // Try it via Sina API
            }

            final String content = data.optString("address");
            final String country = content.split("\\|")[0];
            if (!"CN".equals(country) && !"HK".equals(country)) {
                LOGGER.log(Level.WARN, "Found other country via Baidu [" + country + ", " + ip + "]");

                return null;
            }

            final String province = content.split("\\|")[1];
            String city = content.split("\\|")[2];

            if ("None".equals(province) || "None".equals(city)) {
                return getAddressSina(ip); // Try it via Sina API
            }

            city = StringUtils.replace(city, "市", "");

            final JSONObject ret = new JSONObject();
            ret.put(Common.COUNTRY, "中国");
            ret.put(Common.PROVINCE, province);
            ret.put(Common.CITY, city);

            return ret;
        } catch (final SocketTimeoutException e) {
            LOGGER.log(Level.ERROR, "Get location from Baidu timeout [ip=" + ip + "]");

            return null;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Can't get location from Baidu [ip=" + ip + "]", e);

            return null;
        } finally {
            if (null != conn) {
                try {
                    conn.disconnect();
                } catch (final Exception e) {
                    LOGGER.log(Level.ERROR, "Close HTTP connection error", e);
                }
            }
        }
    }

    /**
     * Gets province, city of the specified IP by Sina API.
     *
     * @param ip the specified IP
     * @return address info, for example      <pre>
     * {
     *     "province": "",
     *     "city": ""
     * }
     * </pre>, returns {@code null} if not found
     */
    private static JSONObject getAddressSina(final String ip) {
        HttpURLConnection conn = null;

        try {
            final URL url = new URL("http://int.dpool.sina.com.cn/iplookup/iplookup.php?ip=" + ip + "&format=json");

            conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);

            final BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line = null;

            final StringBuilder sb = new StringBuilder();
            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
            }

            final JSONObject data = new JSONObject(sb.toString());
            if (1 != data.optInt("ret")) {
                return null;
            }

            final String country = data.optString("country");
            final String province = data.optString("province");
            String city = data.optString("city");
            city = StringUtils.replace(city, "市", "");

            final JSONObject ret = new JSONObject();
            ret.put(Common.COUNTRY, country);
            ret.put(Common.PROVINCE, province);
            ret.put(Common.CITY, city);

            return ret;
        } catch (final SocketTimeoutException e) {
            LOGGER.log(Level.ERROR, "Get location from Sina timeout [ip=" + ip + "]");

            return null;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Can't get location from Sina [ip=" + ip + "]", e);

            return null;
        } finally {
            if (null != conn) {
                try {
                    conn.disconnect();
                } catch (final Exception e) {
                    LOGGER.log(Level.ERROR, "Close HTTP connection error", e);
                }
            }
        }
    }

    /**
     * Private constructor.
     */
    private Geos() {
    }
}
