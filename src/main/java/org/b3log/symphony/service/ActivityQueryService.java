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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Activity query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jul 2, 2015
 * @since 1.3.0
 */
@Service
public class ActivityQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ActivityQueryService.class.getName());

    /**
     * Pointtransfer query service.
     */
    @Inject
    private PointtransferQueryService pointtransferQueryService;

    /**
     * Does checkin today?
     *
     * @param userId the specified user id
     * @return {@code true} if checkin succeeded, returns {@code false} otherwise
     */
    public synchronized boolean isCheckedin(final String userId) {
        final Calendar calendar = Calendar.getInstance();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < Symphonys.getInt("activityDailyCheckinTimeMin")
                || hour > Symphonys.getInt("activityDailyCheckinTimeMax")) {
            return true;
        }

        final Date now = new Date();

        final List<JSONObject> records = pointtransferQueryService.getLatestPointtransfers(userId,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHECKIN, 1);
        if (records.isEmpty()) {
            return false;
        }

        final JSONObject maybeToday = records.get(0);
        final long time = maybeToday.optLong(Pointtransfer.TIME);

        return DateUtils.isSameDay(now, new Date(time));
    }
}
