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
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
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
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.12, Dec 31, 2012
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
     * Option repository.
     */
    private OptionRepository optionRepository = OptionRepository.getInstance();
    /**
     * Tag repository.
     */
    private TagRepository tagRepository = TagRepository.getInstance();
    /**
     * User repository.
     */
    private UserRepository userRepository = UserRepository.getInstance();
    /**
     * Event manager.
     */
    private EventManager eventManager = EventManager.getInstance();
    /**
     * Language service.
     */
    private LangPropsService langPropsService = LangPropsService.getInstance();

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
     * @return generated comment id
     * @throws ServiceException service exception
     */
    public String addComment(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = commentRepository.beginTransaction();

        try {
            final JSONObject commenter = requestJSONObject.optJSONObject(Comment.COMMENT_T_COMMENTER);

            final long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - commenter.optLong(UserExt.USER_LATEST_CMT_TIME) < Symphonys.getLong("minStepArticleTime")) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                LOGGER.log(Level.WARNING, "Adds comment too frequent [userName={0}]", commenter.optString(User.USER_NAME));
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
                    .replace("<", "&lt;").replace(ret, ret).replace(">", "&gt;").replace("_esc_enter_88250_", "<br/>");

            comment.put(Comment.COMMENT_AUTHOR_EMAIL, requestJSONObject.optString(Comment.COMMENT_AUTHOR_EMAIL));
            comment.put(Comment.COMMENT_AUTHOR_ID, requestJSONObject.optString(Comment.COMMENT_AUTHOR_ID));
            comment.put(Comment.COMMENT_ON_ARTICLE_ID, articleId);
            final boolean fromClient = requestJSONObject.has(Comment.COMMENT_CLIENT_COMMENT_ID);
            if (fromClient) {
                comment.put(Comment.COMMENT_CLIENT_COMMENT_ID, requestJSONObject.optString(Comment.COMMENT_CLIENT_COMMENT_ID));

                // Appends original commenter name
                final String authorName = comment.optString(Comment.COMMENT_T_AUTHOR_NAME);
                securedContent += " <i class='ft-small'>by " + authorName + "</i>";
                comment.remove(Comment.COMMENT_T_AUTHOR_NAME);
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
                eventManager.fireEventAsynchronously(new Event<JSONObject>(EventTypes.ADD_COMMENT_TO_ARTICLE, eventData));
            } catch (final EventException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }

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
