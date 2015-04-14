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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.servlet.renderer.freemarker.FreeMarkerRenderer;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.processor.advice.AdminCheck;
import org.b3log.symphony.service.ArticleMgmtService;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.CommentMgmtService;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.OptionMgmtService;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.TagQueryService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Filler;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Admin processor.
 *
 * <ul>
 * <li>Shows admin index (/admin/index), GET</li>
 * <li>Shows users (/admin/users), GET</li>
 * <li>Shows a user (/admin/user/{userId}), GET</li>
 * <li>Updates a user (/admin/user/{userId}), POST</li>
 * <li>Shows articles (/admin/articles), GET</li>
 * <li>Shows an article (/admin/article/{articleId}), GET</li>
 * <li>Updates an article (/admin/article/{articleId}), POST</li>
 * <li>Shows comments (/admin/comments), GET</li>
 * <li>Show a comment (/admin/comment/{commentId}), GET</li>
 * <li>Updates a comment (/admin/comment/{commentId}), POST</li>
 * <li>Shows tags (/admin/tags), GET</li>
 * <li>Shows miscellaneous (/admin/misc), GET</li>
 * <li>Updates miscellaneous (/admin/misc), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.0, Apr 14, 2015
 * @since 1.1.0
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
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Article management service.
     */
    @Inject
    private ArticleMgmtService articleMgmtService;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Comment management service.
     */
    @Inject
    private CommentMgmtService commentMgmtService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Option management service.
     */
    @Inject
    private OptionMgmtService optionMgmtService;

    /**
     * Tag query service.
     */
    @Inject
    private TagQueryService tagQueryService;

    /**
     * Filler.
     */
    @Inject
    private Filler filler;
    
    /**
     * Pagination window size.
     */
    private static final int WINDOW_SIZE = 15;

    /**
     * Shows admin index.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin", method = HTTPRequestMethod.GET)
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
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String userName = request.getParameter("name");
        if (!Strings.isEmptyOrNull(userName)) {
            requestJSONObject.put(User.USER_NAME, userName);
        }

        final JSONObject result = userQueryService.getUsers(requestJSONObject);

        dataModel.put(User.USERS, CollectionUtils.jsonArrayToList(result.optJSONArray(User.USERS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows a user.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = AdminCheck.class)
    public void showUser(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                         final String userId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/user.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = userQueryService.getUser(userId);
        dataModel.put(User.USER, user);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates a user.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}", method = HTTPRequestMethod.POST)
    @Before(adviceClass = AdminCheck.class)
    public void updateUser(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                           final String userId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/user.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = userQueryService.getUser(userId);
        dataModel.put(User.USER, user);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            user.put(name, value);
        }

        userMgmtService.updateUser(userId, user);

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
                final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String articleId = request.getParameter("id");
        if (!Strings.isEmptyOrNull(articleId)) {
            requestJSONObject.put(Keys.OBJECT_ID, articleId);
        }

        final Map<String, Class<?>> articleFields = new HashMap<String, Class<?>>();
        articleFields.put(Keys.OBJECT_ID, String.class);
        articleFields.put(Article.ARTICLE_TITLE, String.class);
        articleFields.put(Article.ARTICLE_PERMALINK, String.class);
        articleFields.put(Article.ARTICLE_CREATE_TIME, Long.class);
        articleFields.put(Article.ARTICLE_VIEW_CNT, Integer.class);
        articleFields.put(Article.ARTICLE_COMMENT_CNT, Integer.class);
        articleFields.put(Article.ARTICLE_AUTHOR_EMAIL, String.class);
        articleFields.put(Article.ARTICLE_TAGS, String.class);
        articleFields.put(Article.ARTICLE_STATUS, Integer.class);

        final JSONObject result = articleQueryService.getArticles(requestJSONObject, articleFields);
        dataModel.put(Article.ARTICLES, CollectionUtils.jsonArrayToList(result.optJSONArray(Article.ARTICLES)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows an article.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/article/{articleId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = AdminCheck.class)
    public void showArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                            final String articleId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject article = articleQueryService.getArticle(articleId);
        dataModel.put(Article.ARTICLE, article);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates an article.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/article/{articleId}", method = HTTPRequestMethod.POST)
    @Before(adviceClass = AdminCheck.class)
    public void updateArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                              final String articleId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject article = articleQueryService.getArticle(articleId);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            article.put(name, value);
        }

        articleMgmtService.updateArticle(articleId, article);

        article = articleQueryService.getArticle(articleId);
        dataModel.put(Article.ARTICLE, article);

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
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final Map<String, Class<?>> commentFields = new HashMap<String, Class<?>>();
        commentFields.put(Keys.OBJECT_ID, String.class);
        commentFields.put(Comment.COMMENT_CREATE_TIME, String.class);
        commentFields.put(Comment.COMMENT_AUTHOR_ID, String.class);
        commentFields.put(Comment.COMMENT_ON_ARTICLE_ID, String.class);
        commentFields.put(Comment.COMMENT_SHARP_URL, String.class);
        commentFields.put(Comment.COMMENT_STATUS, Integer.class);
        commentFields.put(Comment.COMMENT_CONTENT, String.class);

        final JSONObject result = commentQueryService.getComments(requestJSONObject, commentFields);
        dataModel.put(Comment.COMMENTS, CollectionUtils.jsonArrayToList(result.optJSONArray(Comment.COMMENTS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows a comment.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param commentId the specified comment id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/comment/{commentId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = AdminCheck.class)
    public void showComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                            final String commentId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/comment.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject comment = commentQueryService.getComment(commentId);
        dataModel.put(Comment.COMMENT, comment);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates a comment.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param commentId the specified comment id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/comment/{commentId}", method = HTTPRequestMethod.POST)
    @Before(adviceClass = AdminCheck.class)
    public void updateComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                              final String commentId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/comment.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject comment = commentQueryService.getComment(commentId);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            comment.put(name, value);
        }

        commentMgmtService.updateComment(commentId, comment);

        comment = commentQueryService.getComment(commentId);
        dataModel.put(Comment.COMMENT, comment);

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

        final List<JSONObject> misc = optionQueryService.getMisc();
        dataModel.put(Option.OPTIONS, misc);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates admin miscellaneous.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/misc", method = HTTPRequestMethod.POST)
    @Before(adviceClass = AdminCheck.class)
    public void updateMisc(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/misc.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        List<JSONObject> misc = new ArrayList<JSONObject>();

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            final JSONObject option = new JSONObject();
            option.put(Keys.OBJECT_ID, name);
            option.put(Option.OPTION_VALUE, value);
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_MISC);

            misc.add(option);
        }

        for (final JSONObject option : misc) {
            optionMgmtService.updateOption(option.getString(Keys.OBJECT_ID), option);
        }

        misc = optionQueryService.getMisc();
        dataModel.put(Option.OPTIONS, misc);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows admin tags.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/tags", method = HTTPRequestMethod.GET)
    @Before(adviceClass = AdminCheck.class)
    public void showTags(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/tags.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Integer.valueOf("20");
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String tagTitle = request.getParameter("title");
        if (!Strings.isEmptyOrNull(tagTitle)) {
            requestJSONObject.put(Tag.TAG_TITLE, tagTitle);
        }

        final Map<String, Class<?>> tagFields = new HashMap<String, Class<?>>();
        tagFields.put(Keys.OBJECT_ID, String.class);
        tagFields.put(Tag.TAG_COMMENT_CNT, String.class);
        tagFields.put(Tag.TAG_DESCRIPTION, String.class);
        tagFields.put(Tag.TAG_ICON_PATH, String.class);
        tagFields.put(Tag.TAG_REFERENCE_CNT, String.class);
        tagFields.put(Tag.TAG_STATUS, String.class);
        tagFields.put(Tag.TAG_TITLE, String.class);

        final JSONObject result = tagQueryService.getTags(requestJSONObject, tagFields);
        dataModel.put(Tag.TAGS, CollectionUtils.jsonArrayToList(result.optJSONArray(Tag.TAGS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }
}
