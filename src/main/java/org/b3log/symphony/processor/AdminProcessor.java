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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.MD5;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Domain;
import org.b3log.symphony.model.Invitecode;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.AdminCheck;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.UserRegister2Validation;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.service.ArticleMgmtService;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.CommentMgmtService;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.DomainMgmtService;
import org.b3log.symphony.service.DomainQueryService;
import org.b3log.symphony.service.InvitecodeMgmtService;
import org.b3log.symphony.service.InvitecodeQueryService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.OptionMgmtService;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.PointtransferMgmtService;
import org.b3log.symphony.service.PointtransferQueryService;
import org.b3log.symphony.service.SearchMgmtService;
import org.b3log.symphony.service.TagMgmtService;
import org.b3log.symphony.service.TagQueryService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Admin processor.
 *
 * <ul>
 * <li>Shows admin index (/admin/index), GET</li>
 * <li>Shows users (/admin/users), GET</li>
 * <li>Shows a user (/admin/user/{userId}), GET</li>
 * <li>Shows add user (/admin/add-user), GET</li>
 * <li>Adds a user (/admin/add-user), POST</li>
 * <li>Updates a user (/admin/user/{userId}), POST</li>
 * <li>Updates a user's email (/admin/user/{userId}/email), POST</li>
 * <li>Updates a user's username (/admin/user/{userId}/username), POST</li>
 * <li>Charges a user's point (/admin/user/{userId}/charge-point), POST</li>
 * <li>Exchanges a user's point (/admin/user/{userId}/exchange-point), POST</li>
 * <li>Deducts a user's abuse point (/admin/user/{userId}/abuse-point), POST</li>
 * <li>Shows articles (/admin/articles), GET</li>
 * <li>Shows an article (/admin/article/{articleId}), GET</li>
 * <li>Updates an article (/admin/article/{articleId}), POST</li>
 * <li>Removes an article (/admin/remove-article), POST</li>
 * <li>Shows add article (/admin/add-article), GET</li>
 * <li>Adds an article (/admin/add-article), POST</li>
 * <li>Shows comments (/admin/comments), GET</li>
 * <li>Show a comment (/admin/comment/{commentId}), GET</li>
 * <li>Updates a comment (/admin/comment/{commentId}), POST</li>
 * <li>Removes a comment (/admin/remove-comment), POST</li>
 * <li>Shows domains (/admin/domains, GET</li>
 * <li>Show a domain (/admin/domain/{domainId}, GET</li>
 * <li>Updates a domain (/admin/domain/{domainId}), POST</li>
 * <li>Shows tags (/admin/tags), GET</li>
 * <li>Show a tag (/admin/tag/{tagId}), GET</li>
 * <li>Updates a tag (/admin/tag/{tagId}), POST</li>
 * <li>Generates invitecodes (/admin/invitecodes/generate), POST</li>
 * <li>Shows invitecodes (/admin/invitecodes), GET</li>
 * <li>Show an invitecode (/admin/invitecode/{invitecodeId}), GET</li>
 * <li>Updates an invitecode (/admin/invitecode/{invitecodeId}), POST</li>
 * <li>Shows miscellaneous (/admin/misc), GET</li>
 * <li>Updates miscellaneous (/admin/misc), POST</li>
 * <li>Search index (/admin/search/index), POST</li>
 * <li>Search index one article (/admin/search-index-article), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.17.3.5, Jul 5, 2016
 * @since 1.1.0
 */
@RequestProcessor
public class AdminProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AdminProcessor.class.getName());

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

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
     * Domain query service.
     */
    @Inject
    private DomainQueryService domainQueryService;

    /**
     * Tag query service.
     */
    @Inject
    private TagQueryService tagQueryService;

    /**
     * Domain management service.
     */
    @Inject
    private DomainMgmtService domainMgmtService;

    /**
     * Tag management service.
     */
    @Inject
    private TagMgmtService tagMgmtService;

    /**
     * Pointtransfer management service.
     */
    @Inject
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Pointtransfer query service.
     */
    @Inject
    private PointtransferQueryService pointtransferQueryService;

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Search management service.
     */
    @Inject
    private SearchMgmtService searchMgmtService;

    /**
     * Invitecode query service.
     */
    @Inject
    private InvitecodeQueryService invitecodeQueryService;

    /**
     * Invitecode management service.
     */
    @Inject
    private InvitecodeMgmtService invitecodeMgmtService;

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
     * Pagination page size.
     */
    private static final int PAGE_SIZE = 20;

    /**
     * Generates invitecodes.
     *
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/invitecodes/generate", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void generateInvitecodes(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String quantityStr = request.getParameter("quantity");
        int quantity = 20;
        try {
            quantity = Integer.valueOf(quantityStr);
        } catch (final NumberFormatException e) {
        }

        invitecodeMgmtService.generateInvitecodes(quantity);

        response.sendRedirect(Latkes.getServePath() + "/admin/invitecodes");
    }

    /**
     * Shows admin invitecodes.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/invitecodes", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showInvitecodes(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/invitecodes.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final JSONObject result = invitecodeQueryService.getInvitecodes(requestJSONObject);
        dataModel.put(Invitecode.INVITECODES, CollectionUtils.jsonArrayToList(result.optJSONArray(Invitecode.INVITECODES)));

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
     * Shows an invitecode.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param invitecodeId the specified invitecode id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/invitecode/{invitecodeId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showInvitecode(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String invitecodeId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/invitecode.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject invitecode = invitecodeQueryService.getInvitecodeById(invitecodeId);
        dataModel.put(Invitecode.INVITECODE, invitecode);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates an invitecode.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param invitecodeId the specified invitecode id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/invitecode/{invitecodeId}", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateInvitecode(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String invitecodeId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/invitecode.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject invitecode = invitecodeQueryService.getInvitecodeById(invitecodeId);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            invitecode.put(name, value);
        }

        invitecodeMgmtService.updateInvitecode(invitecodeId, invitecode);

        invitecode = invitecodeQueryService.getInvitecodeById(invitecodeId);
        dataModel.put(Invitecode.INVITECODE, invitecode);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows add article.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/add-article", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showAddArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/add-article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Adds an article.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/add-article", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String userName = request.getParameter(User.USER_NAME);
        final JSONObject author = userQueryService.getUserByName(userName);
        if (null == author) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, langPropsService.get("notFoundUserLabel"));
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        final String timeStr = request.getParameter(Common.TIME);
        final String articleTitle = request.getParameter(Article.ARTICLE_TITLE);
        final String articleTags = request.getParameter(Article.ARTICLE_TAGS);
        final String articleContent = request.getParameter(Article.ARTICLE_CONTENT);
        String rewardContent = request.getParameter(Article.ARTICLE_REWARD_CONTENT);
        final String rewardPoint = request.getParameter(Article.ARTICLE_REWARD_POINT);

        long time = System.currentTimeMillis();

        try {
            final Date date = DateUtils.parseDate(timeStr, new String[]{"yyyy-MM-dd'T'HH:mm"});

            time = date.getTime();
            final int random = RandomUtils.nextInt(9999);
            time += random;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Parse time failed, using current time instead");
        }

        final JSONObject article = new JSONObject();
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_TAGS, articleTags);
        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_REWARD_CONTENT, rewardContent);
        article.put(Article.ARTICLE_REWARD_POINT, Integer.valueOf(rewardPoint));
        article.put(User.USER_NAME, userName);
        article.put(Common.TIME, time);

        try {
            articleMgmtService.addArticleByAdmin(article);
        } catch (final ServiceException e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/articles");
    }

    /**
     * Adds a reserved word.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/add-reserved-word", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addReservedWord(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        String word = request.getParameter(Common.WORD);
        word = StringUtils.trim(word);
        if (StringUtils.isBlank(word)) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, langPropsService.get("invalidReservedWordLabel"));
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        if (optionQueryService.existReservedWord(word)) {
            response.sendRedirect(Latkes.getServePath() + "/admin/reserved-words");

            return;
        }

        try {
            final JSONObject reservedWord = new JSONObject();
            reservedWord.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_RESERVED_WORDS);
            reservedWord.put(Option.OPTION_VALUE, word);

            optionMgmtService.addOption(reservedWord);
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/reserved-words");
    }

    /**
     * Shows add reserved word.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/add-reserved-word", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showAddReservedWord(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/add-reserved-word.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates a reserved word.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param id the specified reserved wordid
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/reserved-word/{id}", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateReservedWord(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String id) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/reserved-word.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject word = optionQueryService.getOption(id);
        dataModel.put(Common.WORD, word);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            word.put(name, value);
        }

        optionMgmtService.updateOption(id, word);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows reserved words.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/reserved-words", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showReservedWords(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/reserved-words.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModel.put(Common.WORDS, optionQueryService.getReservedWords());

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows a reserved word.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param id the specified reserved word id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/reserved-word/{id}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showReservedWord(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String id) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/reserved-word.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject word = optionQueryService.getOption(id);
        dataModel.put(Common.WORD, word);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Removes a reserved word.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/remove-reserved-word", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void removeReservedWord(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String id = request.getParameter("id");
        optionMgmtService.removeOption(id);

        response.sendRedirect(Latkes.getServePath() + "/admin/reserved-words");
    }

    /**
     * Removes a comment.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/remove-comment", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void removeComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String commentId = request.getParameter(Comment.COMMENT_T_ID);
        commentMgmtService.removeComment(commentId);

        response.sendRedirect(Latkes.getServePath() + "/admin/comments");
    }

    /**
     * Removes an article.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/remove-article", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void removeArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String articleId = request.getParameter(Article.ARTICLE_T_ID);
        articleMgmtService.removeArticle(articleId);

        response.sendRedirect(Latkes.getServePath() + "/admin/articles");
    }

    /**
     * Shows admin index.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showIndex(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/index.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);

        if ((Boolean) dataModel.get(Common.IS_MOBILE)) {
            final JSONObject statistic = optionQueryService.getStatistic();
            dataModel.put(Option.CATEGORY_C_STATISTIC, statistic);
        }
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
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showUsers(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/users.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String nameOrEmail = request.getParameter(Common.USER_NAME_OR_EMAIL);
        if (!Strings.isEmptyOrNull(nameOrEmail)) {
            requestJSONObject.put(Common.USER_NAME_OR_EMAIL, nameOrEmail);
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
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showUser(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/user.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = userQueryService.getUser(userId);
        dataModel.put(User.USER, user);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows add user.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/add-user", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showAddUser(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/add-user.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Adds a user.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/add-user", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addUser(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String userName = request.getParameter(User.USER_NAME);
        final String email = request.getParameter(User.USER_EMAIL);
        final String password = request.getParameter(User.USER_PASSWORD);
        final String appRole = request.getParameter(UserExt.USER_APP_ROLE);

        final boolean nameInvalid = UserRegisterValidation.invalidUserName(userName);
        final boolean emailInvalid = !Strings.isEmail(email);
        final boolean passwordInvalid = UserRegister2Validation.invalidUserPassword(password);

        if (nameInvalid || emailInvalid || passwordInvalid) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            if (nameInvalid) {
                dataModel.put(Keys.MSG, langPropsService.get("invalidUserNameLabel"));
            } else if (emailInvalid) {
                dataModel.put(Keys.MSG, langPropsService.get("invalidEmailLabel"));
            } else if (passwordInvalid) {
                dataModel.put(Keys.MSG, langPropsService.get("invalidPasswordLabel"));
            }

            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        String userId;
        try {
            final JSONObject user = new JSONObject();
            user.put(User.USER_NAME, userName);
            user.put(User.USER_EMAIL, email);
            user.put(User.USER_PASSWORD, MD5.hash(password));
            user.put(UserExt.USER_APP_ROLE, appRole);
            user.put(UserExt.USER_STATUS, UserExt.USER_STATUS_C_VALID);

            userId = userMgmtService.addUser(user);
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
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
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateUser(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/user.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = userQueryService.getUser(userId);
        dataModel.put(User.USER, user);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            if (name.equals(UserExt.USER_POINT) || name.equals(UserExt.USER_APP_ROLE) || name.equals(UserExt.USER_STATUS)
                    || name.equals(UserExt.USER_COMMENT_VIEW_MODE)) {
                user.put(name, Integer.valueOf(value));
            } else if (name.equals(User.USER_PASSWORD)) {
                final String oldPwd = (String) user.getString(name);
                if (!oldPwd.equals(value) && !Strings.isEmptyOrNull(value)) {
                    user.put(name, MD5.hash(value));
                }
            } else if (name.equals(UserExt.SYNC_TO_CLIENT)) {
                user.put(UserExt.SYNC_TO_CLIENT, Boolean.valueOf(value));
            } else {
                user.put(name, value);
            }
        }

        userMgmtService.updateUser(userId, user);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates a user's email.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}/email", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateUserEmail(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        final JSONObject user = userQueryService.getUser(userId);
        final String oldEmail = user.optString(User.USER_EMAIL);
        final String newEmail = request.getParameter(User.USER_EMAIL);

        if (oldEmail.equals(newEmail)) {
            response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);

            return;
        }

        user.put(User.USER_EMAIL, newEmail);

        try {
            userMgmtService.updateUserEmail(userId, user);
        } catch (final ServiceException e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Updates a user's username.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}/username", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateUserName(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        final JSONObject user = userQueryService.getUser(userId);
        final String oldUserName = user.optString(User.USER_NAME);
        final String newUserName = request.getParameter(User.USER_NAME);

        if (oldUserName.equals(newUserName)) {
            response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);

            return;
        }

        user.put(User.USER_NAME, newUserName);

        try {
            userMgmtService.updateUserName(userId, user);
        } catch (final ServiceException e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Charges a user's point.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}/charge-point", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void chargePoint(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        final String pointStr = request.getParameter(Common.POINT);
        final String memo = request.getParameter("memo");

        if (StringUtils.isBlank(pointStr) || StringUtils.isBlank(memo) || !Strings.isNumeric(memo.split("-")[0])) {
            LOGGER.warn("Charge point memo format error");

            response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);

            return;
        }

        try {
            final int point = Integer.valueOf(pointStr);

            final String transferId = pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                    Pointtransfer.TRANSFER_TYPE_C_CHARGE, point, memo);

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, userId);
            notification.put(Notification.NOTIFICATION_DATA_ID, transferId);

            notificationMgmtService.addPointChargeNotification(notification);
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Deducts a user's abuse point.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}/abuse-point", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void abusePoint(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        final String pointStr = request.getParameter(Common.POINT);

        try {
            final int point = Integer.valueOf(pointStr);

            final JSONObject user = userQueryService.getUser(userId);
            final int currentPoint = user.optInt(UserExt.USER_POINT);

            if (currentPoint - point < 0) {
                final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
                context.setRenderer(renderer);
                renderer.setTemplateName("admin/error.ftl");
                final Map<String, Object> dataModel = renderer.getDataModel();

                dataModel.put(Keys.MSG, langPropsService.get("insufficientBalanceLabel"));
                filler.fillHeaderAndFooter(request, response, dataModel);

                return;
            }

            final String memo = request.getParameter(Common.MEMO);

            final String transferId = pointtransferMgmtService.transfer(userId, Pointtransfer.ID_C_SYS,
                    Pointtransfer.TRANSFER_TYPE_C_ABUSE_DEDUCT, point, memo);

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, userId);
            notification.put(Notification.NOTIFICATION_DATA_ID, transferId);

            notificationMgmtService.addAbusePointDeductNotification(notification);
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Compensates a user's initial point.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}/init-point", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void initPoint(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        try {
            final JSONObject user = userQueryService.getUser(userId);
            if (null == user
                    || UserExt.USER_STATUS_C_VALID != user.optInt(UserExt.USER_STATUS)
                    || UserExt.NULL_USER_NAME.equals(user.optString(User.USER_NAME))) {
                response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);

                return;
            }

            final List<JSONObject> records
                    = pointtransferQueryService.getLatestPointtransfers(userId, Pointtransfer.TRANSFER_TYPE_C_INIT, 1);
            if (records.isEmpty()) {
                pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId, Pointtransfer.TRANSFER_TYPE_C_INIT,
                        Pointtransfer.TRANSFER_SUM_C_INIT, userId, Long.valueOf(userId));
            }
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Exchanges a user's point.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}/exchange-point", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void exchangePoint(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        final String pointStr = request.getParameter(Common.POINT);

        try {
            final int point = Integer.valueOf(pointStr);

            final JSONObject user = userQueryService.getUser(userId);
            final int currentPoint = user.optInt(UserExt.USER_POINT);

            if (currentPoint - point < Symphonys.getInt("pointExchangeMin")) {
                final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
                context.setRenderer(renderer);
                renderer.setTemplateName("admin/error.ftl");
                final Map<String, Object> dataModel = renderer.getDataModel();

                dataModel.put(Keys.MSG, langPropsService.get("insufficientBalanceLabel"));
                filler.fillHeaderAndFooter(request, response, dataModel);

                return;
            }

            final String memo = String.valueOf(Math.floor(point / (double) Symphonys.getInt("pointExchangeUnit")));

            final String transferId = pointtransferMgmtService.transfer(userId, Pointtransfer.ID_C_SYS,
                    Pointtransfer.TRANSFER_TYPE_C_EXCHANGE, point, memo);

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, userId);
            notification.put(Notification.NOTIFICATION_DATA_ID, transferId);

            notificationMgmtService.addPointExchangeNotification(notification);
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
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
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = PAGE_SIZE;
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
        articleFields.put(Article.ARTICLE_AUTHOR_ID, String.class);
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
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String articleId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
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
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String articleId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject article = articleQueryService.getArticle(articleId);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            if (name.equals(Article.ARTICLE_REWARD_POINT)
                    || name.equals(Article.ARTICLE_STATUS)
                    || name.equals(Article.ARTICLE_TYPE)
                    || name.equals(Article.ARTICLE_GOOD_CNT)
                    || name.equals(Article.ARTICLE_BAD_CNT)) {
                article.put(name, Integer.valueOf(value));
            } else {
                article.put(name, value);
            }
        }

        final String articleTags = Tag.formatTags(article.optString(Article.ARTICLE_TAGS));
        article.put(Article.ARTICLE_TAGS, articleTags);

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
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showComments(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/comments.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = PAGE_SIZE;
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
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
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
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String commentId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
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
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String commentId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
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
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showMisc(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
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
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateMisc(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
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
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showTags(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/tags.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String tagTitle = request.getParameter(Common.TITLE);
        if (!Strings.isEmptyOrNull(tagTitle)) {
            requestJSONObject.put(Tag.TAG_TITLE, tagTitle);
        }

        final Map<String, Class<?>> tagFields = new HashMap<String, Class<?>>();
        tagFields.put(Keys.OBJECT_ID, String.class);
        tagFields.put(Tag.TAG_TITLE, String.class);
        tagFields.put(Tag.TAG_DESCRIPTION, String.class);
        tagFields.put(Tag.TAG_ICON_PATH, String.class);
        tagFields.put(Tag.TAG_COMMENT_CNT, Integer.class);
        tagFields.put(Tag.TAG_REFERENCE_CNT, Integer.class);
        tagFields.put(Tag.TAG_FOLLOWER_CNT, Integer.class);
        tagFields.put(Tag.TAG_STATUS, Integer.class);
        tagFields.put(Tag.TAG_GOOD_CNT, Integer.class);
        tagFields.put(Tag.TAG_BAD_CNT, Integer.class);

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

    /**
     * Shows a tag.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param tagId the specified tag id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/tag/{tagId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showTag(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String tagId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/tag.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject tag = tagQueryService.getTag(tagId);
        dataModel.put(Tag.TAG, tag);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates a tag.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param tagId the specified tag id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/tag/{tagId}", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateTag(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String tagId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/tag.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject tag = tagQueryService.getTag(tagId);

        final String oldTitle = tag.optString(Tag.TAG_TITLE);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            tag.put(name, value);
        }

        final String newTitle = tag.optString(Tag.TAG_TITLE);

        if (oldTitle.equalsIgnoreCase(newTitle)) {
            tagMgmtService.updateTag(tagId, tag);
        }

        tag = tagQueryService.getTag(tagId);
        dataModel.put(Tag.TAG, tag);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows admin domains.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/domains", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showDomains(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/domains.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String domainTitle = request.getParameter(Common.TITLE);
        if (!Strings.isEmptyOrNull(domainTitle)) {
            requestJSONObject.put(Domain.DOMAIN_TITLE, domainTitle);
        }

        final Map<String, Class<?>> domainFields = new HashMap<String, Class<?>>();
        domainFields.put(Keys.OBJECT_ID, String.class);
        domainFields.put(Domain.DOMAIN_TITLE, String.class);
        domainFields.put(Domain.DOMAIN_DESCRIPTION, String.class);
        domainFields.put(Domain.DOMAIN_ICON_PATH, String.class);
        domainFields.put(Domain.DOMAIN_STATUS, String.class);
        domainFields.put(Domain.DOMAIN_URI, String.class);

        final JSONObject result = domainQueryService.getDomains(requestJSONObject, domainFields);
        dataModel.put(Domain.DOMAINS, CollectionUtils.jsonArrayToList(result.optJSONArray(Domain.DOMAINS)));

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
     * Shows a domain.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param domainId the specified domain id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/domain/{domainId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showDomain(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String domainId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/domain.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject domain = domainQueryService.getDomain(domainId);
        dataModel.put(Domain.DOMAIN, domain);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates a domain.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param domainId the specified domain id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/domain/{domainId}", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateDomain(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String domainId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/domain.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject domain = domainQueryService.getDomain(domainId);
        final String oldTitle = domain.optString(Domain.DOMAIN_TITLE);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            String value = request.getParameter(name);

            if (Domain.DOMAIN_ICON_PATH.equals(name)) {
                value = StringUtils.replace(value, "\"", "'");
            }

            domain.put(name, value);
        }

        domain.remove(Domain.DOMAIN_T_TAGS);

        final String newTitle = domain.optString(Domain.DOMAIN_TITLE);

        if (oldTitle.equalsIgnoreCase(newTitle)) {
            domainMgmtService.updateDomain(domainId, domain);
        }

        domain = domainQueryService.getDomain(domainId);
        dataModel.put(Domain.DOMAIN, domain);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows add domain.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/add-domain", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showAddDomain(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/add-domain.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Adds a domain.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/add-domain", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addDomain(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String domainTitle = request.getParameter(Domain.DOMAIN_TITLE);

        if (StringUtils.isBlank(domainTitle)) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, langPropsService.get("invalidDomainTitleLabel"));

            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        if (null != domainQueryService.getByTitle(domainTitle)) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, langPropsService.get("duplicatedDomainLabel"));

            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        String domainId;
        try {
            final JSONObject domain = new JSONObject();
            domain.put(Domain.DOMAIN_TITLE, domainTitle);
            domain.put(Domain.DOMAIN_URI, domainTitle);
            domain.put(Domain.DOMAIN_DESCRIPTION, domainTitle);
            domain.put(Domain.DOMAIN_STATUS, Domain.DOMAIN_STATUS_C_VALID);

            domainId = domainMgmtService.addDomain(domain);
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/domain/" + domainId);
    }

    /**
     * Removes a domain.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/remove-domain", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void removeDomain(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String domainId = request.getParameter(Domain.DOMAIN_T_ID);
        domainMgmtService.removeDomain(domainId);

        response.sendRedirect(Latkes.getServePath() + "/admin/domains");
    }

    /**
     * Adds a tag into a domain.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param domainId the specified domain id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/domain/{domainId}/add-tag", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addDomain(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String domainId)
            throws Exception {
        final String tagTitle = request.getParameter(Tag.TAG_TITLE);
        final JSONObject tag = tagQueryService.getTagByTitle(tagTitle);

        if (null == tag) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, langPropsService.get("invalidTagLabel"));

            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        if (domainQueryService.containTag(tagTitle, domainId)) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            String msg = langPropsService.get("domainContainTagLabel");
            msg = msg.replace("{tag}", tagTitle);

            dataModel.put(Keys.MSG, msg);

            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        final JSONObject domainTag = new JSONObject();
        domainTag.put(Domain.DOMAIN + "_" + Keys.OBJECT_ID, domainId);
        domainTag.put(Tag.TAG + "_" + Keys.OBJECT_ID, tag.optString(Keys.OBJECT_ID));

        domainMgmtService.addDomainTag(domainTag);

        response.sendRedirect(Latkes.getServePath() + "/admin/domain/" + domainId);
    }

    /**
     * Removes a tag from a domain.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param domainId the specified domain id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/domain/{domainId}/remove-tag", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void removeDomain(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String domainId)
            throws Exception {
        final String tagTitle = request.getParameter(Tag.TAG_TITLE);
        final JSONObject tag = tagQueryService.getTagByTitle(tagTitle);

        if (null == tag) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, langPropsService.get("invalidTagLabel"));

            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        final JSONObject domainTag = new JSONObject();
        domainTag.put(Domain.DOMAIN + "_" + Keys.OBJECT_ID, domainId);
        domainTag.put(Tag.TAG + "_" + Keys.OBJECT_ID, tag.optString(Keys.OBJECT_ID));

        domainMgmtService.removeDomainTag(domainId, tag.optString(Keys.OBJECT_ID));

        response.sendRedirect(Latkes.getServePath() + "/admin/domain/" + domainId);
    }

    /**
     * Search index.
     *
     * @param context the specified context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/search/index", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void searchIndex(final HTTPRequestContext context) throws Exception {
        context.renderJSON(true);

        if (Symphonys.getBoolean("es.enabled")) {
            searchMgmtService.rebuildESIndex();
        }

        if (Symphonys.getBoolean("algolia.enabled")) {
            searchMgmtService.rebuildAlgoliaIndex();
        }

        final JSONObject stat = optionQueryService.getStatistic();
        final int articleCount = stat.optInt(Option.ID_C_STATISTIC_ARTICLE_COUNT);
        final int pages = (int) Math.ceil((double) articleCount / 50.0);

        for (int pageNum = 1; pageNum <= pages; pageNum++) {
            final List<JSONObject> articles = articleQueryService.getValidArticles(pageNum, 50, Article.ARTICLE_TYPE_C_NORMAL, Article.ARTICLE_TYPE_C_CITY_BROADCAST);

            for (final JSONObject article : articles) {
                if (Symphonys.getBoolean("algolia.enabled")) {
                    searchMgmtService.updateAlgoliaDocument(article);
                }

                if (Symphonys.getBoolean("es.enabled")) {
                    searchMgmtService.updateESDocument(article, Article.ARTICLE);
                }
            }

            LOGGER.info("Indexed page [" + pageNum + "]");
        }

        LOGGER.info("Index finished");
    }

    /**
     * Search index one article.
     *
     * @param context the specified context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/search-index-article", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void searchIndexArticle(final HTTPRequestContext context) throws Exception {
        final String articleId = context.getRequest().getParameter(Article.ARTICLE_T_ID);
        final JSONObject article = articleQueryService.getArticle(articleId);

        if (null == article || Article.ARTICLE_TYPE_C_DISCUSSION == article.optInt(Article.ARTICLE_TYPE)
                || Article.ARTICLE_TYPE_C_THOUGHT == article.optInt(Article.ARTICLE_TYPE)) {
            return;
        }

        if (Symphonys.getBoolean("algolia.enabled")) {
            searchMgmtService.updateAlgoliaDocument(article);
        }

        if (Symphonys.getBoolean("es.enabled")) {
            searchMgmtService.updateESDocument(article, Article.ARTICLE);
        }

        context.getResponse().sendRedirect(Latkes.getServePath() + "/admin/articles");
    }
}
