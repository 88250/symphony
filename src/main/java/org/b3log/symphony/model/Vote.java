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
 * This class defines all vote model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 13, 2015
 * @since 1.3.0
 */
public final class Vote {

    /**
     * Vote.
     */
    public static final String VOTE = "vote";

    /**
     * Votes.
     */
    public static final String VOTES = "votes";

    /**
     * Key of user id.
     */
    public static final String USER_ID = "userId";

    /**
     * Key of type.
     */
    public static final String TYPE = "type";

    /**
     * Key of data type.
     */
    public static final String DATA_TYPE = "dataType";

    /**
     * Key of data id.
     */
    public static final String DATA_ID = "dataId";

    // Type constants
    /**
     * Type - Up.
     */
    public static final int TYPE_C_UP = 0;

    /**
     * Type - Down.
     */
    public static final int TYPE_C_DOWN = 1;

    // Data Type constants
    /**
     * Data Type - Article.
     */
    public static final int DATA_TYPE_C_ARTICLE = 0;

    /**
     * Data Type - Comment.
     */
    public static final int DATA_TYPE_C_COMMENT = 1;

    /**
     * Data Type - User.
     */
    public static final int DATA_TYPE_C_USER = 2;

    /**
     * Data Type - Tag.
     */
    public static final int DATA_TYPE_C_TAG = 3;

    /**
     * Private constructor.
     */
    private Vote() {
    }
}
