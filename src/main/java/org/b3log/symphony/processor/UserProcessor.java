/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
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
import org.b3log.latke.Latkes;
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
import org.b3log.latke.util.MD5;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Follow;
import org.b3log.symphony.model.UserExt;
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
 * <li>User Home (/member/{userName}), GET</li>
 * <li>Settings (/settings), GET</li>
 * <li>Profiles (/settings/profiles), POST</li>
 * <li>Sync (/settings/sync/b3), POST</li>
 * <li>Password (/settings/password), POST</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.6, May 30, 2013
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
    public void showHome(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                         final String userName) throws Exception {
        final JSONObject user = userQueryService.getUserByName(userName);
        if (null == user) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);

        final Map<String, Object> dataModel = renderer.getDataModel();
        filler.fillHeader(request, response, dataModel);
        filler.fillFooter(dataModel);

        if (UserExt.USER_STATUS_C_INVALID == user.optInt(UserExt.USER_STATUS)) {
            renderer.setTemplateName("/home/block.ftl");

            return;
        }

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        renderer.setTemplateName("/home/home.ftl");

        fillUserThumbnailURL(user);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(User.USER);
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
    public void showHomeComments(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                                 final String userName) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/comments.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = userQueryService.getUserByName(userName);
        if (null == user) {
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

        dataModel.put(User.USER, user);
        fillUserThumbnailURL(user);

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

        filler.fillHeader(request, response, dataModel);
        filler.fillFooter(dataModel);
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
    public void showSettings(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/settings.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = userQueryService.getCurrentUser(request);

        dataModel.put(User.USER, user);

        fillUserThumbnailURL(user);

        filler.fillHeader(request, response, dataModel);
        filler.fillFooter(dataModel);
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
    @Before(adviceClass = UpdateProfilesValidation.class)
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
    @Before(adviceClass = UpdateSyncB3Validation.class)
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
    @Before(adviceClass = UpdatePasswordValidation.class)
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
     * Fills the specified user thumbnail URL.
     *
     * @param user the specified user
     */
    private void fillUserThumbnailURL(final JSONObject user) {
        final String userEmail = user.optString(User.USER_EMAIL);
        final String thumbnailURL = "http://secure.gravatar.com/avatar/" + MD5.hash(userEmail) + "?s=140&d="
                                    + Latkes.getStaticServePath() + "/images/user-thumbnail.png";
        user.put(UserExt.USER_T_THUMBNAIL_URL, thumbnailURL);
    }
}
