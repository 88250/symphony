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
package org.b3log.symphony.util;

/**
 * Status code constants and utilities.
 * <p>
 * 因为一些历史原因，所以目前仅有 API2 在使用该类，其他的响应还是使用 "sc": boolean 来实现。
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Apr 3, 2017
 * @since 2.0.0
 */
public final class StatusCodes {

    /**
     * Indicates success.
     */
    public static final int SUCC = 0;

    /**
     * Indicates not found.
     */
    public static final int NOT_FOUND = 1;

    /**
     * Indicates an error occurred.
     */
    public static final int ERR = -1;

}
