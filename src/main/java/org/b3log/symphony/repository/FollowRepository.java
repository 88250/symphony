/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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
import org.b3log.symphony.model.Follow;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Follow repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Dec 18, 2018
 * @since 0.2.5
 */
@Repository
public class FollowRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public FollowRepository() {
        super(Follow.FOLLOW);
    }

    /**
     * Removes a follow relationship by the specified follower id and the specified following entity id.
     *
     * @param followerId    the specified follower id
     * @param followingId   the specified following entity id
     * @param followingType the specified following type
     * @throws RepositoryException repository exception
     */
    public void removeByFollowerIdAndFollowingId(final String followerId, final String followingId, final int followingType)
            throws RepositoryException {
        final JSONObject toRemove = getByFollowerIdAndFollowingId(followerId, followingId, followingType);

        if (null == toRemove) {
            return;
        }

        remove(toRemove.optString(Keys.OBJECT_ID));
    }

    /**
     * Gets a follow relationship by the specified follower id and the specified following entity id.
     *
     * @param followerId    the specified follower id
     * @param followingId   the specified following entity id
     * @param followingType the specified following type
     * @return follow relationship, returns {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByFollowerIdAndFollowingId(final String followerId, final String followingId, final int followingType)
            throws RepositoryException {
        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Follow.FOLLOWER_ID, FilterOperator.EQUAL, followerId));
        filters.add(new PropertyFilter(Follow.FOLLOWING_ID, FilterOperator.EQUAL, followingId));
        filters.add(new PropertyFilter(Follow.FOLLOWING_TYPE, FilterOperator.EQUAL, followingType));

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
     * @param followerId    the specified follower id
     * @param followingId   the specified following entity id
     * @param followingType the specified following type
     * @return {@code true} if exists, returns {@code false} otherwise
     * @throws RepositoryException repository exception
     */
    public boolean exists(final String followerId, final String followingId, final int followingType)
            throws RepositoryException {
        return null != getByFollowerIdAndFollowingId(followerId, followingId, followingType);
    }

    /**
     * Get follows by the specified following id and type.
     *
     * @param followingId    the specified following id
     * @param type           the specified type
     * @param currentPageNum the specified current page number, MUST greater then {@code 0}
     * @param pageSize       the specified page size(count of a page contains objects), MUST greater then {@code 0}
     * @return for example      <pre>
     * {
     *     "pagination": {
     *       "paginationPageCount": 88250
     *     },
     *     "rslts": [{
     *         Follow
     *     }, ....]
     * }
     * </pre>
     * @throws RepositoryException repository exception
     */
    public JSONObject getByFollowingId(final String followingId, final int type, final int currentPageNum, final int pageSize) throws RepositoryException {
        final Query query = new Query().
                setFilter(CompositeFilterOperator.and(
                        new PropertyFilter(Follow.FOLLOWING_ID, FilterOperator.EQUAL, followingId),
                        new PropertyFilter(Follow.FOLLOWING_TYPE, FilterOperator.EQUAL, type))).
                setPage(currentPageNum, pageSize).setPageCount(1);

        return get(query);
    }
}
