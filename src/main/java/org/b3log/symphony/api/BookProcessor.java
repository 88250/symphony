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

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Book;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.BookMgmtService;
import org.b3log.symphony.service.BookQueryService;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Book processor.
 * <p>
 * <ul>
 * <li>Gets articles with the specified tags (/apis/articles?tags=tag1,tag2&p=1&size=10), GET</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.4, Jan 21, 2017
 * @since 1.9.0
 */
@RequestProcessor
public class BookProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(BookProcessor.class);

    /**
     * Book query service.
     */
    @Inject
    private BookQueryService bookQueryService;

    /**
     * Book management service.
     */
    @Inject
    private BookMgmtService bookMgmtService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Get books.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/books", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = {StopwatchEndAdvice.class})
    public void getBooks(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        String pageStr = requestJSONObject.optString("p");
        if (!Strings.isNumeric(pageStr)) {
            pageStr = "1";
        }

        final int pageNum = Integer.valueOf(pageStr);

        final JSONObject queryRequest = new JSONObject();
        queryRequest.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        queryRequest.put(Pagination.PAGINATION_PAGE_SIZE, 10);
        queryRequest.put(Pagination.PAGINATION_WINDOW_SIZE, 10);

        final Map<String, Class<?>> queryFields = new HashMap<>();
        queryFields.put(Keys.OBJECT_ID, String.class);
        queryFields.put(Article.ARTICLE_TITLE, String.class);
        queryFields.put(Article.ARTICLE_CREATE_TIME, Long.class);
        queryFields.put(Article.ARTICLE_VIEW_CNT, Integer.class);
        queryFields.put(Article.ARTICLE_COMMENT_CNT, Integer.class);
        queryFields.put(Article.ARTICLE_AUTHOR_ID, String.class);
        queryFields.put(Article.ARTICLE_TAGS, String.class);
        queryFields.put(Article.ARTICLE_STATUS, Integer.class);

        final JSONObject ret = bookQueryService.getSharedBooks(queryRequest, queryFields);
        ret.put(Keys.STATUS_CODE, true);

        context.renderJSON(ret);
    }

    /**
     * Shares a book.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/book/share", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {StopwatchEndAdvice.class})
    public void shareBook(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response) {
        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Parses request failed", e);
            context.renderJSON(false);
            context.renderJSONValue(Keys.MSG, langPropsService.get("queryFailedLabel"));

            return;
        }

        String isbn = requestJSONObject.optString(Common.ISBN);
        isbn = StringUtils.trim(isbn);

        if (StringUtils.isBlank(isbn)) {
            context.renderJSON(false);
            context.renderJSONValue(Keys.MSG, langPropsService.get("queryFailedLabel"));

            return;
        }

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final JSONObject book = bookQueryService.getBookByISBN(isbn);
        if (null == book) {
            context.renderJSON(false);
            context.renderJSONValue(Keys.MSG, langPropsService.get("queryFailedLabel"));

            return;
        }


        final JSONObject result = bookMgmtService.shareBook(book, user);

        final JSONRenderer jsonRenderer = new JSONRenderer();
        jsonRenderer.setJSONObject(result);
        context.setRenderer(jsonRenderer);
    }

    /**
     * Gets a book.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/book/info", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = {StopwatchEndAdvice.class})
    public void getBook(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        String isbn = requestJSONObject.optString(Common.ISBN);
        isbn = StringUtils.trim(isbn);

        if (StringUtils.isBlank(isbn)) {
            context.renderJSON(false);
            context.renderJSONValue(Keys.MSG, langPropsService.get("queryFailedLabel"));

            return;
        }

        final JSONObject book = bookQueryService.getBookByISBN(isbn);
        if (null == book) {
            context.renderJSON(false);
            context.renderJSONValue(Keys.MSG, langPropsService.get("queryFailedLabel"));

            return;
        }

        final JSONObject ret = new JSONObject();
        ret.put(Keys.STATUS_CODE, true);
        ret.put(Book.BOOK, book);

        context.renderJSON(ret);
    }
}
