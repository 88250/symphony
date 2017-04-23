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
import org.b3log.symphony.model.Domain;
import org.b3log.symphony.processor.advice.AnonymousViewCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.DomainQueryService;
import org.b3log.symphony.util.StatusCodes;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain API v2.
 * <p>
 * <ul>
 * <li>Gets domains (/api/v2/domains), GET</li>
 * <li>Gets a domain (/api/v2/domain/{domainURI}), GET</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Mar 8, 2016
 * @since 2.0.0
 */
@RequestProcessor
public class DomainAPI2 {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DomainAPI2.class);
    /**
     * Domain query service.
     */
    @Inject
    private DomainQueryService domainQueryService;

    /**
     * Gets a domain.
     *
     * @param context   the specified context
     * @param request   the specified request
     * @param domainURI the specified domain URI
     */
    @RequestProcessing(value = {"/api/v2/domain/{domainURI}"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getDomain(final HTTPRequestContext context, final HttpServletRequest request, final String domainURI) {
        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        JSONObject data = null;
        try {
            final JSONObject domain = domainQueryService.getByURI(domainURI);
            if (null == domain) {
                ret.put(Keys.MSG, "Domain not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            final List<JSONObject> tags = domainQueryService.getTags(domain.optString(Keys.OBJECT_ID));
            domain.put(Domain.DOMAIN_T_TAGS, (Object) tags);

            V2s.cleanDomain(domain);

            data = new JSONObject();
            data.put(Domain.DOMAIN, domain);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets a domain failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }

    /**
     * Gets domains.
     *
     * @param context the specified context
     * @param request the specified request
     */
    @RequestProcessing(value = {"/api/v2/domains"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getDomains(final HTTPRequestContext context, final HttpServletRequest request) {
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

            final String domainTitle = request.getParameter(Common.TITLE);
            if (!Strings.isEmptyOrNull(domainTitle)) {
                requestJSONObject.put(Domain.DOMAIN_TITLE, domainTitle);
            }

            final Map<String, Class<?>> domainFields = new HashMap<>();
            domainFields.put(Keys.OBJECT_ID, String.class);
            domainFields.put(Domain.DOMAIN_TITLE, String.class);
            domainFields.put(Domain.DOMAIN_DESCRIPTION, String.class);
            domainFields.put(Domain.DOMAIN_ICON_PATH, String.class);
            domainFields.put(Domain.DOMAIN_STATUS, String.class);
            domainFields.put(Domain.DOMAIN_URI, String.class);
            domainFields.put(Domain.DOMAIN_TAG_COUNT, Integer.class);

            final JSONObject result = domainQueryService.getDomains(requestJSONObject, domainFields);
            final List<JSONObject> domains = CollectionUtils.jsonArrayToList(result.optJSONArray(Domain.DOMAINS));
            V2s.cleanDomains(domains);

            data = new JSONObject();
            data.put(Domain.DOMAINS, domains);
            data.put(Pagination.PAGINATION, result.optJSONObject(Pagination.PAGINATION));

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets domains failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }
}
