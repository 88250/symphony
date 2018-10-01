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
package org.b3log.symphony.service;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Ids;
import org.b3log.symphony.event.EventTypes;
import org.b3log.symphony.model.*;
import org.b3log.symphony.repository.*;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Comment management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.15.0.0, Aug 28, 2018
 * @since 0.2.0
 */
@Service
public class CommentMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentMgmtService.class);

    /**
     * Revision repository.
     */
    @Inject
    private RevisionRepository revisionRepository;

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
     * Tag-Article repository.
     */
    @Inject
    private TagArticleRepository tagArticleRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Notification repository.
     */
    @Inject
    private NotificationRepository notificationRepository;

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
     * Pointtransfer management service.
     */
    @Inject
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Reward management service.
     */
    @Inject
    private RewardMgmtService rewardMgmtService;

    /**
     * Reward query service.
     */
    @Inject
    private RewardQueryService rewardQueryService;

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Liveness management service.
     */
    @Inject
    private LivenessMgmtService livenessMgmtService;

    /**
     * Accepts a comment specified with the given comment id.
     *
     * @param commentId
     * @throws ServiceException service exception
     */
    public void acceptComment(final String commentId) throws ServiceException {
        try {
            final JSONObject comment = commentRepository.get(commentId);
            final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
            final Query query = new Query().setFilter(new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId));
            final List<JSONObject> comments = CollectionUtils.jsonArrayToList(commentRepository.get(query).optJSONArray(Keys.RESULTS));
            for (final JSONObject c : comments) {
                final int offered = c.optInt(Comment.COMMENT_QNA_OFFERED);
                if (Comment.COMMENT_QNA_OFFERED_C_YES == offered) {
                    return;
                }
            }

            final String rewardId = Ids.genTimeMillisId();

            final JSONObject article = articleRepository.get(articleId);
            final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final String commentAuthorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
            final int offerPoint = article.optInt(Article.ARTICLE_QNA_OFFER_POINT);
            if (Comment.COMMENT_ANONYMOUS_C_PUBLIC == comment.optInt(Comment.COMMENT_ANONYMOUS)) {
                final boolean succ = null != pointtransferMgmtService.transfer(articleAuthorId, commentAuthorId,
                        Pointtransfer.TRANSFER_TYPE_C_QNA_OFFER, offerPoint, rewardId, System.currentTimeMillis(), "");
                if (!succ) {
                    throw new ServiceException(langPropsService.get("transferFailLabel"));
                }
            }

            comment.put(Comment.COMMENT_QNA_OFFERED, Comment.COMMENT_QNA_OFFERED_C_YES);
            final Transaction transaction = commentRepository.beginTransaction();
            commentRepository.update(commentId, comment);
            transaction.commit();

            final JSONObject reward = new JSONObject();
            reward.put(Keys.OBJECT_ID, rewardId);
            reward.put(Reward.SENDER_ID, articleAuthorId);
            reward.put(Reward.DATA_ID, articleId);
            reward.put(Reward.TYPE, Reward.TYPE_C_ACCEPT_COMMENT);
            rewardMgmtService.addReward(reward);

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, commentAuthorId);
            notification.put(Notification.NOTIFICATION_DATA_ID, rewardId);
            notificationMgmtService.addCommentAcceptNotification(notification);

            livenessMgmtService.incLiveness(articleAuthorId, Liveness.LIVENESS_ACCEPT_ANSWER);
        } catch (final ServiceException e) {
            throw e;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Accepts a comment [id=" + commentId + "] failed", e);

            throw new ServiceException(langPropsService.get("systemErrLabel"));
        }
    }

    /**
     * Removes a comment specified with the given comment id. A comment is removable if:
     * <ul>
     * <li>No replies</li>
     * <li>No ups, downs</li>
     * <li>No thanks</li>
     * </ul>
     * Sees https://github.com/b3log/symphony/issues/451 for more details.
     *
     * @param commentId the given commentId id
     * @throws ServiceException service exception
     */
    public void removeComment(final String commentId) throws ServiceException {
        JSONObject comment = null;

        try {
            comment = commentRepository.get(commentId);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets a comment [id=" + commentId + "] failed", e);
        }

        if (null == comment) {
            return;
        }

        final int replyCnt = comment.optInt(Comment.COMMENT_REPLY_CNT);
        if (replyCnt > 0) {
            throw new ServiceException(langPropsService.get("removeCommentFoundReplyLabel"));
        }

        final int ups = comment.optInt(Comment.COMMENT_GOOD_CNT);
        final int downs = comment.optInt(Comment.COMMENT_BAD_CNT);
        if (ups > 0 || downs > 0) {
            throw new ServiceException(langPropsService.get("removeCommentFoundWatchEtcLabel"));
        }

        final int thankCnt = (int) rewardQueryService.rewardedCount(commentId, Reward.TYPE_C_COMMENT);
        if (thankCnt > 0) {
            throw new ServiceException(langPropsService.get("removeCommentFoundThankLabel"));
        }

        if (Comment.COMMENT_QNA_OFFERED_C_YES == comment.optInt(Comment.COMMENT_QNA_OFFERED)) {
            throw new ServiceException(langPropsService.get("removeCommentFoundOfferedLabel"));
        }

        // Perform removal
        removeCommentByAdmin(commentId);
    }

    /**
     * Removes a comment specified with the given comment id. Calls this method will remove all existed data related
     * with the specified comment forcibly.
     *
     * @param commentId the given comment id
     */
    @Transactional
    public void removeCommentByAdmin(final String commentId) {
        try {
            commentRepository.removeComment(commentId);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Removes a comment error [id=" + commentId + "]", e);
        }
    }

    /**
     * A user specified by the given sender id thanks the author of a comment specified by the given comment id.
     *
     * @param commentId the given comment id
     * @param senderId  the given sender id
     * @throws ServiceException service exception
     */
    public void thankComment(final String commentId, final String senderId) throws ServiceException {
        try {
            final JSONObject comment = commentRepository.get(commentId);

            if (null == comment) {
                return;
            }

            if (Comment.COMMENT_STATUS_C_INVALID == comment.optInt(Comment.COMMENT_STATUS)) {
                return;
            }

            final JSONObject sender = userRepository.get(senderId);
            if (null == sender) {
                return;
            }

            if (UserExt.USER_STATUS_C_VALID != sender.optInt(UserExt.USER_STATUS)) {
                return;
            }

            final String receiverId = comment.optString(Comment.COMMENT_AUTHOR_ID);
            final JSONObject receiver = userRepository.get(receiverId);
            if (null == receiver) {
                return;
            }

            if (UserExt.USER_STATUS_C_VALID != receiver.optInt(UserExt.USER_STATUS)) {
                return;
            }

            if (receiverId.equals(senderId)) {
                throw new ServiceException(langPropsService.get("thankSelfLabel"));
            }

            final int rewardPoint = Symphonys.getInt("pointThankComment");

            if (rewardQueryService.isRewarded(senderId, commentId, Reward.TYPE_C_COMMENT)) {
                return;
            }

            final String rewardId = Ids.genTimeMillisId();

            if (Comment.COMMENT_ANONYMOUS_C_PUBLIC == comment.optInt(Comment.COMMENT_ANONYMOUS)) {
                final boolean succ = null != pointtransferMgmtService.transfer(senderId, receiverId,
                        Pointtransfer.TRANSFER_TYPE_C_COMMENT_REWARD, rewardPoint, rewardId, System.currentTimeMillis(), "");

                if (!succ) {
                    throw new ServiceException(langPropsService.get("transferFailLabel"));
                }
            }

            final JSONObject reward = new JSONObject();
            reward.put(Keys.OBJECT_ID, rewardId);
            reward.put(Reward.SENDER_ID, senderId);
            reward.put(Reward.DATA_ID, commentId);
            reward.put(Reward.TYPE, Reward.TYPE_C_COMMENT);

            rewardMgmtService.addReward(reward);

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, receiverId);
            notification.put(Notification.NOTIFICATION_DATA_ID, rewardId);

            notificationMgmtService.addCommentThankNotification(notification);

            livenessMgmtService.incLiveness(senderId, Liveness.LIVENESS_THANK);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Thanks a comment[id=" + commentId + "] failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Adds a comment with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "commentContent": "",
     *                          "commentAuthorId": "",
     *                          "commentOnArticleId": "",
     *                          "commentOriginalCommentId": "", // optional
     *                          "commentAuthorName": "" // If from client
     *                          "commentIP": "", // optional, default to ""
     *                          "commentUA": "", // optional, default to ""
     *                          "commentAnonymous": int, // optional, default to 0 (public)
     *                          "commentVisible": int, // optional, default to 0 (all)
     *                          "userCommentViewMode": int
     *                          , see {@link Comment} for more details
     * @return generated comment id
     * @throws ServiceException service exception
     */
    public synchronized String addComment(final JSONObject requestJSONObject) throws ServiceException {
        final long currentTimeMillis = System.currentTimeMillis();
        final String commentAuthorId = requestJSONObject.optString(Comment.COMMENT_AUTHOR_ID);
        JSONObject commenter;
        try {
            commenter = userRepository.get(commentAuthorId);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets comment author failed", e);

            throw new ServiceException(langPropsService.get("systemErrLabel"));
        }

        if (null == commenter) {
            LOGGER.log(Level.ERROR, "Not found user [id=" + commentAuthorId + "]");

            throw new ServiceException(langPropsService.get("systemErrLabel"));
        }

        if (UserExt.USER_STATUS_C_VALID != commenter.optInt(UserExt.USER_STATUS)) {
            throw new ServiceException(langPropsService.get("userStatusInvalidLabel"));
        }

        final String articleId = requestJSONObject.optString(Comment.COMMENT_ON_ARTICLE_ID);
        final String ip = requestJSONObject.optString(Comment.COMMENT_IP);
        String ua = requestJSONObject.optString(Comment.COMMENT_UA);
        final int commentAnonymous = requestJSONObject.optInt(Comment.COMMENT_ANONYMOUS);
        final int commentVisible = requestJSONObject.optInt(Comment.COMMENT_VISIBLE);
        final int commentViewMode = requestJSONObject.optInt(UserExt.USER_COMMENT_VIEW_MODE);

        if (currentTimeMillis - commenter.optLong(UserExt.USER_LATEST_CMT_TIME) < Symphonys.getLong("minStepCmtTime")
                && !Role.ROLE_ID_C_ADMIN.equals(commenter.optString(User.USER_ROLE))
                && !UserExt.COM_BOT_NAME.equals(commenter.optString(User.USER_NAME))) {
            LOGGER.log(Level.WARN, "Adds comment too frequent [userName={0}]", commenter.optString(User.USER_NAME));
            throw new ServiceException(langPropsService.get("tooFrequentCmtLabel"));
        }

        final String commenterName = commenter.optString(User.USER_NAME);

        JSONObject article;
        try {
            // check if admin allow to add comment
            final JSONObject option = optionRepository.get(Option.ID_C_MISC_ALLOW_ADD_COMMENT);

            if (!"0".equals(option.optString(Option.OPTION_VALUE))) {
                throw new ServiceException(langPropsService.get("notAllowAddCommentLabel"));
            }

            final int balance = commenter.optInt(UserExt.USER_POINT);

            if (Comment.COMMENT_ANONYMOUS_C_ANONYMOUS == commentAnonymous) {
                final int anonymousPoint = Symphonys.getInt("anonymous.point");
                if (balance < anonymousPoint) {
                    String anonymousEnabelPointLabel = langPropsService.get("anonymousEnabelPointLabel");
                    anonymousEnabelPointLabel
                            = anonymousEnabelPointLabel.replace("${point}", String.valueOf(anonymousPoint));
                    throw new ServiceException(anonymousEnabelPointLabel);
                }
            }

            article = articleRepository.get(articleId);

            if (!TuringQueryService.ROBOT_NAME.equals(commenterName)) {
                int pointSum = Pointtransfer.TRANSFER_SUM_C_ADD_COMMENT;

                // Point
                final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                if (articleAuthorId.equals(commentAuthorId)) {
                    pointSum = Pointtransfer.TRANSFER_SUM_C_ADD_SELF_ARTICLE_COMMENT;
                }

                if (balance - pointSum < 0) {
                    throw new ServiceException(langPropsService.get("insufficientBalanceLabel"));
                }
            }
        } catch (final RepositoryException e) {
            throw new ServiceException(e);
        }

        final int articleAnonymous = article.optInt(Article.ARTICLE_ANONYMOUS);

        final Transaction transaction = commentRepository.beginTransaction();

        try {
            article.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT) + 1);
            article.put(Article.ARTICLE_LATEST_CMTER_NAME, commenter.optString(User.USER_NAME));
            if (Comment.COMMENT_ANONYMOUS_C_ANONYMOUS == commentAnonymous) {
                article.put(Article.ARTICLE_LATEST_CMTER_NAME, UserExt.ANONYMOUS_USER_NAME);
            }
            article.put(Article.ARTICLE_LATEST_CMT_TIME, currentTimeMillis);

            final String ret = Ids.genTimeMillisId();
            final JSONObject comment = new JSONObject();
            comment.put(Keys.OBJECT_ID, ret);

            String content = requestJSONObject.optString(Comment.COMMENT_CONTENT).
                    replace("_esc_enter_88250_", "<br/>"); // Solo client escape

            comment.put(Comment.COMMENT_AUTHOR_ID, commentAuthorId);
            comment.put(Comment.COMMENT_ON_ARTICLE_ID, articleId);

            final String originalCmtId = requestJSONObject.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID);
            comment.put(Comment.COMMENT_ORIGINAL_COMMENT_ID, originalCmtId);

            if (StringUtils.isNotBlank(originalCmtId)) {
                final JSONObject originalCmt = commentRepository.get(originalCmtId);
                final int originalCmtReplyCnt = originalCmt.optInt(Comment.COMMENT_REPLY_CNT);
                originalCmt.put(Comment.COMMENT_REPLY_CNT, originalCmtReplyCnt + 1);
                commentRepository.update(originalCmtId, originalCmt);

                notificationMgmtService.makeRead(commentAuthorId, Arrays.asList(originalCmtId));
            }

            content = Emotions.toAliases(content);
            content = content.replaceAll("\\s+$", ""); // https://github.com/b3log/symphony/issues/389
            content += " "; // in case of tailing @user
            content = content.replace(langPropsService.get("uploadingLabel", Locale.SIMPLIFIED_CHINESE), "");
            content = content.replace(langPropsService.get("uploadingLabel", Locale.US), "");

            comment.put(Comment.COMMENT_CONTENT, content);
            comment.put(Comment.COMMENT_CREATE_TIME, System.currentTimeMillis());
            comment.put(Comment.COMMENT_SHARP_URL, "/article/" + articleId + "#" + ret);
            comment.put(Comment.COMMENT_STATUS, Comment.COMMENT_STATUS_C_VALID);
            comment.put(Comment.COMMENT_IP, ip);
            if (StringUtils.length(ua) > Common.MAX_LENGTH_UA) {
                LOGGER.log(Level.WARN, "UA is too long [" + ua + "]");
                ua = StringUtils.substring(ua, 0, Common.MAX_LENGTH_UA);
            }
            comment.put(Comment.COMMENT_UA, ua);
            comment.put(Comment.COMMENT_ANONYMOUS, commentAnonymous);
            comment.put(Comment.COMMENT_VISIBLE, commentVisible);

            final JSONObject cmtCntOption = optionRepository.get(Option.ID_C_STATISTIC_CMT_COUNT);
            final int cmtCnt = cmtCntOption.optInt(Option.OPTION_VALUE);
            cmtCntOption.put(Option.OPTION_VALUE, String.valueOf(cmtCnt + 1));

            articleRepository.update(articleId, article); // Updates article comment count, latest commenter name and time
            optionRepository.update(Option.ID_C_STATISTIC_CMT_COUNT, cmtCntOption); // Updates global comment count
            // Updates tag comment count and User-Tag relation
            final String tagsString = article.optString(Article.ARTICLE_TAGS);
            final String[] tagStrings = tagsString.split(",");
            for (int i = 0; i < tagStrings.length; i++) {
                final String tagTitle = tagStrings[i].trim();
                final JSONObject tag = tagRepository.getByTitle(tagTitle);
                tag.put(Tag.TAG_COMMENT_CNT, tag.optInt(Tag.TAG_COMMENT_CNT) + 1);
                tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

                tagRepository.update(tag.optString(Keys.OBJECT_ID), tag);
            }

            // Updates user comment count, latest comment time
            commenter.put(UserExt.USER_COMMENT_COUNT, commenter.optInt(UserExt.USER_COMMENT_COUNT) + 1);
            commenter.put(UserExt.USER_LATEST_CMT_TIME, currentTimeMillis);
            userRepository.update(commenter.optString(Keys.OBJECT_ID), commenter);

            comment.put(Comment.COMMENT_GOOD_CNT, 0);
            comment.put(Comment.COMMENT_BAD_CNT, 0);
            comment.put(Comment.COMMENT_SCORE, 0D);
            comment.put(Comment.COMMENT_REPLY_CNT, 0);
            comment.put(Comment.COMMENT_AUDIO_URL, "");
            comment.put(Comment.COMMENT_QNA_OFFERED, Comment.COMMENT_QNA_OFFERED_C_NOT);

            // Adds the comment
            final String commentId = commentRepository.add(comment);

            // Updates tag-article relation stat.
            final List<JSONObject> tagArticleRels = tagArticleRepository.getByArticleId(articleId);
            for (final JSONObject tagArticleRel : tagArticleRels) {
                tagArticleRel.put(Article.ARTICLE_LATEST_CMT_TIME, currentTimeMillis);
                tagArticleRel.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT));

                tagArticleRepository.update(tagArticleRel.optString(Keys.OBJECT_ID), tagArticleRel);
            }

            // Revision
            final JSONObject revision = new JSONObject();
            revision.put(Revision.REVISION_AUTHOR_ID, comment.optString(Comment.COMMENT_AUTHOR_ID));

            final JSONObject revisionData = new JSONObject();
            revisionData.put(Comment.COMMENT_CONTENT, content);

            revision.put(Revision.REVISION_DATA, revisionData.toString());
            revision.put(Revision.REVISION_DATA_ID, commentId);
            revision.put(Revision.REVISION_DATA_TYPE, Revision.DATA_TYPE_C_COMMENT);

            revisionRepository.add(revision);

            transaction.commit();

            if (Comment.COMMENT_ANONYMOUS_C_PUBLIC == commentAnonymous
                    && Article.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous
                    && !TuringQueryService.ROBOT_NAME.equals(commenterName)) {
                // Point
                final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                if (articleAuthorId.equals(commentAuthorId)) {
                    pointtransferMgmtService.transfer(commentAuthorId, Pointtransfer.ID_C_SYS,
                            Pointtransfer.TRANSFER_TYPE_C_ADD_COMMENT, Pointtransfer.TRANSFER_SUM_C_ADD_SELF_ARTICLE_COMMENT,
                            commentId, System.currentTimeMillis(), "");
                } else {
                    pointtransferMgmtService.transfer(commentAuthorId, articleAuthorId,
                            Pointtransfer.TRANSFER_TYPE_C_ADD_COMMENT, Pointtransfer.TRANSFER_SUM_C_ADD_COMMENT,
                            commentId, System.currentTimeMillis(), "");
                }

                livenessMgmtService.incLiveness(commentAuthorId, Liveness.LIVENESS_COMMENT);
            }

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Comment.COMMENT, comment);
            eventData.put(Article.ARTICLE, article);
            eventData.put(UserExt.USER_COMMENT_VIEW_MODE, commentViewMode);

            try {
                eventManager.fireEventAsynchronously(new Event<JSONObject>(EventTypes.ADD_COMMENT_TO_ARTICLE, eventData));
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
     * @param comment   the specified comment
     * @throws ServiceException service exception
     */
    public void updateComment(final String commentId, final JSONObject comment) throws ServiceException {
        final Transaction transaction = commentRepository.beginTransaction();

        try {
            final String commentAuthorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
            final JSONObject author = userRepository.get(commentAuthorId);
            if (UserExt.USER_STATUS_C_VALID != author.optInt(UserExt.USER_STATUS)) {
                throw new ServiceException(langPropsService.get("userStatusInvalidLabel"));
            }

            final JSONObject oldComment = commentRepository.get(commentId);
            final String oldContent = oldComment.optString(Comment.COMMENT_CONTENT);

            String content = comment.optString(Comment.COMMENT_CONTENT);
            content = Emotions.toAliases(content);
            content = content.replaceAll("\\s+$", ""); // https://github.com/b3log/symphony/issues/389
            content += " "; // in case of tailing @user
            content = content.replace(langPropsService.get("uploadingLabel", Locale.SIMPLIFIED_CHINESE), "");
            content = content.replace(langPropsService.get("uploadingLabel", Locale.US), "");
            comment.put(Comment.COMMENT_CONTENT, content);

            commentRepository.update(commentId, comment);

            if (!oldContent.equals(content)) {
                // Revision
                final JSONObject revision = new JSONObject();
                revision.put(Revision.REVISION_AUTHOR_ID, commentAuthorId);

                final JSONObject revisionData = new JSONObject();
                revisionData.put(Comment.COMMENT_CONTENT, content);

                revision.put(Revision.REVISION_DATA, revisionData.toString());
                revision.put(Revision.REVISION_DATA_ID, commentId);
                revision.put(Revision.REVISION_DATA_TYPE, Revision.DATA_TYPE_C_COMMENT);

                revisionRepository.add(revision);
            }

            transaction.commit();

            final JSONObject article = articleRepository.get(comment.optString(Comment.COMMENT_ON_ARTICLE_ID));
            final int articleAnonymous = article.optInt(Article.ARTICLE_ANONYMOUS);
            final int commentAnonymous = comment.optInt(Comment.COMMENT_ANONYMOUS);

            if (Comment.COMMENT_ANONYMOUS_C_PUBLIC == commentAnonymous
                    && Article.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous) {
                // Point
                final long now = System.currentTimeMillis();
                final long createTime = comment.optLong(Keys.OBJECT_ID);
                if (now - createTime > 1000 * 60 * 5) {
                    pointtransferMgmtService.transfer(commentAuthorId, Pointtransfer.ID_C_SYS,
                            Pointtransfer.TRANSFER_TYPE_C_UPDATE_COMMENT,
                            Pointtransfer.TRANSFER_SUM_C_UPDATE_COMMENT, commentId, now, "");
                }
            }

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Article.ARTICLE, article);
            eventData.put(Comment.COMMENT, comment);
            try {
                eventManager.fireEventAsynchronously(new Event<>(EventTypes.UPDATE_COMMENT, eventData));
            } catch (final EventException e) {
                LOGGER.log(Level.ERROR, e.getMessage(), e);
            }
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates a comment [id=" + commentId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates the specified comment by the given comment id.
     * <p>
     * <b>Note</b>: This method just for admin console.
     * </p>
     *
     * @param commentId the given comment id
     * @param comment   the specified comment
     * @throws ServiceException service exception
     */
    public void updateCommentByAdmin(final String commentId, final JSONObject comment) throws ServiceException {
        final Transaction transaction = commentRepository.beginTransaction();

        try {
            final String commentAuthorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
            final JSONObject author = userRepository.get(commentAuthorId);
            if (UserExt.USER_STATUS_C_VALID != author.optInt(UserExt.USER_STATUS)) {
                throw new ServiceException(langPropsService.get("userStatusInvalidLabel"));
            }

            final JSONObject oldComment = commentRepository.get(commentId);
            final String oldContent = oldComment.optString(Comment.COMMENT_CONTENT);

            String content = comment.optString(Comment.COMMENT_CONTENT);
            content = Emotions.toAliases(content);
            content = content.replaceAll("\\s+$", ""); // https://github.com/b3log/symphony/issues/389
            content += " "; // in case of tailing @user
            content = content.replace(langPropsService.get("uploadingLabel", Locale.SIMPLIFIED_CHINESE), "");
            content = content.replace(langPropsService.get("uploadingLabel", Locale.US), "");
            comment.put(Comment.COMMENT_CONTENT, content);

            commentRepository.update(commentId, comment);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates a comment [id=" + commentId + "] failed", e);
            throw new ServiceException(e);
        }
    }
}
