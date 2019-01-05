/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.event.ArticleBaiduSender;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.PermissionCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.UserRegister2Validation;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.Escapes;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
 * @version 2.30.1.1, Jan 5, 2019
 * @since 1.1.0
 */
@RequestProcessor
public class AdminProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AdminProcessor.class);

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
     * Shows audit log.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/admin/auditlog", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showAuditlog(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
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
        dataModel.put(Operation.OPERATIONS, CollectionUtils.jsonArrayToList(result.optJSONArray(Operation.OPERATIONS)));

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
    @RequestProcessing(value = "/admin/report/ignore/{reportId}", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void makeReportIgnored(final RequestContext context) {
        final String reportId = context.pathVar("reportId");
        final HttpServletRequest request = context.getRequest();
        reportMgmtService.makeReportIgnored(reportId);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_MAKE_REPORT_IGNORED, reportId));

        context.sendRedirect(Latkes.getServePath() + "/admin/reports");
    }

    /**
     * Makes a report as handled .
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/admin/report/{reportId}", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void makeReportHandled(final RequestContext context) {
        final String reportId = context.pathVar("reportId");
        final HttpServletRequest request = context.getRequest();
        reportMgmtService.makeReportHandled(reportId);
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_MAKE_REPORT_HANDLED, reportId));

        context.sendRedirect(Latkes.getServePath() + "/admin/reports");
    }

    /**
     * Shows reports.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/admin/reports", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showReports(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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
        dataModel.put(Report.REPORTS, CollectionUtils.jsonArrayToList(result.optJSONArray(Report.REPORTS)));

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
    @RequestProcessing(value = "/admin/role/{roleId}/remove", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void removeRole(final RequestContext context) {
        final String roleId = context.pathVar("roleId");
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin/breezemoons", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showBreezemoons(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/breezemoons.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;
        final int avatarViewMode = (int) context.attr(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final Map<String, Class<?>> fields = new HashMap<>();
        fields.put(Keys.OBJECT_ID, String.class);
        fields.put(Breezemoon.BREEZEMOON_CONTENT, String.class);
        fields.put(Breezemoon.BREEZEMOON_CREATED, Long.class);
        fields.put(Breezemoon.BREEZEMOON_AUTHOR_ID, String.class);
        fields.put(Breezemoon.BREEZEMOON_STATUS, Integer.class);

        final JSONObject result = breezemoonQueryService.getBreezemoons(avatarViewMode, requestJSONObject, fields);
        dataModel.put(Breezemoon.BREEZEMOONS, CollectionUtils.jsonArrayToList(result.optJSONArray(Breezemoon.BREEZEMOONS)));

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
    @RequestProcessing(value = "/admin/breezemoon/{breezemoonId}", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/breezemoon/{breezemoonId}", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void updateBreezemoon(final RequestContext context) {
        final String breezemoonId = context.pathVar("breezemoonId");
        final HttpServletRequest request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/breezemoon.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject breezemoon = breezemoonQueryService.getBreezemoon(breezemoonId);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
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
    @RequestProcessing(value = "/admin/remove-breezemoon", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void removeBreezemoon(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
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
    @RequestProcessing(value = "/admin/tags/remove-unused", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void removeUnusedTags(final RequestContext context) {
        context.renderJSON(true);

        tagMgmtService.removeUnusedTags();
        operationMgmtService.addOperation(Operation.newOperation(context.getRequest(), Operation.OPERATION_CODE_C_REMOVE_UNUSED_TAGS, ""));
    }

    /**
     * Adds an role.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/admin/role", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void addRole(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
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
    @RequestProcessing(value = "/admin/role/{roleId}/permissions", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void updateRolePermissions(final RequestContext context) {
        final String roleId = context.pathVar("roleId");
        final HttpServletRequest request = context.getRequest();

        final Map<String, String[]> parameterMap = request.getParameterMap();
        final Set<String> permissionIds = parameterMap.keySet();

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
    @RequestProcessing(value = "/admin/role/{roleId}/permissions", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/roles", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/ad/side", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void updateSideAd(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
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
    @RequestProcessing(value = "/admin/ad/banner", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void updateBanner(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
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
    @RequestProcessing(value = "/admin/ad", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/add-tag", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/add-tag", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void addTag(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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

        final JSONObject admin = (JSONObject) context.attr(Common.CURRENT_USER);
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
    @RequestProcessing(value = "/admin/stick-article", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void stickArticle(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin/cancel-stick-article", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void stickCancelArticle(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin/invitecodes/generate", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void generateInvitecodes(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin/invitecodes", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showInvitecodes(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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
        dataModel.put(Invitecode.INVITECODES, CollectionUtils.jsonArrayToList(result.optJSONArray(Invitecode.INVITECODES)));

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
    @RequestProcessing(value = "/admin/invitecode/{invitecodeId}", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/invitecode/{invitecodeId}", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void updateInvitecode(final RequestContext context) {
        final String invitecodeId = context.pathVar("invitecodeId");
        final HttpServletRequest request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/invitecode.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject invitecode = invitecodeQueryService.getInvitecodeById(invitecodeId);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
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
    @RequestProcessing(value = "/admin/add-article", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/add-article", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void addArticle(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin/add-reserved-word", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void addReservedWord(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin/add-reserved-word", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/reserved-word/{id}", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void updateReservedWord(final RequestContext context) {
        final String id = context.pathVar("id");
        final HttpServletRequest request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/reserved-word.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject word = optionQueryService.getOption(id);
        dataModel.put(Common.WORD, word);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
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
    @RequestProcessing(value = "/admin/reserved-words", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showReservedWords(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/reserved-words.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModel.put(Common.WORDS, optionQueryService.getReservedWords());

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows a reserved word.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/admin/reserved-word/{id}", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/remove-reserved-word", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void removeReservedWord(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin/remove-comment", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void removeComment(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

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
    @RequestProcessing(value = "/admin/remove-article", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void removeArticle(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/users", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showUsers(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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
        dataModel.put(User.USERS, CollectionUtils.jsonArrayToList(result.optJSONArray(User.USERS)));

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
    @RequestProcessing(value = "/admin/user/{userId}", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/add-user", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/add-user", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void addUser(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

        final String userName = context.param(User.USER_NAME);
        final String email = context.param(User.USER_EMAIL);
        final String password = context.param(User.USER_PASSWORD);
        final String appRole = context.param(UserExt.USER_APP_ROLE);

        final boolean nameInvalid = UserRegisterValidation.invalidUserName(userName);
        final boolean emailInvalid = !Strings.isEmail(email);
        final boolean passwordInvalid = UserRegister2Validation.invalidUserPassword(password);

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

            final JSONObject admin = (JSONObject) context.attr(Common.CURRENT_USER);
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
    @RequestProcessing(value = "/admin/user/{userId}", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void updateUser(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final HttpServletRequest request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/user.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = userQueryService.getUser(userId);
        dataModel.put(User.USER, user);
        final String oldRole = user.optString(User.USER_ROLE);

        final JSONObject result = roleQueryService.getRoles(1, Integer.MAX_VALUE, 10);
        final List<JSONObject> roles = (List<JSONObject>) result.opt(Role.ROLES);
        dataModel.put(Role.ROLES, roles);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
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

        final JSONObject currentUser = (JSONObject) context.attr(Common.CURRENT_USER);
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
    @RequestProcessing(value = "/admin/user/{userId}/email", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void updateUserEmail(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin/user/{userId}/username", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void updateUserName(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin/user/{userId}/charge-point", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void chargePoint(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin/user/{userId}/abuse-point", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void abusePoint(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin/user/{userId}/init-point", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void initPoint(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

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
    @RequestProcessing(value = "/admin/user/{userId}/exchange-point", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void exchangePoint(final RequestContext context) {
        final String userId = context.pathVar("userId");
        final HttpServletRequest request = context.getRequest();
        final String pointStr = context.param(Common.POINT);

        try {
            final int point = Integer.valueOf(pointStr);

            final JSONObject user = userQueryService.getUser(userId);
            final int currentPoint = user.optInt(UserExt.USER_POINT);

            if (currentPoint - point < Symphonys.getInt("pointExchangeMin")) {
                final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
                final Map<String, Object> dataModel = renderer.getDataModel();

                dataModel.put(Keys.MSG, langPropsService.get("insufficientBalanceLabel"));
                dataModelService.fillHeaderAndFooter(context, dataModel);

                return;
            }

            final String memo = String.valueOf(Math.floor(point / (double) Symphonys.getInt("pointExchangeUnit")));

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
    @RequestProcessing(value = "/admin/articles", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showArticles(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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

        final Map<String, Class<?>> articleFields = new HashMap<>();
        articleFields.put(Keys.OBJECT_ID, String.class);
        articleFields.put(Article.ARTICLE_TITLE, String.class);
        articleFields.put(Article.ARTICLE_PERMALINK, String.class);
        articleFields.put(Article.ARTICLE_CREATE_TIME, Long.class);
        articleFields.put(Article.ARTICLE_VIEW_CNT, Integer.class);
        articleFields.put(Article.ARTICLE_COMMENT_CNT, Integer.class);
        articleFields.put(Article.ARTICLE_AUTHOR_ID, String.class);
        articleFields.put(Article.ARTICLE_TAGS, String.class);
        articleFields.put(Article.ARTICLE_STATUS, Integer.class);
        articleFields.put(Article.ARTICLE_STICK, Long.class);

        final int avatarViewMode = (int) context.attr(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject result = articleQueryService.getArticles(avatarViewMode, requestJSONObject, articleFields);
        dataModel.put(Article.ARTICLES, CollectionUtils.jsonArrayToList(result.optJSONArray(Article.ARTICLES)));

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
    @RequestProcessing(value = "/admin/article/{articleId}", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/article/{articleId}", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void updateArticle(final RequestContext context) {
        final String articleId = context.pathVar("articleId");
        final HttpServletRequest request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject article = articleQueryService.getArticle(articleId);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
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
                    || name.equals(Article.ARTICLE_PUSH_ORDER)) {
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
    @RequestProcessing(value = "/admin/comments", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showComments(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/comments.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final Map<String, Class<?>> commentFields = new HashMap<>();
        commentFields.put(Keys.OBJECT_ID, String.class);
        commentFields.put(Comment.COMMENT_CREATE_TIME, String.class);
        commentFields.put(Comment.COMMENT_AUTHOR_ID, String.class);
        commentFields.put(Comment.COMMENT_ON_ARTICLE_ID, String.class);
        commentFields.put(Comment.COMMENT_SHARP_URL, String.class);
        commentFields.put(Comment.COMMENT_STATUS, Integer.class);
        commentFields.put(Comment.COMMENT_CONTENT, String.class);

        final int avatarViewMode = (int) context.attr(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject result = commentQueryService.getComments(avatarViewMode, requestJSONObject, commentFields);
        dataModel.put(Comment.COMMENTS, CollectionUtils.jsonArrayToList(result.optJSONArray(Comment.COMMENTS)));

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
    @RequestProcessing(value = "/admin/comment/{commentId}", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/comment/{commentId}", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void updateComment(final RequestContext context) {
        final String commentId = context.pathVar("commentId");
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/comment.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject comment = commentQueryService.getComment(commentId);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
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
    @RequestProcessing(value = "/admin/misc", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/misc", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void updateMisc(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/misc.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        List<JSONObject> misc = new ArrayList<>();

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
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
    @RequestProcessing(value = "/admin/tags", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showTags(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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

        final Map<String, Class<?>> tagFields = new HashMap<>();
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
        tagFields.put(Tag.TAG_URI, String.class);
        tagFields.put(Tag.TAG_CSS, String.class);

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

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows a tag.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/admin/tag/{tagId}", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/tag/{tagId}", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void updateTag(final RequestContext context) {
        final String tagId = context.pathVar("tagId");
        final HttpServletRequest request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/tag.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject tag = tagQueryService.getTag(tagId);

        final String oldTitle = tag.optString(Tag.TAG_TITLE);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
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
    @RequestProcessing(value = "/admin/domains", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showDomains(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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

        final Map<String, Class<?>> domainFields = new HashMap<>();
        domainFields.put(Keys.OBJECT_ID, String.class);
        domainFields.put(Domain.DOMAIN_TITLE, String.class);
        domainFields.put(Domain.DOMAIN_DESCRIPTION, String.class);
        domainFields.put(Domain.DOMAIN_ICON_PATH, String.class);
        domainFields.put(Domain.DOMAIN_STATUS, String.class);
        domainFields.put(Domain.DOMAIN_URI, String.class);

        final JSONObject result = domainQueryService.getDomains(requestJSONObject, domainFields);
        dataModel.put(Common.ALL_DOMAINS, CollectionUtils.jsonArrayToList(result.optJSONArray(Domain.DOMAINS)));

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
    @RequestProcessing(value = "/admin/domain/{domainId}", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/domain/{domainId}", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void updateDomain(final RequestContext context) {
        final String domainId = context.pathVar("domainId");
        final HttpServletRequest request = context.getRequest();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/domain.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject domain = domainQueryService.getDomain(domainId);
        final String oldTitle = domain.optString(Domain.DOMAIN_TITLE);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
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
    @RequestProcessing(value = "/admin/add-domain", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
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
    @RequestProcessing(value = "/admin/add-domain", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void addDomain(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin/remove-domain", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void removeDomain(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

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
    @RequestProcessing(value = "/admin/domain/{domainId}/add-tag", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void addDomainTag(final RequestContext context) {
        final String domainId = context.pathVar("domainId");
        final HttpServletRequest request = context.getRequest();

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

            final JSONObject admin = (JSONObject) context.attr(Common.CURRENT_USER);
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
    @RequestProcessing(value = "/admin/domain/{domainId}/remove-tag", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void removeDomainTag(final RequestContext context) {
        final String domainId = context.pathVar("domainId");
        final HttpServletRequest request = context.getRequest();

        final String tagTitle = context.param(Tag.TAG_TITLE);
        final JSONObject tag = tagQueryService.getTagByTitle(tagTitle);

        if (null == tag) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, langPropsService.get("invalidTagLabel"));

            dataModelService.fillHeaderAndFooter(context, dataModel);

            return;
        }

        final JSONObject domainTag = new JSONObject();
        domainTag.put(Domain.DOMAIN + "_" + Keys.OBJECT_ID, domainId);
        domainTag.put(Tag.TAG + "_" + Keys.OBJECT_ID, tag.optString(Keys.OBJECT_ID));

        domainMgmtService.removeDomainTag(domainId, tag.optString(Keys.OBJECT_ID));
        operationMgmtService.addOperation(Operation.newOperation(request, Operation.OPERATION_CODE_C_REMOVE_DOMAIN_TAG, domainId));

        context.sendRedirect(Latkes.getServePath() + "/admin/domain/" + domainId);
    }

    /**
     * Rebuilds article search index.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/admin/search/index", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void rebuildArticleSearchIndex(final RequestContext context) {
        context.renderJSON(true);

        if (Symphonys.getBoolean("es.enabled")) {
            searchMgmtService.rebuildESIndex();
        }

        if (Symphonys.getBoolean("algolia.enabled")) {
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
    @RequestProcessing(value = "/admin/search-index-article", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
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

        if (Symphonys.getBoolean("algolia.enabled")) {
            searchMgmtService.updateAlgoliaDocument(article);
        }

        if (Symphonys.getBoolean("es.enabled")) {
            searchMgmtService.updateESDocument(article, Article.ARTICLE);
        }

        final String articlePermalink = Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK);
        ArticleBaiduSender.sendToBaidu(articlePermalink);
    }
}
