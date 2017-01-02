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
 * This class defines all invitecode model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Aug 26, 2016
 * @since 1.4.0
 */
public final class Invitecode {

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
     * Key of generator id.
     */
    public static final String GENERATOR_ID = "generatorId";

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
