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
package org.b3log.symphony.service;

import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Permission;
import org.b3log.symphony.model.Role;
import org.b3log.symphony.repository.RolePermissionRepository;
import org.b3log.symphony.repository.RoleRepository;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.Set;

/**
 * Role management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Dec 8, 2016
 * @since 1.8.0
 */
@Service
public class RoleMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(RoleMgmtService.class);

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
     * Adds the specified role.
     *
     * @param role the specified role
     */
    @Transactional
    public void addRole(final JSONObject role) {
        try {
            final String roleName = role.optString(Role.ROLE_NAME);

            final Query query = new Query().
                    setFilter(new PropertyFilter(Role.ROLE_NAME, FilterOperator.EQUAL, roleName));
            if (roleRepository.count(query) > 0) {
                return;
            }

            roleRepository.add(role);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Adds role failed", e);
        }
    }

    /**
     * Updates role permissions.
     *
     * @param roleId the specified role id
     */
    @Transactional
    public void updateRolePermissions(final String roleId, final Set<String> permissionIds) {
        try {
            rolePermissionRepository.removeByRoleId(roleId);

            for (final String permissionId : permissionIds) {
                final JSONObject rel = new JSONObject();
                rel.put(Role.ROLE_ID, roleId);
                rel.put(Permission.PERMISSION_ID, permissionId);

                rolePermissionRepository.add(rel);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Updates role permissions failed", e);
        }
    }
}
