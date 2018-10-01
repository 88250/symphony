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

import jodd.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Liveness;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.CharacterRepository;
import org.b3log.symphony.repository.PointtransferRepository;
import org.b3log.symphony.util.Results;
import org.b3log.symphony.util.Symphonys;
import org.b3log.symphony.util.Tesseracts;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Activity management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.6.10.1, Jan 30, 2018
 * @since 1.3.0
 */
@Service
public class ActivityMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ActivityMgmtService.class);

    /**
     * Character repository.
     */
    @Inject
    private CharacterRepository characterRepository;

    /**
     * Pointtransfer repository.
     */
    @Inject
    private PointtransferRepository pointtransferRepository;

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
     * Starts eating snake.
     *
     * @param userId the specified user id
     * @return result
     */
    public synchronized JSONObject startEatingSnake(final String userId) {
        final JSONObject ret = Results.falseResult();

        final int startPoint = pointtransferRepository.getActivityEatingSnakeAvg(userId);

        final boolean succ = null != pointtransferMgmtService.transfer(userId, Pointtransfer.ID_C_SYS,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_EATINGSNAKE,
                startPoint, "", System.currentTimeMillis(), "");

        ret.put(Keys.STATUS_CODE, succ);

        final String msg = succ ? "started" : langPropsService.get("activityStartEatingSnakeFailLabel");
        ret.put(Keys.MSG, msg);

        livenessMgmtService.incLiveness(userId, Liveness.LIVENESS_ACTIVITY);

        return ret;
    }

    /**
     * Collects eating snake.
     *
     * @param userId the specified user id
     * @param score  the specified score
     * @return result
     */
    public synchronized JSONObject collectEatingSnake(final String userId, final int score) {
        final JSONObject ret = Results.falseResult();

        if (score < 1) {
            ret.put(Keys.STATUS_CODE, true);

            return ret;
        }

        final int max = Symphonys.getInt("pointActivityEatingSnakeCollectMax");
        final int amout = score > max ? max : score;

        final boolean succ = null != pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_EATINGSNAKE_COLLECT, amout,
                "", System.currentTimeMillis(), "");

        if (!succ) {
            ret.put(Keys.MSG, "Sorry, transfer point failed, please contact admin");
        }

        ret.put(Keys.STATUS_CODE, succ);

        return ret;
    }

    /**
     * Submits the specified character to recognize.
     *
     * @param userId       the specified user id
     * @param characterImg the specified character image encoded by Base64
     * @param character    the specified character
     * @return recognition result
     */
    public synchronized JSONObject submitCharacter(final String userId, final String characterImg, final String character) {
        String recongnizeFailedMsg = langPropsService.get("activityCharacterRecognizeFailedLabel");

        final JSONObject ret = new JSONObject();
        ret.put(Keys.STATUS_CODE, false);
        ret.put(Keys.MSG, recongnizeFailedMsg);

        if (StringUtils.isBlank(characterImg) || StringUtils.isBlank(character)) {
            ret.put(Keys.STATUS_CODE, false);
            ret.put(Keys.MSG, recongnizeFailedMsg);

            return ret;
        }

        final byte[] data = Base64.decode(characterImg);
        OutputStream stream = null;
        final String tmpDir = System.getProperty("java.io.tmpdir");
        final String imagePath = tmpDir + "/" + userId + "-character.png";

        try {
            stream = new FileOutputStream(imagePath);
            stream.write(data);
            stream.flush();
            stream.close();
        } catch (final IOException e) {
            LOGGER.log(Level.ERROR, "Submits character failed", e);

            return ret;
        } finally {
            if (null != stream) {
                try {
                    stream.close();
                } catch (final IOException ex) {
                    LOGGER.log(Level.ERROR, "Closes stream failed", ex);
                }
            }
        }

        final String recognizedCharacter = Tesseracts.recognizeCharacter(imagePath);
        LOGGER.info("Character [" + character + "], recognized [" + recognizedCharacter + "], image path [" + imagePath
                + "]");
        if (StringUtils.equals(character, recognizedCharacter)) {
            final Query query = new Query();
            query.setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(org.b3log.symphony.model.Character.CHARACTER_USER_ID, FilterOperator.EQUAL, userId),
                    new PropertyFilter(org.b3log.symphony.model.Character.CHARACTER_CONTENT, FilterOperator.EQUAL, character)
            ));

            try {
                if (characterRepository.count(query) > 0) {
                    return ret;
                }
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Count characters failed [userId=" + userId + ", character=" + character + "]", e);

                return ret;
            }

            final JSONObject record = new JSONObject();
            record.put(org.b3log.symphony.model.Character.CHARACTER_CONTENT, character);
            record.put(org.b3log.symphony.model.Character.CHARACTER_IMG, characterImg);
            record.put(org.b3log.symphony.model.Character.CHARACTER_USER_ID, userId);

            String characterId = "";
            final Transaction transaction = characterRepository.beginTransaction();
            try {
                characterId = characterRepository.add(record);

                transaction.commit();
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Submits character failed", e);

                if (null != transaction) {
                    transaction.rollback();
                }

                return ret;
            }

            pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                    Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHARACTER, Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHARACTER,
                    characterId, System.currentTimeMillis(), "");

            ret.put(Keys.STATUS_CODE, true);
            ret.put(Keys.MSG, langPropsService.get("activityCharacterRecognizeSuccLabel"));
        } else {
            recongnizeFailedMsg = recongnizeFailedMsg.replace("{一}", recognizedCharacter);
            ret.put(Keys.STATUS_CODE, false);
            ret.put(Keys.MSG, recongnizeFailedMsg);
        }

        return ret;
    }

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
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHECKIN, sum, userId, System.currentTimeMillis(), "");
        if (!succ) {
            return Integer.MIN_VALUE;
        }

        try {
            final JSONObject user = userQueryService.getUser(userId);

            int currentStreakStart = user.optInt(UserExt.USER_CURRENT_CHECKIN_STREAK_START);
            int currentStreakEnd = user.optInt(UserExt.USER_CURRENT_CHECKIN_STREAK_END);

            final Date today = new Date();
            user.put(UserExt.USER_CHECKIN_TIME, today.getTime());

            final String todayStr = DateFormatUtils.format(today, "yyyyMMdd");
            final int todayInt = Integer.valueOf(todayStr);

            if (0 == currentStreakStart) {
                user.put(UserExt.USER_CURRENT_CHECKIN_STREAK_START, todayInt);
                user.put(UserExt.USER_CURRENT_CHECKIN_STREAK_END, todayInt);
                user.put(UserExt.USER_LONGEST_CHECKIN_STREAK_START, todayInt);
                user.put(UserExt.USER_LONGEST_CHECKIN_STREAK_END, todayInt);
                user.put(UserExt.USER_CURRENT_CHECKIN_STREAK, 1);
                user.put(UserExt.USER_LONGEST_CHECKIN_STREAK, 1);

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

            userMgmtService.updateUser(userId, user);

            if (currentStreakDays > 0 && 0 == currentStreakDays % 10) {
                // Additional Point
                pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                        Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHECKIN_STREAK,
                        Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKINT_STREAK, userId, System.currentTimeMillis(), "");
            }

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
     * @param userId       the specified user id
     * @param amount       the specified amount
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
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_1A0001, amount, date + "-" + smallOrLarge, System.currentTimeMillis(), "");

        ret.put(Keys.STATUS_CODE, succ);

        final String msg = succ
                ? langPropsService.get("activityBetSuccLabel") : langPropsService.get("activityBetFailLabel");
        ret.put(Keys.MSG, msg);

        livenessMgmtService.incLiveness(userId, Liveness.LIVENESS_ACTIVITY);

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

        if (StringUtils.isBlank(smallOrLarge)) {
            ret.put(Keys.MSG, langPropsService.get("activity1A0001CollectFailLabel"));

            return ret;
        }

        ret.put(Keys.STATUS_CODE, true);
        if (StringUtils.equals(smallOrLarge, smallOrLargeResult)) {
            final int amount = sum * 2;

            final boolean succ = null != pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                    Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_1A0001_COLLECT, amount,
                    DateFormatUtils.format(new Date(), "yyyyMMdd") + "-" + smallOrLargeResult, System.currentTimeMillis(), "");

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
     */
    public synchronized void yesterdayLivenessReward(final String userId) {
        if (activityQueryService.isCollectedYesterdayLivenessReward(userId)) {
            return;
        }

        final JSONObject yesterdayLiveness = livenessQueryService.getYesterdayLiveness(userId);
        if (null == yesterdayLiveness) {
            return;
        }

        final int sum = Liveness.calcPoint(yesterdayLiveness);

        if (0 == sum) {
            return;
        }

        boolean succ = null != pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_YESTERDAY_LIVENESS_REWARD, sum, userId, System.currentTimeMillis(), "");
        if (!succ) {
            return;
        }

        // Today liveness (activity)
        livenessMgmtService.incLiveness(userId, Liveness.LIVENESS_ACTIVITY);
    }

    /**
     * Starts Gobang.
     *
     * @param userId the specified user id
     * @return result
     */
    public synchronized JSONObject startGobang(final String userId) {
        final JSONObject ret = Results.falseResult();

        final int startPoint = Pointtransfer.TRANSFER_SUM_C_ACTIVITY_GOBANG_START;

        final boolean succ = null != pointtransferMgmtService.transfer(userId, Pointtransfer.ID_C_SYS,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_GOBANG,
                startPoint, "", System.currentTimeMillis(), "");

        ret.put(Keys.STATUS_CODE, succ);

        final String msg = succ ? "started" : langPropsService.get("activityStartGobangFailLabel");
        ret.put(Keys.MSG, msg);

        livenessMgmtService.incLiveness(userId, Liveness.LIVENESS_ACTIVITY);

        return ret;
    }

    /**
     * Collects Gobang.
     *
     * @param userId the specified user id
     * @param score  the specified score
     * @return result
     */
    public synchronized JSONObject collectGobang(final String userId, final int score) {
        final JSONObject ret = Results.falseResult();

        final boolean succ = null != pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_GOBANG_COLLECT, score,
                "", System.currentTimeMillis(), "");

        if (!succ) {
            ret.put(Keys.MSG, "Sorry, transfer point failed, please contact admin");
        }

        ret.put(Keys.STATUS_CODE, succ);

        return ret;
    }
}
