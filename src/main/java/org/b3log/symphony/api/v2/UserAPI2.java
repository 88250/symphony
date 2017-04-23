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
package org.b3log.symphony.api.v2;

import org.b3log.latke.Keys;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.AnonymousViewCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.StatusCodes;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * User API v2.
 * <p>
 * <ul>
 * <li>Gets a user (/api/v2/user/{userName}), GET</li>
 * <li>Gets a user's articles (/api/v2/user/{userName}/articles), GET</li>
 * <li>Gets a user's comments (/api/v2/user/{userName}/comments), GET</li>
 * <li>Gets a user's watching articles (/api/v2/user/{userName}/watching/articles), GET</li>
 * <li>Gets a user's following users (/api/v2/user/{userName}/following/users), GET</li>
 * <li>Gets a user's following tags (/api/v2/user/{userName}/following/tags), GET</li>
 * <li>Gets a user's following articles (/api/v2/user/{userName}/following/articles), GET</li>
 * <li>Gets a user's followers (/api/v2/user/{userName}/followers), GET</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.0, Mar 8, 2016
 * @since 2.1.0
 */
@RequestProcessor
public class UserAPI2 {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UserAPI2.class);
    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;
    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;
    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;
    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;
    /**
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    /**
     * Gets a user's followers.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param userName the specified username
     */
    @RequestProcessing(value = {"/api/v2/user/{userName}/followers"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getUserFollowers(final HTTPRequestContext context, final HttpServletRequest request, final String userName) {
        int page = 1;
        final String p = request.getParameter("p");
        if (Strings.isNumeric(p)) {
            page = Integer.parseInt(p);
        }

        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        if (UserRegisterValidation.invalidUserName(userName)) {
            ret.put(Keys.MSG, "User not found");
            ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

            return;
        }

        JSONObject data = null;
        try {
            final JSONObject user = userQueryService.getUserByName(userName);
            if (null == user) {
                ret.put(Keys.MSG, "User not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            final String followerId = user.optString(Keys.OBJECT_ID);
            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

            final JSONObject followersResult = followQueryService.getFollowerUsers(avatarViewMode,
                    followerId, page, V2s.PAGE_SIZE);
            final List<JSONObject> followers = (List<JSONObject>) followersResult.opt(Keys.RESULTS);
            V2s.cleanUsers(followers);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
            data = new JSONObject();
            data.put(User.USERS, followers);

            final int followerUserCnt = followersResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
            final int pageCount = (int) Math.ceil((double) followerUserCnt / (double) V2s.PAGE_SIZE);

            final JSONObject pagination = new JSONObject();
            final List<Integer> pageNums = Paginator.paginate(page, V2s.PAGE_SIZE, pageCount, V2s.WINDOW_SIZE);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

            data.put(Pagination.PAGINATION, pagination);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets a user's followers failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }

    /**
     * Gets a user's following articles.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param userName the specified username
     */
    @RequestProcessing(value = {"/api/v2/user/{userName}/following/articles"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getUserFollowingArticles(final HTTPRequestContext context, final HttpServletRequest request, final String userName) {
        int page = 1;
        final String p = request.getParameter("p");
        if (Strings.isNumeric(p)) {
            page = Integer.parseInt(p);
        }

        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        if (UserRegisterValidation.invalidUserName(userName)) {
            ret.put(Keys.MSG, "User not found");
            ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

            return;
        }

        JSONObject data = null;
        try {
            final JSONObject user = userQueryService.getUserByName(userName);
            if (null == user) {
                ret.put(Keys.MSG, "User not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            final String followerId = user.optString(Keys.OBJECT_ID);
            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

            final JSONObject followingArticlesResult = followQueryService.getFollowingArticles(avatarViewMode,
                    followerId, page, V2s.PAGE_SIZE);
            final List<JSONObject> articles = (List<JSONObject>) followingArticlesResult.opt(Keys.RESULTS);
            V2s.cleanArticles(articles);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
            data = new JSONObject();
            data.put(Article.ARTICLES, articles);

            final int followingArticleCnt = followingArticlesResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
            final int pageCount = (int) Math.ceil(followingArticleCnt / (double) V2s.PAGE_SIZE);

            final JSONObject pagination = new JSONObject();
            final List<Integer> pageNums = Paginator.paginate(page, V2s.PAGE_SIZE, pageCount, V2s.WINDOW_SIZE);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

            data.put(Pagination.PAGINATION, pagination);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets a user's following articles failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }

    /**
     * Gets a user's following tags.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param userName the specified username
     */
    @RequestProcessing(value = {"/api/v2/user/{userName}/following/tags"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getUserFollowingTags(final HTTPRequestContext context, final HttpServletRequest request, final String userName) {
        int page = 1;
        final String p = request.getParameter("p");
        if (Strings.isNumeric(p)) {
            page = Integer.parseInt(p);
        }

        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        if (UserRegisterValidation.invalidUserName(userName)) {
            ret.put(Keys.MSG, "User not found");
            ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

            return;
        }

        JSONObject data = null;
        try {
            final JSONObject user = userQueryService.getUserByName(userName);
            if (null == user) {
                ret.put(Keys.MSG, "User not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            final String followerId = user.optString(Keys.OBJECT_ID);
            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

            final JSONObject followingTagsResult = followQueryService.getFollowingTags(
                    followerId, page, V2s.PAGE_SIZE);
            final List<JSONObject> tags = (List<JSONObject>) followingTagsResult.opt(Keys.RESULTS);
            V2s.cleanTags(tags);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
            data = new JSONObject();
            data.put(Tag.TAGS, tags);

            final int followingTagCnt = followingTagsResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
            final int pageCount = (int) Math.ceil(followingTagCnt / (double) V2s.PAGE_SIZE);

            final JSONObject pagination = new JSONObject();
            final List<Integer> pageNums = Paginator.paginate(page, V2s.PAGE_SIZE, pageCount, V2s.WINDOW_SIZE);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

            data.put(Pagination.PAGINATION, pagination);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets a user's following tags failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }

    /**
     * Gets a user's following users.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param userName the specified username
     */
    @RequestProcessing(value = {"/api/v2/user/{userName}/following/users"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getUserFollowingUsers(final HTTPRequestContext context, final HttpServletRequest request, final String userName) {
        int page = 1;
        final String p = request.getParameter("p");
        if (Strings.isNumeric(p)) {
            page = Integer.parseInt(p);
        }

        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        if (UserRegisterValidation.invalidUserName(userName)) {
            ret.put(Keys.MSG, "User not found");
            ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

            return;
        }

        JSONObject data = null;
        try {
            final JSONObject user = userQueryService.getUserByName(userName);
            if (null == user) {
                ret.put(Keys.MSG, "User not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            final String followerId = user.optString(Keys.OBJECT_ID);
            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

            final JSONObject followingUsersResult = followQueryService.getFollowingUsers(avatarViewMode,
                    followerId, page, V2s.PAGE_SIZE);
            final List<JSONObject> users = (List<JSONObject>) followingUsersResult.opt(Keys.RESULTS);
            V2s.cleanUsers(users);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
            data = new JSONObject();
            data.put(User.USERS, users);

            final int followingUserCnt = followingUsersResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
            final int pageCount = (int) Math.ceil((double) followingUserCnt / (double) V2s.PAGE_SIZE);

            final JSONObject pagination = new JSONObject();
            final List<Integer> pageNums = Paginator.paginate(page, V2s.PAGE_SIZE, pageCount, V2s.WINDOW_SIZE);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

            data.put(Pagination.PAGINATION, pagination);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets a user's following users failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }

    /**
     * Gets a user's watching articles.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param userName the specified username
     */
    @RequestProcessing(value = {"/api/v2/user/{userName}/watching/articles"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getUserWatchingArticles(final HTTPRequestContext context, final HttpServletRequest request, final String userName) {
        int page = 1;
        final String p = request.getParameter("p");
        if (Strings.isNumeric(p)) {
            page = Integer.parseInt(p);
        }

        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        if (UserRegisterValidation.invalidUserName(userName)) {
            ret.put(Keys.MSG, "User not found");
            ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

            return;
        }

        JSONObject data = null;
        try {
            final JSONObject user = userQueryService.getUserByName(userName);
            if (null == user) {
                ret.put(Keys.MSG, "User not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            final String followerId = user.optString(Keys.OBJECT_ID);
            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

            final JSONObject watchingArticlesResult = followQueryService.getWatchingArticles(avatarViewMode,
                    followerId, page, V2s.PAGE_SIZE);
            final List<JSONObject> articles = (List<JSONObject>) watchingArticlesResult.opt(Keys.RESULTS);
            V2s.cleanArticles(articles);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
            data = new JSONObject();
            data.put(Article.ARTICLES, articles);

            final int followingArticleCnt = watchingArticlesResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
            final int pageCount = (int) Math.ceil(followingArticleCnt / (double) V2s.PAGE_SIZE);

            final JSONObject pagination = new JSONObject();
            final List<Integer> pageNums = Paginator.paginate(page, V2s.PAGE_SIZE, pageCount, V2s.WINDOW_SIZE);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

            data.put(Pagination.PAGINATION, pagination);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets a user's watching articles failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }

    /**
     * Gets a user's comments.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param userName the specified username
     */
    @RequestProcessing(value = {"/api/v2/user/{userName}/comments"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getUserComments(final HTTPRequestContext context, final HttpServletRequest request, final String userName) {
        int page = 1;
        final String p = request.getParameter("p");
        if (Strings.isNumeric(p)) {
            page = Integer.parseInt(p);
        }

        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        if (UserRegisterValidation.invalidUserName(userName)) {
            ret.put(Keys.MSG, "User not found");
            ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

            return;
        }

        JSONObject data = null;
        try {
            final JSONObject user = userQueryService.getUserByName(userName);
            if (null == user) {
                ret.put(Keys.MSG, "User not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
            final JSONObject currentUser = userQueryService.getCurrentUser(request);

            final List<JSONObject> comments = commentQueryService.getUserComments(avatarViewMode,
                    user.optString(Keys.OBJECT_ID), Comment.COMMENT_ANONYMOUS_C_PUBLIC, page, V2s.PAGE_SIZE, currentUser);
            V2s.cleanComments(comments);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
            data = new JSONObject();
            data.put(Comment.COMMENTS, comments);

            final int commentCnt = user.optInt(UserExt.USER_COMMENT_COUNT);
            final int pageCount = (int) Math.ceil((double) commentCnt / (double) V2s.PAGE_SIZE);

            final JSONObject pagination = new JSONObject();
            final List<Integer> pageNums = Paginator.paginate(page, V2s.PAGE_SIZE, pageCount, V2s.WINDOW_SIZE);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

            data.put(Pagination.PAGINATION, pagination);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets a user's comments failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }

    /**
     * Gets a user's articles.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param userName the specified username
     */
    @RequestProcessing(value = {"/api/v2/user/{userName}/articles"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getUserArticles(final HTTPRequestContext context, final HttpServletRequest request, final String userName) {
        int page = 1;
        final String p = request.getParameter("p");
        if (Strings.isNumeric(p)) {
            page = Integer.parseInt(p);
        }

        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        if (UserRegisterValidation.invalidUserName(userName)) {
            ret.put(Keys.MSG, "User not found");
            ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

            return;
        }

        JSONObject data = null;
        try {
            final JSONObject user = userQueryService.getUserByName(userName);
            if (null == user) {
                ret.put(Keys.MSG, "User not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

            final List<JSONObject> articles = articleQueryService.getUserArticles(avatarViewMode,
                    user.optString(Keys.OBJECT_ID), Article.ARTICLE_ANONYMOUS_C_PUBLIC, page, V2s.PAGE_SIZE);
            V2s.cleanArticles(articles);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
            data = new JSONObject();
            data.put(Article.ARTICLES, articles);

            final int articleCnt = user.optInt(UserExt.USER_ARTICLE_COUNT);
            final int pageCount = (int) Math.ceil((double) articleCnt / (double) V2s.PAGE_SIZE);

            final JSONObject pagination = new JSONObject();
            final List<Integer> pageNums = Paginator.paginate(page, V2s.PAGE_SIZE, pageCount, V2s.WINDOW_SIZE);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

            data.put(Pagination.PAGINATION, pagination);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets a user's articles failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }

    /**
     * Gets a user by the specified username.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param userName the specified username
     */
    @RequestProcessing(value = {"/api/v2/user/{userName}"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getUser(final HTTPRequestContext context, final HttpServletRequest request, final String userName) {
        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        if (UserRegisterValidation.invalidUserName(userName)) {
            ret.put(Keys.MSG, "User not found");
            ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

            return;
        }

        JSONObject data = null;
        try {
            final JSONObject user = userQueryService.getUserByName(userName);
            if (null == user) {
                ret.put(Keys.MSG, "User not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
            avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

            data = new JSONObject();
            data.put(User.USER, user);
            V2s.cleanUser(user);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets a user failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }
}
