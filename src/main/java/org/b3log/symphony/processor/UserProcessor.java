/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.processor;

import com.qiniu.util.Auth;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.*;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.*;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.*;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * User processor.
 * <p>
 * For user
 * <ul>
 * <li>User articles (/member/{userName}), GET</li>
 * <li>User comments (/member/{userName}/comments), GET</li>
 * <li>User following users (/member/{userName}/following/users), GET</li>
 * <li>User following tags (/member/{userName}/following/tags), GET</li>
 * <li>User following articles (/member/{userName}/following/articles), GET</li>
 * <li>User followers (/member/{userName}/followers), GET</li>
 * <li>User points (/member/{userName}/points), GET</li>
 * <li>Shows settings (/settings), GET</li>
 * <li>Shows settings pages (/settings/*), GET</li>
 * <li>Updates profiles (/settings/profiles), POST</li>
 * <li>Updates user avatar (/settings/avatar), POST</li>
 * <li>Geo status (/settings/geo/status), POST</li>
 * <li>Transfer point (/point/transfer), POST</li>
 * <li>Sync (/settings/sync/b3), POST</li>
 * <li>Privacy (/settings/privacy), POST</li>
 * <li>Function (/settings/function), POST</li>
 * <li>Updates emotions (/settings/emotionList), POST</li>
 * <li>Password (/settings/password), POST</li>
 * <li>Point buy invitecode (/point/buy-invitecode), POST</li>
 * <li>SyncUser (/apis/user), POST</li>
 * <li>Lists usernames (/users/names), GET</li>
 * <li>Lists emotions (/users/emotions), GET</li>
 * <li>Exports posts(article/comment) to a file (/export/posts), POST</li>
 * <li>Queries invitecode state (/invitecode/state), GET</li>
 * <li>Shows link forge (/member/{userName}/forge/link), GET</li>
 * <li>i18n (/settings/i18n), POST</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.26.14.35, Jan 20, 2017
 * @since 0.2.0
 */
@RequestProcessor
public class UserProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UserProcessor.class.getName());

    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    /**
     * Article management service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    /**
     * Emotion query service.
     */
    @Inject
    private EmotionQueryService emotionQueryService;

    /**
     * Emotion management service.
     */
    @Inject
    private EmotionMgmtService emotionMgmtService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

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
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Post export service.
     */
    @Inject
    private PostExportService postExportService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Invitecode management service.
     */
    @Inject
    private InvitecodeMgmtService invitecodeMgmtService;

    /**
     * Invitecode query service.
     */
    @Inject
    private InvitecodeQueryService invitecodeQueryService;

    /**
     * Link forge query service.
     */
    @Inject
    private LinkForgeQueryService linkForgeQueryService;

    /**
     * Role query service.
     */
    @Inject
    private RoleQueryService roleQueryService;

    /**
     * Updates user i18n.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     */
    @RequestProcessing(value = "/settings/i18n", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class})
    public void updateI18n(final HTTPRequestContext context,
                           final HttpServletRequest request, final HttpServletResponse response) {
        context.renderJSON();

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            requestJSONObject = new JSONObject();
        }

        String userLanguage = requestJSONObject.optString(UserExt.USER_LANGUAGE, Locale.SIMPLIFIED_CHINESE.toString());
        if (!Languages.getAvailableLanguages().contains(userLanguage)) {
            userLanguage = Locale.US.toString();
        }

        String userTimezone = requestJSONObject.optString(UserExt.USER_TIMEZONE, TimeZone.getDefault().getID());
        if (!Arrays.asList(TimeZone.getAvailableIDs()).contains(userTimezone)) {
            userTimezone = TimeZone.getDefault().getID();
        }

        try {
            final JSONObject user = userQueryService.getCurrentUser(request);
            user.put(UserExt.USER_LANGUAGE, userLanguage);
            user.put(UserExt.USER_TIMEZONE, userTimezone);

            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Shows user link forge.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/forge/link", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, UserBlockCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showLinkForge(final HTTPRequestContext context, final HttpServletRequest request,
                              final HttpServletResponse response, final String userName) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/link-forge.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));
        fillHomeUser(dataModel, user);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        final List<JSONObject> tags = linkForgeQueryService.getUserForgedLinks(user.optString(Keys.OBJECT_ID));
        dataModel.put(Tag.TAGS, (Object) tags);

        dataModel.put(Common.TYPE, "linkForge");
    }

    /**
     * Queries invitecode state.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/invitecode/state", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class})
    public void queryInvitecode(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONObject ret = Results.falseResult();
        context.renderJSON(ret);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        String invitecode = requestJSONObject.optString(Invitecode.INVITECODE);
        if (StringUtils.isBlank(invitecode)) {
            ret.put(Keys.STATUS_CODE, -1);
            ret.put(Keys.MSG, invitecode + " " + langPropsService.get("notFoundInvitecodeLabel"));

            return;
        }

        invitecode = invitecode.trim();

        final JSONObject result = invitecodeQueryService.getInvitecode(invitecode);

        if (null == result) {
            ret.put(Keys.STATUS_CODE, -1);
            ret.put(Keys.MSG, langPropsService.get("notFoundInvitecodeLabel"));
        } else {
            final int status = result.optInt(Invitecode.STATUS);
            ret.put(Keys.STATUS_CODE, status);

            switch (status) {
                case Invitecode.STATUS_C_USED:
                    ret.put(Keys.MSG, langPropsService.get("invitecodeUsedLabel"));

                    break;
                case Invitecode.STATUS_C_UNUSED:
                    String msg = langPropsService.get("invitecodeOkLabel");
                    msg = msg.replace("${time}", DateFormatUtils.format(result.optLong(Keys.OBJECT_ID)
                            + Symphonys.getLong("invitecode.expired"), "yyyy-MM-dd HH:mm"));

                    ret.put(Keys.MSG, msg);

                    break;
                case Invitecode.STATUS_C_STOPUSE:
                    ret.put(Keys.MSG, langPropsService.get("invitecodeStopLabel"));

                    break;
                default:
                    ret.put(Keys.MSG, langPropsService.get("notFoundInvitecodeLabel"));
            }
        }
    }

    /**
     * Point buy invitecode.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/point/buy-invitecode", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    public void pointBuy(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONObject ret = Results.falseResult();
        context.renderJSON(ret);

        final String allowRegister = optionQueryService.getAllowRegister();
        if (!"2".equals(allowRegister)) {
            return;
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String fromId = currentUser.optString(Keys.OBJECT_ID);
        final String userName = currentUser.optString(User.USER_NAME);

        final String invitecode = invitecodeMgmtService.userGenInvitecode(fromId, userName);

        final String transferId = pointtransferMgmtService.transfer(fromId, Pointtransfer.ID_C_SYS,
                Pointtransfer.TRANSFER_TYPE_C_BUY_INVITECODE, Pointtransfer.TRANSFER_SUM_C_BUY_INVITECODE,
                invitecode, System.currentTimeMillis());
        final boolean succ = null != transferId;
        ret.put(Keys.STATUS_CODE, succ);
        if (!succ) {
            ret.put(Keys.MSG, langPropsService.get("exchangeFailedLabel"));
        } else {
            String msg = langPropsService.get("expireTipLabel");
            msg = msg.replace("${time}", DateFormatUtils.format(System.currentTimeMillis()
                    + Symphonys.getLong("invitecode.expired"), "yyyy-MM-dd HH:mm"));
            ret.put(Keys.MSG, invitecode + " " + msg);
        }
    }

    /**
     * Shows settings pages.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = {"/settings", "/settings/*"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    public void showSettings(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        final String requestURI = request.getRequestURI();
        String page = StringUtils.substringAfter(requestURI, "/settings/");
        if (StringUtils.isBlank(page)) {
            page = "profile";
        }
        page += ".ftl";
        renderer.setTemplateName("/home/settings/" + page);
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));
        fillHomeUser(dataModel, user);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String userId = user.optString(Keys.OBJECT_ID);

        final int invitedUserCount = userQueryService.getInvitedUserCount(userId);
        dataModel.put(Common.INVITED_USER_COUNT, invitedUserCount);

        // Qiniu file upload authenticate
        final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
        final String uploadToken = auth.uploadToken(Symphonys.get("qiniu.bucket"));
        dataModel.put("qiniuUploadToken", uploadToken);
        dataModel.put("qiniuDomain", Symphonys.get("qiniu.domain"));

        if (!Symphonys.getBoolean("qiniu.enabled")) {
            dataModel.put("qiniuUploadToken", "");
        }

        final long imgMaxSize = Symphonys.getLong("upload.img.maxSize");
        dataModel.put("imgMaxSize", imgMaxSize);
        final long fileMaxSize = Symphonys.getLong("upload.file.maxSize");
        dataModel.put("fileMaxSize", fileMaxSize);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        String inviteTipLabel = (String) dataModel.get("inviteTipLabel");
        inviteTipLabel = inviteTipLabel.replace("{point}", String.valueOf(Pointtransfer.TRANSFER_SUM_C_INVITE_REGISTER));
        dataModel.put("inviteTipLabel", inviteTipLabel);

        String pointTransferTipLabel = (String) dataModel.get("pointTransferTipLabel");
        pointTransferTipLabel = pointTransferTipLabel.replace("{point}", Symphonys.get("pointTransferMin"));
        dataModel.put("pointTransferTipLabel", pointTransferTipLabel);

        String dataExportTipLabel = (String) dataModel.get("dataExportTipLabel");
        dataExportTipLabel = dataExportTipLabel.replace("{point}",
                String.valueOf(Pointtransfer.TRANSFER_SUM_C_DATA_EXPORT));
        dataModel.put("dataExportTipLabel", dataExportTipLabel);

        final String allowRegister = optionQueryService.getAllowRegister();
        dataModel.put("allowRegister", allowRegister);

        String buyInvitecodeLabel = langPropsService.get("buyInvitecodeLabel");
        buyInvitecodeLabel = buyInvitecodeLabel.replace("${point}",
                String.valueOf(Pointtransfer.TRANSFER_SUM_C_BUY_INVITECODE));
        buyInvitecodeLabel = buyInvitecodeLabel.replace("${point2}",
                String.valueOf(Pointtransfer.TRANSFER_SUM_C_INVITECODE_USED));
        dataModel.put("buyInvitecodeLabel", buyInvitecodeLabel);

        final List<JSONObject> invitecodes = invitecodeQueryService.getValidInvitecodes(userId);
        for (final JSONObject invitecode : invitecodes) {
            String msg = langPropsService.get("expireTipLabel");
            msg = msg.replace("${time}", DateFormatUtils.format(invitecode.optLong(Keys.OBJECT_ID)
                    + Symphonys.getLong("invitecode.expired"), "yyyy-MM-dd HH:mm"));
            invitecode.put(Common.MEMO, msg);
        }

        dataModel.put(Invitecode.INVITECODES, (Object) invitecodes);

        if (requestURI.contains("function")) {
            final String emojis = emotionQueryService.getEmojis(userId);
            final String[][] emojiLists = {{
                    "smile",
                    "laughing",
                    "smirk",
                    "heart_eyes",
                    "kissing_heart",
                    "flushed",
                    "grin",
                    "stuck_out_tongue_closed_eyes",
                    "kissing",
                    "sleeping",
                    "anguished",
                    "open_mouth",
                    "expressionless",
                    "unamused",
                    "sweat_smile",
                    "weary",
                    "sob",
                    "joy",
                    "astonished",
                    "scream"
            }, {
                    "tired_face",
                    "rage",
                    "triumph",
                    "yum",
                    "mask",
                    "sunglasses",
                    "dizzy_face",
                    "imp",
                    "smiling_imp",
                    "innocent",
                    "alien",
                    "yellow_heart",
                    "blue_heart",
                    "purple_heart",
                    "heart",
                    "green_heart",
                    "broken_heart",
                    "dizzy",
                    "anger",
                    "exclamation"
            }, {
                    "question",
                    "zzz",
                    "notes",
                    "poop",
                    "+1",
                    "-1",
                    "ok_hand",
                    "punch",
                    "v",
                    "hand",
                    "point_up",
                    "point_down",
                    "pray",
                    "clap",
                    "muscle",
                    "ok_woman",
                    "no_good",
                    "raising_hand",
                    "massage",
                    "haircut"
            }, {
                    "nail_care",
                    "see_no_evil",
                    "feet",
                    "kiss",
                    "eyes",
                    "trollface",
                    "snowman",
                    "zap",
                    "cat",
                    "dog",
                    "mouse",
                    "hamster",
                    "rabbit",
                    "frog",
                    "koala",
                    "pig",
                    "monkey",
                    "racehorse",
                    "camel",
                    "sheep"
            }, {
                    "elephant",
                    "panda_face",
                    "snake",
                    "hatched_chick",
                    "hatching_chick",
                    "turtle",
                    "bug",
                    "honeybee",
                    "beetle",
                    "snail",
                    "octopus",
                    "whale",
                    "dolphin",
                    "dragon",
                    "goat",
                    "paw_prints",
                    "tulip",
                    "four_leaf_clover",
                    "rose",
                    "mushroom"
            }, {
                    "seedling",
                    "shell",
                    "crescent_moon",
                    "partly_sunny",
                    "octocat",
                    "jack_o_lantern",
                    "ghost",
                    "santa",
                    "tada",
                    "camera",
                    "loudspeaker",
                    "hourglass",
                    "lock",
                    "key",
                    "bulb",
                    "hammer",
                    "moneybag",
                    "smoking",
                    "bomb",
                    "gun"
            }, {
                    "hocho",
                    "pill",
                    "syringe",
                    "scissors",
                    "swimmer",
                    "black_joker",
                    "coffee",
                    "tea",
                    "sake",
                    "beer",
                    "wine_glass",
                    "pizza",
                    "hamburger",
                    "poultry_leg",
                    "meat_on_bone",
                    "dango",
                    "doughnut",
                    "icecream",
                    "shaved_ice",
                    "cake"
            }, {
                    "cookie",
                    "lollipop",
                    "apple",
                    "green_apple",
                    "tangerine",
                    "lemon",
                    "cherries",
                    "grapes",
                    "watermelon",
                    "strawberry",
                    "peach",
                    "melon",
                    "banana",
                    "pear",
                    "pineapple",
                    "sweet_potato",
                    "eggplant",
                    "tomato",
                    Emotion.EOF_EMOJI // 标记结束以便在function.ftl中处理
            }};

            dataModel.put(Emotion.EMOTIONS, emojis);
            dataModel.put(Emotion.SHORT_T_LIST, emojiLists);
        }

        if (requestURI.contains("i18n")) {
            dataModel.put(Common.LANGUAGES, Languages.getAvailableLanguages());

            final List<JSONObject> timezones = new ArrayList<>();
            final List<TimeZones.TimeZoneWithDisplayNames> timeZones = TimeZones.getInstance().getTimeZones();
            for (final TimeZones.TimeZoneWithDisplayNames timeZone : timeZones) {
                final JSONObject timezone = new JSONObject();

                timezone.put(Common.ID, timeZone.getTimeZone().getID());
                timezone.put(Common.NAME, timeZone.getDisplayName());

                timezones.add(timezone);
            }
            dataModel.put(Common.TIMEZONES, timezones);
        }

        dataModel.put(Common.TYPE, "settings");
    }

    /**
     * Shows user home anonymous comments page.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/comments/anonymous", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, UserBlockCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeAnonymousComments(final HTTPRequestContext context, final HttpServletRequest request,
                                          final HttpServletResponse response, final String userName) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/comments.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        JSONObject currentUser = null;
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
        }

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        if (null == currentUser || (!currentUser.optString(Keys.OBJECT_ID).equals(user.optString(Keys.OBJECT_ID)))
                && !Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomeCmtsCnt");
        final int windowSize = Symphonys.getInt("userHomeCmtsWindowSize");

        fillHomeUser(dataModel, user);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final List<JSONObject> userComments = commentQueryService.getUserComments(
                avatarViewMode, user.optString(Keys.OBJECT_ID), Comment.COMMENT_ANONYMOUS_C_ANONYMOUS,
                pageNum, pageSize, currentUser);
        dataModel.put(Common.USER_HOME_COMMENTS, userComments);

        int recordCount = 0;
        int pageCount = 0;
        if (!userComments.isEmpty()) {
            final JSONObject first = userComments.get(0);
            pageCount = first.optInt(Pagination.PAGINATION_PAGE_COUNT);
            recordCount = first.optInt(Pagination.PAGINATION_RECORD_COUNT);
        }

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);

        dataModel.put(Common.TYPE, "commentsAnonymous");
    }

    /**
     * Shows user home anonymous articles page.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/articles/anonymous", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, UserBlockCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showAnonymousArticles(final HTTPRequestContext context, final HttpServletRequest request,
                                      final HttpServletResponse response, final String userName) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/home.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        JSONObject currentUser = null;
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
        }

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        if (null == currentUser || (!currentUser.optString(Keys.OBJECT_ID).equals(user.optString(Keys.OBJECT_ID)))
                && !Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        dataModel.put(User.USER, user);
        fillHomeUser(dataModel, user);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        if (isLoggedIn) {
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int pageSize = Symphonys.getInt("userHomeArticlesCnt");
        final int windowSize = Symphonys.getInt("userHomeArticlesWindowSize");

        final List<JSONObject> userArticles = articleQueryService.getUserArticles(avatarViewMode,
                user.optString(Keys.OBJECT_ID), Article.ARTICLE_ANONYMOUS_C_ANONYMOUS, pageNum, pageSize);
        dataModel.put(Common.USER_HOME_ARTICLES, userArticles);

        int recordCount = 0;
        int pageCount = 0;
        if (!userArticles.isEmpty()) {
            final JSONObject first = userArticles.get(0);
            pageCount = first.optInt(Pagination.PAGINATION_PAGE_COUNT);
            recordCount = first.optInt(Pagination.PAGINATION_RECORD_COUNT);
        }

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);

        dataModel.put(Common.IS_MY_ARTICLE, userName.equals(currentUser.optString(User.USER_NAME)));

        dataModel.put(Common.TYPE, "articlesAnonymous");
    }

    /**
     * Exports posts(article/comment) to a file.
     *
     * @param context the specified context
     * @param request the specified request
     */
    @RequestProcessing(value = "/export/posts", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class})
    public void exportPosts(final HTTPRequestContext context, final HttpServletRequest request) {
        context.renderJSON();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String userId = user.optString(Keys.OBJECT_ID);

        final String downloadURL = postExportService.exportPosts(userId);
        if ("-1".equals(downloadURL)) {
            context.renderJSONValue(Keys.MSG, langPropsService.get("insufficientBalanceLabel"));

        } else if (StringUtils.isBlank(downloadURL)) {
            return;
        }

        context.renderJSON(true).renderJSONValue("url", downloadURL);
    }

    /**
     * Shows user home page.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHome(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                         final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        renderer.setTemplateName("/home/home.ftl");

        dataModel.put(User.USER, user);
        fillHomeUser(dataModel, user);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int pageSize = Symphonys.getInt("userHomeArticlesCnt");
        final int windowSize = Symphonys.getInt("userHomeArticlesWindowSize");

        final List<JSONObject> userArticles = articleQueryService.getUserArticles(avatarViewMode,
                user.optString(Keys.OBJECT_ID), Article.ARTICLE_ANONYMOUS_C_PUBLIC, pageNum, pageSize);
        dataModel.put(Common.USER_HOME_ARTICLES, userArticles);

        final int articleCnt = user.optInt(UserExt.USER_ARTICLE_COUNT);
        final int pageCount = (int) Math.ceil((double) articleCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, articleCnt);

        final JSONObject currentUser = Sessions.currentUser(request);
        if (null == currentUser) {
            dataModel.put(Common.IS_MY_ARTICLE, false);
        } else {
            dataModel.put(Common.IS_MY_ARTICLE, userName.equals(currentUser.optString(User.USER_NAME)));
        }

        dataModel.put(Common.TYPE, "home");
    }

    /**
     * Shows user home comments page.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/comments", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeComments(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                                 final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/comments.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomeCmtsCnt");
        final int windowSize = Symphonys.getInt("userHomeCmtsWindowSize");

        fillHomeUser(dataModel, user);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        JSONObject currentUser = null;
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final List<JSONObject> userComments = commentQueryService.getUserComments(avatarViewMode,
                user.optString(Keys.OBJECT_ID), Comment.COMMENT_ANONYMOUS_C_PUBLIC, pageNum, pageSize, currentUser);
        dataModel.put(Common.USER_HOME_COMMENTS, userComments);

        final int commentCnt = user.optInt(UserExt.USER_COMMENT_COUNT);
        final int pageCount = (int) Math.ceil((double) commentCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, commentCnt);

        dataModel.put(Common.TYPE, "comments");
    }

    /**
     * Shows user home following users page.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/following/users", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeFollowingUsers(final HTTPRequestContext context, final HttpServletRequest request,
                                       final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/following-users.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomeFollowingUsersCnt");
        final int windowSize = Symphonys.getInt("userHomeFollowingUsersWindowSize");

        fillHomeUser(dataModel, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final JSONObject followingUsersResult = followQueryService.getFollowingUsers(avatarViewMode,
                followingId, pageNum, pageSize);
        final List<JSONObject> followingUsers = (List<JSONObject>) followingUsersResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_USERS, followingUsers);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingUser : followingUsers) {
                final String homeUserFollowingUserId = followingUser.optString(Keys.OBJECT_ID);

                followingUser.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingUserId, Follow.FOLLOWING_TYPE_C_USER));
            }
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int followingUserCnt = followingUsersResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) followingUserCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followingUserCnt);

        dataModel.put(Common.TYPE, "followingUsers");
    }

    /**
     * Shows user home following tags page.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/following/tags", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeFollowingTags(final HTTPRequestContext context, final HttpServletRequest request,
                                      final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/following-tags.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomeFollowingTagsCnt");
        final int windowSize = Symphonys.getInt("userHomeFollowingTagsWindowSize");

        fillHomeUser(dataModel, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final JSONObject followingTagsResult = followQueryService.getFollowingTags(followingId, pageNum, pageSize);
        final List<JSONObject> followingTags = (List<JSONObject>) followingTagsResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_TAGS, followingTags);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingTag : followingTags) {
                final String homeUserFollowingTagId = followingTag.optString(Keys.OBJECT_ID);

                followingTag.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingTagId, Follow.FOLLOWING_TYPE_C_TAG));
            }
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int followingTagCnt = followingTagsResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil(followingTagCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followingTagCnt);

        dataModel.put(Common.TYPE, "followingTags");
    }

    /**
     * Shows user home following articles page.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/following/articles", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeFollowingArticles(final HTTPRequestContext context, final HttpServletRequest request,
                                          final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/following-articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomeFollowingArticlesCnt");
        final int windowSize = Symphonys.getInt("userHomeFollowingArticlesWindowSize");

        fillHomeUser(dataModel, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final JSONObject followingArticlesResult = followQueryService.getFollowingArticles(avatarViewMode,
                followingId, pageNum, pageSize);
        final List<JSONObject> followingArticles = (List<JSONObject>) followingArticlesResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_ARTICLES, followingArticles);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingArticle : followingArticles) {
                final String homeUserFollowingArticleId = followingArticle.optString(Keys.OBJECT_ID);

                followingArticle.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE));
            }
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int followingArticleCnt = followingArticlesResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil(followingArticleCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followingArticleCnt);

        dataModel.put(Common.TYPE, "followingArticles");
    }

    /**
     * Shows user home watching articles page.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/watching/articles", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeWatchingArticles(final HTTPRequestContext context, final HttpServletRequest request,
                                          final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/watching-articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomeFollowingArticlesCnt");
        final int windowSize = Symphonys.getInt("userHomeFollowingArticlesWindowSize");

        fillHomeUser(dataModel, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final JSONObject followingArticlesResult = followQueryService.getWatchingArticles(avatarViewMode,
                followingId, pageNum, pageSize);
        final List<JSONObject> followingArticles = (List<JSONObject>) followingArticlesResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_ARTICLES, followingArticles);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingArticle : followingArticles) {
                final String homeUserFollowingArticleId = followingArticle.optString(Keys.OBJECT_ID);

                followingArticle.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE));
            }
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int followingArticleCnt = followingArticlesResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil(followingArticleCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followingArticleCnt);

        dataModel.put(Common.TYPE, "watchingArticles");
    }

    /**
     * Shows user home follower users page.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/followers", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeFollowers(final HTTPRequestContext context, final HttpServletRequest request,
                                  final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/followers.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomeFollowersCnt");
        final int windowSize = Symphonys.getInt("userHomeFollowersWindowSize");

        fillHomeUser(dataModel, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject followerUsersResult = followQueryService.getFollowerUsers(avatarViewMode,
                followingId, pageNum, pageSize);
        final List<JSONObject> followerUsers = (List) followerUsersResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWER_USERS, followerUsers);

        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followerUser : followerUsers) {
                final String homeUserFollowerUserId = followerUser.optString(Keys.OBJECT_ID);

                followerUser.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowerUserId, Follow.FOLLOWING_TYPE_C_USER));
            }
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int followerUserCnt = followerUsersResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) followerUserCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followerUserCnt);

        dataModel.put(Common.TYPE, "followers");

        notificationMgmtService.makeRead(followingId, Notification.DATA_TYPE_C_NEW_FOLLOWER);
    }

    /**
     * Shows user home points page.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/points", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomePoints(final HTTPRequestContext context, final HttpServletRequest request,
                               final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/points.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomePointsCnt");
        final int windowSize = Symphonys.getInt("userHomePointsWindowSize");

        fillHomeUser(dataModel, user);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final JSONObject userPointsResult
                = pointtransferQueryService.getUserPoints(user.optString(Keys.OBJECT_ID), pageNum, pageSize);
        final List<JSONObject> userPoints
                = CollectionUtils.<JSONObject>jsonArrayToList(userPointsResult.optJSONArray(Keys.RESULTS));
        dataModel.put(Common.USER_HOME_POINTS, userPoints);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, user.optString(Keys.OBJECT_ID), Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int pointsCnt = userPointsResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) pointsCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModel.put(Common.TYPE, "points");
    }

    /**
     * Updates user geo status.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     */
    @RequestProcessing(value = "/settings/geo/status", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class})
    public void updateGeoStatus(final HTTPRequestContext context,
                                final HttpServletRequest request, final HttpServletResponse response) {
        context.renderJSON();

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            requestJSONObject = new JSONObject();
        }

        int geoStatus = requestJSONObject.optInt(UserExt.USER_GEO_STATUS);
        if (UserExt.USER_GEO_STATUS_C_PRIVATE != geoStatus && UserExt.USER_GEO_STATUS_C_PUBLIC != geoStatus) {
            geoStatus = UserExt.USER_GEO_STATUS_C_PUBLIC;
        }

        try {
            final JSONObject user = userQueryService.getCurrentUser(request);
            user.put(UserExt.USER_GEO_STATUS, geoStatus);

            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Updates user privacy.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/settings/privacy", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class})
    public void updatePrivacy(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        context.renderJSON();

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            requestJSONObject = new JSONObject();
        }

        final boolean articleStatus = requestJSONObject.optBoolean(UserExt.USER_ARTICLE_STATUS);
        final boolean commentStatus = requestJSONObject.optBoolean(UserExt.USER_COMMENT_STATUS);
        final boolean followingUserStatus = requestJSONObject.optBoolean(UserExt.USER_FOLLOWING_USER_STATUS);
        final boolean followingTagStatus = requestJSONObject.optBoolean(UserExt.USER_FOLLOWING_TAG_STATUS);
        final boolean followingArticleStatus = requestJSONObject.optBoolean(UserExt.USER_FOLLOWING_ARTICLE_STATUS);
        final boolean watchingArticleStatus = requestJSONObject.optBoolean(UserExt.USER_WATCHING_ARTICLE_STATUS);
        final boolean followerStatus = requestJSONObject.optBoolean(UserExt.USER_FOLLOWER_STATUS);
        final boolean pointStatus = requestJSONObject.optBoolean(UserExt.USER_POINT_STATUS);
        final boolean onlineStatus = requestJSONObject.optBoolean(UserExt.USER_ONLINE_STATUS);
        final boolean timelineStatus = requestJSONObject.optBoolean(UserExt.USER_TIMELINE_STATUS);
        final boolean uaStatus = requestJSONObject.optBoolean(UserExt.USER_UA_STATUS);
        final boolean userForgeLinkStatus = requestJSONObject.optBoolean(UserExt.USER_FORGE_LINK_STATUS);
        final boolean userJoinPointRank = requestJSONObject.optBoolean(UserExt.USER_JOIN_POINT_RANK);
        final boolean userJoinUsedPointRank = requestJSONObject.optBoolean(UserExt.USER_JOIN_USED_POINT_RANK);

        final JSONObject user = userQueryService.getCurrentUser(request);

        user.put(UserExt.USER_ONLINE_STATUS, onlineStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_ARTICLE_STATUS, articleStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_COMMENT_STATUS, commentStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_FOLLOWING_USER_STATUS, followingUserStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_FOLLOWING_TAG_STATUS, followingTagStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_FOLLOWING_ARTICLE_STATUS, followingArticleStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_WATCHING_ARTICLE_STATUS, watchingArticleStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_FOLLOWER_STATUS, followerStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_POINT_STATUS, pointStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_TIMELINE_STATUS, timelineStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_UA_STATUS, uaStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_JOIN_POINT_RANK, userJoinPointRank
                ? UserExt.USER_JOIN_POINT_RANK_C_JOIN : UserExt.USER_JOIN_POINT_RANK_C_NOT_JOIN);
        user.put(UserExt.USER_JOIN_USED_POINT_RANK, userJoinUsedPointRank
                ? UserExt.USER_JOIN_USED_POINT_RANK_C_JOIN : UserExt.USER_JOIN_USED_POINT_RANK_C_NOT_JOIN);
        user.put(UserExt.USER_FORGE_LINK_STATUS, userForgeLinkStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);

        try {
            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Updates user function.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/settings/function", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class})
    public void updateFunction(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        context.renderJSON();

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            requestJSONObject = new JSONObject();
        }

        String userListPageSizeStr = requestJSONObject.optString(UserExt.USER_LIST_PAGE_SIZE);
        final int userCommentViewMode = requestJSONObject.optInt(UserExt.USER_COMMENT_VIEW_MODE);
        final int userAvatarViewMode = requestJSONObject.optInt(UserExt.USER_AVATAR_VIEW_MODE);
        final boolean notifyStatus = requestJSONObject.optBoolean(UserExt.USER_NOTIFY_STATUS);
        final boolean subMailStatus = requestJSONObject.optBoolean(UserExt.USER_SUB_MAIL_STATUS);
        final boolean keyboardShortcutsStatus = requestJSONObject.optBoolean(UserExt.USER_KEYBOARD_SHORTCUTS_STATUS);

        int userListPageSize;
        try {
            userListPageSize = Integer.valueOf(userListPageSizeStr);

            if (10 > userListPageSize) {
                userListPageSize = 10;
            }

            if (userListPageSize > 30) {
                userListPageSize = 30;
            }
        } catch (final Exception e) {
            userListPageSize = Symphonys.getInt("indexArticlesCnt");
        }

        final JSONObject user = userQueryService.getCurrentUser(request);

        user.put(UserExt.USER_LIST_PAGE_SIZE, userListPageSize);
        user.put(UserExt.USER_COMMENT_VIEW_MODE, userCommentViewMode);
        user.put(UserExt.USER_AVATAR_VIEW_MODE, userAvatarViewMode);
        user.put(UserExt.USER_NOTIFY_STATUS, notifyStatus
                ? UserExt.USER_XXX_STATUS_C_ENABLED : UserExt.USER_XXX_STATUS_C_DISABLED);
        user.put(UserExt.USER_SUB_MAIL_STATUS, subMailStatus
                ? UserExt.USER_XXX_STATUS_C_ENABLED : UserExt.USER_XXX_STATUS_C_DISABLED);
        user.put(UserExt.USER_KEYBOARD_SHORTCUTS_STATUS, keyboardShortcutsStatus
                ? UserExt.USER_XXX_STATUS_C_ENABLED : UserExt.USER_XXX_STATUS_C_DISABLED);

        try {
            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Updates user profiles.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/settings/profiles", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, UpdateProfilesValidation.class})
    public void updateProfiles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String userTags = requestJSONObject.optString(UserExt.USER_TAGS);
        final String userURL = requestJSONObject.optString(User.USER_URL);
        final String userQQ = requestJSONObject.optString(UserExt.USER_QQ);
        final String userIntro = requestJSONObject.optString(UserExt.USER_INTRO);
        final String userNickname = requestJSONObject.optString(UserExt.USER_NICKNAME);

        final JSONObject user = userQueryService.getCurrentUser(request);

        user.put(UserExt.USER_TAGS, userTags);
        user.put(User.USER_URL, userURL);
        user.put(UserExt.USER_QQ, userQQ);
        user.put(UserExt.USER_INTRO, userIntro.replace("<", "&lt;").replace(">", "&gt"));
        user.put(UserExt.USER_NICKNAME, userNickname.replace("<", "&lt;").replace(">", "&gt"));
        user.put(UserExt.USER_AVATAR_TYPE, UserExt.USER_AVATAR_TYPE_C_UPLOAD);

        try {
            userMgmtService.updateProfiles(user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Updates user avatar.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/settings/avatar", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, UpdateProfilesValidation.class})
    public void updateAvatar(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);
        final String userAvatarURL = requestJSONObject.optString(UserExt.USER_AVATAR_URL);

        final long now = System.currentTimeMillis();

        final JSONObject user = userQueryService.getCurrentUser(request);

        user.put(UserExt.USER_AVATAR_TYPE, UserExt.USER_AVATAR_TYPE_C_UPLOAD);
        user.put(UserExt.USER_UPDATE_TIME, System.currentTimeMillis());

        if (Symphonys.getBoolean("qiniu.enabled")) {
            final String qiniuDomain = Symphonys.get("qiniu.domain");

            if (!StringUtils.startsWith(userAvatarURL, qiniuDomain)) {
                user.put(UserExt.USER_AVATAR_URL, Symphonys.get("defaultThumbnailURL"));
            } else {
                user.put(UserExt.USER_AVATAR_URL, userAvatarURL);
            }
        } else {
            user.put(UserExt.USER_AVATAR_URL, userAvatarURL);
        }

        try {
            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Point transfer.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/point/transfer", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, PointTransferValidation.class})
    public void pointTransfer(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONObject ret = Results.falseResult();
        context.renderJSON(ret);

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final int amount = requestJSONObject.optInt(Common.AMOUNT);
        final JSONObject toUser = (JSONObject) request.getAttribute(Common.TO_USER);
        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);

        final String fromId = currentUser.optString(Keys.OBJECT_ID);
        final String toId = toUser.optString(Keys.OBJECT_ID);

        final String transferId = pointtransferMgmtService.transfer(fromId, toId,
                Pointtransfer.TRANSFER_TYPE_C_ACCOUNT2ACCOUNT, amount, toId, System.currentTimeMillis());
        final boolean succ = null != transferId;
        ret.put(Keys.STATUS_CODE, succ);
        if (!succ) {
            ret.put(Keys.MSG, langPropsService.get("transferFailLabel"));
        } else {
            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, toId);
            notification.put(Notification.NOTIFICATION_DATA_ID, transferId);

            notificationMgmtService.addPointTransferNotification(notification);
        }
    }

    /**
     * Updates user B3log sync.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/settings/sync/b3", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, UpdateSyncB3Validation.class})
    public void updateSyncB3(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String b3Key = requestJSONObject.optString(UserExt.USER_B3_KEY);
        final String addArticleURL = requestJSONObject.optString(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL);
        final String updateArticleURL = requestJSONObject.optString(UserExt.USER_B3_CLIENT_UPDATE_ARTICLE_URL);
        final String addCommentURL = requestJSONObject.optString(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL);
        final boolean syncWithSymphonyClient = requestJSONObject.optBoolean(UserExt.SYNC_TO_CLIENT, false);

        final JSONObject user = userQueryService.getCurrentUser(request);
        user.put(UserExt.USER_B3_KEY, b3Key);
        user.put(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL, addArticleURL);
        user.put(UserExt.USER_B3_CLIENT_UPDATE_ARTICLE_URL, updateArticleURL);
        user.put(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL, addCommentURL);
        user.put(UserExt.SYNC_TO_CLIENT, syncWithSymphonyClient);

        try {
            userMgmtService.updateSyncB3(user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg, e);

            context.renderMsg(msg);
        }
    }

    /**
     * Updates user password.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/settings/password", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, UpdatePasswordValidation.class})
    public void updatePassword(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String password = requestJSONObject.optString(User.USER_PASSWORD);
        final String newPassword = requestJSONObject.optString(User.USER_NEW_PASSWORD);

        final JSONObject user = userQueryService.getCurrentUser(request);

        if (!password.equals(user.optString(User.USER_PASSWORD))) {
            context.renderMsg(langPropsService.get("invalidOldPwdLabel"));

            return;
        }

        user.put(User.USER_PASSWORD, newPassword);

        try {
            userMgmtService.updatePassword(user);
            context.renderTrueResult();
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg, e);

            context.renderMsg(msg);
        }
    }

    /**
     * Updates user emotions.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/settings/emotionList", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, UpdateEmotionListValidation.class})
    public void updateEmoji(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);
        final String emotionList = requestJSONObject.optString(Emotion.EMOTIONS);

        final JSONObject user = userQueryService.getCurrentUser(request);

        try {
            emotionMgmtService.setEmotionList(user.optString(Keys.OBJECT_ID), emotionList);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg, e);

            context.renderMsg(msg);
        }
    }

    /**
     * Sync user. Experimental API.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/apis/user", method = HTTPRequestMethod.POST)
    public void syncUser(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);

        final String name = requestJSONObject.optString(User.USER_NAME);
        final String email = requestJSONObject.optString(User.USER_EMAIL);
        final String password = requestJSONObject.optString(User.USER_PASSWORD);
        final String clientHost = requestJSONObject.optString(Client.CLIENT_HOST);
        final String clientB3Key = requestJSONObject.optString(UserExt.USER_B3_KEY);
        final String addArticleURL = clientHost + "/apis/symphony/article";
        final String updateArticleURL = clientHost + "/apis/symphony/article";
        final String addCommentURL = clientHost + "/apis/symphony/comment";

        if (UserRegisterValidation.invalidUserName(name)) {
            LOGGER.log(Level.WARN, "Sync add user[name={0}, host={1}] error, caused by the username is invalid",
                    name, clientHost);

            return;
        }

        final String maybeIP = StringUtils.substringBetween(clientHost, "://", ":");
        if (Networks.isIPv4(maybeIP)) {
            LOGGER.log(Level.WARN, "Sync add user[name={0}, host={1}] error, caused by the client host is IPv4",
                    name, clientHost);

            return;
        }

        JSONObject user = userQueryService.getUserByEmail(email);
        if (null == user) {
            user = new JSONObject();
            user.put(User.USER_NAME, name);
            user.put(User.USER_EMAIL, email);
            user.put(User.USER_PASSWORD, password);
            user.put(UserExt.USER_LANGUAGE, "zh_CN");
            user.put(UserExt.USER_B3_KEY, clientB3Key);
            user.put(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL, addArticleURL);
            user.put(UserExt.USER_B3_CLIENT_UPDATE_ARTICLE_URL, updateArticleURL);
            user.put(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL, addCommentURL);
            user.put(UserExt.USER_STATUS, UserExt.USER_STATUS_C_VALID); // One Move

            try {
                final String id = userMgmtService.addUser(user);
                user.put(Keys.OBJECT_ID, id);

                userMgmtService.updateSyncB3(user);

                LOGGER.log(Level.INFO, "Added a user[{0}] via Solo[{1}] sync", name, clientHost);

                context.renderTrueResult();
            } catch (final ServiceException e) {
                LOGGER.log(Level.ERROR, "Sync add user[name={0}, email={1}, host={2}] error: {3}",
                        name, email, clientHost, e.getMessage());
            }

            return;
        }

        String userKey = user.optString(UserExt.USER_B3_KEY);
        if (StringUtils.isBlank(userKey) || (Strings.isNumeric(userKey) && userKey.length() == clientB3Key.length())) {
            userKey = clientB3Key;

            user.put(UserExt.USER_B3_KEY, userKey);
            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);
        }

        if (!userKey.equals(clientB3Key)) {
            LOGGER.log(Level.WARN, "Sync update user[name={0}, email={1}, host={2}] B3Key dismatch [sym={3}, solo={4}]",
                    name, email, clientHost, user.optString(UserExt.USER_B3_KEY), clientB3Key);

            return;
        }

        user.put(UserExt.USER_B3_KEY, clientB3Key);
        user.put(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL, addArticleURL);
        user.put(UserExt.USER_B3_CLIENT_UPDATE_ARTICLE_URL, updateArticleURL);
        user.put(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL, addCommentURL);

        try {
            userMgmtService.updateSyncB3(user);

            LOGGER.log(Level.INFO, "Updated a user[name={0}] via Solo[{1}] sync", name, clientHost);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            LOGGER.log(Level.ERROR, "Sync update user[name=" + name + ", host=" + clientHost + "] error", e);
        }
    }

    /**
     * Resets unverified users.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/cron/users/reset-unverified", method = HTTPRequestMethod.GET)
    public void resetUnverifiedUsers(final HTTPRequestContext context,
                                     final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        userMgmtService.resetUnverifiedUsers();

        context.renderJSON().renderTrueResult();
    }

    /**
     * Lists usernames.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/users/names", method = HTTPRequestMethod.GET)
    public void listNames(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        if (null == Sessions.currentUser(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        context.renderJSON().renderTrueResult();

        final String namePrefix = request.getParameter("name");
        if (StringUtils.isBlank(namePrefix)) {
            final List<JSONObject> admins = userQueryService.getAdmins();
            final List<JSONObject> userNames = new ArrayList<>();

            for (final JSONObject admin : admins) {
                final JSONObject userName = new JSONObject();
                userName.put(User.USER_NAME, admin.optString(User.USER_NAME));

                final String avatar = avatarQueryService.getAvatarURLByUser(
                        UserExt.USER_AVATAR_VIEW_MODE_C_STATIC, admin, "20");
                userName.put(UserExt.USER_AVATAR_URL, avatar);

                userNames.add(userName);
            }

            context.renderJSONValue(Common.USER_NAMES, userNames);

            return;
        }

        final List<JSONObject> userNames = userQueryService.getUserNamesByPrefix(namePrefix);

        context.renderJSONValue(Common.USER_NAMES, userNames);
    }

    /**
     * Lists emotions.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/users/emotions", method = HTTPRequestMethod.GET)
    public void getEmotions(final HTTPRequestContext context, final HttpServletRequest request,
                            final HttpServletResponse response) throws Exception {
        context.renderJSON();

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        if (null == currentUser) {
            context.renderJSONValue("emotions", "");

            return;
        }

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final String emotions = emotionQueryService.getEmojis(userId);

        context.renderJSONValue("emotions", emotions);
    }

    /**
     * Loads usernames.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/cron/users/load-names", method = HTTPRequestMethod.GET)
    public void loadUserNames(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        userQueryService.loadUserNames();

        context.renderJSON().renderTrueResult();
    }

    /**
     * Fills home user.
     *
     * @param dataModel the specified data model
     * @param user      the specified user
     */
    private void fillHomeUser(final Map<String, Object> dataModel, final JSONObject user) {
        dataModel.put(User.USER, user);

        final String roleId = user.optString(User.USER_ROLE);
        final JSONObject role = roleQueryService.getRole(roleId);
        user.put(Role.ROLE_NAME, role.optString(Role.ROLE_NAME));
    }
}
