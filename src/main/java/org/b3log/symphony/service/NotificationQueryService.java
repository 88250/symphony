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
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.CompositeFilter;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.Filter;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.Reward;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.CommentRepository;
import org.b3log.symphony.repository.NotificationRepository;
import org.b3log.symphony.repository.PointtransferRepository;
import org.b3log.symphony.repository.RewardRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Emotions;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Notification query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.4.2.5, Apr 18, 2016
 * @since 0.2.5
 */
@Service
public class NotificationQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(NotificationQueryService.class.getName());

    /**
     * Notification repository.
     */
    @Inject
    private NotificationRepository notificationRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Comment repository.
     */
    @Inject
    private CommentRepository commentRepository;

    /**
     * Reward repository.
     */
    @Inject
    private RewardRepository rewardRepository;

    /**
     * Pointtransfer repository.
     */
    @Inject
    private PointtransferRepository pointtransferRepository;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Gets the count of unread notifications of a user specified with the given user id.
     *
     * @param userId the given user id
     * @return count of unread notifications, returns {@code 0} if occurs exception
     */
    public int getUnreadNotificationCount(final String userId) {
        Stopwatchs.start("Gets unread notification count");
        try {
            final Query query = new Query();
            query.setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId),
                    new PropertyFilter(Notification.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false)
            ));

            try {
                return (int) notificationRepository.count(query);
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Gets unread notification count failed [userId=" + userId + "]", e);

                return 0;
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the count of unread notifications of a user specified with the given user id and data type.
     *
     * @param userId the given user id
     * @param notificationDataType the specified notification data type
     * @return count of unread notifications, returns {@code 0} if occurs exception
     * @see Notification#DATA_TYPE_C_ARTICLE
     * @see Notification#DATA_TYPE_C_AT
     * @see Notification#DATA_TYPE_C_COMMENT
     * @see Notification#DATA_TYPE_C_COMMENTED
     * @see Notification#DATA_TYPE_C_BROADCAST
     */
    public int getUnreadNotificationCountByType(final String userId, final int notificationDataType) {
        final List<Filter> filters = new ArrayList<Filter>();

        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, notificationDataType));

        final Query query = new Query();
        query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).addProjection(Keys.OBJECT_ID, String.class);

        try {
            final JSONObject result = notificationRepository.get(query);

            return result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets [commented] notification count failed [userId=" + userId + "]", e);

            return 0;
        }
    }

    /**
     * Gets the count of unread 'point' notifications of a user specified with the given user id.
     *
     * @param userId the given user id
     * @return count of unread notifications, returns {@code 0} if occurs exception
     * @see Notification#DATA_TYPE_C_POINT_ARTICLE_REWARD
     * @see Notification#DATA_TYPE_C_POINT_CHARGE
     * @see Notification#DATA_TYPE_C_POINT_EXCHANGE
     * @see Notification#DATA_TYPE_C_ABUSE_POINT_DEDUCT
     * @see Notification#DATA_TYPE_C_POINT_COMMENT_THANK
     * @see Notification#DATA_TYPE_C_POINT_TRANSFER
     */
    public int getUnreadPointNotificationCount(final String userId) {
        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false));

        final List<Filter> subFilters = new ArrayList<Filter>();
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_POINT_ARTICLE_REWARD));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_POINT_CHARGE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_POINT_EXCHANGE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_ABUSE_POINT_DEDUCT));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_POINT_COMMENT_THANK));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_POINT_TRANSFER));

        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));

        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            final JSONObject result = notificationRepository.get(query);

            return result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets [commented] notification count failed [userId=" + userId + "]", e);

            return 0;
        }
    }

    /**
     * Gets 'point' type notifications with the specified user id, current page number and page size.
     *
     * @param userId the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "description": int,
     *         "hasRead": boolean
     *     }, ....]
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    public JSONObject getPointNotifications(final String userId, final int currentPageNum, final int pageSize)
            throws ServiceException {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<JSONObject>();

        ret.put(Keys.RESULTS, (Object) rslts);

        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));

        final List<Filter> subFilters = new ArrayList<Filter>();
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_POINT_ARTICLE_REWARD));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_POINT_CHARGE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_POINT_EXCHANGE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_ABUSE_POINT_DEDUCT));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_POINT_COMMENT_THANK));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_POINT_TRANSFER));

        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));

        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                addSort(Notification.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONObject queryResult = notificationRepository.get(query);
            final JSONArray results = queryResult.optJSONArray(Keys.RESULTS);

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT));

            for (int i = 0; i < results.length(); i++) {
                final JSONObject notification = results.optJSONObject(i);
                final String dataId = notification.optString(Notification.NOTIFICATION_DATA_ID);
                final int dataType = notification.optInt(Notification.NOTIFICATION_DATA_TYPE);
                String desTemplate = "";

                switch (dataType) {
                    case Notification.DATA_TYPE_C_POINT_ARTICLE_REWARD:
                        desTemplate = langPropsService.get("notificationArticleRewardLabel");

                        final JSONObject reward7 = rewardRepository.get(dataId);
                        final String senderId7 = reward7.optString(Reward.SENDER_ID);
                        final JSONObject user7 = userRepository.get(senderId7);
                        final String articleId7 = reward7.optString(Reward.DATA_ID);
                        final JSONObject article7 = articleRepository.get(articleId7);

                        final String userLink7 = "<a href=\"/member/" + user7.optString(User.USER_NAME) + "\">"
                                + user7.optString(User.USER_NAME) + "</a>";
                        desTemplate = desTemplate.replace("{user}", userLink7);

                        final String articleLink7 = "<a href=\""
                                + article7.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + article7.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink7);

                        break;
                    case Notification.DATA_TYPE_C_POINT_CHARGE:
                        desTemplate = langPropsService.get("notificationPointChargeLabel");

                        final JSONObject transfer5 = pointtransferRepository.get(dataId);
                        final int sum5 = transfer5.optInt(Pointtransfer.SUM);
                        final String memo5 = transfer5.optString(Pointtransfer.DATA_ID);
                        final String yuan = memo5.split("-")[0];

                        desTemplate = desTemplate.replace("{yuan}", yuan);
                        desTemplate = desTemplate.replace("{point}", String.valueOf(sum5));

                        break;
                    case Notification.DATA_TYPE_C_POINT_EXCHANGE:
                        desTemplate = langPropsService.get("notificationPointExchangeLabel");

                        final JSONObject transfer6 = pointtransferRepository.get(dataId);
                        final int sum6 = transfer6.optInt(Pointtransfer.SUM);
                        final String yuan6 = transfer6.optString(Pointtransfer.DATA_ID);

                        desTemplate = desTemplate.replace("{yuan}", yuan6);
                        desTemplate = desTemplate.replace("{point}", String.valueOf(sum6));

                        break;
                    case Notification.DATA_TYPE_C_ABUSE_POINT_DEDUCT:
                        desTemplate = langPropsService.get("notificationAbusePointDeductLabel");

                        final JSONObject transfer7 = pointtransferRepository.get(dataId);
                        final int sum7 = transfer7.optInt(Pointtransfer.SUM);
                        final String memo7 = transfer7.optString(Pointtransfer.DATA_ID);

                        desTemplate = desTemplate.replace("{action}", memo7);
                        desTemplate = desTemplate.replace("{point}", String.valueOf(sum7));

                        break;
                    case Notification.DATA_TYPE_C_POINT_COMMENT_THANK:
                        desTemplate = langPropsService.get("notificationCmtThankLabel");

                        final JSONObject reward8 = rewardRepository.get(dataId);
                        final String senderId8 = reward8.optString(Reward.SENDER_ID);
                        final JSONObject user8 = userRepository.get(senderId8);
                        final JSONObject comment8 = commentRepository.get(reward8.optString(Reward.DATA_ID));
                        final String articleId8 = comment8.optString(Comment.COMMENT_ON_ARTICLE_ID);
                        final JSONObject article8 = articleRepository.get(articleId8);

                        final String userLink8 = "<a href=\"/member/" + user8.optString(User.USER_NAME) + "\">"
                                + user8.optString(User.USER_NAME) + "</a>";
                        desTemplate = desTemplate.replace("{user}", userLink8);

                        final String articleLink8 = "<a href=\""
                                + article8.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + article8.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink8);

                        break;
                    case Notification.DATA_TYPE_C_POINT_TRANSFER:
                        desTemplate = langPropsService.get("notificationPointTransferLabel");

                        final JSONObject transfer101 = pointtransferRepository.get(dataId);
                        final String fromId101 = transfer101.optString(Pointtransfer.FROM_ID);
                        final JSONObject user101 = userRepository.get(fromId101);
                        final int sum101 = transfer101.optInt(Pointtransfer.SUM);

                        final String userLink101 = "<a href=\"/member/" + user101.optString(User.USER_NAME) + "\">"
                                + user101.optString(User.USER_NAME) + "</a>";
                        desTemplate = desTemplate.replace("{user}", userLink101);
                        desTemplate = desTemplate.replace("{amount}", String.valueOf(sum101));

                        break;
                    default:
                        throw new AssertionError();
                }

                notification.put(Common.DESCRIPTION, desTemplate);

                rslts.add(notification);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets [commented] notifications", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets 'commented' type notifications with the specified user id, current page number and page size.
     *
     * @param userId the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "commentAuthorName": "",
     *         "commentContent": "",
     *         "commentAuthorThumbnailURL": "",
     *         "commentArticleTitle": "",
     *         "commentArticleType": int,
     *         "commentSharpURL": "",
     *         "commentCreateTime": java.util.Date,
     *         "hasRead": boolean
     *     }, ....]
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    public JSONObject getCommentedNotifications(final String userId, final int currentPageNum, final int pageSize)
            throws ServiceException {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<JSONObject>();

        ret.put(Keys.RESULTS, (Object) rslts);

        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_COMMENTED));

        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                addSort(Notification.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONObject queryResult = notificationRepository.get(query);
            final JSONArray results = queryResult.optJSONArray(Keys.RESULTS);

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT));

            for (int i = 0; i < results.length(); i++) {
                final JSONObject notification = results.optJSONObject(i);
                final String commentId = notification.optString(Notification.NOTIFICATION_DATA_ID);

                final JSONObject comment = commentQueryService.getCommentById(commentId);

                final Query q = new Query().setPageCount(1).
                        addProjection(Article.ARTICLE_TITLE, String.class).
                        addProjection(Article.ARTICLE_TYPE, Integer.class).
                        setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL,
                                comment.optString(Comment.COMMENT_ON_ARTICLE_ID)));
                final JSONArray rlts = articleRepository.get(q).optJSONArray(Keys.RESULTS);
                final JSONObject article = rlts.optJSONObject(0);
                final String articleTitle = article.optString(Article.ARTICLE_TITLE);
                final int articleType = article.optInt(Article.ARTICLE_TYPE);

                final JSONObject commentedNotification = new JSONObject();
                commentedNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                commentedNotification.put(Comment.COMMENT_T_AUTHOR_NAME, comment.optString(Comment.COMMENT_T_AUTHOR_NAME));
                commentedNotification.put(Comment.COMMENT_CONTENT, comment.optString(Comment.COMMENT_CONTENT));
                commentedNotification.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL,
                        comment.optString(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL));
                commentedNotification.put(Common.THUMBNAIL_UPDATE_TIME, comment.optJSONObject(Comment.COMMENT_T_COMMENTER).
                        optLong(UserExt.USER_UPDATE_TIME));
                commentedNotification.put(Comment.COMMENT_T_ARTICLE_TITLE, Emotions.convert(articleTitle));
                commentedNotification.put(Comment.COMMENT_T_ARTICLE_TYPE, articleType);
                commentedNotification.put(Comment.COMMENT_SHARP_URL, comment.optString(Comment.COMMENT_SHARP_URL));
                commentedNotification.put(Comment.COMMENT_CREATE_TIME, comment.opt(Comment.COMMENT_CREATE_TIME));
                commentedNotification.put(Notification.NOTIFICATION_HAS_READ, notification.optBoolean(Notification.NOTIFICATION_HAS_READ));

                rslts.add(commentedNotification);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets [commented] notifications", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets 'at' type notifications with the specified user id, current page number and page size.
     *
     * @param userId the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "authorName": "",
     *         "content": "",
     *         "thumbnailURL": "",
     *         "articleTitle": "",
     *         "articleType": int,
     *         "url": "",
     *         "createTime": java.util.Date,
     *         "hasRead": boolean,
     *         "atInArticle": boolean,
     *         "articleTags": "", // if atInArticle is true
     *         "articleCommentCnt": int // if atInArticle is true
     *     }, ....]
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    public JSONObject getAtNotifications(final String userId, final int currentPageNum, final int pageSize)
            throws ServiceException {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<JSONObject>();

        ret.put(Keys.RESULTS, (Object) rslts);

        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_AT));

        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                addSort(Notification.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONObject queryResult = notificationRepository.get(query);
            final JSONArray results = queryResult.optJSONArray(Keys.RESULTS);

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT));

            for (int i = 0; i < results.length(); i++) {
                final JSONObject notification = results.optJSONObject(i);
                final String commentId = notification.optString(Notification.NOTIFICATION_DATA_ID);

                final JSONObject comment = commentQueryService.getCommentById(commentId);
                if (null != comment) {
                    final Query q = new Query().setPageCount(1).
                            addProjection(Article.ARTICLE_TITLE, String.class).
                            addProjection(Article.ARTICLE_TYPE, Integer.class).
                            setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL,
                                    comment.optString(Comment.COMMENT_ON_ARTICLE_ID)));
                    final JSONArray rlts = articleRepository.get(q).optJSONArray(Keys.RESULTS);
                    final JSONObject article = rlts.optJSONObject(0);
                    final String articleTitle = article.optString(Article.ARTICLE_TITLE);
                    final int articleType = article.optInt(Article.ARTICLE_TYPE);

                    final JSONObject atNotification = new JSONObject();
                    atNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                    atNotification.put(Common.AUTHOR_NAME, comment.optString(Comment.COMMENT_T_AUTHOR_NAME));
                    atNotification.put(Common.CONTENT, comment.optString(Comment.COMMENT_CONTENT));
                    atNotification.put(Common.THUMBNAIL_URL,
                            comment.optString(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL));
                    atNotification.put(Common.THUMBNAIL_UPDATE_TIME, comment.optJSONObject(Comment.COMMENT_T_COMMENTER).
                            optLong(UserExt.USER_UPDATE_TIME));
                    atNotification.put(Article.ARTICLE_TITLE, Emotions.convert(articleTitle));
                    atNotification.put(Article.ARTICLE_TYPE, articleType);
                    atNotification.put(Common.URL, comment.optString(Comment.COMMENT_SHARP_URL));
                    atNotification.put(Common.CREATE_TIME, comment.opt(Comment.COMMENT_CREATE_TIME));
                    atNotification.put(Notification.NOTIFICATION_HAS_READ, notification.optBoolean(Notification.NOTIFICATION_HAS_READ));
                    atNotification.put(Notification.NOTIFICATION_T_AT_IN_ARTICLE, false);

                    rslts.add(atNotification);
                } else { // The 'at' in article content
                    final JSONObject article = articleRepository.get(commentId);

                    final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                    final JSONObject articleAuthor = userRepository.get(articleAuthorId);

                    final JSONObject atNotification = new JSONObject();
                    atNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                    atNotification.put(Common.AUTHOR_NAME, articleAuthor.optString(User.USER_NAME));
                    atNotification.put(Common.CONTENT, "");
                    final String thumbnailURL = avatarQueryService.getAvatarURLByUser(articleAuthor);
                    atNotification.put(Common.THUMBNAIL_URL, thumbnailURL);
                    atNotification.put(Common.THUMBNAIL_UPDATE_TIME, articleAuthor.optLong(UserExt.USER_UPDATE_TIME));
                    atNotification.put(Article.ARTICLE_TITLE, Emotions.convert(article.optString(Article.ARTICLE_TITLE)));
                    atNotification.put(Article.ARTICLE_TYPE, article.optInt(Article.ARTICLE_TYPE));
                    atNotification.put(Common.URL, article.optString(Article.ARTICLE_PERMALINK));
                    atNotification.put(Common.CREATE_TIME, new Date(article.optLong(Article.ARTICLE_CREATE_TIME)));
                    atNotification.put(Notification.NOTIFICATION_HAS_READ, notification.optBoolean(Notification.NOTIFICATION_HAS_READ));
                    atNotification.put(Notification.NOTIFICATION_T_AT_IN_ARTICLE, true);
                    atNotification.put(Article.ARTICLE_TAGS, article.optString(Article.ARTICLE_TAGS));
                    atNotification.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT));

                    rslts.add(atNotification);
                }
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets [at] notifications", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Gets 'followingUser' type notifications with the specified user id, current page number and page size.
     *
     * @param userId the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "authorName": "",
     *         "content": "",
     *         "thumbnailURL": "",
     *         "articleTitle": "",
     *         "articleType": int,
     *         "articleTags": "",
     *         "articleCommentCnt": int,
     *         "url": "",
     *         "createTime": java.util.Date,
     *         "hasRead": boolean,
     *         "type": "", // article/comment
     *     }, ....]
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    public JSONObject getFollowingUserNotifications(final String userId, final int currentPageNum, final int pageSize)
            throws ServiceException {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<JSONObject>();

        ret.put(Keys.RESULTS, (Object) rslts);

        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_FOLLOWING_USER));

        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                addSort(Notification.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONObject queryResult = notificationRepository.get(query);
            final JSONArray results = queryResult.optJSONArray(Keys.RESULTS);

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT));

            for (int i = 0; i < results.length(); i++) {
                final JSONObject notification = results.optJSONObject(i);
                final String articleId = notification.optString(Notification.NOTIFICATION_DATA_ID);

                final Query q = new Query().setPageCount(1).
                        addProjection(Article.ARTICLE_TITLE, String.class).
                        addProjection(Article.ARTICLE_TYPE, Integer.class).
                        addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
                        addProjection(Article.ARTICLE_PERMALINK, String.class).
                        addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
                        addProjection(Article.ARTICLE_TAGS, String.class).
                        addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
                        setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, articleId));
                final JSONArray rlts = articleRepository.get(q).optJSONArray(Keys.RESULTS);
                final JSONObject article = rlts.optJSONObject(0);

                if (null == article) {
                    LOGGER.warn("Not found article[id=" + articleId + ']');

                    continue;
                }

                final String articleTitle = article.optString(Article.ARTICLE_TITLE);
                final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                final JSONObject author = userRepository.get(articleAuthorId);

                if (null == author) {
                    LOGGER.warn("Not found user[id=" + articleAuthorId + ']');

                    continue;
                }

                final JSONObject followingUserNotification = new JSONObject();
                followingUserNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                followingUserNotification.put(Common.AUTHOR_NAME, author.optString(User.USER_NAME));
                followingUserNotification.put(Common.CONTENT, "");
                followingUserNotification.put(Common.THUMBNAIL_URL, avatarQueryService.getAvatarURLByUser(author));
                followingUserNotification.put(Common.THUMBNAIL_UPDATE_TIME, author.optLong(UserExt.USER_UPDATE_TIME));
                followingUserNotification.put(Article.ARTICLE_TITLE, Emotions.convert(articleTitle));
                followingUserNotification.put(Common.URL, article.optString(Article.ARTICLE_PERMALINK));
                followingUserNotification.put(Common.CREATE_TIME, new Date(article.optLong(Article.ARTICLE_CREATE_TIME)));
                followingUserNotification.put(Notification.NOTIFICATION_HAS_READ,
                        notification.optBoolean(Notification.NOTIFICATION_HAS_READ));
                followingUserNotification.put(Common.TYPE, Article.ARTICLE);
                followingUserNotification.put(Article.ARTICLE_TYPE, article.optInt(Article.ARTICLE_TYPE));
                followingUserNotification.put(Article.ARTICLE_TAGS, article.optString(Article.ARTICLE_TAGS));
                followingUserNotification.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT));

                rslts.add(followingUserNotification);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets [followingUser] notifications", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Gets 'broadcast' type notifications with the specified user id, current page number and page size.
     *
     * @param userId the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "authorName": "",
     *         "content": "",
     *         "thumbnailURL": "",
     *         "articleTitle": "",
     *         "articleType": int,
     *         "articleTags": "",
     *         "articleCommentCnt": int,
     *         "url": "",
     *         "createTime": java.util.Date,
     *         "hasRead": boolean,
     *         "type": "", // article/comment
     *     }, ....]
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    public JSONObject getBroadcastNotifications(final String userId, final int currentPageNum, final int pageSize)
            throws ServiceException {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<JSONObject>();

        ret.put(Keys.RESULTS, (Object) rslts);

        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_BROADCAST));

        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                addSort(Notification.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONObject queryResult = notificationRepository.get(query);
            final JSONArray results = queryResult.optJSONArray(Keys.RESULTS);

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT));

            for (int i = 0; i < results.length(); i++) {
                final JSONObject notification = results.optJSONObject(i);
                final String articleId = notification.optString(Notification.NOTIFICATION_DATA_ID);

                final Query q = new Query().setPageCount(1).
                        addProjection(Article.ARTICLE_TITLE, String.class).
                        addProjection(Article.ARTICLE_TYPE, Integer.class).
                        addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
                        addProjection(Article.ARTICLE_PERMALINK, String.class).
                        addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
                        addProjection(Article.ARTICLE_TAGS, String.class).
                        addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
                        setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, articleId));
                final JSONArray rlts = articleRepository.get(q).optJSONArray(Keys.RESULTS);
                final JSONObject article = rlts.optJSONObject(0);

                if (null == article) {
                    LOGGER.warn("Not found article[id=" + articleId + ']');

                    continue;
                }

                final String articleTitle = article.optString(Article.ARTICLE_TITLE);
                final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                final JSONObject author = userRepository.get(articleAuthorId);

                if (null == author) {
                    LOGGER.warn("Not found user[id=" + articleAuthorId + ']');

                    continue;
                }

                final JSONObject broadcastNotification = new JSONObject();
                broadcastNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                broadcastNotification.put(Common.AUTHOR_NAME, author.optString(User.USER_NAME));
                broadcastNotification.put(Common.CONTENT, "");
                broadcastNotification.put(Common.THUMBNAIL_URL, avatarQueryService.getAvatarURLByUser(author));
                broadcastNotification.put(Common.THUMBNAIL_UPDATE_TIME, author.optLong(UserExt.USER_UPDATE_TIME));
                broadcastNotification.put(Article.ARTICLE_TITLE, articleTitle);
                broadcastNotification.put(Common.URL, article.optString(Article.ARTICLE_PERMALINK));
                broadcastNotification.put(Common.CREATE_TIME, new Date(article.optLong(Article.ARTICLE_CREATE_TIME)));
                broadcastNotification.put(Notification.NOTIFICATION_HAS_READ,
                        notification.optBoolean(Notification.NOTIFICATION_HAS_READ));
                broadcastNotification.put(Common.TYPE, Article.ARTICLE);
                broadcastNotification.put(Article.ARTICLE_TYPE, article.optInt(Article.ARTICLE_TYPE));
                broadcastNotification.put(Article.ARTICLE_TAGS, article.optString(Article.ARTICLE_TAGS));
                broadcastNotification.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT));

                rslts.add(broadcastNotification);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets [broadcast] notifications", e);

            throw new ServiceException(e);
        }
    }
}
