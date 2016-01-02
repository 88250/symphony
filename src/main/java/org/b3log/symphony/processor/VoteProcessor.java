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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Vote;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.service.VoteMgmtService;
import org.b3log.symphony.service.VoteQueryService;
import org.json.JSONObject;

/**
 * Vote processor.
 *
 * <ul>
 * <li>Votes up an article (/vote/up/article), POST</li>
 * <li>Votes down an article (/vote/down/article), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.1, Jan 2, 2016
 * @since 1.3.0
 */
@RequestProcessor
public class VoteProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(VoteProcessor.class.getName());

    /**
     * Vote management service.
     */
    @Inject
    private VoteMgmtService voteMgmtService;

    /**
     * Vote query service.
     */
    @Inject
    private VoteQueryService voteQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Votes up an article.
     *
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "dataId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/vote/up/article", method = HTTPRequestMethod.POST)
    @Before(adviceClass = LoginCheck.class)
    public void voteUpArticle(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        if (!Role.ADMIN_ROLE.equals(currentUser.optString(User.USER_ROLE))
                && voteQueryService.isOwn(userId, dataId, Vote.DATA_TYPE_C_ARTICLE)) {
            context.renderFalseResult().renderMsg(langPropsService.get("cantVoteSelfLabel"));

            return;
        }

        final int vote = voteQueryService.isVoted(userId, dataId);
        if (Vote.TYPE_C_UP == vote) {
            voteMgmtService.voteCancel(userId, dataId, Vote.DATA_TYPE_C_ARTICLE);
        } else {
            voteMgmtService.voteUpArticle(userId, dataId);
        }

        context.renderTrueResult().renderJSONValue(Vote.TYPE, vote);
    }

    /**
     * Votes down an article.
     *
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "dataId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/vote/down/article", method = HTTPRequestMethod.POST)
    @Before(adviceClass = LoginCheck.class)
    public void voteDownArticle(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        if (!Role.ADMIN_ROLE.equals(currentUser.optString(User.USER_ROLE))
                && voteQueryService.isOwn(userId, dataId, Vote.DATA_TYPE_C_ARTICLE)) {
            context.renderFalseResult().renderMsg(langPropsService.get("cantVoteSelfLabel"));

            return;
        }

        final int vote = voteQueryService.isVoted(userId, dataId);
        if (Vote.TYPE_C_DOWN == vote) {
            voteMgmtService.voteCancel(userId, dataId, Vote.DATA_TYPE_C_ARTICLE);
        } else {
            voteMgmtService.voteDownArticle(userId, dataId);
        }

        context.renderTrueResult().renderJSONValue(Vote.TYPE, vote);
    }
}
