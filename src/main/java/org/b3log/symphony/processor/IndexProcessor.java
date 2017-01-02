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
package org.b3log.symphony.processor;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Locales;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.AnonymousViewCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;

/**
 * Index processor.
 * <p>
 * <ul>
 * <li>Shows index (/), GET</li>
 * <li>Shows recent articles (/recent), GET</li>
 * <li>Shows hot articles (/hot), GET</li>
 * <li>Shows perfect articles (/perfect), GET</li>
 * <li>Shows about (/about), GET</li>
 * <li>Shows b3log (/b3log), GET</li>
 * <li>Shows SymHub (/symhub), GET</li>
 * <li>Shows kill browser (/kill-browser), GET</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.12.3.24, Dec 26, 2016
 * @since 0.2.0
 */
@RequestProcessor
public class IndexProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(IndexProcessor.class.getName());

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Timeline management service.
     */
    @Inject
    private TimelineMgmtService timelineMgmtService;

    /**
     * Shows md guide.
     *
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/guide/markdown", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showMDGuide(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("other/md-guide.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        InputStream inputStream = null;
        try {
            inputStream = IndexProcessor.class.getResourceAsStream("/md_guide.md");
            final String md = IOUtils.toString(inputStream, "UTF-8");
            String html = Emotions.convert(md);
            html = Markdowns.toHTML(html);

            dataModel.put("md", md);
            dataModel.put("html", html);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Loads markdown guide failed", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows index.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = {"", "/"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showIndex(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("index.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final List<JSONObject> recentArticles = articleQueryService.getIndexRecentArticles(avatarViewMode);
        dataModel.put(Common.RECENT_ARTICLES, recentArticles);

        JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            userMgmtService.tryLogInWithCookie(request, context.getResponse());
        }

        currentUser = userQueryService.getCurrentUser(request);

        if (null != currentUser) {
            if (!UserExt.finshedGuide(currentUser)) {
                response.sendRedirect(Latkes.getServePath() + "/guide");

                return;
            }

            final String userId = currentUser.optString(Keys.OBJECT_ID);

            final int pageSize = Symphonys.getInt("indexArticlesCnt");

            final List<JSONObject> followingTagArticles = articleQueryService.getFollowingTagArticles(
                    avatarViewMode, userId, 1, pageSize);
            dataModel.put(Common.FOLLOWING_TAG_ARTICLES, followingTagArticles);

            final List<JSONObject> followingUserArticles = articleQueryService.getFollowingUserArticles(
                    avatarViewMode, userId, 1, pageSize);
            dataModel.put(Common.FOLLOWING_USER_ARTICLES, followingUserArticles);
        } else {
            dataModel.put(Common.FOLLOWING_TAG_ARTICLES, Collections.emptyList());
            dataModel.put(Common.FOLLOWING_USER_ARTICLES, Collections.emptyList());
        }

        final List<JSONObject> perfectArticles = articleQueryService.getIndexPerfectArticles(avatarViewMode);
        dataModel.put(Common.PERFECT_ARTICLES, perfectArticles);

        final List<JSONObject> timelines = timelineMgmtService.getTimelines();
        dataModel.put(Common.TIMELINES, timelines);

        dataModel.put(Common.SELECTED, Common.INDEX);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillIndexTags(dataModel);
    }

    /**
     * Shows recent articles.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = {"/recent", "/recent/hot", "/recent/good", "/recent/reply"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showRecent(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("recent.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        int pageSize = Symphonys.getInt("indexArticlesCnt");
        final JSONObject user = userQueryService.getCurrentUser(request);
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);

            if (!UserExt.finshedGuide(user)) {
                response.sendRedirect(Latkes.getServePath() + "/guide");

                return;
            }
        }

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        String sortModeStr = StringUtils.substringAfter(request.getRequestURI(), "/recent");
        int sortMode;
        switch (sortModeStr) {
            case "":
                sortMode = 0;

                break;
            case "/hot":
                sortMode = 1;

                break;
            case "/good":
                sortMode = 2;

                break;
            case "/reply":
                sortMode = 3;

                break;
            default:
                sortMode = 0;
        }

        final JSONObject result = articleQueryService.getRecentArticles(avatarViewMode, sortMode, pageNum, pageSize);
        final List<JSONObject> allArticles = (List<JSONObject>) result.get(Article.ARTICLES);

        dataModel.put(Common.SELECTED, Common.RECENT);

        final List<JSONObject> stickArticles = new ArrayList<>();

        final Iterator<JSONObject> iterator = allArticles.iterator();
        while (iterator.hasNext()) {
            final JSONObject article = iterator.next();

            final boolean stick = article.optInt(Article.ARTICLE_T_STICK_REMAINS) > 0;
            article.put(Article.ARTICLE_T_IS_STICK, stick);

            if (stick) {
                stickArticles.add(article);
                iterator.remove();
            }
        }

        dataModel.put(Common.STICK_ARTICLES, stickArticles);
        dataModel.put(Common.LATEST_ARTICLES, allArticles);

        final JSONObject pagination = result.getJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);

        final List<Integer> pageNums = (List<Integer>) pagination.get(Pagination.PAGINATION_PAGE_NUMS);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        dataModelService.fillRandomArticles(avatarViewMode, dataModel);
        dataModelService.fillSideHotArticles(avatarViewMode, dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        dataModel.put(Common.CURRENT, StringUtils.substringAfter(request.getRequestURI(), "/recent"));
    }

    /**
     * Shows hot articles.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/hot", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHotArticles(final HTTPRequestContext context,
                                final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("hot.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        int pageSize = Symphonys.getInt("indexArticlesCnt");

        final JSONObject user = userQueryService.getCurrentUser(request);
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);
        }

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final List<JSONObject> indexArticles = articleQueryService.getHotArticles(avatarViewMode, pageSize);
        dataModel.put(Common.INDEX_ARTICLES, indexArticles);

        dataModel.put(Common.SELECTED, Common.HOT);

        Stopwatchs.start("Fills");
        try {
            dataModelService.fillHeaderAndFooter(request, response, dataModel);
            if (!(Boolean) dataModel.get(Common.IS_MOBILE)) {
                dataModelService.fillRandomArticles(avatarViewMode, dataModel);
            }
            dataModelService.fillSideHotArticles(avatarViewMode, dataModel);
            dataModelService.fillSideTags(dataModel);
            dataModelService.fillLatestCmts(dataModel);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Shows SymHub page.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/symhub", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showSymHub(final HTTPRequestContext context,
                           final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("other/symhub.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final List<JSONObject> syms = Symphonys.getSyms();
        dataModel.put("syms", (Object) syms);

        Stopwatchs.start("Fills");
        try {
            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

            dataModelService.fillHeaderAndFooter(request, response, dataModel);
            if (!(Boolean) dataModel.get(Common.IS_MOBILE)) {
                dataModelService.fillRandomArticles(avatarViewMode, dataModel);
            }
            dataModelService.fillSideHotArticles(avatarViewMode, dataModel);
            dataModelService.fillSideTags(dataModel);
            dataModelService.fillLatestCmts(dataModel);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Shows perfect articles.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/perfect", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showPerfectArticles(final HTTPRequestContext context,
                                    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("perfect.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        int pageSize = Symphonys.getInt("indexArticlesCnt");
        final JSONObject user = userQueryService.getCurrentUser(request);
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);

            if (!UserExt.finshedGuide(user)) {
                response.sendRedirect(Latkes.getServePath() + "/guide");

                return;
            }
        }

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject result = articleQueryService.getPerfectArticles(avatarViewMode, pageNum, pageSize);
        final List<JSONObject> perfectArticles = (List<JSONObject>) result.get(Article.ARTICLES);
        dataModel.put(Common.PERFECT_ARTICLES, perfectArticles);

        dataModel.put(Common.SELECTED, Common.PERFECT);

        final JSONObject pagination = result.getJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);

        final List<Integer> pageNums = (List<Integer>) pagination.get(Pagination.PAGINATION_PAGE_NUMS);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillRandomArticles(avatarViewMode, dataModel);
        dataModelService.fillSideHotArticles(avatarViewMode, dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);
    }

    /**
     * Shows about.
     *
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/about", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showAbout(final HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        response.setHeader("Location", "https://hacpai.com/article/1440573175609");
        response.flushBuffer();
    }

    /**
     * Shows b3log.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/b3log", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showB3log(final HTTPRequestContext context,
                          final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("other/b3log.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        dataModelService.fillRandomArticles(avatarViewMode, dataModel);
        dataModelService.fillSideHotArticles(avatarViewMode, dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);
    }

    /**
     * Shows kill browser page with the specified context.
     *
     * @param context  the specified context
     * @param request  the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     */
    @RequestProcessing(value = "/kill-browser", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showKillBrowser(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        renderer.setTemplateName("other/kill-browser.ftl");
        context.setRenderer(renderer);

        final Map<String, Object> dataModel = renderer.getDataModel();

        final Map<String, String> langs = langPropsService.getAll(Locales.getLocale());

        dataModel.putAll(langs);
        Keys.fillRuntime(dataModel);
        dataModelService.fillMinified(dataModel);
    }
}
