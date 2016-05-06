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

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.cache.DomainCache;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Domain;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.DomainQueryService;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Domain processor.
 *
 * <ul>
 * <li>Shows domains (/domains), GET</li>
 * <li>Shows domain article (/{domainURI}), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, May 6, 2016
 * @since 1.4.0
 */
@RequestProcessor
public class DomainProcessor {

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Domain query service.
     */
    @Inject
    private DomainQueryService domainQueryService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Filler.
     */
    @Inject
    private Filler filler;

    /**
     * Domain cache.
     */
    @Inject
    private DomainCache domainCache;

    /**
     * Caches domains.
     *
     * @param request the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @param context the specified HTTP request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/cron/domain/cache-domains", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void cacheDomains(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        domainCache.loadDomains();

        context.renderJSON().renderTrueResult();
    }

    /**
     * Shows domain articles.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param domainURI the specified domain URI
     * @throws Exception exception
     */
    @RequestProcessing(value = "/domain/{domainURI}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showDomainArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String domainURI)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("domain-articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Symphonys.getInt("latestArticlesCnt");

        final JSONObject domain = domainQueryService.getByURI(domainURI);
        if (null == domain) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final List<JSONObject> tags = domainQueryService.getTags(domain.optString(Keys.OBJECT_ID));
        domain.put(Domain.DOMAIN_T_TAGS, (Object) tags);

        dataModel.put(Domain.DOMAIN, domain);

        final String domainId = domain.optString(Keys.OBJECT_ID);

        final JSONObject result = articleQueryService.getDomainArticles(domainId, pageNum, pageSize);
        final List<JSONObject> latestArticles = (List<JSONObject>) result.opt(Article.ARTICLES);
        dataModel.put(Common.LATEST_ARTICLES, latestArticles);

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);

        final List<Integer> pageNums = (List<Integer>) pagination.opt(Pagination.PAGINATION_PAGE_NUMS);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        filler.fillDomainNav(dataModel);
        filler.fillHeaderAndFooter(request, response, dataModel);
        filler.fillRandomArticles(dataModel);
        filler.fillHotArticles(dataModel);
        filler.fillSideTags(dataModel);
        filler.fillLatestCmts(dataModel);
    }

    /**
     * Shows domains.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/domains", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showDomains(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);

        renderer.setTemplateName("domains.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject statistic = optionQueryService.getStatistic();
        final int tagCnt = statistic.optInt(Option.ID_C_STATISTIC_TAG_COUNT);
        dataModel.put(Tag.TAG_T_COUNT, tagCnt);

        final int domainCnt = statistic.optInt(Option.ID_C_STATISTIC_DOMAIN_COUNT);
        dataModel.put(Domain.DOMAIN_T_COUNT, domainCnt);

        filler.fillDomainNav(dataModel);
        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows hot articles.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/hot", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showRecentArticles(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("hot.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final int pageSize = Symphonys.getInt("indexArticlesCnt");

        final List<JSONObject> indexArticles = articleQueryService.getIndexArticles(pageSize);
        dataModel.put(Common.INDEX_ARTICLES, indexArticles);

        Stopwatchs.start("Fills");
        try {
            filler.fillHeaderAndFooter(request, response, dataModel);
            filler.fillDomainNav(dataModel);
            if (!(Boolean) dataModel.get(Common.IS_MOBILE)) {
                filler.fillRandomArticles(dataModel);
            }
            filler.fillHotArticles(dataModel);
            filler.fillSideTags(dataModel);
            filler.fillLatestCmts(dataModel);
        } finally {
            Stopwatchs.end();
        }
    }
}
