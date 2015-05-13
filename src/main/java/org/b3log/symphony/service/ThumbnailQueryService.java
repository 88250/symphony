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
import org.b3log.latke.model.User;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.MD5;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Thumbnail utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, May 13, 2015
 * @since 0.3.0
 */
@Service
public class ThumbnailQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ThumbnailQueryService.class.getName());

    /**
     * Gravatar address.
     */
    public static final String GRAVATAR = "http://gravatar.duoshuo.com/avatar/";

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Gets the avatar URL for the specified email with the specified size.
     *
     * @param email the specified email
     * @param size the specified size
     * @return the avatar URL
     */
    public String getAvatarURL(final String email, final String size) {
        String ret = Symphonys.get("defaultThumbnailURL");

        try {
            final JSONObject user = userRepository.getByEmail(email);
            if (null == user) {
                return ret;
            }

            final int avatarType = user.optInt(UserExt.USER_AVATAR_TYPE);

            if (UserExt.USER_AVATAR_TYPE_C_GRAVATAR == avatarType) {
                final String userEmail = user.optString(User.USER_EMAIL);
                final String thumbnailURL = getGravatarURL(userEmail, "140");

                ret = thumbnailURL;
            } else if (UserExt.USER_AVATAR_TYPE_C_EXTERNAL_LINK == avatarType) {
                ret = user.optString(UserExt.USER_AVATAR_URL);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.WARN, "Gets thumbnail error", e);
        }

        return ret;
    }

    /**
     * Gets the Gravatar URL for the specified email with the specified size.
     *
     * @param email the specified email
     * @param size the specified size
     * @return the Gravatar URL
     */
    public String getGravatarURL(final String email, final String size) {
        return GRAVATAR + MD5.hash(email) + "?s=" + size + "&d=" + Symphonys.get("defaultThumbnailURL");
    }
}
