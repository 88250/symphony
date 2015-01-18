/*
 * Copyright (c) 2015, b3log.org
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
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.NotificationRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Thumbnails;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Notification query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.3, Nov 14, 2013
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
     * Gets the count of unread notifications of an user specified with the given user id.
     *
     * @param userId the given user id
     * @return count of unread notifications, returns {@code 0} if occurs exception
     */
    // XXX: Performance Issue: count without result list
    public int getUnreadNotificationCount(final String userId) {
        final List<Filter> filters = new ArrayList<Filter>();

        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false));

        final Query query = new Query();
        query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).addProjection(Keys.OBJECT_ID, String.class);

        try {
            final JSONObject result = notificationRepository.get(query);

            return result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets unread notification count failed [userId=" + userId + "]", e);

            return 0;
        }
    }

    /**
     * Gets the count of unread notifications with the specified data of an user specified with the given user id.
     *
     * @param userId the given user id
     * @param notificationDataType the specified notification data type
     * @return count of unread notifications, returns {@code 0} if occurs exception
     * @see Notification#DATA_TYPE_C_ARTICLE
     * @see Notification#DATA_TYPE_C_AT
     * @see Notification#DATA_TYPE_C_COMMENT
     * @see Notification#DATA_TYPE_C_COMMENTED
     */
    // XXX: Performance Issue: count type notification
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
     * Gets 'commented' type notifications with the specified user id, current page number and page size.
     *
     * @param userId the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return result json object, for example,
     * <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "commentAuthorName": "",
     *         "commentContent": "",
     *         "commentAuthorThumbnailURL": "",
     *         "commentArticleTitle": "",
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

                final JSONObject comment = commentQueryService.getComment(commentId);

                final Query q = new Query().setPageCount(1).addProjection(Article.ARTICLE_TITLE, String.class).
                        setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL,
                                                     comment.optString(Comment.COMMENT_ON_ARTICLE_ID)));
                final JSONArray rlts = articleRepository.get(q).optJSONArray(Keys.RESULTS);
                final JSONObject article = rlts.optJSONObject(0);
                final String articleTitle = article.optString(Article.ARTICLE_TITLE);

                final JSONObject commentedNotification = new JSONObject();
                commentedNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                commentedNotification.put(Comment.COMMENT_T_AUTHOR_NAME, comment.optString(Comment.COMMENT_T_AUTHOR_NAME));
                commentedNotification.put(Comment.COMMENT_CONTENT, comment.optString(Comment.COMMENT_CONTENT));
                commentedNotification.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL,
                                          comment.optString(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL));
                commentedNotification.put(Comment.COMMENT_T_ARTICLE_TITLE, articleTitle);
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
     * @return result json object, for example,
     * <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "authorName": "",
     *         "content": "",
     *         "thumbnailURL": "",
     *         "articleTitle": "",
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

                final JSONObject comment = commentQueryService.getComment(commentId);
                if (null != comment) {
                    final Query q = new Query().setPageCount(1).addProjection(Article.ARTICLE_TITLE, String.class).
                            setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL,
                                                         comment.optString(Comment.COMMENT_ON_ARTICLE_ID)));
                    final JSONArray rlts = articleRepository.get(q).optJSONArray(Keys.RESULTS);
                    final JSONObject article = rlts.optJSONObject(0);
                    final String articleTitle = article.optString(Article.ARTICLE_TITLE);

                    final JSONObject atNotification = new JSONObject();
                    atNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                    atNotification.put(Common.AUTHOR_NAME, comment.optString(Comment.COMMENT_T_AUTHOR_NAME));
                    atNotification.put(Common.CONTENT, comment.optString(Comment.COMMENT_CONTENT));
                    atNotification.put(Common.THUMBNAIL_URL,
                                       comment.optString(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL));
                    atNotification.put(Common.ARTICLE_TITLE, articleTitle);
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
                    final String thumbnailURL = Thumbnails.getGravatarURL(articleAuthor.optString(User.USER_EMAIL), "140");
                    atNotification.put(Common.THUMBNAIL_URL, thumbnailURL);
                    atNotification.put(Common.ARTICLE_TITLE, article.optString(Article.ARTICLE_TITLE));
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
     * @return result json object, for example,
     * <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "authorName": "",
     *         "content": "",
     *         "thumbnailURL": "",
     *         "articleTitle": "",
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

                final Query q = new Query().setPageCount(1).addProjection(Article.ARTICLE_TITLE, String.class).
                        addProjection(Article.ARTICLE_AUTHOR_EMAIL, String.class).
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
                final String articleAuthorEmail = article.optString(Article.ARTICLE_AUTHOR_EMAIL);
                final JSONObject author = userRepository.getByEmail(articleAuthorEmail);

                if (null == author) {
                    LOGGER.warn("Not found user[email=" + articleAuthorEmail + ']');

                    continue;
                }

                final JSONObject followingUserNotification = new JSONObject();
                followingUserNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                followingUserNotification.put(Common.AUTHOR_NAME, author.optString(User.USER_NAME));
                followingUserNotification.put(Common.CONTENT, "");
                followingUserNotification.put(Common.THUMBNAIL_URL, Thumbnails.getGravatarURL(articleAuthorEmail, "140"));
                followingUserNotification.put(Common.ARTICLE_TITLE, articleTitle);
                followingUserNotification.put(Common.URL, article.optString(Article.ARTICLE_PERMALINK));
                followingUserNotification.put(Common.CREATE_TIME, new Date(article.optLong(Article.ARTICLE_CREATE_TIME)));
                followingUserNotification.put(Notification.NOTIFICATION_HAS_READ,
                                              notification.optBoolean(Notification.NOTIFICATION_HAS_READ));
                followingUserNotification.put(Common.TYPE, Article.ARTICLE);
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
}
