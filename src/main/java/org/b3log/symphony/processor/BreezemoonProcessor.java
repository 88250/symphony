/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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
package org.b3log.symphony.processor;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.Breezemoon;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.*;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.BreezemoonMgmtService;
import org.b3log.symphony.service.BreezemoonQueryService;
import org.b3log.symphony.service.DataModelService;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.util.Geos;
import org.b3log.symphony.util.Headers;
import org.b3log.symphony.util.StatusCodes;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Breezemoon processor. https://github.com/b3log/symphony/issues/507
 *
 * <ul>
 * <li>Shows watch breezemoons (/watch/breezemoons), GET</li>
 * <li>Adds a breezemoon (/breezemoon), POST</li>
 * <li>Updates a breezemoon (/breezemoon/{id}), PUT</li>
 * <li>Removes a breezemoon (/breezemoon/{id}), DELETE</li>
 * <li>Shows a breezemoon (/breezemoon/{id}), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.2, Jan 5, 2019
 * @since 2.8.0
 */
@RequestProcessor
public class BreezemoonProcessor {

    /**
     * Breezemoon query service.
     */
    @Inject
    private BreezemoonQueryService breezemoonQueryService;

    /**
     * Breezemoon management service.
     */
    @Inject
    private BreezemoonMgmtService breezemoonMgmtService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Optiona query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Shows breezemoon page.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/watch/breezemoons", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After({CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    public void showWatchBreezemoon(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "breezemoon.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        int pageSize = Symphonys.getInt("indexArticlesCnt");
        final int avatarViewMode = (int) context.attr(UserExt.USER_AVATAR_VIEW_MODE);
        final JSONObject user = (JSONObject) context.attr(Common.CURRENT_USER);
        String currentUserId = null;
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);

            if (!UserExt.finshedGuide(user)) {
                context.sendRedirect(Latkes.getServePath() + "/guide");

                return;
            }

            currentUserId = user.optString(Keys.OBJECT_ID);
        }

        final int windowSize = Symphonys.getInt("latestArticlesWindowSize");
        final JSONObject result = breezemoonQueryService.getFollowingUserBreezemoons(avatarViewMode, currentUserId, pageNum, pageSize, windowSize);
        final List<JSONObject> bms = (List<JSONObject>) result.opt(Breezemoon.BREEZEMOONS);
        dataModel.put(Common.WATCHING_BREEZEMOONS, bms);

        dataModelService.fillHeaderAndFooter(context, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        dataModel.put(Common.SELECTED, Common.WATCH);
        dataModel.put(Common.CURRENT, StringUtils.substringAfter(context.requestURI(), "/watch"));
    }

    /**
     * Adds a breezemoon.
     * <p>
     * The request json object (breezemoon):
     * <pre>
     * {
     *   "breezemoonContent": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/breezemoon", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void addBreezemoon(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        if (isInvalid(context, requestJSONObject)) {
            return;
        }

        final JSONObject breezemoon = new JSONObject();
        final String breezemoonContent = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
        breezemoon.put(Breezemoon.BREEZEMOON_CONTENT, breezemoonContent);
        final JSONObject user = (JSONObject) context.attr(Common.CURRENT_USER);
        final String authorId = user.optString(Keys.OBJECT_ID);
        breezemoon.put(Breezemoon.BREEZEMOON_AUTHOR_ID, authorId);
        final String ip = Requests.getRemoteAddr(request);
        breezemoon.put(Breezemoon.BREEZEMOON_IP, ip);
        final String ua = Headers.getHeader(request, Common.USER_AGENT, "");
        breezemoon.put(Breezemoon.BREEZEMOON_UA, ua);
        final JSONObject address = Geos.getAddress(ip);
        if (null != address) {
            breezemoon.put(Breezemoon.BREEZEMOON_CITY, address.optString(Common.CITY));
        }

        try {
            breezemoonMgmtService.addBreezemoon(breezemoon);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            context.renderMsg(e.getMessage());
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Updates a breezemoon.
     * <p>
     * The request json object (breezemoon):
     * <pre>
     * {
     *   "breezemoonContent": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/breezemoon/{id}", method = HttpMethod.PUT)
    @Before({StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void updateBreezemoon(final RequestContext context) {
        final String id = context.pathVar("id");
        context.renderJSON();
        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        if (isInvalid(context, requestJSONObject)) {
            return;
        }

        try {
            final JSONObject old = breezemoonQueryService.getBreezemoon(id);
            if (null == old) {
                throw new ServiceException(langPropsService.get("queryFailedLabel"));
            }

            final JSONObject user = (JSONObject) context.attr(Common.CURRENT_USER);
            if (!old.optString(Breezemoon.BREEZEMOON_AUTHOR_ID).equals(user.optString(Keys.OBJECT_ID))) {
                throw new ServiceException(langPropsService.get("sc403Label"));
            }

            final JSONObject breezemoon = new JSONObject();
            breezemoon.put(Keys.OBJECT_ID, id);
            final String breezemoonContent = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
            breezemoon.put(Breezemoon.BREEZEMOON_CONTENT, breezemoonContent);
            final String ip = Requests.getRemoteAddr(request);
            breezemoon.put(Breezemoon.BREEZEMOON_IP, ip);
            final String ua = Headers.getHeader(request, Common.USER_AGENT, "");
            breezemoon.put(Breezemoon.BREEZEMOON_UA, ua);

            breezemoonMgmtService.updateBreezemoon(breezemoon);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            context.renderMsg(e.getMessage());
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Removes a breezemoon.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/breezemoon/{id}", method = HttpMethod.DELETE)
    @Before({StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void removeBreezemoon(final RequestContext context) {
        final String id = context.pathVar("id");
        context.renderJSON();

        try {
            final JSONObject breezemoon = breezemoonQueryService.getBreezemoon(id);
            if (null == breezemoon) {
                throw new ServiceException(langPropsService.get("queryFailedLabel"));
            }

            final JSONObject user = (JSONObject) context.attr(Common.CURRENT_USER);
            if (!breezemoon.optString(Breezemoon.BREEZEMOON_AUTHOR_ID).equals(user.optString(Keys.OBJECT_ID))) {
                throw new ServiceException(langPropsService.get("sc403Label"));
            }

            breezemoonMgmtService.removeBreezemoon(id);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            context.renderMsg(e.getMessage());
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    private boolean isInvalid(final RequestContext context, final JSONObject requestJSONObject) {
        String breezemoonContent = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
        breezemoonContent = StringUtils.trim(breezemoonContent);
        final long length = StringUtils.length(breezemoonContent);
        if (1 > length || 512 < length) {
            context.renderMsg(langPropsService.get("breezemoonLengthLabel"));
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);

            return true;
        }

        if (optionQueryService.containReservedWord(breezemoonContent)) {
            context.renderMsg(langPropsService.get("contentContainReservedWordLabel"));
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);

            return true;
        }

        requestJSONObject.put(Breezemoon.BREEZEMOON_CONTENT, breezemoonContent);

        return false;
    }
}
