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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Ids;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.event.EventTypes;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Follow;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.Reward;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.OptionRepository;
import org.b3log.symphony.repository.TagArticleRepository;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.repository.UserTagRepository;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Article management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.5.12.11, Aug 31, 2015
 * @since 0.2.0
 */
@Service
public class ArticleMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleMgmtService.class.getName());

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

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
     * User-Tag repository.
     */
    @Inject
    private UserTagRepository userTagRepository;

    /**
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    /**
     * Tag management service.
     */
    @Inject
    private TagMgmtService tagMgmtService;

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
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Increments the view count of the specified article by the given article id.
     *
     * @param articleId the given article id
     * @throws ServiceException service exception
     */
    public void incArticleViewCount(final String articleId) throws ServiceException {
        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final JSONObject article = articleRepository.get(articleId);
            if (null == article) {
                return;
            }

            final int viewCnt = article.optInt(Article.ARTICLE_VIEW_CNT);
            article.put(Article.ARTICLE_VIEW_CNT, viewCnt + 1);

            articleRepository.update(articleId, article);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Incs an article view count failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Adds an article with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "articleTitle": "",
     *     "articleTags": "",
     *     "articleContent": "",
     *     "articleEditorType": "",
     *     "articleAuthorEmail": "",
     *     "articleAuthorId": "",
     *     "articleCommentable": boolean, // optional, default to true
     *     "syncWithSymphonyClient": boolean, // optional
     *     "clientArticleId": "" // optional
     *     "isBroadcast": boolean,
     *     "articleType": int, // optional, default to 0
     *     "articleRewardContent": "", // optional, default to ""
     *     "articleRewardPoint": int // optional default to 0
     * }
     * </pre>, see {@link Article} for more details
     *
     * @return generated article id
     * @throws ServiceException service exception
     */
    public synchronized String addArticle(final JSONObject requestJSONObject) throws ServiceException {
        final long currentTimeMillis = System.currentTimeMillis();
        final boolean fromClient = requestJSONObject.has(Article.ARTICLE_CLIENT_ARTICLE_ID);
        final String authorId = requestJSONObject.optString(Article.ARTICLE_AUTHOR_ID);
        JSONObject author = null;

        final int rewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT, 0);
        if (rewardPoint < 0) {
            throw new ServiceException(langPropsService.get("invalidRewardPointLabel"));
        }

        try {
            // check if admin allow to add article
            final JSONObject option = optionRepository.get(Option.ID_C_MISC_ALLOW_ADD_ARTICLE);

            if (!"0".equals(option.optString(Option.OPTION_VALUE))) {
                throw new ServiceException(langPropsService.get("notAllowAddArticleLabel"));
            }

            author = userRepository.get(authorId);

            if (currentTimeMillis - author.optLong(UserExt.USER_LATEST_ARTICLE_TIME) < Symphonys.getLong("minStepArticleTime")
                    && !Role.ADMIN_ROLE.equals(author.optString(User.USER_ROLE))) {

                LOGGER.log(Level.WARN, "Adds article too frequent [userName={0}]", author.optString(User.USER_NAME));
                throw new ServiceException(langPropsService.get("tooFrequentArticleLabel"));
            }

            if (!fromClient) {
                // Point
                final long followerCnt = followQueryService.getFollowerCount(authorId, Follow.FOLLOWING_TYPE_C_USER);
                final int addition = (int) Math.round(Math.sqrt(followerCnt));

                final int sum = Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE + addition + rewardPoint;
                final int balance = author.optInt(UserExt.USER_POINT);

                if (balance - sum < 0) {
                    throw new ServiceException(langPropsService.get("insufficientBalanceLabel"));
                }
            }

        } catch (final RepositoryException e) {
            throw new ServiceException(e);
        }

        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final String ret = Ids.genTimeMillisId();
            final JSONObject article = new JSONObject();
            article.put(Keys.OBJECT_ID, ret);

            final String clientArticleId = requestJSONObject.optString(Article.ARTICLE_CLIENT_ARTICLE_ID, ret);
            final boolean isBroadcast = requestJSONObject.optBoolean(Article.ARTICLE_T_IS_BROADCAST);

            String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
            articleTitle = Emotions.toAliases(articleTitle);
            article.put(Article.ARTICLE_TITLE, articleTitle);

            article.put(Article.ARTICLE_TAGS, requestJSONObject.optString(Article.ARTICLE_TAGS));
//            if (fromClient) {
//                article.put(Article.ARTICLE_CONTENT, requestJSONObject.optString(Article.ARTICLE_CONTENT));
//            } else {
//                article.put(Article.ARTICLE_CONTENT, requestJSONObject.optString(Article.ARTICLE_CONTENT).
//                        replace("<", "&lt;").replace(">", "&gt;")
//                        .replace("&lt;pre&gt;", "<pre>").replace("&lt;/pre&gt;", "</pre>"));
//            }

            String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
            articleContent = Emotions.toAliases(articleContent);
            article.put(Article.ARTICLE_CONTENT, articleContent);

            article.put(Article.ARTICLE_REWARD_CONTENT, requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT));

            article.put(Article.ARTICLE_EDITOR_TYPE, requestJSONObject.optString(Article.ARTICLE_EDITOR_TYPE));
            article.put(Article.ARTICLE_AUTHOR_EMAIL, requestJSONObject.optString(Article.ARTICLE_AUTHOR_EMAIL));
            article.put(Article.ARTICLE_SYNC_TO_CLIENT, fromClient ? true : author.optBoolean(UserExt.SYNC_TO_CLIENT));
            article.put(Article.ARTICLE_AUTHOR_ID, authorId);
            article.put(Article.ARTICLE_COMMENT_CNT, 0);
            article.put(Article.ARTICLE_VIEW_CNT, 0);
            article.put(Article.ARTICLE_GOOD_CNT, 0);
            article.put(Article.ARTICLE_BAD_CNT, 0);
            article.put(Article.ARTICLE_COLLECT_CNT, 0);
            article.put(Article.ARTICLE_COMMENTABLE, requestJSONObject.optBoolean(Article.ARTICLE_COMMENTABLE, true));
            article.put(Article.ARTICLE_CREATE_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_UPDATE_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_LATEST_CMT_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_PERMALINK, "/article/" + ret);
            if (isBroadcast) {
                article.put(Article.ARTICLE_CLIENT_ARTICLE_ID, "aBroadcast");
            } else {
                article.put(Article.ARTICLE_CLIENT_ARTICLE_ID, clientArticleId);
            }
            article.put(Article.ARTICLE_RANDOM_DOUBLE, Math.random());
            article.put(Article.REDDIT_SCORE, 0);
            article.put(Article.ARTICLE_STATUS, Article.ARTICLE_STATUS_C_VALID);
            article.put(Article.ARTICLE_TYPE,
                    requestJSONObject.optInt(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL));
            article.put(Article.ARTICLE_REWARD_POINT, rewardPoint);
            String city = "";
            article.put(Article.ARTICLE_CITY, city);
            if (UserExt.USER_GEO_STATUS_C_PUBLIC == author.optInt(UserExt.USER_GEO_STATUS)) {
                city = author.optString(UserExt.USER_CITY);
                article.put(Article.ARTICLE_CITY, city);
            }
            tag(article.optString(Article.ARTICLE_TAGS).split(","), article, author);

            final JSONObject articleCntOption = optionRepository.get(Option.ID_C_STATISTIC_ARTICLE_COUNT);
            final int articleCnt = articleCntOption.optInt(Option.OPTION_VALUE);
            articleCntOption.put(Option.OPTION_VALUE, articleCnt + 1);
            optionRepository.update(Option.ID_C_STATISTIC_ARTICLE_COUNT, articleCntOption);

            if (!StringUtils.isBlank(city)) {
                final String cityStatId = city + "-ArticleCount";
                JSONObject cityArticleCntOption = optionRepository.get(cityStatId);

                if (null == cityArticleCntOption) {
                    cityArticleCntOption = new JSONObject();
                    cityArticleCntOption.put(Keys.OBJECT_ID, cityStatId);
                    cityArticleCntOption.put(Option.OPTION_VALUE, 1);
                    cityArticleCntOption.put(Option.OPTION_CATEGORY, city + "-statistic");

                    optionRepository.add(cityArticleCntOption);
                } else {
                    final int cityArticleCnt = cityArticleCntOption.optInt(Option.OPTION_VALUE);
                    cityArticleCntOption.put(Option.OPTION_VALUE, cityArticleCnt + 1);

                    optionRepository.update(cityStatId, cityArticleCntOption);
                }
            }

            author.put(UserExt.USER_ARTICLE_COUNT, author.optInt(UserExt.USER_ARTICLE_COUNT) + 1);
            author.put(UserExt.USER_LATEST_ARTICLE_TIME, currentTimeMillis);
            // Updates user article count (and new tag count), latest article time
            userRepository.update(author.optString(Keys.OBJECT_ID), author);

            final String articleId = articleRepository.add(article);

            transaction.commit();

            // Grows the tag graph
            tagMgmtService.relateTags(article.optString(Article.ARTICLE_TAGS));

            if (!fromClient) {
                // Point
                final long followerCnt = followQueryService.getFollowerCount(authorId, Follow.FOLLOWING_TYPE_C_USER);
                final int addition = (int) Math.round(Math.sqrt(followerCnt));

                pointtransferMgmtService.transfer(authorId, Pointtransfer.ID_C_SYS,
                        Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE,
                        Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE + addition, articleId);

                if (rewardPoint > 0) { // Enabe reward
                    pointtransferMgmtService.transfer(authorId, Pointtransfer.ID_C_SYS,
                            Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE_REWARD, rewardPoint, articleId);
                }
            }

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Common.FROM_CLIENT, fromClient);
            eventData.put(Article.ARTICLE, article);
            try {
                eventManager.fireEventSynchronously(new Event<JSONObject>(EventTypes.ADD_ARTICLE, eventData));
            } catch (final EventException e) {
                LOGGER.log(Level.ERROR, e.getMessage(), e);
            }

            return ret;
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Adds an article failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates an article with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "oId": "",
     *     "articleTitle": "",
     *     "articleTags": "",
     *     "articleContent": "",
     *     "articleEditorType": "",
     *     "articleCommentable": boolean, // optional, default to true
     *     "articleType": int // optional, default to 0
     *     "articleRewardContent": "", // optional, default to ""
     *     "articleRewardPoint": int // optional default to 0
     * }
     * </pre>, see {@link Article} for more details
     *
     * @throws ServiceException service exception
     */
    public synchronized void updateArticle(final JSONObject requestJSONObject) throws ServiceException {
        try {
            // check if admin allow to add article
            final JSONObject option = optionRepository.get(Option.ID_C_MISC_ALLOW_ADD_ARTICLE);

            if (!"0".equals(option.optString(Option.OPTION_VALUE))) {
                throw new ServiceException(langPropsService.get("notAllowAddArticleLabel"));
            }
        } catch (final RepositoryException e) {
            throw new ServiceException(e);
        }

        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final String articleId = requestJSONObject.getString(Keys.OBJECT_ID);
            final JSONObject oldArticle = articleRepository.get(articleId);
            final String authorId = oldArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject author = userRepository.get(authorId);

            processTagsForArticleUpdate(oldArticle, requestJSONObject, author);
            userRepository.update(author.optString(Keys.OBJECT_ID), author);

            final boolean fromClient = requestJSONObject.has(Article.ARTICLE_CLIENT_ARTICLE_ID);

            String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
            articleTitle = Emotions.toAliases(articleTitle);
            oldArticle.put(Article.ARTICLE_TITLE, articleTitle);

            oldArticle.put(Article.ARTICLE_TAGS, requestJSONObject.optString(Article.ARTICLE_TAGS));
            oldArticle.put(Article.ARTICLE_COMMENTABLE, requestJSONObject.optBoolean(Article.ARTICLE_COMMENTABLE, true));
            oldArticle.put(Article.ARTICLE_TYPE,
                    requestJSONObject.optInt(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL));

            String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
            articleContent = Emotions.toAliases(articleContent);
            oldArticle.put(Article.ARTICLE_CONTENT, articleContent);

            final long currentTimeMillis = System.currentTimeMillis();
            final long createTime = oldArticle.optLong(Keys.OBJECT_ID);
            oldArticle.put(Article.ARTICLE_UPDATE_TIME, currentTimeMillis);

            final int rewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT, 0);
            boolean enableReward = false;
            if (1 > oldArticle.optInt(Article.ARTICLE_REWARD_POINT) && 0 < rewardPoint) { // Enable reward
                oldArticle.put(Article.ARTICLE_REWARD_CONTENT, requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT));
                oldArticle.put(Article.ARTICLE_REWARD_POINT, rewardPoint);
                enableReward = true;
            }

            articleRepository.update(articleId, oldArticle);

            transaction.commit();

            if (!fromClient && currentTimeMillis - createTime > 1000 * 60 * 5) {
                // Point
                final long followerCnt = followQueryService.getFollowerCount(authorId, Follow.FOLLOWING_TYPE_C_USER);
                int addition = (int) Math.round(Math.sqrt(followerCnt));
                final long collectCnt = followQueryService.getFollowerCount(articleId, Follow.FOLLOWING_TYPE_C_ARTICLE);
                addition += collectCnt * 2;
                pointtransferMgmtService.transfer(authorId, Pointtransfer.ID_C_SYS,
                        Pointtransfer.TRANSFER_TYPE_C_UPDATE_ARTICLE,
                        Pointtransfer.TRANSFER_SUM_C_UPDATE_ARTICLE + addition, articleId);

                if (enableReward) {
                    pointtransferMgmtService.transfer(authorId, Pointtransfer.ID_C_SYS,
                            Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE_REWARD, rewardPoint, articleId);
                }
            }

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Common.FROM_CLIENT, fromClient);
            eventData.put(Article.ARTICLE, oldArticle);
            try {
                eventManager.fireEventSynchronously(new Event<JSONObject>(EventTypes.UPDATE_ARTICLE, eventData));
            } catch (final EventException e) {
                LOGGER.log(Level.ERROR, e.getMessage(), e);
            }
        } catch (final Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates an article failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates the specified article by the given article id.
     *
     * @param articleId the given article id
     * @param article the specified article
     * @throws ServiceException service exception
     */
    public void updateArticle(final String articleId, final JSONObject article) throws ServiceException {
        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject author = userRepository.get(authorId);
            
            article.put(Article.ARTICLE_COMMENTABLE, Boolean.valueOf(article.optBoolean(Article.ARTICLE_COMMENTABLE)));
            article.put(Article.ARTICLE_SYNC_TO_CLIENT, author.optBoolean(UserExt.SYNC_TO_CLIENT));

            final JSONObject oldArticle = articleRepository.get(articleId);

            processTagsForArticleUpdate(oldArticle, article, author);

            userRepository.update(author.optString(Keys.OBJECT_ID), author);
            articleRepository.update(articleId, article);

            transaction.commit();
        } catch (final Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates an article[id=" + articleId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * A user specified by the given sender id rewards the author of an article specified by the given article id.
     *
     * @param articleId the given article id
     * @param senderId the given sender id
     * @throws ServiceException service exception
     */
    public void reward(final String articleId, final String senderId) throws ServiceException {
        try {
            final JSONObject article = articleRepository.get(articleId);

            if (null == article) {
                return;
            }

            if (Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)) {
                return;
            }

            final JSONObject sender = userRepository.get(senderId);
            if (null == sender) {
                return;
            }

            if (UserExt.USER_STATUS_C_VALID != sender.optInt(UserExt.USER_STATUS)) {
                return;
            }

            final String receiverId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject receiver = userRepository.get(receiverId);
            if (null == receiver) {
                return;
            }

            if (UserExt.USER_STATUS_C_VALID != receiver.optInt(UserExt.USER_STATUS)) {
                return;
            }

            if (receiverId.equals(senderId)) {
                return;
            }

            final int rewardPoint = article.optInt(Article.ARTICLE_REWARD_POINT);
            if (rewardPoint < 1) {
                return;
            }

            if (rewardQueryService.isRewarded(senderId, articleId, Reward.TYPE_C_ARTICLE)) {
                return;
            }

            final String rewardId = Ids.genTimeMillisId();
            final boolean succ = null != pointtransferMgmtService.transfer(senderId, receiverId,
                    Pointtransfer.TRANSFER_TYPE_C_ARTICLE_REWARD, rewardPoint, rewardId);

            if (!succ) {
                throw new ServiceException();
            }

            final JSONObject reward = new JSONObject();
            reward.put(Keys.OBJECT_ID, rewardId);
            reward.put(Reward.SENDER_ID, senderId);
            reward.put(Reward.DATA_ID, articleId);
            reward.put(Reward.TYPE, Reward.TYPE_C_ARTICLE);

            rewardMgmtService.addReward(reward);

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, receiverId);
            notification.put(Notification.NOTIFICATION_DATA_ID, rewardId);

            notificationMgmtService.addArticleRewardNotification(notification);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Rewards an article[id=" + articleId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Processes tags for article update.
     *
     * <ul>
     * <li>Un-tags old article, decrements tag reference count</li>
     * <li>Removes old article-tag relations</li>
     * <li>Saves new article-tag relations with tag reference count</li>
     * </ul>
     *
     * @param oldArticle the specified old article
     * @param newArticle the specified new article
     * @param author the specified author
     * @throws Exception exception
     */
    private synchronized void processTagsForArticleUpdate(final JSONObject oldArticle, final JSONObject newArticle,
            final JSONObject author) throws Exception {
        final String oldArticleId = oldArticle.getString(Keys.OBJECT_ID);
        final List<JSONObject> oldTags = tagRepository.getByArticleId(oldArticleId);
        final String tagsString = newArticle.getString(Article.ARTICLE_TAGS);
        String[] tagStrings = tagsString.split(",");
        final List<JSONObject> newTags = new ArrayList<JSONObject>();

        for (int i = 0; i < tagStrings.length; i++) {
            final String tagTitle = tagStrings[i].trim();
            JSONObject newTag = tagRepository.getByTitle(tagTitle);

            if (null == newTag) {
                newTag = new JSONObject();
                newTag.put(Tag.TAG_TITLE, tagTitle);
            }
            newTags.add(newTag);
        }

        final List<JSONObject> tagsDropped = new ArrayList<JSONObject>();
        final List<JSONObject> tagsNeedToAdd = new ArrayList<JSONObject>();

        for (final JSONObject newTag : newTags) {
            final String newTagTitle = newTag.getString(Tag.TAG_TITLE);

            if (!tagExists(newTagTitle, oldTags)) {
                LOGGER.log(Level.DEBUG, "Tag need to add[title={0}]", newTagTitle);
                tagsNeedToAdd.add(newTag);
            }
        }
        for (final JSONObject oldTag : oldTags) {
            final String oldTagTitle = oldTag.getString(Tag.TAG_TITLE);

            if (!tagExists(oldTagTitle, newTags)) {
                LOGGER.log(Level.DEBUG, "Tag dropped[title={0}]", oldTag);
                tagsDropped.add(oldTag);
            }
        }

        final int articleCmtCnt = oldArticle.getInt(Article.ARTICLE_COMMENT_CNT);

        for (final JSONObject tagDropped : tagsDropped) {
            final String tagId = tagDropped.getString(Keys.OBJECT_ID);
            final int refCnt = tagDropped.getInt(Tag.TAG_REFERENCE_CNT);

            tagDropped.put(Tag.TAG_REFERENCE_CNT, refCnt - 1);
            final int tagCmtCnt = tagDropped.getInt(Tag.TAG_COMMENT_CNT);
            tagDropped.put(Tag.TAG_COMMENT_CNT, tagCmtCnt - articleCmtCnt);

            tagRepository.update(tagId, tagDropped);
        }

        final String[] tagIdsDropped = new String[tagsDropped.size()];

        for (int i = 0; i < tagIdsDropped.length; i++) {
            final JSONObject tag = tagsDropped.get(i);
            final String id = tag.getString(Keys.OBJECT_ID);

            tagIdsDropped[i] = id;
        }

        if (0 != tagIdsDropped.length) {
            removeTagArticleRelations(oldArticleId, tagIdsDropped);
        }

        tagStrings = new String[tagsNeedToAdd.size()];
        for (int i = 0; i < tagStrings.length; i++) {
            final JSONObject tag = tagsNeedToAdd.get(i);
            final String tagTitle = tag.getString(Tag.TAG_TITLE);

            tagStrings[i] = tagTitle;
        }

        newArticle.put(Article.ARTICLE_COMMENT_CNT, articleCmtCnt);
        tag(tagStrings, newArticle, author);
    }

    /**
     * Removes tag-article relations by the specified article id and tag ids of the relations to be removed.
     *
     * <p>
     * Removes all relations if not specified the tag ids.
     * </p>
     *
     * @param articleId the specified article id
     * @param tagIds the specified tag ids of the relations to be removed
     * @throws JSONException json exception
     * @throws RepositoryException repository exception
     */
    private void removeTagArticleRelations(final String articleId, final String... tagIds)
            throws JSONException, RepositoryException {
        final List<String> tagIdList = Arrays.asList(tagIds);
        final List<JSONObject> tagArticleRelations = tagArticleRepository.getByArticleId(articleId);

        for (int i = 0; i < tagArticleRelations.size(); i++) {
            final JSONObject tagArticleRelation = tagArticleRelations.get(i);
            String relationId;

            if (tagIdList.isEmpty()) { // Removes all if un-specified
                relationId = tagArticleRelation.getString(Keys.OBJECT_ID);
                tagArticleRepository.remove(relationId);
            } else {
                if (tagIdList.contains(tagArticleRelation.getString(Tag.TAG + "_" + Keys.OBJECT_ID))) {
                    relationId = tagArticleRelation.getString(Keys.OBJECT_ID);
                    tagArticleRepository.remove(relationId);
                }
            }
        }
    }

    /**
     * Determines whether the specified tag title exists in the specified tags.
     *
     * @param tagTitle the specified tag title
     * @param tags the specified tags
     * @return {@code true} if it exists, {@code false} otherwise
     * @throws JSONException json exception
     */
    private static boolean tagExists(final String tagTitle, final List<JSONObject> tags) throws JSONException {
        for (final JSONObject tag : tags) {
            if (tag.getString(Tag.TAG_TITLE).equals(tagTitle)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Tags the specified article with the specified tag titles.
     *
     * @param tagTitles the specified tag titles
     * @param article the specified article
     * @param author the specified author
     * @throws RepositoryException repository exception
     */
    private synchronized void tag(final String[] tagTitles, final JSONObject article, final JSONObject author)
            throws RepositoryException {
        String articleTags = article.optString(Article.ARTICLE_TAGS);

        for (int i = 0; i < tagTitles.length; i++) {
            final String tagTitle = tagTitles[i].trim();
            JSONObject tag = tagRepository.getByTitle(tagTitle);
            String tagId;
            int userTagType;
            final int articleCmtCnt = article.optInt(Article.ARTICLE_COMMENT_CNT);

            if (null == tag) {
                LOGGER.log(Level.TRACE, "Found a new tag[title={0}] in article[title={1}]",
                        new Object[]{tagTitle, article.optString(Article.ARTICLE_TITLE)});
                tag = new JSONObject();
                tag.put(Tag.TAG_TITLE, tagTitle);
                tag.put(Tag.TAG_REFERENCE_CNT, 1);
                tag.put(Tag.TAG_COMMENT_CNT, articleCmtCnt);
                tag.put(Tag.TAG_FOLLOWER_CNT, 0);
                tag.put(Tag.TAG_DESCRIPTION, "");
                tag.put(Tag.TAG_ICON_PATH, "");
                tag.put(Tag.TAG_STATUS, 0);
                tag.put(Tag.TAG_GOOD_CNT, 0);
                tag.put(Tag.TAG_BAD_CNT, 0);

                tagId = tagRepository.add(tag);
                tag.put(Keys.OBJECT_ID, tagId);
                userTagType = Tag.TAG_TYPE_C_CREATOR;

                final JSONObject tagCntOption = optionRepository.get(Option.ID_C_STATISTIC_TAG_COUNT);
                final int tagCnt = tagCntOption.optInt(Option.OPTION_VALUE);
                tagCntOption.put(Option.OPTION_VALUE, tagCnt + 1);
                optionRepository.update(Option.ID_C_STATISTIC_TAG_COUNT, tagCntOption);

                author.put(UserExt.USER_TAG_COUNT, author.optInt(UserExt.USER_TAG_COUNT) + 1);
            } else {
                tagId = tag.optString(Keys.OBJECT_ID);
                LOGGER.log(Level.TRACE, "Found a existing tag[title={0}, id={1}] in article[title={2}]",
                        new Object[]{tag.optString(Tag.TAG_TITLE), tag.optString(Keys.OBJECT_ID),
                            article.optString(Article.ARTICLE_TITLE)});
                final JSONObject tagTmp = new JSONObject();
                tagTmp.put(Keys.OBJECT_ID, tagId);
                final String title = tag.optString(Tag.TAG_TITLE);
                articleTags = articleTags.replaceAll("(?i)" + tagTitle, title);

                tagTmp.put(Tag.TAG_TITLE, title);
                tagTmp.put(Tag.TAG_COMMENT_CNT, tag.optInt(Tag.TAG_COMMENT_CNT) + articleCmtCnt);
                tagTmp.put(Tag.TAG_STATUS, tag.optInt(Tag.TAG_STATUS));
                tagTmp.put(Tag.TAG_REFERENCE_CNT, tag.optInt(Tag.TAG_REFERENCE_CNT) + 1);
                tagTmp.put(Tag.TAG_FOLLOWER_CNT, tag.optInt(Tag.TAG_FOLLOWER_CNT));
                tagTmp.put(Tag.TAG_DESCRIPTION, tag.optString(Tag.TAG_DESCRIPTION));
                tagTmp.put(Tag.TAG_ICON_PATH, tag.optString(Tag.TAG_ICON_PATH));
                tagTmp.put(Tag.TAG_GOOD_CNT, tag.optInt(Tag.TAG_GOOD_CNT));
                tagTmp.put(Tag.TAG_BAD_CNT, tag.optInt(Tag.TAG_BAD_CNT));

                tagRepository.update(tagId, tagTmp);

                userTagType = Tag.TAG_TYPE_C_ARTICLE;
            }

            article.put(Article.ARTICLE_TAGS, articleTags);

            // Tag-Article relation
            final JSONObject tagArticleRelation = new JSONObject();
            tagArticleRelation.put(Tag.TAG + "_" + Keys.OBJECT_ID, tagId);
            tagArticleRelation.put(Article.ARTICLE + "_" + Keys.OBJECT_ID, article.optString(Keys.OBJECT_ID));
            tagArticleRepository.add(tagArticleRelation);

            // User-Tag relation
            final JSONObject userTagRelation = new JSONObject();
            userTagRelation.put(Tag.TAG + '_' + Keys.OBJECT_ID, tagId);
            userTagRelation.put(User.USER + '_' + Keys.OBJECT_ID, article.optString(Article.ARTICLE_AUTHOR_ID));
            userTagRelation.put(Common.TYPE, userTagType);
            userTagRepository.add(userTagRelation);
        }
    }

    /**
     * Formats the specified article tags.
     *
     * <ul>
     * <li>Trims every tag</li>
     * <li>Deduplication</li>
     * </ul>
     *
     * @param articleTags the specified article tags
     * @return formatted tags string
     */
    public String formatArticleTags(final String articleTags) {
        final String articleTags1 = articleTags.replaceAll("，", ",").replaceAll("、", ",").replaceAll("；", ",")
                .replaceAll(";", ",");
        String[] tagTitles = articleTags1.split(",");

        tagTitles = Strings.trimAll(tagTitles);
        final Set<String> titles = new LinkedHashSet<String>(Arrays.asList(tagTitles)); // deduplication
        tagTitles = titles.toArray(new String[0]);

        final StringBuilder tagsBuilder = new StringBuilder();
        for (final String tagTitle : tagTitles) {
            if (StringUtils.isBlank(tagTitle.trim())) {
                continue;
            }

            tagsBuilder.append(tagTitle.trim()).append(",");
        }
        if (tagsBuilder.length() > 0) {
            tagsBuilder.deleteCharAt(tagsBuilder.length() - 1);
        }

        return tagsBuilder.toString();
    }

    /**
     * Filters the specified article tags.
     *
     * @param articleTags the specified article tags
     * @return filtered tags string
     */
    public String filterReservedTags(final String articleTags) {
        final String[] tags = articleTags.split(",");

        final StringBuilder retBuilder = new StringBuilder();

        for (final String tag : tags) {
            if (!ArrayUtils.contains(Symphonys.RESERVED_TAGS, tag)) {
                retBuilder.append(tag).append(",");
            }
        }
        if (retBuilder.length() > 0) {
            retBuilder.deleteCharAt(retBuilder.length() - 1);
        }

        return retBuilder.toString();
    }
}
