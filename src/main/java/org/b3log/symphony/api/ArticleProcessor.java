/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * @version 1.0.0.0, Oct 19, 2014
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

        final String pageSizeStr = request.getParameter("size");
        if (Strings.isEmptyOrNull(pageSizeStr) || !Strings.isNumeric(pageSizeStr)) {
            pageNumStr = "10";
        }

        final String tagsStr = request.getParameter("tags");
        final String[] tagTitles = tagsStr.split(",");

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Integer.valueOf(pageSizeStr);

        final List<JSONObject> tagList = new ArrayList<JSONObject>();
        for (int i = 0; i < tagTitles.length; i++) {
            final String tagTitle = tagTitles[i];
            final JSONObject tag = tagQueryService.getTagByTitle(tagTitle);
            if (null == tag) {
                continue;
            }

            tagList.add(tag);
        }

        if (tagList.isEmpty()) {
            ret.put(Article.ARTICLES, Collections.emptyList());

            return;
        }

        final Map<String, Class<?>> articleFields = new HashMap<String, Class<?>>();
        articleFields.put(Article.ARTICLE_TITLE, String.class);
        articleFields.put(Article.ARTICLE_PERMALINK, String.class);
        articleFields.put(Article.ARTICLE_CREATE_TIME, Long.class);

        final List<JSONObject> articles = articleQueryService.getArticlesByTags(pageNum, pageSize,
                                                                                articleFields, tagList.toArray(new JSONObject[0]));
        for (final JSONObject article : articles) {
            article.remove(Article.ARTICLE_T_PARTICIPANTS);
            article.remove(Article.ARTICLE_T_PARTICIPANT_NAME);
            article.remove(Article.ARTICLE_T_PARTICIPANT_THUMBNAIL_URL);
            article.remove(Article.ARTICLE_LATEST_CMT_TIME);
            article.remove(Article.ARTICLE_UPDATE_TIME);

            article.put(Article.ARTICLE_CREATE_TIME, ((Date) article.get(Article.ARTICLE_CREATE_TIME)).getTime());
        }

        ret.put(Article.ARTICLES, articles);
    }
}
