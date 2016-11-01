/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2016,  b3log.org & hacpai.com
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
import org.b3log.symphony.model.Follow;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Follow repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 28, 2013
 * @since 0.2.5
 */
@Repository
public class FollowRepository extends AbstractRepository {

    /**
     * Removes a follow relationship by the specified follower id and the specified following entity id.
     *
     * @param followerId the specified follower id
     * @param followingId the specified following entity id
     * @throws RepositoryException repository exception
     */
    public void removeByFollowerIdAndFollowingId(final String followerId, final String followingId) throws RepositoryException {
        final JSONObject toRemove = getByFollowerIdAndFollowingId(followerId, followingId);

        if (null == toRemove) {
            return;
        }

        remove(toRemove.optString(Keys.OBJECT_ID));
    }

    /**
     * Gets a follow relationship by the specified follower id and the specified following entity id.
     *
     * @param followerId the specified follower id
     * @param followingId the specified following entity id
     * @return follow relationship, returns {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByFollowerIdAndFollowingId(final String followerId, final String followingId) throws RepositoryException {
        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Follow.FOLLOWER_ID, FilterOperator.EQUAL, followerId));
        filters.add(new PropertyFilter(Follow.FOLLOWING_ID, FilterOperator.EQUAL, followingId));

        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }

    /**
     * Determines whether exists a follow relationship for the specified follower and the specified following entity.
     *
     * @param followerId the specified follower id
     * @param followingId the specified following entity id
     * @return {@code true} if exists, returns {@code false} otherwise
     * @throws RepositoryException repository exception
     */
    public boolean exists(final String followerId, final String followingId) throws RepositoryException {
        return null != getByFollowerIdAndFollowingId(followerId, followingId);
    }

    /**
     * Public constructor.
     */
    public FollowRepository() {
        super(Follow.FOLLOW);
    }
}
