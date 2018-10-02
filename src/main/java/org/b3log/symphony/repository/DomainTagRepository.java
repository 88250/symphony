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
package org.b3log.symphony.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Domain;
import org.b3log.symphony.model.Tag;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Domain-Tag relation repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Apr 12, 2016
 * @since 1.4.0
 */
@Repository
public class DomainTagRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public DomainTagRepository() {
        super(Domain.DOMAIN + "_" + Tag.TAG);
    }

    /**
     * Gets domain-tag relations by the specified domain id.
     *
     * @param domainId       the specified domain id
     * @param currentPageNum the specified current page number, MUST greater then {@code 0}
     * @param pageSize       the specified page size(count of a page contains objects), MUST greater then {@code 0}
     * @return for example      <pre>
     * {
     *     "pagination": {
     *       "paginationPageCount": 88250
     *     },
     *     "rslts": [{
     *         "oId": "",
     *         "domain_oId": domainId,
     *         "tag_oId": ""
     *     }, ....]
     * }
     * </pre>
     * @throws RepositoryException repository exception
     */
    public JSONObject getByDomainId(final String domainId, final int currentPageNum, final int pageSize)
            throws RepositoryException {
        final Query query = new Query().
                setFilter(new PropertyFilter(Domain.DOMAIN + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, domainId)).
                setCurrentPageNum(currentPageNum).setPageSize(pageSize).setPageCount(1);

        return get(query);
    }

    /**
     * Removes domain-tag relations by the specified domain id.
     *
     * @param domainId the specified domain id
     * @throws RepositoryException repository exception
     */
    public void removeByDomainId(final String domainId) throws RepositoryException {
        final Query query = new Query().
                setFilter(new PropertyFilter(Domain.DOMAIN + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, domainId));
        final JSONArray relations = get(query).optJSONArray(Keys.RESULTS);
        for (int i = 0; i < relations.length(); i++) {
            final JSONObject rel = relations.optJSONObject(i);
            remove(rel.optString(Keys.OBJECT_ID));
        }
    }

    /**
     * Gets domain-tag relations by the specified tag id.
     *
     * @param tagId          the specified tag id
     * @param currentPageNum the specified current page number, MUST greater then {@code 0}
     * @param pageSize       the specified page size(count of a page contains objects), MUST greater then {@code 0}
     * @return for example      <pre>
     * {
     *     "pagination": {
     *       "paginationPageCount": 88250
     *     },
     *     "rslts": [{
     *         "oId": "",
     *         "domain_oId": "",
     *         "tag_oId": tagId
     *     }, ....]
     * }
     * </pre>
     * @throws RepositoryException repository exception
     */
    public JSONObject getByTagId(final String tagId, final int currentPageNum, final int pageSize)
            throws RepositoryException {
        final Query query = new Query().
                setFilter(new PropertyFilter(Tag.TAG + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, tagId)).
                setCurrentPageNum(currentPageNum).setPageSize(pageSize).setPageCount(1);

        return get(query);
    }
}
