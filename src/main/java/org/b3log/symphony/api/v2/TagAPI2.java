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
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.processor.advice.AnonymousViewCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.TagQueryService;
import org.b3log.symphony.util.StatusCodes;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tag API v2.
 * <p>
 * <ul>
 * <li>Gets tags (/api/v2/tags), GET</li>
 * <li>Gets a tag (/api/v2/tag/{tagURI}), GET</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.1, Mar 8, 2016
 * @since 2.0.0
 */
@RequestProcessor
public class TagAPI2 {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TagAPI2.class);
    /**
     * Tag query service.
     */
    @Inject
    private TagQueryService tagQueryService;

    /**
     * Gets a tag.
     *
     * @param context the specified context
     * @param request the specified request
     * @param tagURI  the specified tag URI
     */
    @RequestProcessing(value = {"/api/v2/tag/{tagURI}"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getTags(final HTTPRequestContext context, final HttpServletRequest request, final String tagURI) {
        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        JSONObject data = null;
        try {
            final JSONObject tag = tagQueryService.getTagByURI(tagURI);
            if (null == tag) {
                ret.put(Keys.MSG, "Tag not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            data = new JSONObject();
            data.put(Tag.TAG, tag);
            V2s.cleanTag(tag);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets a tag failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }

    /**
     * Gets tags.
     *
     * @param context the specified context
     * @param request the specified request
     */
    @RequestProcessing(value = {"/api/v2/tags"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getTags(final HTTPRequestContext context, final HttpServletRequest request) {
        int page = 1;
        final String p = request.getParameter("p");
        if (Strings.isNumeric(p)) {
            page = Integer.parseInt(p);
        }

        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        JSONObject data = null;
        try {
            final JSONObject requestJSONObject = new JSONObject();
            requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, page);
            requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, V2s.PAGE_SIZE);
            requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, V2s.WINDOW_SIZE);

            final Map<String, Class<?>> tagFields = new HashMap<>();
            tagFields.put(Keys.OBJECT_ID, String.class);
            tagFields.put(Tag.TAG_TITLE, String.class);
            tagFields.put(Tag.TAG_DESCRIPTION, String.class);
            tagFields.put(Tag.TAG_ICON_PATH, String.class);
            tagFields.put(Tag.TAG_COMMENT_CNT, Integer.class);
            tagFields.put(Tag.TAG_REFERENCE_CNT, Integer.class);
            tagFields.put(Tag.TAG_FOLLOWER_CNT, Integer.class);
            tagFields.put(Tag.TAG_STATUS, Integer.class);
            tagFields.put(Tag.TAG_GOOD_CNT, Integer.class);
            tagFields.put(Tag.TAG_BAD_CNT, Integer.class);
            tagFields.put(Tag.TAG_URI, String.class);

            final JSONObject result = tagQueryService.getTags(requestJSONObject, tagFields);

            data = new JSONObject();
            final List<JSONObject> tags = CollectionUtils.jsonArrayToList(result.optJSONArray(Tag.TAGS));
            V2s.cleanTags(tags);

            data.put(Tag.TAGS, tags);
            data.put(Pagination.PAGINATION, result.optJSONObject(Pagination.PAGINATION));

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets tags failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }
}
