/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
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
import java.util.Collections;
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
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.model.Follow;
import org.b3log.symphony.repository.FollowRepository;
import org.json.JSONObject;

/**
 * Follow query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 28, 2013
 * @since 0.2.5
 */
@Service
public class FollowQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FollowQueryService.class.getName());

    /**
     * Follow repository.
     */
    @Inject
    private FollowRepository followRepository;

    /**
     * Gets following tags of the specified follower.
     * 
     * @param followerId the specified follower id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return following tags, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getFollowingTags(final String followerId, final int currentPageNum, final int pageSize)
            throws ServiceException {
        try {
            return getFollowings(followerId, pageSize, currentPageNum, Follow.FOLLOWING_TYPE_C_TAG);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets following tags of follower[id=" + followerId + "] failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets following users of the specified follower.
     * 
     * @param followerId the specified follower id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return following users, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getFollowingUsers(final String followerId, final int currentPageNum, final int pageSize)
            throws ServiceException {
        try {
            return getFollowings(followerId, pageSize, currentPageNum, Follow.FOLLOWING_TYPE_C_USER);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets following users of follower[id=" + followerId + "] failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets the followings of a follower specified by the given follower id and follow type.
     * 
     * @param followerId the given follower id
     * @param followingType the specified following type
     * @param currentPageNum the specified current page number
     * @param pageSize the specified page size
     * @return followings, returns an empty list if not found
     * @throws RepositoryException 
     */
    private List<JSONObject> getFollowings(final String followerId, final int followingType, final int currentPageNum, final int pageSize)
            throws RepositoryException {
        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Follow.FOLLOWER_ID, FilterOperator.EQUAL, followerId));
        filters.add(new PropertyFilter(Follow.FOLLOWING_TYPE, FilterOperator.EQUAL, followingType));

        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters))
                .setPageSize(pageSize).setCurrentPageNum(currentPageNum);

        final JSONObject result = followRepository.get(query);

        return CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
    }
}
