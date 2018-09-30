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

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Times;
import org.b3log.latke.util.URLs;
import org.b3log.symphony.model.*;
import org.b3log.symphony.repository.FollowRepository;
import org.b3log.symphony.repository.PointtransferRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Sessions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * User query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.8.7.1, Jal 28, 2018
 * @since 0.2.0
 */
@Service
public class UserQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UserQueryService.class);

    /**
     * All usernames.
     */
    public static final List<JSONObject> USER_NAMES = Collections.synchronizedList(new ArrayList<>());

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Follow repository.
     */
    @Inject
    private FollowRepository followRepository;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Pointtransfer repository.
     */
    @Inject
    private PointtransferRepository pointtransferRepository;

    /**
     * Role query service.
     */
    @Inject
    private RoleQueryService roleQueryService;

    /**
     * Get nice users with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return a list of users
     */
    public List<JSONObject> getNiceUsers(int fetchSize) {
        final List<JSONObject> ret = new ArrayList<>();

        final int RANGE_SIZE = 64;

        try {
            final Query userQuery = new Query();
            userQuery.setCurrentPageNum(1).setPageCount(1).setPageSize(RANGE_SIZE).
                    setFilter(new PropertyFilter(UserExt.USER_STATUS, FilterOperator.EQUAL, UserExt.USER_STATUS_C_VALID)).
                    addSort(UserExt.USER_ARTICLE_COUNT, SortDirection.DESCENDING).
                    addSort(UserExt.USER_COMMENT_COUNT, SortDirection.DESCENDING);
            final JSONArray rangeUsers = userRepository.get(userQuery).optJSONArray(Keys.RESULTS);

            final int realLen = rangeUsers.length();
            if (realLen < fetchSize) {
                fetchSize = realLen;
            }


            final List<Integer> indices = CollectionUtils.getRandomIntegers(0, realLen, fetchSize);

            for (final Integer index : indices) {
                ret.add(rangeUsers.getJSONObject(index));
            }

            for (final JSONObject selectedUser : ret) {
                avatarQueryService.fillUserAvatarURL(UserExt.USER_AVATAR_VIEW_MODE_C_STATIC, selectedUser);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Get nice users failed", e);
        }

        return ret;
    }

    /**
     * Gets invite user count of a user specified by the given user id.
     *
     * @param userId the given user id
     * @return invited user count
     */
    public int getInvitedUserCount(final String userId) {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Pointtransfer.TO_ID, FilterOperator.EQUAL, userId),
                CompositeFilterOperator.or(
                        new PropertyFilter(Pointtransfer.TYPE, FilterOperator.EQUAL,
                                Pointtransfer.TRANSFER_TYPE_C_INVITECODE_USED),
                        new PropertyFilter(Pointtransfer.TYPE, FilterOperator.EQUAL,
                                Pointtransfer.TRANSFER_TYPE_C_INVITE_REGISTER))
        ));

        try {
            return (int) pointtransferRepository.count(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets invited user count failed", e);

            return 0;
        }
    }

    /**
     * Gets latest logged in users by the specified time.
     *
     * @param time           the specified start time
     * @param currentPageNum the specified current page number
     * @param pageSize       the specified page size
     * @param windowSize     the specified window size
     * @return for example,      <pre>
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
    public JSONObject getLatestLoggedInUsers(final long time, final int currentPageNum, final int pageSize,
                                             final int windowSize) throws ServiceException {
        final JSONObject ret = new JSONObject();

        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setCurrentPageNum(currentPageNum).setPageSize(pageSize)
                .setFilter(CompositeFilterOperator.and(
                        new PropertyFilter(UserExt.USER_STATUS, FilterOperator.EQUAL, UserExt.USER_STATUS_C_VALID),
                        new PropertyFilter(UserExt.USER_LATEST_LOGIN_TIME, FilterOperator.GREATER_THAN_OR_EQUAL, time)
                ));

        JSONObject result = null;
        try {
            result = userRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets latest logged in user failed", e);

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
     * Gets user count of the specified day.
     *
     * @param day the specified day
     * @return user count
     */
    public int getUserCntInDay(final Date day) {
        final long time = day.getTime();
        final long start = Times.getDayStartTime(time);
        final long end = Times.getDayEndTime(time);

        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, start),
                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, end),
                new PropertyFilter(UserExt.USER_STATUS, FilterOperator.EQUAL, UserExt.USER_STATUS_C_VALID)
        ));

        try {
            return (int) userRepository.count(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Count day user failed", e);

            return 1;
        }
    }

    /**
     * Gets user count of the specified month.
     *
     * @param month the specified month
     * @return user count
     */
    public int getUserCntInMonth(final Date month) {
        final long time = month.getTime();
        final long start = Times.getMonthStartTime(time);
        final long end = Times.getMonthEndTime(time);

        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, start),
                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, end),
                new PropertyFilter(UserExt.USER_STATUS, FilterOperator.EQUAL, UserExt.USER_STATUS_C_VALID)
        ));

        try {
            return (int) userRepository.count(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Count month user failed", e);

            return 1;
        }
    }

    /**
     * Loads all usernames from database.
     */
    public void loadUserNames() {
        USER_NAMES.clear();

        final Query query = new Query().setPageCount(1).
                setFilter(new PropertyFilter(UserExt.USER_STATUS, FilterOperator.EQUAL, UserExt.USER_STATUS_C_VALID)).
                addProjection(User.USER_NAME, String.class).
                addProjection(UserExt.USER_AVATAR_URL, String.class);
        try {
            final JSONObject result = userRepository.get(query); // XXX: Performance Issue
            final JSONArray array = result.optJSONArray(Keys.RESULTS);
            for (int i = 0; i < array.length(); i++) {
                final JSONObject user = array.optJSONObject(i);

                final JSONObject u = new JSONObject();
                u.put(User.USER_NAME, user.optString(User.USER_NAME));
                u.put(UserExt.USER_T_NAME_LOWER_CASE, user.optString(User.USER_NAME).toLowerCase());
                final String avatar = avatarQueryService.getAvatarURLByUser(UserExt.USER_AVATAR_VIEW_MODE_C_STATIC, user, "20");
                u.put(UserExt.USER_AVATAR_URL, avatar);
                USER_NAMES.add(u);
            }

            Collections.sort(USER_NAMES, (u1, u2) -> {
                final String u1Name = u1.optString(UserExt.USER_T_NAME_LOWER_CASE);
                final String u2Name = u2.optString(UserExt.USER_T_NAME_LOWER_CASE);

                return u1Name.compareTo(u2Name);
            });
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Loads usernames error", e);
        }
    }

    /**
     * Gets usernames by the specified name prefix.
     *
     * @param namePrefix the specified name prefix
     * @return a list of usernames, for example      <pre>
     * [
     *     {
     *         "userName": "",
     *         "userAvatarURL": "",
     *     }, ....
     * ]
     * </pre>
     */
    public List<JSONObject> getUserNamesByPrefix(final String namePrefix) {
        final JSONObject nameToSearch = new JSONObject();
        nameToSearch.put(UserExt.USER_T_NAME_LOWER_CASE, namePrefix.toLowerCase());

        int index = Collections.binarySearch(USER_NAMES, nameToSearch, (u1, u2) -> {
            String u1Name = u1.optString(UserExt.USER_T_NAME_LOWER_CASE);
            final String inputName = u2.optString(UserExt.USER_T_NAME_LOWER_CASE);

            if (u1Name.length() < inputName.length()) {
                return u1Name.compareTo(inputName);
            }

            u1Name = u1Name.substring(0, inputName.length());

            return u1Name.compareTo(inputName);
        });

        final List<JSONObject> ret = new ArrayList<>();

        if (index < 0) {
            return ret;
        }

        int start = index;
        int end = index;

        while (start > -1 && USER_NAMES.get(start).optString(UserExt.USER_T_NAME_LOWER_CASE).startsWith(namePrefix.toLowerCase())) {
            start--;
        }

        start++;

        if (start < index - 5) {
            end = start + 5;
        } else {
            while (end < USER_NAMES.size() && end < index + 5 && USER_NAMES.get(end).optString(UserExt.USER_T_NAME_LOWER_CASE).startsWith(namePrefix.toLowerCase())) {
                end++;

                if (end >= start + 5) {
                    break;
                }
            }
        }

        return USER_NAMES.subList(start, end);
    }

    /**
     * Gets the current user.
     *
     * @param request the specified request
     * @return the current user, {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getCurrentUser(final HttpServletRequest request) throws ServiceException {
        final JSONObject currentUser = Sessions.currentUser(request);
        if (null == currentUser) {
            return null;
        }

        final String id = currentUser.optString(Keys.OBJECT_ID);

        return getUser(id);
    }

    /**
     * Gets the administrators.
     *
     * @return administrators, returns an empty list if not found or error
     * @throws ServiceException service exception
     */
    public List<JSONObject> getAdmins() throws ServiceException {
        try {
            return userRepository.getAdmins();
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets admins failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the super administrator.
     *
     * @return super administrator
     * @throws ServiceException service exception
     */
    public JSONObject getSA() throws ServiceException {
        return getAdmins().get(0);
    }

    /**
     * Gets the community bot.
     *
     * @return default commenter
     */
    public JSONObject getComBot() {
        final JSONObject ret = getUserByName(UserExt.COM_BOT_NAME);
        ret.remove(UserExt.USER_T_POINT_HEX);
        ret.remove(UserExt.USER_T_POINT_CC);

        return ret;
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
     * <p>
     * A user name is between &#64; and a punctuation, a blank or a line break (\n). For example, the specified text is
     * <pre>&#64;88250 It is a nice day. &#64;Vanessa, we are on the way.</pre> There are two user names in the text,
     * 88250 and Vanessa.
     * </p>
     *
     * @param text the specified text
     * @return user names, returns an empty set if not found
     */
    public Set<String> getUserNames(final String text) {
        final Set<String> ret = new HashSet<>();
        int idx = text.indexOf('@');
        if (-1 == idx) {
            return ret;
        }

        String copy = text.trim();
        copy = copy.replaceAll("\\n", " ");
        String[] uNames = StringUtils.substringsBetween(copy, "@", " ");
        String tail = StringUtils.substringAfterLast(copy, "@");
        if (tail.contains(" ")) {
            tail = null;
        }

        if (null != tail) {
            if (null == uNames) {
                uNames = new String[1];
                uNames[0] = tail;
            } else {
                uNames = Arrays.copyOf(uNames, uNames.length + 1);
                uNames[uNames.length - 1] = tail;
            }
        }

        String[] uNames2 = StringUtils.substringsBetween(copy, "@", "<");
        final Set<String> maybeUserNameSet;
        if (null == uNames) {
            uNames = uNames2;

            if (null == uNames) {
                return ret;
            }

            maybeUserNameSet = CollectionUtils.arrayToSet(uNames);
        } else {
            maybeUserNameSet = CollectionUtils.arrayToSet(uNames);

            if (null != uNames2) {
                maybeUserNameSet.addAll(CollectionUtils.arrayToSet(uNames2));
            }
        }

        for (String maybeUserName : maybeUserNameSet) {
            maybeUserName = maybeUserName.trim();
            if (null != getUserByName(maybeUserName)) { // Found a user
                ret.add(maybeUserName);
            }
        }

        return ret;
    }

    /**
     * Gets a user by the specified name.
     *
     * @param name the specified name
     * @return user, returns {@code null} if not found
     */
    public JSONObject getUserByName(final String name) {
        try {
            final JSONObject ret = userRepository.getByName(name);
            if (null == ret) {
                return null;
            }

            final int point = ret.optInt(UserExt.USER_POINT);
            final int appRole = ret.optInt(UserExt.USER_APP_ROLE);
            if (UserExt.USER_APP_ROLE_C_HACKER == appRole) {
                ret.put(UserExt.USER_T_POINT_HEX, Integer.toHexString(point));
            } else {
                ret.put(UserExt.USER_T_POINT_CC, UserExt.toCCString(point));
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets user by name[" + name + "] failed", e);

            return null;
        }
    }

    /**
     * Gets users by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "userNameOrEmail": "", // optional
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10,
     *                          , see {@link Pagination} for more details
     * @return for example,      <pre>
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
        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                setCurrentPageNum(currentPageNum).setPageSize(pageSize);

        if (requestJSONObject.has(Common.QUERY)) {
            final String q = requestJSONObject.optString(Common.QUERY);
            final List<Filter> filters = new ArrayList<>();
            filters.add(new PropertyFilter(User.USER_NAME, FilterOperator.EQUAL, q));
            filters.add(new PropertyFilter(User.USER_EMAIL, FilterOperator.EQUAL, q));
            filters.add(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, q));
            query.setFilter(new CompositeFilter(CompositeFilterOperator.OR, filters));
        }

        JSONObject result;
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

        for (int i = 0; i < users.length(); i++) {
            final JSONObject user = users.optJSONObject(i);
            user.put(UserExt.USER_T_CREATE_TIME, new Date(user.optLong(Keys.OBJECT_ID)));

            avatarQueryService.fillUserAvatarURL(UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, user);

            final JSONObject role = roleQueryService.getRole(user.optString(User.USER_ROLE));
            user.put(Role.ROLE_NAME, role.optString(Role.ROLE_NAME));
        }

        return ret;
    }

    /**
     * Gets users by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "userCity": "",
     *                          "userLatestLoginTime": long, // optional, default to 0
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10,
     *                          }, see {@link Pagination} for more details
     * @return for example,      <pre>
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
    public JSONObject getUsersByCity(final JSONObject requestJSONObject) throws ServiceException {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final String city = requestJSONObject.optString(UserExt.USER_CITY);
        final long latestTime = requestJSONObject.optLong(UserExt.USER_LATEST_LOGIN_TIME);

        final Query query = new Query().addSort(UserExt.USER_LATEST_LOGIN_TIME, SortDirection.DESCENDING)
                .setCurrentPageNum(currentPageNum).setPageSize(pageSize)
                .setFilter(CompositeFilterOperator.and(
                        new PropertyFilter(UserExt.USER_CITY, FilterOperator.EQUAL, city),
                        new PropertyFilter(UserExt.USER_GEO_STATUS, FilterOperator.EQUAL, UserExt.USER_GEO_STATUS_C_PUBLIC),
                        new PropertyFilter(UserExt.USER_STATUS, FilterOperator.EQUAL, UserExt.USER_STATUS_C_VALID),
                        new PropertyFilter(UserExt.USER_LATEST_LOGIN_TIME, FilterOperator.GREATER_THAN_OR_EQUAL, latestTime)
                ));
        JSONObject result;
        try {
            result = userRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets users by city error", e);

            throw new ServiceException(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray users = result.optJSONArray(Keys.RESULTS);
        try {
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                users.getJSONObject(i).put(Common.IS_FOLLOWING,
                        followRepository.exists(requestJSONObject.optString(Keys.OBJECT_ID), user.optString(Keys.OBJECT_ID),
                                Follow.FOLLOWING_TYPE_C_USER));
            }
        } catch (final RepositoryException | JSONException e) {
            LOGGER.log(Level.ERROR, "Fills following failed", e);
        }
        ret.put(User.USERS, users);
        return ret;
    }

    /**
     * Gets a user by the specified user id.
     *
     * @param userId the specified user id
     * @return for example,      <pre>
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
     * @param redirectURL redirect URL after logged in
     * @return logout URL, returns {@code null} if the user is not logged in
     */
    public String getLogoutURL(final String redirectURL) {
        String to = Latkes.getServePath();
        to = URLs.encode(to + redirectURL);

        return Latkes.getContextPath() + "/logout?goto=" + to;
    }

    /**
     * Gets the URL of user login.
     *
     * @param redirectURL redirect URL after logged in
     * @return login URL
     */
    public String getLoginURL(final String redirectURL) {
        String to = Latkes.getServePath();
        to = URLs.encode(to + redirectURL);

        return Latkes.getContextPath() + "/login?goto=" + to;
    }
}
