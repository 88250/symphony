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
     * @param ip     the specified IP
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
