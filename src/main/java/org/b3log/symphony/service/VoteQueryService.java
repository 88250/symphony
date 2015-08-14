/*
 * Copyright (c) 2012-2015, b3log.org
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
package org.b3log.symphony.service;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.CompositeFilter;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.Filter;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Vote;
import org.b3log.symphony.repository.VoteRepository;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Vote query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 13, 2015
 * @since 1.3.0
 */
@Service
public class VoteQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(VoteQueryService.class.getName());

    /**
     * Vote repository.
     */
    @Inject
    private VoteRepository voteRepository;

    /**
     * Determines whether the specified user dose vote the specified entity.
     *
     * @param userId the specified user id
     * @param dataId the specified entity id
     * @return voted type, returns {@code -1} if has not voted yet
     */
    public int isVoted(final String userId, final String dataId) {
        try {
            final List<Filter> filters = new ArrayList<Filter>();
            filters.add(new PropertyFilter(Vote.USER_ID, FilterOperator.EQUAL, userId));
            filters.add(new PropertyFilter(Vote.DATA_ID, FilterOperator.EQUAL, dataId));

            final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

            final JSONObject result = voteRepository.get(query);
            final JSONArray array = result.optJSONArray(Keys.RESULTS);

            if (0 == array.length()) {
                return -1;
            }

            final JSONObject vote = array.optJSONObject(0);

            return vote.optInt(Vote.TYPE);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, e.getMessage());

            return -1;
        }
    }
}
