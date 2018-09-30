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
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.*;
import org.b3log.symphony.model.Breezemoon;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.BreezemoonRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Images;
import org.b3log.symphony.util.Markdowns;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Breezemoon query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.6, Sep 7, 2018
 * @since 2.8.0
 */
@Service
public class BreezemoonQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(BreezemoonQueryService.class);

    /**
     * Breezemoon repository.
     */
    @Inject
    private BreezemoonRepository breezemoonRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Get following user breezemoons.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id, may be {@code null}
     * @param page           the specified page number
     * @param pageSize       the specified page size
     * @param windowSize     the specified window size
     * @return for example, <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "breezemoons": [{
     *         "id": "",
     *         "breezemoonContent": ""
     *      }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public JSONObject getFollowingUserBreezemoons(final int avatarViewMode, final String userId,
                                                  final int page, final int pageSize, final int windowSize) throws ServiceException {
        final JSONObject ret = new JSONObject();

        final List<JSONObject> users = (List<JSONObject>) followQueryService.getFollowingUsers(
                avatarViewMode, userId, 1, Integer.MAX_VALUE).opt(Keys.RESULTS);
        if (users.isEmpty()) {
            return getBreezemoons(avatarViewMode, userId, "", page, pageSize, windowSize);
        }

        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setPageSize(pageSize).setCurrentPageNum(page);
        final List<String> followingUserIds = new ArrayList<>();
        for (final JSONObject user : users) {
            followingUserIds.add(user.optString(Keys.OBJECT_ID));
        }
        query.setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Breezemoon.BREEZEMOON_STATUS, FilterOperator.EQUAL, Breezemoon.BREEZEMOON_STATUS_C_VALID),
                new PropertyFilter(Breezemoon.BREEZEMOON_AUTHOR_ID, FilterOperator.IN, followingUserIds)
        ));

        JSONObject result;
        try {
            Stopwatchs.start("Query following user breezemoons");

            result = breezemoonRepository.get(query);
            final JSONArray data = result.optJSONArray(Keys.RESULTS);
            final List<JSONObject> bms = CollectionUtils.jsonArrayToList(data);
            if (bms.isEmpty()) {
                return getBreezemoons(avatarViewMode, userId, "", page, pageSize, windowSize);
            }

            organizeBreezemoons(avatarViewMode, userId, bms);
            ret.put(Breezemoon.BREEZEMOONS, (Object) bms);

        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets following user breezemoons failed", e);

            throw new ServiceException(e);
        } finally {
            Stopwatchs.end();
        }

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        ret.put(Pagination.PAGINATION, pagination);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final List<Integer> pageNums = Paginator.paginate(page, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        return ret;
    }

    /**
     * Get breezemoons with the specified user id, current page number.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param currentUserId  the specified current user id, may be {@code null}
     * @param authorId       the specified user id, empty "" for all users
     * @param page           the specified current page number
     * @param pageSize       the specified page size
     * @param windowSize     the specified window size
     * @return for example, <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "breezemoons": [{
     *         "id": "",
     *         "breezemoonContent": ""
     *      }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     * @see Pagination
     */
    public JSONObject getBreezemoons(final int avatarViewMode, final String currentUserId, final String authorId,
                                     final int page, final int pageSize, final int windowSize) throws ServiceException {
        final JSONObject ret = new JSONObject();
        CompositeFilter filter;
        final Filter statusFilter = new PropertyFilter(Breezemoon.BREEZEMOON_STATUS, FilterOperator.EQUAL, Breezemoon.BREEZEMOON_STATUS_C_VALID);
        if (StringUtils.isNotBlank(authorId)) {
            filter = CompositeFilterOperator.and(new PropertyFilter(Breezemoon.BREEZEMOON_AUTHOR_ID, FilterOperator.EQUAL, authorId), statusFilter);
        } else {
            filter = CompositeFilterOperator.and(new PropertyFilter(Breezemoon.BREEZEMOON_AUTHOR_ID, FilterOperator.NOT_EQUAL, authorId), statusFilter);
        }
        final Query query = new Query().setFilter(filter).addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).setCurrentPageNum(page).setPageSize(20);
        JSONObject result;
        try {
            result = breezemoonRepository.get(query);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Get breezemoons failed", e);

            throw new ServiceException(e);
        }

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        ret.put(Pagination.PAGINATION, pagination);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final List<Integer> pageNums = Paginator.paginate(page, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> bms = CollectionUtils.jsonArrayToList(data);
        try {
            organizeBreezemoons(avatarViewMode, currentUserId, bms);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Get breezemoons failed", e);

            throw new ServiceException(e);
        }

        ret.put(Breezemoon.BREEZEMOONS, (Object) bms);

        return ret;
    }

    /**
     * Get breezemoons by the specified request json object.
     *
     * @param avatarViewMode    the specified avatar view mode
     * @param requestJSONObject the specified request json object, for example,
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10,
     *                          , see {@link Pagination} for more details
     * @param fields            the specified fields to return
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "breezemoons": [{
     *         "oId": "",
     *         "breezemoonContent": "",
     *         "breezemoonCreateTime": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     * @see Pagination
     */

    public JSONObject getBreezemoons(final int avatarViewMode,
                                     final JSONObject requestJSONObject, final Map<String, Class<?>> fields) throws ServiceException {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize)
                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
        for (final Map.Entry<String, Class<?>> field : fields.entrySet()) {
            query.addProjection(field.getKey(), field.getValue());
        }

        JSONObject result;
        try {
            result = breezemoonRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Get breezemoons failed", e);

            throw new ServiceException(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> breezemoons = CollectionUtils.jsonArrayToList(data);
        try {
            organizeBreezemoons(avatarViewMode, "admin", breezemoons);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Organize breezemoons failed", e);

            throw new ServiceException(e);
        }

        ret.put(Breezemoon.BREEZEMOONS, breezemoons);

        return ret;
    }

    /**
     * Gets a breezemoon by the specified id.
     *
     * @param breezemoonId the specified id
     * @return breezemoon, return {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getBreezemoon(final String breezemoonId) throws ServiceException {
        try {
            return breezemoonRepository.get(breezemoonId);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets a breezemoon [id=" + breezemoonId + "] failed", e);

            throw new ServiceException(e);
        }
    }

    private void organizeBreezemoons(final int avatarViewMode, final String currentUserId, final List<JSONObject> breezemoons) throws Exception {
        final Iterator<JSONObject> iterator = breezemoons.iterator();
        while (iterator.hasNext()) {
            final JSONObject bm = iterator.next();

            final String authorId = bm.optString(Breezemoon.BREEZEMOON_AUTHOR_ID);
            final JSONObject author = userRepository.get(authorId);
            if (UserExt.USER_XXX_STATUS_C_PRIVATE == author.optInt(UserExt.USER_BREEZEMOON_STATUS)
                    && !StringUtils.equals(currentUserId, authorId) && !"admin".equals(currentUserId)) {
                iterator.remove();

                continue;
            }
            bm.put(Breezemoon.BREEZEMOON_T_AUTHOR_NAME, author.optString(User.USER_NAME));
            bm.put(Breezemoon.BREEZEMOON_T_AUTHOR_THUMBNAIL_URL + "48", avatarQueryService.getAvatarURLByUser(avatarViewMode, author, "48"));
            final long time = bm.optLong(Breezemoon.BREEZEMOON_CREATED);
            bm.put(Common.TIME_AGO, Times.getTimeAgo(time, Locales.getLocale()));
            bm.put(Breezemoon.BREEZEMOON_T_CREATE_TIME, new Date(time));
            String content = bm.optString(Breezemoon.BREEZEMOON_CONTENT);
            content = shortLinkQueryService.linkArticle(content);
            content = shortLinkQueryService.linkTag(content);
            content = Emotions.convert(content);
            content = Markdowns.toHTML(content);
            content = Markdowns.clean(content, "");
            content = Images.qiniuImgProcessing(content);
            bm.put(Breezemoon.BREEZEMOON_CONTENT, content);
        }
    }
}
