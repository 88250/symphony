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
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.AnonymousViewCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.DomainQueryService;
import org.b3log.symphony.util.StatusCodes;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * Article API v2.
 * <p>
 * <ul>
 * <li>Gets latest articles (/api/v2/articles/latest), GET</li>
 * <li>Gets domain articles (/api/v2/articles/domain/{domainURI}), GET</li>
 * <li>Gets tag articles (/api/v2/articles/tag), GET</li>
 * <li>Gets an article (/api/v2/article), GET</li>
 * <li>Adds an article (/api/v2/article), POST</li>
 * <li>Updates an article (/api/v2/article), PUT</li>
 * <li>Adds a comment (/api/v2/comment), POST</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 4, 2016
 * @since 2.0.0
 */
@RequestProcessor
public class ArticleAPI2 {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleAPI2.class);
    /**
     * Pagination page size.
     */
    private static final int PAGE_SIZE = 20;
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
     * Gets domain articles.
     *
     * @param context the specified context
     * @param request the specified request
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
            cleanDomain(domain);

            final String domainId = domain.optString(Keys.OBJECT_ID);
            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

            final JSONObject result = articleQueryService.getDomainArticles(avatarViewMode, domainId, page, PAGE_SIZE);
            final List<JSONObject> articles = (List<JSONObject>) result.opt(Article.ARTICLES);
            data.put(Article.ARTICLES, articles);
            cleanArticles(articles);

            data.put(Pagination.PAGINATION, result.optJSONObject(Pagination.PAGINATION));

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets domain [uri=" + domainURI + "] articles failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);

        context.renderJSON(ret);
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

            data = articleQueryService.getRecentArticles(avatarViewMode, sortMode, page, PAGE_SIZE);
            final List<JSONObject> articles = (List<JSONObject>) data.opt(Article.ARTICLES);
            cleanArticles(articles);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets latest articles failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);

        context.renderJSON(ret);
    }

    private void cleanArticles(final List<JSONObject> articles) {
        for (final JSONObject article : articles) {
            cleanArticle(article);

            article.remove(Article.ARTICLE_CONTENT);
            article.remove(Article.ARTICLE_REWARD_POINT);
            article.remove(Article.ARTICLE_COMMENTABLE);
            article.remove(Article.ARTICLE_ANONYMOUS_VIEW);
            article.remove(Article.ARTICLE_REWARD_CONTENT);
        }
    }

    private void cleanArticle(final JSONObject article) {
        article.put(Article.ARTICLE_CREATE_TIME, ((Date) article.opt(Article.ARTICLE_CREATE_TIME)).getTime());
        article.put(Article.ARTICLE_UPDATE_TIME, ((Date) article.opt(Article.ARTICLE_UPDATE_TIME)).getTime());
        article.put(Article.ARTICLE_LATEST_CMT_TIME, ((Date) article.opt(Article.ARTICLE_LATEST_CMT_TIME)).getTime());

        article.remove(Article.ARTICLE_T_LATEST_CMT);
        article.remove(Article.ARTICLE_LATEST_CMT_TIME);
        article.remove(Article.ARTICLE_LATEST_CMTER_NAME);
        article.remove(Article.ARTICLE_SYNC_TO_CLIENT);
        article.remove(Article.ARTICLE_ANONYMOUS);
        article.remove(Article.ARTICLE_STATUS);
        article.remove(Article.ARTICLE_T_PARTICIPANTS);
        article.remove(Article.REDDIT_SCORE);
        article.remove(Article.ARTICLE_CLIENT_ARTICLE_ID);
        article.remove(Article.ARTICLE_CITY);
        article.remove(Article.ARTICLE_IP);
        article.remove(Article.ARTICLE_EDITOR_TYPE);
        article.remove(Article.ARTICLE_RANDOM_DOUBLE);
        article.remove(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK);
        article.remove(Article.ARTICLE_T_HEAT);

        final JSONObject author = article.optJSONObject(Article.ARTICLE_T_AUTHOR);
        cleanUser(author);
    }

    private void cleanDomain(final JSONObject domain) {
        final String uri = domain.optString(Domain.DOMAIN_URI);
        domain.put(Domain.DOMAIN_URI, Latkes.getServePath() + "/domain/" + uri);

        final List<JSONObject> tags = (List<JSONObject>) domain.opt(Domain.DOMAIN_T_TAGS);
        for (final JSONObject tag : tags) {
            cleanTag(tag);
        }

        domain.remove(Domain.DOMAIN_TYPE);
        domain.remove(Domain.DOMAIN_STATUS);
        domain.remove(Domain.DOMAIN_SEO_DESC);
        domain.remove(Domain.DOMAIN_SEO_KEYWORDS);
        domain.remove(Domain.DOMAIN_SEO_TITLE);
        domain.remove(Domain.DOMAIN_CSS);
        domain.remove(Domain.DOMAIN_SORT);
    }

    private void cleanTag(final JSONObject tag) {
        final String iconPath = tag.optString(Tag.TAG_ICON_PATH);
        if (StringUtils.isBlank(iconPath)) {
            tag.put(Tag.TAG_ICON_PATH, "");
        } else {
            tag.put(Tag.TAG_ICON_PATH, Latkes.getStaticServePath() + "/images/tags/" + iconPath);
        }

        final String uri = tag.optString(Tag.TAG_URI);
        tag.put(Tag.TAG_URI, Latkes.getServePath() + "/tag/" + uri);

        Tag.fillDescription(tag);

        tag.remove(Tag.TAG_STATUS);
        tag.remove(Tag.TAG_RANDOM_DOUBLE);
        tag.remove(Tag.TAG_CSS);
        tag.remove(Tag.TAG_SEO_DESC);
        tag.remove(Tag.TAG_SEO_TITLE);
        tag.remove(Tag.TAG_SEO_KEYWORDS);
        tag.remove(Tag.TAG_T_DESCRIPTION_TEXT);
    }

    private void cleanUser(final JSONObject user) {
        user.remove(UserExt.USER_QQ);
        user.remove(UserExt.USER_B3_KEY);
        user.remove(UserExt.USER_POINT_STATUS);
        user.remove(UserExt.USER_LATEST_LOGIN_IP);
        user.remove(UserExt.USER_FOLLOWER_STATUS);
        user.remove(UserExt.USER_GUIDE_STEP);
        user.remove(UserExt.USER_ONLINE_STATUS);
        user.remove(UserExt.USER_CURRENT_CHECKIN_STREAK_START);
        user.remove(UserExt.USER_COMMENT_STATUS);
        user.remove(UserExt.USER_UA_STATUS);
        user.remove(UserExt.USER_LATEST_ARTICLE_TIME);
        user.remove(UserExt.USER_FORGE_LINK_STATUS);
        user.remove(UserExt.USER_AVATAR_TYPE);
        user.remove(UserExt.USER_SUB_MAIL_SEND_TIME);
        user.remove(UserExt.USER_UPDATE_TIME);
        user.remove(UserExt.USER_SUB_MAIL_STATUS);
        user.remove(UserExt.USER_JOIN_POINT_RANK);
        user.remove(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL);
        user.remove(UserExt.USER_LATEST_LOGIN_TIME);
        user.remove(User.USER_PASSWORD);
        user.remove(UserExt.USER_AVATAR_VIEW_MODE);
        user.remove(UserExt.USER_LONGEST_CHECKIN_STREAK_END);
        user.remove(UserExt.USER_WATCHING_ARTICLE_STATUS);
        user.remove(UserExt.USER_LATEST_CMT_TIME);
        user.remove(UserExt.USER_FOLLOWING_TAG_STATUS);
        user.remove(UserExt.USER_TIMELINE_STATUS);
        user.remove(UserExt.USER_B3_CLIENT_UPDATE_ARTICLE_URL);
        user.remove(UserExt.USER_JOIN_USED_POINT_RANK);
        user.remove(UserExt.USER_CURRENT_CHECKIN_STREAK_END);
        user.remove(UserExt.USER_FOLLOWING_ARTICLE_STATUS);
        user.remove(UserExt.USER_KEYBOARD_SHORTCUTS_STATUS);
        user.remove(User.USER_EMAIL);
        user.remove(UserExt.USER_ARTICLE_STATUS);
        user.remove(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL);
        user.remove(UserExt.USER_GEO_STATUS);
        user.remove(UserExt.USER_LONGEST_CHECKIN_STREAK_START);
        user.remove(UserExt.USER_NOTIFY_STATUS);
        user.remove(UserExt.USER_FOLLOWING_USER_STATUS);
        user.remove(UserExt.SYNC_TO_CLIENT);
        user.remove(UserExt.USER_ONLINE_FLAG);
        user.remove(UserExt.USER_TIMEZONE);
        user.remove(UserExt.USER_LIST_PAGE_SIZE);
        user.remove(UserExt.USER_MOBILE_SKIN);
        user.remove(UserExt.USER_SKIN);
        user.remove(UserExt.USER_STATUS);
        user.remove(UserExt.USER_COUNTRY);
        user.remove(UserExt.USER_PROVINCE);
        user.remove(UserExt.USER_CITY);
        user.remove(UserExt.USER_COMMENT_VIEW_MODE);
    }
}
