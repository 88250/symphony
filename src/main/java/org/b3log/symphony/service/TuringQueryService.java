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
package org.b3log.symphony.service;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Turing query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.3, Aug 2, 2018
 * @since 1.4.0
 */
@Service
public class TuringQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TuringQueryService.class);

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
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Chat with Turing Robot.
     *
     * @param userName the specified user name
     * @param msg      the specified message
     * @return robot returned message, return {@code null} if not found
     */
    public String chat(final String userName, final String msg) {
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(msg) || !TURING_ENABLED) {
            return null;
        }

        try {
            final JSONObject reqData = new JSONObject();
            reqData.put("reqType", 0);
            final JSONObject perception = new JSONObject();
            final JSONObject inputText = new JSONObject();
            inputText.put("text", msg);
            perception.put("inputText", inputText);
            reqData.put("perception", perception);
            final JSONObject userInfo = new JSONObject();
            userInfo.put("apiKey", TURING_KEY);
            userInfo.put("userId", userName);
            userInfo.put("userIdName", userName);
            reqData.put("userInfo", userInfo);

            final HttpResponse response = HttpRequest.post(TURING_API).bodyText(reqData.toString()).contentTypeJson().timeout(5000).send();
            response.charset("UTF-8");
            final JSONObject data = new JSONObject(response.bodyText());
            final JSONObject intent = data.optJSONObject("intent");
            final int code = intent.optInt("code");
            final JSONArray results = data.optJSONArray("results");
            switch (code) {
                case 5000:
                case 6000:
                case 4000:
                case 4001:
                case 4002:
                case 4003:
                case 4005:
                case 4007:
                case 4100:
                case 4200:
                case 4300:
                case 4400:
                case 4500:
                case 4600:
                case 4602:
                case 7002:
                case 8008:
                    LOGGER.log(Level.ERROR, "Turing query failed with code [" + code + "]");

                    return langPropsService.get("turingQuotaExceedLabel");
                case 10004:
                case 10019:
                case 10014:
                case 10013:
                case 10008:
                case 10011:
                    final StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < results.length(); i++) {
                        final JSONObject result = results.optJSONObject(i);
                        final String resultType = result.optString("resultType");
                        String values = result.optJSONObject("values").optString(resultType);
                        if (StringUtils.endsWithAny(values, new String[]{"jpg", "png", "gif"})) {
                            values = "![](" + values + ")";
                        }

                        builder.append(values).append("\n");
                    }
                    String ret = builder.toString();
                    ret = StringUtils.trim(ret);

                    return ret;
                default:
                    LOGGER.log(Level.WARN, "Turing Robot default return [" + data.toString(4) + "]");

                    return langPropsService.get("turingQuotaExceedLabel");
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Chat with Turing Robot failed", e);
        }

        return null;
    }
}
