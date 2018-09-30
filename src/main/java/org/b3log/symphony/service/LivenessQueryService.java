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

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.Liveness;
import org.b3log.symphony.repository.LivenessRepository;
import org.json.JSONObject;

import java.util.Date;

/**
 * Liveness query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Mar 23, 2016
 * @since 1.4.0
 */
@Service
public class LivenessQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LivenessQueryService.class);

    /**
     * Liveness repository.
     */
    @Inject
    private LivenessRepository livenessRepository;

    /**
     * Gets point of current liveness.
     *
     * @param userId the specified user id
     * @return point
     */
    public int getCurrentLivenessPoint(final String userId) {
        Stopwatchs.start("Gets liveness");
        try {
            final String date = DateFormatUtils.format(new Date(), "yyyyMMdd");

            try {
                final JSONObject liveness = livenessRepository.getByUserAndDate(userId, date);
                if (null == liveness) {
                    return 0;
                }

                return Liveness.calcPoint(liveness);
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Gets current liveness point failed", e);

                return 0;
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the yesterday's liveness.
     *
     * @param userId the specified user id
     * @return yesterday's liveness, returns {@code null} if not found
     */
    public JSONObject getYesterdayLiveness(final String userId) {
        final Date yesterday = DateUtils.addDays(new Date(), -1);
        final String date = DateFormatUtils.format(yesterday, "yyyyMMdd");

        try {
            return livenessRepository.getByUserAndDate(userId, date);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets yesterday's liveness failed", e);

            return null;
        }
    }
}
