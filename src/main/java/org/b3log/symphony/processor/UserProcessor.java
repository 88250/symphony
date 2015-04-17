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
package org.b3log.symphony.processor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.servlet.renderer.freemarker.FreeMarkerRenderer;
import org.b3log.latke.user.GeneralUser;
import org.b3log.latke.user.UserService;
import org.b3log.latke.user.UserServiceFactory;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Client;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Follow;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.UserBlockCheck;
import org.b3log.symphony.processor.advice.validate.UpdatePasswordValidation;
import org.b3log.symphony.processor.advice.validate.UpdateProfilesValidation;
import org.b3log.symphony.processor.advice.validate.UpdateSyncB3Validation;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.FollowQueryService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.QueryResults;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * User processor.
 *
 * <p>
 * For user
 * <ul>
 * <li>User articles (/member/{userName}), GET</li>
 * <li>User comments (/member/{userName}/comments), GET</li>
 * <li>User comments (/member/{userName}/following/users), GET</li>
 * <li>User comments (/member/{userName}/following/tags), GET</li>
 * <li>User comments (/member/{userName}/followers), GET</li>
 * <li>Settings (/settings), GET</li>
 * <li>Profiles (/settings/profiles), POST</li>
 * <li>Sync (/settings/sync/b3), POST</li>
 * <li>Password (/settings/password), POST</li>
 * <li>SyncUser (/apis/user), POST</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.1.8, Apr 17, 2015
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
     * User service.
     */
    private UserService userService = UserServiceFactory.getUserService();

    /**
     * Filler.
     */
    @Inject
    private Filler filler;

    /**
     * Shows user home page.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = UserBlockCheck.class)
    public void showHome(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                         final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        final Map<String, Object> dataModel = renderer.getDataModel();
        filler.fillHeaderAndFooter(request, response, dataModel);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        renderer.setTemplateName("/home/home.ftl");

        dataModel.put(User.USER, user);
        filler.fillUserThumbnailURL(user);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int pageSize = Symphonys.getInt("userHomeArticlesCnt");
        final int windowSize = Symphonys.getInt("userHomeArticlesWindowSize");

        final List<JSONObject> userArticles = articleQueryService.getUserArticles(user.optString(Keys.OBJECT_ID), pageNum, pageSize);
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

        final GeneralUser currentUser = userService.getCurrentUser(request);
        if (null == currentUser) {
            dataModel.put(Common.IS_MY_ARTICLE, false);
        } else {
            dataModel.put(Common.IS_MY_ARTICLE, userName.equals(currentUser.getNickname()));
        }
    }

    /**
     * Shows user home comments page.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/comments", method = HTTPRequestMethod.GET)
    @Before(adviceClass = UserBlockCheck.class)
    public void showHomeComments(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                                 final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/comments.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        filler.fillHeaderAndFooter(request, response, dataModel);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomeCmtsCnt");
        final int windowSize = Symphonys.getInt("userHomeCmtsWindowSize");

        dataModel.put(User.USER, user);
        filler.fillUserThumbnailURL(user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final List<JSONObject> userComments = commentQueryService.getUserComments(user.optString(Keys.OBJECT_ID), pageNum, pageSize);
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
    }

    /**
     * Shows user home following users page.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/following/users", method = HTTPRequestMethod.GET)
    @Before(adviceClass = UserBlockCheck.class)
    public void showHomeFollowingUsers(final HTTPRequestContext context, final HttpServletRequest request,
                                       final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/following-users.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        filler.fillHeaderAndFooter(request, response, dataModel);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomeFollowingUsersCnt");
        final int windowSize = Symphonys.getInt("userHomeFollowingUsersWindowSize");

        dataModel.put(User.USER, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);
        filler.fillUserThumbnailURL(user);

        final JSONObject followingUsersResult = followQueryService.getFollowingUsers(followingId, pageNum, pageSize);
        final List<JSONObject> followingUsers = (List<JSONObject>) followingUsersResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_USERS, followingUsers);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingUser : followingUsers) {
                final String homeUserFollowingUserId = followingUser.optString(Keys.OBJECT_ID);

                followingUser.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingUserId));
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
    }
    
     /**
     * Shows user home following tags page.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/following/tags", method = HTTPRequestMethod.GET)
    @Before(adviceClass = UserBlockCheck.class)
    public void showHomeFollowingTags(final HTTPRequestContext context, final HttpServletRequest request,
                                       final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/following-tags.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        filler.fillHeaderAndFooter(request, response, dataModel);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomeFollowingTagsCnt");
        final int windowSize = Symphonys.getInt("userHomeFollowingTagsWindowSize");

        dataModel.put(User.USER, user);
        filler.fillUserThumbnailURL(user);

        final String followerId = user.optString(Keys.OBJECT_ID);

        final JSONObject followingTagsResult = followQueryService.getFollowingTags(followerId, pageNum, pageSize);
        final List<JSONObject> followingTags = (List<JSONObject>) followingTagsResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_TAGS, followingTags);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            for (final JSONObject followingTag : followingTags) {
                final String homeUserFollowingTagId = followingTag.optString(Keys.OBJECT_ID);

                followingTag.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingTagId));
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
    }


    /**
     * Shows user home follower users page.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/followers", method = HTTPRequestMethod.GET)
    @Before(adviceClass = UserBlockCheck.class)
    public void showHomeFollowers(final HTTPRequestContext context, final HttpServletRequest request,
                                  final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/followers.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        filler.fillHeaderAndFooter(request, response, dataModel);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomeFollowersCnt");
        final int windowSize = Symphonys.getInt("userHomeFollowersWindowSize");

        dataModel.put(User.USER, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final JSONObject followerUsersResult = followQueryService.getFollowerUsers(followingId, pageNum, pageSize);
        final List<JSONObject> followerUsers = (List) followerUsersResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWER_USERS, followerUsers);
        filler.fillUserThumbnailURL(user);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followerUser : followerUsers) {
                final String homeUserFollowerUserId = followerUser.optString(Keys.OBJECT_ID);

                followerUser.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowerUserId));
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
    }

    /**
     * Shows settings page.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/settings", method = HTTPRequestMethod.GET)
    @Before(adviceClass = LoginCheck.class)
    public void showSettings(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/settings.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));
        dataModel.put(User.USER, user);
        filler.fillUserThumbnailURL(user);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates user profiles.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/settings/profiles", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, UpdateProfilesValidation.class})
    public void updateProfiles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String userURL = requestJSONObject.optString(User.USER_URL);
        final String userQQ = requestJSONObject.optString(UserExt.USER_QQ);
        final String userIntro = requestJSONObject.optString(UserExt.USER_INTRO);

        final JSONObject user = userQueryService.getCurrentUser(request);
        user.put(User.USER_URL, userURL);
        user.put(UserExt.USER_QQ, userQQ);
        user.put(UserExt.USER_INTRO, userIntro.replace("<", "&lt;").replace(">", "&gt"));

        try {
            userMgmtService.updateProfiles(user);
            ret.put(Keys.STATUS_CODE, true);
        } catch (final ServiceException e) {
            ret.put(Keys.MSG, e.getMessage());
        }
    }

    /**
     * Updates user B3log sync.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/settings/sync/b3", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, UpdateSyncB3Validation.class})
    public void updateSyncB3(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String b3Key = requestJSONObject.optString(UserExt.USER_B3_KEY);
        final String addArticleURL = requestJSONObject.optString(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL);
        final String updateArticleURL = requestJSONObject.optString(UserExt.USER_B3_CLIENT_UPDATE_ARTICLE_URL);
        final String addCommentURL = requestJSONObject.optString(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL);

        final JSONObject user = userQueryService.getCurrentUser(request);
        user.put(UserExt.USER_B3_KEY, b3Key);
        user.put(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL, addArticleURL);
        user.put(UserExt.USER_B3_CLIENT_UPDATE_ARTICLE_URL, updateArticleURL);
        user.put(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL, addCommentURL);

        try {
            userMgmtService.updateSyncB3(user);
            ret.put(Keys.STATUS_CODE, true);
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg, e);

            ret.put(Keys.MSG, msg);
        }
    }

    /**
     * Updates user password.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/settings/password", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, UpdatePasswordValidation.class})
    public void updatePassword(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String password = requestJSONObject.optString(User.USER_PASSWORD);
        final String newPassword = requestJSONObject.optString(User.USER_NEW_PASSWORD);

        final JSONObject user = userQueryService.getCurrentUser(request);

        if (!password.equals(user.optString(User.USER_PASSWORD))) {
            ret.put(Keys.MSG, langPropsService.get("invalidOldPwdLabel"));

            return;
        }

        user.put(User.USER_PASSWORD, newPassword);

        try {
            userMgmtService.updatePassword(user);
            ret.put(Keys.STATUS_CODE, true);
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg, e);

            ret.put(Keys.MSG, msg);
        }
    }

    /**
     * Sync user. Experimental API.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/apis/user", method = HTTPRequestMethod.POST)
    public void syncUser(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);

        final String name = requestJSONObject.optString(User.USER_NAME);
        final String email = requestJSONObject.optString(User.USER_EMAIL);
        final String password = requestJSONObject.optString(User.USER_PASSWORD);
        final String clientHost = requestJSONObject.optString(Client.CLIENT_HOST);
        final String b3Key = requestJSONObject.optString(UserExt.USER_B3_KEY);
        final String addArticleURL = clientHost + "/apis/symphony/article";
        final String updateArticleURL = clientHost + "/apis/symphony/article";
        final String addCommentURL = clientHost + "/apis/symphony/comment";

        JSONObject user = userQueryService.getUserByEmail(email);
        if (null == user) {
            user = new JSONObject();
            user.put(User.USER_NAME, name);
            user.put(User.USER_EMAIL, email);
            user.put(User.USER_PASSWORD, password);
            user.put(UserExt.USER_B3_KEY, b3Key);
            user.put(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL, addArticleURL);
            user.put(UserExt.USER_B3_CLIENT_UPDATE_ARTICLE_URL, updateArticleURL);
            user.put(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL, addCommentURL);

            try {
                final String id = userMgmtService.addUser(user);
                user.put(Keys.OBJECT_ID, id);

                userMgmtService.updateSyncB3(user);

                LOGGER.log(Level.INFO, "Added a user[{0}] via Solo[{1}] sync", name, clientHost);

                ret.put(Keys.STATUS_CODE, true);
            } catch (final ServiceException e) {
                LOGGER.log(Level.ERROR, "Sync add user[" + name + "] error", e);
            }

            return;
        }

        if (!user.optString(UserExt.USER_B3_KEY).equals(b3Key)) {
            LOGGER.log(Level.WARN, "Sync update user[name={0}, host={1}] B3Key dismatch [sym={2}, solo={3}]",
                       name, clientHost, user.optString(UserExt.USER_B3_KEY), b3Key);

            return;
        }

        user.put(User.USER_NAME, name);
        user.put(User.USER_EMAIL, email);
        user.put(User.USER_PASSWORD, password);
        user.put(UserExt.USER_B3_KEY, b3Key);
        user.put(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL, addArticleURL);
        user.put(UserExt.USER_B3_CLIENT_UPDATE_ARTICLE_URL, updateArticleURL);
        user.put(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL, addCommentURL);

        try {
            userMgmtService.updatePassword(user);
            userMgmtService.updateSyncB3(user);

            LOGGER.log(Level.INFO, "Updated a user[name={0}] via Solo[{1}] sync", name, clientHost);

            ret.put(Keys.STATUS_CODE, true);
        } catch (final ServiceException e) {
            LOGGER.log(Level.ERROR, "Sync update user[name=" + name + ", host=" + clientHost + "] error", e);
        }
    }
}
