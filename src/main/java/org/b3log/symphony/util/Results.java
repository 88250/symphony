/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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

import org.b3log.latke.Keys;
import org.json.JSONObject;

/**
 * Result utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 3.0.0.0, Feb 10, 2019
 * @since 0.2.0
 */
public final class Results {

    /**
     * Constructs a successful result.
     *
     * @return result
     */
    public static JSONObject newSucc() {
        return new JSONObject().put(Keys.CODE, StatusCodes.SUCC).put(Keys.MSG, "");
    }

    /**
     * Constructs a failed result.
     *
     * @return result
     */
    public static JSONObject newFail() {
        return new JSONObject().put(Keys.CODE, StatusCodes.ERR).put(Keys.MSG, "System is abnormal, please try again later");
    }

    /**
     * Private constructor.
     */
    private Results() {
    }
}
