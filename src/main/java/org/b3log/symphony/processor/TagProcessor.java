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
package org.b3log.symphony.processor;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.FollowQueryService;
import org.b3log.symphony.service.TagQueryService;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Tag processor.
 *
 * <ul>
 * <li>Shows tags wall (/tags), GET</li>
 * <li>Shows tag articles (/tag/{tagTitle}), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.0.3, Apr 14, 2016
 * @since 0.2.0
 */
@RequestProcessor
public class TagProcessor {

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
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    /**
     * Filler.
     */
    @Inject
    private Filler filler;

    /**
     * Tag cache.
     */
    @Inject
    private TagCache tagCache;

    /**
     * Caches tags.
     *
     * @param request the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @param context the specified HTTP request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/cron/tag/cache-tags", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void cacheIconTags(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        tagCache.loadIconTags();
        tagCache.loadAllTags();

        context.renderJSON().renderTrueResult();
    }

    /**
     * Shows tags wall.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/tags", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showTagsWall(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);

        renderer.setTemplateName("tags.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final List<JSONObject> trendTags = tagQueryService.getTrendTags(Symphonys.getInt("tagsWallTrendCnt"));
        final List<JSONObject> coldTags = tagQueryService.getColdTags(Symphonys.getInt("tagsWallColdCnt"));

        dataModel.put(Common.TREND_TAGS, trendTags);
        dataModel.put(Common.COLD_TAGS, coldTags);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows tag articles.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param tagTitle the specified tag title
     * @throws Exception exception
     */
    @RequestProcessing(value = "/tag/{tagTitle}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showTagArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String tagTitle) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);

        renderer.setTemplateName("tag-articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        filler.fillHeaderAndFooter(request, response, dataModel);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Symphonys.getInt("tagArticlesCnt");
        final int windowSize = Symphonys.getInt("tagArticlesWindowSize");

        final JSONObject tag = tagQueryService.getTagByTitle(tagTitle);
        if (null == tag) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        tag.put(Common.IS_RESERVED, tagQueryService.isReservedTag(tagTitle));

        dataModel.put(Tag.TAG, tag);

        final String tagId = tag.optString(Keys.OBJECT_ID);

        final List<JSONObject> relatedTags = tagQueryService.getRelatedTags(tagId, Symphonys.getInt("tagRelatedTagsCnt"));
        tag.put(Tag.TAG_T_RELATED_TAGS, (Object) relatedTags);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, tagId);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        final List<JSONObject> articles = articleQueryService.getArticlesByTag(tag, pageNum, pageSize);
        dataModel.put(Article.ARTICLES, articles);

        final JSONObject tagCreator = tagQueryService.getCreator(tagId);

        tag.put(Tag.TAG_T_CREATOR_THUMBNAIL_URL, tagCreator.optString(Tag.TAG_T_CREATOR_THUMBNAIL_URL));
        tag.put(Tag.TAG_T_CREATOR_NAME, tagCreator.optString(Tag.TAG_T_CREATOR_NAME));
        tag.put(Tag.TAG_T_CREATOR_THUMBNAIL_UPDATE_TIME, tagCreator.optLong(Tag.TAG_T_CREATOR_THUMBNAIL_UPDATE_TIME));
        tag.put(Tag.TAG_T_PARTICIPANTS, (Object) tagQueryService.getParticipants(tagId, Symphonys.getInt("tagParticipantsCnt")));

        final int tagRefCnt = tag.getInt(Tag.TAG_REFERENCE_CNT);
        final int pageCount = (int) Math.ceil(tagRefCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        filler.fillRandomArticles(dataModel);
        filler.fillHotArticles(dataModel);
        filler.fillSideTags(dataModel);
        filler.fillLatestCmts(dataModel);
    }
}
