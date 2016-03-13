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
import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
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
import org.b3log.symphony.model.Domain;
import org.b3log.symphony.repository.DomainRepository;
import org.b3log.symphony.util.Markdowns;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * Domain query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 13, 2016
 * @since 1.4.0
 */
@Service
public class DomainQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DomainQueryService.class.getName());

    /**
     * Domain repository.
     */
    @Inject
    private DomainRepository domainRepository;

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Gets a domain by the specified domain title.
     *
     * @param domainTitle the specified domain title
     * @return domain, returns {@code null} if not null
     * @throws ServiceException service exception
     */
    public JSONObject getByTitle(final String domainTitle) throws ServiceException {
        try {
            final JSONObject ret = domainRepository.getByTitle(domainTitle);
            if (null == ret) {
                return null;
            }

            if (Domain.DOMAIN_STATUS_C_VALID != ret.optInt(Domain.DOMAIN_STATUS)) {
                return null;
            }

            String description = ret.optString(Domain.DOMAIN_DESCRIPTION);
            String descriptionText = ret.optString(Domain.DOMAIN_TITLE);
            if (StringUtils.isNotBlank(description)) {
                description = shortLinkQueryService.linkTag(description);
                description = Markdowns.toHTML(description);

                ret.put(Domain.DOMAIN_DESCRIPTION, description);
                descriptionText = Jsoup.parse(description).text();
            }

            if (StringUtils.isBlank(ret.optString(Domain.DOMAIN_SEO_TITLE))) {
                ret.put(Domain.DOMAIN_SEO_TITLE, domainTitle);
            }

            if (StringUtils.isBlank(ret.optString(Domain.DOMAIN_SEO_DESC))) {
                ret.put(Domain.DOMAIN_SEO_DESC, descriptionText);
            }

            if (StringUtils.isBlank(ret.optString(Domain.DOMAIN_SEO_KEYWORDS))) {
                ret.put(Domain.DOMAIN_SEO_KEYWORDS, domainTitle);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets tag [title=" + domainTitle + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets domains by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "domainTitle": "", // optional
     *     "paginationCurrentPageNum": 1,
     *     "paginationPageSize": 20,
     *     "paginationWindowSize": 10
     * }, see {@link Pagination} for more details
     * </pre>
     *
     * @param domainFields the specified domain fields to return
     *
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "domain": [{
     *         "oId": "",
     *         "domainTitle": "",
     *         "domainDescription": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     * @see Pagination
     */
    public JSONObject getDomains(final JSONObject requestJSONObject, final Map<String, Class<?>> domainFields) throws ServiceException {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
        for (final Map.Entry<String, Class<?>> field : domainFields.entrySet()) {
            query.addProjection(field.getKey(), field.getValue());
        }

        if (requestJSONObject.has(Domain.DOMAIN_TITLE)) {
            query.setFilter(new PropertyFilter(Domain.DOMAIN_TITLE, FilterOperator.EQUAL, requestJSONObject.optString(Domain.DOMAIN_TITLE)));
        }

        JSONObject result = null;

        try {
            result = domainRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets domains failed", e);

            throw new ServiceException(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> domains = CollectionUtils.<JSONObject>jsonArrayToList(data);

        ret.put(Domain.DOMAINS, domains);

        return ret;
    }

    /**
     * Gets a domain by the specified id.
     *
     * @param domainId the specified id
     * @return a domain, return {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getDomain(final String domainId) throws ServiceException {
        try {
            return domainRepository.get(domainId);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets a domain [tagId=" + domainId + "] failed", e);
            throw new ServiceException(e);
        }
    }
}
