/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.service;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.Breezemoon;
import org.b3log.symphony.repository.BreezemoonRepository;
import org.b3log.symphony.util.Markdowns;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Breezemoon query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, May 21, 2018
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
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    /**
     * Get following user breezemoons.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return following tag articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getFollowingUserBreezemoons(final int avatarViewMode, final String userId,
                                                        final int currentPageNum, final int pageSize) throws ServiceException {
        final List<JSONObject> users = (List<JSONObject>) followQueryService.getFollowingUsers(
                avatarViewMode, userId, 1, Integer.MAX_VALUE).opt(Keys.RESULTS);
        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setPageSize(pageSize).setCurrentPageNum(currentPageNum);
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
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets following user breezemoons failed", e);

            throw new ServiceException(e);
        } finally {
            Stopwatchs.end();
        }

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> ret = CollectionUtils.jsonArrayToList(data);
        organizeBreezemoons(avatarViewMode, ret);

        return ret;
    }

    /**
     * Get breezemoon with the specified user id, current page number.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param authorId       the specified user id, empty "" for all users
     * @param page           the specified current page number
     * @param pageSize the specified page size
     * @return for example,      <pre>
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
    public JSONObject getBreezemoons(final int avatarViewMode, final String authorId, final int page, final int pageSize) throws ServiceException {
        final JSONObject ret = new JSONObject();
        final int windowSize = 10;
        CompositeFilter filter;
        final Filter statusFilter = new PropertyFilter(Breezemoon.BREEZEMOON_STATUS, FilterOperator.EQUAL, Breezemoon.BREEZEMOON_STATUS_C_VALID);
        if (StringUtils.isNotBlank(authorId)) {
            filter = CompositeFilterOperator.and(new PropertyFilter(Breezemoon.BREEZEMOON_AUTHOR_ID, FilterOperator.EQUAL, authorId), statusFilter);
        } else {
            filter = CompositeFilterOperator.and(new PropertyFilter(Breezemoon.BREEZEMOON_AUTHOR_ID, FilterOperator.NOT_EQUAL, authorId), statusFilter);
        }
        final Query query = new Query().setFilter(filter).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).setCurrentPageNum(page).setPageSize(20);
        JSONObject result;
        try {
            result = breezemoonRepository.get(query);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Get breezemoons failed", e);

            throw new ServiceException(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(page, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final List<JSONObject> retBms = new ArrayList<>();
        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> bms = CollectionUtils.jsonArrayToList(data);
        try {
            organizeBreezemoons(avatarViewMode, bms);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Get breezemoons failed", e);

            throw new ServiceException(e);
        }

        for (final JSONObject bm : bms) {
            final JSONObject retBm = new JSONObject();
            retBms.add(retBm);

            retBm.put(Breezemoon.BREEZEMOON_CONTENT, bm.optString(Breezemoon.BREEZEMOON_CONTENT));
        }

        ret.put(Breezemoon.BREEZEMOONS, (Object) retBms);

        return ret;
    }

    private static void organizeBreezemoons(final int avatarViewMode, final List<JSONObject> breezemoons) {
        for (final JSONObject bm : breezemoons) {
            String content = bm.optString(Breezemoon.BREEZEMOON_CONTENT);
            content = Markdowns.toHTML(content);
            content = Markdowns.clean(content, "");
            bm.put(Breezemoon.BREEZEMOON_CONTENT, content);
        }
    }
}
