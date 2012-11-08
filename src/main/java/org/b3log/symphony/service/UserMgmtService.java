/*
 * Copyright (c) 2012, B3log Team
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.OptionRepository;
import org.b3log.symphony.repository.UserRepository;
import org.json.JSONObject;

/**
 * User management service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.7, Nov 8, 2012
 * @since 0.2.0
 */
public final class UserMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UserMgmtService.class.getName());
    /**
     * Singleton.
     */
    private static final UserMgmtService SINGLETON = new UserMgmtService();
    /**
     * User repository.
     */
    private UserRepository userRepository = UserRepository.getInstance();
    /**
     * Option repository.
     */
    private OptionRepository optionRepository = OptionRepository.getInstance();
    /**
     * Language service.
     */
    private LangPropsService langPropsService = LangPropsService.getInstance();

    /**
     * Updates a user's online status and saves the login time.
     * 
     * @param userId the specified user id
     * @param onlineFlag the specified online flag
     * @throws ServiceException service exception 
     */
    public void updateOnlineStatus(final String userId, final boolean onlineFlag) throws ServiceException {
        Transaction transaction = null;

        try {
            final JSONObject user = userRepository.get(userId);
            if (null == user) {
                return;
            }

            transaction = userRepository.beginTransaction();

            user.put(UserExt.USER_ONLINE_FLAG, onlineFlag);
            user.put(UserExt.USER_LATEST_LOGIN_TIME, System.currentTimeMillis());

            userRepository.update(userId, user);

            transaction.commit();
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Updates user online status failed", e);

            if (null != transaction && transaction.isActive()) {
                transaction.rollback();
            }

            throw new ServiceException(e);
        }
    }

    /**
     * Updates a user's profiles by the specified request json object.
     *
     * @param requestJSONObject the specified request json object (user), for example,
     * <pre>
     * {
     *     "oId": "",
     *     "userURL": "",
     *     "userQQ": "",
     *     "userIntro": ""
     * }
     * </pre>
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

            // Update
            oldUser.put(User.USER_URL, requestJSONObject.optString(User.USER_URL));
            oldUser.put(UserExt.USER_QQ, requestJSONObject.optString(UserExt.USER_QQ));
            oldUser.put(UserExt.USER_INTRO, requestJSONObject.optString(UserExt.USER_INTRO));

            userRepository.update(oldUserId, oldUser);
            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.SEVERE, "Updates user profiles failed", e);
            throw new ServiceException(langPropsService.get("updateFailLabel"));
        }
    }

    /**
     * Updates a user's sync B3log settings by the specified request json object.
     *
     * @param requestJSONObject the specified request json object (user), for example,
     * <pre>
     * {
     *     "oId": "",
     *     "userB3Key": "",
     *     "userB3ClientAddArticleURL": "",
     *     "userB3ClientAddCommentURL": "",
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public void updateSyncB3(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = userRepository.beginTransaction();

        try {
            final String oldUserId = requestJSONObject.optString(Keys.OBJECT_ID);
            final JSONObject oldUser = userRepository.get(oldUserId);

            if (null == oldUser) {
                throw new ServiceException(langPropsService.get("updateFailLabel"));
            }

            // Update
            oldUser.put(UserExt.USER_B3_KEY, requestJSONObject.optString(UserExt.USER_B3_KEY));
            oldUser.put(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL, requestJSONObject.optString(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL));
            oldUser.put(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL, requestJSONObject.optString(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL));

            userRepository.update(oldUserId, oldUser);
            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.SEVERE, "Updates user sync b3log settings failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates a user's password by the specified request json object.
     *
     * @param requestJSONObject the specified request json object (user), for example,
     * <pre>
     * {
     *     "oId": "",
     *     "userPassword": "",
     * }
     * </pre>
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

            LOGGER.log(Level.SEVERE, "Updates user sync b3log settings failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Adds a user with the specified request json object.
     * 
     * @param requestJSONObject the specified request json object, for example,
     * <pre>
     * {
     *     "userName": "",
     *     "userEmail": "",
     *     "userPassword": "",
     *     "userRole": "" // optional, uses {@value Role#DEFAULT_ROLE} instead, if not speciffied
     * }
     * </pre>,see {@link User} for more details
     * @return generated user id
     * @throws ServiceException if user name or email duplicated, or repository exception
     */
    public String addUser(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = userRepository.beginTransaction();

        try {
            final String userEmail = requestJSONObject.optString(User.USER_EMAIL).trim().toLowerCase();
            final String userName = requestJSONObject.optString(User.USER_NAME);
            if (null != userRepository.getByName(userName)) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                throw new ServiceException(langPropsService.get("duplicatedUserNameLabel"));
            }

            if (null != userRepository.getByEmail(userEmail)) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                throw new ServiceException(langPropsService.get("duplicatedEmailLabel"));
            }

            final JSONObject user = new JSONObject();

            user.put(User.USER_EMAIL, userEmail);
            user.put(User.USER_NAME, userName);
            user.put(User.USER_PASSWORD, requestJSONObject.optString(User.USER_PASSWORD));
            user.put(User.USER_ROLE, requestJSONObject.optString(User.USER_ROLE, Role.DEFAULT_ROLE));

            user.put(User.USER_URL, "");
            user.put(UserExt.USER_ARTICLE_COUNT, 0);
            user.put(UserExt.USER_COMMENT_COUNT, 0);
            user.put(UserExt.USER_TAG_COUNT, 0);
            user.put(UserExt.USER_STATUS, 0);
            user.put(UserExt.USER_B3_KEY, "");
            user.put(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL, "");
            user.put(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL, "");
            user.put(UserExt.USER_INTRO, "");
            user.put(UserExt.USER_QQ, "");
            user.put(UserExt.USER_ONLINE_FLAG, false);
            user.put(UserExt.USER_LATEST_ARTICLE_TIME, 0L);
            user.put(UserExt.USER_LATEST_CMT_TIME, 0L);
            user.put(UserExt.USER_LATEST_LOGIN_TIME, 0L);

            final JSONObject memberCntOption = optionRepository.get(Option.ID_C_STATISTIC_MEMBER_COUNT);
            int memberCount = memberCntOption.optInt(Option.OPTION_VALUE);
            ++memberCount;
            user.put(UserExt.USER_NO, memberCount);

            userRepository.add(user);

            // Updates stat. (member count +1)
            memberCntOption.put(Option.OPTION_VALUE, String.valueOf(memberCount));
            optionRepository.update(Option.ID_C_STATISTIC_MEMBER_COUNT, memberCntOption);

            transaction.commit();

            return user.optString(Keys.OBJECT_ID);
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.SEVERE, "Adds a user failed", e);
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

            LOGGER.log(Level.SEVERE, "Removes a user[id=" + userId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the {@link UserMgmtService} singleton.
     *
     * @return the singleton
     */
    public static UserMgmtService getInstance() {
        return SINGLETON;
    }

    /**
     * Private constructor.
     */
    private UserMgmtService() {
    }
}
