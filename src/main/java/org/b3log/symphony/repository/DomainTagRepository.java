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
package org.b3log.symphony.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
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
     * @param domainId the specified domain id
     * @param currentPageNum the specified current page number, MUST greater then {@code 0}
     * @param pageSize the specified page size(count of a page contains objects), MUST greater then {@code 0}
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
     *
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
     * @param tagId the specified tag id
     * @param currentPageNum the specified current page number, MUST greater then {@code 0}
     * @param pageSize the specified page size(count of a page contains objects), MUST greater then {@code 0}
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
     *
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
