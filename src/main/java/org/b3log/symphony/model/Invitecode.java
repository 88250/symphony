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
 * This class defines all invitecode model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jul 2, 2016
 * @since 1.4.0
 */
public class Invitecode {

    /**
     * Invitecode.
     */
    public static final String INVITECODE = "invitecode";

    /**
     * Invitecodes.
     */
    public static final String INVITECODES = "invitecodes";

    /**
     * Key of code.
     */
    public static final String CODE = "code";

    /**
     * Key of user id.
     */
    public static final String USER_ID = "userId";
    
    /**
     * Key of use time.
     */
    public static final String USE_TIME = "useTime";

    /**
     * Key of status.
     */
    public static final String STATUS = "status";

    /**
     * Key of memo.
     */
    public static final String MEMO = "memo";

    // Status constants
    /**
     * Status - Used.
     */
    public static final int STATUS_C_USED = 0;
    /**
     * Status - Unused.
     */
    public static final int STATUS_C_UNUSED = 1;

    /**
     * Status - Stop use.
     */
    public static final int STATUS_C_STOPUSE = 2;
}
