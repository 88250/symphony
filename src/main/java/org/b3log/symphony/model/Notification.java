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
 * This class defines all notification model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.7.0.2, Sep 22, 2016
 * @since 0.2.5
 */
public final class Notification {

    /**
     * Notification.
     */
    public static final String NOTIFICATION = "notification";

    /**
     * Notifications.
     */
    public static final String NOTIFICATIONS = "notifications";

    /**
     * Key of user id.
     */
    public static final String NOTIFICATION_USER_ID = "userId";

    /**
     * Key of data id.
     */
    public static final String NOTIFICATION_DATA_ID = "dataId";

    /**
     * Key of data type.
     */
    public static final String NOTIFICATION_DATA_TYPE = "dataType";

    /**
     * Key of has read.
     */
    public static final String NOTIFICATION_HAS_READ = "hasRead";

    // Data type constants
    /**
     * Data type - article.
     */
    public static final int DATA_TYPE_C_ARTICLE = 0;

    /**
     * Data type - comment.
     */
    public static final int DATA_TYPE_C_COMMENT = 1;

    /**
     * Data type - @.
     */
    public static final int DATA_TYPE_C_AT = 2;

    /**
     * Data type - commented.
     */
    public static final int DATA_TYPE_C_COMMENTED = 3;

    /**
     * Data type - followingUser.
     */
    public static final int DATA_TYPE_C_FOLLOWING_USER = 4;

    /**
     * Data type - point charge.
     */
    public static final int DATA_TYPE_C_POINT_CHARGE = 5;

    /**
     * Data type - point transfer.
     */
    public static final int DATA_TYPE_C_POINT_TRANSFER = 6;

    /**
     * Data type - article reward.
     */
    public static final int DATA_TYPE_C_POINT_ARTICLE_REWARD = 7;

    /**
     * Data type - comment reward (thank).
     */
    public static final int DATA_TYPE_C_POINT_COMMENT_THANK = 8;

    /**
     * Data type - broadcast.
     */
    public static final int DATA_TYPE_C_BROADCAST = 9;

    /**
     * Data type - point exchange.
     */
    public static final int DATA_TYPE_C_POINT_EXCHANGE = 10;

    /**
     * Data type - abuse point deduct.
     */
    public static final int DATA_TYPE_C_ABUSE_POINT_DEDUCT = 11;

    /**
     * Data type - article thank.
     */
    public static final int DATA_TYPE_C_POINT_ARTICLE_THANK = 12;

    /**
     * Data type - reply.
     */
    public static final int DATA_TYPE_C_REPLY = 13;

    /**
     * Data type - invitecode used.
     */
    public static final int DATA_TYPE_C_INVITECODE_USED = 14;

    /**
     * Data type - system announcement - article.
     */
    public static final int DATA_TYPE_C_SYS_ANNOUNCE_ARTICLE = 15;

    /**
     * Data type - system announcement - new user.
     */
    public static final int DATA_TYPE_C_SYS_ANNOUNCE_NEW_USER = 16;

    //// Transient ////
    /**
     * Key of unread notification count.
     */
    public static final String NOTIFICATION_T_UNREAD_COUNT = "unreadNotificationCount";

    /**
     * Key of at in article.
     */
    public static final String NOTIFICATION_T_AT_IN_ARTICLE = "atInArticle";

    /**
     * Private constructor.
     */
    private Notification() {
    }
}
