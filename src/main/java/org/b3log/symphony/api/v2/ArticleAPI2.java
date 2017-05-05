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
package org.b3log.symphony.api.v2;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.AnonymousViewCheck;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.PermissionCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.ArticleAddValidation;
import org.b3log.symphony.processor.advice.validate.ArticleUpdateValidation;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.StatusCodes;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Article API v2.
 * <p>
 * <ul>
 * <li>Gets latest articles (/api/v2/articles/latest), GET</li>
 * <li>Gets domain articles (/api/v2/articles/domain/{domainURI}), GET</li>
 * <li>Gets tag articles (/api/v2/articles/tag/{tagURI}), GET</li>
 * <li>Gets an article (/api/v2/article/{articleId}), GET</li>
 * <li>Adds an article (/api/v2/article), POST</li>
 * <li>Updates an article (/api/v2/article/{articleId}), PUT</li>
 * <li>Adds a comment (/api/v2/article/comment), POST</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.0.1, Mar 10, 2016
 * @since 2.0.0
 */
@RequestProcessor
public class ArticleAPI2 {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleAPI2.class);
    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;
    /**
     * Domain query service.
     */
    @Inject
    private DomainQueryService domainQueryService;
    /**
     * Tag query service.
     */
    @Inject
    private TagQueryService tagQueryService;
    /**
     * Article mangement service.
     */
    @Inject
    private ArticleMgmtService articleMgmtService;
    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Updates an article.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param id       the specified article id
     */
    @RequestProcessing(value = "/api/v2/article/{id}", method = HTTPRequestMethod.PUT)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, ArticleUpdateValidation.class, PermissionCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                              final String id) {
        try {
            if (Strings.isEmptyOrNull(id)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);

                return;
            }

            context.renderJSON();

            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

            final JSONObject oldArticle = articleQueryService.getArticleById(avatarViewMode, id);
            if (null == oldArticle) {
                context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);
                context.renderMsg("Article not found");

                return;
            }

            final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

            final String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
            String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
            final String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
            final boolean articleCommentable = true;
            final int articleType = requestJSONObject.optInt(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL);
            final String articleRewardContent = requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT);
            final int articleRewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT);
            final String ip = Requests.getRemoteAddr(request);
            final String ua = request.getHeader(Common.USER_AGENT);

            final JSONObject article = new JSONObject();
            article.put(Keys.OBJECT_ID, id);
            article.put(Article.ARTICLE_TITLE, articleTitle);
            article.put(Article.ARTICLE_CONTENT, articleContent);
            article.put(Article.ARTICLE_EDITOR_TYPE, 0);
            article.put(Article.ARTICLE_COMMENTABLE, articleCommentable);
            article.put(Article.ARTICLE_TYPE, articleType);
            article.put(Article.ARTICLE_REWARD_CONTENT, articleRewardContent);
            article.put(Article.ARTICLE_REWARD_POINT, articleRewardPoint);
            article.put(Article.ARTICLE_IP, "");
            if (StringUtils.isNotBlank(ip)) {
                article.put(Article.ARTICLE_IP, ip);
            }
            article.put(Article.ARTICLE_UA, "");
            if (StringUtils.isNotBlank(ua)) {
                article.put(Article.ARTICLE_UA, ua);
            }

            final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
            if (null == currentUser
                    || !currentUser.optString(Keys.OBJECT_ID).equals(oldArticle.optString(Article.ARTICLE_AUTHOR_ID))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);

                return;
            }

            article.put(Article.ARTICLE_AUTHOR_ID, currentUser.optString(Keys.OBJECT_ID));

            if (!Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
                articleTags = articleMgmtService.filterReservedTags(articleTags);
            }

            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType && StringUtils.isBlank(articleTags)) {
                articleTags = "小黑屋";
            }

            if (Article.ARTICLE_TYPE_C_THOUGHT == articleType && StringUtils.isBlank(articleTags)) {
                articleTags = "思绪";
            }

            article.put(Article.ARTICLE_TAGS, articleTags);

            articleMgmtService.updateArticle(article);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = e.getMessage();
            LOGGER.log(Level.ERROR, "Updates article[id=" + id + "] failed: {0}", e.getMessage());

            context.renderMsg(msg);
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Adds an article.
     *
     * @param context the specified context
     * @param request the specified request
     */
    @RequestProcessing(value = "/api/v2/article", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, ArticleAddValidation.class, PermissionCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addArticle(final HTTPRequestContext context, final HttpServletRequest request) {
        context.renderJSON();

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
        final String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        final boolean articleCommentable = true;
        final int articleType = Article.ARTICLE_TYPE_C_NORMAL;
        final String articleRewardContent = requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT);
        final int articleRewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT);
        final String ip = Requests.getRemoteAddr(request);
        final String ua = request.getHeader(Common.USER_AGENT);
        final boolean isAnonymous = requestJSONObject.optBoolean(Article.ARTICLE_ANONYMOUS, false);
        final int articleAnonymous = isAnonymous
                ? Article.ARTICLE_ANONYMOUS_C_ANONYMOUS : Article.ARTICLE_ANONYMOUS_C_PUBLIC;
        final boolean syncWithSymphonyClient = requestJSONObject.optBoolean(Article.ARTICLE_SYNC_TO_CLIENT, false);

        final JSONObject article = new JSONObject();
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_EDITOR_TYPE, 0);
        article.put(Article.ARTICLE_COMMENTABLE, articleCommentable);
        article.put(Article.ARTICLE_TYPE, articleType);
        article.put(Article.ARTICLE_REWARD_CONTENT, articleRewardContent);
        article.put(Article.ARTICLE_REWARD_POINT, articleRewardPoint);
        article.put(Article.ARTICLE_IP, "");
        if (StringUtils.isNotBlank(ip)) {
            article.put(Article.ARTICLE_IP, ip);
        }
        article.put(Article.ARTICLE_UA, "");
        if (StringUtils.isNotBlank(ua)) {
            article.put(Article.ARTICLE_UA, ua);
        }
        article.put(Article.ARTICLE_ANONYMOUS, articleAnonymous);
        article.put(Article.ARTICLE_SYNC_TO_CLIENT, syncWithSymphonyClient);

        try {
            final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);

            article.put(Article.ARTICLE_AUTHOR_ID, currentUser.optString(Keys.OBJECT_ID));

            if (!Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
                articleTags = articleMgmtService.filterReservedTags(articleTags);
            }

            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType && StringUtils.isBlank(articleTags)) {
                articleTags = "小黑屋";
            }

            if (Article.ARTICLE_TYPE_C_THOUGHT == articleType && StringUtils.isBlank(articleTags)) {
                articleTags = "思绪";
            }

            article.put(Article.ARTICLE_TAGS, articleTags);
            article.put(Article.ARTICLE_T_IS_BROADCAST, false);

            articleMgmtService.addArticle(article);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final ServiceException e) {
            final String msg = e.getMessage();
            LOGGER.log(Level.ERROR, "Adds article[title=" + articleTitle + "] failed: {0}", e.getMessage());

            context.renderMsg(msg);
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Gets an article.
     *
     * @param context   the specified context
     * @param request   the specified request
     * @param articleId the specified article id
     */
    @RequestProcessing(value = {"/api/v2/article/{articleId}"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getArticle(final HTTPRequestContext context, final HttpServletRequest request, final String articleId) {
        int page = 1;
        final String p = request.getParameter("p");
        if (Strings.isNumeric(p)) {
            page = Integer.parseInt(p);
        }

        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        JSONObject data = null;
        try {
            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
            final JSONObject article = articleQueryService.getArticleById(avatarViewMode, articleId);
            if (null == article) {
                ret.put(Keys.MSG, "Article not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            data = new JSONObject();
            data.put(Article.ARTICLE, article);

            final List<JSONObject> articleComments = commentQueryService.getArticleComments(
                    avatarViewMode, articleId, page, V2s.PAGE_SIZE, UserExt.USER_COMMENT_VIEW_MODE_C_TRADITIONAL);
            article.put(Article.ARTICLE_T_COMMENTS, (Object) articleComments);

            V2s.cleanArticle(article);

            final int commentCnt = article.getInt(Article.ARTICLE_COMMENT_CNT);
            final int pageCount = (int) Math.ceil((double) commentCnt / (double) V2s.PAGE_SIZE);

            final JSONObject pagination = new JSONObject();
            final List<Integer> pageNums = Paginator.paginate(page, V2s.PAGE_SIZE, pageCount, V2s.WINDOW_SIZE);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

            data.put(Pagination.PAGINATION, pagination);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets article failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }


    /**
     * Gets tag articles.
     *
     * @param context the specified context
     * @param request the specified request
     * @param tagURI  the specified tag URI
     */
    @RequestProcessing(value = {"/api/v2/articles/tag/{tagURI}", "/api/v2/articles/tag/{tagURI}/hot",
            "/api/v2/articles/tag/{tagURI}/good", "/api/v2/articles/tag/{tagURI}/reply",
            "/api/v2/articles/tag/{tagURI}/perfect"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getTagArticles(final HTTPRequestContext context, final HttpServletRequest request,
                               final String tagURI) {
        int page = 1;
        final String p = request.getParameter("p");
        if (Strings.isNumeric(p)) {
            page = Integer.parseInt(p);
        }

        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        JSONObject data = null;
        try {
            final JSONObject tag = tagQueryService.getTagByURI(tagURI);
            if (null == tag) {
                ret.put(Keys.MSG, "Tag not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            data = new JSONObject();

            data.put(Tag.TAG, tag);
            V2s.cleanTag(tag);

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

            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
            final List<JSONObject> articles = articleQueryService.getArticlesByTag(avatarViewMode, sortMode, tag,
                    page, V2s.PAGE_SIZE);
            data.put(Article.ARTICLES, articles);
            V2s.cleanArticles(articles);

            final int tagRefCnt = tag.getInt(Tag.TAG_REFERENCE_CNT);
            final int pageCount = (int) Math.ceil(tagRefCnt / (double) V2s.PAGE_SIZE);

            final JSONObject pagination = new JSONObject();
            final List<Integer> pageNums = Paginator.paginate(page, V2s.PAGE_SIZE, pageCount, V2s.WINDOW_SIZE);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

            data.put(Pagination.PAGINATION, pagination);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets tag [uri=" + tagURI + "] articles failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }

    /**
     * Gets domain articles.
     *
     * @param context   the specified context
     * @param request   the specified request
     * @param domainURI the specified domain URI
     */
    @RequestProcessing(value = {"/api/v2/articles/domain/{domainURI}"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getDomainArticles(final HTTPRequestContext context, final HttpServletRequest request,
                                  final String domainURI) {
        int page = 1;
        final String p = request.getParameter("p");
        if (Strings.isNumeric(p)) {
            page = Integer.parseInt(p);
        }

        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        JSONObject data = null;
        try {
            final JSONObject domain = domainQueryService.getByURI(domainURI);
            if (null == domain) {
                ret.put(Keys.MSG, "Domain not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            data = new JSONObject();

            final List<JSONObject> tags = domainQueryService.getTags(domain.optString(Keys.OBJECT_ID));
            domain.put(Domain.DOMAIN_T_TAGS, (Object) tags);

            data.put(Domain.DOMAIN, domain);
            V2s.cleanDomain(domain);

            final String domainId = domain.optString(Keys.OBJECT_ID);
            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

            final JSONObject result = articleQueryService.getDomainArticles(avatarViewMode, domainId, page, V2s.PAGE_SIZE);
            final List<JSONObject> articles = (List<JSONObject>) result.opt(Article.ARTICLES);
            data.put(Article.ARTICLES, articles);
            V2s.cleanArticles(articles);

            data.put(Pagination.PAGINATION, result.optJSONObject(Pagination.PAGINATION));

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets domain [uri=" + domainURI + "] articles failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }

    /**
     * Gets latest articles.
     *
     * @param context the specified context
     * @param request the specified request
     */
    @RequestProcessing(value = {"/api/v2/articles/latest", "/api/v2/articles/latest/hot",
            "/api/v2/articles/latest/good", "/api/v2/articles/latest/reply"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getLatestArticles(final HTTPRequestContext context, final HttpServletRequest request) {
        int page = 1;
        final String p = request.getParameter("p");
        if (Strings.isNumeric(p)) {
            page = Integer.parseInt(p);
        }

        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        JSONObject data = null;
        try {
            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

            String sortModeStr = StringUtils.substringAfter(request.getRequestURI(), "/latest");
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
                default:
                    sortMode = 0;
            }

            data = articleQueryService.getRecentArticles(avatarViewMode, sortMode, page, V2s.PAGE_SIZE);
            final List<JSONObject> articles = (List<JSONObject>) data.opt(Article.ARTICLES);
            V2s.cleanArticles(articles);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets latest articles failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }
}
