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
