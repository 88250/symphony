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

import java.net.URL;
import java.net.URLEncoder;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.HTTPResponse;
import org.b3log.latke.urlfetch.URLFetchService;
import org.b3log.latke.urlfetch.URLFetchServiceFactory;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Turing query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Apr 27, 2016
 * @since 1.4.0
 */
@Service
public class TuringQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TuringQueryService.class.getName());

    /**
     * Enabled Turing Robot or not.
     */
    private static final boolean TURING_ENABLED = Symphonys.getBoolean("turing.enabled");

    /**
     * Turing Robot API.
     */
    private static final String TURING_API = Symphonys.get("turing.api");

    /**
     * Turing Robot Key.
     */
    private static final String TURING_KEY = Symphonys.get("turing.key");

    /**
     * Robot name.
     */
    public static final String ROBOT_NAME = Symphonys.get("turing.name");

    /**
     * Robot avatar.
     */
    public static final String ROBOT_AVATAR = Symphonys.get("turing.avatar");

    /**
     * URL fetch service.
     */
    private static final URLFetchService URL_FETCH_SVC = URLFetchServiceFactory.getURLFetchService();

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Chat with Turing Robot.
     *
     * @param userName the specified user name
     * @param msg the specified message
     * @return robot returned message, return {@code null} if not found
     */
    public String chat(final String userName, final String msg) {
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(msg) || !TURING_ENABLED) {
            return null;
        }

        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.POST);

        try {
            request.setURL(new URL(TURING_API));

            final String body = "key=" + URLEncoder.encode(TURING_KEY, "UTF-8")
                    + "&info=" + URLEncoder.encode(msg, "UTF-8")
                    + "&userid=" + URLEncoder.encode(userName, "UTF-8");
            request.setPayload(body.getBytes("UTF-8"));

            final HTTPResponse response = URL_FETCH_SVC.fetch(request);
            final JSONObject data = new JSONObject(new String(response.getContent(), "UTF-8"));
            final int code = data.optInt("code");

            switch (code) {
                case 40001:
                case 40002:
                case 40007:
                    LOGGER.log(Level.ERROR, data.optString("text"));

                    return null;
                case 40004:
                    return langPropsService.get("turingQuotaExceedLabel");
                case 100000:
                    return data.optString("text");
                case 200000:
                    return data.optString("text") + " " + data.optString("url");
                case 302000:
                    String ret302000 = data.optString("text") + " ";
                    final JSONArray list302000 = data.optJSONArray("list");
                    final StringBuilder builder302000 = new StringBuilder();
                    for (int i = 0; i < list302000.length(); i++) {
                        final JSONObject news = list302000.optJSONObject(i);
                        builder302000.append(news.optString("article")).append(news.optString("detailurl"))
                                .append("\n\n");
                    }

                    return ret302000 + " " + builder302000.toString();
                case 308000:
                    String ret308000 = data.optString("text") + " ";
                    final JSONArray list308000 = data.optJSONArray("list");
                    final StringBuilder builder308000 = new StringBuilder();
                    for (int i = 0; i < list308000.length(); i++) {
                        final JSONObject news = list308000.optJSONObject(i);
                        builder308000.append(news.optString("name")).append(news.optString("detailurl"))
                                .append("\n\n");
                    }

                    return ret308000 + " " + builder308000.toString();
                default:
                    LOGGER.log(Level.WARN, "Turing Robot default return [" + data.toString(4) + "]");
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Chat with Turing Robot failed", e);
        }

        return null;
    }
}
