/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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
import org.b3log.latke.http.Dispatcher;
import org.b3log.latke.http.Request;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.URLs;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.middleware.AnonymousViewCheckMidware;
import org.b3log.symphony.processor.middleware.LoginCheckMidware;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.StatusCodes;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tag processor.
 * <ul>
 * <li>Shows the tags wall (/tags), GET</li>
 * <li>Show tag articles (/tag/{tagURI}), GET</li>
 * <li>Query tags (/tags/query), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 3.0.0.0, Feb 11, 2020
 * @since 0.2.0
 */
@Singleton
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
     * Register request handlers.
     */
    public static void register() {
        final BeanManager beanManager = BeanManager.getInstance();
        final LoginCheckMidware loginCheck = beanManager.getReference(LoginCheckMidware.class);
        final AnonymousViewCheckMidware anonymousViewCheckMidware = beanManager.getReference(AnonymousViewCheckMidware.class);

        final TagProcessor tagProcessor = beanManager.getReference(TagProcessor.class);
        Dispatcher.get("/tags/query", tagProcessor::queryTags, loginCheck::handle);
        Dispatcher.get("/tags", tagProcessor::showTagsWall, anonymousViewCheckMidware::handle);
        Dispatcher.group().middlewares(anonymousViewCheckMidware::handle).router().get().uris(new String[]{"/tag/{tagURI}", "/tag/{tagURI}/hot", "/tag/{tagURI}/good", "/tag/{tagURI}/reply", "/tag/{tagURI}/perfect"}).handler(tagProcessor::showTagArticles);
    }

    /**
     * Queries tags.
     *
     * @param context the specified context
     */
    public void queryTags(final RequestContext context) {
        if (!Sessions.isLoggedIn()) {
            context.setStatus(403);
            return;
        }

        context.renderJSON(StatusCodes.SUCC);

        final String titlePrefix = context.param("title");

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
     * @param context the specified context
     */
    public void showTagsWall(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "tags.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final List<JSONObject> trendTags = tagQueryService.getTrendTags(Symphonys.TAGS_CNT);
        final List<JSONObject> coldTags = tagQueryService.getColdTags(Symphonys.TAGS_CNT);

        dataModel.put(Common.TREND_TAGS, trendTags);
        dataModel.put(Common.COLD_TAGS, coldTags);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Show tag articles.
     *
     * @param context the specified context
     */
    public void showTagArticles(final RequestContext context) {
        final String tagURI = context.pathVar("tagURI");
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "tag-articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(context, dataModel);
        final int pageNum = Paginator.getPage(request);
        int pageSize = Symphonys.ARTICLE_LIST_CNT;

        final JSONObject user = Sessions.getUser();
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);

            if (!UserExt.finshedGuide(user)) {
                context.sendRedirect(Latkes.getServePath() + "/guide");
                return;
            }
        }

        final JSONObject tag = tagQueryService.getTagByURI(tagURI);
        if (null == tag) {
            context.sendError(404);
            return;
        }
        tag.put(Common.IS_RESERVED, tagQueryService.isReservedTag(tag.optString(Tag.TAG_TITLE)));
        dataModel.put(Tag.TAG, tag);
        final String tagId = tag.optString(Keys.OBJECT_ID);
        final List<JSONObject> relatedTags = tagQueryService.getRelatedTags(tagId, Symphonys.TAG_RELATED_TAGS_CNT);
        tag.put(Tag.TAG_T_RELATED_TAGS, (Object) relatedTags);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = Sessions.getUser();
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, tagId, Follow.FOLLOWING_TYPE_C_TAG);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        String sortModeStr = StringUtils.substringAfter(context.requestURI(), "/tag/" + tagURI);
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

        final List<JSONObject> articles = articleQueryService.getArticlesByTag(sortMode, tag, pageNum, pageSize);
        dataModel.put(Article.ARTICLES, articles);
        final JSONObject tagCreator = tagQueryService.getCreator(tagId);
        tag.put(Tag.TAG_T_CREATOR_THUMBNAIL_URL, tagCreator.optString(Tag.TAG_T_CREATOR_THUMBNAIL_URL));
        tag.put(Tag.TAG_T_CREATOR_NAME, tagCreator.optString(Tag.TAG_T_CREATOR_NAME));
        tag.put(Tag.TAG_T_PARTICIPANTS, (Object) tagQueryService.getParticipants(tagId, Symphonys.ARTICLE_LIST_PARTICIPANTS_CNT));

        final int tagRefCnt = tag.getInt(Tag.TAG_REFERENCE_CNT);
        final int pageCount = (int) Math.ceil(tagRefCnt / (double) pageSize);
        final int windowSize = Symphonys.ARTICLE_LIST_WIN_SIZE;
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

        dataModel.put(Common.CURRENT, StringUtils.substringAfter(URLs.decode(context.requestURI()),
                "/tag/" + tagURI));
    }
}
