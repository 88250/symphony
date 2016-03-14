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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.CompositeFilterOperator;
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
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.repository.DomainRepository;
import org.b3log.symphony.repository.DomainTagRepository;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.util.Markdowns;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * Domain query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Mar 14, 2016
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
     * Tag repository.
     */
    @Inject
    private TagRepository tagRepository;

    /**
     * Domain tag repository.
     */
    @Inject
    private DomainTagRepository domainTagRepository;

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Gets most tag domain.
     *
     * @param fetchSize the specified fetch size
     * @return domains, returns an empty list if not found
     */
    public List<JSONObject> getMostTagDomain(final int fetchSize) {
        final Query query = new Query().addSort(Domain.DOMAIN_SORT, SortDirection.ASCENDING).
                addSort(Domain.DOMAIN_TAG_COUNT, SortDirection.DESCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                setPageSize(fetchSize).setPageCount(1);
        try {
            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(domainRepository.get(query).optJSONArray(Keys.RESULTS));
            for (final JSONObject domain : ret) {
                final List<JSONObject> tags = getTags(domain.optString(Keys.OBJECT_ID));

                domain.put(Domain.DOMAIN_T_TAGS, (Object) tags);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets most tag domain error", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets a domain's tags.
     *
     * @param domainId the specified domain id
     * @return tags, returns an empty list if not found
     */
    public List<JSONObject> getTags(final String domainId) {
        final List<JSONObject> ret = new ArrayList<JSONObject>();

        final Query query = new Query().
                setFilter(new PropertyFilter(Domain.DOMAIN + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, domainId));
        try {
            final List<JSONObject> relations = CollectionUtils.jsonArrayToList(
                    domainTagRepository.get(query).optJSONArray(Keys.RESULTS));

            for (final JSONObject relation : relations) {
                final String tagId = relation.optString(Tag.TAG + "_" + Keys.OBJECT_ID);
                final JSONObject tag = tagRepository.get(tagId);

                ret.add(tag);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets domain [id=" + domainId + "] tags error", e);
        }

        return ret;
    }

    /**
     * Gets a domain by the specified domain URI.
     *
     * @param domainURI the specified domain URI
     * @return domain, returns {@code null} if not null
     * @throws ServiceException service exception
     */
    public JSONObject getByURI(final String domainURI) throws ServiceException {
        try {
            final JSONObject ret = domainRepository.getByURI(domainURI);
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

            final String domainTitle = ret.optString(Domain.DOMAIN_TITLE);

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
            LOGGER.log(Level.ERROR, "Gets domain [URI=" + domainURI + "] failed", e);

            throw new ServiceException(e);
        }
    }

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
            LOGGER.log(Level.ERROR, "Gets domain [title=" + domainTitle + "] failed", e);

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
                addSort(Domain.DOMAIN_SORT, SortDirection.ASCENDING).
                addSort(Domain.DOMAIN_TAG_COUNT, SortDirection.DESCENDING).
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
            final JSONObject ret = domainRepository.get(domainId);
            final List<JSONObject> tags = getTags(domainId);
            ret.put(Domain.DOMAIN_T_TAGS, (Object) tags);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets a domain [tagId=" + domainId + "] failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Whether a tag specified by the given tag title in a domain specified by the given domain id.
     *
     * @param tagTitle the given tag title
     * @param domainId the given domain id
     * @return {@code true} if the tag in the domain, returns {@code false} otherwise
     */
    public boolean containTag(final String tagTitle, final String domainId) {
        try {
            final JSONObject domain = domainRepository.get(domainId);
            if (null == domain) {
                return true;
            }

            final JSONObject tag = tagRepository.getByTitle(tagTitle);
            if (null == tag) {
                return true;
            }

            final Query query = new Query().setFilter(
                    CompositeFilterOperator.and(
                            new PropertyFilter(Domain.DOMAIN + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, domainId),
                            new PropertyFilter(Tag.TAG + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID))));

            return domainTagRepository.count(query) > 0;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Check domain tag [tagTitle=" + tagTitle + ", domainId=" + domainId + "] failed", e);

            return true;
        }
    }
}
