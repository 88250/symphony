/*
 * Copyright (c) 2012-2015, b3log.org
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

import javax.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Vote;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.VoteRepository;
import org.json.JSONObject;

/**
 * Vote management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 13, 2015
 * @since 1.3.0
 */
@Service
public class VoteMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(VoteMgmtService.class.getName());

    /**
     * Vote repository.
     */
    @Inject
    private VoteRepository voteRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * The specified user vote up the specified article.
     *
     * @param userId the specified user id
     * @param articleId the specified article id
     * @throws ServiceException service exception
     */
    @Transactional
    public void voteUpArticle(final String userId, final String articleId) throws ServiceException {
        try {
            up(userId, articleId, Vote.DATA_TYPE_C_ARTICLE);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + userId + "] vote up an article[id=" + articleId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * The specified user vote down the specified article.
     *
     * @param userId the specified user id
     * @param articleId the specified article id
     * @throws ServiceException service exception
     */
    @Transactional
    public void voteDownArticle(final String userId, final String articleId) throws ServiceException {
        try {
            down(userId, articleId, Vote.DATA_TYPE_C_ARTICLE);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + userId + "] vote down an article[id=" + articleId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * The specified user vote up the specified data entity with the specified data type.
     *
     * @param userId the specified user id
     * @param dataId the specified data entity id
     * @param dataType the specified data type
     * @throws RepositoryException repository exception
     */
    private void up(final String userId, final String dataId, final int dataType) throws RepositoryException {
        final int oldType = voteRepository.removeIfExists(userId, dataId);

        if (Vote.DATA_TYPE_C_ARTICLE == dataType) {
            final JSONObject article = articleRepository.get(dataId);
            if (null == article) {
                LOGGER.log(Level.ERROR, "Not found article [id={0}] to vote up", dataId);

                return;
            }

            if (-1 == oldType) {
                article.put(Article.ARTICLE_GOOD_CNT, article.optInt(Article.ARTICLE_GOOD_CNT) + 1);

                articleRepository.update(dataId, article);
            } else if (Vote.TYPE_C_DOWN == oldType) {
                article.put(Article.ARTICLE_BAD_CNT, article.optInt(Article.ARTICLE_BAD_CNT) - 1);
                article.put(Article.ARTICLE_GOOD_CNT, article.optInt(Article.ARTICLE_GOOD_CNT) + 1);

                articleRepository.update(dataId, article);
            }
        }

        final JSONObject vote = new JSONObject();
        vote.put(Vote.USER_ID, userId);
        vote.put(Vote.DATA_ID, dataId);
        vote.put(Vote.TYPE, Vote.TYPE_C_UP);
        vote.put(Vote.DATA_TYPE, dataType);

        voteRepository.add(vote);
    }

    /**
     * The specified user vote down the specified data entity with the specified data type.
     *
     * @param userId the specified user id
     * @param dataId the specified data entity id
     * @param dataType the specified data type
     * @throws RepositoryException repository exception
     */
    private void down(final String userId, final String dataId, final int dataType) throws RepositoryException {
        final int oldType = voteRepository.removeIfExists(userId, dataId);

        if (Vote.DATA_TYPE_C_ARTICLE == dataType) {
            final JSONObject article = articleRepository.get(dataId);
            if (null == article) {
                LOGGER.log(Level.ERROR, "Not found article [id={0}] to vote down", dataId);

                return;
            }

            if (-1 == oldType) {
                article.put(Article.ARTICLE_BAD_CNT, article.optInt(Article.ARTICLE_BAD_CNT) + 1);

                articleRepository.update(dataId, article);
            } else if (Vote.TYPE_C_UP == oldType) {
                article.put(Article.ARTICLE_GOOD_CNT, article.optInt(Article.ARTICLE_GOOD_CNT) - 1);
                article.put(Article.ARTICLE_BAD_CNT, article.optInt(Article.ARTICLE_BAD_CNT) + 1);

                articleRepository.update(dataId, article);
            }
        }

        final JSONObject vote = new JSONObject();
        vote.put(Vote.USER_ID, userId);
        vote.put(Vote.DATA_ID, dataId);
        vote.put(Vote.TYPE, Vote.TYPE_C_DOWN);
        vote.put(Vote.DATA_TYPE, dataType);

        voteRepository.add(vote);
    }
}
