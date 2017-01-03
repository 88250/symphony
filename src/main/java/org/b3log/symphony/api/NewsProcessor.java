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
 * News processor.
 *
 * <ul>
 * <li>Gets the news (/apis/news), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Nov 8, 2012
 * @since 0.2.0
 */
@RequestProcessor
public class NewsProcessor {

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * News fetch size.
     */
    private static final int NEWS_FETCH_SIZE = 10;

    /**
     * Gets the news.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/apis/news", method = HTTPRequestMethod.GET)
    public void getNews(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONRenderer renderer = new JSONRenderer().setJSONP(true);
        context.setRenderer(renderer);

        renderer.setCallback(request.getParameter("callback"));

        final JSONObject ret = new JSONObject();
        renderer.setJSONObject(ret);

        final List<JSONObject> news = articleQueryService.getNews(1, NEWS_FETCH_SIZE);

        ret.put(Article.ARTICLES, news);
    }
}
