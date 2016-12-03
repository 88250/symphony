/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2016,  b3log.org & hacpai.com
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
 * This class defines all permission model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Dec 3, 2016
 * @since 1.8.0
 */
public final class Permission {

    /**
     * Permission.
     */
    public static final String PERMISSION = "permission";

    /**
     * Permissions.
     */
    public static final String PERMISSIONS = "permissions";

    /**
     * Permission category.
     */
    public static final String PERMISSION_CATEGORY = "permissionCategory";

    // Category constants
    /**
     * Category - common function.
     */
    public static final int PERMISSION_CATEGORY_C_COMMON = 0;

    /**
     * Category - user management.
     */
    public static final int PERMISSION_CATEGORY_C_USER = 1;

    /**
     * Category - article management.
     */
    public static final int PERMISSION_CATEGORY_C_ARTICLE = 2;

    /**
     * Category - comment management.
     */
    public static final int PERMISSION_CATEGORY_C_COMMENT = 3;

    /**
     * Category - domain management.
     */
    public static final int PERMISSION_CATEGORY_C_DOMAIN = 4;

    /**
     * Category - tag management.
     */
    public static final int PERMISSION_CATEGORY_C_TAG = 5;

    /**
     * Category - reserved word management.
     */
    public static final int PERMISSION_CATEGORY_C_RESERVED_WORD = 6;

    /**
     * Category - advertise management.
     */
    public static final int PERMISSION_CATEGORY_C_AD = 7;

    /**
     * Category - misc management.
     */
    public static final int PERMISSION_CATEGORY_C_MISC = 8;

    /**
     * Private constructor.
     */
    private Permission() {
    }
}
