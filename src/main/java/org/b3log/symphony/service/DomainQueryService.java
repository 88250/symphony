/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Paginator;
import org.b3log.symphony.model.Domain;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.repository.DomainRepository;
import org.b3log.symphony.repository.DomainTagRepository;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.util.Markdowns;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.1, May 12, 2019
 * @since 1.4.0
 */
@Service
public class DomainQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(DomainQueryService.class);

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
     * Gets all domains.
     *
     * @return domains, returns an empty list if not found
     */
    public List<JSONObject> getAllDomains() {
        final Query query = new Query().
                addSort(Domain.DOMAIN_SORT, SortDirection.ASCENDING).
                addSort(Domain.DOMAIN_TAG_COUNT, SortDirection.DESCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                setPage(1, Integer.MAX_VALUE).setPageCount(1);
        try {
            final List<JSONObject> ret = domainRepository.getList(query);
            for (final JSONObject domain : ret) {
                final List<JSONObject> tags = getTags(domain.optString(Keys.OBJECT_ID));
                domain.put(Domain.DOMAIN_T_TAGS, (Object) tags);
            }
            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets all domains failed", e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets most tag navigation domains.
     *
     * @param fetchSize the specified fetch size
     * @return domains, returns an empty list if not found
     */
    public List<JSONObject> getMostTagNaviDomains(final int fetchSize) {
        final Query query = new Query().
                setFilter(new PropertyFilter(Domain.DOMAIN_NAV, FilterOperator.EQUAL, Domain.DOMAIN_NAV_C_ENABLED)).
                addSort(Domain.DOMAIN_SORT, SortDirection.ASCENDING).
                addSort(Domain.DOMAIN_TAG_COUNT, SortDirection.DESCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                setPage(1, fetchSize).setPageCount(1);
        try {
            final List<JSONObject> ret = domainRepository.getList(query);
            for (final JSONObject domain : ret) {
                final List<JSONObject> tags = getTags(domain.optString(Keys.OBJECT_ID));
                domain.put(Domain.DOMAIN_T_TAGS, (Object) tags);
            }
            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets most tag navigation domains failed", e);
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
        final List<JSONObject> ret = new ArrayList<>();
        final Query query = new Query().
                setFilter(new PropertyFilter(Domain.DOMAIN + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, domainId));
        try {
            final List<JSONObject> relations = domainTagRepository.getList(query);
            for (final JSONObject relation : relations) {
                final String tagId = relation.optString(Tag.TAG + "_" + Keys.OBJECT_ID);
                final JSONObject tag = tagRepository.get(tagId);
                ret.add(tag);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets domain [id=" + domainId + "] tags failed", e);
        }
        return ret;
    }

    /**
     * Gets a domain by the specified domain URI.
     *
     * @param domainURI the specified domain URI
     * @return domain, returns {@code null} if not null
     */
    public JSONObject getByURI(final String domainURI) {
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

            return null;
        }
    }

    /**
     * Gets a domain by the specified domain title.
     *
     * @param domainTitle the specified domain title
     * @return domain, returns {@code null} if not null
     */
    public JSONObject getByTitle(final String domainTitle) {
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

            return null;
        }
    }

    /**
     * Gets domains by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          {
     *                          "domainTitle": "", // optional
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10
     *                          }, see {@link Pagination} for more details
     * @param domainFields      the specified domain fields to return
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "domains": [{
     *         "oId": "",
     *         "domainTitle": "",
     *         "domainDescription": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @see Pagination
     */
    public JSONObject getDomains(final JSONObject requestJSONObject, final List<String> domainFields) {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setPage(currentPageNum, pageSize).
                addSort(Domain.DOMAIN_SORT, SortDirection.ASCENDING).
                addSort(Domain.DOMAIN_TAG_COUNT, SortDirection.DESCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
        for (final String field : domainFields) {
            query.select(field);
        }

        if (requestJSONObject.has(Domain.DOMAIN_TITLE)) {
            query.setFilter(new PropertyFilter(Domain.DOMAIN_TITLE, FilterOperator.EQUAL, requestJSONObject.optString(Domain.DOMAIN_TITLE)));
        }

        JSONObject result;
        try {
            result = domainRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets domains failed", e);
            return null;
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        ret.put(Domain.DOMAINS, result.opt(Keys.RESULTS));
        return ret;
    }

    /**
     * Gets a domain by the specified id.
     *
     * @param domainId the specified id
     * @return a domain, return {@code null} if not found
     */
    public JSONObject getDomain(final String domainId) {
        try {
            final JSONObject ret = domainRepository.get(domainId);
            final List<JSONObject> tags = getTags(domainId);
            ret.put(Domain.DOMAIN_T_TAGS, (Object) tags);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets a domain [domainId=" + domainId + "] failed", e);

            return null;
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
