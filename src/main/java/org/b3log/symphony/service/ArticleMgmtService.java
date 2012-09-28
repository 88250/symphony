/*
 * Copyright (c) 2012, B3log Team
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
package org.b3log.symphony.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.repository.ArticleRepository;
import org.json.JSONObject;

/**
 * Article management service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Sep 28, 2012
 * @since 0.2.0
 */
public final class ArticleMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleMgmtService.class.getName());
    /**
     * Singleton.
     */
    private static final ArticleMgmtService SINGLETON = new ArticleMgmtService();
    /**
     * Article repository.
     */
    private ArticleRepository articleRepository = ArticleRepository.getInstance();
    /**
     * Language service.
     */
    private LangPropsService langPropsService = LangPropsService.getInstance();

    /**
     * Adds an article with the specified request json object.
     * 
     * @param requestJSONObject the specified request json object, for example,
     * <pre>
     * {
     *     "articleTitle": "",
     *     "articleTags": "",
     *     "articleContent": "",
     *     "articleEditorType": "",
     *     "articleAuthorEmail": ""
     * }
     * </pre>,see {@link Article} for more details
     * @return generated article id
     * @throws ServiceException service exception
     */
    public String addArticle(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final JSONObject article = new JSONObject();

            article.put(Article.ARTICLE_TITLE, requestJSONObject.optString(Article.ARTICLE_TITLE));
            article.put(Article.ARTICLE_TAGS, requestJSONObject.optString(Article.ARTICLE_TAGS));
            article.put(Article.ARTICLE_CONTENT, requestJSONObject.optString(Article.ARTICLE_CONTENT));
            article.put(Article.ARTICLE_EDITOR_TYPE, requestJSONObject.optString(Article.ARTICLE_EDITOR_TYPE));
            article.put(Article.ARTICLE_AUTHOR_EMAIL, requestJSONObject.optString(Article.ARTICLE_AUTHOR_EMAIL));

            final long currentTimeMillis = System.currentTimeMillis();

            article.put(Article.ARTICLE_COMMENT_CNT, 0);
            article.put(Article.ARTICLE_VIEW_CNT, 0);
            article.put(Article.ARTICLE_COMMENTABLE, true);
            article.put(Article.ARTICLE_CREATE_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_PERMALINK, "TODO permalink");
            article.put(Article.ARTICLE_RANDOM_DOUBLE, Math.random());
            article.put(Article.ARTICLE_STATUS, 0);
            article.put(Article.ARTICLE_UPDATE_TIME, 0);
            article.put(Article.ARTICLE_UPDATE_TIME, currentTimeMillis);

            articleRepository.add(article);

            transaction.commit();

            return article.optString(Keys.OBJECT_ID);
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.SEVERE, "Adds a user failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the {@link UserMgmtService} singleton.
     *
     * @return the singleton
     */
    public static ArticleMgmtService getInstance() {
        return SINGLETON;
    }

    /**
     * Private constructor.
     */
    private ArticleMgmtService() {
    }
}
