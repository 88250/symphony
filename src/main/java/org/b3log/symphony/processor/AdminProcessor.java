/*
 * Copyright (c) 2012-2015, b3log.org
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

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.servlet.renderer.freemarker.FreeMarkerRenderer;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.processor.advice.AdminCheck;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Filler;
import org.json.JSONObject;

/**
 * Admin processor.
 *
 * <ul>
 * <li>Shows admin index (/admin/index), GET</li>
 * <li>Shows users (/admin/users), GET</li>
 * <li>Updates a user (/admin/user/{userId}), PUT</li>
 * <li>Shows articles (/admin/articles), GET</li>
 * <li>Updates an article (/admin/article/{articleId}), PUT</li>
 * <li>Shows comments (/admin/comments), GET</li>
 * <li>Updates a comment (/admin/comment/{commentId}), PUT</li>
 * <li>Shows miscellaneous (/admin/misc), GET</li>
 * <li>Updates flag of allow register(/admin/misc/allow-register), PUT</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Apr 2, 2015
 * @since 0.3.0
 */
@RequestProcessor
public class AdminProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AdminProcessor.class.getName());

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
     * Filler.
     */
    @Inject
    private Filler filler;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Shows admin index.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/index", method = HTTPRequestMethod.GET)
    @Before(adviceClass = AdminCheck.class)
    public void showIndex(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/index.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows admin users.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/users", method = HTTPRequestMethod.GET)
    @Before(adviceClass = AdminCheck.class)
    public void showUsers(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/users.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Integer.valueOf("20");
        final int windowSize = Integer.valueOf("20");

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final JSONObject result = userQueryService.getUsers(requestJSONObject);
        dataModel.put(User.USERS, result.optJSONArray(User.USERS));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final int pageNums = pagination.optInt(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows admin articles.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/articles", method = HTTPRequestMethod.GET)
    @Before(adviceClass = AdminCheck.class)
    public void showArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Integer.valueOf("20");
        final int windowSize = Integer.valueOf("20");

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final Map<String, Class<?>> articleFields = new HashMap<String, Class<?>>();
        articleFields.put(Article.ARTICLE_TITLE, String.class);
        articleFields.put(Article.ARTICLE_PERMALINK, String.class);
        articleFields.put(Article.ARTICLE_CREATE_TIME, Long.class);
        articleFields.put(Article.ARTICLE_VIEW_CNT, Integer.class);
        articleFields.put(Article.ARTICLE_COMMENT_CNT, Integer.class);

        final JSONObject result = articleQueryService.getArticles(requestJSONObject, articleFields);
        dataModel.put(Article.ARTICLES, result.optJSONArray(Article.ARTICLES));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final int pageNums = pagination.optInt(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows admin comments.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/comments", method = HTTPRequestMethod.GET)
    @Before(adviceClass = AdminCheck.class)
    public void showComments(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/comments.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Integer.valueOf("20");
        final int windowSize = Integer.valueOf("20");

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final Map<String, Class<?>> commentFields = new HashMap<String, Class<?>>();
        commentFields.put(Comment.COMMENT_CREATE_TIME, String.class);
        commentFields.put(Comment.COMMENT_AUTHOR_ID, String.class);
        commentFields.put(Comment.COMMENT_ON_ARTICLE_ID, String.class);

        final JSONObject result = commentQueryService.getComments(requestJSONObject, commentFields);
        dataModel.put(Comment.COMMENTS, result.optJSONArray(Comment.COMMENTS));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final int pageNums = pagination.optInt(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows admin miscellaneous.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/misc", method = HTTPRequestMethod.GET)
    @Before(adviceClass = AdminCheck.class)
    public void showMisc(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/misc.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);
    }
}
