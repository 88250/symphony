/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
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

import com.qiniu.util.Auth;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
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
import org.b3log.symphony.model.Client;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Follow;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.CSRFCheck;
import org.b3log.symphony.processor.advice.CSRFToken;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.UserBlockCheck;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.PointTransferValidation;
import org.b3log.symphony.processor.advice.validate.UpdatePasswordValidation;
import org.b3log.symphony.processor.advice.validate.UpdateProfilesValidation;
import org.b3log.symphony.processor.advice.validate.UpdateSyncB3Validation;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.FollowQueryService;
import org.b3log.symphony.service.AvatarQueryService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.PointtransferMgmtService;
import org.b3log.symphony.service.PointtransferQueryService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.Results;
import org.b3log.symphony.util.Sessions;
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
 * <li>User following users (/member/{userName}/following/users), GET</li>
 * <li>User following tags (/member/{userName}/following/tags), GET</li>
 * <li>User following articles (/member/{userName}/following/articles), GET</li>
 * <li>User followers (/member/{userName}/followers), GET</li>
 * <li>User points (/member/{userName}/points), GET</li>
 * <li>Settings (/settings), GET</li>
 * <li>Profiles (/settings/profiles), POST</li>
 * <li>Geo status (/settings/geo/status), POST</li>
 * <li>Sync (/settings/sync/b3), POST</li>
 * <li>Password (/settings/password), POST</li>
 * <li>SyncUser (/apis/user), POST</li>
 * <li>Lists usernames (/users/names), GET</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.14.8.16, Apr 26, 2016
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
     * Filler.
     */
    @Inject
    private Filler filler;

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
     * Shows user home page.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, UserBlockCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showHome(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        final Map<String, Object> dataModel = renderer.getDataModel();
        filler.fillHeaderAndFooter(request, response, dataModel);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        renderer.setTemplateName("/home/home.ftl");

        dataModel.put(User.USER, user);
        fillHomeUser(dataModel, user);
        avatarQueryService.fillUserAvatarURL(user);

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

        final JSONObject currentUser = Sessions.currentUser(request);
        if (null == currentUser) {
            dataModel.put(Common.IS_MY_ARTICLE, false);
        } else {
            dataModel.put(Common.IS_MY_ARTICLE, userName.equals(currentUser.optString(User.USER_NAME)));
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
    @Before(adviceClass = {StopwatchStartAdvice.class, UserBlockCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showHomeComments(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
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

        fillHomeUser(dataModel, user);
        avatarQueryService.fillUserAvatarURL(user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        JSONObject currentUser = null;
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final List<JSONObject> userComments = commentQueryService.getUserComments(user.optString(Keys.OBJECT_ID),
                pageNum, pageSize, currentUser);
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
    @Before(adviceClass = {StopwatchStartAdvice.class, UserBlockCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showHomeFollowingUsers(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
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

        fillHomeUser(dataModel, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);
        avatarQueryService.fillUserAvatarURL(user);

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
    @Before(adviceClass = {StopwatchStartAdvice.class, UserBlockCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showHomeFollowingTags(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
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

        fillHomeUser(dataModel, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);
        avatarQueryService.fillUserAvatarURL(user);

        final JSONObject followingTagsResult = followQueryService.getFollowingTags(followingId, pageNum, pageSize);
        final List<JSONObject> followingTags = (List<JSONObject>) followingTagsResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_TAGS, followingTags);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

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
     * Shows user home following articles page.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/following/articles", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, UserBlockCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showHomeFollowingArticles(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/following-articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        filler.fillHeaderAndFooter(request, response, dataModel);

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
        avatarQueryService.fillUserAvatarURL(user);

        final JSONObject followingArticlesResult = followQueryService.getFollowingArticles(followingId, pageNum, pageSize);
        final List<JSONObject> followingArticles = (List<JSONObject>) followingArticlesResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_ARTICLES, followingArticles);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingArticle : followingArticles) {
                final String homeUserFollowingArticleId = followingArticle.optString(Keys.OBJECT_ID);

                followingArticle.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingArticleId));
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
    @Before(adviceClass = {StopwatchStartAdvice.class, UserBlockCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showHomeFollowers(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
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

        fillHomeUser(dataModel, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final JSONObject followerUsersResult = followQueryService.getFollowerUsers(followingId, pageNum, pageSize);
        final List<JSONObject> followerUsers = (List) followerUsersResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWER_USERS, followerUsers);
        avatarQueryService.fillUserAvatarURL(user);

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
     * Shows user home points page.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/member/{userName}/points", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, UserBlockCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showHomePoints(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/points.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        filler.fillHeaderAndFooter(request, response, dataModel);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomePointsCnt");
        final int windowSize = Symphonys.getInt("userHomePointsWindowSize");

        fillHomeUser(dataModel, user);
        avatarQueryService.fillUserAvatarURL(user);

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

            final boolean isFollowing = followQueryService.isFollowing(followerId, user.optString(Keys.OBJECT_ID));
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
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {CSRFToken.class, StopwatchEndAdvice.class})
    public void showSettings(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/settings.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));
        fillHomeUser(dataModel, user);
        avatarQueryService.fillUserAvatarURL(user);

        // Qiniu file upload authenticate
        final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
        final String uploadToken = auth.uploadToken(Symphonys.get("qiniu.bucket"),
                "avatar/" + user.optString(Keys.OBJECT_ID));
        dataModel.put("qiniuUploadToken", uploadToken);
        dataModel.put("qiniuDomain", Symphonys.get("qiniu.domain"));

        if (!Symphonys.getBoolean("qiniu.enabled")) {
            dataModel.put("qiniuUploadToken", "");
        }

        final long imgMaxSize = Symphonys.getLong("upload.img.maxSize");
        dataModel.put("imgMaxSize", imgMaxSize);
        final long fileMaxSize = Symphonys.getLong("upload.file.maxSize");
        dataModel.put("fileMaxSize", fileMaxSize);

        filler.fillHeaderAndFooter(request, response, dataModel);

        String inviteTipLabel = (String) dataModel.get("inviteTipLabel");
        inviteTipLabel = inviteTipLabel.replace("{point}", String.valueOf(Pointtransfer.TRANSFER_SUM_C_INVITE_REGISTER));
        dataModel.put("inviteTipLabel", inviteTipLabel);

        String pointTransferTipLabel = (String) dataModel.get("pointTransferTipLabel");
        pointTransferTipLabel = pointTransferTipLabel.replace("{point}", Symphonys.get("pointTransferMin"));
        dataModel.put("pointTransferTipLabel", pointTransferTipLabel);
    }

    /**
     * Updates user geo status.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/settings/geo/status", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class})
    public void updateGeoStatus(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
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

        int geoStatus = requestJSONObject.optInt(UserExt.USER_GEO_STATUS);
        if (UserExt.USER_GEO_STATUS_C_PRIVATE != geoStatus && UserExt.USER_GEO_STATUS_C_PRIVATE != geoStatus) {
            geoStatus = UserExt.USER_GEO_STATUS_C_PUBLIC;
        }

        final JSONObject user = userQueryService.getCurrentUser(request);

        user.put(UserExt.USER_GEO_STATUS, geoStatus);

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
     * @param context the specified context
     * @param request the specified request
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
        final String userAvatarURL = requestJSONObject.optString(UserExt.USER_AVATAR_URL);
        final boolean userJoinPointRank = requestJSONObject.optBoolean(UserExt.USER_JOIN_POINT_RANK);
        final boolean userJoinUsedPointRank = requestJSONObject.optBoolean(UserExt.USER_JOIN_USED_POINT_RANK);
        final int userCommentViewMode = requestJSONObject.optInt(UserExt.USER_COMMENT_VIEW_MODE);

        final JSONObject user = userQueryService.getCurrentUser(request);

        user.put(UserExt.USER_TAGS, userTags);
        user.put(User.USER_URL, userURL);
        user.put(UserExt.USER_QQ, userQQ);
        user.put(UserExt.USER_INTRO, userIntro.replace("<", "&lt;").replace(">", "&gt"));
        user.put(UserExt.USER_AVATAR_TYPE, UserExt.USER_AVATAR_TYPE_C_UPLOAD);
        user.put(UserExt.USER_JOIN_POINT_RANK,
                userJoinPointRank
                        ? UserExt.USER_JOIN_POINT_RANK_C_JOIN : UserExt.USER_JOIN_POINT_RANK_C_NOT_JOIN);
        user.put(UserExt.USER_JOIN_USED_POINT_RANK,
                userJoinUsedPointRank
                        ? UserExt.USER_JOIN_USED_POINT_RANK_C_JOIN : UserExt.USER_JOIN_USED_POINT_RANK_C_NOT_JOIN);
        user.put(UserExt.USER_COMMENT_VIEW_MODE, userCommentViewMode);

        if (Symphonys.getBoolean("qiniu.enabled")) {
            final String qiniuDomain = Symphonys.get("qiniu.domain");

            if (!StringUtils.startsWith(userAvatarURL, qiniuDomain)) {
                user.put(UserExt.USER_AVATAR_URL, Symphonys.get("defaultThumbnailURL"));
            } else {
                user.put(UserExt.USER_AVATAR_URL, qiniuDomain + "/avatar/" + user.optString(Keys.OBJECT_ID)
                        + "?" + new Date().getTime());
            }
        } else {
            user.put(UserExt.USER_AVATAR_URL, userAvatarURL);
        }

        try {
            userMgmtService.updateProfiles(user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Point transfer.
     *
     * @param context the specified context
     * @param request the specified request
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
                Pointtransfer.TRANSFER_TYPE_C_ACCOUNT2ACCOUNT, amount, toId);
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
     * @param context the specified context
     * @param request the specified request
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
     * @param context the specified context
     * @param request the specified request
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

//        final String maybeIP = StringUtils.substringBetween(clientHost, "://", ":");
//        if (isIPv4(maybeIP)) {
//            LOGGER.log(Level.WARN, "Sync add user[name={0}, host={1}] error, caused by the client host is invalid",
//                    name, clientHost);
//
//            return;
//        }
        JSONObject user = userQueryService.getUserByEmail(email);
        if (null == user) {
            user = new JSONObject();
            user.put(User.USER_NAME, name);
            user.put(User.USER_EMAIL, email);
            user.put(User.USER_PASSWORD, password);
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
                LOGGER.log(Level.ERROR, "Sync add user[name={0}, host={1}] error: " + e.getMessage(), name, clientHost);
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

        user.put(User.USER_NAME, name);
        user.put(User.USER_EMAIL, email);
        user.put(User.USER_PASSWORD, password);
        user.put(UserExt.USER_B3_KEY, clientB3Key);
        user.put(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL, addArticleURL);
        user.put(UserExt.USER_B3_CLIENT_UPDATE_ARTICLE_URL, updateArticleURL);
        user.put(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL, addCommentURL);

        try {
            userMgmtService.updatePassword(user);
            userMgmtService.updateSyncB3(user);

            LOGGER.log(Level.INFO, "Updated a user[name={0}] via Solo[{1}] sync", name, clientHost);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            LOGGER.log(Level.ERROR, "Sync update user[name=" + name + ", host=" + clientHost + "] error", e);
        }
    }

    /**
     * Resets unverified users..
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/users/reset-unverified", method = HTTPRequestMethod.GET)
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
     * @param context the specified context
     * @param request the specified request
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
            final List<JSONObject> userNames = new ArrayList<JSONObject>();

            for (final JSONObject admin : admins) {
                final JSONObject userName = new JSONObject();
                userName.put(User.USER_NAME, admin.optString(User.USER_NAME));

                String avatar = admin.optString(UserExt.USER_AVATAR_URL);
                if (StringUtils.isBlank(avatar)) {
                    avatar = AvatarQueryService.DEFAULT_AVATAR_URL;
                }
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
     * Loads usernames.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/users/load-names", method = HTTPRequestMethod.GET)
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
     * @param user the specified user
     */
    private void fillHomeUser(final Map<String, Object> dataModel, final JSONObject user) {
        dataModel.put(User.USER, user);
    }
}
