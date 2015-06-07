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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.servlet.renderer.freemarker.FreeMarkerRenderer;
import org.b3log.latke.user.GeneralUser;
import org.b3log.latke.user.UserService;
import org.b3log.latke.user.UserServiceFactory;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Client;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.validate.ArticleAddValidation;
import org.b3log.symphony.processor.advice.validate.ArticleUpdateValidation;
import org.b3log.symphony.service.ArticleMgmtService;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.ClientMgmtService;
import org.b3log.symphony.service.ClientQueryService;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.FollowQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.QueryResults;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Article processor.
 *
 * <ul>
 * <li>Shows an article (/article/{articleId}), GET</li>
 * <li>Shows article adding form page (/add-article), GET</li>
 * <li>Adds an article (/article) <em>locally</em>, POST</li>
 * <li>Updates an article (/article/{id}) <em>locally</em>, PUT</li>
 * <li>Adds an article (/rhythm/article) <em>remotely</em>, POST</li>
 * <li>Markdowns text (/markdown), POST</li>
 * </ul>
 *
 * <p>
 * The '<em>locally</em>' means user post an article on Symphony directly rather than receiving an article from
 * externally (for example Rhythm).
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.4.2.19, Jun 7, 2015
 * @since 0.2.0
 */
@RequestProcessor
public class ArticleProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleProcessor.class.getName());

    /**
     * Article management service.
     */
    @Inject
    private ArticleMgmtService articleMgmtService;

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
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Client management service.
     */
    @Inject
    private ClientMgmtService clientMgmtService;

    /**
     * Client query service.
     */
    @Inject
    private ClientQueryService clientQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * User service.
     */
    private UserService userService = UserServiceFactory.getUserService();
    /**
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;
    /**
     * Filler.
     */
    @Inject
    private Filler filler;

    /**
     * Shows add article.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/add-article", method = HTTPRequestMethod.GET)
    @Before(adviceClass = LoginCheck.class)
    public void showAddArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);

        renderer.setTemplateName("/home/add-article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows article with the specified article id.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/{articleId}", method = HTTPRequestMethod.GET)
    public void showArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String articleId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);

        renderer.setTemplateName("/article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject article = articleQueryService.getArticleById(articleId);
        if (null == article) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        filler.fillHeaderAndFooter(request, response, dataModel);

        final String authorEmail = article.optString(Article.ARTICLE_AUTHOR_EMAIL);
        final JSONObject author = userQueryService.getUserByEmail(authorEmail);
        article.put(Article.ARTICLE_T_AUTHOR_NAME, author.optString(User.USER_NAME));
        article.put(Article.ARTICLE_T_AUTHOR_URL, author.optString(User.USER_URL));
        article.put(Article.ARTICLE_T_AUTHOR_INTRO, author.optString(UserExt.USER_INTRO));
        dataModel.put(Article.ARTICLE, article);

        article.put(Common.IS_MY_ARTICLE, false);

        article.put(Article.ARTICLE_T_AUTHOR, author);

        articleQueryService.processArticleContent(article);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String currentUserId = currentUser.optString(Keys.OBJECT_ID);

            article.put(Common.IS_MY_ARTICLE, currentUserId.equals(article.optString(Article.ARTICLE_AUTHOR_ID)));

            final boolean isFollowing = followQueryService.isFollowing(currentUserId, articleId);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Symphonys.getInt("articleCommentsPageSize");
        final int windowSize = Symphonys.getInt("articleCommentsWindowSize");

        final List<JSONObject> articleComments = commentQueryService.getArticleComments(articleId, pageNum, pageSize);
        article.put(Article.ARTICLE_T_COMMENTS, (Object) articleComments);

        final int commentCnt = article.getInt(Article.ARTICLE_COMMENT_CNT);
        final int pageCount = (int) Math.ceil((double) commentCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Common.ARTICLE_COMMENTS_PAGE_SIZE, pageSize);

        if (!(Boolean) request.getAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT)) {
            articleMgmtService.incArticleViewCount(articleId);
        }

        filler.fillRelevantArticles(dataModel, article);
        filler.fillRandomArticles(dataModel);
    }

    /**
     * Adds an article locally.
     *
     * <p>
     * The request json object (an article):
     * <pre>
     * {
     *   "articleTitle": "",
     *   "articleTags": "", // Tags spliting by ','
     *   "articleContent": "",
     *   "syncWithSymphonyClient": boolean,
     *   "articleCommentable": boolean
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws IOException io exception
     * @throws ServletException servlet exception
     */
    @RequestProcessing(value = "/article", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, ArticleAddValidation.class})
    public void addArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        final String articleTags = formatArticleTags(requestJSONObject.optString(Article.ARTICLE_TAGS));
        final String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        final boolean syncToClient = requestJSONObject.optBoolean(Article.ARTICLE_SYNC_TO_CLIENT);
        final boolean articleCommentable = requestJSONObject.optBoolean(Article.ARTICLE_COMMENTABLE);

        final JSONObject article = new JSONObject();
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_TAGS, articleTags);
        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_EDITOR_TYPE, 0);
        article.put(Article.ARTICLE_SYNC_TO_CLIENT, syncToClient);
        article.put(Article.ARTICLE_COMMENTABLE, articleCommentable);

        try {
            final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);

            article.put(Article.ARTICLE_AUTHOR_ID, currentUser.optString(Keys.OBJECT_ID));

            final String authorEmail = currentUser.optString(User.USER_EMAIL);
            article.put(Article.ARTICLE_AUTHOR_EMAIL, authorEmail);

            article.put(Article.ARTICLE_T_IS_BROADCAST, false);

            articleMgmtService.addArticle(article);
            ret.put(Keys.STATUS_CODE, true);
        } catch (final ServiceException e) {
            final String msg = e.getMessage();
            LOGGER.log(Level.ERROR, "Adds article[title=" + articleTitle + "] failed: {0}", e.getMessage());

            ret.put(Keys.MSG, msg);
        }
    }

    /**
     * Shows add article.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/update-article", method = HTTPRequestMethod.GET)
    @Before(adviceClass = LoginCheck.class)
    public void showUpdateArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String articleId = request.getParameter("id");
        if (Strings.isEmptyOrNull(articleId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final JSONObject article = articleQueryService.getArticleById(articleId);
        if (null == article) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final GeneralUser currentUser = userService.getCurrentUser(request);
        if (null == currentUser
                || !currentUser.getId().equals(article.optString(Article.ARTICLE_AUTHOR_ID))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);

        renderer.setTemplateName("/home/add-article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModel.put(Article.ARTICLE, article);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates an article locally.
     *
     * <p>
     * The request json object (an article):
     * <pre>
     * {
     *   "articleTitle": "",
     *   "articleTags": "", // Tags spliting by ','
     *   "articleContent": "",
     *   "syncWithSymphonyClient": boolean,
     *   "articleCommentable": boolean
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param id the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/{id}", method = HTTPRequestMethod.PUT)
    @Before(adviceClass = {LoginCheck.class, ArticleUpdateValidation.class})
    public void updateArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String id) throws Exception {
        if (Strings.isEmptyOrNull(id)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final JSONObject oldArticle = articleQueryService.getArticleById(id);
        if (null == oldArticle) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        final String articleTags = formatArticleTags(requestJSONObject.optString(Article.ARTICLE_TAGS));
        final String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        final boolean syncToClient = requestJSONObject.optBoolean(Article.ARTICLE_SYNC_TO_CLIENT);
        final boolean articleCommentable = requestJSONObject.optBoolean(Article.ARTICLE_COMMENTABLE);

        final JSONObject article = new JSONObject();
        article.put(Keys.OBJECT_ID, id);
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_TAGS, articleTags);
        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_EDITOR_TYPE, 0);
        article.put(Article.ARTICLE_SYNC_TO_CLIENT, syncToClient);
        article.put(Article.ARTICLE_COMMENTABLE, articleCommentable);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        if (null == currentUser
                || !currentUser.optString(Keys.OBJECT_ID).equals(oldArticle.optString(Article.ARTICLE_AUTHOR_ID))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        article.put(Article.ARTICLE_AUTHOR_ID, currentUser.optString(Keys.OBJECT_ID));

        final String authorEmail = currentUser.optString(User.USER_EMAIL);
        article.put(Article.ARTICLE_AUTHOR_EMAIL, authorEmail);

        try {
            articleMgmtService.updateArticle(article);
            ret.put(Keys.STATUS_CODE, true);
        } catch (final ServiceException e) {
            final String msg = e.getMessage();
            LOGGER.log(Level.ERROR, "Adds article[title=" + articleTitle + "] failed: {0}", e.getMessage());

            ret.put(Keys.MSG, msg);
        }
    }

    /**
     * Adds an article remotely.
     *
     * <p>
     * This interface will be called by Rhythm, so here is no article data validation, just only validate the B3
     * key.</p>
     *
     * <p>
     * The request json object, for example,
     * <pre>
     * {
     *     "article": {
     *         "articleAuthorEmail": "DL88250@gmail.com",
     *         "articleContent": "&lt;p&gt;test&lt;\/p&gt;",
     *         "articleCreateDate": 1350635469922,
     *         "articlePermalink": "/articles/2012/10/19/1350635469866.html",
     *         "articleTags": "test",
     *         "articleTitle": "test",
     *         "clientArticleId": "1350635469866",
     *         "oId": "1350635469866"
     *     },
     *     "clientAdminEmail": "DL88250@gmail.com",
     *     "clientHost": "http://localhost:11099",
     *     "clientName": "B3log Solo",
     *     "clientTitle": "简约设计の艺术",
     *     "clientRuntimeEnv": "LOCAL",
     *     "clientVersion": "0.5.0",
     *     "symphonyKey": "....",
     *     "userB3Key": "Your key"
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/rhythm/article", method = HTTPRequestMethod.POST)
    public void addArticleFromRhythm(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);

        final String userB3Key = requestJSONObject.getString(UserExt.USER_B3_KEY);
        final String symphonyKey = requestJSONObject.getString(Common.SYMPHONY_KEY);
        final String clientAdminEmail = requestJSONObject.getString(Client.CLIENT_ADMIN_EMAIL);
        final String clientName = requestJSONObject.getString(Client.CLIENT_NAME);
        final String clientTitle = requestJSONObject.getString(Client.CLIENT_T_TITLE);
        final String clientVersion = requestJSONObject.getString(Client.CLIENT_VERSION);
        final String clientHost = requestJSONObject.getString(Client.CLIENT_HOST);
        final String clientRuntimeEnv = requestJSONObject.getString(Client.CLIENT_RUNTIME_ENV);

        final JSONObject user = userQueryService.getUserByEmail(clientAdminEmail);
        if (null == user) {
            LOGGER.log(Level.WARN, "The user[email={0}] not found in community", clientAdminEmail);

            return;
        }

        if (!Symphonys.get("keyOfSymphony").equals(symphonyKey) || !user.optString(UserExt.USER_B3_KEY).equals(userB3Key)) {
            LOGGER.log(Level.WARN, "B3 key not match, ignored add article");

            return;
        }

        final JSONObject originalArticle = requestJSONObject.getJSONObject(Article.ARTICLE);
        final String authorId = user.optString(Keys.OBJECT_ID);
        final String clientArticleId = originalArticle.optString(Keys.OBJECT_ID);

        final String articleTitle = originalArticle.optString(Article.ARTICLE_TITLE);
        final String articleTags = formatArticleTags(originalArticle.optString(Article.ARTICLE_TAGS));
        String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);

        final JSONObject article = new JSONObject();
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_TAGS, articleTags);
        article.put(Article.ARTICLE_EDITOR_TYPE, 0);
        article.put(Article.ARTICLE_SYNC_TO_CLIENT, false);
        article.put(Article.ARTICLE_CLIENT_ARTICLE_ID, clientArticleId);

        article.put(Article.ARTICLE_AUTHOR_ID, authorId);
        article.put(Article.ARTICLE_AUTHOR_EMAIL, clientAdminEmail.toLowerCase().trim());

        final String permalink = originalArticle.optString(Article.ARTICLE_PERMALINK);

        final JSONObject articleExisted = articleQueryService.getArticleByClientArticleId(authorId, clientArticleId);
        final boolean toAdd = null == articleExisted;
        if (!toAdd) { // Client requests to add an article, but the article already exist in server
            article.put(Keys.OBJECT_ID, articleExisted.optString(Keys.OBJECT_ID));
            article.put(Article.ARTICLE_T_IS_BROADCAST, false);

            articleContent += "<p class='fn-clear'><span class='fn-right'><span class='ft-small'>该文章同步自</span> "
                    + "<i style='margin-right:5px;'><a target='_blank' href='"
                    + clientHost + permalink + "'>" + clientTitle + "</a></i></span></p>";
        } else { // Add
            final boolean isBroadcast = "aBroadcast".equals(permalink);
            if (isBroadcast) {
                articleContent += "<p class='fn-clear'><span class='fn-right'><span class='ft-small'>该广播来自</span> "
                        + "<i style='margin-right:5px;'><a target='_blank' href='"
                        + clientHost + "'>" + clientTitle + "</a></i></span></p>";
            } else {
                articleContent += "<p class='fn-clear'><span class='fn-right'><span class='ft-small'>该文章同步自</span> "
                        + "<i style='margin-right:5px;'><a target='_blank' href='"
                        + clientHost + permalink + "'>" + clientTitle + "</a></i></span></p>";
            }

            article.put(Article.ARTICLE_T_IS_BROADCAST, isBroadcast);
        }

        article.put(Article.ARTICLE_CONTENT, articleContent);

        try {
            if (toAdd) {
                articleMgmtService.addArticle(article);
            } else {
                articleMgmtService.updateArticle(article);
            }

            ret.put(Keys.STATUS_CODE, true);
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg, e);

            ret.put(Keys.MSG, msg);
        }

        // Updates client record
        JSONObject client = clientQueryService.getClientByAdminEmail(clientAdminEmail);
        if (null == client) {
            client = new JSONObject();
            client.put(Client.CLIENT_ADMIN_EMAIL, clientAdminEmail);
            client.put(Client.CLIENT_HOST, clientHost);
            client.put(Client.CLIENT_NAME, clientName);
            client.put(Client.CLIENT_RUNTIME_ENV, clientRuntimeEnv);
            client.put(Client.CLIENT_VERSION, clientVersion);
            client.put(Client.CLIENT_LATEST_ADD_COMMENT_TIME, 0L);
            client.put(Client.CLIENT_LATEST_ADD_ARTICLE_TIME, System.currentTimeMillis());

            clientMgmtService.addClient(client);
        } else {
            client.put(Client.CLIENT_ADMIN_EMAIL, clientAdminEmail);
            client.put(Client.CLIENT_HOST, clientHost);
            client.put(Client.CLIENT_NAME, clientName);
            client.put(Client.CLIENT_RUNTIME_ENV, clientRuntimeEnv);
            client.put(Client.CLIENT_VERSION, clientVersion);
            client.put(Client.CLIENT_LATEST_ADD_ARTICLE_TIME, System.currentTimeMillis());

            clientMgmtService.updateClient(client);
        }
    }

    /**
     * Updates an article remotely.
     *
     * <p>
     * This interface will be called by Rhythm, so here is no article data validation, just only validate the B3
     * key.</p>
     *
     * <p>
     * The request json object, for example,
     * <pre>
     * {
     *     "article": {
     *         "articleAuthorEmail": "DL88250@gmail.com",
     *         "articleContent": "&lt;p&gt;test&lt;\/p&gt;",
     *         "articleCreateDate": 1350635469922,
     *         "articlePermalink": "/articles/2012/10/19/1350635469866.html",
     *         "articleTags": "test",
     *         "articleTitle": "test",
     *         "clientArticleId": "1350635469866",
     *         "oId": "1350635469866"
     *     },
     *     "clientAdminEmail": "DL88250@gmail.com",
     *     "clientHost": "http://localhost:11099",
     *     "clientName": "B3log Solo",
     *     "clientTitle": "简约设计の艺术",
     *     "clientRuntimeEnv": "LOCAL",
     *     "clientVersion": "0.5.0",
     *     "symphonyKey": "....",
     *     "userB3Key": "Your key"
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/rhythm/article", method = HTTPRequestMethod.PUT)
    public void updateArticleFromRhythm(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);

        final String userB3Key = requestJSONObject.getString(UserExt.USER_B3_KEY);
        final String symphonyKey = requestJSONObject.getString(Common.SYMPHONY_KEY);
        final String clientAdminEmail = requestJSONObject.getString(Client.CLIENT_ADMIN_EMAIL);
        final String clientName = requestJSONObject.getString(Client.CLIENT_NAME);
        final String clientTitle = requestJSONObject.getString(Client.CLIENT_T_TITLE);
        final String clientVersion = requestJSONObject.getString(Client.CLIENT_VERSION);
        final String clientHost = requestJSONObject.getString(Client.CLIENT_HOST);
        final String clientRuntimeEnv = requestJSONObject.getString(Client.CLIENT_RUNTIME_ENV);

        final JSONObject user = userQueryService.getUserByEmail(clientAdminEmail);
        if (null == user) {
            LOGGER.log(Level.WARN, "The user[email={0}] not found in community", clientAdminEmail);

            return;
        }

        if (!Symphonys.get("keyOfSymphony").equals(symphonyKey) || !user.optString(UserExt.USER_B3_KEY).equals(userB3Key)) {
            LOGGER.log(Level.WARN, "B3 key not match, ignored add article");

            return;
        }

        final JSONObject originalArticle = requestJSONObject.getJSONObject(Article.ARTICLE);

        final String articleTitle = originalArticle.optString(Article.ARTICLE_TITLE);
        final String articleTags = formatArticleTags(originalArticle.optString(Article.ARTICLE_TAGS));
        String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);

        final String permalink = originalArticle.optString(Article.ARTICLE_PERMALINK);
        articleContent += "<p class='fn-clear'><span class='fn-right'><span class='ft-small'>该文章同步自</span> "
                + "<i style='margin-right:5px;'><a target='_blank' href='"
                + clientHost + permalink + "'>" + clientTitle + "</a></i></span></p>";

        final String authorId = user.optString(Keys.OBJECT_ID);
        final String clientArticleId = originalArticle.optString(Keys.OBJECT_ID);
        final JSONObject oldArticle = articleQueryService.getArticleByClientArticleId(authorId, clientArticleId);
        if (null == oldArticle) {
            LOGGER.log(Level.WARN, "Not found article [clientHost={0}, clientArticleId={1}] to update", clientHost, clientArticleId);

            return;
        }

        final JSONObject article = new JSONObject();
        article.put(Keys.OBJECT_ID, oldArticle.optString(Keys.OBJECT_ID));
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_TAGS, articleTags);
        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_EDITOR_TYPE, 0);
        article.put(Article.ARTICLE_SYNC_TO_CLIENT, false);
        article.put(Article.ARTICLE_CLIENT_ARTICLE_ID, clientArticleId);

        article.put(Article.ARTICLE_AUTHOR_ID, authorId);
        article.put(Article.ARTICLE_AUTHOR_EMAIL, clientAdminEmail.toLowerCase().trim());

        article.put(Article.ARTICLE_T_IS_BROADCAST, false);

        try {
            articleMgmtService.updateArticle(article);

            ret.put(Keys.STATUS_CODE, true);
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg, e);

            ret.put(Keys.MSG, msg);
        }

        // Updates client record
        JSONObject client = clientQueryService.getClientByAdminEmail(clientAdminEmail);
        if (null == client) {
            client = new JSONObject();
            client.put(Client.CLIENT_ADMIN_EMAIL, clientAdminEmail);
            client.put(Client.CLIENT_HOST, clientHost);
            client.put(Client.CLIENT_NAME, clientName);
            client.put(Client.CLIENT_RUNTIME_ENV, clientRuntimeEnv);
            client.put(Client.CLIENT_VERSION, clientVersion);
            client.put(Client.CLIENT_LATEST_ADD_COMMENT_TIME, 0L);
            client.put(Client.CLIENT_LATEST_ADD_ARTICLE_TIME, System.currentTimeMillis());

            clientMgmtService.addClient(client);
        } else {
            client.put(Client.CLIENT_ADMIN_EMAIL, clientAdminEmail);
            client.put(Client.CLIENT_HOST, clientHost);
            client.put(Client.CLIENT_NAME, clientName);
            client.put(Client.CLIENT_RUNTIME_ENV, clientRuntimeEnv);
            client.put(Client.CLIENT_VERSION, clientVersion);
            client.put(Client.CLIENT_LATEST_ADD_ARTICLE_TIME, System.currentTimeMillis());

            clientMgmtService.updateClient(client);
        }
    }

    /**
     * Markdowns.
     *
     * <p>
     * Renders the response with a json object, for example,
     * <pre>
     * {
     *     "html": ""
     * }
     * </pre>
     * </p>
     *
     * @param request the specified http servlet request
     * @param response the specified http servlet response
     * @param context the specified http request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/markdown", method = HTTPRequestMethod.POST)
    public void markdown2HTML(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);
        final JSONObject result = new JSONObject();
        renderer.setJSONObject(result);

        result.put(Keys.STATUS_CODE, true);

        String markdownText = request.getParameter("markdownText");
        if (Strings.isEmptyOrNull(markdownText)) {
            result.put("html", "");

            return;
        }

        final JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        markdownText = markdownText.replace("<", "&lt;").replace(">", "&gt;").replace("&lt;pre&gt;", "<pre>").replace("&lt;/pre&gt;",
                "</pre>");

        result.put("html", Markdowns.toHTML(markdownText));
    }

    /**
     * Gets article preview content.
     *
     * <p>
     * Renders the response with a json object, for example,
     * <pre>
     * {
     *     "html": ""
     * }
     * </pre>
     * </p>
     *
     * @param request the specified http servlet request
     * @param response the specified http servlet response
     * @param context the specified http request context
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/{articleId}/preview", method = HTTPRequestMethod.GET)
    public void getArticlePreviewContent(final HttpServletRequest request, final HttpServletResponse response,
            final HTTPRequestContext context, final String articleId) throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);
        final JSONObject result = QueryResults.trueResult();
        renderer.setJSONObject(result);

        result.put("html", "");

        final JSONObject article = articleQueryService.getArticle(articleId);
        if (null == article) {
            result.put(Keys.STATUS_CODE, false);

            return;
        }

        final int length = Integer.valueOf("150");
        String content = article.optString(Article.ARTICLE_CONTENT);
        content = Emotions.convert(content);
        content = Markdowns.toHTML(content);

        content = Jsoup.clean(content, Whitelist.none());
        if (content.length() >= length) {
            content = StringUtils.substring(content, 0, length)
                    + " ....";
        }

        result.put("html", content);
    }

    /**
     * Formats the specified article tags.
     *
     * <p>
     * Trims every tag.
     * </p>
     *
     * @param articleTags the specified article tags
     * @return formatted tags string
     */
    private String formatArticleTags(final String articleTags) {
        final String articleTags1 = articleTags.replaceAll("，", ",").replaceAll("、", ",");
        final String[] tagTitles = articleTags1.split(",");
        final StringBuilder tagsBuilder = new StringBuilder();
        for (final String tagTitle : tagTitles) {
            tagsBuilder.append(tagTitle.trim()).append(",");
        }
        if (tagsBuilder.length() > 0) {
            tagsBuilder.deleteCharAt(tagsBuilder.length() - 1);
        }

        return tagsBuilder.toString();
    }
}
