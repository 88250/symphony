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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.Follow;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.service.FollowMgmtService;
import org.b3log.symphony.util.QueryResults;
import org.json.JSONObject;

/**
 * Follow processor.
 *
 * <ul>
 * <li>Follows a user (/follow/user), POST</li>
 * <li>Unfollows a user (/follow/user), DELETE</li>
 * <li>Follows a tag (/follow/tag), POST</li>
 * <li>Unfollows a tag (/follow/tag), DELETE</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 11, 2013
 * @since 0.2.5
 */
@RequestProcessor
public class FollowProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FollowProcessor.class.getName());

    /**
     * Follow management service.
     */
    @Inject
    private FollowMgmtService followMgmtService;

    /**
     * Follows a user.
     *
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/follow/user", method = HTTPRequestMethod.POST)
    @Before(adviceClass = LoginCheck.class)
    public void followUser(final HTTPRequestContext context, final HttpServletRequest request,
                           final HttpServletResponse response) throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String followingUserId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.followUser(followerUserId, followingUserId);

        ret.put(Keys.STATUS_CODE, true);
    }

    /**
     * Unfollows a user.
     *
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/follow/user", method = HTTPRequestMethod.DELETE)
    @Before(adviceClass = LoginCheck.class)
    public void unfollowUser(final HTTPRequestContext context, final HttpServletRequest request,
                             final HttpServletResponse response) throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String followingUserId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.removeFollow(followerUserId, followingUserId);

        ret.put(Keys.STATUS_CODE, true);
    }

    /**
     * Follows a tag.
     *
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/follow/tag", method = HTTPRequestMethod.POST)
    @Before(adviceClass = LoginCheck.class)
    public void followTag(final HTTPRequestContext context, final HttpServletRequest request,
                          final HttpServletResponse response) throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String followingTagId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.followTag(followerUserId, followingTagId);

        ret.put(Keys.STATUS_CODE, true);
    }

    /**
     * Unfollows a tag.
     *
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/follow/tag", method = HTTPRequestMethod.DELETE)
    @Before(adviceClass = LoginCheck.class)
    public void unfollowTag(final HTTPRequestContext context, final HttpServletRequest request,
                            final HttpServletResponse response) throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String followingTagId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.removeFollow(followerUserId, followingTagId);

        ret.put(Keys.STATUS_CODE, true);
    }
}
