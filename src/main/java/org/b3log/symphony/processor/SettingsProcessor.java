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
package org.b3log.symphony.processor;

import com.qiniu.util.Auth;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
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
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.CSRFCheck;
import org.b3log.symphony.processor.advice.CSRFToken;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.UpdateEmotionListValidation;
import org.b3log.symphony.processor.advice.validate.UpdatePasswordValidation;
import org.b3log.symphony.processor.advice.validate.UpdateProfilesValidation;
import org.b3log.symphony.processor.advice.validate.UpdateSyncB3Validation;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.Languages;
import org.b3log.symphony.util.Symphonys;
import org.b3log.symphony.util.TimeZones;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Settings processor.
 * <ul>
 * <li>Shows settings (/settings), GET</li>
 * <li>Shows settings pages (/settings/*), GET</li>
 * <li>Updates profiles (/settings/profiles), POST</li>
 * <li>Updates user avatar (/settings/avatar), POST</li>
 * <li>Geo status (/settings/geo/status), POST</li>
 * <li>Sync (/settings/sync/b3), POST</li>
 * <li>Privacy (/settings/privacy), POST</li>
 * <li>Function (/settings/function), POST</li>
 * <li>Updates emotions (/settings/emotionList), POST</li>
 * <li>Password (/settings/password), POST</li>
 * <li>Updates i18n (/settings/i18n), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.5, May 23, 2018
 * @since 2.4.0
 */
@RequestProcessor
public class SettingsProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SettingsProcessor.class);

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
     * Invitecode query service.
     */
    @Inject
    private InvitecodeQueryService invitecodeQueryService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

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
        UserProcessor.fillHomeUser(dataModel, user, roleQueryService);

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
            dataModel.put(Emotion.EMOTIONS, emotionQueryService.getEmojis(userId));
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
        final boolean breezemoonStatus = requestJSONObject.optBoolean(UserExt.USER_BREEZEMOON_STATUS);
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
        user.put(UserExt.USER_BREEZEMOON_STATUS, breezemoonStatus
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
        final int userListViewMode = requestJSONObject.optInt(UserExt.USER_LIST_VIEW_MODE);
        final boolean notifyStatus = requestJSONObject.optBoolean(UserExt.USER_NOTIFY_STATUS);
        final boolean subMailStatus = requestJSONObject.optBoolean(UserExt.USER_SUB_MAIL_STATUS);
        final boolean keyboardShortcutsStatus = requestJSONObject.optBoolean(UserExt.USER_KEYBOARD_SHORTCUTS_STATUS);
        final boolean userReplyWatchArticleStatus = requestJSONObject.optBoolean(UserExt.USER_REPLY_WATCH_ARTICLE_STATUS);

        int userListPageSize;
        try {
            userListPageSize = Integer.valueOf(userListPageSizeStr);

            if (10 > userListPageSize) {
                userListPageSize = 10;
            }

            if (userListPageSize > 60) {
                userListPageSize = 60;
            }
        } catch (final Exception e) {
            userListPageSize = Symphonys.getInt("indexArticlesCnt");
        }

        final JSONObject user = userQueryService.getCurrentUser(request);

        user.put(UserExt.USER_LIST_PAGE_SIZE, userListPageSize);
        user.put(UserExt.USER_COMMENT_VIEW_MODE, userCommentViewMode);
        user.put(UserExt.USER_AVATAR_VIEW_MODE, userAvatarViewMode);
        user.put(UserExt.USER_LIST_VIEW_MODE, userListViewMode);
        user.put(UserExt.USER_NOTIFY_STATUS, notifyStatus
                ? UserExt.USER_XXX_STATUS_C_ENABLED : UserExt.USER_XXX_STATUS_C_DISABLED);
        user.put(UserExt.USER_SUB_MAIL_STATUS, subMailStatus
                ? UserExt.USER_XXX_STATUS_C_ENABLED : UserExt.USER_XXX_STATUS_C_DISABLED);
        user.put(UserExt.USER_KEYBOARD_SHORTCUTS_STATUS, keyboardShortcutsStatus
                ? UserExt.USER_XXX_STATUS_C_ENABLED : UserExt.USER_XXX_STATUS_C_DISABLED);
        user.put(UserExt.USER_REPLY_WATCH_ARTICLE_STATUS, userReplyWatchArticleStatus
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
        user.put(UserExt.USER_INTRO, userIntro);
        user.put(UserExt.USER_NICKNAME, userNickname);
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

        final JSONObject user = userQueryService.getCurrentUser(request);

        user.put(UserExt.USER_AVATAR_TYPE, UserExt.USER_AVATAR_TYPE_C_UPLOAD);
        user.put(UserExt.USER_UPDATE_TIME, System.currentTimeMillis());

        if (Strings.contains(userAvatarURL, new String[]{"<", ">", "\"", "'"})) {
            user.put(UserExt.USER_AVATAR_URL, Symphonys.get("defaultThumbnailURL"));
        } else {
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
        }

        try {
            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
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

    private static final String[][] emojiLists = {{
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
}
