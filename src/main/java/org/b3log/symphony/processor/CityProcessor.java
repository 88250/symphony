/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Paginator;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * City processor.
 * <ul>
 * <li>Shows city articles (/city/{city}), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.3.1.10, Aug 9, 2018
 * @since 1.3.0
 */
@RequestProcessor
public class CityProcessor {

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langService;

    /**
     * Shows city articles.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param city     the specified city
     * @throws Exception exception
     */
    @RequestProcessing(value = {"/city/{city}", "/city/{city}/articles"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showCityArticles(final HTTPRequestContext context,
                                 final HttpServletRequest request, final HttpServletResponse response, final String city) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);

        renderer.setTemplateName("city.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        dataModel.put(Common.CURRENT, "");

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        List<JSONObject> articles = new ArrayList<>();
        dataModel.put(Article.ARTICLES, articles); // an empty list to avoid null check in template
        dataModel.put(Common.SELECTED, Common.CITY);

        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (!UserExt.finshedGuide(user)) {
            response.sendRedirect(Latkes.getServePath() + "/guide");

            return;
        }

        dataModel.put(UserExt.USER_GEO_STATUS, true);
        dataModel.put(Common.CITY_FOUND, true);
        dataModel.put(Common.CITY, langService.get("sameCityLabel"));

        if (UserExt.USER_GEO_STATUS_C_PUBLIC != user.optInt(UserExt.USER_GEO_STATUS)) {
            dataModel.put(UserExt.USER_GEO_STATUS, false);

            return;
        }

        final String userCity = user.optString(UserExt.USER_CITY);

        String queryCity = city;
        if ("my".equals(city)) {
            dataModel.put(Common.CITY, userCity);
            queryCity = userCity;
        } else {
            dataModel.put(Common.CITY, city);
        }

        if (StringUtils.isBlank(userCity)) {
            dataModel.put(Common.CITY_FOUND, false);

            return;
        }

        final int pageNum = Paginator.getPage(request);
        final int pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);
        final int windowSize = Symphonys.getInt("cityArticlesWindowSize");

        final JSONObject statistic = optionQueryService.getOption(queryCity + "-ArticleCount");
        if (null != statistic) {
            articles = articleQueryService.getArticlesByCity(avatarViewMode, queryCity, pageNum, pageSize);
            dataModel.put(Article.ARTICLES, articles);
        }

        final int articleCnt = null == statistic ? 0 : statistic.optInt(Option.OPTION_VALUE);
        final int pageCount = (int) Math.ceil(articleCnt / (double) pageSize);

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
     * Shows city users.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param city     the specified city
     * @throws Exception exception
     */
    @RequestProcessing(value = {"/city/{city}/users"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showCityUsers(final HTTPRequestContext context,
                              final HttpServletRequest request, final HttpServletResponse response, final String city) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);

        renderer.setTemplateName("city.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        dataModel.put(Common.CURRENT, "/users");

        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        List<JSONObject> users = new ArrayList<>();
        dataModel.put(User.USERS, users);
        dataModel.put(Common.SELECTED, Common.CITY);

        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (!UserExt.finshedGuide(user)) {
            response.sendRedirect(Latkes.getServePath() + "/guide");

            return;
        }

        dataModel.put(UserExt.USER_GEO_STATUS, true);
        dataModel.put(Common.CITY_FOUND, true);
        dataModel.put(Common.CITY, langService.get("sameCityLabel"));
        if (UserExt.USER_GEO_STATUS_C_PUBLIC != user.optInt(UserExt.USER_GEO_STATUS)) {
            dataModel.put(UserExt.USER_GEO_STATUS, false);

            return;
        }

        final String userCity = user.optString(UserExt.USER_CITY);

        String queryCity = city;
        if ("my".equals(city)) {
            dataModel.put(Common.CITY, userCity);
            queryCity = userCity;
        } else {
            dataModel.put(Common.CITY, city);
        }

        if (StringUtils.isBlank(userCity)) {
            dataModel.put(Common.CITY_FOUND, false);

            return;
        }

        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("cityUserPageSize");
        final int windowSize = Symphonys.getInt("cityUsersWindowSize");

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Keys.OBJECT_ID, user.optString(Keys.OBJECT_ID));
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);
        final long latestLoginTime = DateUtils.addDays(new Date(), Integer.MIN_VALUE).getTime(); // all users
        requestJSONObject.put(UserExt.USER_LATEST_LOGIN_TIME, latestLoginTime);
        requestJSONObject.put(UserExt.USER_CITY, queryCity);
        final JSONObject result = userQueryService.getUsersByCity(requestJSONObject);
        final JSONArray cityUsers = result.optJSONArray(User.USERS);
        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        if (null != cityUsers && cityUsers.length() > 0) {
            for (int i = 0; i < cityUsers.length(); i++) {
                users.add(cityUsers.getJSONObject(i));
            }
            dataModel.put(User.USERS, users);
        }

        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
    }
}
