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

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Liveness;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.util.Results;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Activity management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.6.2, Apr 14, 2016
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
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Timeline management service.
     */
    @Inject
    private TimelineMgmtService timelineMgmtService;

    /**
     * Liveness management service.
     */
    @Inject
    private LivenessMgmtService livenessMgmtService;

    /**
     * Liveness query service.
     */
    @Inject
    private LivenessQueryService livenessQueryService;

    /**
     * Daily checkin.
     *
     * @param userId the specified user id
     * @return {@code Random int} if checkin succeeded, returns {@code Integer.MIN_VALUE} otherwise
     */
    public synchronized int dailyCheckin(final String userId) {
        if (activityQueryService.isCheckedinToday(userId)) {
            return Integer.MIN_VALUE;
        }

        final Random random = new Random();
        final int sum = random.nextInt(Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MAX)
                % (Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MAX - Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MIN + 1)
                + Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MIN;
        final boolean succ = null != pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHECKIN, sum, userId);
        if (!succ) {
            return Integer.MIN_VALUE;
        }

        try {
            final JSONObject user = userQueryService.getUser(userId);

            int currentStreakStart = user.optInt(UserExt.USER_CURRENT_CHECKIN_STREAK_START);
            int currentStreakEnd = user.optInt(UserExt.USER_CURRENT_CHECKIN_STREAK_END);

            final Date today = new Date();
            final String todayStr = DateFormatUtils.format(today, "yyyyMMdd");
            final int todayInt = Integer.valueOf(todayStr);

            if (0 == currentStreakStart) {
                user.put(UserExt.USER_CURRENT_CHECKIN_STREAK_START, todayInt);
                user.put(UserExt.USER_CURRENT_CHECKIN_STREAK_END, todayInt);
                user.put(UserExt.USER_LONGEST_CHECKIN_STREAK_START, todayInt);
                user.put(UserExt.USER_LONGEST_CHECKIN_STREAK_END, todayInt);

                userMgmtService.updateUser(userId, user);

                return sum;
            }

            final Date endDate = DateUtils.parseDate(String.valueOf(currentStreakEnd), new String[]{"yyyyMMdd"});
            final Date nextDate = DateUtils.addDays(endDate, 1);

            if (DateUtils.isSameDay(nextDate, today)) {
                user.put(UserExt.USER_CURRENT_CHECKIN_STREAK_END, todayInt);
            } else {
                user.put(UserExt.USER_CURRENT_CHECKIN_STREAK_START, todayInt);
                user.put(UserExt.USER_CURRENT_CHECKIN_STREAK_END, todayInt);
            }

            currentStreakStart = user.optInt(UserExt.USER_CURRENT_CHECKIN_STREAK_START);
            currentStreakEnd = user.optInt(UserExt.USER_CURRENT_CHECKIN_STREAK_END);
            final int longestStreakStart = user.optInt(UserExt.USER_LONGEST_CHECKIN_STREAK_START);
            final int longestStreakEnd = user.optInt(UserExt.USER_LONGEST_CHECKIN_STREAK_END);

            final Date currentStreakStartDate
                    = DateUtils.parseDate(String.valueOf(currentStreakStart), new String[]{"yyyyMMdd"});
            final Date currentStreakEndDate
                    = DateUtils.parseDate(String.valueOf(currentStreakEnd), new String[]{"yyyyMMdd"});
            final Date longestStreakStartDate
                    = DateUtils.parseDate(String.valueOf(longestStreakStart), new String[]{"yyyyMMdd"});
            final Date longestStreakEndDate
                    = DateUtils.parseDate(String.valueOf(longestStreakEnd), new String[]{"yyyyMMdd"});

            final int currentStreakDays
                    = (int) ((currentStreakEndDate.getTime() - currentStreakStartDate.getTime()) / 86400000) + 1;
            final int longestStreakDays
                    = (int) ((longestStreakEndDate.getTime() - longestStreakStartDate.getTime()) / 86400000) + 1;

            user.put(UserExt.USER_CURRENT_CHECKIN_STREAK, currentStreakDays);
            user.put(UserExt.USER_LONGEST_CHECKIN_STREAK, longestStreakDays);

            if (longestStreakDays < currentStreakDays) {
                user.put(UserExt.USER_LONGEST_CHECKIN_STREAK_START, currentStreakStart);
                user.put(UserExt.USER_LONGEST_CHECKIN_STREAK_END, currentStreakEnd);

                user.put(UserExt.USER_LONGEST_CHECKIN_STREAK, currentStreakDays);
            }

            user.put(UserExt.USER_CHECKIN_TIME, today.getTime());

            userMgmtService.updateUser(userId, user);

            if (currentStreakDays > 0 && 0 == currentStreakDays % 10) {
                // Additional Point
                pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                        Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHECKIN_STREAK,
                        Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKINT_STREAK, userId);
            }

            final String userName = user.optString(User.USER_NAME);

            // Timeline
            final JSONObject timeline = new JSONObject();
            timeline.put(Common.TYPE, Common.ACTIVITY);
            String content = langPropsService.get("timelineActivityCheckinLabel");
            content = content.replace("{user}", "<a target='_blank' rel='nofollow' href='" + Latkes.getServePath()
                    + "/member/" + userName + "'>" + userName + "</a>");
            timeline.put(Common.CONTENT, content);

            timelineMgmtService.addTimeline(timeline);

            // Liveness
            livenessMgmtService.incLiveness(userId, Liveness.LIVENESS_ACTIVITY);

            return sum;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Checkin streak error", e);

            return Integer.MIN_VALUE;
        }
    }

    /**
     * Bets 1A0001.
     *
     * @param userId the specified user id
     * @param amount the specified amount
     * @param smallOrLarge the specified small or large
     * @return result
     */
    public synchronized JSONObject bet1A0001(final String userId, final int amount, final int smallOrLarge) {
        final JSONObject ret = Results.falseResult();

        if (activityQueryService.is1A0001Today(userId)) {
            ret.put(Keys.MSG, langPropsService.get("activityParticipatedLabel"));

            return ret;
        }

        final String date = DateFormatUtils.format(new Date(), "yyyyMMdd");

        final boolean succ = null != pointtransferMgmtService.transfer(userId, Pointtransfer.ID_C_SYS,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_1A0001, amount, date + "-" + smallOrLarge);

        ret.put(Keys.STATUS_CODE, succ);

        final String msg = succ
                ? langPropsService.get("activityBetSuccLabel") : langPropsService.get("activityBetFailLabel");
        ret.put(Keys.MSG, msg);

        try {
            final JSONObject user = userQueryService.getUser(userId);
            final String userName = user.optString(User.USER_NAME);

            // Timeline
            final JSONObject timeline = new JSONObject();
            timeline.put(Common.TYPE, Common.ACTIVITY);
            String content = langPropsService.get("timelineActivity1A0001Label");
            content = content.replace("{user}", "<a target='_blank' rel='nofollow' href='" + Latkes.getServePath()
                    + "/member/" + userName + "'>" + userName + "</a>");
            timeline.put(Common.CONTENT, content);

            timelineMgmtService.addTimeline(timeline);

            // Liveness
            livenessMgmtService.incLiveness(userId, Liveness.LIVENESS_ACTIVITY);
        } catch (final ServiceException e) {
            LOGGER.log(Level.ERROR, "Timeline error", e);
        }

        return ret;
    }

    /**
     * Collects 1A0001.
     *
     * @param userId the specified user id
     * @return result
     */
    public synchronized JSONObject collect1A0001(final String userId) {
        final JSONObject ret = Results.falseResult();

        if (!activityQueryService.is1A0001Today(userId)) {
            ret.put(Keys.MSG, langPropsService.get("activityNotParticipatedLabel"));

            return ret;
        }

        if (activityQueryService.isCollected1A0001Today(userId)) {
            ret.put(Keys.MSG, langPropsService.get("activityParticipatedLabel"));

            return ret;
        }

        final List<JSONObject> records = pointtransferQueryService.getLatestPointtransfers(userId,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_1A0001, 1);
        final JSONObject pointtransfer = records.get(0);
        final String data = pointtransfer.optString(Pointtransfer.DATA_ID);
        final String smallOrLarge = data.split("-")[1];
        final int sum = pointtransfer.optInt(Pointtransfer.SUM);

        String smallOrLargeResult = null;
        try {
            final Document doc = Jsoup.parse(new URL("http://stockpage.10jqka.com.cn/1A0001/quote/header/"), 5000);
            final JSONObject result = new JSONObject(doc.text());
            final String price = result.optJSONObject("data").optJSONObject("1A0001").optString("10");

            if (!price.contains(".")) {
                smallOrLargeResult = "0";
            } else {
                int endInt = 0;
                if (price.split("\\.")[1].length() > 1) {
                    final String end = price.substring(price.length() - 1);
                    endInt = Integer.valueOf(end);
                }

                if (0 <= endInt && endInt <= 4) {
                    smallOrLargeResult = "0";
                } else if (5 <= endInt && endInt <= 9) {
                    smallOrLargeResult = "1";
                } else {
                    LOGGER.error("Activity 1A0001 collect result [" + endInt + "]");
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Collect 1A0001 failed", e);

            ret.put(Keys.MSG, langPropsService.get("activity1A0001CollectFailLabel"));

            return ret;
        }

        if (Strings.isEmptyOrNull(smallOrLarge)) {
            ret.put(Keys.MSG, langPropsService.get("activity1A0001CollectFailLabel"));

            return ret;
        }

        ret.put(Keys.STATUS_CODE, true);
        if (StringUtils.equals(smallOrLarge, smallOrLargeResult)) {
            final int amount = sum * 2;

            final boolean succ = null != pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                    Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_1A0001_COLLECT, amount,
                    DateFormatUtils.format(new Date(), "yyyyMMdd") + "-" + smallOrLargeResult);

            if (succ) {
                String msg = langPropsService.get("activity1A0001CollectSucc1Label");
                msg = msg.replace("{point}", String.valueOf(amount));

                ret.put(Keys.MSG, msg);
            } else {
                ret.put(Keys.MSG, langPropsService.get("activity1A0001CollectFailLabel"));
            }
        } else {
            ret.put(Keys.MSG, langPropsService.get("activity1A0001CollectSucc0Label"));
        }

        return ret;
    }

    /**
     * Collects yesterday's liveness reward.
     *
     * @param userId the specified user id
     * @return {@code Random int} if checkin succeeded, returns {@code Integer.MIN_VALUE} otherwise
     */
    public synchronized int yesterdayLivenessReward(final String userId) {
        if (activityQueryService.isCollectedYesterdayLivenessReward(userId)) {
            return Integer.MIN_VALUE;
        }

        final JSONObject yesterdayLiveness = livenessQueryService.getYesterdayLiveness(userId);
        if (null == yesterdayLiveness) {
            return Integer.MIN_VALUE;
        }

        final int sum = Liveness.calcPoint(yesterdayLiveness);

        boolean succ = null != pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_YESTERDAY_LIVENESS_REWARD, sum, userId);
        if (!succ) {
            return Integer.MIN_VALUE;
        }

        // Liveness
        livenessMgmtService.incLiveness(userId, Liveness.LIVENESS_ACTIVITY);

        return 0;
    }
}
