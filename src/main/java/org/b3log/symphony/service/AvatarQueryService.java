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
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * User avatar query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.1, Jun 26, 2015
 * @since 0.3.0
 */
@Service
public class AvatarQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AvatarQueryService.class.getName());

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Default avatar URL.
     */
    private static final String DEFAULT_AVATAR_URL = Symphonys.get("defaultThumbnailURL");

    /**
     * Fills the specified user thumbnail URL.
     *
     * @param user the specified user
     */
    public void fillUserAvatarURL(final JSONObject user) {
        final String originalURL = user.optString(UserExt.USER_AVATAR_URL);
        if (!StringUtils.startsWith(originalURL, Symphonys.get("qiniu.domain"))) {
            user.put(UserExt.USER_AVATAR_URL, DEFAULT_AVATAR_URL);
            
            return;
        }

        user.put(UserExt.USER_AVATAR_URL, StringUtils.substringBeforeLast(originalURL, "?"));
    }

    /**
     * Gets the avatar URL for the specified email with the specified size.
     *
     * @param email the specified email
     * @return the avatar URL
     */
    public String getAvatarURL(final String email) {
        try {
            final JSONObject user = userRepository.getByEmail(email);
            if (null == user) {
                return DEFAULT_AVATAR_URL;
            }

            final String originalURL = user.optString(UserExt.USER_AVATAR_URL);
            if (!StringUtils.startsWith(originalURL, Symphonys.get("qiniu.domain"))) {
                return DEFAULT_AVATAR_URL;
            }

            return StringUtils.substringBeforeLast(originalURL, "?");
        } catch (final RepositoryException e) {
            LOGGER.log(Level.WARN, "Gets user avatar error", e);

            return DEFAULT_AVATAR_URL;
        }
    }
}
