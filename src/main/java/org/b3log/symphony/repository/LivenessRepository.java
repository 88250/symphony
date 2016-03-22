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
import org.b3log.symphony.model.Liveness;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Liveness repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 22, 2016
 * @since 1.4.0
 */
@Repository
public class LivenessRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public LivenessRepository() {
        super(Liveness.LIVENESS);
    }

    /**
     * Gets a liveness by the specified user id and date.
     *
     * @param userId the specified user id
     * @param date the specified date
     * @return a liveness, {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByUserAndDate(final String userId, final String date) throws RepositoryException {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Liveness.LIVENESS_USER_ID, FilterOperator.EQUAL, userId),
                new PropertyFilter(Liveness.LIVENESS_DATE, FilterOperator.EQUAL, date))).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }
}
