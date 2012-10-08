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

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.MD5;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Article query service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Oct 2, 2012
 * @since 0.2.0
 */
public final class ArticleQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleQueryService.class.getName());
    /**
     * Singleton.
     */
    private static final ArticleQueryService SINGLETON = new ArticleQueryService();
    /**
     * Article repository.
     */
    private ArticleRepository articleRepository = ArticleRepository.getInstance();
    /**
     * Comment query service.
     */
    private CommentQueryService commentQueryService = CommentQueryService.getInstance();

    /**
     * Gets an article by the specified id.
     * 
     * @param articleId the specified id
     * @return article, return {@code null} if not found
     * @throws ServiceException service exception 
     */
    public JSONObject getArticleById(final String articleId) throws ServiceException {
        try {
            final JSONObject ret = articleRepository.get(articleId);

            if (null == ret) {
                return null;
            }

            organizeArticle(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets article [articleId=" + articleId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the user articles with the specified user id, page number and page size.
     * 
     * @param userId the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return user articles, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getUserArticles(final String userId, final int currentPageNum, final int pageSize) throws ServiceException {
        final Query query = new Query().addSort(Article.ARTICLE_CREATE_TIME, SortDirection.DESCENDING)
                .setPageCount(currentPageNum).setPageSize(pageSize);
        try {
            final JSONObject result = articleRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            organizeArticles(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets user articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the random articles with the specified fetch size.
     * 
     * @param fetchSize the specified fetch size
     * @return recent articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getRandomArticles(final int fetchSize) throws ServiceException {
        try {
            final List<JSONObject> ret = articleRepository.getRandomly(fetchSize);
            organizeArticles(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets random articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the recent (sort by create time) articles with the specified fetch size.
     * 
     * @param fetchSize the specified fetch size
     * @return recent articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getRecentArticles(final int fetchSize) throws ServiceException {
        final Query query = new Query().addSort(Article.ARTICLE_CREATE_TIME, SortDirection.DESCENDING)
                .setPageCount(1).setPageSize(fetchSize);

        try {
            final JSONObject result = articleRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            organizeArticles(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets recent articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the latest comment articles with the specified fetch size.
     * 
     * @param fetchSize the specified fetch size
     * @return recent articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getLatestCmtArticles(final int fetchSize) throws ServiceException {
        final Query query = new Query().addSort(Article.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING)
                .setPageCount(1).setPageSize(fetchSize);

        try {
            final JSONObject result = articleRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            organizeArticles(ret);
            
            // Gets participants
            final Integer participantsCnt = Symphonys.getInt("latestCmtArticleParticipantsCnt");
            for (final JSONObject article : ret) {
                final String participantName = "";
                final String participantThumbnailURL = "";

                final List<JSONObject> articleParticipants =
                        commentQueryService.getArticleLatestParticipants(article.optString(Keys.OBJECT_ID), participantsCnt);
                article.put(Article.ARTICLE_T_PARTICIPANTS, articleParticipants);

                article.put(Article.ARTICLE_T_PARTICIPANT_NAME, participantName);
                article.put(Article.ARTICLE_T_PARTICIPANT_THUMBNAIL_URL, participantThumbnailURL);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets latest comment articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Organizes the specified articles.
     * 
     * <ul>
     *   <li>converts create/update/latest comment time (long) to date type</li>
     *   <li>generates author thumbnail URL</li>
     * </ul>
     * 
     * @param articles the specified articles
     */
    private static void organizeArticles(final List<JSONObject> articles) {
        for (final JSONObject article : articles) {
            organizeArticle(article);
        }
    }

    /**
     * Organizes the specified article.
     * 
     * <ul>
     *   <li>converts create/update/latest comment time (long) to date type</li>
     *   <li>generates author thumbnail URL</li>
     * </ul>
     * 
     * @param article the specified article
     */
    private static void organizeArticle(final JSONObject article) {
        toArticleDate(article);
        genArticleAuthorThumbnailURL(article);
    }

    /**
     * Converts the specified article create/update/latest comment time (long) to date type.
     * 
     * @param article the specified article
     */
    private static void toArticleDate(final JSONObject article) {
        article.put(Article.ARTICLE_CREATE_TIME, new Date(article.optLong(Article.ARTICLE_CREATE_TIME)));
        article.put(Article.ARTICLE_UPDATE_TIME, new Date(article.optLong(Article.ARTICLE_UPDATE_TIME)));
        article.put(Article.ARTICLE_LATEST_CMT_TIME, new Date(article.optLong(Article.ARTICLE_LATEST_CMT_TIME)));
    }

    /**
     * Generates the specified article author thumbnail URL.
     * 
     * @param article the specified article
     */
    private static void genArticleAuthorThumbnailURL(final JSONObject article) {
        final String hashedEmail = MD5.hash(article.optString(Article.ARTICLE_AUTHOR_EMAIL));
        final String thumbnailURL = "http://secure.gravatar.com/avatar/" + hashedEmail + "?s=140&d="
                + Latkes.getStaticServePath() + "/images/user-thumbnail.png";

        article.put(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL, thumbnailURL);
    }

    /**
     * Gets the {@link ArticleQueryService} singleton.
     *
     * @return the singleton
     */
    public static ArticleQueryService getInstance() {
        return SINGLETON;
    }

    /**
     * Private constructor.
     */
    private ArticleQueryService() {
    }
}
