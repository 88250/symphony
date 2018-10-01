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
package org.b3log.symphony.processor;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Paginator;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.AnonymousViewCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tag processor.
 * <ul>
 * <li>Shows tags wall (/tags), GET</li>
 * <li>Shows tag articles (/tag/{tagTitle}), GET</li>
 * <li>Query tags (/tags/query), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.7.0.13, Jun 6, 2018
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
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Queries tags.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     */
    @RequestProcessing(value = "/tags/query", method = HTTPRequestMethod.GET)
    public void queryTags(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response) {
        if (null == request.getAttribute(Common.CURRENT_USER)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        context.renderJSON().renderTrueResult();

        final String titlePrefix = request.getParameter("title");

        List<JSONObject> tags;
        final int fetchSize = 7;
        if (StringUtils.isBlank(titlePrefix)) {
            tags = tagQueryService.getTags(fetchSize);
        } else {
            tags = tagQueryService.getTagsByPrefix(titlePrefix, fetchSize);
        }

        final List<String> ret = new ArrayList<>();
        for (final JSONObject tag : tags) {
            ret.add(tag.optString(Tag.TAG_TITLE));
        }

        context.renderJSONValue(Tag.TAGS, ret);
    }

    /**
     * Shows tags wall.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/tags", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showTagsWall(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("tags.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final List<JSONObject> trendTags = tagQueryService.getTrendTags(Symphonys.getInt("tagsWallTrendCnt"));
        final List<JSONObject> coldTags = tagQueryService.getColdTags(Symphonys.getInt("tagsWallColdCnt"));

        dataModel.put(Common.TREND_TAGS, trendTags);
        dataModel.put(Common.COLD_TAGS, coldTags);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows tag articles.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param tagURI   the specified tag URI
     * @throws Exception exception
     */
    @RequestProcessing(value = {"/tag/{tagURI}", "/tag/{tagURI}/hot", "/tag/{tagURI}/good", "/tag/{tagURI}/reply",
            "/tag/{tagURI}/perfect"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showTagArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                                final String tagURI) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("tag-articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        int pageSize = Symphonys.getInt("indexArticlesCnt");

        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);

            if (!UserExt.finshedGuide(user)) {
                response.sendRedirect(Latkes.getServePath() + "/guide");

                return;
            }
        }

        final JSONObject tag = tagQueryService.getTagByURI(tagURI);
        if (null == tag) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        tag.put(Common.IS_RESERVED, tagQueryService.isReservedTag(tag.optString(Tag.TAG_TITLE)));

        dataModel.put(Tag.TAG, tag);

        final String tagId = tag.optString(Keys.OBJECT_ID);

        final List<JSONObject> relatedTags = tagQueryService.getRelatedTags(tagId, Symphonys.getInt("tagRelatedTagsCnt"));
        tag.put(Tag.TAG_T_RELATED_TAGS, (Object) relatedTags);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, tagId, Follow.FOLLOWING_TYPE_C_TAG);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        String sortModeStr = StringUtils.substringAfter(request.getRequestURI(), "/tag/" + tagURI);
        int sortMode;
        switch (sortModeStr) {
            case "":
                sortMode = 0;

                break;
            case "/hot":
                sortMode = 1;

                break;
            case "/good":
                sortMode = 2;

                break;
            case "/reply":
                sortMode = 3;

                break;
            case "/perfect":
                sortMode = 4;

                break;
            default:
                sortMode = 0;
        }

        final List<JSONObject> articles = articleQueryService.getArticlesByTag(avatarViewMode, sortMode, tag,
                pageNum, pageSize);
        dataModel.put(Article.ARTICLES, articles);

        final JSONObject tagCreator = tagQueryService.getCreator(avatarViewMode, tagId);

        tag.put(Tag.TAG_T_CREATOR_THUMBNAIL_URL, tagCreator.optString(Tag.TAG_T_CREATOR_THUMBNAIL_URL));
        tag.put(Tag.TAG_T_CREATOR_NAME, tagCreator.optString(Tag.TAG_T_CREATOR_NAME));
        tag.put(Tag.TAG_T_PARTICIPANTS, (Object) tagQueryService.getParticipants(
                avatarViewMode, tagId, Symphonys.getInt("tagParticipantsCnt")));

        final int tagRefCnt = tag.getInt(Tag.TAG_REFERENCE_CNT);
        final int pageCount = (int) Math.ceil(tagRefCnt / (double) pageSize);
        final int windowSize = Symphonys.getInt("tagArticlesWindowSize");
        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        dataModel.put(Common.CURRENT, StringUtils.substringAfter(URLDecoder.decode(request.getRequestURI(), "UTF-8"),
                "/tag/" + tagURI));
    }
}
