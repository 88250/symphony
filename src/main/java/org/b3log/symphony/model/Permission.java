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
