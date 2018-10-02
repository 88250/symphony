/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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
package org.b3log.symphony.service;

import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Permission;
import org.b3log.symphony.model.Role;
import org.b3log.symphony.repository.PermissionRepository;
import org.b3log.symphony.repository.RolePermissionRepository;
import org.b3log.symphony.repository.RoleRepository;
import org.b3log.symphony.repository.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Role query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.6.0.0, Jun 23, 2018
 * @since 1.8.0
 */
@Service
public class RoleQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(RoleQueryService.class);

    /**
     * Role repository.
     */
    @Inject
    private RoleRepository roleRepository;

    /**
     * Role-Permission repository.
     */
    @Inject
    private RolePermissionRepository rolePermissionRepository;

    /**
     * Permission repository.
     */
    @Inject
    private PermissionRepository permissionRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Count the specified role's uses.
     *
     * @param roleId the specified role id
     * @return use count, returns integer max value if fails
     */
    public int countUser(final String roleId) {
        try {
            final Query userCountQuery = new Query().setFilter(new PropertyFilter(User.USER_ROLE, FilterOperator.EQUAL, roleId));

            return (int) userRepository.count(userCountQuery);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Count role [id=" + roleId + "] uses failed", e);

            return Integer.MAX_VALUE;
        }
    }

    /**
     * Checks whether the specified user has the specified requisite permissions.
     *
     * @param userId               the specified user id
     * @param requisitePermissions the specified requisite permissions
     * @return @code true} if the role has the specified requisite permissions, returns @code false} otherwise
     */
    public boolean userHasPermissions(final String userId, final Set<String> requisitePermissions) {
        try {
            final JSONObject user = userRepository.get(userId);
            final String roleId = user.optString(User.USER_ROLE);
            final Set<String> permissions = getPermissions(roleId);

            return Permission.hasPermission(requisitePermissions, permissions);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Checks user [" + userId + "] has permission failed", e);

            return false;
        }
    }

    /**
     * Checks whether the specified role has the specified requisite permissions.
     *
     * @param roleId               the specified role id
     * @param requisitePermissions the specified requisite permissions
     * @return @code true} if the role has the specified requisite permissions, returns @code false} otherwise
     */
    public boolean hasPermissions(final String roleId, final Set<String> requisitePermissions) {
        final Set<String> permissions = getPermissions(roleId);

        return Permission.hasPermission(requisitePermissions, permissions);
    }

    /**
     * Gets an role specified by the given role id.
     *
     * @param roleId the given role id
     * @return an role, returns {@code null} if not found
     */
    public JSONObject getRole(final String roleId) {
        try {
            final JSONObject ret = roleRepository.get(roleId);

            if (!Strings.isNumeric(roleId)) {
                ret.put(Role.ROLE_NAME, langPropsService.get(roleId + "NameLabel"));
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets role failed", e);

            return null;
        }
    }

    /**
     * Gets all permissions and marks grant of a user specified by the given user id.
     *
     * @param userId the given user id
     * @return a map of permissions&lt;permissionId, permission&gt;, returns an empty map if not found
     */
    public Map<String, JSONObject> getUserPermissionsGrantMap(final String userId) {
        final List<JSONObject> permissions = getUserPermissionsGrant(userId);
        if (permissions.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, JSONObject> ret = new HashMap<>();
        for (final JSONObject permission : permissions) {
            ret.put(permission.optString(Keys.OBJECT_ID), permission);
        }

        return ret;
    }

    /**
     * Gets all permissions and marks grant of a user specified by the given user id.
     *
     * @param userId the given user id
     * @return a list of permissions, returns an empty list if not found
     */
    public List<JSONObject> getUserPermissionsGrant(final String userId) {
        try {
            final JSONObject user = userRepository.get(userId);
            if (null == user) {
                return getPermissionsGrant(Role.ROLE_ID_C_VISITOR);
            }

            final String roleId = user.optString(User.USER_ROLE);

            return getPermissionsGrant(roleId);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets user permissions grant failed", e);

            return getPermissionsGrant(Role.ROLE_ID_C_VISITOR);
        }
    }

    /**
     * Gets grant permissions of a user specified by the given user id.
     *
     * @param userId the given user id
     * @return a list of permissions, returns an empty set if not found
     */
    public Set<String> getUserPermissions(final String userId) {
        try {
            final JSONObject user = userRepository.get(userId);
            if (null == user) {
                return Collections.emptySet();
            }

            final String roleId = user.optString(User.USER_ROLE);

            return getPermissions(roleId);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets grant permissions of user [id=" + userId + "] failed", e);

            return Collections.emptySet();
        }
    }

    /**
     * Gets all permissions and marks grant of an role specified by the given role id.
     *
     * @param roleId the given role id
     * @return a map of permissions&lt;permissionId, permission&gt;, returns an empty map if not found
     */
    public Map<String, JSONObject> getPermissionsGrantMap(final String roleId) {
        final List<JSONObject> permissions = getPermissionsGrant(roleId);
        if (permissions.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, JSONObject> ret = new HashMap<>();
        for (final JSONObject permission : permissions) {
            ret.put(permission.optString(Keys.OBJECT_ID), permission);
        }

        return ret;
    }

    /**
     * Gets all permissions and marks grant of an role specified by the given role id.
     *
     * @param roleId the given role id
     * @return a list of permissions, returns an empty list if not found
     */
    public List<JSONObject> getPermissionsGrant(final String roleId) {
        final List<JSONObject> ret = new ArrayList<>();

        try {
            final List<JSONObject> permissions = CollectionUtils.jsonArrayToList(
                    permissionRepository.get(new Query()).optJSONArray(Keys.RESULTS));
            final List<JSONObject> rolePermissions = rolePermissionRepository.getByRoleId(roleId);

            for (final JSONObject permission : permissions) {
                final String permissionId = permission.optString(Keys.OBJECT_ID);
                permission.put(Permission.PERMISSION_T_GRANT, false);
                ret.add(permission);

                for (final JSONObject rolePermission : rolePermissions) {
                    final String grantPermissionId = rolePermission.optString(Permission.PERMISSION_ID);

                    if (permissionId.equals(grantPermissionId)) {
                        permission.put(Permission.PERMISSION_T_GRANT, true);

                        break;
                    }
                }
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets permissions grant of role [id=" + roleId + "] failed", e);
        }

        return ret;
    }

    /**
     * Gets permissions of an role specified by the given role id.
     *
     * @param roleId the given role id
     * @return a list of permissions, returns an empty list if not found
     */
    public Set<String> getPermissions(final String roleId) {
        final Set<String> ret = new HashSet<>();

        try {
            final List<JSONObject> rolePermissions = rolePermissionRepository.getByRoleId(roleId);
            for (final JSONObject rolePermission : rolePermissions) {
                final String permissionId = rolePermission.optString(Permission.PERMISSION_ID);

                ret.add(permissionId);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets permissions of role [id=" + roleId + "] failed", e);

            return Collections.emptySet();
        }
    }

    /**
     * Gets roles by the specified request json object.
     *
     * @param currentPage the specified current page number
     * @param pageSize    the specified page size
     * @param windowSize  the specified window size
     * @return for example, <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "roles": [{
     *         "oId": "",
     *         "roleName": "",
     *         "roleDescription": "",
     *         "roleUserCount": int,
     *         "permissions": [
     *             {
     *                 "oId": "adUpdateADSide",
     *                 "permissionCategory": int
     *             }, ....
     *         ]
     *     }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     * @see Pagination
     */
    public JSONObject getRoles(final int currentPage, final int pageSize, final int windowSize)
            throws ServiceException {
        final JSONObject ret = new JSONObject();

        final Query query = new Query().setCurrentPageNum(currentPage).setPageSize(pageSize).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        JSONObject result = null;

        try {
            result = roleRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets roles failed", e);

            throw new ServiceException(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPage, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> roles = CollectionUtils.jsonArrayToList(data);

        try {
            for (final JSONObject role : roles) {
                final List<JSONObject> permissions = new ArrayList<>();
                role.put(Permission.PERMISSIONS, (Object) permissions);

                final String roleId = role.optString(Keys.OBJECT_ID);
                final List<JSONObject> rolePermissions = rolePermissionRepository.getByRoleId(roleId);
                for (final JSONObject rolePermission : rolePermissions) {
                    final String permissionId = rolePermission.optString(Permission.PERMISSION_ID);
                    final JSONObject permission = permissionRepository.get(permissionId);

                    permissions.add(permission);
                }

                final Query userCountQuery = new Query().
                        setFilter(new PropertyFilter(User.USER_ROLE, FilterOperator.EQUAL, roleId));
                final int count = (int) userRepository.count(userCountQuery);
                role.put(Role.ROLE_T_USER_COUNT, count);

                // fill description
                if (Strings.isNumeric(roleId)) {
                    continue;
                }

                String roleName = role.optString(Role.ROLE_NAME);
                try {
                    roleName = langPropsService.get(roleId + "NameLabel");
                } catch (final Exception e) {
                    // ignored
                }

                String roleDesc = role.optString(Role.ROLE_DESCRIPTION);
                try {
                    roleDesc = langPropsService.get(roleId + "DescLabel");
                } catch (final Exception e) {
                    // ignored
                }

                role.put(Role.ROLE_NAME, roleName);
                role.put(Role.ROLE_DESCRIPTION, roleDesc);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets role permissions failed", e);

            throw new ServiceException(e);
        }

        Collections.sort(roles, (o1, o2) -> ((List) o2.opt(Permission.PERMISSIONS)).size()
                - ((List) o1.opt(Permission.PERMISSIONS)).size());

        ret.put(Role.ROLES, (Object) roles);

        return ret;
    }
}
