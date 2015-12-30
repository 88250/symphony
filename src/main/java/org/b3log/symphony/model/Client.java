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
