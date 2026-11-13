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

import org.b3log.latke.Latkes;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.Query;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.RevisionRepository;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Article revision authorization regression tests without database access.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 13, 2026
 * @since 3.6.4
 */
public class RevisionQueryServiceTestCase {

    private static final JSONObject ARTICLE = new JSONObject().
            put(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_DISCUSSION).
            put(Article.ARTICLE_STATUS, Article.ARTICLE_STATUS_C_VALID).
            put(Article.ARTICLE_AUTHOR_ID, "author").
            put(Article.ARTICLE_CONTENT, "secret");

    private Properties originalLocalProps;

    private Properties originalLatkeProps;

    @BeforeClass
    public void setUp() throws Exception {
        final Field localPropsField = Latkes.class.getDeclaredField("localProps");
        final Field latkePropsField = Latkes.class.getDeclaredField("latkeProps");
        localPropsField.setAccessible(true);
        latkePropsField.setAccessible(true);
        originalLocalProps = (Properties) localPropsField.get(null);
        originalLatkeProps = (Properties) latkePropsField.get(null);
        final Properties local = new Properties();
        local.setProperty("runtimeDatabase", "NONE");
        final Properties latke = new Properties();
        try (final InputStream input = getClass().getResourceAsStream("/latke.properties")) {
            latke.load(input);
        }
        Latkes.setLocalProps(local);
        Latkes.setLatkeProps(latke);
    }

    @AfterClass
    public void tearDown() {
        Latkes.setLocalProps(originalLocalProps);
        Latkes.setLatkeProps(originalLatkeProps);
    }

    @Test
    public void unauthorizedViewerDoesNotQueryRevisions() throws Exception {
        final AtomicBoolean queried = new AtomicBoolean();
        final RevisionQueryService service = service(false);
        inject(service, "articleRepository", new ArticleRepository() {
            @Override
            public JSONObject get(final String id) {
                return ARTICLE;
            }
        });
        inject(service, "revisionRepository", new RevisionRepository() {
            @Override
            public List<JSONObject> getList(final Query query) {
                queried.set(true);
                return List.of();
            }
        });

        final List<JSONObject> revisions = service.getArticleRevisions(
                new JSONObject().put(User.USER_NAME, "unrelated"), "article");

        assertTrue(revisions.isEmpty());
        assertFalse(queried.get());
    }

    @Test
    public void authorizedViewerQueriesRevisions() throws Exception {
        final AtomicBoolean queried = new AtomicBoolean();
        final RevisionQueryService service = service(true);
        inject(service, "articleRepository", new ArticleRepository() {
            @Override
            public JSONObject get(final String id) {
                return ARTICLE;
            }
        });
        inject(service, "revisionRepository", new RevisionRepository() {
            @Override
            public List<JSONObject> getList(final Query query) {
                queried.set(true);
                return List.of();
            }
        });

        final List<JSONObject> revisions = service.getArticleRevisions(
                new JSONObject().put(User.USER_NAME, "author"), "article");

        assertTrue(revisions.isEmpty());
        assertTrue(queried.get());
    }

    private static RevisionQueryService service(final boolean viewable) {
        return new RevisionQueryService() {
            @Override
            ArticleQueryService getArticleQueryService() {
                return new ArticleQueryService() {
                    @Override
                    public boolean canViewArticle(final JSONObject article, final JSONObject viewer) {
                        return viewable;
                    }
                };
            }
        };
    }

    private static void inject(final Object target, final String name, final Object value) throws Exception {
        final Field field = RevisionQueryService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
