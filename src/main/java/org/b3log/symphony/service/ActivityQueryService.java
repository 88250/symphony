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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.cache.UserCache;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Activity query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.4.1.1, Apr 14, 2016
 * @since 1.3.0
 */
@Service
public class ActivityQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ActivityQueryService.class.getName());

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Pointtransfer query service.
     */
    @Inject
    private PointtransferQueryService pointtransferQueryService;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * User cache.
     */
    @Inject
    private UserCache userCache;

    /**
     * Gets the top checkin users with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return users, returns an empty list if not found
     */
    public List<JSONObject> getTopCheckinUsers(final int fetchSize) {
        final List<JSONObject> ret = new ArrayList<JSONObject>();

        final Query query = new Query().addSort(UserExt.USER_LONGEST_CHECKIN_STREAK, SortDirection.DESCENDING).
                addSort(UserExt.USER_CURRENT_CHECKIN_STREAK, SortDirection.DESCENDING).
                setCurrentPageNum(1).setPageSize(fetchSize);

        try {
            final JSONObject result = userRepository.get(query);
            final List<JSONObject> users = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final JSONObject user : users) {
                if (UserExt.USER_APP_ROLE_C_HACKER == user.optInt(UserExt.USER_APP_ROLE)) {
                    user.put(UserExt.USER_T_POINT_HEX, Integer.toHexString(user.optInt(UserExt.USER_POINT)));
                } else {
                    user.put(UserExt.USER_T_POINT_CC, UserExt.toCCString(user.optInt(UserExt.USER_POINT)));
                }

                avatarQueryService.fillUserAvatarURL(user);

                ret.add(user);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets top checkin users error", e);
        }

        return ret;
    }

    /**
     * Does checkin today?
     *
     * @param userId the specified user id
     * @return {@code true} if checkin succeeded, returns {@code false} otherwise
     */
    public synchronized boolean isCheckedinToday(final String userId) {
        Stopwatchs.start("Checks checkin");
        try {
            final Calendar calendar = Calendar.getInstance();
            final int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour < Symphonys.getInt("activityDailyCheckinTimeMin")
                    || hour > Symphonys.getInt("activityDailyCheckinTimeMax")) {
                return true;
            }

            final Date now = new Date();

            final JSONObject user = userRepository.get(userId);

            final long time = user.optLong(UserExt.USER_CHECKIN_TIME);
            return DateUtils.isSameDay(now, new Date(time));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Checks checkin failed", e);

            return true;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Does participate 1A0001 today?
     *
     * @param userId the specified user id
     * @return {@code true} if participated, returns {@code false} otherwise
     */
    public synchronized boolean is1A0001Today(final String userId) {
        final Date now = new Date();

        final List<JSONObject> records = pointtransferQueryService.getLatestPointtransfers(userId,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_1A0001, 1);
        if (records.isEmpty()) {
            return false;
        }

        final JSONObject maybeToday = records.get(0);
        final long time = maybeToday.optLong(Pointtransfer.TIME);

        return DateUtils.isSameDay(now, new Date(time));
    }

    /**
     * Did collect 1A0001 today?
     *
     * @param userId the specified user id
     * @return {@code true} if collected, returns {@code false} otherwise
     */
    public synchronized boolean isCollected1A0001Today(final String userId) {
        final Date now = new Date();

        final List<JSONObject> records = pointtransferQueryService.getLatestPointtransfers(userId,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_1A0001_COLLECT, 1);
        if (records.isEmpty()) {
            return false;
        }

        final JSONObject maybeToday = records.get(0);
        final long time = maybeToday.optLong(Pointtransfer.TIME);

        return DateUtils.isSameDay(now, new Date(time));
    }

    /**
     * Did collect yesterday's liveness reward?
     *
     * @param userId the specified user id
     * @return {@code true} if collected, returns {@code false} otherwise
     */
    public synchronized boolean isCollectedYesterdayLivenessReward(final String userId) {
        final Date now = new Date();

        final List<JSONObject> records = pointtransferQueryService.getLatestPointtransfers(userId,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_YESTERDAY_LIVENESS_REWARD, 1);
        if (records.isEmpty()) {
            return false;
        }

        final JSONObject maybeToday = records.get(0);
        final long time = maybeToday.optLong(Pointtransfer.TIME);

        return DateUtils.isSameDay(now, new Date(time));
    }
}
