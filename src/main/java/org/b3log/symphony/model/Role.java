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
package org.b3log.symphony.model;

/**
 * This class defines all role model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.2, Dec 14, 2016
 * @since 1.8.0
 */
public final class Role {

    /**
     * Role.
     */
    public static final String ROLE = "role";

    /**
     * Roles.
     */
    public static final String ROLES = "roles";

    /**
     * Key of role name.
     */
    public static final String ROLE_NAME = "roleName";

    /**
     * Key of role description.
     */
    public static final String ROLE_DESCRIPTION = "roleDescription";

    /**
     * Key of role id.
     */
    public static final String ROLE_ID = "roleId";

    //// Transient ////
    /**
     * Key of user count.
     */
    public static final String ROLE_T_USER_COUNT = "roleUserCount";

    // Role name constants
    /**
     * Role name - default.
     */
    public static final String ROLE_ID_C_DEFAULT = "defaultRole";

    /**
     * Role name - admin.
     */
    public static final String ROLE_ID_C_ADMIN = "adminRole";

    /**
     * Role name - leader.
     */
    public static final String ROLE_ID_C_LEADER = "leaderRole";

    /**
     * Role name - regular.
     */
    public static final String ROLE_ID_C_REGULAR = "regularRole";

    /**
     * Role name - member.
     */
    public static final String ROLE_ID_C_MEMBER = "memberRole";

    /**
     * Role name - visitor.
     */
    public static final String ROLE_ID_C_VISITOR = "visitorRole";

    /**
     * Private constructor.
     */
    private Role() {
    }
}
