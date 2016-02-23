/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.model;

/**
 * This class defines all notification model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.0.2, Feb 23, 2016
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
