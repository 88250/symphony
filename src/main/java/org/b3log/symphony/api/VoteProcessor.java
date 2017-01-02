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
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.1, Jul 31, 2016
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
            voteMgmtService.voteUp(userId, id, Vote.DATA_TYPE_C_ARTICLE);
        }

        ret.put(Vote.TYPE, vote);
        ret.put(Keys.STATUS_CODE, true);
    }
}
