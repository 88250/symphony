/*
 * Copyright (c) 2012-2015, b3log.org
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
package org.b3log.symphony.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.json.JSONObject;

/**
 * Activity api processor.
 *
 * <ul>
 * <li>Daily checkin (/api/v1/activities/checkin), POST</li>
 * </ul>
 *
 * @author <a href="http://wdx.me">DX</a>
 * @version 1.0.0.0, Aug 13, 2015
 * @since 0.2.5
 */
public class ActivityProcessor {
    
    /**
     * Mobile logins user.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     */
    @RequestProcessing(value = "/api/v1/activities/checkin", method = HTTPRequestMethod.POST)
    public void checkin(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response) {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);
        final JSONObject ret = new JSONObject();
        renderer.setJSONObject(ret);
        ret.put("balance", 1905);
        ret.put("reward", 5);
        ret.put("streak", 2);
        ret.put("streaked", 10);
    }
}
