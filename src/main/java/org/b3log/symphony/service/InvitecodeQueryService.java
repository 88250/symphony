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

import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Paginator;
import org.b3log.symphony.model.Invitecode;
import org.b3log.symphony.repository.InvitecodeRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Invitecode query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.3, Sep 20, 2016
 * @since 1.4.0
 */
@Service
public class InvitecodeQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(InvitecodeQueryService.class);

    /**
     * Invitecode repository.
     */
    @Inject
    private InvitecodeRepository invitecodeRepository;

    /**
     * Gets valid invitecodes by the specified generator id.
     *
     * @param generatorId the specified generator id
     * @return for example,      <pre>
     * {
     *     "oId": "",
     *     "code": "",
     *     "memo": "",
     *     ....
     * }
     * </pre>, returns an empty list if not found
     */
    public List<JSONObject> getValidInvitecodes(final String generatorId) {
        final Query query = new Query().setFilter(
                CompositeFilterOperator.and(
                        new PropertyFilter(Invitecode.GENERATOR_ID, FilterOperator.EQUAL, generatorId),
                        new PropertyFilter(Invitecode.STATUS, FilterOperator.EQUAL, Invitecode.STATUS_C_UNUSED)
                ));

        try {
            return CollectionUtils.jsonArrayToList(invitecodeRepository.get(query).optJSONArray(Keys.RESULTS));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets valid invitecode failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets an invitecode with the specified code.
     *
     * @param code the specified code
     * @return invitecode, returns {@code null} if not found
     */
    public JSONObject getInvitecode(final String code) {
        final Query query = new Query().setFilter(new PropertyFilter(Invitecode.CODE, FilterOperator.EQUAL, code));

        try {
            final JSONObject result = invitecodeRepository.get(query);
            final JSONArray codes = result.optJSONArray(Keys.RESULTS);
            if (0 == codes.length()) {
                return null;
            }

            return codes.optJSONObject(0);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets invitecode error", e);

            return null;
        }
    }

    /**
     * Gets invitecodes by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     *                                                                            {
     *                                                                                "paginationCurrentPageNum": 1,
     *                                                                                "paginationPageSize": 20,
     *                                                                                "paginationWindowSize": 10
     *                                                                            }, see {@link Pagination} for more details
     *                                                                            </pre>
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "invitecodes": [{
     *         "oId": "",
     *         "code": "",
     *         "memo": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     * @see Pagination
     */
    public JSONObject getInvitecodes(final JSONObject requestJSONObject) throws ServiceException {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                addSort(Invitecode.STATUS, SortDirection.DESCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        JSONObject result = null;

        try {
            result = invitecodeRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets invitecodes failed", e);

            throw new ServiceException(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> invitecodes = CollectionUtils.jsonArrayToList(data);

        ret.put(Invitecode.INVITECODES, invitecodes);

        return ret;
    }

    /**
     * Gets an invitecode by the specified invitecode id.
     *
     * @param invitecodeId the specified invitecode id
     * @return for example,      <pre>
     * {
     *     "oId": "",
     *     "code": "",
     *     "memo": "",
     *     ....
     * }
     * </pre>, returns {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getInvitecodeById(final String invitecodeId) throws ServiceException {
        try {
            return invitecodeRepository.get(invitecodeId);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets an invitecode failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Gets an invitecode by the specified user id.
     *
     * @param userId the specified user id
     * @return for example,      <pre>
     * {
     *     "oId": "",
     *     "code": "",
     *     "memo": "",
     *     ....
     * }
     * </pre>, returns {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getInvitecodeByUserId(final String userId) throws ServiceException {
        final Query query = new Query().setFilter(new PropertyFilter(Invitecode.USER_ID, FilterOperator.EQUAL, userId));

        try {
            final JSONArray data = invitecodeRepository.get(query).optJSONArray(Keys.RESULTS);
            if (1 > data.length()) {
                return null;
            }

            return data.optJSONObject(0);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets an invitecode failed", e);

            throw new ServiceException(e);
        }
    }
}
