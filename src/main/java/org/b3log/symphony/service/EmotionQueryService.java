/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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
package org.b3log.symphony.service;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.repository.EmotionRepository;

/**
 * Emotion query service.
 *
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Apr 25, 2017
 * @since 1.5.0
 */
@Service
public class EmotionQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(EmotionQueryService.class);

    /**
     * Common used emoji string.
     */
    private static final String COMMON_USED = "smile,flushed,joy,sob,yum,trollface,tada,heart,+1,ok_hand,pray";

    /**
     * Emotion repository.
     */
    @Inject
    private EmotionRepository emotionRepository;

    /**
     * Gets a user's emotion (emoji with type=0).
     *
     * @param userId the specified user id
     * @return emoji string join with {@code ","}, returns a common used emoji string if not found
     */
    public String getEmojis(final String userId) {
        try {
            final String ret = emotionRepository.getUserEmojis(userId);
            if (StringUtils.isBlank(ret)) {
                return COMMON_USED;
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, e.getMessage());

            return COMMON_USED;
        }
    }
}
