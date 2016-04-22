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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.Times;
import org.json.JSONObject;

/**
 * Data statistic processor.
 *
 * <ul>
 * <li>Shows data statistic (/statistic), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Apr 21, 2016
 * @since 1.4.0
 */
@RequestProcessor
public class StatisticProcessor {

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
     * Filler.
     */
    @Inject
    private Filler filler;

    /**
     * Shows data statistic.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/statistic", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showStatistic(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("statistic.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final Date end = new Date();
        final Date dayStart = DateUtils.addDays(end, -30);

        final List<String> monthDays = new ArrayList<String>();
        dataModel.put("monthDays", monthDays);
        final List<Integer> userCnts = new ArrayList<Integer>();
        dataModel.put("userCnts", userCnts);
        final List<Integer> articleCnts = new ArrayList<Integer>();
        dataModel.put("articleCnts", articleCnts);
        final List<Integer> commentCnts = new ArrayList<Integer>();
        dataModel.put("commentCnts", commentCnts);

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

        final List<String> months = new ArrayList<String>();
        dataModel.put("months", months);
        final List<Integer> historyUserCnts = new ArrayList<Integer>();
        dataModel.put("historyUserCnts", historyUserCnts);
        final List<Integer> historyArticleCnts = new ArrayList<Integer>();
        dataModel.put("historyArticleCnts", historyArticleCnts);
        final List<Integer> historyCommentCnts = new ArrayList<Integer>();
        dataModel.put("historyCommentCnts", historyCommentCnts);

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

        filler.fillHeaderAndFooter(request, response, dataModel);
        filler.fillRandomArticles(dataModel);
        filler.fillHotArticles(dataModel);
        filler.fillSideTags(dataModel);
        filler.fillLatestCmts(dataModel);
    }
}
