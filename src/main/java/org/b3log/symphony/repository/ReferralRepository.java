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
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Referral;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Referral repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Apr 28, 2016
 * @since 1.4.0
 */
@Repository
public class ReferralRepository extends AbstractRepository {

    /**
     * Gets a referral by the specified data id and IP.
     *
     * @param dataId the specified data id
     * @param ip the specified IP
     * @return referral, returns {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByDataIdAndIP(final String dataId, final String ip) throws RepositoryException {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Referral.REFERRAL_DATA_ID, FilterOperator.EQUAL, dataId),
                new PropertyFilter(Referral.REFERRAL_IP, FilterOperator.EQUAL, ip)
        )).setPageCount(1).setPageSize(1).setCurrentPageNum(1);

        final JSONArray records = get(query).optJSONArray(Keys.RESULTS);
        if (records.length() < 1) {
            return null;
        }

        return records.optJSONObject(0);
    }

    /**
     * Public constructor.
     */
    public ReferralRepository() {
        super(Referral.REFERRAL);
    }
}
