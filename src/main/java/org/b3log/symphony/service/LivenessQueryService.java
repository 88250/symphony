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
package org.b3log.symphony.service;

import java.util.Date;
import javax.inject.Inject;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.Liveness;
import org.b3log.symphony.repository.LivenessRepository;
import org.json.JSONObject;

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
    private static final Logger LOGGER = Logger.getLogger(LivenessQueryService.class.getName());

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
