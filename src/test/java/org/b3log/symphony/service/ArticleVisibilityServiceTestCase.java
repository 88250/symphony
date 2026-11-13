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

import org.b3log.latke.model.User;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Role;
import org.b3log.symphony.model.UserExt;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Set;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Article visibility regression tests without database access.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 13, 2026
 * @since 3.6.4
 */
public class ArticleVisibilityServiceTestCase {

    private ArticleVisibilityService articleVisibilityService;

    private JSONObject author;

    private JSONObject invited;

    private JSONObject unrelated;

    private JSONObject admin;

    @BeforeClass
    public void setUp() throws Exception {
        articleVisibilityService = new ArticleVisibilityService();
        inject(articleVisibilityService, "userQueryService", new UserQueryService() {
            @Override
            public Set<String> getUserNames(final String text) {
                return text.contains("@invited") ? Set.of("invited") : Set.of();
            }

            @Override
            public JSONObject getUser(final String userId) {
                if ("author".equals(userId)) {
                    return new JSONObject().put(User.USER_NAME, "author");
                }
                if ("invalid".equals(userId)) {
                    return new JSONObject().put(User.USER_NAME, "invalid").
                            put(UserExt.USER_STATUS, UserExt.USER_STATUS_C_INVALID);
                }
                return null;
            }
        });

        author = new JSONObject().put(User.USER_NAME, "author");
        invited = new JSONObject().put(User.USER_NAME, "invited");
        unrelated = new JSONObject().put(User.USER_NAME, "unrelated");
        admin = new JSONObject().put(User.USER_NAME, "admin").
                put(User.USER_ROLE, Role.ROLE_ID_C_ADMIN);
    }

    @Test
    public void normalArticleIsViewableByUnrelatedViewer() {
        assertTrue(articleVisibilityService.canViewArticle(
                article(Article.ARTICLE_TYPE_C_NORMAL, Article.ARTICLE_STATUS_C_VALID, "author", "body"), unrelated));
    }

    @Test
    public void discussionIsViewableByAuthor() {
        assertTrue(articleVisibilityService.canViewArticle(
                article(Article.ARTICLE_TYPE_C_DISCUSSION, Article.ARTICLE_STATUS_C_VALID, "author", "secret"), author));
    }

    @Test
    public void discussionIsViewableByInvitedUser() {
        assertTrue(articleVisibilityService.canViewArticle(
                article(Article.ARTICLE_TYPE_C_DISCUSSION, Article.ARTICLE_STATUS_C_VALID, "author", "@invited secret"), invited));
    }

    @Test
    public void discussionIsViewableByAdmin() {
        assertTrue(articleVisibilityService.canViewArticle(
                article(Article.ARTICLE_TYPE_C_DISCUSSION, Article.ARTICLE_STATUS_C_VALID, "author", "secret"), admin));
    }

    @Test
    public void discussionIsHiddenFromUnrelatedViewer() {
        assertFalse(articleVisibilityService.canViewArticle(
                article(Article.ARTICLE_TYPE_C_DISCUSSION, Article.ARTICLE_STATUS_C_VALID, "author", "secret"), unrelated));
    }

    @Test
    public void discussionIsHiddenFromAnonymousViewer() {
        assertFalse(articleVisibilityService.canViewArticle(
                article(Article.ARTICLE_TYPE_C_DISCUSSION, Article.ARTICLE_STATUS_C_VALID, "author", "secret"), null));
    }

    @Test
    public void blockedArticleIsHidden() {
        assertFalse(articleVisibilityService.canViewArticle(
                article(Article.ARTICLE_TYPE_C_NORMAL, Article.ARTICLE_STATUS_C_INVALID, "author", "blocked"), author));
    }

    @Test
    public void invalidAuthorHidesArticle() {
        assertFalse(articleVisibilityService.canViewArticle(
                article(Article.ARTICLE_TYPE_C_NORMAL, Article.ARTICLE_STATUS_C_VALID, "invalid", "body"), unrelated));
    }

    private static JSONObject article(final int type, final int status, final String authorId, final String content) {
        return new JSONObject().put(Article.ARTICLE_TYPE, type).
                put(Article.ARTICLE_STATUS, status).
                put(Article.ARTICLE_AUTHOR_ID, authorId).
                put(Article.ARTICLE_CONTENT, content);
    }

    private static void inject(final Object target, final String name, final Object value) throws Exception {
        final Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
