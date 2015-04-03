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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.user.GeneralUser;
import org.b3log.latke.user.UserService;
import org.b3log.latke.user.UserServiceFactory;
import org.b3log.latke.util.Paginator;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.repository.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * User query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.4, Oct 11, 2013
 * @since 0.2.0
 */
@Service
public class UserQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UserQueryService.class.getName());

    /**
     * User service.
     */
    private UserService userService = UserServiceFactory.getUserService();

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Gets the current user.
     *
     * @param request the specified request
     * @return the current user, {@code null} if not found
     * @throws ServiceException service exception 
     */
    public JSONObject getCurrentUser(final HttpServletRequest request) throws ServiceException {
        final GeneralUser currentUser = UserServiceFactory.getUserService().getCurrentUser(request);
        if (null == currentUser) {
            return null;
        }

        final String email = currentUser.getEmail();

        return getUserByEmail(email);
    }

    /**
     * Gets the administrator.
     * 
     * @return administrator, returns {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getAdmin() throws ServiceException {
        try {
            return userRepository.getAdmin();
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets admin failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the default commenter.
     * 
     * @return default commenter
     * @throws ServiceException service exception
     */
    public JSONObject getDefaultCommenter() throws ServiceException {
        return getUserByName(UserExt.DEFAULT_CMTER_NAME);
    }

    /**
     * Gets a user by the specified email.
     *
     * @param email the specified email
     * @return user, returns {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getUserByEmail(final String email) throws ServiceException {
        try {
            return userRepository.getByEmail(email);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets user by email[" + email + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets user names from the specified text.
     * 
     * <p>
     * A user name is between &#64; and a punctuation, a blank or a line break (\n).
     * For example, the specified text is 
     * <pre>&#64;88250 It is a nice day. &#64;Vanessa, we are on the way.</pre>
     * There are two user names in the text, 88250 and Vanessa.
     * </p>
     * 
     * @param text the specified text
     * @return user names, returns an empty set if not found
     * @throws ServiceException service exception 
     */
    public Set<String> getUserNames(final String text) throws ServiceException {
        final Set<String> ret = new HashSet<String>();

        int idx = text.indexOf('@');

        if (-1 == idx) {
            return ret;
        }

        String copy = text.trim();
        copy = copy.replaceAll("\\n", " ");
        String[] userNames = StringUtils.substringsBetween(copy, "@", " ");
        String tail = StringUtils.substringAfterLast(copy, "@");
        
        if (tail.contains(" ")) {
            tail = null;
        }
        
        if (null != tail) {
            if (null == userNames) {
                userNames = new String[1];
                userNames[0] = tail;
            } else {
                userNames = Arrays.copyOf(userNames, userNames.length + 1);
                userNames[userNames.length - 1] = tail;
            }
        }
        
        if (null == userNames) {
            return ret;
        }

        for (int i = 0; i < userNames.length; i++) {
            final String maybeUserName = userNames[i];

            if (!UserRegisterValidation.invalidUserName(maybeUserName)) { // A string match the user name pattern
                if (null != getUserByName(maybeUserName)) { // Found a user
                    ret.add(maybeUserName);

                    copy = copy.replace("@" + maybeUserName, "");
                    idx = copy.indexOf('@');
                    if (-1 == idx) {
                        return ret;
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Gets a user by the specified name.
     *
     * @param name the specified name
     * @return user, returns {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getUserByName(final String name) throws ServiceException {
        try {
            return userRepository.getByName(name);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets user by name[" + name + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets users by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     * <pre>
     * {
     *     "paginationCurrentPageNum": 1,
     *     "paginationPageSize": 20,
     *     "paginationWindowSize": 10,
     * }, see {@link Pagination} for more details
     * </pre>
     * @return for example,
     * <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "users": [{
     *         "oId": "",
     *         "userName": "",
     *         "userEmail": "",
     *         "userPassword": "",
     *         "roleName": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     * @see Pagination
     */
    public JSONObject getUsers(final JSONObject requestJSONObject) throws ServiceException {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize);

        JSONObject result = null;

        try {
            result = userRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets users failed", e);

            throw new ServiceException(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray users = result.optJSONArray(Keys.RESULTS);
        ret.put(User.USERS, users);

        return ret;
    }

    /**
     * Gets a user by the specified user id.
     *
     * @param userId the specified user id
     * @return for example,
     * <pre>
     * {
     *     "oId": "",
     *     "userName": "",
     *     "userEmail": "",
     *     "userPassword": "",
     *     ....
     * }
     * </pre>, returns {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getUser(final String userId) throws ServiceException {
        try {
            return userRepository.get(userId);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets a user failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the URL of user logout.
     *
     * @return logout URL, returns {@code null} if the user is not logged in
     */
    public String getLogoutURL() {
        return userService.createLogoutURL("/");
    }

    /**
     * Gets the URL of user login.
     *
     * @param redirectURL redirect URL after logged in
     * @return login URL
     */
    public String getLoginURL(final String redirectURL) {
        return userService.createLoginURL(redirectURL);
    }
}
