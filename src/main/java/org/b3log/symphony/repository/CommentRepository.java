/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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
package org.b3log.symphony.repository;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.cache.CommentCache;
import org.b3log.symphony.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Comment repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.1.0, May 7, 2018
 * @since 0.2.0
 */
@Repository
public class CommentRepository extends AbstractRepository {

    /**
     * Comment cache.
     */
    @Inject
    private CommentCache commentCache;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Revision repository.
     */
    @Inject
    private RevisionRepository revisionRepository;

    /**
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    /**
     * Notification repository.
     */
    @Inject
    private NotificationRepository notificationRepository;

    /**
     * Public constructor.
     */
    public CommentRepository() {
        super(Comment.COMMENT);
    }

    /**
     * Removes a comment specified with the given comment id. Calls this method will remove all existed data related
     * with the specified comment forcibly.
     *
     * @param commentId the given comment id
     * @throws RepositoryException repository exception
     */
    public void removeComment(final String commentId) throws RepositoryException {
        final JSONObject comment = get(commentId);
        if (null == comment) {
            return;
        }

        remove(comment.optString(Keys.OBJECT_ID));

        final String commentAuthorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
        final JSONObject commenter = userRepository.get(commentAuthorId);
        commenter.put(UserExt.USER_COMMENT_COUNT, commenter.optInt(UserExt.USER_COMMENT_COUNT) - 1);
        userRepository.update(commentAuthorId, commenter);

        final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
        final JSONObject article = articleRepository.get(articleId);
        article.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT) - 1);
        if (0 < article.optInt(Article.ARTICLE_COMMENT_CNT)) {
            final Query latestCmtQuery = new Query().
                    setFilter(new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId)).
                    addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).setCurrentPageNum(1).setPageSize(1);
            final JSONObject latestCmt = get(latestCmtQuery).optJSONArray(Keys.RESULTS).optJSONObject(0);
            article.put(Article.ARTICLE_LATEST_CMT_TIME, latestCmt.optLong(Keys.OBJECT_ID));
            final JSONObject latestCmtAuthor = userRepository.get(latestCmt.optString(Comment.COMMENT_AUTHOR_ID));
            article.put(Article.ARTICLE_LATEST_CMTER_NAME, latestCmtAuthor.optString(User.USER_NAME));
        } else {
            article.put(Article.ARTICLE_LATEST_CMT_TIME, 0);
            article.put(Article.ARTICLE_LATEST_CMTER_NAME, "");
        }
        articleRepository.update(articleId, article);

        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Revision.REVISION_DATA_ID, FilterOperator.EQUAL, commentId),
                new PropertyFilter(Revision.REVISION_DATA_TYPE, FilterOperator.EQUAL, Revision.DATA_TYPE_C_COMMENT)
        ));
        final JSONArray commentRevisions = revisionRepository.get(query).optJSONArray(Keys.RESULTS);
        for (int j = 0; j < commentRevisions.length(); j++) {
            final JSONObject articleRevision = commentRevisions.optJSONObject(j);
            revisionRepository.remove(articleRevision.optString(Keys.OBJECT_ID));
        }

        final JSONObject commentCntOption = optionRepository.get(Option.ID_C_STATISTIC_CMT_COUNT);
        commentCntOption.put(Option.OPTION_VALUE, commentCntOption.optInt(Option.OPTION_VALUE) - 1);
        optionRepository.update(Option.ID_C_STATISTIC_CMT_COUNT, commentCntOption);

        final String originalCommentId = comment.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID);
        if (StringUtils.isNotBlank(originalCommentId)) {
            final JSONObject originalComment = get(originalCommentId);
            if (null != originalComment) {
                originalComment.put(Comment.COMMENT_REPLY_CNT, originalComment.optInt(Comment.COMMENT_REPLY_CNT) - 1);

                update(originalCommentId, originalComment);
            }
        }

        notificationRepository.removeByDataId(commentId);
    }

    @Override
    public void remove(final String id) throws RepositoryException {
        super.remove(id);

        commentCache.removeComment(id);
    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {
        JSONObject ret = commentCache.getComment(id);
        if (null != ret) {
            return ret;
        }

        ret = super.get(id);
        if (null == ret) {
            return null;
        }

        commentCache.putComment(ret);

        return ret;
    }

    @Override
    public void update(final String id, final JSONObject comment) throws RepositoryException {
        super.update(id, comment);

        comment.put(Keys.OBJECT_ID, id);
        commentCache.putComment(comment);
    }
}
