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
import org.b3log.latke.Keys;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Ids;
import org.b3log.symphony.event.EventTypes;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.CommentRepository;
import org.b3log.symphony.repository.OptionRepository;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Comment management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.1.14, Apr 9, 2015
 * @since 0.2.0
 */
@Service
public class CommentMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentMgmtService.class.getName());

    /**
     * Comment repository.
     */
    @Inject
    private CommentRepository commentRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    /**
     * Tag repository.
     */
    @Inject
    private TagRepository tagRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Event manager.
     */
    @Inject
    private EventManager eventManager;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

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
     *     "commentOriginalCommentId": "", // optional
     *     "clientCommentId": "" // optional,
     *     "commentAuthorName": "" // If from client
     *     "commenter": {
     *         // User model
     *     }
     * }
     * </pre>, see {@link Comment} for more details
     *
     * @return generated comment id
     * @throws ServiceException service exception
     */
    public String addComment(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = commentRepository.beginTransaction();

        try {
            final JSONObject commenter = requestJSONObject.optJSONObject(Comment.COMMENT_T_COMMENTER);

            final long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - commenter.optLong(UserExt.USER_LATEST_CMT_TIME) < Symphonys.getLong("minStepCmtTime")) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                LOGGER.log(Level.WARN, "Adds comment too frequent [userName={0}]", commenter.optString(User.USER_NAME));
                throw new ServiceException(langPropsService.get("tooFrequentArticleLabel"));
            }

            final String articleId = requestJSONObject.optString(Comment.COMMENT_ON_ARTICLE_ID);
            final JSONObject article = articleRepository.get(articleId);
            article.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT) + 1);
            article.put(Article.ARTICLE_LATEST_CMT_TIME, System.currentTimeMillis());

            final String ret = Ids.genTimeMillisId();
            final JSONObject comment = new JSONObject();
            comment.put(Keys.OBJECT_ID, ret);

            String securedContent = requestJSONObject.optString(Comment.COMMENT_CONTENT)
                    .replace("<", "&lt;").replace(ret, ret).replace(">", "&gt;")
                    .replace("&lt;pre&gt;", "<pre>").replace("_esc_enter_88250_", "<br/>");

            comment.put(Comment.COMMENT_AUTHOR_EMAIL, requestJSONObject.optString(Comment.COMMENT_AUTHOR_EMAIL));
            comment.put(Comment.COMMENT_AUTHOR_ID, requestJSONObject.optString(Comment.COMMENT_AUTHOR_ID));
            comment.put(Comment.COMMENT_ON_ARTICLE_ID, articleId);
            final boolean fromClient = requestJSONObject.has(Comment.COMMENT_CLIENT_COMMENT_ID);
            if (fromClient) {
                comment.put(Comment.COMMENT_CLIENT_COMMENT_ID, requestJSONObject.optString(Comment.COMMENT_CLIENT_COMMENT_ID));

                // Appends original commenter name
                final String authorName = requestJSONObject.optString(Comment.COMMENT_T_AUTHOR_NAME);
                securedContent += " <i class='ft-small'>by " + authorName + "</i>";
            }
            comment.put(Comment.COMMENT_ORIGINAL_COMMENT_ID, requestJSONObject.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID));
            comment.put(Comment.COMMENT_CONTENT, securedContent);

            comment.put(Comment.COMMENT_CREATE_TIME, System.currentTimeMillis());
            comment.put(Comment.COMMENT_SHARP_URL, "/article/" + articleId + "#" + ret);
            comment.put(Comment.COMMENT_STATUS, 0);

            final JSONObject cmtCntOption = optionRepository.get(Option.ID_C_STATISTIC_CMT_COUNT);
            final int cmtCnt = cmtCntOption.optInt(Option.OPTION_VALUE);
            cmtCntOption.put(Option.OPTION_VALUE, String.valueOf(cmtCnt + 1));

            articleRepository.update(articleId, article); // Updates article comment count
            optionRepository.update(Option.ID_C_STATISTIC_CMT_COUNT, cmtCntOption); // Updates global comment count
            // Updates tag comment count and User-Tag relation
            final String tagsString = article.optString(Article.ARTICLE_TAGS);
            final String[] tagStrings = tagsString.split(",");
            for (int i = 0; i < tagStrings.length; i++) {
                final String tagTitle = tagStrings[i].trim();
                final JSONObject tag = tagRepository.getByTitle(tagTitle);
                tag.put(Tag.TAG_COMMENT_CNT, tag.optInt(Tag.TAG_COMMENT_CNT) + 1);
                tagRepository.update(tag.optString(Keys.OBJECT_ID), tag);
            }

            // Updates user comment count, latest comment time
            commenter.put(UserExt.USER_COMMENT_COUNT, commenter.optInt(UserExt.USER_COMMENT_COUNT) + 1);
            commenter.put(UserExt.USER_LATEST_CMT_TIME, currentTimeMillis);
            userRepository.update(commenter.optString(Keys.OBJECT_ID), commenter);

            // Adds the comment
            commentRepository.add(comment);

            transaction.commit();

            final JSONObject eventData = new JSONObject();
            eventData.put(Comment.COMMENT, comment);
            eventData.put(Common.FROM_CLIENT, fromClient);
            eventData.put(Article.ARTICLE, article);

            try {
                eventManager.fireEventSynchronously(new Event<JSONObject>(EventTypes.ADD_COMMENT_TO_ARTICLE, eventData));
            } catch (final EventException e) {
                LOGGER.log(Level.ERROR, e.getMessage(), e);
            }

            return ret;
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Adds a comment failed", e);
            throw new ServiceException(e);
        }
    }
    
    /**
     * Updates the specified comment by the given comment id.
     *
     * @param commentId the given comment id
     * @param comment the specified comment
     * @throws ServiceException service exception
     */
    public void updateComment(final String commentId, final JSONObject comment) throws ServiceException {
        final Transaction transaction = commentRepository.beginTransaction();

        try {
            commentRepository.update(commentId, comment);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates a comment[id=" + commentId + "] failed", e);
            throw new ServiceException(e);
        }
    }
}
