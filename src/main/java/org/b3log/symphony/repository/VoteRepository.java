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
 * @version 1.0.0.0, Aug 13, 2015
 * @since 1.3.0
 */
@Repository
public class VoteRepository extends AbstractRepository {

    /**
     * Removes vote if it exists.
     *
     * @param userId the specified user id
     * @param dataId the specified data entity id
     * @return the removed vote type, returns {@code -1} if removed nothing
     * @throws RepositoryException repository exception
     */
    public int removeIfExists(final String userId, final String dataId) throws RepositoryException {
        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Vote.USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Vote.DATA_ID, FilterOperator.EQUAL, dataId));

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
