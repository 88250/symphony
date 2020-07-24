/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.http.Dispatcher;
import org.b3log.latke.http.Request;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.Response;
import org.b3log.latke.http.function.Handler;
import org.b3log.latke.http.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.event.ArticleBaiduSender;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.middleware.PermissionMidware;
import org.b3log.symphony.processor.middleware.validate.UserRegister2ValidationMidware;
import org.b3log.symphony.processor.middleware.validate.UserRegisterValidationMidware;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.Escapes;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.StatusCodes;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.*;

/**
 * Admin processor.
 * <ul>
 * <li>Shows admin index (/admin), GET</li>
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
 * <li>Show comments (/admin/comments), GET</li>
 * <li>Shows a comment (/admin/comment/{commentId}), GET</li>
 * <li>Updates a comment (/admin/comment/{commentId}), POST</li>
 * <li>Removes a comment (/admin/remove-comment), POST</li>
 * <li>Show breezemoons (/admin/breezemoons), GET</li>
 * <li>Shows a breezemoon (/admin/breezemoon/{breezemoonId}), GET</li>
 * <li>Updates a breezemoon (/admin/breezemoon/{breezemoonId}), POST</li>
 * <li>Removes a breezemoon (/admin/remove-breezemoon), POST</li>
 * <li>Shows domains (/admin/domains, GET</li>
 * <li>Show a domain (/admin/domain/{domainId}, GET</li>
 * <li>Updates a domain (/admin/domain/{domainId}), POST</li>
 * <li>Shows tags (/admin/tags), GET</li>
 * <li>Removes unused tags (/admin/tags/remove-unused), POST</li>
 * <li>Show a tag (/admin/tag/{tagId}), GET</li>
 * <li>Shows add tag (/admin/add-tag), GET</li>
 * <li>Adds a tag (/admin/add-tag), POST</li>
 * <li>Updates a tag (/admin/tag/{tagId}), POST</li>
 * <li>Generates invitecodes (/admin/invitecodes/generate), POST</li>
 * <li>Shows invitecodes (/admin/invitecodes), GET</li>
 * <li>Show an invitecode (/admin/invitecode/{invitecodeId}), GET</li>
 * <li>Updates an invitecode (/admin/invitecode/{invitecodeId}), POST</li>
 * <li>Shows miscellaneous (/admin/misc), GET</li>
 * <li>Updates miscellaneous (/admin/misc), POST</li>
 * <li>Rebuilds article search index (/admin/search/index), POST</li>
 * <li>Rebuilds one article search index(/admin/search-index-article), POST</li>
 * <li>Shows ad (/admin/ad), GET</li>
 * <li>Updates ad (/admin/ad), POST</li>
 * <li>Shows role permissions (/admin/role/{roleId}/permissions), GET</li>
 * <li>Updates role permissions (/admin/role/{roleId}/permissions), POST</li>
 * <li>Removes an role (/admin/role/{roleId}/remove), POST</li>
 * <li>Adds an role (/admin/role), POST</li>
 * <li>Show reports (/admin/reports), GET</li>
 * <li>Makes a report as handled (/admin/report/{reportId}), GET</li>
 * <li>Shows audit log (/admin/auditlog), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author Bill Ho
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="https://qiankunpingtai.cn">qiankunpingtai</a>
 * @version 3.0.0.1, May 16, 2020
 * @since 1.1.0
 */
@Singleton
public class AdminProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(AdminProcessor.class);

    /**
     * Pagination window size.
     */
    private static final int WINDOW_SIZE = 15;

    /**
     * Pagination page size.
     */
    private static final int PAGE_SIZE = 60;

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
     * Role query service.
     */
    @Inject
    private RoleQueryService roleQueryService;

    /**
     * Role management service.
     */
    @Inject
    private RoleMgmtService roleMgmtService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Breezemoon query service.
     */
    @Inject
    private BreezemoonQueryService breezemoonQueryService;

    /**
     * Breezemoon management service.
     */
    @Inject
    private BreezemoonMgmtService breezemoonMgmtService;

    /**
     * Report management service.
     */
    @Inject
    private ReportMgmtService reportMgmtService;

    /**
     * Report query service.
     */
    @Inject
    private ReportQueryService reportQueryService;

    /**
     * Operation management service.
     */
    @Inject
    private OperationMgmtService operationMgmtService;

    /**
     * Operation query service.
     */
    @Inject
    private OperationQueryService operationQueryService;

    /**
     * Register request handlers.
     */
    public static void register() {
        final BeanManager beanManager = BeanManager.getInstance();
        final AdminProcessor adminProcessor = beanManager.getReference(AdminProcessor.class);
        final PermissionMidware permissionMidware = beanManager.getReference(PermissionMidware.class);
        final Handler[] middlewares = new Handler[]{permissionMidware::check};

        Dispatcher.get("/admin/auditlog", adminProcessor::showAuditlog, middlewares);
        Dispatcher.get("/admin/report/ignore/{reportId}", adminProcessor::makeReportIgnored, middlewares);
        Dispatcher.get("/admin/report/{reportId}", adminProcessor::makeReportHandled, middlewares);
        Dispatcher.get("/admin/reports", adminProcessor::showReports, middlewares);
        Dispatcher.post("/admin/role/{roleId}/remove", adminProcessor::removeRole, middlewares);
        Dispatcher.get("/admin/breezemoons", adminProcessor::showBreezemoons, middlewares);
        Dispatcher.get("/admin/breezemoon/{breezemoonId}", adminProcessor::showBreezemoon, middlewares);
        Dispatcher.post("/admin/breezemoon/{breezemoonId}", adminProcessor::updateBreezemoon, middlewares);
        Dispatcher.post("/admin/remove-breezemoon", adminProcessor::removeBreezemoon, middlewares);
        Dispatcher.post("/admin/tags/remove-unused", adminProcessor::removeUnusedTags, middlewares);
        Dispatcher.post("/admin/role", adminProcessor::addRole, middlewares);
        Dispatcher.post("/admin/role/{roleId}/permissions", adminProcessor::updateRolePermissions, middlewares);
        Dispatcher.get("/admin/role/{roleId}/permissions", adminProcessor::showRolePermissions, middlewares);
        Dispatcher.get("/admin/roles", adminProcessor::showRoles, middlewares);
        Dispatcher.post("/admin/ad/side", adminProcessor::updateSideAd, middlewares);
        Dispatcher.post("/admin/ad/banner", adminProcessor::updateBanner, middlewares);
        Dispatcher.get("/admin/ad", adminProcessor::showAd, middlewares);
        Dispatcher.get("/admin/add-tag", adminProcessor::showAddTag, middlewares);
        Dispatcher.post("/admin/add-tag", adminProcessor::addTag, middlewares);
        Dispatcher.post("/admin/stick-article", adminProcessor::stickArticle, middlewares);
        Dispatcher.post("/admin/cancel-stick-article", adminProcessor::stickCancelArticle, middlewares);
        Dispatcher.post("/admin/invitecodes/generate", adminProcessor::generateInvitecodes, middlewares);
        Dispatcher.get("/admin/invitecodes", adminProcessor::showInvitecodes, middlewares);
        Dispatcher.get("/admin/invitecode/{invitecodeId}", adminProcessor::showInvitecode, middlewares);
        Dispatcher.post("/admin/invitecode/{invitecodeId}", adminProcessor::updateInvitecode, middlewares);
        Dispatcher.get("/admin/add-article", adminProcessor::showAddArticle, middlewares);
        Dispatcher.post("/admin/add-article", adminProcessor::addArticle, middlewares);
        Dispatcher.post("/admin/add-reserved-word", adminProcessor::addReservedWord, middlewares);
        Dispatcher.get("/admin/add-reserved-word", adminProcessor::showAddReservedWord, middlewares);
        Dispatcher.post("/admin/reserved-word/{id}", adminProcessor::updateReservedWord, middlewares);
        Dispatcher.get("/admin/reserved-words", adminProcessor::showReservedWords, middlewares);
        Dispatcher.get("/admin/reserved-word/{id}", adminProcessor::showReservedWord, middlewares);
        Dispatcher.post("/admin/remove-reserved-word", adminProcessor::removeReservedWord, middlewares);
        Dispatcher.post("/admin/remove-comment", adminProcessor::removeComment, middlewares);
        Dispatcher.post("/admin/remove-article", adminProcessor::removeArticle, middlewares);
        Dispatcher.get("/admin", adminProcessor::showAdminIndex, middlewares);
        Dispatcher.get("/admin/users", adminProcessor::showUsers, middlewares);
        Dispatcher.get("/admin/user/{userId}", adminProcessor::showUser, middlewares);
        Dispatcher.get("/admin/add-user", adminProcessor::showAddUser, middlewares);
        Dispatcher.post("/admin/add-user", adminProcessor::addUser, middlewares);
        Dispatcher.post("/admin/user/{userId}", adminProcessor::updateUser, middlewares);
        Dispatcher.post("/admin/user/{userId}/email", adminProcessor::updateUserEmail, middlewares);
        Dispatcher.post("/admin/user/{userId}/username", adminProcessor::updateUserName, middlewares);
        Dispatcher.post("/admin/user/{userId}/charge-point", adminProcessor::chargePoint, middlewares);
        Dispatcher.post("/admin/user/{userId}/abuse-point", adminProcessor::abusePoint, middlewares);
        Dispatcher.post("/admin/user/{userId}/init-point", adminProcessor::initPoint, middlewares);
        Dispatcher.post("/admin/user/{userId}/exchange-point", adminProcessor::exchangePoint, middlewares);
        Dispatcher.get("/admin/articles", adminProcessor::showArticles, middlewares);
        Dispatcher.get("/admin/article/{articleId}", adminProcessor::showArticle, middlewares);
        Dispatcher.post("/admin/article/{articleId}", adminProcessor::updateArticle, middlewares);
        Dispatcher.get("/admin/comments", adminProcessor::showComments, middlewares);
        Dispatcher.get("/admin/comment/{commentId}", adminProcessor::showComment, middlewares);
        Dispatcher.post("/admin/comment/{commentId}", adminProcessor::updateComment, middlewares);
        Dispatcher.get("/admin/misc", adminProcessor::showMisc, middlewares);
        Dispatcher.post("/admin/misc", adminProcessor::updateMisc, middlewares);
        Dispatcher.get("/admin/tags", adminProcessor::showTags, middlewares);
        Dispatcher.get("/admin/tag/{tagId}", adminProcessor::showTag, middlewares);
        Dispatcher.post("/admin/tag/{tagId}", adminProcessor::updateTag, middlewares);
        Dispatcher.get("/admin/domains", adminProcessor::showDomains, middlewares);
        Dispatcher.get("/admin/domain/{domainId}", adminProcessor::showDomain, middlewares);
        Dispatcher.post("/admin/domain/{domainId}", adminProcessor::updateDomain, middlewares);
        Dispatcher.get("/admin/add-domain", adminProcessor::showAddDomain, middlewares);
        Dispatcher.post("/admin/add-domain", adminProcessor::addDomain, middlewares);
        Dispatcher.post("/admin/remove-domain", adminProcessor::removeDomain, middlewares);
        Dispatcher.post("/admin/domain/{domainId}/add-tag", adminProcessor::addDomainTag, middlewares);
        Dispatcher.post("/admin/domain/{domainId}/remove-tag", adminProcessor::removeDomainTag, middlewares);
        Dispatcher.post("/admin/search/index", adminProcessor::rebuildArticleSearchIndex, middlewares);
        Dispatcher.post("/admin/search-index-article", adminProcessor::rebuildOneArticleSearchIndex, middlewares);
    }

    /**
     * Shows audit log.
     *
     * @param context the specified context
     */
    public void showAuditlog(final RequestContext context) {
        final Request request = context.getRequest();
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/auditlog.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final JSONObject result = operationQueryService.getAuditlogs(requestJSONObject);
        dataModel.put(Operation.OPERATIONS, result.opt(Operation.OPERATIONS));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Makes a report as ignored .
     *
     * @param context the specified context
     */
    public void makeReportIgnored(final RequestContext context) {
        final String reportId = context.pathVar("reportId");
        final Request request = context.getRequest();
        reportMgmtService.makeReportIgnored(reportId);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_MAKE_REPORT_IGNORED, reportId));

        context.sendRedirect(Latkes.getServePath() + "/admin/reports");
    }

    /**
     * Makes a report as handled .
     *
     * @param context the specified context
     */
    public void makeReportHandled(final RequestContext context) {
        final String reportId = context.pathVar("reportId");
        final Request request = context.getRequest();
        reportMgmtService.makeReportHandled(reportId);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_MAKE_REPORT_HANDLED, reportId));

        context.sendRedirect(Latkes.getServePath() + "/admin/reports");
    }

    /**
     * Shows reports.
     *
     * @param context the specified context
     */
    public void showReports(final RequestContext context) {
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/reports.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final JSONObject result = reportQueryService.getReports(requestJSONObject);
        dataModel.put(Report.REPORTS, result.opt(Report.REPORTS));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Removes an role.
     *
     * @param context the specified context
     */
    public void removeRole(final RequestContext context) {
        final String roleId = context.pathVar("roleId");
        final Request request = context.getRequest();

        final int count = roleQueryService.countUser(roleId);
        if (0 < count) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.put(Keys.MSG, "Still [" + count + "] users are using this role.");
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        final JSONObject role = roleQueryService.getRole(roleId);
        final String roleName = role.optString(Role.ROLE_NAME);
        roleMgmtService.removeRole(roleId);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_REMOVE_ROLE, roleName));

        context.sendRedirect(Latkes.getServePath() + "/admin/roles");
    }

    /**
     * Show admin breezemoons.
     *
     * @param context the specified context
     */
    public void showBreezemoons(final RequestContext context) {
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/breezemoons.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final List<String> fields = new ArrayList<>();
        fields.add(Keys.OBJECT_ID);
        fields.add(Breezemoon.BREEZEMOON_CONTENT);
        fields.add(Breezemoon.BREEZEMOON_CREATED);
        fields.add(Breezemoon.BREEZEMOON_AUTHOR_ID);
        fields.add(Breezemoon.BREEZEMOON_STATUS);
        final JSONObject result = breezemoonQueryService.getBreezemoons(requestJSONObject, fields);
        dataModel.put(Breezemoon.BREEZEMOONS, result.opt(Breezemoon.BREEZEMOONS));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows a breezemoon.
     *
     * @param context the specified context
     */
    public void showBreezemoon(final RequestContext context) {
        final String breezemoonId = context.pathVar("breezemoonId");

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/breezemoon.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject breezemoon = breezemoonQueryService.getBreezemoon(breezemoonId);
        Escapes.escapeHTML(breezemoon);
        dataModel.put(Breezemoon.BREEZEMOON, breezemoon);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Updates a breezemoon.
     *
     * @param context the specified context
     */
    public void updateBreezemoon(final RequestContext context) {
        final String breezemoonId = context.pathVar("breezemoonId");
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/breezemoon.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject breezemoon = breezemoonQueryService.getBreezemoon(breezemoonId);

        final Iterator<String> parameterNames = request.getParameterNames().iterator();
        while (parameterNames.hasNext()) {
            final String name = parameterNames.next();
            final String value = context.param(name);

            if (name.equals(Breezemoon.BREEZEMOON_STATUS)) {
                breezemoon.put(name, Integer.valueOf(value));
            } else {
                breezemoon.put(name, value);
            }
        }

        try {
            breezemoonMgmtService.updateBreezemoon(breezemoon);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_BREEZEMOON, breezemoonId));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Updates a breezemoon failed", e);
            return;
        }

        breezemoon = breezemoonQueryService.getBreezemoon(breezemoonId);
        dataModel.put(Breezemoon.BREEZEMOON, breezemoon);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Removes a breezemoon.
     *
     * @param context the specified context
     */
    public void removeBreezemoon(final RequestContext context) {
        final Request request = context.getRequest();
        final String id = context.param(Common.ID);

        try {
            breezemoonMgmtService.removeBreezemoon(id);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_REMOVE_BREEZEMOON, id));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Removes a breezemoon failed", e);
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/breezemoons");
    }

    /**
     * Removes unused tags.
     *
     * @param context the specified context
     */
    public void removeUnusedTags(final RequestContext context) {
        context.renderJSON(StatusCodes.SUCC);
        tagMgmtService.removeUnusedTags();
        operationMgmtService.addOperation(Operation.newOperation(context.getRequest(), Operation.OPERATION_CODE_C_REMOVE_UNUSED_TAGS, ""));
    }

    /**
     * Adds an role.
     *
     * @param context the specified context
     */
    public void addRole(final RequestContext context) {
        final Request request = context.getRequest();
        final String roleName = context.param(Role.ROLE_NAME);
        if (StringUtils.isBlank(roleName)) {
            context.sendRedirect(Latkes.getServePath() + "/admin/roles");
            return;
        }

        final String roleDesc = context.param(Role.ROLE_DESCRIPTION);

        final JSONObject role = new JSONObject();
        role.put(Role.ROLE_NAME, roleName);
        role.put(Role.ROLE_DESCRIPTION, roleDesc);

        roleMgmtService.addRole(role);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERAIONT_CODE_C_ADD_ROLE, roleName));

        context.sendRedirect(Latkes.getServePath() + "/admin/roles");
    }

    /**
     * Updates role permissions.
     *
     * @param context the specified context
     */
    public void updateRolePermissions(final RequestContext context) {
        final String roleId = context.pathVar("roleId");
        final Request request = context.getRequest();

        final Set<String> permissionIds = request.getParameterNames();

        roleMgmtService.updateRolePermissions(roleId, permissionIds);
        final JSONObject role = roleQueryService.getRole(roleId);
        final String roleName = role.optString(Role.ROLE_NAME);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_ROLE_PERMS, roleName));

        context.sendRedirect(Latkes.getServePath() + "/admin/role/" + roleId + "/permissions");
    }

    /**
     * Shows role permissions.
     *
     * @param context the specified context
     */
    public void showRolePermissions(final RequestContext context) {
        final String roleId = context.pathVar("roleId");

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/role-permissions.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject role = roleQueryService.getRole(roleId);
        dataModel.put(Role.ROLE, role);

        final Map<String, List<JSONObject>> categories = new TreeMap<>();

        final List<JSONObject> permissions = roleQueryService.getPermissionsGrant(roleId);
        for (final JSONObject permission : permissions) {
            final String label = permission.optString(Keys.OBJECT_ID) + "PermissionLabel";
            permission.put(Permission.PERMISSION_T_LABEL, langPropsService.get(label));

            String category = permission.optString(Permission.PERMISSION_CATEGORY);
            category = langPropsService.get(category + "PermissionLabel");

            final List<JSONObject> categoryPermissions = categories.computeIfAbsent(category, k -> new ArrayList<>());
            categoryPermissions.add(permission);
        }

        dataModel.put(Permission.PERMISSION_T_CATEGORIES, categories);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows roles.
     *
     * @param context the specified context
     */
    public void showRoles(final RequestContext context) {

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/roles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject result = roleQueryService.getRoles(1, Integer.MAX_VALUE, 10);
        final List<JSONObject> roles = (List<JSONObject>) result.opt(Role.ROLES);

        dataModel.put(Role.ROLES, roles);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Updates side ad.
     *
     * @param context the specified context
     */
    public void updateSideAd(final RequestContext context) {
        final Request request = context.getRequest();
        final String sideFullAd = context.param("sideFullAd");

        JSONObject adOption = optionQueryService.getOption(Option.ID_C_SIDE_FULL_AD);
        if (null == adOption) {
            adOption = new JSONObject();
            adOption.put(Keys.OBJECT_ID, Option.ID_C_SIDE_FULL_AD);
            adOption.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_AD);
            adOption.put(Option.OPTION_VALUE, sideFullAd);
            optionMgmtService.addOption(adOption);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_ADD_AD_POS, Option.ID_C_SIDE_FULL_AD));
        } else {
            adOption.put(Option.OPTION_VALUE, sideFullAd);
            optionMgmtService.updateOption(Option.ID_C_SIDE_FULL_AD, adOption);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_AD_POS, Option.ID_C_SIDE_FULL_AD));
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/ad");
    }

    /**
     * Updates banner.
     *
     * @param context the specified context
     */
    public void updateBanner(final RequestContext context) {
        final Request request = context.getRequest();
        final String headerBanner = context.param("headerBanner");

        JSONObject adOption = optionQueryService.getOption(Option.ID_C_HEADER_BANNER);
        if (null == adOption) {
            adOption = new JSONObject();
            adOption.put(Keys.OBJECT_ID, Option.ID_C_HEADER_BANNER);
            adOption.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_AD);
            adOption.put(Option.OPTION_VALUE, headerBanner);
            optionMgmtService.addOption(adOption);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_ADD_AD_POS, Option.ID_C_HEADER_BANNER));
        } else {
            adOption.put(Option.OPTION_VALUE, headerBanner);
            optionMgmtService.updateOption(Option.ID_C_HEADER_BANNER, adOption);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_AD_POS, Option.ID_C_HEADER_BANNER));
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/ad");
    }

    /**
     * Shows ad.
     *
     * @param context the specified context
     */
    public void showAd(final RequestContext context) {

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/ad.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModel.put("sideFullAd", "");
        dataModel.put("headerBanner", "");

        final JSONObject sideAdOption = optionQueryService.getOption(Option.ID_C_SIDE_FULL_AD);
        if (null != sideAdOption) {
            dataModel.put("sideFullAd", sideAdOption.optString(Option.OPTION_VALUE));
        }

        final JSONObject headerBanner = optionQueryService.getOption(Option.ID_C_HEADER_BANNER);
        if (null != headerBanner) {
            dataModel.put("headerBanner", headerBanner.optString(Option.OPTION_VALUE));
        }

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows add tag.
     *
     * @param context the specified context
     */
    public void showAddTag(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/add-tag.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Adds a tag.
     *
     * @param context the specified context
     */
    public void addTag(final RequestContext context) {
        final Request request = context.getRequest();

        String title = StringUtils.trim(context.param(Tag.TAG_TITLE));
        try {
            if (StringUtils.isBlank(title)) {
                throw new Exception(langPropsService.get("tagsErrorLabel"));
            }

            title = Tag.formatTags(title);

            if (!Tag.containsWhiteListTags(title)) {
                if (!Tag.TAG_TITLE_PATTERN.matcher(title).matches()) {
                    throw new Exception(langPropsService.get("tagsErrorLabel"));
                }

                if (title.length() > Tag.MAX_TAG_TITLE_LENGTH) {
                    throw new Exception(langPropsService.get("tagsErrorLabel"));
                }
            }
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());

            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        final JSONObject admin = Sessions.getUser();
        final String userId = admin.optString(Keys.OBJECT_ID);

        String tagId;
        try {
            tagId = tagMgmtService.addTag(userId, title);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_ADD_TAG, title));
        } catch (final ServiceException e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/tag/" + tagId);
    }

    /**
     * Sticks an article.
     *
     * @param context the specified context
     */
    public void stickArticle(final RequestContext context) {
        final Request request = context.getRequest();

        final String articleId = context.param(Article.ARTICLE_T_ID);
        articleMgmtService.adminStick(articleId);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_STICK_ARTICLE, articleId));
        context.sendRedirect(Latkes.getServePath() + "/admin/articles");
    }

    /**
     * Cancels stick an article.
     *
     * @param context the specified context
     */
    public void stickCancelArticle(final RequestContext context) {
        final Request request = context.getRequest();

        final String articleId = context.param(Article.ARTICLE_T_ID);
        articleMgmtService.adminCancelStick(articleId);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_CANCEL_STICK_ARTICLE, articleId));
        context.sendRedirect(Latkes.getServePath() + "/admin/articles");
    }

    /**
     * Generates invitecodes.
     *
     * @param context the specified context
     */
    public void generateInvitecodes(final RequestContext context) {
        final Request request = context.getRequest();

        final String quantityStr = context.param("quantity");
        int quantity = 20;
        try {
            quantity = Integer.valueOf(quantityStr);
        } catch (final NumberFormatException e) {
            // ignore
        }

        String memo = context.param("memo");
        if (StringUtils.isBlank(memo)) {
            memo = "注册帖";
        }

        invitecodeMgmtService.adminGenInvitecodes(quantity, memo);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_GENERATE_INVITECODES, quantity + " " + memo));

        context.sendRedirect(Latkes.getServePath() + "/admin/invitecodes");
    }

    /**
     * Shows admin invitecodes.
     *
     * @param context the specified context
     */
    public void showInvitecodes(final RequestContext context) {
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/invitecodes.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final JSONObject result = invitecodeQueryService.getInvitecodes(requestJSONObject);
        final List<JSONObject> invitecodes = (List<JSONObject>) result.opt(Invitecode.INVITECODES);
        invitecodes.forEach(Escapes::escapeHTML);
        dataModel.put(Invitecode.INVITECODES, invitecodes);

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows an invitecode.
     *
     * @param context the specified context
     */
    public void showInvitecode(final RequestContext context) {
        final String invitecodeId = context.pathVar("invitecodeId");
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/invitecode.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject invitecode = invitecodeQueryService.getInvitecodeById(invitecodeId);
        dataModel.put(Invitecode.INVITECODE, invitecode);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Updates an invitecode.
     *
     * @param context the specified context
     */
    public void updateInvitecode(final RequestContext context) {
        final String invitecodeId = context.pathVar("invitecodeId");
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/invitecode.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject invitecode = invitecodeQueryService.getInvitecodeById(invitecodeId);

        final Iterator<String> parameterNames = request.getParameterNames().iterator();
        while (parameterNames.hasNext()) {
            final String name = parameterNames.next();
            final String value = context.param(name);

            invitecode.put(name, value);
        }

        try {
            invitecodeMgmtService.updateInvitecode(invitecodeId, invitecode);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_INVITECODE, invitecodeId));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Updates an invitecode failed", e);
            return;
        }

        invitecode = invitecodeQueryService.getInvitecodeById(invitecodeId);
        dataModel.put(Invitecode.INVITECODE, invitecode);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows add article.
     *
     * @param context the specified context
     */
    public void showAddArticle(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/add-article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Adds an article.
     *
     * @param context the specified context
     */
    public void addArticle(final RequestContext context) {
        final Request request = context.getRequest();

        final String userName = context.param(User.USER_NAME);
        final JSONObject author = userQueryService.getUserByName(userName);
        if (null == author) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.put(Keys.MSG, langPropsService.get("notFoundUserLabel"));
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        final String timeStr = context.param(Common.TIME);
        final String articleTitle = context.param(Article.ARTICLE_TITLE);
        final String articleTags = context.param(Article.ARTICLE_TAGS);
        final String articleContent = context.param(Article.ARTICLE_CONTENT);
        String rewardContent = context.param(Article.ARTICLE_REWARD_CONTENT);
        final String rewardPoint = context.param(Article.ARTICLE_REWARD_POINT);
        final int articleShowInList = Article.ARTICLE_SHOW_IN_LIST_C_YES;
        long time = System.currentTimeMillis();

        try {
            final Date date = DateUtils.parseDate(timeStr, new String[]{"yyyy-MM-dd'T'HH:mm"});

            time = date.getTime();
            final int random = RandomUtils.nextInt(9999);
            time += random;
        } catch (final ParseException e) {
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
        article.put(Article.ARTICLE_SHOW_IN_LIST, articleShowInList);
        try {
            final String articleId = articleMgmtService.addArticleByAdmin(article);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_ADD_ARTICLE, articleId));
        } catch (final ServiceException e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/articles");
    }

    /**
     * Adds a reserved word.
     *
     * @param context the specified context
     */
    public void addReservedWord(final RequestContext context) {
        final Request request = context.getRequest();

        String word = context.param(Common.WORD);
        word = StringUtils.trim(word);
        if (StringUtils.isBlank(word)) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.put(Keys.MSG, langPropsService.get("invalidReservedWordLabel"));
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        if (optionQueryService.isReservedWord(word)) {
            context.sendRedirect(Latkes.getServePath() + "/admin/reserved-words");
            return;
        }

        try {
            final JSONObject reservedWord = new JSONObject();
            reservedWord.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_RESERVED_WORDS);
            reservedWord.put(Option.OPTION_VALUE, word);

            optionMgmtService.addOption(reservedWord);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_ADD_RESERVED_WORD, word));
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/reserved-words");
    }

    /**
     * Shows add reserved word.
     *
     * @param context the specified context
     */
    public void showAddReservedWord(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/add-reserved-word.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Updates a reserved word.
     *
     * @param context the specified context
     */
    public void updateReservedWord(final RequestContext context) {
        final String id = context.pathVar("id");
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/reserved-word.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject word = optionQueryService.getOption(id);
        dataModel.put(Common.WORD, word);

        final Iterator<String> parameterNames = request.getParameterNames().iterator();
        while (parameterNames.hasNext()) {
            final String name = parameterNames.next();
            final String value = context.param(name);

            word.put(name, value);
        }

        optionMgmtService.updateOption(id, word);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_RESERVED_WORD, word.optString(Option.OPTION_VALUE)));

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows reserved words.
     *
     * @param context the specified context
     */
    public void showReservedWords(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/reserved-words.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final List<JSONObject> words = optionQueryService.getReservedWords();
        words.forEach(Escapes::escapeHTML);
        dataModel.put(Common.WORDS, words);
        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows a reserved word.
     *
     * @param context the specified context
     */
    public void showReservedWord(final RequestContext context) {
        final String id = context.pathVar("id");
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/reserved-word.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final JSONObject word = optionQueryService.getOption(id);
        dataModel.put(Common.WORD, word);
        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Removes a reserved word.
     *
     * @param context the specified context
     */
    public void removeReservedWord(final RequestContext context) {
        final Request request = context.getRequest();

        final String id = context.param("id");
        final JSONObject option = optionQueryService.getOption(id);
        final String word = option.optString(Option.OPTION_VALUE);
        optionMgmtService.removeOption(id);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_REMOVE_RESERVED_WORD, word));

        context.sendRedirect(Latkes.getServePath() + "/admin/reserved-words");
    }

    /**
     * Removes a comment.
     *
     * @param context the specified context
     */
    public void removeComment(final RequestContext context) {
        final Request request = context.getRequest();

        final String commentId = context.param(Comment.COMMENT_T_ID);
        commentMgmtService.removeCommentByAdmin(commentId);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_REMOVE_COMMENT, commentId));

        context.sendRedirect(Latkes.getServePath() + "/admin/comments");
    }

    /**
     * Removes an article.
     *
     * @param context the specified context
     */
    public void removeArticle(final RequestContext context) {
        final Request request = context.getRequest();

        final String articleId = context.param(Article.ARTICLE_T_ID);
        articleMgmtService.removeArticleByAdmin(articleId);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_REMOVE_ARTICLE, articleId));

        context.sendRedirect(Latkes.getServePath() + "/admin/articles");
    }

    /**
     * Shows admin index.
     *
     * @param context the specified context
     */
    public void showAdminIndex(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/index.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModelService.fillHeaderAndFooter(context, dataModel);

        dataModel.put(Common.ONLINE_VISITOR_CNT, optionQueryService.getOnlineVisitorCount());
        dataModel.put(Common.ONLINE_MEMBER_CNT, optionQueryService.getOnlineMemberCount());

        final JSONObject statistic = optionQueryService.getStatistic();
        dataModel.put(Option.CATEGORY_C_STATISTIC, statistic);
    }

    /**
     * Shows admin users.
     *
     * @param context the specified context
     */
    public void showUsers(final RequestContext context) {
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/users.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);
        final String query = context.param(Common.QUERY);
        if (StringUtils.isNotBlank(query)) {
            requestJSONObject.put(Common.QUERY, query);
        }

        final JSONObject result = userQueryService.getUsers(requestJSONObject);
        dataModel.put(User.USERS, result.opt(User.USERS));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows a user.
     *
     * @param context the specified context
     */
    public void showUser(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/user.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = userQueryService.getUser(userId);
        Escapes.escapeHTML(user);
        dataModel.put(User.USER, user);

        final JSONObject result = roleQueryService.getRoles(1, Integer.MAX_VALUE, 10);
        final List<JSONObject> roles = (List<JSONObject>) result.opt(Role.ROLES);
        dataModel.put(Role.ROLES, roles);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows add user.
     *
     * @param context the specified context
     */
    public void showAddUser(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/add-user.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Adds a user.
     *
     * @param context the specified context
     */
    public void addUser(final RequestContext context) {
        final Request request = context.getRequest();

        final String userName = context.param(User.USER_NAME);
        final String email = context.param(User.USER_EMAIL);
        final String password = context.param(User.USER_PASSWORD);
        final String appRole = context.param(UserExt.USER_APP_ROLE);

        final boolean nameInvalid = UserRegisterValidationMidware.invalidUserName(userName);
        final boolean emailInvalid = !Strings.isEmail(email);
        final boolean passwordInvalid = UserRegister2ValidationMidware.invalidUserPassword(password);

        if (nameInvalid || emailInvalid || passwordInvalid) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            if (nameInvalid) {
                dataModel.put(Keys.MSG, langPropsService.get("invalidUserNameLabel"));
            } else if (emailInvalid) {
                dataModel.put(Keys.MSG, langPropsService.get("invalidEmailLabel"));
            } else if (passwordInvalid) {
                dataModel.put(Keys.MSG, langPropsService.get("invalidPasswordLabel"));
            }

            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        String userId;
        try {
            final JSONObject user = new JSONObject();
            user.put(User.USER_NAME, userName);
            user.put(User.USER_EMAIL, email);
            user.put(User.USER_PASSWORD, DigestUtils.md5Hex(password));
            user.put(UserExt.USER_APP_ROLE, appRole);
            user.put(UserExt.USER_STATUS, UserExt.USER_STATUS_C_VALID);

            final JSONObject admin = Sessions.getUser();
            user.put(UserExt.USER_LANGUAGE, admin.optString(UserExt.USER_LANGUAGE));

            userId = userMgmtService.addUser(user);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_ADD_USER, userId));
        } catch (final ServiceException e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Updates a user.
     *
     * @param context the specified context
     */
    public void updateUser(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/user.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = userQueryService.getUser(userId);
        dataModel.put(User.USER, user);
        final String oldRole = user.optString(User.USER_ROLE);

        final JSONObject result = roleQueryService.getRoles(1, Integer.MAX_VALUE, 10);
        final List<JSONObject> roles = (List<JSONObject>) result.opt(Role.ROLES);
        dataModel.put(Role.ROLES, roles);

        final Iterator<String> parameterNames = request.getParameterNames().iterator();
        while (parameterNames.hasNext()) {
            final String name = parameterNames.next();
            final String value = context.param(name);

            switch (name) {
                case UserExt.USER_POINT:
                case UserExt.USER_APP_ROLE:
                case UserExt.USER_STATUS:
                case UserExt.USER_COMMENT_VIEW_MODE:
                case UserExt.USER_AVATAR_VIEW_MODE:
                case UserExt.USER_LIST_PAGE_SIZE:
                case UserExt.USER_LIST_VIEW_MODE:
                case UserExt.USER_NOTIFY_STATUS:
                case UserExt.USER_SUB_MAIL_STATUS:
                case UserExt.USER_KEYBOARD_SHORTCUTS_STATUS:
                case UserExt.USER_REPLY_WATCH_ARTICLE_STATUS:
                case UserExt.USER_GEO_STATUS:
                case UserExt.USER_ARTICLE_STATUS:
                case UserExt.USER_COMMENT_STATUS:
                case UserExt.USER_FOLLOWING_USER_STATUS:
                case UserExt.USER_FOLLOWING_TAG_STATUS:
                case UserExt.USER_FOLLOWING_ARTICLE_STATUS:
                case UserExt.USER_WATCHING_ARTICLE_STATUS:
                case UserExt.USER_BREEZEMOON_STATUS:
                case UserExt.USER_FOLLOWER_STATUS:
                case UserExt.USER_POINT_STATUS:
                case UserExt.USER_ONLINE_STATUS:
                case UserExt.USER_UA_STATUS:
                case UserExt.USER_JOIN_POINT_RANK:
                case UserExt.USER_JOIN_USED_POINT_RANK:
                case UserExt.USER_FORWARD_PAGE_STATUS:
                    user.put(name, Integer.valueOf(value));
                    break;
                case User.USER_PASSWORD:
                    final String oldPwd = user.getString(name);
                    if (!oldPwd.equals(value) && StringUtils.isNotBlank(value)) {
                        user.put(name, DigestUtils.md5Hex(value));
                    }
                    break;
                default:
                    user.put(name, value);
                    break;
            }
        }

        final JSONObject currentUser = Sessions.getUser();
        if (!Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
            user.put(User.USER_ROLE, oldRole);
        }

        try {
            userMgmtService.updateUser(userId, user);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_USER, userId));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Updates a user failed", e);
            return;
        }

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Updates a user's email.
     *
     * @param context the specified context
     */
    public void updateUserEmail(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final Request request = context.getRequest();

        final JSONObject user = userQueryService.getUser(userId);
        final String oldEmail = user.optString(User.USER_EMAIL);
        final String newEmail = context.param(User.USER_EMAIL);

        if (oldEmail.equals(newEmail)) {
            context.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
            return;
        }

        user.put(User.USER_EMAIL, newEmail);

        try {
            userMgmtService.updateUserEmail(userId, user);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_USER_EMAIL, userId));
        } catch (final ServiceException e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Updates a user's username.
     *
     * @param context the specified context
     */
    public void updateUserName(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final Request request = context.getRequest();

        final JSONObject user = userQueryService.getUser(userId);
        final String oldUserName = user.optString(User.USER_NAME);
        final String newUserName = context.param(User.USER_NAME);

        if (oldUserName.equals(newUserName)) {
            context.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
            return;
        }

        user.put(User.USER_NAME, newUserName);

        try {
            userMgmtService.updateUserName(userId, user);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_USER_NAME, userId));
        } catch (final ServiceException e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Charges a user's point.
     *
     * @param context the specified context
     */
    public void chargePoint(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final Request request = context.getRequest();

        final String pointStr = context.param(Common.POINT);
        final String memo = context.param("memo");

        if (StringUtils.isBlank(pointStr) || StringUtils.isBlank(memo) || !Strings.isNumeric(memo.split("-")[0])) {
            LOGGER.warn("Charge point memo format error");

            context.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
            return;
        }

        try {
            final int point = Integer.valueOf(pointStr);

            final String transferId = pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                    Pointtransfer.TRANSFER_TYPE_C_CHARGE, point, memo, System.currentTimeMillis(), "");
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_CHARGE_POINT, transferId));

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, userId);
            notification.put(Notification.NOTIFICATION_DATA_ID, transferId);
            notificationMgmtService.addPointChargeNotification(notification);
        } catch (final NumberFormatException | ServiceException e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Deducts a user's abuse point.
     *
     * @param context the specified context
     */
    public void abusePoint(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final Request request = context.getRequest();

        final String pointStr = context.param(Common.POINT);

        try {
            final int point = Integer.valueOf(pointStr);

            final JSONObject user = userQueryService.getUser(userId);
            final int currentPoint = user.optInt(UserExt.USER_POINT);

            if (currentPoint - point < 0) {
                final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
                final Map<String, Object> dataModel = renderer.getDataModel();
                dataModel.put(Keys.MSG, langPropsService.get("insufficientBalanceLabel"));
                dataModelService.fillHeaderAndFooter(context, dataModel);
                return;
            }

            final String memo = context.param(Common.MEMO);

            final String transferId = pointtransferMgmtService.transfer(userId, Pointtransfer.ID_C_SYS,
                    Pointtransfer.TRANSFER_TYPE_C_ABUSE_DEDUCT, point, memo, System.currentTimeMillis(), "");
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_DEDUCT_POINT, transferId));

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, userId);
            notification.put(Notification.NOTIFICATION_DATA_ID, transferId);
            notificationMgmtService.addAbusePointDeductNotification(notification);
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Compensates a user's initial point.
     *
     * @param context the specified context
     */
    public void initPoint(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final Request request = context.getRequest();
        final Response response = context.getResponse();

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
                final String transferId = pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId, Pointtransfer.TRANSFER_TYPE_C_INIT,
                        Pointtransfer.TRANSFER_SUM_C_INIT, userId, Long.valueOf(userId), "");
                operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_INIT_POINT, transferId));
            }
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Exchanges a user's point.
     *
     * @param context the specified context
     */
    public void exchangePoint(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final Request request = context.getRequest();
        final String pointStr = context.param(Common.POINT);

        try {
            final int point = Integer.valueOf(pointStr);

            final JSONObject user = userQueryService.getUser(userId);
            final int currentPoint = user.optInt(UserExt.USER_POINT);

            if (currentPoint - point < Symphonys.POINT_EXCHANGE_MIN) {
                final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
                final Map<String, Object> dataModel = renderer.getDataModel();

                dataModel.put(Keys.MSG, langPropsService.get("insufficientBalanceLabel"));
                dataModelService.fillHeaderAndFooter(context, dataModel);
                return;
            }

            final String memo = String.valueOf(Math.floor(point / (double) Symphonys.POINT_EXCHANGE_UNIT));

            final String transferId = pointtransferMgmtService.transfer(userId, Pointtransfer.ID_C_SYS,
                    Pointtransfer.TRANSFER_TYPE_C_EXCHANGE, point, memo, System.currentTimeMillis(), "");
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_EXCHANGE_POINT, transferId));

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, userId);
            notification.put(Notification.NOTIFICATION_DATA_ID, transferId);
            notificationMgmtService.addPointExchangeNotification(notification);
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Shows admin articles.
     *
     * @param context the specified context
     */
    public void showArticles(final RequestContext context) {
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String articleId = context.param("id");
        if (StringUtils.isNotBlank(articleId)) {
            requestJSONObject.put(Keys.OBJECT_ID, articleId);
        }

        final List<String> articleFields = new ArrayList<>();
        articleFields.add(Keys.OBJECT_ID);
        articleFields.add(Article.ARTICLE_TITLE);
        articleFields.add(Article.ARTICLE_PERMALINK);
        articleFields.add(Article.ARTICLE_CREATE_TIME);
        articleFields.add(Article.ARTICLE_VIEW_CNT);
        articleFields.add(Article.ARTICLE_COMMENT_CNT);
        articleFields.add(Article.ARTICLE_AUTHOR_ID);
        articleFields.add(Article.ARTICLE_TAGS);
        articleFields.add(Article.ARTICLE_STATUS);
        articleFields.add(Article.ARTICLE_STICK);
        final JSONObject result = articleQueryService.getArticles(requestJSONObject, articleFields);
        dataModel.put(Article.ARTICLES, result.opt(Article.ARTICLES));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows an article.
     *
     * @param context the specified context
     */
    public void showArticle(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String articleId = context.pathVar("articleId");
        final JSONObject article = articleQueryService.getArticle(articleId);
        Escapes.escapeHTML(article);
        dataModel.put(Article.ARTICLE, article);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Updates an article.
     *
     * @param context the specified context
     */
    public void updateArticle(final RequestContext context) {
        final String articleId = context.pathVar("articleId");
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject article = articleQueryService.getArticle(articleId);

        final Iterator<String> parameterNames = request.getParameterNames().iterator();
        while (parameterNames.hasNext()) {
            final String name = parameterNames.next();
            final String value = context.param(name);
            if (name.equals(Article.ARTICLE_REWARD_POINT)
                    || name.equals(Article.ARTICLE_QNA_OFFER_POINT)
                    || name.equals(Article.ARTICLE_STATUS)
                    || name.equals(Article.ARTICLE_TYPE)
                    || name.equals(Article.ARTICLE_THANK_CNT)
                    || name.equals(Article.ARTICLE_GOOD_CNT)
                    || name.equals(Article.ARTICLE_BAD_CNT)
                    || name.equals(Article.ARTICLE_PERFECT)
                    || name.equals(Article.ARTICLE_ANONYMOUS_VIEW)
                    || name.equals(Article.ARTICLE_PUSH_ORDER)
                    || name.equals(Article.ARTICLE_SHOW_IN_LIST)) {
                article.put(name, Integer.valueOf(value));
            } else {
                article.put(name, value);
            }
        }

        final String articleTags = Tag.formatTags(article.optString(Article.ARTICLE_TAGS));
        article.put(Article.ARTICLE_TAGS, articleTags);

        articleMgmtService.updateArticleByAdmin(articleId, article);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_ARTICLE, articleId));

        article = articleQueryService.getArticle(articleId);
        String title = article.optString(Article.ARTICLE_TITLE);
        title = Escapes.escapeHTML(title);
        article.put(Article.ARTICLE_TITLE, title);
        dataModel.put(Article.ARTICLE, article);

        updateArticleSearchIndex(article);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows admin comments.
     *
     * @param context the specified context
     */
    public void showComments(final RequestContext context) {
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/comments.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final List<String> commentFields = new ArrayList<>();
        commentFields.add(Keys.OBJECT_ID);
        commentFields.add(Comment.COMMENT_CREATE_TIME);
        commentFields.add(Comment.COMMENT_AUTHOR_ID);
        commentFields.add(Comment.COMMENT_ON_ARTICLE_ID);
        commentFields.add(Comment.COMMENT_SHARP_URL);
        commentFields.add(Comment.COMMENT_STATUS);
        commentFields.add(Comment.COMMENT_CONTENT);
        final JSONObject result = commentQueryService.getComments(requestJSONObject, commentFields);
        dataModel.put(Comment.COMMENTS, result.opt(Comment.COMMENTS));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows a comment.
     *
     * @param context the specified context
     */
    public void showComment(final RequestContext context) {
        final String commentId = context.pathVar("commentId");

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/comment.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject comment = commentQueryService.getComment(commentId);
        Escapes.escapeHTML(comment);
        dataModel.put(Comment.COMMENT, comment);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Updates a comment.
     *
     * @param context the specified context
     */
    public void updateComment(final RequestContext context) {
        final String commentId = context.pathVar("commentId");
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/comment.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject comment = commentQueryService.getComment(commentId);

        final Iterator<String> parameterNames = request.getParameterNames().iterator();
        while (parameterNames.hasNext()) {
            final String name = parameterNames.next();
            final String value = context.param(name);

            if (name.equals(Comment.COMMENT_STATUS)
                    || name.equals(Comment.COMMENT_THANK_CNT)
                    || name.equals(Comment.COMMENT_GOOD_CNT)
                    || name.equals(Comment.COMMENT_BAD_CNT)) {
                comment.put(name, Integer.valueOf(value));
            } else {
                comment.put(name, value);
            }
        }

        commentMgmtService.updateCommentByAdmin(commentId, comment);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_COMMENT, commentId));

        comment = commentQueryService.getComment(commentId);
        dataModel.put(Comment.COMMENT, comment);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows admin miscellaneous.
     *
     * @param context the specified context
     */
    public void showMisc(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/misc.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final List<JSONObject> misc = optionQueryService.getMisc();
        dataModel.put(Option.OPTIONS, misc);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Updates admin miscellaneous.
     *
     * @param context the specified context
     */
    public void updateMisc(final RequestContext context) {
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/misc.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        List<JSONObject> misc = new ArrayList<>();

        final Iterator<String> parameterNames = request.getParameterNames().iterator();
        while (parameterNames.hasNext()) {
            final String name = parameterNames.next();
            final String value = context.param(name);

            final JSONObject option = new JSONObject();
            option.put(Keys.OBJECT_ID, name);
            option.put(Option.OPTION_VALUE, value);
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_MISC);

            misc.add(option);
        }

        for (final JSONObject option : misc) {
            optionMgmtService.updateOption(option.getString(Keys.OBJECT_ID), option);
        }
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_MISC, ""));

        misc = optionQueryService.getMisc();
        dataModel.put(Option.OPTIONS, misc);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows admin tags.
     *
     * @param context the specified context
     */
    public void showTags(final RequestContext context) {
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/tags.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String tagTitle = context.param(Common.TITLE);
        if (StringUtils.isNotBlank(tagTitle)) {
            requestJSONObject.put(Tag.TAG_TITLE, tagTitle);
        }

        final List<String> tagFields = new ArrayList<>();
        tagFields.add(Keys.OBJECT_ID);
        tagFields.add(Tag.TAG_TITLE);
        tagFields.add(Tag.TAG_DESCRIPTION);
        tagFields.add(Tag.TAG_ICON_PATH);
        tagFields.add(Tag.TAG_COMMENT_CNT);
        tagFields.add(Tag.TAG_REFERENCE_CNT);
        tagFields.add(Tag.TAG_FOLLOWER_CNT);
        tagFields.add(Tag.TAG_STATUS);
        tagFields.add(Tag.TAG_GOOD_CNT);
        tagFields.add(Tag.TAG_BAD_CNT);
        tagFields.add(Tag.TAG_URI);
        tagFields.add(Tag.TAG_CSS);
        final JSONObject result = tagQueryService.getTags(requestJSONObject, tagFields);
        dataModel.put(Tag.TAGS, result.opt(Tag.TAGS));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows a tag.
     *
     * @param context the specified context
     */
    public void showTag(final RequestContext context) {
        final String tagId = context.pathVar("tagId");

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/tag.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject tag = tagQueryService.getTag(tagId);
        if (null == tag) {
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");

            dataModel.put(Keys.MSG, langPropsService.get("notFoundTagLabel"));
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        dataModel.put(Tag.TAG, tag);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Updates a tag.
     *
     * @param context the specified context
     */
    public void updateTag(final RequestContext context) {
        final String tagId = context.pathVar("tagId");
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/tag.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject tag = tagQueryService.getTag(tagId);

        final String oldTitle = tag.optString(Tag.TAG_TITLE);

        final Iterator<String> parameterNames = request.getParameterNames().iterator();
        while (parameterNames.hasNext()) {
            final String name = parameterNames.next();
            final String value = context.param(name);

            if (name.equals(Tag.TAG_REFERENCE_CNT)
                    || name.equals(Tag.TAG_COMMENT_CNT)
                    || name.equals(Tag.TAG_FOLLOWER_CNT)
                    || name.contains(Tag.TAG_LINK_CNT)
                    || name.contains(Tag.TAG_STATUS)
                    || name.equals(Tag.TAG_GOOD_CNT)
                    || name.equals(Tag.TAG_BAD_CNT)
                    || name.equals(Tag.TAG_SHOW_SIDE_AD)) {
                tag.put(name, Integer.valueOf(value));
            } else {
                tag.put(name, value);
            }
        }

        final String newTitle = tag.optString(Tag.TAG_TITLE);

        if (oldTitle.equalsIgnoreCase(newTitle)) {
            try {
                tagMgmtService.updateTag(tagId, tag);
                operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_TAG, tagId));
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Updates a tag failed", e);
                return;
            }
        }

        tag = tagQueryService.getTag(tagId);
        dataModel.put(Tag.TAG, tag);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows admin domains.
     *
     * @param context the specified context
     */
    public void showDomains(final RequestContext context) {
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/domains.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String domainTitle = context.param(Common.TITLE);
        if (StringUtils.isNotBlank(domainTitle)) {
            requestJSONObject.put(Domain.DOMAIN_TITLE, domainTitle);
        }

        final List<String> domainFields = new ArrayList<>();
        domainFields.add(Keys.OBJECT_ID);
        domainFields.add(Domain.DOMAIN_TITLE);
        domainFields.add(Domain.DOMAIN_DESCRIPTION);
        domainFields.add(Domain.DOMAIN_ICON_PATH);
        domainFields.add(Domain.DOMAIN_STATUS);
        domainFields.add(Domain.DOMAIN_URI);
        final JSONObject result = domainQueryService.getDomains(requestJSONObject, domainFields);
        final List<JSONObject> domains = (List<JSONObject>) result.opt(Domain.DOMAINS);
        for (final JSONObject domain : domains) {
            final String iconPath = domain.optString(Domain.DOMAIN_ICON_PATH);
            Escapes.escapeHTML(domain);
            domain.put(Domain.DOMAIN_ICON_PATH, iconPath);
        }
        dataModel.put(Common.ALL_DOMAINS, domains);

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows a domain.
     *
     * @param context the specified context
     */
    public void showDomain(final RequestContext context) {
        final String domainId = context.pathVar("domainId");

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/domain.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject domain = domainQueryService.getDomain(domainId);
        dataModel.put(Domain.DOMAIN, domain);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Updates a domain.
     *
     * @param context the specified context
     */
    public void updateDomain(final RequestContext context) {
        final String domainId = context.pathVar("domainId");
        final Request request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/domain.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject domain = domainQueryService.getDomain(domainId);
        final String oldTitle = domain.optString(Domain.DOMAIN_TITLE);

        final Iterator<String> parameterNames = request.getParameterNames().iterator();
        while (parameterNames.hasNext()) {
            final String name = parameterNames.next();
            String value = context.param(name);

            if (Domain.DOMAIN_ICON_PATH.equals(name)) {
                value = StringUtils.replace(value, "\"", "'");
            }

            domain.put(name, value);
        }

        domain.remove(Domain.DOMAIN_T_TAGS);

        final String newTitle = domain.optString(Domain.DOMAIN_TITLE);

        if (oldTitle.equalsIgnoreCase(newTitle)) {
            domainMgmtService.updateDomain(domainId, domain);
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_UPDATE_DOMAIN, domainId));
        }

        domain = domainQueryService.getDomain(domainId);
        dataModel.put(Domain.DOMAIN, domain);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows add domain.
     *
     * @param context the specified context
     */
    public void showAddDomain(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/add-domain.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Adds a domain.
     *
     * @param context the specified context
     */
    public void addDomain(final RequestContext context) {
        final Request request = context.getRequest();

        final String domainTitle = context.param(Domain.DOMAIN_TITLE);

        if (StringUtils.isBlank(domainTitle)) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, langPropsService.get("invalidDomainTitleLabel"));

            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        if (null != domainQueryService.getByTitle(domainTitle)) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.put(Keys.MSG, langPropsService.get("duplicatedDomainLabel"));
            dataModelService.fillHeaderAndFooter(context, dataModel);
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
            operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_ADD_DOMAIN, domainId));
        } catch (final ServiceException e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        context.sendRedirect(Latkes.getServePath() + "/admin/domain/" + domainId);
    }

    /**
     * Removes a domain.
     *
     * @param context the specified context
     */
    public void removeDomain(final RequestContext context) {
        final Request request = context.getRequest();

        final String domainId = context.param(Domain.DOMAIN_T_ID);
        domainMgmtService.removeDomain(domainId);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_REMOVE_DOMAIN, domainId));

        context.sendRedirect(Latkes.getServePath() + "/admin/domains");
    }

    /**
     * Adds a tag into a domain.
     *
     * @param context the specified context
     */
    public void addDomainTag(final RequestContext context) {
        final String domainId = context.pathVar("domainId");
        final Request request = context.getRequest();

        String tagTitle = context.param(Tag.TAG_TITLE);
        final JSONObject tag = tagQueryService.getTagByTitle(tagTitle);

        String tagId;
        if (tag != null) {
            tagId = tag.optString(Keys.OBJECT_ID);
        } else {
            try {
                if (StringUtils.isBlank(tagTitle)) {
                    throw new Exception(langPropsService.get("tagsErrorLabel"));
                }

                tagTitle = Tag.formatTags(tagTitle);

                if (!Tag.containsWhiteListTags(tagTitle)) {
                    if (!Tag.TAG_TITLE_PATTERN.matcher(tagTitle).matches()) {
                        throw new Exception(langPropsService.get("tagsErrorLabel"));
                    }

                    if (tagTitle.length() > Tag.MAX_TAG_TITLE_LENGTH) {
                        throw new Exception(langPropsService.get("tagsErrorLabel"));
                    }
                }
            } catch (final Exception e) {
                final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
                final Map<String, Object> dataModel = renderer.getDataModel();
                dataModel.put(Keys.MSG, e.getMessage());
                dataModelService.fillHeaderAndFooter(context, dataModel);
                return;
            }

            final JSONObject admin = Sessions.getUser();
            final String userId = admin.optString(Keys.OBJECT_ID);

            try {
                tagId = tagMgmtService.addTag(userId, tagTitle);
            } catch (final ServiceException e) {
                final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
                final Map<String, Object> dataModel = renderer.getDataModel();

                dataModel.put(Keys.MSG, e.getMessage());
                dataModelService.fillHeaderAndFooter(context, dataModel);
                return;
            }
        }

        if (domainQueryService.containTag(tagTitle, domainId)) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            String msg = langPropsService.get("domainContainTagLabel");
            msg = msg.replace("{tag}", tagTitle);

            dataModel.put(Keys.MSG, msg);

            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        final JSONObject domainTag = new JSONObject();
        domainTag.put(Domain.DOMAIN + "_" + Keys.OBJECT_ID, domainId);
        domainTag.put(Tag.TAG + "_" + Keys.OBJECT_ID, tagId);

        domainMgmtService.addDomainTag(domainTag);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_ADD_DOMAIN_TAG, domainId));

        context.sendRedirect(Latkes.getServePath() + "/admin/domain/" + domainId);
    }

    /**
     * Removes a tag from a domain.
     *
     * @param context the specified context
     */
    public void removeDomainTag(final RequestContext context) {
        final String domainId = context.pathVar("domainId");
        final Request request = context.getRequest();

        final String tagTitle = context.param(Tag.TAG_TITLE);
        final JSONObject tag = tagQueryService.getTagByTitle(tagTitle);
        if (null == tag) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.put(Keys.MSG, langPropsService.get("invalidTagLabel"));
            dataModelService.fillHeaderAndFooter(context, dataModel);
            return;
        }

        domainMgmtService.removeDomainTag(domainId, tag.optString(Keys.OBJECT_ID));
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_REMOVE_DOMAIN_TAG, domainId));

        context.sendRedirect(Latkes.getServePath() + "/admin/domain/" + domainId);
    }

    /**
     * Rebuilds article search index.
     *
     * @param context the specified context
     */
    public void rebuildArticleSearchIndex(final RequestContext context) {
        context.renderJSON(StatusCodes.SUCC);

        if (Symphonys.ES_ENABLED) {
            searchMgmtService.rebuildESIndex();
        }

        if (Symphonys.ALGOLIA_ENABLED) {
            searchMgmtService.rebuildAlgoliaIndex();
        }

        Symphonys.EXECUTOR_SERVICE.submit(() -> {
            try {
                final JSONObject stat = optionQueryService.getStatistic();
                final int articleCount = stat.optInt(Option.ID_C_STATISTIC_ARTICLE_COUNT);
                final int pages = (int) Math.ceil((double) articleCount / 50.0);

                for (int pageNum = 1; pageNum <= pages; pageNum++) {
                    final List<JSONObject> articles = articleQueryService.getValidArticles(pageNum, 50, Article.ARTICLE_TYPE_C_NORMAL, Article.ARTICLE_TYPE_C_CITY_BROADCAST);

                    for (final JSONObject article : articles) {
                        if (Symphonys.ALGOLIA_ENABLED) {
                            searchMgmtService.updateAlgoliaDocument(article);
                        }

                        if (Symphonys.ES_ENABLED) {
                            searchMgmtService.updateESDocument(article, Article.ARTICLE);
                        }
                    }

                    LOGGER.info("Indexed page [" + pageNum + "]");
                }

                LOGGER.info("Index finished");
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Search index failed", e);
            }
        });

        operationMgmtService.addOperation(Operation.newOperation(context.getRequest(), Operation.OPERATION_CODE_C_REBUILD_ARTICLES_SEARCH, ""));
    }

    /**
     * Rebuilds one article search index.
     *
     * @param context the specified context
     */
    public void rebuildOneArticleSearchIndex(final RequestContext context) {
        final String articleId = context.getRequest().getParameter(Article.ARTICLE_T_ID);
        final JSONObject article = articleQueryService.getArticle(articleId);

        updateArticleSearchIndex(article);
        operationMgmtService.addOperation(Operation.newOperation(context.getRequest(), Operation.OPERATION_CODE_C_REBUILD_ARTICLE_SEARCH, articleId));

        context.sendRedirect(Latkes.getServePath() + "/admin/articles");
    }

    private void updateArticleSearchIndex(final JSONObject article) {
        if (null == article || Article.ARTICLE_TYPE_C_DISCUSSION == article.optInt(Article.ARTICLE_TYPE)
                || Article.ARTICLE_TYPE_C_THOUGHT == article.optInt(Article.ARTICLE_TYPE)) {
            return;
        }

        if (Symphonys.ALGOLIA_ENABLED) {
            searchMgmtService.updateAlgoliaDocument(article);
        }

        if (Symphonys.ES_ENABLED) {
            searchMgmtService.updateESDocument(article, Article.ARTICLE);
        }

        final String articlePermalink = Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK);
        ArticleBaiduSender.sendToBaidu(articlePermalink);
    }
}
