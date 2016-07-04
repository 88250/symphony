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

import java.util.List;
import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Paginator;
import org.b3log.symphony.model.Invitecode;
import org.b3log.symphony.repository.InvitecodeRepository;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Invitecode query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Jul 4, 2016
 * @since 1.4.0
 */
@Service
public class InvitecodeQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(InvitecodeQueryService.class.getName());

    /**
     * Invitecode repository.
     */
    @Inject
    private InvitecodeRepository invitecodeRepository;

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
     * {
     *     "paginationCurrentPageNum": 1,
     *     "paginationPageSize": 20,
     *     "paginationWindowSize": 10
     * }, see {@link Pagination} for more details
     * </pre>
     *
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
     *
     * @throws ServiceException service exception
     * @see Pagination
     */
    public JSONObject getInvitecodes(final JSONObject requestJSONObject) throws ServiceException {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                addSort(Invitecode.STATUS, SortDirection.DESCENDING);

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
        final List<JSONObject> invitecodes = CollectionUtils.<JSONObject>jsonArrayToList(data);

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
     *
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
     *
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
