/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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

import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.PointtransferRepository;
import org.b3log.symphony.repository.UserRepository;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Activity query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.5.2.0, May 9, 2018
 * @since 1.3.0
 */
@Service
public class ActivityQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ActivityQueryService.class);

    /**
     * Pointtransfer repository.
     */
    @Inject
    private PointtransferRepository pointtransferRepository;

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
     * Gets average point of activity eating snake of a user specified by the given user id.
     *
     * @param userId the given user id
     * @return average point, if the point small than {@code 1}, returns {@code pointActivityEatingSnake} which
     * configured in sym.properties
     */
    public int getEatingSnakeAvgPoint(final String userId) {
        return pointtransferRepository.getActivityEatingSnakeAvg(userId);
    }

    /**
     * Gets the top eating snake users (single game max) with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return users, returns an empty list if not found
     */
    public List<JSONObject> getTopEatingSnakeUsersMax(final int fetchSize) {
        final List<JSONObject> ret = new ArrayList<>();

        try {
            final List<JSONObject> users = userRepository.select("SELECT\n"
                    + "	u.*, MAX(sum) AS point\n"
                    + "FROM\n"
                    + "	" + pointtransferRepository.getName() + " AS p,\n"
                    + "	" + userRepository.getName() + " AS u\n"
                    + "WHERE\n"
                    + "	p.toId = u.oId\n"
                    + "AND type = 27\n"
                    + "GROUP BY\n"
                    + "	toId\n"
                    + "ORDER BY\n"
                    + "	point DESC\n"
                    + "LIMIT ?", fetchSize);

            for (final JSONObject user : users) {
                avatarQueryService.fillUserAvatarURL(user);

                ret.add(user);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets top eating snake users failed", e);
        }

        return ret;
    }

    /**
     * Gets the top eating snake users (sum) with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return users, returns an empty list if not found
     */
    public List<JSONObject> getTopEatingSnakeUsersSum(final int fetchSize) {
        final List<JSONObject> ret = new ArrayList<>();

        try {
            final List<JSONObject> users = userRepository.select("SELECT\n"
                    + "	u.*, Sum(sum) AS point\n"
                    + "FROM\n"
                    + "	" + pointtransferRepository.getName() + " AS p,\n"
                    + "	" + userRepository.getName() + " AS u\n"
                    + "WHERE\n"
                    + "	p.toId = u.oId\n"
                    + "AND type = 27\n"
                    + "GROUP BY\n"
                    + "	toId\n"
                    + "ORDER BY\n"
                    + "	point DESC\n"
                    + "LIMIT ?", fetchSize);

            for (final JSONObject user : users) {
                avatarQueryService.fillUserAvatarURL(user);

                ret.add(user);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets top eating snake users failed", e);
        }

        return ret;
    }

    /**
     * Gets the top checkin users with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return users, returns an empty list if not found
     */
    public List<JSONObject> getTopCheckinUsers(final int fetchSize) {
        final List<JSONObject> ret = new ArrayList<>();
        final Query query = new Query().addSort(UserExt.USER_LONGEST_CHECKIN_STREAK, SortDirection.DESCENDING).
                addSort(UserExt.USER_CURRENT_CHECKIN_STREAK, SortDirection.DESCENDING).
                setPage(1, fetchSize);
        try {
            final JSONObject result = userRepository.get(query);
            final List<JSONObject> users = (List<JSONObject>) result.opt(Keys.RESULTS);
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
            LOGGER.log(Level.ERROR, "Gets top checkin users failed", e);
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
            final JSONObject user = userRepository.get(userId);
            final long time = user.optLong(UserExt.USER_CHECKIN_TIME);
            if (DateUtils.isSameDay(new Date(), new Date(time))) {
                return true;
            }

            // 使用缓存检查在某个竞态条件下会有问题。如果缓存检查没有签到，则再查库判断
            final List<JSONObject> latestPointtransfers = pointtransferQueryService.getLatestPointtransfers(userId, Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHECKIN, 1);
            if (latestPointtransfers.isEmpty()) {
                return false;
            }

            final JSONObject latestPointtransfer = latestPointtransfers.get(0);
            final long checkinTime = latestPointtransfer.optLong(Pointtransfer.TIME);

            return DateUtils.isSameDay(new Date(), new Date(checkinTime));
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
