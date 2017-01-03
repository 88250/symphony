/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.repository;

import java.util.ArrayList;
import java.util.List;
import org.b3log.latke.Keys;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.CompositeFilter;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.Filter;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Vote;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Vote repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Jul 31, 2016
 * @since 1.3.0
 */
@Repository
public class VoteRepository extends AbstractRepository {

    /**
     * Removes vote if it exists.
     *
     * @param userId the specified user id
     * @param dataId the specified data entity id
     * @param dataType the specified data type
     * @return the removed vote type, returns {@code -1} if removed nothing
     * @throws RepositoryException repository exception
     */
    public int removeIfExists(final String userId, final String dataId, final int dataType) throws RepositoryException {
        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Vote.USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Vote.DATA_ID, FilterOperator.EQUAL, dataId));
        filters.add(new PropertyFilter(Vote.DATA_TYPE, FilterOperator.EQUAL, dataType));

        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return -1;
        }

        final JSONObject voteToRemove = array.optJSONObject(0);

        remove(voteToRemove.optString(Keys.OBJECT_ID));

        return voteToRemove.optInt(Vote.TYPE);
    }

    /**
     * Public constructor.
     */
    public VoteRepository() {
        super(Vote.VOTE);
    }
}
