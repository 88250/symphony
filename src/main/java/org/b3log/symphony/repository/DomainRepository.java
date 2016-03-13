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
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Domain repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 13, 2013
 * @since 1.4.0
 */
@Repository
public class DomainRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public DomainRepository() {
        super(Domain.DOMAIN);
    }

    /**
     * Gets a domain by the specified domain title.
     *
     * @param domainTitle the specified domain title
     * @return a domain, {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByTitle(final String domainTitle) throws RepositoryException {
        final Query query = new Query().setFilter(new PropertyFilter(Domain.DOMAIN_TITLE, FilterOperator.EQUAL, domainTitle)).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }
    
    /**
     * Gets a domain by the specified domain URI.
     *
     * @param domainURI the specified domain URI
     * @return a domain, {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByURI(final String domainURI) throws RepositoryException {
        final Query query = new Query().setFilter(new PropertyFilter(Domain.DOMAIN_URI, FilterOperator.EQUAL, domainURI)).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }
}
