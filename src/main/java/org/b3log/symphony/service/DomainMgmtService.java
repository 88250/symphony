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
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.cache.DomainCache;
import org.b3log.symphony.model.Domain;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.repository.DomainRepository;
import org.b3log.symphony.repository.DomainTagRepository;
import org.b3log.symphony.repository.OptionRepository;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Domain management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.3.3, Mar 30, 2018
 * @since 1.4.0
 */
@Service
public class DomainMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DomainMgmtService.class);

    /**
     * Domain repository.
     */
    @Inject
    private DomainRepository domainRepository;

    /**
     * Domain tag repository.
     */
    @Inject
    private DomainTagRepository domainTagRepository;

    /**
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    /**
     * Domain cache.
     */
    @Inject
    private DomainCache domainCache;

    /**
     * Removes a domain-tag relation.
     *
     * @param domainId the specified domain id
     * @param tagId    the specified tag id
     * @throws ServiceException service exception
     */
    @Transactional
    public void removeDomainTag(final String domainId, final String tagId) throws ServiceException {
        try {
            final JSONObject domain = domainRepository.get(domainId);
            domain.put(Domain.DOMAIN_TAG_COUNT, domain.optInt(Domain.DOMAIN_TAG_COUNT) - 1);

            domainRepository.update(domainId, domain);

            final Query query = new Query().setFilter(
                    CompositeFilterOperator.and(
                            new PropertyFilter(Domain.DOMAIN + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, domainId),
                            new PropertyFilter(Tag.TAG + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, tagId)));

            final JSONArray relations = domainTagRepository.get(query).optJSONArray(Keys.RESULTS);
            if (relations.length() < 1) {
                return;
            }

            final JSONObject relation = relations.optJSONObject(0);
            domainTagRepository.remove(relation.optString(Keys.OBJECT_ID));

            // Refresh cache
            domainCache.loadDomains();
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Adds a domain-tag relation failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Adds a domain-tag relation.
     *
     * @param domainTag the specified domain-tag relation
     * @throws ServiceException service exception
     */
    @Transactional
    public void addDomainTag(final JSONObject domainTag) throws ServiceException {
        try {
            final String domainId = domainTag.optString(Domain.DOMAIN + "_" + Keys.OBJECT_ID);
            final JSONObject domain = domainRepository.get(domainId);
            domain.put(Domain.DOMAIN_TAG_COUNT, domain.optInt(Domain.DOMAIN_TAG_COUNT) + 1);

            domainRepository.update(domainId, domain);
            domainTagRepository.add(domainTag);

            // Refresh cache
            domainCache.loadDomains();
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Adds a domain-tag relation failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Adds a domain relation.
     *
     * @param domain the specified domain relation
     * @return domain id
     * @throws ServiceException service exception
     */
    @Transactional
    public String addDomain(final JSONObject domain) throws ServiceException {
        try {
            final JSONObject record = new JSONObject();
            record.put(Domain.DOMAIN_CSS, domain.optString(Domain.DOMAIN_CSS));
            record.put(Domain.DOMAIN_DESCRIPTION, domain.optString(Domain.DOMAIN_DESCRIPTION));
            record.put(Domain.DOMAIN_ICON_PATH, domain.optString(Domain.DOMAIN_ICON_PATH));
            record.put(Domain.DOMAIN_SEO_DESC, domain.optString(Domain.DOMAIN_SEO_DESC));
            record.put(Domain.DOMAIN_SEO_KEYWORDS, domain.optString(Domain.DOMAIN_SEO_KEYWORDS));
            record.put(Domain.DOMAIN_SEO_TITLE, domain.optString(Domain.DOMAIN_SEO_TITLE));
            record.put(Domain.DOMAIN_STATUS, domain.optInt(Domain.DOMAIN_STATUS));
            record.put(Domain.DOMAIN_TITLE, domain.optString(Domain.DOMAIN_TITLE));
            record.put(Domain.DOMAIN_URI, domain.optString(Domain.DOMAIN_URI));
            record.put(Domain.DOMAIN_TAG_COUNT, 0);
            record.put(Domain.DOMAIN_TYPE, "");
            record.put(Domain.DOMAIN_SORT, 10);
            record.put(Domain.DOMAIN_NAV, Domain.DOMAIN_NAV_C_ENABLED);

            final JSONObject domainCntOption = optionRepository.get(Option.ID_C_STATISTIC_DOMAIN_COUNT);
            final int domainCnt = domainCntOption.optInt(Option.OPTION_VALUE);
            domainCntOption.put(Option.OPTION_VALUE, domainCnt + 1);
            optionRepository.update(Option.ID_C_STATISTIC_DOMAIN_COUNT, domainCntOption);

            final String ret = domainRepository.add(record);

            // Refresh cache
            domainCache.loadDomains();

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Adds a domain failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Updates the specified domain by the given domain id.
     *
     * @param domainId the given domain id
     * @param domain   the specified domain
     * @throws ServiceException service exception
     */
    @Transactional
    public void updateDomain(final String domainId, final JSONObject domain) throws ServiceException {
        try {
            domainRepository.update(domainId, domain);

            // Refresh cache
            domainCache.loadDomains();
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Updates a domain [id=" + domainId + "] failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Removes the specified domain by the given domain id.
     *
     * @param domainId the given domain id
     * @throws ServiceException service exception
     */
    @Transactional
    public void removeDomain(final String domainId) throws ServiceException {
        try {
            domainTagRepository.removeByDomainId(domainId);
            domainRepository.remove(domainId);

            final JSONObject domainCntOption = optionRepository.get(Option.ID_C_STATISTIC_DOMAIN_COUNT);
            final int domainCnt = domainCntOption.optInt(Option.OPTION_VALUE);
            domainCntOption.put(Option.OPTION_VALUE, domainCnt - 1);
            optionRepository.update(Option.ID_C_STATISTIC_DOMAIN_COUNT, domainCntOption);

            // Refresh cache
            domainCache.loadDomains();
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Updates a domain [id=" + domainId + "] failed", e);

            throw new ServiceException(e);
        }
    }
}
