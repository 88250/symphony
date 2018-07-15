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
package org.b3log.symphony.model;

/**
 * This class defines all notification model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.17.0.0, Jul 15, 2017
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
     * Data type - following - user.
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

    /**
     * Data type - new follower.
     */
    public static final int DATA_TYPE_C_NEW_FOLLOWER = 17;

    /**
     * Data type - invitation link used.
     */
    public static final int DATA_TYPE_C_INVITATION_LINK_USED = 18;

    /**
     * Data type - system announcement - role changed.
     */
    public static final int DATA_TYPE_C_SYS_ANNOUNCE_ROLE_CHANGED = 19;

    /**
     * Data type - following - article update.
     */
    public static final int DATA_TYPE_C_FOLLOWING_ARTICLE_UPDATE = 20;

    /**
     * Data type - following - article comment.
     */
    public static final int DATA_TYPE_C_FOLLOWING_ARTICLE_COMMENT = 21;

    /**
     * Data type - point - perfect article.
     */
    public static final int DATA_TYPE_C_POINT_PERFECT_ARTICLE = 22;

    /**
     * Data type - article new follower.
     */
    public static final int DATA_TYPE_C_ARTICLE_NEW_FOLLOWER = 23;

    /**
     * Data type - article new watcher.
     */
    public static final int DATA_TYPE_C_ARTICLE_NEW_WATCHER = 24;

    /**
     * Data type - comment vote up.
     */
    public static final int DATA_TYPE_C_COMMENT_VOTE_UP = 25;

    /**
     * Data type - comment vote down.
     */
    public static final int DATA_TYPE_C_COMMENT_VOTE_DOWN = 26;

    /**
     * Data type - article vote up.
     */
    public static final int DATA_TYPE_C_ARTICLE_VOTE_UP = 27;

    /**
     * Data type - article vote down.
     */
    public static final int DATA_TYPE_C_ARTICLE_VOTE_DOWN = 28;

    /**
     * Data type - comment accept.
     */
    public static final int DATA_TYPE_C_POINT_COMMENT_ACCEPT = 33;

    /**
     * Data type - report handled.
     */
    public static final int DATA_TYPE_C_POINT_REPORT_HANDLED = 36;

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
     * Key of is comment.
     */
    public static final String NOTIFICATION_T_IS_COMMENT = "isComment";

    /**
     * Private constructor.
     */
    private Notification() {
    }
}
