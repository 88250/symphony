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
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.util.Ids;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Statistic;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.CommentRepository;
import org.b3log.symphony.repository.StatisticRepository;
import org.json.JSONObject;

/**
 * Comment management service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Oct 7, 2012
 * @since 0.2.0
 */
public final class CommentMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentMgmtService.class.getName());
    /**
     * Singleton.
     */
    private static final CommentMgmtService SINGLETON = new CommentMgmtService();
    /**
     * Comment repository.
     */
    private CommentRepository commentRepository = CommentRepository.getInstance();
    /**
     * Article repository.
     */
    private ArticleRepository articleRepository = ArticleRepository.getInstance();
    /**
     * Statistic repository.
     */
    private StatisticRepository statisticRepository = StatisticRepository.getInstance();

    /**
     * Adds a comment with the specified request json object.
     * 
     * @param requestJSONObject the specified request json object, for example,
     * <pre>
     * {
     *     "commentContent": "",
     *     "commentAuthorId": "",
     *     "commentAuthorEmail": "",
     *     "commentOnArticleId": "",
     *     "commentOriginalCommentId": "" // optional
     * }
     * </pre>, see {@link Comment} for more details
     * @return generated comment id
     * @throws ServiceException service exception
     */
    public String addComment(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = commentRepository.beginTransaction();

        try {
            final String articleId = requestJSONObject.optString(Comment.COMMENT_ON_ARTICLE_ID);
            final JSONObject article = articleRepository.get(articleId);
            article.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT) + 1);

            final String ret = Ids.genTimeMillisId();
            final JSONObject comment = new JSONObject();
            comment.put(Keys.OBJECT_ID, ret);

            comment.put(Comment.COMMENT_CONTENT, requestJSONObject.optString(Comment.COMMENT_CONTENT));
            comment.put(Comment.COMMENT_AUTHOR_EMAIL, requestJSONObject.optString(Comment.COMMENT_AUTHOR_EMAIL));
            comment.put(Comment.COMMENT_AUTHOR_ID, requestJSONObject.optString(Comment.COMMENT_AUTHOR_ID));
            comment.put(Comment.COMMENT_ON_ARTICLE_ID, articleId);
            comment.put(Comment.COMMENT_ORIGINAL_COMMENT_ID, requestJSONObject.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID));

            final long currentTimeMillis = System.currentTimeMillis();

            comment.put(Comment.COMMENT_CREATE_TIME, currentTimeMillis);
            comment.put(Comment.COMMENT_SHARP_URL, "/article/" + articleId + "#" + ret);
            comment.put(Comment.COMMENT_STATUS, 0);

            final JSONObject statistic = statisticRepository.get(Statistic.STATISTIC);
            statistic.put(Statistic.STATISTIC_CMT_COUNT, statistic.optInt(Statistic.STATISTIC_CMT_COUNT) + 1);

            articleRepository.update(articleId, article); // Updates article comment count
            statisticRepository.update(Statistic.STATISTIC, statistic); // Updates global comment count
            commentRepository.add(comment);

            transaction.commit();

            return ret;
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.SEVERE, "Adds a comment failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the {@link CommentMgmtService} singleton.
     *
     * @return the singleton
     */
    public static CommentMgmtService getInstance() {
        return SINGLETON;
    }

    /**
     * Private constructor.
     */
    private CommentMgmtService() {
    }
}
