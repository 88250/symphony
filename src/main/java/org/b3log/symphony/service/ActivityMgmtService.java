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

import java.util.Random;
import javax.inject.Inject;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Pointtransfer;

/**
 * Activity management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jul 2, 2015
 * @since 1.3.0
 */
@Service
public class ActivityMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ActivityMgmtService.class.getName());

    /**
     * Pointtransfer query service.
     */
    @Inject
    private PointtransferQueryService pointtransferQueryService;

    /**
     * Pointtransfer management service.
     */
    @Inject
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Activity query service.
     */
    @Inject
    private ActivityQueryService activityQueryService;

    /**
     * Daily checkin.
     *
     * @param userId the specified user id
     * @return {@code true} if checkin succeeded, returns {@code false} otherwise
     */
    public synchronized boolean dailyCheckin(final String userId) {
        if (activityQueryService.isCheckedin(userId)) {
            return false;
        }

        final Random random = new Random();
        final int sum = random.nextInt(Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MAX)
                % (Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MAX - Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MIN + 1)
                + Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MIN;
        pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHECKIN, sum, userId);

        return true;
    }
}
