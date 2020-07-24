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
package org.b3log.symphony.repository;

import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Liveness;
import org.json.JSONObject;

/**
 * Liveness repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Feb 27, 2019
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
     * Remove liveness by the specified user id.
     *
     * @param userId the specified user id
     * @throws RepositoryException repository exception
     */
    public void removeByUserId(final String userId) throws RepositoryException {
        remove(new Query().setFilter(new PropertyFilter(Liveness.LIVENESS_USER_ID, FilterOperator.EQUAL, userId)));
    }

    /**
     * Gets a liveness by the specified user id and date.
     *
     * @param userId the specified user id
     * @param date   the specified date
     * @return a liveness, {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByUserAndDate(final String userId, final String date) throws RepositoryException {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Liveness.LIVENESS_USER_ID, FilterOperator.EQUAL, userId),
                new PropertyFilter(Liveness.LIVENESS_DATE, FilterOperator.EQUAL, date))).setPageCount(1);
        return getFirst(query);
    }
}
