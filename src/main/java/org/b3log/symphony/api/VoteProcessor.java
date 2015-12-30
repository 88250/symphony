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
package org.b3log.symphony.api;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.symphony.model.Vote;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.service.VoteMgmtService;
import org.b3log.symphony.service.VoteQueryService;
import org.b3log.symphony.util.Results;
import org.json.JSONObject;

/**
 * Vote processor.
 *
 * <ul>
 * <li>Votes up an article (/api/v1/stories/{id}/upvote), POST</li>
 * <li>Votes down an article (/api/v1/stories/{id}/downvote), POST</li>
 * </ul>
 *
 * @author <a href="http://wdx.me">DX</a>
 * @version 1.1.0.0, Aug 15, 2015
 * @since 1.3.0
 */
@RequestProcessor
public class VoteProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(org.b3log.symphony.api.VoteProcessor.class.getName());

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

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
     * @param id data id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/api/v1/stories/{id}/upvote", method = HTTPRequestMethod.POST)
    public void voteUpArticle(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response, final String id) throws Exception {
        final String auth = request.getHeader("Authorization");
        if (auth == null) {//TODO validate
            return;
        }
        final String email = new JSONObject(auth.substring("Bearer ".length())).optString("userEmail");

        final JSONObject currentUser = userQueryService.getUserByEmail(email);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = Results.falseResult();
        renderer.setJSONObject(ret);

        if (voteQueryService.isOwn(userId, id, Vote.DATA_TYPE_C_ARTICLE)) {
            ret.put(Keys.STATUS_CODE, false);
            ret.put(Keys.MSG, langPropsService.get("cantVoteSelfLabel"));

            return;
        }

        final int vote = voteQueryService.isVoted(userId, id);
        if (Vote.TYPE_C_UP == vote) {
            voteMgmtService.voteCancel(userId, id, Vote.DATA_TYPE_C_ARTICLE);
        } else {
            voteMgmtService.voteUpArticle(userId, id);
        }

        ret.put(Vote.TYPE, vote);
        ret.put(Keys.STATUS_CODE, true);
    }
}
