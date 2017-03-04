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

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.ArticleQueryService;
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
 * <li>Gets domain articles (/api/v2/articles/domain), GET</li>
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
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Gets latest articles.
     *
     * @param context the specified context
     * @param request the specified request
     */
    @RequestProcessing(value = "/api/v2/articles/latest", method = HTTPRequestMethod.GET)
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

            data = articleQueryService.getRecentArticles(avatarViewMode, 0, page, 20);
            final List<JSONObject> articles = (List<JSONObject>) data.opt(Article.ARTICLES);
            for (final JSONObject article : articles) {
                cleanArticle(article);

                article.remove(Article.ARTICLE_CONTENT);
            }

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets latest articles failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);

        context.renderJSON(ret);
    }

    private void cleanArticle(final JSONObject article) {
        article.put(Article.ARTICLE_CREATE_TIME, ((Date)article.opt(Article.ARTICLE_CREATE_TIME)).getTime());
        article.put(Article.ARTICLE_UPDATE_TIME, ((Date)article.opt(Article.ARTICLE_UPDATE_TIME)).getTime());
        article.put(Article.ARTICLE_LATEST_CMT_TIME, ((Date)article.opt(Article.ARTICLE_LATEST_CMT_TIME)).getTime());

        article.remove(Article.ARTICLE_T_LATEST_CMT);
        article.remove(Article.ARTICLE_LATEST_CMT_TIME);
        article.remove(Article.ARTICLE_LATEST_CMTER_NAME);
        article.remove(Article.ARTICLE_SYNC_TO_CLIENT);
        article.remove(Article.ARTICLE_ANONYMOUS);
        article.remove(Article.ARTICLE_STATUS);

        final JSONObject author = article.optJSONObject(Article.ARTICLE_T_AUTHOR);
        cleanUser(author);
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
