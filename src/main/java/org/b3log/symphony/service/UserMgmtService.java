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

import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Ids;
import org.b3log.latke.util.URLs;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.repository.*;
import org.b3log.symphony.util.Geos;
import org.b3log.symphony.util.Gravatars;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * User management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author Bill Ho
 * @version 1.16.0.5, Sep 26, 2018
 * @since 0.2.0
 */
@Service
public class UserMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UserMgmtService.class);

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Comment repository.
     */
    @Inject
    private CommentRepository commentRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    /**
     * Tag repository.
     */
    @Inject
    private TagRepository tagRepository;

    /**
     * User-Tag repository.
     */
    @Inject
    private UserTagRepository userTagRepository;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Pointtransfer management service.
     */
    @Inject
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Deactivates the specified user.
     *
     * @param userId the specified user id
     * @throws ServiceException service exception
     */
    public void deactivateUser(final String userId) throws ServiceException {
        final Transaction transaction = userRepository.beginTransaction();
        try {
            final JSONObject user = userRepository.get(userId);
            final String userNo = user.optString(UserExt.USER_NO);
            final String newName = UserExt.ANONYMOUS_USER_NAME + userNo;
            user.put(User.USER_NAME, newName);
            user.put(User.USER_EMAIL, newName + UserExt.USER_BUILTIN_EMAIL_SUFFIX);
            user.put(UserExt.USER_NICKNAME, "");
            user.put(UserExt.USER_TAGS, "");
            user.put(User.USER_URL, "");
            user.put(UserExt.USER_INTRO, "");
            user.put(UserExt.USER_AVATAR_URL, AvatarQueryService.DEFAULT_AVATAR_URL);
            user.put(UserExt.USER_CITY, "");
            user.put(UserExt.USER_PROVINCE, "");
            user.put(UserExt.USER_COUNTRY, "");
            user.put(User.USER_ROLE, Role.ROLE_ID_C_DEFAULT);
            user.put(UserExt.USER_ONLINE_FLAG, false);
            user.put(UserExt.USER_STATUS, UserExt.USER_STATUS_C_DEACTIVATED);

            userRepository.update(userId, user);
            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Deactivates a user [id=" + userId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates a user's online status and saves the login time and IP.
     *
     * @param userId     the specified user id
     * @param ip         the specified IP, could be {@code null}
     * @param onlineFlag the specified online flag
     * @param force      the specified force flag to update
     */
    public void updateOnlineStatus(final String userId, final String ip, final boolean onlineFlag, final boolean force) {
        Transaction transaction = null;

        try {
            final JSONObject user = userRepository.get(userId);
            if (null == user) {
                return;
            }

            final long updatedAt = user.optLong(UserExt.USER_UPDATE_TIME);
            final long now = System.currentTimeMillis();
            if (now - updatedAt < 1000 * 60 && !force) {
                return;
            }

            if (StringUtils.isNotBlank(ip)) {
                final JSONObject address = Geos.getAddress(ip);
                if (null != address) {
                    final String country = address.optString(Common.COUNTRY);
                    final String province = address.optString(Common.PROVINCE);
                    final String city = address.optString(Common.CITY);

                    user.put(UserExt.USER_COUNTRY, country);
                    user.put(UserExt.USER_PROVINCE, province);
                    user.put(UserExt.USER_CITY, city);
                }
                user.put(UserExt.USER_LATEST_LOGIN_IP, ip);
            }

            transaction = userRepository.beginTransaction();

            user.put(UserExt.USER_ONLINE_FLAG, onlineFlag);
            user.put(UserExt.USER_LATEST_LOGIN_TIME, now);
            user.put(UserExt.USER_UPDATE_TIME, now);

            userRepository.update(userId, user);

            transaction.commit();
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Updates user online status failed [id=" + userId + ", ip=" + ip + ", flag=" + onlineFlag + "]", e);

            if (null != transaction && transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Updates a user's profiles by the specified request json object.
     *
     * @param requestJSONObject the specified request json object (user), for example,
     *                          "oId": "",
     *                          "userNickname": "",
     *                          "userTags": "",
     *                          "userURL": "",
     *                          "userQQ": "",
     *                          "userIntro": "",
     *                          "userAvatarType": int,
     *                          "userAvatarURL": "",
     *                          "userCommentViewMode": int
     * @throws ServiceException service exception
     */
    public void updateProfiles(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = userRepository.beginTransaction();

        try {
            final String oldUserId = requestJSONObject.optString(Keys.OBJECT_ID);
            final JSONObject oldUser = userRepository.get(oldUserId);

            if (null == oldUser) {
                throw new ServiceException(langPropsService.get("updateFailLabel"));
            }

            // Tag
            final String userTags = requestJSONObject.optString(UserExt.USER_TAGS);
            oldUser.put(UserExt.USER_TAGS, userTags);

            tag(oldUser);

            // Update
            oldUser.put(UserExt.USER_NICKNAME, requestJSONObject.optString(UserExt.USER_NICKNAME));
            oldUser.put(User.USER_URL, requestJSONObject.optString(User.USER_URL));
            oldUser.put(UserExt.USER_QQ, requestJSONObject.optString(UserExt.USER_QQ));
            oldUser.put(UserExt.USER_INTRO, requestJSONObject.optString(UserExt.USER_INTRO));
            oldUser.put(UserExt.USER_AVATAR_TYPE, requestJSONObject.optString(UserExt.USER_AVATAR_TYPE));
            oldUser.put(UserExt.USER_AVATAR_URL, requestJSONObject.optString(UserExt.USER_AVATAR_URL));
            oldUser.put(UserExt.USER_COMMENT_VIEW_MODE, requestJSONObject.optInt(UserExt.USER_COMMENT_VIEW_MODE));

            oldUser.put(UserExt.USER_UPDATE_TIME, System.currentTimeMillis());

            userRepository.update(oldUserId, oldUser);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates user profiles failed", e);
            throw new ServiceException(langPropsService.get("updateFailLabel"));
        }
    }

    /**
     * Updates a user's password by the specified request json object.
     *
     * @param requestJSONObject the specified request json object (user), for example,
     *                          "oId": "",
     *                          "userPassword": "", // Hashed
     * @throws ServiceException service exception
     */
    public void updatePassword(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = userRepository.beginTransaction();

        try {
            final String oldUserId = requestJSONObject.optString(Keys.OBJECT_ID);
            final JSONObject oldUser = userRepository.get(oldUserId);

            if (null == oldUser) {
                throw new ServiceException(langPropsService.get("updateFailLabel"));
            }

            // Update
            oldUser.put(User.USER_PASSWORD, requestJSONObject.optString(User.USER_PASSWORD));

            userRepository.update(oldUserId, oldUser);
            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates user password failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Adds a user with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "userName": "",
     *                          "userEmail": "",
     *                          "userPassword": "", // Hashed
     *                          "userLanguage": "", // optional, default to "zh_CN"
     *                          "userAppRole": int, // optional, default to 0
     *                          "userRole": "", // optional, uses {@value Role#ROLE_ID_C_DEFAULT} instead if not specified
     *                          "userStatus": int, // optional, uses {@value UserExt#USER_STATUS_C_NOT_VERIFIED} instead if not specified
     *                          "userGuideStep": int, // optional, uses {@value UserExt#USER_GUIDE_STEP_UPLOAD_AVATAR} instead if not specified
     *                          "userAvatarURL": "" // optional, generate it if not specified
     *                          ,see {@link User} for more details
     * @return generated user id
     * @throws ServiceException if user name or email duplicated, or repository exception
     */
    public synchronized String addUser(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = userRepository.beginTransaction();

        try {
            final String userEmail = requestJSONObject.optString(User.USER_EMAIL).trim().toLowerCase();
            final String userName = requestJSONObject.optString(User.USER_NAME);
            JSONObject user = userRepository.getByName(userName);
            if (null != user && (UserExt.USER_STATUS_C_VALID == user.optInt(UserExt.USER_STATUS)
                    || UserExt.USER_STATUS_C_INVALID_LOGIN == user.optInt(UserExt.USER_STATUS)
                    || UserExt.USER_STATUS_C_DEACTIVATED == user.optInt(UserExt.USER_STATUS)
                    || UserExt.NULL_USER_NAME.equals(userName))) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                throw new ServiceException(langPropsService.get("duplicatedUserNameLabel") + " [" + userName + "]");
            }

            boolean toUpdate = false;
            String ret = null;
            String avatarURL = null;
            user = userRepository.getByEmail(userEmail);
            int userNo = 0;
            if (null != user) {
                if (UserExt.USER_STATUS_C_VALID == user.optInt(UserExt.USER_STATUS)
                        || UserExt.USER_STATUS_C_INVALID_LOGIN == user.optInt(UserExt.USER_STATUS)
                        || UserExt.USER_STATUS_C_DEACTIVATED == user.optInt(UserExt.USER_STATUS)) {
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }

                    throw new ServiceException(langPropsService.get("duplicatedEmailLabel"));
                }

                toUpdate = true;
                ret = user.optString(Keys.OBJECT_ID);
                userNo = user.optInt(UserExt.USER_NO);
                avatarURL = user.optString(UserExt.USER_AVATAR_URL);
            }

            user = new JSONObject();
            user.put(User.USER_NAME, userName);
            user.put(User.USER_EMAIL, userEmail);
            user.put(UserExt.USER_APP_ROLE, requestJSONObject.optInt(UserExt.USER_APP_ROLE));
            user.put(User.USER_PASSWORD, requestJSONObject.optString(User.USER_PASSWORD));
            user.put(User.USER_ROLE, requestJSONObject.optString(User.USER_ROLE, Role.ROLE_ID_C_DEFAULT));
            user.put(User.USER_URL, "");
            user.put(UserExt.USER_ARTICLE_COUNT, 0);
            user.put(UserExt.USER_COMMENT_COUNT, 0);
            user.put(UserExt.USER_TAG_COUNT, 0);
            user.put(UserExt.USER_INTRO, "");
            user.put(UserExt.USER_NICKNAME, "");
            user.put(UserExt.USER_AVATAR_TYPE, UserExt.USER_AVATAR_TYPE_C_UPLOAD);
            user.put(UserExt.USER_QQ, "");
            user.put(UserExt.USER_ONLINE_FLAG, false);
            user.put(UserExt.USER_LATEST_ARTICLE_TIME, 0L);
            user.put(UserExt.USER_LATEST_CMT_TIME, 0L);
            user.put(UserExt.USER_LATEST_LOGIN_TIME, 0L);
            user.put(UserExt.USER_LATEST_LOGIN_IP, "");
            user.put(UserExt.USER_CHECKIN_TIME, 0);
            user.put(UserExt.USER_CURRENT_CHECKIN_STREAK_START, 0);
            user.put(UserExt.USER_CURRENT_CHECKIN_STREAK_END, 0);
            user.put(UserExt.USER_LONGEST_CHECKIN_STREAK_START, 0);
            user.put(UserExt.USER_LONGEST_CHECKIN_STREAK_END, 0);
            user.put(UserExt.USER_LONGEST_CHECKIN_STREAK, 0);
            user.put(UserExt.USER_CURRENT_CHECKIN_STREAK, 0);
            user.put(UserExt.USER_POINT, 0);
            user.put(UserExt.USER_USED_POINT, 0);
            user.put(UserExt.USER_JOIN_POINT_RANK, UserExt.USER_JOIN_POINT_RANK_C_JOIN);
            user.put(UserExt.USER_JOIN_USED_POINT_RANK, UserExt.USER_JOIN_USED_POINT_RANK_C_JOIN);
            user.put(UserExt.USER_TAGS, "");
            user.put(UserExt.USER_SKIN, Symphonys.get("skinDirName"));
            user.put(UserExt.USER_MOBILE_SKIN, Symphonys.get("mobileSkinDirName"));
            user.put(UserExt.USER_COUNTRY, "");
            user.put(UserExt.USER_PROVINCE, "");
            user.put(UserExt.USER_CITY, "");
            user.put(UserExt.USER_UPDATE_TIME, 0L);
            user.put(UserExt.USER_GEO_STATUS, UserExt.USER_GEO_STATUS_C_PUBLIC);
            final int status = requestJSONObject.optInt(UserExt.USER_STATUS, UserExt.USER_STATUS_C_NOT_VERIFIED);
            user.put(UserExt.USER_STATUS, status);
            user.put(UserExt.USER_COMMENT_VIEW_MODE, UserExt.USER_COMMENT_VIEW_MODE_C_REALTIME);
            user.put(UserExt.USER_ONLINE_STATUS, UserExt.USER_XXX_STATUS_C_PUBLIC);
            user.put(UserExt.USER_ARTICLE_STATUS, UserExt.USER_XXX_STATUS_C_PUBLIC);
            user.put(UserExt.USER_COMMENT_STATUS, UserExt.USER_XXX_STATUS_C_PUBLIC);
            user.put(UserExt.USER_FOLLOWING_ARTICLE_STATUS, UserExt.USER_XXX_STATUS_C_PUBLIC);
            user.put(UserExt.USER_WATCHING_ARTICLE_STATUS, UserExt.USER_XXX_STATUS_C_PUBLIC);
            user.put(UserExt.USER_FOLLOWING_TAG_STATUS, UserExt.USER_XXX_STATUS_C_PUBLIC);
            user.put(UserExt.USER_FOLLOWING_USER_STATUS, UserExt.USER_XXX_STATUS_C_PUBLIC);
            user.put(UserExt.USER_FOLLOWER_STATUS, UserExt.USER_XXX_STATUS_C_PUBLIC);
            user.put(UserExt.USER_BREEZEMOON_STATUS, UserExt.USER_XXX_STATUS_C_PUBLIC);
            user.put(UserExt.USER_POINT_STATUS, UserExt.USER_XXX_STATUS_C_PUBLIC);
            user.put(UserExt.USER_UA_STATUS, UserExt.USER_XXX_STATUS_C_PUBLIC);
            user.put(UserExt.USER_NOTIFY_STATUS, UserExt.USER_XXX_STATUS_C_ENABLED);
            user.put(UserExt.USER_SUB_MAIL_STATUS, UserExt.USER_XXX_STATUS_C_ENABLED);
            user.put(UserExt.USER_LIST_PAGE_SIZE, Symphonys.getInt("indexArticlesCnt"));
            user.put(UserExt.USER_LIST_VIEW_MODE, UserExt.USER_LIST_VIEW_MODE_TITLE);
            user.put(UserExt.USER_AVATAR_VIEW_MODE, UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL);
            user.put(UserExt.USER_SUB_MAIL_SEND_TIME, System.currentTimeMillis());
            user.put(UserExt.USER_KEYBOARD_SHORTCUTS_STATUS, UserExt.USER_XXX_STATUS_C_DISABLED);
            user.put(UserExt.USER_REPLY_WATCH_ARTICLE_STATUS, UserExt.USER_SUB_MAIL_STATUS_DISABLED);
            user.put(UserExt.USER_FORWARD_PAGE_STATUS, UserExt.USER_XXX_STATUS_C_ENABLED);

            final JSONObject optionLanguage = optionRepository.get(Option.ID_C_MISC_LANGUAGE);
            final String adminSpecifiedLang = optionLanguage.optString(Option.OPTION_VALUE);
            if ("0".equals(adminSpecifiedLang)) {
                user.put(UserExt.USER_LANGUAGE, requestJSONObject.optString(UserExt.USER_LANGUAGE, "zh_CN"));
            } else {
                user.put(UserExt.USER_LANGUAGE, adminSpecifiedLang);
            }

            user.put(UserExt.USER_TIMEZONE, requestJSONObject.optString(UserExt.USER_TIMEZONE, "Asia/Shanghai"));
            user.put(UserExt.USER_GUIDE_STEP, requestJSONObject.optInt(UserExt.USER_GUIDE_STEP, UserExt.USER_GUIDE_STEP_UPLOAD_AVATAR));

            if (toUpdate) {
                user.put(UserExt.USER_NO, userNo);

                if (!AvatarQueryService.DEFAULT_AVATAR_URL.equals(avatarURL)) { // generate/upload avatar succ
                    if (Symphonys.getBoolean("qiniu.enabled")) {
                        user.put(UserExt.USER_AVATAR_URL, Symphonys.get("qiniu.domain") + "/avatar/" + ret + "?"
                                + new Date().getTime());
                    } else {
                        user.put(UserExt.USER_AVATAR_URL, avatarURL + "?" + new Date().getTime());
                    }

                    avatarURL = user.optString(UserExt.USER_AVATAR_URL);
                    if (255 < StringUtils.length(avatarURL)) {
                        LOGGER.warn("Length of user [" + userName + "]'s avatar URL [" + avatarURL + "] larger then 255");
                        avatarURL = AvatarQueryService.DEFAULT_AVATAR_URL;
                        user.put(UserExt.USER_AVATAR_URL, avatarURL);
                    }

                    userRepository.update(ret, user);
                }
            } else {
                ret = Ids.genTimeMillisId();
                user.put(Keys.OBJECT_ID, ret);

                final String specifiedAvatar = requestJSONObject.optString(UserExt.USER_AVATAR_URL);
                if (AvatarQueryService.DEFAULT_AVATAR_URL.equals(specifiedAvatar)) {
                    user.put(UserExt.USER_AVATAR_URL, specifiedAvatar);
                } else {
                    try {
                        byte[] avatarData;

                        final String hash = DigestUtils.md5Hex(ret);
                        avatarData = Gravatars.getRandomAvatarData(hash); // https://github.com/b3log/symphony/issues/569
                        if (null == avatarData) {
                            final BufferedImage img = avatarQueryService.createAvatar(hash, 512);
                            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(img, "jpg", baos);
                            baos.flush();
                            avatarData = baos.toByteArray();
                            baos.close();
                        }

                        if (Symphonys.getBoolean("qiniu.enabled")) {
                            final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
                            final UploadManager uploadManager = new UploadManager(new Configuration());

                            uploadManager.put(avatarData, "avatar/" + ret, auth.uploadToken(Symphonys.get("qiniu.bucket")),
                                    null, "image/jpeg", false);
                            user.put(UserExt.USER_AVATAR_URL, Symphonys.get("qiniu.domain") + "/avatar/" + ret + "?" + new Date().getTime());
                        } else {
                            final String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";
                            try (final OutputStream output = new FileOutputStream(Symphonys.get("upload.dir") + fileName)) {
                                IOUtils.write(avatarData, output);
                            }

                            user.put(UserExt.USER_AVATAR_URL, Latkes.getServePath() + "/upload/" + fileName);
                        }
                    } catch (final IOException e) {
                        LOGGER.log(Level.ERROR, "Generates avatar error, using default thumbnail instead", e);

                        user.put(UserExt.USER_AVATAR_URL, AvatarQueryService.DEFAULT_AVATAR_URL);
                    }
                }

                final JSONObject memberCntOption = optionRepository.get(Option.ID_C_STATISTIC_MEMBER_COUNT);
                final int memberCount = memberCntOption.optInt(Option.OPTION_VALUE) + 1; // Updates stat. (member count +1)

                user.put(UserExt.USER_NO, memberCount);

                userRepository.add(user);

                memberCntOption.put(Option.OPTION_VALUE, String.valueOf(memberCount));
                optionRepository.update(Option.ID_C_STATISTIC_MEMBER_COUNT, memberCntOption);
            }

            transaction.commit();

            if (UserExt.USER_STATUS_C_VALID == status) {
                // Point
                pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, ret,
                        Pointtransfer.TRANSFER_TYPE_C_INIT, Pointtransfer.TRANSFER_SUM_C_INIT, ret, System.currentTimeMillis(), "");

                // Occupy the username, defeat others
                final Transaction trans = userRepository.beginTransaction();
                try {
                    final Query query = new Query();
                    final List<Filter> filters = new ArrayList<>();
                    filters.add(new PropertyFilter(User.USER_NAME, FilterOperator.EQUAL, userName));
                    filters.add(new PropertyFilter(UserExt.USER_STATUS, FilterOperator.EQUAL,
                            UserExt.USER_STATUS_C_NOT_VERIFIED));
                    query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

                    final JSONArray others = userRepository.get(query).optJSONArray(Keys.RESULTS);
                    for (int i = 0; i < others.length(); i++) {
                        final JSONObject u = others.optJSONObject(i);
                        final String id = u.optString(Keys.OBJECT_ID);
                        final String mail = u.optString(User.USER_EMAIL);

                        u.put(User.USER_NAME, UserExt.NULL_USER_NAME);
                        u.put(User.USER_EMAIL, "");
                        u.put(UserExt.USER_STATUS, UserExt.USER_STATUS_C_NOT_VERIFIED);

                        userRepository.update(id, u);

                        LOGGER.log(Level.INFO, "Defeated a user [email=" + mail + "]");
                    }

                    trans.commit();
                } catch (final RepositoryException e) {
                    if (trans.isActive()) {
                        trans.rollback();
                    }

                    LOGGER.log(Level.ERROR, "Defeat others error", e);
                }

                final JSONObject notification = new JSONObject();
                notification.put(Notification.NOTIFICATION_USER_ID, ret);
                notification.put(Notification.NOTIFICATION_DATA_ID, "");
                notificationMgmtService.addSysAnnounceNewUserNotification(notification);

                // Refresh usernames
                final JSONObject u = new JSONObject();
                u.put(User.USER_NAME, user.optString(User.USER_NAME));
                u.put(UserExt.USER_T_NAME_LOWER_CASE, user.optString(User.USER_NAME).toLowerCase());
                final String avatar = avatarQueryService.getAvatarURLByUser(UserExt.USER_AVATAR_VIEW_MODE_C_STATIC, user, "20");
                u.put(UserExt.USER_AVATAR_URL, avatar);
                UserQueryService.USER_NAMES.add(u);
                Collections.sort(UserQueryService.USER_NAMES, (u1, u2) -> {
                    final String u1Name = u1.optString(UserExt.USER_T_NAME_LOWER_CASE);
                    final String u2Name = u2.optString(UserExt.USER_T_NAME_LOWER_CASE);

                    return u1Name.compareTo(u2Name);
                });
            }

            return ret;
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Adds a user failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Removes a user specified by the given user id.
     *
     * @param userId the given user id
     * @throws ServiceException service exception
     */
    public void removeUser(final String userId) throws ServiceException {
        final Transaction transaction = userRepository.beginTransaction();

        try {
            userRepository.remove(userId);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Removes a user[id=" + userId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates the specified user by the given user id.
     *
     * @param userId the given user id
     * @param user   the specified user
     * @throws ServiceException service exception
     */
    public void updateUser(final String userId, final JSONObject user) throws ServiceException {
        final Transaction transaction = userRepository.beginTransaction();

        try {
            final JSONObject old = userRepository.get(userId);
            final String oldRoleId = old.optString(User.USER_ROLE);
            final String newRoleId = user.optString(User.USER_ROLE);

            userRepository.update(userId, user);

            transaction.commit();

            if (!oldRoleId.equals(newRoleId)) {
                final JSONObject notification = new JSONObject();
                notification.put(Notification.NOTIFICATION_USER_ID, userId);
                notification.put(Notification.NOTIFICATION_DATA_ID, oldRoleId + "-" + newRoleId);
                notificationMgmtService.addSysAnnounceRoleChangedNotification(notification);
            }
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates a user[id=" + userId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates the specified user's email by the given user id.
     *
     * @param userId the given user id
     * @param user   the specified user, contains the new email
     * @throws ServiceException service exception
     */
    public void updateUserEmail(final String userId, final JSONObject user) throws ServiceException {
        final String newEmail = user.optString(User.USER_EMAIL);

        final Transaction transaction = userRepository.beginTransaction();

        try {
            if (null != userRepository.getByEmail(newEmail)) {
                throw new ServiceException(langPropsService.get("duplicatedEmailLabel") + " [" + newEmail + "]");
            }

            userRepository.update(userId, user);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates email of the user[id=" + userId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates the specified user's username by the given user id.
     *
     * @param userId the given user id
     * @param user   the specified user, contains the new username
     * @throws ServiceException service exception
     */
    public void updateUserName(final String userId, final JSONObject user) throws ServiceException {
        final String newUserName = user.optString(User.USER_NAME);

        final Transaction transaction = userRepository.beginTransaction();

        try {
            if (UserRegisterValidation.invalidUserName(newUserName)) {
                throw new ServiceException(langPropsService.get("invalidUserNameLabel") + " [" + newUserName + "]");
            }

            if (!UserExt.NULL_USER_NAME.equals(newUserName) && null != userRepository.getByName(newUserName)) {
                throw new ServiceException(langPropsService.get("duplicatedUserNameLabel") + " [" + newUserName + "]");
            }

            userRepository.update(userId, user);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates username of the user[id=" + userId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Resets unverified users.
     */
    @Transactional
    public void resetUnverifiedUsers() {
        final Date now = new Date();
        final long yesterdayTime = DateUtils.addDays(now, -1).getTime();

        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(UserExt.USER_STATUS, FilterOperator.EQUAL, UserExt.USER_STATUS_C_NOT_VERIFIED));
        filters.add(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN_OR_EQUAL, yesterdayTime));
        filters.add(new PropertyFilter(User.USER_NAME, FilterOperator.NOT_EQUAL, UserExt.NULL_USER_NAME));

        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            final JSONObject result = userRepository.get(query);
            final JSONArray users = result.optJSONArray(Keys.RESULTS);

            for (int i = 0; i < users.length(); i++) {
                final JSONObject user = users.optJSONObject(i);
                user.put(User.USER_NAME, UserExt.NULL_USER_NAME);
                final String email = user.optString(User.USER_EMAIL);
                user.put(User.USER_EMAIL, "");

                final String id = user.optString(Keys.OBJECT_ID);
                userRepository.update(id, user);

                LOGGER.log(Level.INFO, "Reset unverified user [email=" + email + "]");
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Reset unverified users failed", e);
        }
    }

    /**
     * Tags the specified user with the specified tag titles.
     *
     * @param user the specified article
     * @throws RepositoryException repository exception
     */
    private synchronized void tag(final JSONObject user) throws RepositoryException {
        // Clear
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(User.USER + '_' + Keys.OBJECT_ID,
                FilterOperator.EQUAL, user.optString(Keys.OBJECT_ID)));
        filters.add(new PropertyFilter(Common.TYPE, FilterOperator.EQUAL, Tag.TAG_TYPE_C_USER_SELF));

        final Query query = new Query();
        query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        final JSONArray results = userTagRepository.get(query).optJSONArray(Keys.RESULTS);
        for (int i = 0; i < results.length(); i++) {
            final JSONObject rel = results.optJSONObject(i);
            final String id = rel.optString(Keys.OBJECT_ID);

            userTagRepository.remove(id);
        }

        // Add
        String tagTitleStr = user.optString(UserExt.USER_TAGS);
        final String[] tagTitles = tagTitleStr.split(",");

        for (final String title : tagTitles) {
            final String tagTitle = title.trim();
            JSONObject tag = tagRepository.getByTitle(tagTitle);
            String tagId;

            if (null == tag) {
                LOGGER.log(Level.TRACE, "Found a new tag[title={0}] in user [name={1}]",
                        tagTitle, user.optString(User.USER_NAME));
                tag = new JSONObject();
                tag.put(Tag.TAG_TITLE, tagTitle);
                String tagURI = tagTitle;
                tagURI = URLs.encode(tagTitle);
                tag.put(Tag.TAG_URI, tagURI);
                tag.put(Tag.TAG_CSS, "");
                tag.put(Tag.TAG_REFERENCE_CNT, 0);
                tag.put(Tag.TAG_COMMENT_CNT, 0);
                tag.put(Tag.TAG_FOLLOWER_CNT, 0);
                tag.put(Tag.TAG_LINK_CNT, 0);
                tag.put(Tag.TAG_DESCRIPTION, "");
                tag.put(Tag.TAG_ICON_PATH, "");
                tag.put(Tag.TAG_STATUS, 0);
                tag.put(Tag.TAG_GOOD_CNT, 0);
                tag.put(Tag.TAG_BAD_CNT, 0);
                tag.put(Tag.TAG_SEO_TITLE, tagTitle);
                tag.put(Tag.TAG_SEO_KEYWORDS, tagTitle);
                tag.put(Tag.TAG_SEO_DESC, "");
                tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());
                tag.put(Tag.TAG_AD, "");
                tag.put(Tag.TAG_SHOW_SIDE_AD, 0);

                tagId = tagRepository.add(tag);

                final JSONObject tagCntOption = optionRepository.get(Option.ID_C_STATISTIC_TAG_COUNT);
                final int tagCnt = tagCntOption.optInt(Option.OPTION_VALUE);
                tagCntOption.put(Option.OPTION_VALUE, tagCnt + 1);
                optionRepository.update(Option.ID_C_STATISTIC_TAG_COUNT, tagCntOption);

                // User-Tag relation (creator)
                final JSONObject userTagRelation = new JSONObject();
                userTagRelation.put(Tag.TAG + '_' + Keys.OBJECT_ID, tagId);
                userTagRelation.put(User.USER + '_' + Keys.OBJECT_ID, user.optString(Keys.OBJECT_ID));
                userTagRelation.put(Common.TYPE, Tag.TAG_TYPE_C_CREATOR);

                userTagRepository.add(userTagRelation);
            } else {
                tagId = tag.optString(Keys.OBJECT_ID);
                LOGGER.log(Level.TRACE, "Found a existing tag[title={0}, id={1}] in user[name={2}]",
                        tag.optString(Tag.TAG_TITLE), tag.optString(Keys.OBJECT_ID), user.optString(User.USER_NAME));

                tagTitleStr = tagTitleStr.replaceAll("(?i)" + Pattern.quote(tagTitle), tag.optString(Tag.TAG_TITLE));
            }

            // User-Tag relation (userself)
            final JSONObject userTagRelation = new JSONObject();
            userTagRelation.put(Tag.TAG + '_' + Keys.OBJECT_ID, tagId);
            userTagRelation.put(User.USER + '_' + Keys.OBJECT_ID, user.optString(Keys.OBJECT_ID));
            userTagRelation.put(Common.TYPE, Tag.TAG_TYPE_C_USER_SELF);

            userTagRepository.add(userTagRelation);
        }

        user.put(UserExt.USER_TAGS, tagTitleStr);
    }
}
