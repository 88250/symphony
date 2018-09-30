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

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Times;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.processor.advice.AnonymousViewCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Data statistic processor.
 * <ul>
 * <li>Shows data statistic (/statistic), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.2.1.1, Jul 27, 2018
 * @since 1.4.0
 */
@RequestProcessor
public class StatisticProcessor {

    /**
     * Month days.
     */
    private final List<String> monthDays = new ArrayList<>();

    /**
     * User counts.
     */
    private final List<Integer> userCnts = new ArrayList<>();

    /**
     * Article counts.
     */
    private final List<Integer> articleCnts = new ArrayList<>();

    /**
     * Comment counts.
     */
    private final List<Integer> commentCnts = new ArrayList<>();

    /**
     * History months.
     */
    private final List<String> months = new ArrayList<>();

    /**
     * History user counts.
     */
    private final List<Integer> historyUserCnts = new ArrayList<>();

    /**
     * History article counts.
     */
    private final List<Integer> historyArticleCnts = new ArrayList<>();

    /**
     * History comment counts.
     */
    private final List<Integer> historyCommentCnts = new ArrayList<>();

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Visit management service.
     */
    @Inject
    private VisitMgmtService visitMgmtService;

    /**
     * Loads statistic data.
     *
     * @param request  the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @param context  the specified HTTP request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/cron/stat", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void loadStatData(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final Date end = new Date();
        final Date dayStart = DateUtils.addDays(end, -30);

        monthDays.clear();
        userCnts.clear();
        articleCnts.clear();
        commentCnts.clear();
        months.clear();
        historyArticleCnts.clear();
        historyCommentCnts.clear();
        historyUserCnts.clear();

        for (int i = 0; i < 31; i++) {
            final Date day = DateUtils.addDays(dayStart, i);
            monthDays.add(DateFormatUtils.format(day, "yyyy-MM-dd"));

            final int userCnt = userQueryService.getUserCntInDay(day);
            userCnts.add(userCnt);

            final int articleCnt = articleQueryService.getArticleCntInDay(day);
            articleCnts.add(articleCnt);

            final int commentCnt = commentQueryService.getCommentCntInDay(day);
            commentCnts.add(commentCnt);
        }

        final JSONObject firstAdmin = userQueryService.getAdmins().get(0);
        final long monthStartTime = Times.getMonthStartTime(firstAdmin.optLong(Keys.OBJECT_ID));
        final Date monthStart = new Date(monthStartTime);

        int i = 1;
        while (true) {
            final Date month = DateUtils.addMonths(monthStart, i);

            if (month.after(end)) {
                break;
            }

            i++;

            months.add(DateFormatUtils.format(month, "yyyy-MM"));

            final int userCnt = userQueryService.getUserCntInMonth(month);
            historyUserCnts.add(userCnt);

            final int articleCnt = articleQueryService.getArticleCntInMonth(month);
            historyArticleCnts.add(articleCnt);

            final int commentCnt = commentQueryService.getCommentCntInMonth(month);
            historyCommentCnts.add(commentCnt);
        }

        visitMgmtService.expire();

        context.renderJSON().renderTrueResult();
    }

    /**
     * Shows data statistic.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/statistic", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showStatistic(final HTTPRequestContext context,
                              final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("statistic.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModel.put("monthDays", monthDays);
        dataModel.put("userCnts", userCnts);
        dataModel.put("articleCnts", articleCnts);
        dataModel.put("commentCnts", commentCnts);

        dataModel.put("months", months);
        dataModel.put("historyUserCnts", historyUserCnts);
        dataModel.put("historyArticleCnts", historyArticleCnts);
        dataModel.put("historyCommentCnts", historyCommentCnts);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        dataModel.put(Common.ONLINE_VISITOR_CNT, optionQueryService.getOnlineVisitorCount());
        dataModel.put(Common.ONLINE_MEMBER_CNT, optionQueryService.getOnlineMemberCount());

        final JSONObject statistic = optionQueryService.getStatistic();
        dataModel.put(Option.CATEGORY_C_STATISTIC, statistic);
    }
}
