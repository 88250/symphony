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
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.Follow;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.FollowRepository;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.repository.UserRepository;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Follow query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.4.0.7, May 23, 2018
 * @since 0.2.5
 */
@Service
public class FollowQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FollowQueryService.class);

    /**
     * Follow repository.
     */
    @Inject
    private FollowRepository followRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Tag repository.
     */
    @Inject
    private TagRepository tagRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Determines whether exists a follow relationship for the specified follower and the specified following entity.
     *
     * @param followerId    the specified follower id
     * @param followingId   the specified following entity id
     * @param followingType the specified following type
     * @return {@code true} if exists, returns {@code false} otherwise
     */
    public boolean isFollowing(final String followerId, final String followingId, final int followingType) {
        Stopwatchs.start("Is following");
        try {
            return followRepository.exists(followerId, followingId, followingType);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Determines following failed[followerId=" + followerId + ", followingId="
                    + followingId + ']', e);

            return false;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets following users of the specified follower.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param followerId     the specified follower id, may be {@code null}
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         User
     *     }, ....]
     * }
     * </pre>
     */
    public JSONObject getFollowingUsers(final int avatarViewMode, final String followerId, final int currentPageNum, final int pageSize) {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> records = new ArrayList<>();
        ret.put(Keys.RESULTS, (Object) records);
        ret.put(Pagination.PAGINATION_RECORD_COUNT, 0);

        if (StringUtils.isBlank(followerId)) {
            return ret;
        }

        try {
            final JSONObject result = getFollowings(followerId, Follow.FOLLOWING_TYPE_C_USER, currentPageNum, pageSize);
            final List<JSONObject> followings = (List<JSONObject>) result.opt(Keys.RESULTS);
            for (final JSONObject follow : followings) {
                final String followingId = follow.optString(Follow.FOLLOWING_ID);
                final JSONObject user = userRepository.get(followingId);
                if (null == user) {
                    LOGGER.log(Level.WARN, "Not found user[id=" + followingId + ']');

                    continue;
                }

                avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

                records.add(user);
            }

            ret.put(Pagination.PAGINATION_RECORD_COUNT, result.optInt(Pagination.PAGINATION_RECORD_COUNT));
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets following users of follower[id=" + followerId + "] failed", e);
        }

        return ret;
    }

    /**
     * Gets following tags of the specified follower.
     *
     * @param followerId     the specified follower id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         Tag
     *     }, ....]
     * }
     * </pre>
     */
    public JSONObject getFollowingTags(final String followerId, final int currentPageNum, final int pageSize) {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> records = new ArrayList<>();
        ret.put(Keys.RESULTS, (Object) records);
        ret.put(Pagination.PAGINATION_RECORD_COUNT, 0);

        try {
            final JSONObject result = getFollowings(followerId, Follow.FOLLOWING_TYPE_C_TAG, currentPageNum, pageSize);
            final List<JSONObject> followings = (List<JSONObject>) result.opt(Keys.RESULTS);
            for (final JSONObject follow : followings) {
                final String followingId = follow.optString(Follow.FOLLOWING_ID);
                final JSONObject tag = tagRepository.get(followingId);
                if (null == tag) {
                    LOGGER.log(Level.WARN, "Not found tag [followerId=" + followerId + ", followingId=" + followingId + ']');
                    // Fix error data caused by history bug
                    try {
                        final Transaction transaction = followRepository.beginTransaction();
                        followRepository.removeByFollowerIdAndFollowingId(followerId, followingId, Follow.FOLLOWING_TYPE_C_TAG);
                        transaction.commit();
                    } catch (final Exception e) {
                        LOGGER.log(Level.ERROR, "Fix history data failed", e);
                    }

                    continue;
                }

                records.add(tag);
            }

            ret.put(Pagination.PAGINATION_RECORD_COUNT, result.optInt(Pagination.PAGINATION_RECORD_COUNT));
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets following tags of follower[id=" + followerId + "] failed", e);
        }

        return ret;
    }

    /**
     * Gets following articles of the specified follower.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param followerId     the specified follower id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         Article
     *     }, ....]
     * }
     * </pre>
     */
    public JSONObject getFollowingArticles(final int avatarViewMode, final String followerId, final int currentPageNum, final int pageSize) {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> records = new ArrayList<>();
        ret.put(Keys.RESULTS, (Object) records);
        ret.put(Pagination.PAGINATION_RECORD_COUNT, 0);

        try {
            final JSONObject result = getFollowings(followerId, Follow.FOLLOWING_TYPE_C_ARTICLE, currentPageNum, pageSize);
            final List<JSONObject> followings = (List<JSONObject>) result.opt(Keys.RESULTS);
            final ArticleQueryService articleQueryService = BeanManager.getInstance().getReference(ArticleQueryService.class);
            for (final JSONObject follow : followings) {
                final String followingId = follow.optString(Follow.FOLLOWING_ID);
                final JSONObject article = articleRepository.get(followingId);
                if (null == article) {
                    LOGGER.log(Level.WARN, "Not found article [id=" + followingId + ']');

                    continue;
                }

                articleQueryService.organizeArticle(avatarViewMode, article);

                records.add(article);
            }

            ret.put(Pagination.PAGINATION_RECORD_COUNT, result.optInt(Pagination.PAGINATION_RECORD_COUNT));
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Get following articles of follower [id=" + followerId + "] failed", e);
        }

        return ret;
    }

    /**
     * Gets watching articles of the specified follower.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param followerId     the specified follower id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         Article
     *     }, ....]
     * }
     * </pre>
     */
    public JSONObject getWatchingArticles(final int avatarViewMode, final String followerId, final int currentPageNum, final int pageSize) {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> records = new ArrayList<>();
        ret.put(Keys.RESULTS, (Object) records);
        ret.put(Pagination.PAGINATION_RECORD_COUNT, 0);

        try {
            final JSONObject result = getFollowings(followerId, Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH, currentPageNum, pageSize);
            final List<JSONObject> followings = (List<JSONObject>) result.opt(Keys.RESULTS);
            final ArticleQueryService articleQueryService = BeanManager.getInstance().getReference(ArticleQueryService.class);
            for (final JSONObject follow : followings) {
                final String followingId = follow.optString(Follow.FOLLOWING_ID);
                final JSONObject article = articleRepository.get(followingId);
                if (null == article) {
                    LOGGER.log(Level.WARN, "Not found article [id=" + followingId + ']');

                    continue;
                }

                articleQueryService.organizeArticle(avatarViewMode, article);

                records.add(article);
            }

            ret.put(Pagination.PAGINATION_RECORD_COUNT, result.optInt(Pagination.PAGINATION_RECORD_COUNT));
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Get watching articles of follower [id=" + followerId + "] failed", e);
        }

        return ret;
    }

    /**
     * Gets watcher users of the specified watching article.
     *
     * @param avatarViewMode    the specified avatar view mode
     * @param watchingArticleId the specified watching article id
     * @param currentPageNum    the specified page number
     * @param pageSize          the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         User
     *     }, ....]
     * }
     * </pre>
     */
    public JSONObject getArticleWatchers(final int avatarViewMode, final String watchingArticleId, final int currentPageNum, final int pageSize) {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> records = new ArrayList<>();
        ret.put(Keys.RESULTS, (Object) records);
        ret.put(Pagination.PAGINATION_RECORD_COUNT, 0);

        try {
            final JSONObject result = getFollowers(watchingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH, currentPageNum, pageSize);
            final List<JSONObject> followers = (List<JSONObject>) result.opt(Keys.RESULTS);
            for (final JSONObject follow : followers) {
                final String followerId = follow.optString(Follow.FOLLOWER_ID);
                final JSONObject user = userRepository.get(followerId);
                if (null == user) {
                    LOGGER.log(Level.WARN, "Not found user[id=" + followerId + ']');

                    continue;
                }

                avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

                records.add(user);
            }

            ret.put(Pagination.PAGINATION_RECORD_COUNT, result.optInt(Pagination.PAGINATION_RECORD_COUNT));
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets watcher users of watching article [id=" + watchingArticleId + "] failed", e);
        }

        return ret;
    }

    /**
     * Gets follower users of the specified following user.
     *
     * @param avatarViewMode  the specified avatar view mode
     * @param followingUserId the specified following user id
     * @param currentPageNum  the specified page number
     * @param pageSize        the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         User
     *     }, ....]
     * }
     * </pre>
     */
    public JSONObject getFollowerUsers(final int avatarViewMode, final String followingUserId, final int currentPageNum, final int pageSize) {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> records = new ArrayList<>();
        ret.put(Keys.RESULTS, (Object) records);
        ret.put(Pagination.PAGINATION_RECORD_COUNT, 0);

        try {
            final JSONObject result = getFollowers(followingUserId, Follow.FOLLOWING_TYPE_C_USER, currentPageNum, pageSize);
            final List<JSONObject> followers = (List<JSONObject>) result.opt(Keys.RESULTS);
            for (final JSONObject follow : followers) {
                final String followerId = follow.optString(Follow.FOLLOWER_ID);
                final JSONObject user = userRepository.get(followerId);
                if (null == user) {
                    LOGGER.log(Level.WARN, "Not found user[id=" + followerId + ']');

                    continue;
                }

                avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

                records.add(user);
            }

            ret.put(Pagination.PAGINATION_RECORD_COUNT, result.optInt(Pagination.PAGINATION_RECORD_COUNT));
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets follower users of following user[id=" + followingUserId + "] failed", e);
        }

        return ret;
    }

    /**
     * Gets the following count of a follower specified by the given follower id and following type.
     *
     * @param followerId    the given follower id
     * @param followingType the given following type
     * @return count
     */
    public long getFollowingCount(final String followerId, final int followingType) {
        Stopwatchs.start("Gets following count [" + followingType + "]");
        try {
            final List<Filter> filters = new ArrayList<>();
            filters.add(new PropertyFilter(Follow.FOLLOWER_ID, FilterOperator.EQUAL, followerId));
            filters.add(new PropertyFilter(Follow.FOLLOWING_TYPE, FilterOperator.EQUAL, followingType));

            final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

            try {
                return followRepository.count(query);
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Counts following count error", e);

                return 0;
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the follower count of a following specified by the given following id and following type.
     *
     * @param followingId   the given following id
     * @param followingType the given following type
     * @return count
     */
    public long getFollowerCount(final String followingId, final int followingType) {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Follow.FOLLOWING_ID, FilterOperator.EQUAL, followingId));
        filters.add(new PropertyFilter(Follow.FOLLOWING_TYPE, FilterOperator.EQUAL, followingType));

        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            return followRepository.count(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Counts follower count error", e);

            return 0;
        }
    }

    /**
     * Gets the followings of a follower specified by the given follower id and following type.
     *
     * @param followerId     the given follower id
     * @param followingType  the specified following type
     * @param currentPageNum the specified current page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "",
     *         "followerId": "",
     *         "followingId": "",
     *         "followingType": int
     *     }, ....]
     * }
     * </pre>
     * @throws RepositoryException repository exception
     */
    private JSONObject getFollowings(final String followerId, final int followingType, final int currentPageNum, final int pageSize)
            throws RepositoryException {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Follow.FOLLOWER_ID, FilterOperator.EQUAL, followerId));
        filters.add(new PropertyFilter(Follow.FOLLOWING_TYPE, FilterOperator.EQUAL, followingType));

        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters))
                .setPageSize(pageSize).setCurrentPageNum(currentPageNum);

        final JSONObject result = followRepository.get(query);
        final List<JSONObject> records = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));
        final int recordCnt = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);

        final JSONObject ret = new JSONObject();
        ret.put(Keys.RESULTS, (Object) records);
        ret.put(Pagination.PAGINATION_RECORD_COUNT, recordCnt);

        return ret;
    }

    /**
     * Gets the followers of a following specified by the given following id and follow type.
     *
     * @param followingId    the given following id
     * @param followingType  the specified following type
     * @param currentPageNum the specified current page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "",
     *         "followerId": "",
     *         "followingId": "",
     *         "followingType": int
     *     }, ....]
     * }
     * </pre>
     * @throws RepositoryException repository exception
     */
    private JSONObject getFollowers(final String followingId, final int followingType, final int currentPageNum, final int pageSize)
            throws RepositoryException {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Follow.FOLLOWING_ID, FilterOperator.EQUAL, followingId));
        filters.add(new PropertyFilter(Follow.FOLLOWING_TYPE, FilterOperator.EQUAL, followingType));

        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters))
                .setPageSize(pageSize).setCurrentPageNum(currentPageNum);

        final JSONObject result = followRepository.get(query);

        final List<JSONObject> records = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));
        final int recordCnt = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);

        final JSONObject ret = new JSONObject();
        ret.put(Keys.RESULTS, (Object) records);
        ret.put(Pagination.PAGINATION_RECORD_COUNT, recordCnt);

        return ret;
    }
}
