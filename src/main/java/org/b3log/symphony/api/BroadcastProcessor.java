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
package org.b3log.symphony.api;

import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.service.ArticleQueryService;
import org.json.JSONObject;

/**
 * Broadcast processor.
 *
 * <ul>
 * <li>Gets the broadcasts (/apis/broadcasts), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Apr 18, 2013
 * @since 0.2.1
 */
@RequestProcessor
public class BroadcastProcessor {

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Broadcasts fetch size.
     */
    private static final int BROADCASTS_FETCH_SIZE = 10;

    /**
     * Gets the broadcasts.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/apis/broadcasts", method = HTTPRequestMethod.GET)
    public void getBroadcasts(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONRenderer renderer = new JSONRenderer().setJSONP(true);
        context.setRenderer(renderer);

        renderer.setCallback(request.getParameter("callback"));

        final JSONObject ret = new JSONObject();
        renderer.setJSONObject(ret);

        final List<JSONObject> broadcasts = articleQueryService.getBroadcasts(1, BROADCASTS_FETCH_SIZE);

        ret.put(Article.ARTICLES, broadcasts);
    }
}
