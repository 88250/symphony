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
package org.b3log.symphony.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.http.Dispatcher;
import org.b3log.latke.http.Request;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.middleware.CSRFMidware;
import org.b3log.symphony.processor.middleware.LoginCheckMidware;
import org.b3log.symphony.processor.middleware.validate.Activity1A0001CollectValidationMidware;
import org.b3log.symphony.processor.middleware.validate.Activity1A0001ValidationMidware;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.StatusCodes;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Activity processor.
 * <ul>
 * <li>Shows activities (/activities), GET</li>
 * <li>Daily checkin (/activity/daily-checkin), GET</li>
 * <li>Shows 1A0001 (/activity/1A0001), GET</li>
 * <li>Bets 1A0001 (/activity/1A0001/bet), POST</li>
 * <li>Collects 1A0001 (/activity/1A0001/collect), POST</li>
 * <li>Shows character (/activity/character), GET</li>
 * <li>Submit character (/activity/character/submit), POST</li>
 * <li>Shows eating snake (/activity/eating-snake), GET</li>
 * <li>Starts eating snake (/activity/eating-snake/start), POST</li>
 * <li>Collects eating snake(/activity/eating-snake/collect), POST</li>
 * <li>Shows gobang (/activity/gobang), GET</li>
 * <li>Starts gobang (/activity/gobang/start), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="https://hacpai.com/member/ZephyrJung">Zephyr</a>
 * @version 2.0.0.1, Jun 20, 2020
 * @since 1.3.0
 */
@Singleton
public class ActivityProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ActivityProcessor.class);

    /**
     * Activity management service.
     */
    @Inject
    private ActivityMgmtService activityMgmtService;

    /**
     * Activity query service.
     */
    @Inject
    private ActivityQueryService activityQueryService;

    /**
     * Character query service.
     */
    @Inject
    private CharacterQueryService characterQueryService;

    /**
     * Pointtransfer query service.
     */
    @Inject
    private PointtransferQueryService pointtransferQueryService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Register request handlers.
     */
    public static void register() {
        final BeanManager beanManager = BeanManager.getInstance();
        final LoginCheckMidware loginCheck = beanManager.getReference(LoginCheckMidware.class);
        final CSRFMidware csrfMidware = beanManager.getReference(CSRFMidware.class);
        final Activity1A0001ValidationMidware activity1A0001ValidationMidware = beanManager.getReference(Activity1A0001ValidationMidware.class);
        final Activity1A0001CollectValidationMidware activity1A0001CollectValidationMidware = beanManager.getReference(Activity1A0001CollectValidationMidware.class);

        final ActivityProcessor activityProcessor = beanManager.getReference(ActivityProcessor.class);
        Dispatcher.get("/activity/character", activityProcessor::showCharacter, loginCheck::handle, csrfMidware::fill);
        Dispatcher.post("/activity/character/submit", activityProcessor::submitCharacter, loginCheck::handle);
        Dispatcher.get("/activities", activityProcessor::showActivities);
        Dispatcher.get("/activity/checkin", activityProcessor::showDailyCheckin);
        Dispatcher.get("/activity/daily-checkin", activityProcessor::dailyCheckin, loginCheck::handle);
        Dispatcher.get("/activity/yesterday-liveness-reward", activityProcessor::yesterdayLivenessReward, loginCheck::handle);
        Dispatcher.get("/activity/1A0001", activityProcessor::show1A0001, csrfMidware::fill);
        Dispatcher.post("/activity/1A0001/bet", activityProcessor::bet1A0001, loginCheck::handle, csrfMidware::check, activity1A0001ValidationMidware::handle);
        Dispatcher.post("/activity/1A0001/collect", activityProcessor::collect1A0001, loginCheck::handle, activity1A0001CollectValidationMidware::handle);
        Dispatcher.get("/activity/eating-snake", activityProcessor::showEatingSnake, loginCheck::handle, csrfMidware::fill);
        Dispatcher.post("/activity/eating-snake/start", activityProcessor::startEatingSnake, loginCheck::handle, csrfMidware::check);
        Dispatcher.post("/activity/eating-snake/collect", activityProcessor::collectEatingSnake, loginCheck::handle, csrfMidware::fill);
        Dispatcher.get("/activity/gobang", activityProcessor::showGobang, loginCheck::handle, csrfMidware::fill);
        Dispatcher.post("/activity/gobang/start", activityProcessor::startGobang, loginCheck::handle);
    }

    /**
     * Shows 1A0001.
     *
     * @param context the specified context
     */
    public void showCharacter(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "activity/character.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModelService.fillHeaderAndFooter(context, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        final JSONObject user = Sessions.getUser();
        final String userId = user.optString(Keys.OBJECT_ID);

        String activityCharacterGuideLabel = langPropsService.get("activityCharacterGuideLabel");

        final String character = characterQueryService.getUnwrittenCharacter(userId);
        if (StringUtils.isBlank(character)) {
            dataModel.put("noCharacter", true);
            return;
        }

        final int totalCharacterCount = characterQueryService.getTotalCharacterCount();
        final int writtenCharacterCount = characterQueryService.getWrittenCharacterCount();
        final String totalProgress = String.format("%.2f", (double) writtenCharacterCount / (double) totalCharacterCount * 100);
        dataModel.put("totalProgress", totalProgress);

        final int userWrittenCharacterCount = characterQueryService.getWrittenCharacterCount(userId);
        final String userProgress = String.format("%.2f", (double) userWrittenCharacterCount / (double) totalCharacterCount * 100);
        dataModel.put("userProgress", userProgress);

        activityCharacterGuideLabel = activityCharacterGuideLabel.replace("{character}", character);
        dataModel.put("activityCharacterGuideLabel", activityCharacterGuideLabel);
    }

    /**
     * Submits character.
     *
     * @param context the specified context
     */
    public void submitCharacter(final RequestContext context) {
        final Request request = context.getRequest();
        context.renderJSON(StatusCodes.ERR);

        JSONObject requestJSONObject;
        try {
            requestJSONObject = context.requestJSON();
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Submits character failed", e);
            context.renderMsg(langPropsService.get("activityCharacterRecognizeFailedLabel"));
            return;
        }

        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final String dataURL = requestJSONObject.optString("dataURL");
        final String dataPart = StringUtils.substringAfter(dataURL, ",");
        final String character = requestJSONObject.optString("character");

        final JSONObject result = activityMgmtService.submitCharacter(userId, dataPart, character);
        context.renderJSON(result);
    }

    /**
     * Shows activity page.
     *
     * @param context the specified context
     */
    public void showActivities(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "home/activities.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(context, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        dataModel.put("pointActivityCheckinMin", Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MIN);
        dataModel.put("pointActivityCheckinMax", Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MAX);
        dataModel.put("pointActivityCheckinStreak", Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKINT_STREAK);
        dataModel.put("activitYesterdayLivenessRewardMaxPoint", Symphonys.ACTIVITY_YESTERDAY_REWARD_MAX);
    }

    /**
     * Shows daily checkin page.
     *
     * @param context the specified context
     */
    public void showDailyCheckin(final RequestContext context) {
        final JSONObject user = Sessions.getUser();
        final String userId = user.optString(Keys.OBJECT_ID);
        if (activityQueryService.isCheckedinToday(userId)) {
            context.sendRedirect(Latkes.getServePath() + "/member/" + user.optString(User.USER_NAME) + "/points");
            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "activity/checkin.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(context, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);
    }

    /**
     * Daily checkin.
     *
     * @param context the specified context
     */
    public void dailyCheckin(final RequestContext context) {
        final JSONObject user = Sessions.getUser();
        final String userId = user.optString(Keys.OBJECT_ID);
        activityMgmtService.dailyCheckin(userId);

        context.sendRedirect(Latkes.getServePath() + "/member/" + user.optString(User.USER_NAME) + "/points");
    }

    /**
     * Yesterday liveness reward.
     *
     * @param context the specified context
     */
    public void yesterdayLivenessReward(final RequestContext context) {
        final Request request = context.getRequest();
        final JSONObject user = Sessions.getUser();
        final String userId = user.optString(Keys.OBJECT_ID);

        activityMgmtService.yesterdayLivenessReward(userId);

        context.sendRedirect(Latkes.getServePath() + "/member/" + user.optString(User.USER_NAME) + "/points");
    }

    /**
     * Shows 1A0001.
     *
     * @param context the specified context
     */
    public void show1A0001(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "activity/1A0001.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        final boolean closed = Symphonys.ACTIVITY_1A0001_CLOSED;
        dataModel.put(Common.CLOSED, closed);

        final Calendar calendar = Calendar.getInstance();
        final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        final boolean closed1A0001 = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
        dataModel.put(Common.CLOSED_1A0001, closed1A0001);

        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        final boolean end = hour > 14 || (hour == 14 && minute > 55);
        dataModel.put(Common.END, end);

        final boolean collected = activityQueryService.isCollected1A0001Today(userId);
        dataModel.put(Common.COLLECTED, collected);

        final boolean participated = activityQueryService.is1A0001Today(userId);
        dataModel.put(Common.PARTICIPATED, participated);

        while (true) {
            if (closed) {
                dataModel.put(Keys.MSG, langPropsService.get("activityClosedLabel"));
                break;
            }

            if (closed1A0001) {
                dataModel.put(Keys.MSG, langPropsService.get("activity1A0001CloseLabel"));
                break;
            }

            if (collected) {
                dataModel.put(Keys.MSG, langPropsService.get("activityParticipatedLabel"));
                break;
            }

            if (participated) {
                dataModel.put(Common.HOUR, hour);

                final List<JSONObject> records = pointtransferQueryService.getLatestPointtransfers(userId,
                        Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_1A0001, 1);
                final JSONObject pointtransfer = records.get(0);
                final String data = pointtransfer.optString(Pointtransfer.DATA_ID);
                final String smallOrLarge = data.split("-")[1];
                final int sum = pointtransfer.optInt(Pointtransfer.SUM);
                String msg = langPropsService.get("activity1A0001BetedLabel");
                final String small = langPropsService.get("activity1A0001BetSmallLabel");
                final String large = langPropsService.get("activity1A0001BetLargeLabel");
                msg = msg.replace("{smallOrLarge}", StringUtils.equals(smallOrLarge, "0") ? small : large);
                msg = msg.replace("{point}", String.valueOf(sum));

                dataModel.put(Keys.MSG, msg);
                break;
            }

            if (end) {
                dataModel.put(Keys.MSG, langPropsService.get("activityEndLabel"));
                break;
            }
            break;
        }

        dataModelService.fillHeaderAndFooter(context, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);
    }

    /**
     * Bets 1A0001.
     *
     * @param context the specified context
     */
    public void bet1A0001(final RequestContext context) {
        context.renderJSON(StatusCodes.ERR);
        final JSONObject requestJSONObject = (JSONObject) context.attr(Keys.REQUEST);
        final int amount = requestJSONObject.optInt(Common.AMOUNT);
        final int smallOrLarge = requestJSONObject.optInt(Common.SMALL_OR_LARGE);
        final JSONObject currentUser = Sessions.getUser();
        final String fromId = currentUser.optString(Keys.OBJECT_ID);
        final JSONObject ret = activityMgmtService.bet1A0001(fromId, amount, smallOrLarge);
        if (StatusCodes.SUCC == ret.optInt(Keys.CODE)) {
            String msg = langPropsService.get("activity1A0001BetedLabel");
            final String small = langPropsService.get("activity1A0001BetSmallLabel");
            final String large = langPropsService.get("activity1A0001BetLargeLabel");
            msg = msg.replace("{smallOrLarge}", smallOrLarge == 0 ? small : large);
            msg = msg.replace("{point}", String.valueOf(amount));
            context.renderCodeMsg(StatusCodes.SUCC, msg);
        }
    }

    /**
     * Collects 1A0001.
     *
     * @param context the specified context
     */
    public void collect1A0001(final RequestContext context) {
        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        final JSONObject ret = activityMgmtService.collect1A0001(userId);

        context.renderJSON(ret);
    }

    /**
     * Shows eating snake.
     *
     * @param context the specified context
     */
    public void showEatingSnake(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "activity/eating-snake.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(context, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        final List<JSONObject> maxUsers = activityQueryService.getTopEatingSnakeUsersMax(10);
        dataModel.put("maxUsers", maxUsers);

        final List<JSONObject> sumUsers = activityQueryService.getTopEatingSnakeUsersSum(10);
        dataModel.put("sumUsers", sumUsers);

        final JSONObject user = Sessions.getUser();
        final String userId = user.optString(Keys.OBJECT_ID);
        final int startPoint = activityQueryService.getEatingSnakeAvgPoint(userId);

        String pointActivityEatingSnake = langPropsService.get("activityStartEatingSnakeTipLabel");
        pointActivityEatingSnake = pointActivityEatingSnake.replace("{point}", String.valueOf(startPoint));
        dataModel.put("activityStartEatingSnakeTipLabel", pointActivityEatingSnake);
    }

    /**
     * Starts eating snake.
     *
     * @param context the specified context
     */
    public void startEatingSnake(final RequestContext context) {
        final Request request = context.getRequest();
        final JSONObject currentUser = Sessions.getUser();
        final String fromId = currentUser.optString(Keys.OBJECT_ID);

        final JSONObject ret = activityMgmtService.startEatingSnake(fromId);

        context.renderJSON(ret);
    }

    /**
     * Collects eating snake.
     *
     * @param context the specified context
     */
    public void collectEatingSnake(final RequestContext context) {
        JSONObject requestJSONObject;
        try {
            requestJSONObject = context.requestJSON();
            final int score = requestJSONObject.optInt("score");
            final JSONObject user = Sessions.getUser();
            final JSONObject ret = activityMgmtService.collectEatingSnake(user.optString(Keys.OBJECT_ID), score);
            context.renderJSON(ret);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Collects eating snake game failed", e);
            context.renderCodeMsg(StatusCodes.ERR, "err....");
        }
    }

    /**
     * Shows gobang.
     *
     * @param context the specified context
     */
    public void showGobang(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "activity/gobang.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(context, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);

        String pointActivityGobang = langPropsService.get("activityStartGobangTipLabel");
        pointActivityGobang = pointActivityGobang.replace("{point}", String.valueOf(Pointtransfer.TRANSFER_SUM_C_ACTIVITY_GOBANG_START));
        dataModel.put("activityStartGobangTipLabel", pointActivityGobang);
    }

    /**
     * Starts gobang.
     *
     * @param context the specified context
     */
    public void startGobang(final RequestContext context) {
        final JSONObject ret = new JSONObject().put(Keys.CODE, StatusCodes.SUCC);
        final JSONObject currentUser = Sessions.getUser();
        final boolean succ = currentUser.optInt(UserExt.USER_POINT) - Pointtransfer.TRANSFER_SUM_C_ACTIVITY_GOBANG_START >= 0;
        if (succ) {
            ret.put(Keys.CODE, StatusCodes.SUCC);
        }
        final String msg = succ ? "started" : langPropsService.get("activityStartGobangFailLabel");
        ret.put(Keys.MSG, msg);
        context.renderJSON(ret);
    }
}
