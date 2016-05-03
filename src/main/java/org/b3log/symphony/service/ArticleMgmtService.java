/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
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
import java.util.HashSet;
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
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Ids;
import org.b3log.symphony.event.EventTypes;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Follow;
import org.b3log.symphony.model.Liveness;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.Revision;
import org.b3log.symphony.model.Reward;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.CommentRepository;
import org.b3log.symphony.repository.NotificationRepository;
import org.b3log.symphony.repository.OptionRepository;
import org.b3log.symphony.repository.RevisionRepository;
import org.b3log.symphony.repository.TagArticleRepository;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.repository.UserTagRepository;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * Article management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.9.19.15, Apr 20, 2016
 * @since 0.2.0
 */
@Service
public class ArticleMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleMgmtService.class.getName());

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
     * Notification repository.
     */
    @Inject
    private NotificationRepository notificationRepository;

    /**
     * Revision repository.
     */
    @Inject
    private RevisionRepository revisionRepository;

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
     * Tag query service.
     */
    @Inject
    private TagQueryService tagQueryService;

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
     * Search management service.
     */
    @Inject
    private SearchMgmtService searchMgmtService;

    /**
     * Generate tag max count.
     */
    private static final int GEN_TAG_MAX_CNT = 4;

    /**
     * Removes an article specified with the given article id.
     *
     * @param articleId the given article id
     */
    @Transactional
    public void removeArticle(final String articleId) {
        try {
            final JSONObject article = articleRepository.get(articleId);

            if (null == article) {
                return;
            }

            final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject author = userRepository.get(authorId);
            author.put(UserExt.USER_ARTICLE_COUNT, author.optInt(UserExt.USER_ARTICLE_COUNT) - 1);
            userRepository.update(author.optString(Keys.OBJECT_ID), author);

            final String city = article.optString(Article.ARTICLE_CITY);
            final String cityStatId = city + "-ArticleCount";
            final JSONObject cityArticleCntOption = optionRepository.get(cityStatId);
            if (null != cityArticleCntOption) {
                cityArticleCntOption.put(Option.OPTION_VALUE,
                        cityArticleCntOption.optInt(Option.OPTION_VALUE) - 1);
                optionRepository.update(cityStatId, cityArticleCntOption);
            }

            final JSONObject articleCntOption = optionRepository.get(Option.ID_C_STATISTIC_ARTICLE_COUNT);
            articleCntOption.put(Option.OPTION_VALUE, articleCntOption.optInt(Option.OPTION_VALUE) - 1);
            optionRepository.update(Option.ID_C_STATISTIC_ARTICLE_COUNT, articleCntOption);

            articleRepository.remove(articleId);

            final List<JSONObject> tagArticleRels = tagArticleRepository.getByArticleId(articleId);
            for (final JSONObject tagArticleRel : tagArticleRels) {
                final String tagId = tagArticleRel.optString(Tag.TAG + "_" + Keys.OBJECT_ID);
                final JSONObject tag = tagRepository.get(tagId);
                tag.put(Tag.TAG_REFERENCE_CNT, tag.optInt(Tag.TAG_REFERENCE_CNT) - 1);
                tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

                tagRepository.update(tagId, tag);
            }

            tagArticleRepository.removeByArticleId(articleId);

            notificationRepository.removeByDataId(articleId);

            final Query query = new Query().setFilter(new PropertyFilter(
                    Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId)).setPageCount(1);
            final JSONArray comments = commentRepository.get(query).optJSONArray(Keys.RESULTS);
            final int commentCnt = comments.length();
            for (int i = 0; i < commentCnt; i++) {
                final JSONObject comment = comments.optJSONObject(i);
                final String commentId = comment.optString(Keys.OBJECT_ID);

                final String commentAuthorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
                final JSONObject commenter = userRepository.get(commentAuthorId);
                commenter.put(UserExt.USER_COMMENT_COUNT, commenter.optInt(UserExt.USER_COMMENT_COUNT) - 1);
                userRepository.update(commentAuthorId, commenter);

                commentRepository.remove(commentId);

                notificationRepository.removeByDataId(commentId);
            }

            final JSONObject commentCntOption = optionRepository.get(Option.ID_C_STATISTIC_CMT_COUNT);
            commentCntOption.put(Option.OPTION_VALUE, commentCntOption.optInt(Option.OPTION_VALUE) - commentCnt);
            optionRepository.update(Option.ID_C_STATISTIC_CMT_COUNT, commentCntOption);

            if (Symphonys.getBoolean("algolia.enabled")) {
                searchMgmtService.removeAlgoliaDocument(article);
            }

            if (Symphonys.getBoolean("es.enabled")) {
                searchMgmtService.removeESDocument(article, Article.ARTICLE);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Removes an article error [id=" + articleId + "]", e);
        }
    }

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

            article.put(Article.ARTICLE_RANDOM_DOUBLE, Math.random());

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
     *     "clientArticleId": "", // optional
     *     "clientArticlePermalink": "", // optional
     *     "isBroadcast": boolean, // Client broadcast, optional
     *     "articleType": int, // optional, default to 0
     *     "articleRewardContent": "", // optional, default to ""
     *     "articleRewardPoint": int, // optional, default to 0
     *     "articleIP": "", // optional, default to ""
     *     "articleUA": "", // optional, default to ""
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

        String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);

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

            final JSONObject maybeExist = articleRepository.getByTitle(articleTitle);
            if (null != maybeExist) {
                final String existArticleAuthorId = maybeExist.optString(Article.ARTICLE_AUTHOR_ID);
                final JSONObject existArticleAuthor = userRepository.get(existArticleAuthorId);
                final String userName = existArticleAuthor.optString(User.USER_NAME);
                String msg = langPropsService.get("duplicatedArticleTitleLabel");
                msg = msg.replace("{user}", "<a target='_blank' href='/member/" + userName + "'>" + userName + "</a>");
                msg = msg.replace("{article}", "<a target='_blank' href='/article/" + maybeExist.optString(Keys.OBJECT_ID)
                        + "'>" + articleTitle + "</a>");

                throw new ServiceException(msg);
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
            final String clientArticlePermalink = requestJSONObject.optString(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK);
            final boolean isBroadcast = requestJSONObject.optBoolean(Article.ARTICLE_T_IS_BROADCAST);

            articleTitle = Emotions.toAliases(articleTitle);
            article.put(Article.ARTICLE_TITLE, articleTitle);

            article.put(Article.ARTICLE_TAGS, requestJSONObject.optString(Article.ARTICLE_TAGS));

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
            article.put(Article.ARTICLE_LATEST_CMT_TIME, 0);
            article.put(Article.ARTICLE_LATEST_CMTER_NAME, "");
            article.put(Article.ARTICLE_PERMALINK, "/article/" + ret);
            if (isBroadcast) {
                article.put(Article.ARTICLE_CLIENT_ARTICLE_ID, "aBroadcast");
            } else {
                article.put(Article.ARTICLE_CLIENT_ARTICLE_ID, clientArticleId);
            }
            article.put(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK, clientArticlePermalink);
            article.put(Article.ARTICLE_RANDOM_DOUBLE, Math.random());
            article.put(Article.REDDIT_SCORE, 0);
            article.put(Article.ARTICLE_STATUS, Article.ARTICLE_STATUS_C_VALID);
            final int articleType = requestJSONObject.optInt(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL);
            article.put(Article.ARTICLE_TYPE, articleType);
            article.put(Article.ARTICLE_REWARD_POINT, rewardPoint);
            String city = "";
            article.put(Article.ARTICLE_CITY, city);
            if (UserExt.USER_GEO_STATUS_C_PUBLIC == author.optInt(UserExt.USER_GEO_STATUS)) {
                city = author.optString(UserExt.USER_CITY);
                article.put(Article.ARTICLE_CITY, city);
            }

            String articleTags = article.optString(Article.ARTICLE_TAGS);
            articleTags = Tag.formatTags(articleTags);
            String[] tagTitles = articleTags.split(",");
            if (tagTitles.length < GEN_TAG_MAX_CNT && Article.ARTICLE_TYPE_C_DISCUSSION != articleType
                    && Article.ARTICLE_TYPE_C_THOUGHT != articleType && !Tag.containsReservedTags(articleTags)) {
                final String content = article.optString(Article.ARTICLE_TITLE)
                        + " " + Jsoup.parse("<p>" + article.optString(Article.ARTICLE_CONTENT) + "</p>").text();
                final List<String> genTags = tagQueryService.generateTags(content, GEN_TAG_MAX_CNT);
                if (!genTags.isEmpty()) {
                    articleTags = articleTags + "," + StringUtils.join(genTags, ",");
                    articleTags = Tag.formatTags(articleTags);
                    articleTags = Tag.useHead(articleTags, GEN_TAG_MAX_CNT);
                }
            }

            if (StringUtils.isBlank(articleTags)) {
                articleTags = "B3log";
            }

            articleTags = Tag.formatTags(articleTags);
            article.put(Article.ARTICLE_TAGS, articleTags);
            tagTitles = articleTags.split(",");

            tag(tagTitles, article, author);

            final String ip = requestJSONObject.optString(Article.ARTICLE_IP);
            article.put(Article.ARTICLE_IP, ip);

            String ua = requestJSONObject.optString(Article.ARTICLE_UA);
            if (StringUtils.length(ua) > Common.MAX_LENGTH_UA) {
                ua = StringUtils.substring(ua, 0, Common.MAX_LENGTH_UA);
            }
            article.put(Article.ARTICLE_UA, ua);

            article.put(Article.ARTICLE_STICK, 0L);

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

            if (Article.ARTICLE_TYPE_C_THOUGHT != articleType) {
                // Revision
                final JSONObject revision = new JSONObject();
                revision.put(Revision.REVISION_AUTHOR_ID, authorId);
                final JSONObject revisionData = new JSONObject();
                revisionData.put(Article.ARTICLE_TITLE, articleTitle);
                revisionData.put(Article.ARTICLE_CONTENT, articleContent);
                revision.put(Revision.REVISION_DATA, revisionData.toString());
                revision.put(Revision.REVISION_DATA_ID, articleId);
                revision.put(Revision.REVISION_DATA_TYPE, Revision.DATA_TYPE_C_ARTICLE);

                revisionRepository.add(revision);
            }

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
                            Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE_REWARD,
                            Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_REWARD, articleId);
                }

                if (Article.ARTICLE_TYPE_C_CITY_BROADCAST == articleType) {
                    pointtransferMgmtService.transfer(authorId, Pointtransfer.ID_C_SYS,
                            Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE_BROADCAST,
                            Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST, articleId);
                }

                // Liveness
                livenessMgmtService.incLiveness(authorId, Liveness.LIVENESS_ARTICLE);
            }

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Common.FROM_CLIENT, fromClient);
            eventData.put(Article.ARTICLE, article);
            try {
                eventManager.fireEventAsynchronously(new Event<JSONObject>(EventTypes.ADD_ARTICLE, eventData));
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
     *     "clientArticlePermalink": "", // optional
     *     "articleType": int // optional, default to 0
     *     "articleRewardContent": "", // optional, default to ""
     *     "articleRewardPoint": int, // optional, default to 0
     *     "articleIP": "", // optional, default to ""
     *     "articleUA": "" // optional, default to ""
     * }
     * </pre>, see {@link Article} for more details
     *
     * @throws ServiceException service exception
     */
    public synchronized void updateArticle(final JSONObject requestJSONObject) throws ServiceException {
        String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        final boolean fromClient = requestJSONObject.has(Article.ARTICLE_CLIENT_ARTICLE_ID);

        String articleId;
        JSONObject oldArticle;
        String authorId;
        JSONObject author;
        int updatePointSum;

        try {
            // check if admin allow to add article
            final JSONObject option = optionRepository.get(Option.ID_C_MISC_ALLOW_ADD_ARTICLE);

            if (!"0".equals(option.optString(Option.OPTION_VALUE))) {
                throw new ServiceException(langPropsService.get("notAllowAddArticleLabel"));
            }

            articleId = requestJSONObject.optString(Keys.OBJECT_ID);
            oldArticle = articleRepository.get(articleId);
            authorId = oldArticle.optString(Article.ARTICLE_AUTHOR_ID);
            author = userRepository.get(authorId);

            final long followerCnt = followQueryService.getFollowerCount(authorId, Follow.FOLLOWING_TYPE_C_USER);
            int addition = (int) Math.round(Math.sqrt(followerCnt));
            final long collectCnt = followQueryService.getFollowerCount(articleId, Follow.FOLLOWING_TYPE_C_ARTICLE);
            addition += collectCnt * 2;
            updatePointSum = Pointtransfer.TRANSFER_SUM_C_UPDATE_ARTICLE + addition;

            if (!fromClient) {
                // Point
                final int balance = author.optInt(UserExt.USER_POINT);
                if (balance - updatePointSum < 0) {
                    throw new ServiceException(langPropsService.get("insufficientBalanceLabel"));
                }
            }

            final JSONObject maybeExist = articleRepository.getByTitle(articleTitle);
            if (null != maybeExist) {
                final String existArticleAuthorId = maybeExist.optString(Article.ARTICLE_AUTHOR_ID);

                if (!existArticleAuthorId.equals(requestJSONObject.optString(Article.ARTICLE_AUTHOR_ID))) {
                    final JSONObject existArticleAuthor = userRepository.get(existArticleAuthorId);
                    final String userName = existArticleAuthor.optString(User.USER_NAME);
                    String msg = langPropsService.get("duplicatedArticleTitleLabel");
                    msg = msg.replace("{user}", "<a target='_blank' href='/member/" + userName + "'>" + userName + "</a>");
                    msg = msg.replace("{article}", "<a target='_blank' href='/article/" + maybeExist.optString(Keys.OBJECT_ID)
                            + "'>" + articleTitle + "</a>");

                    throw new ServiceException(msg);
                }
            }
        } catch (final RepositoryException e) {
            throw new ServiceException(e);
        }

        final int articleType = requestJSONObject.optInt(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL);

        final Transaction transaction = articleRepository.beginTransaction();

        try {
            processTagsForArticleUpdate(oldArticle, requestJSONObject, author);
            userRepository.update(author.optString(Keys.OBJECT_ID), author);

            articleTitle = Emotions.toAliases(articleTitle);
            oldArticle.put(Article.ARTICLE_TITLE, articleTitle);

            oldArticle.put(Article.ARTICLE_TAGS, requestJSONObject.optString(Article.ARTICLE_TAGS));
            oldArticle.put(Article.ARTICLE_COMMENTABLE, requestJSONObject.optBoolean(Article.ARTICLE_COMMENTABLE, true));
            oldArticle.put(Article.ARTICLE_TYPE, articleType);

            String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
            articleContent = Emotions.toAliases(articleContent);
            oldArticle.put(Article.ARTICLE_CONTENT, articleContent);

            final long currentTimeMillis = System.currentTimeMillis();
            final long createTime = oldArticle.optLong(Keys.OBJECT_ID);
            oldArticle.put(Article.ARTICLE_UPDATE_TIME, currentTimeMillis);

            final int rewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT, 0);
            boolean enableReward = false;
            if (0 < rewardPoint) {
                oldArticle.put(Article.ARTICLE_REWARD_CONTENT, requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT));
                oldArticle.put(Article.ARTICLE_REWARD_POINT, rewardPoint);

                if (1 > oldArticle.optInt(Article.ARTICLE_REWARD_POINT)) {
                    enableReward = true;
                }
            }

            final String ip = requestJSONObject.optString(Article.ARTICLE_IP);
            oldArticle.put(Article.ARTICLE_IP, ip);

            String ua = requestJSONObject.optString(Article.ARTICLE_UA);
            if (StringUtils.length(ua) > Common.MAX_LENGTH_UA) {
                ua = StringUtils.substring(ua, 0, Common.MAX_LENGTH_UA);
            }
            oldArticle.put(Article.ARTICLE_UA, ua);

            final String clientArticlePermalink = requestJSONObject.optString(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK);
            oldArticle.put(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK, clientArticlePermalink);

            articleRepository.update(articleId, oldArticle);

            if (Article.ARTICLE_TYPE_C_THOUGHT != articleType) {
                // Revision
                final JSONObject revision = new JSONObject();
                revision.put(Revision.REVISION_AUTHOR_ID, authorId);
                final JSONObject revisionData = new JSONObject();
                revisionData.put(Article.ARTICLE_TITLE, articleTitle);
                revisionData.put(Article.ARTICLE_CONTENT, articleContent);
                revision.put(Revision.REVISION_DATA, revisionData.toString());
                revision.put(Revision.REVISION_DATA_ID, articleId);
                revision.put(Revision.REVISION_DATA_TYPE, Revision.DATA_TYPE_C_ARTICLE);

                revisionRepository.add(revision);
            }

            transaction.commit();

            if (!fromClient) {
                if (currentTimeMillis - createTime > 1000 * 60 * 5) {
                    pointtransferMgmtService.transfer(authorId, Pointtransfer.ID_C_SYS,
                            Pointtransfer.TRANSFER_TYPE_C_UPDATE_ARTICLE,
                            updatePointSum, articleId);
                }

                if (enableReward) {
                    pointtransferMgmtService.transfer(authorId, Pointtransfer.ID_C_SYS,
                            Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE_REWARD,
                            Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_REWARD, articleId);
                }
            }

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Common.FROM_CLIENT, fromClient);
            eventData.put(Article.ARTICLE, oldArticle);
            try {
                eventManager.fireEventAsynchronously(new Event<JSONObject>(EventTypes.UPDATE_ARTICLE, eventData));
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

            if (Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)) {
                article.put(Article.ARTICLE_TAGS, "回收站");
            }

            processTagsForArticleUpdate(oldArticle, article, author);

            String articleTitle = article.optString(Article.ARTICLE_TITLE);
            articleTitle = Emotions.toAliases(articleTitle);
            article.put(Article.ARTICLE_TITLE, articleTitle);

            String articleContent = article.optString(Article.ARTICLE_CONTENT);
            articleContent = Emotions.toAliases(articleContent);
            article.put(Article.ARTICLE_CONTENT, articleContent);

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

            livenessMgmtService.incLiveness(senderId, Liveness.LIVENESS_REWARD);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Rewards an article[id=" + articleId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Sticks an article specified by the given article id.
     *
     * @param articleId the given article id
     * @throws ServiceException service exception
     */
    public synchronized void stick(final String articleId) throws ServiceException {
        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final JSONObject article = articleRepository.get(articleId);
            if (null == article) {
                return;
            }

            final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject author = userRepository.get(authorId);
            final int balance = author.optInt(UserExt.USER_POINT);

            if (balance - Pointtransfer.TRANSFER_SUM_C_STICK_ARTICLE < 0) {
                throw new ServiceException(langPropsService.get("insufficientBalanceLabel"));
            }

            final Query query = new Query().
                    setFilter(new PropertyFilter(Article.ARTICLE_STICK, FilterOperator.GREATER_THAN, 0L));
            final JSONArray articles = articleRepository.get(query).optJSONArray(Keys.RESULTS);
            if (articles.length() > 1) {
                final Set<String> ids = new HashSet<String>();
                for (int i = 0; i < articles.length(); i++) {
                    ids.add(articles.optJSONObject(i).optString(Keys.OBJECT_ID));
                }

                if (!ids.contains(articleId)) {
                    throw new ServiceException(langPropsService.get("stickExistLabel"));
                }
            }

            article.put(Article.ARTICLE_STICK, System.currentTimeMillis());

            articleRepository.update(articleId, article);

            transaction.commit();

            final boolean succ = null != pointtransferMgmtService.transfer(article.optString(Article.ARTICLE_AUTHOR_ID),
                    Pointtransfer.ID_C_SYS, Pointtransfer.TRANSFER_TYPE_C_STICK_ARTICLE,
                    Pointtransfer.TRANSFER_SUM_C_STICK_ARTICLE, articleId);
            if (!succ) {
                throw new ServiceException(langPropsService.get("stickFailedLabel"));
            }
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Sticks an article[id=" + articleId + "] failed", e);

            throw new ServiceException(langPropsService.get("stickFailedLabel"));
        }
    }

    /**
     * Expires sticked articles.
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void expireStick() throws ServiceException {
        try {
            final Query query = new Query().
                    setFilter(new PropertyFilter(Article.ARTICLE_STICK, FilterOperator.GREATER_THAN, 0L));
            final JSONArray articles = articleRepository.get(query).optJSONArray(Keys.RESULTS);
            if (articles.length() < 1) {
                return;
            }

            final long stepTime = Symphonys.getLong("stickArticleTime");
            final long now = System.currentTimeMillis();

            for (int i = 0; i < articles.length(); i++) {
                final JSONObject article = articles.optJSONObject(i);
                final long expired = article.optLong(Article.ARTICLE_STICK) + stepTime;

                if (expired < now) {
                    article.put(Article.ARTICLE_STICK, 0L);
                    articleRepository.update(article.optString(Keys.OBJECT_ID), article);
                }
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Expires sticked articles failed", e);

            throw new ServiceException();
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
        String tagsString = newArticle.getString(Article.ARTICLE_TAGS);
        tagsString = Tag.formatTags(tagsString);
        String[] tagStrings = tagsString.split(",");

        final int articleType = newArticle.optInt(Article.ARTICLE_TYPE);
        if (tagStrings.length < GEN_TAG_MAX_CNT && Article.ARTICLE_TYPE_C_DISCUSSION != articleType
                && Article.ARTICLE_TYPE_C_THOUGHT != articleType && !Tag.containsReservedTags(tagsString)) {
            final String content = newArticle.optString(Article.ARTICLE_TITLE)
                    + " " + Jsoup.parse("<p>" + newArticle.optString(Article.ARTICLE_CONTENT) + "</p>").text();
            final List<String> genTags = tagQueryService.generateTags(content, GEN_TAG_MAX_CNT);
            if (!genTags.isEmpty()) {
                tagsString = tagsString + "," + StringUtils.join(genTags, ",");
                tagsString = Tag.formatTags(tagsString);
                tagsString = Tag.useHead(tagsString, GEN_TAG_MAX_CNT);
            }
        }

        if (StringUtils.isBlank(tagsString)) {
            tagsString = "B3log";
        }

        tagsString = Tag.formatTags(tagsString);
        newArticle.put(Article.ARTICLE_TAGS, tagsString);
        tagStrings = tagsString.split(",");

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
            tagDropped.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

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
            } else if (tagIdList.contains(tagArticleRelation.getString(Tag.TAG + "_" + Keys.OBJECT_ID))) {
                relationId = tagArticleRelation.getString(Keys.OBJECT_ID);
                tagArticleRepository.remove(relationId);
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
     * @param tagTitles the specified (new) tag titles
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
                tag.put(Tag.TAG_SEO_TITLE, tagTitle);
                tag.put(Tag.TAG_SEO_KEYWORDS, tagTitle);
                tag.put(Tag.TAG_SEO_DESC, "");
                tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

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

                tagTmp.put(Tag.TAG_TITLE, title);
                tagTmp.put(Tag.TAG_COMMENT_CNT, tag.optInt(Tag.TAG_COMMENT_CNT) + articleCmtCnt);
                tagTmp.put(Tag.TAG_STATUS, tag.optInt(Tag.TAG_STATUS));
                tagTmp.put(Tag.TAG_REFERENCE_CNT, tag.optInt(Tag.TAG_REFERENCE_CNT) + 1);
                tagTmp.put(Tag.TAG_FOLLOWER_CNT, tag.optInt(Tag.TAG_FOLLOWER_CNT));
                tagTmp.put(Tag.TAG_DESCRIPTION, tag.optString(Tag.TAG_DESCRIPTION));
                tagTmp.put(Tag.TAG_ICON_PATH, tag.optString(Tag.TAG_ICON_PATH));
                tagTmp.put(Tag.TAG_GOOD_CNT, tag.optInt(Tag.TAG_GOOD_CNT));
                tagTmp.put(Tag.TAG_BAD_CNT, tag.optInt(Tag.TAG_BAD_CNT));
                tagTmp.put(Tag.TAG_SEO_DESC, tag.optString(Tag.TAG_SEO_DESC));
                tagTmp.put(Tag.TAG_SEO_KEYWORDS, tag.optString(Tag.TAG_SEO_KEYWORDS));
                tagTmp.put(Tag.TAG_SEO_TITLE, tag.optString(Tag.TAG_SEO_TITLE));
                tagTmp.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

                tagRepository.update(tagId, tagTmp);

                userTagType = Tag.TAG_TYPE_C_ARTICLE;
            }

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

        final String[] tags = articleTags.split(",");
        final StringBuilder builder = new StringBuilder();
        for (final String tagTitle : tags) {
            final JSONObject tag = tagRepository.getByTitle(tagTitle);

            builder.append(tag.optString(Tag.TAG_TITLE)).append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        article.put(Article.ARTICLE_TAGS, builder.toString());
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

    /**
     * Adds an article with the specified request json object.
     *
     * <p>
     * <b>Note</b>: This method just for admin console.
     * </p>
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "articleTitle": "",
     *     "articleTags": "",
     *     "articleContent": "",
     *     "userName": "",
     *     "time": long
     * }
     * </pre>, see {@link Article} for more details
     *
     * @return generated article id
     * @throws ServiceException service exception
     */
    public synchronized String addArticleByAdmin(final JSONObject requestJSONObject) throws ServiceException {
        JSONObject author;

        try {
            author = userRepository.getByName(requestJSONObject.optString(User.USER_NAME));
            if (null == author) {
                throw new ServiceException(langPropsService.get("notFoundUserLabel"));
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.DEBUG, "Admin adds article failed", e);

            throw new ServiceException(e.getMessage());
        }

        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final long time = requestJSONObject.optLong(Common.TIME);
            final String ret = String.valueOf(time);
            final JSONObject article = new JSONObject();
            article.put(Keys.OBJECT_ID, ret);
            article.put(Article.ARTICLE_CLIENT_ARTICLE_ID, ret);
            article.put(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK, "");
            article.put(Article.ARTICLE_AUTHOR_ID, author.optString(Keys.OBJECT_ID));
            article.put(Article.ARTICLE_AUTHOR_EMAIL, author.optString(User.USER_EMAIL));
            article.put(Article.ARTICLE_TITLE, Emotions.toAliases(requestJSONObject.optString(Article.ARTICLE_TITLE)));
            article.put(Article.ARTICLE_CONTENT, Emotions.toAliases(requestJSONObject.optString(Article.ARTICLE_CONTENT)));
            article.put(Article.ARTICLE_REWARD_CONTENT, "");
            article.put(Article.ARTICLE_EDITOR_TYPE, 0);
            article.put(Article.ARTICLE_SYNC_TO_CLIENT, false);
            article.put(Article.ARTICLE_COMMENT_CNT, 0);
            article.put(Article.ARTICLE_VIEW_CNT, 0);
            article.put(Article.ARTICLE_GOOD_CNT, 0);
            article.put(Article.ARTICLE_BAD_CNT, 0);
            article.put(Article.ARTICLE_COLLECT_CNT, 0);
            article.put(Article.ARTICLE_COMMENTABLE, true);
            article.put(Article.ARTICLE_CREATE_TIME, time);
            article.put(Article.ARTICLE_UPDATE_TIME, time);
            article.put(Article.ARTICLE_LATEST_CMT_TIME, 0);
            article.put(Article.ARTICLE_LATEST_CMTER_NAME, "");
            article.put(Article.ARTICLE_PERMALINK, "/article/" + ret);
            article.put(Article.ARTICLE_RANDOM_DOUBLE, Math.random());
            article.put(Article.REDDIT_SCORE, 0);
            article.put(Article.ARTICLE_STATUS, Article.ARTICLE_STATUS_C_VALID);
            article.put(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL);
            article.put(Article.ARTICLE_REWARD_POINT, 0);
            article.put(Article.ARTICLE_CITY, "");
            String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
            articleTags = Tag.formatTags(articleTags);
            String[] tagTitles = articleTags.split(",");
            if (tagTitles.length < GEN_TAG_MAX_CNT && !Tag.containsReservedTags(articleTags)) {
                final String content = article.optString(Article.ARTICLE_TITLE)
                        + " " + Jsoup.parse("<p>" + article.optString(Article.ARTICLE_CONTENT) + "</p>").text();
                final List<String> genTags = tagQueryService.generateTags(content, GEN_TAG_MAX_CNT);
                if (!genTags.isEmpty()) {
                    articleTags = articleTags + "," + StringUtils.join(genTags, ",");
                    articleTags = Tag.formatTags(articleTags);
                    articleTags = Tag.useHead(articleTags, GEN_TAG_MAX_CNT);
                }
            }

            if (StringUtils.isBlank(articleTags)) {
                articleTags = "B3log";
            }

            articleTags = Tag.formatTags(articleTags);
            article.put(Article.ARTICLE_TAGS, articleTags);
            tagTitles = articleTags.split(",");

            tag(tagTitles, article, author);

            final String ip = requestJSONObject.optString(Article.ARTICLE_IP);
            article.put(Article.ARTICLE_IP, ip);

            String ua = requestJSONObject.optString(Article.ARTICLE_UA);
            if (StringUtils.length(ua) > Common.MAX_LENGTH_UA) {
                ua = StringUtils.substring(ua, 0, Common.MAX_LENGTH_UA);
            }
            article.put(Article.ARTICLE_UA, ua);

            article.put(Article.ARTICLE_STICK, 0L);

            final JSONObject articleCntOption = optionRepository.get(Option.ID_C_STATISTIC_ARTICLE_COUNT);
            final int articleCnt = articleCntOption.optInt(Option.OPTION_VALUE);
            articleCntOption.put(Option.OPTION_VALUE, articleCnt + 1);
            optionRepository.update(Option.ID_C_STATISTIC_ARTICLE_COUNT, articleCntOption);

            author.put(UserExt.USER_ARTICLE_COUNT, author.optInt(UserExt.USER_ARTICLE_COUNT) + 1);
            author.put(UserExt.USER_LATEST_ARTICLE_TIME, time);
            // Updates user article count (and new tag count), latest article time
            userRepository.update(author.optString(Keys.OBJECT_ID), author);

            final String articleId = articleRepository.add(article);

            // Revision
            final JSONObject revision = new JSONObject();
            revision.put(Revision.REVISION_AUTHOR_ID, author.optString(Keys.OBJECT_ID));
            final JSONObject revisionData = new JSONObject();
            revisionData.put(Article.ARTICLE_TITLE, article.optString(Article.ARTICLE_TITLE));
            revisionData.put(Article.ARTICLE_CONTENT, article.optString(Article.ARTICLE_CONTENT));
            revision.put(Revision.REVISION_DATA, revisionData.toString());
            revision.put(Revision.REVISION_DATA_ID, articleId);
            revision.put(Revision.REVISION_DATA_TYPE, Revision.DATA_TYPE_C_ARTICLE);

            revisionRepository.add(revision);

            transaction.commit();

            // Grows the tag graph
            tagMgmtService.relateTags(article.optString(Article.ARTICLE_TAGS));

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Common.FROM_CLIENT, false);
            eventData.put(Article.ARTICLE, article);
            try {
                eventManager.fireEventAsynchronously(new Event<JSONObject>(EventTypes.ADD_ARTICLE, eventData));
            } catch (final EventException e) {
                LOGGER.log(Level.ERROR, e.getMessage(), e);
            }

            return ret;
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Admin adds an article failed", e);
            throw new ServiceException(e.getMessage());
        }
    }
}
