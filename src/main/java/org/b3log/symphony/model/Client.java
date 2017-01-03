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
 * This class defines all solo model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Nov 19, 2012
 * @since 0.2.0
 */
public final class Client {

    /**
     * Client.
     */
    public static final String CLIENT = "client";

    /**
     * Clients.
     */
    public static final String CLIENTS = "clients";

    /**
     * Key of client name.
     */
    public static final String CLIENT_NAME = "clientName";

    /**
     * Key of client version.
     */
    public static final String CLIENT_VERSION = "clientVersion";

    /**
     * Key of client host.
     */
    public static final String CLIENT_HOST = "clientHost";

    /**
     * Key of client runtime environment.
     */
    public static final String CLIENT_RUNTIME_ENV = "clientRuntimeEnv";

    /**
     * Key of client latest add (client -> symphony) article time.
     */
    public static final String CLIENT_LATEST_ADD_ARTICLE_TIME = "clientLatestAddArticleTime";

    /**
     * Key of client latest add (client -> symphony) comment time.
     */
    public static final String CLIENT_LATEST_ADD_COMMENT_TIME = "clientLatestAddCommentTime";

    /**
     * Key of client administrator email.
     */
    public static final String CLIENT_ADMIN_EMAIL = "clientAdminEmail";

    //// Transient ////
    /**
     * Key of client title.
     */
    public static final String CLIENT_T_TITLE = "clientTitle";

    /**
     * Private constructor.
     */
    private Client() {
    }
}
