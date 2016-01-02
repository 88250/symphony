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
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.TagQueryService;
import org.json.JSONObject;

/**
 * Article processor.
 *
 * <ul>
 * <li>Gets articles with the specified tags (/apis/articles?tags=tag1,tag2&p=1&size=10), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.1, Jan 2, 2016
 * @since 0.2.5
 */
@RequestProcessor
public class ArticleProcessor {

    /**
     * Tag query service.
     */
    @Inject
    private TagQueryService tagQueryService;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Gets articles.with the specified tags.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/apis/articles", method = HTTPRequestMethod.GET)
    public void getTagsArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONRenderer renderer = new JSONRenderer().setJSONP(true);
        context.setRenderer(renderer);

        String callback = request.getParameter("callback");
        if (Strings.isEmptyOrNull(callback)) {
            callback = "callback";
        }
        renderer.setCallback(callback);

        final JSONObject ret = new JSONObject();
        renderer.setJSONObject(ret);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        String pageSizeStr = request.getParameter("size");
        if (Strings.isEmptyOrNull(pageSizeStr) || !Strings.isNumeric(pageSizeStr)) {
            pageSizeStr = "10";
        }

        final String tagsStr = request.getParameter("tags");
        final String[] tagTitles = tagsStr.split(",");

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Integer.valueOf(pageSizeStr);

        final List<JSONObject> interests = articleQueryService.getInterests(pageNum, pageSize, tagTitles);

        ret.put(Article.ARTICLES, interests);
    }

    /**
     * Gets articles.with the specified tags.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/api/v1/stories/", method = HTTPRequestMethod.GET)
    public void getArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        int currentPage = 1;
        final int pageSize = 25;
        final String page = request.getParameter("page");
        if (Strings.isNumeric(page)) {
            currentPage = Integer.parseInt(page);
        }

        final JSONObject ret = new JSONObject();
        ret.put("stories", this.articleQueryService.getTopArticlesWithComments(currentPage, pageSize));

        context.renderJSON(ret);
    }

    /**
     * Gets articles.with the specified tags.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/api/v1/stories/recent", method = HTTPRequestMethod.GET)
    public void getRecentArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        int currentPage = 1;
        final int pageSize = 25;
        final String page = request.getParameter("page");
        if (Strings.isNumeric(page)) {
            currentPage = Integer.parseInt(page);
        }

        final JSONObject ret = new JSONObject();
        ret.put("stories", this.articleQueryService.getRecentArticlesWithComments(currentPage, pageSize));

        context.renderJSON(ret);
    }

    /**
     * Gets articles with the specified query.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/api/v1/stories/search", method = HTTPRequestMethod.GET)
    public void searchArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        getRecentArticles(context, request, response);
    }
}
