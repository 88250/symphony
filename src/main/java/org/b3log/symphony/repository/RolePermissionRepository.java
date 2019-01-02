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
package org.b3log.symphony.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Permission;
import org.b3log.symphony.model.Role;
import org.json.JSONObject;

import java.util.List;

/**
 * Role-Permission repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Aug 27, 2018
 * @since 1.8.0
 */
@Repository
public class RolePermissionRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public RolePermissionRepository() {
        super(Role.ROLE + "_" + Permission.PERMISSION);
    }

    /**
     * Removes role-permission relations by the specified role id.
     *
     * @param roleId the specified role id
     * @throws RepositoryException repository exception
     */
    public void removeByRoleId(final String roleId) throws RepositoryException {
        final List<JSONObject> toRemoves = getByRoleId(roleId);
        for (final JSONObject toRemove : toRemoves) {
            remove(toRemove.optString(Keys.OBJECT_ID));
        }
    }

    /**
     * Gets role-permission relations by the specified role id.
     *
     * @param roleId the specified role id
     * @return for example      <pre>
     * [{
     *         "oId": "",
     *         "roleId": roleId,
     *         "permissionId": ""
     * }, ....], returns an empty list if not found
     * </pre>
     * @throws RepositoryException repository exception
     */
    public List<JSONObject> getByRoleId(final String roleId) throws RepositoryException {
        final Query query = new Query().setFilter(
                new PropertyFilter(Role.ROLE_ID, FilterOperator.EQUAL, roleId)).
                setPageCount(1);

        return getList(query);
    }
}
