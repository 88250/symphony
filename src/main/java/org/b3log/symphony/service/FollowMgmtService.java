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

import javax.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Follow;
import org.b3log.symphony.repository.FollowRepository;
import org.json.JSONObject;

/**
 * Follow management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 28, 2013
 * @since 0.2.5
 */
@Service
public class FollowMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FollowMgmtService.class.getName());

    /**
     * Follow repository.
     */
    @Inject
    private FollowRepository followRepository;

    /**
     * The specified follower follows the specified following tag.
     *
     * @param followerId the specified follower id
     * @param followingTagId the specified following tag id
     * @throws ServiceException service exception
     */
    @Transactional
    public void followTag(final String followerId, final String followingTagId) throws ServiceException {
        try {
            follow(followerId, followingTagId, Follow.FOLLOWING_TYPE_C_TAG);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] follows a tag[id=" + followingTagId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * The specified follower follows the specified following user.
     *
     * @param followerId the specified follower id
     * @param followingUserId the specified following user id
     * @throws ServiceException service exception
     */
    @Transactional
    public void followUser(final String followerId, final String followingUserId) throws ServiceException {
        try {
            follow(followerId, followingUserId, Follow.FOLLOWING_TYPE_C_USER);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] follows an user[id=" + followingUserId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * The specified follower follows the specified following entity with the specified following type.
     *
     * @param followerId the specified follower id
     * @param followingId the specified following entity id
     * @param followingType the specified following type
     * @throws RepositoryException repository exception
     */
    private void follow(final String followerId, final String followingId, final int followingType) throws RepositoryException {
        if (followRepository.exists(followerId, followingId)) {
            return;
        }

        final JSONObject follow = new JSONObject();
        follow.put(Follow.FOLLOWER_ID, followerId);
        follow.put(Follow.FOLLOWING_ID, followingId);
        follow.put(Follow.FOLLOWING_TYPE, followingType);

        followRepository.add(follow);
    }

    /**
     * Removes a follow relationship.
     *
     * @param followerId the specified follower id
     * @param followingId the specified following entity id
     * @throws ServiceException service exception
     */
    @Transactional
    public void removeFollow(final String followerId, final String followingId) throws ServiceException {
        try {
            followRepository.removeByFollowerIdAndFollowingId(followerId, followingId);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Removes a follow[folowerId=" + followerId + ", followingId=" + followingId + "] failed", e);
        }
    }
}
