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
package org.b3log.symphony.service;

import org.apache.commons.lang3.StringUtils;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.model.User;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Role;
import org.b3log.symphony.model.UserExt;
import org.json.JSONObject;

import java.util.Set;

/**
 * Article visibility service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 13, 2026
 * @since 3.6.4
 */
@Service
public class ArticleVisibilityService {

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Checks whether the specified viewer can view the specified article.
     *
     * @param article the specified article
     * @param viewer  the specified viewer, may be {@code null}
     * @return {@code true} if the viewer can view it, returns {@code false} otherwise
     */
    public boolean canViewArticle(final JSONObject article, final JSONObject viewer) {
        if (null == article) {
            return false;
        }

        if (Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)) {
            return false;
        }

        final JSONObject author = userQueryService.getUser(article.optString(Article.ARTICLE_AUTHOR_ID));
        if (null != author && UserExt.USER_STATUS_C_INVALID == author.optInt(UserExt.USER_STATUS)) {
            return false;
        }

        return canViewDiscussion(article, viewer);
    }

    /**
     * Checks whether the specified viewer can view the specified discussion article.
     *
     * @param article the specified article
     * @param viewer  the specified viewer, may be {@code null}
     * @return {@code true} if the viewer can view it, returns {@code false} otherwise
     */
    public boolean canViewDiscussion(final JSONObject article, final JSONObject viewer) {
        if (Article.ARTICLE_TYPE_C_DISCUSSION != article.optInt(Article.ARTICLE_TYPE)) {
            return true;
        }

        final String viewerName = null == viewer ? "" : viewer.optString(User.USER_NAME);
        final String viewerRole = null == viewer ? "" : viewer.optString(User.USER_ROLE);
        if (Role.ROLE_ID_C_ADMIN.equals(viewerRole)) {
            return true;
        }
        if (StringUtils.isBlank(viewerName)) {
            return false;
        }

        final String authorName = getAuthorName(article);
        if (StringUtils.isNotBlank(authorName) && authorName.equals(viewerName)) {
            return true;
        }

        final Set<String> userNames = userQueryService.getUserNames(article.optString(Article.ARTICLE_CONTENT));
        for (final String userName : userNames) {
            if (userName.equals(viewerName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the author name of the specified article.
     *
     * @param article the specified article
     * @return author name, returns an empty string if not found
     */
    private String getAuthorName(final JSONObject article) {
        String ret = article.optString(Article.ARTICLE_T_AUTHOR_NAME);
        if (StringUtils.isBlank(ret)) {
            final JSONObject author = userQueryService.getUser(article.optString(Article.ARTICLE_AUTHOR_ID));
            ret = null == author ? "" : author.optString(User.USER_NAME);
        }
        return ret;
    }
}
