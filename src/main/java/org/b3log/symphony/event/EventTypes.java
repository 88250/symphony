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
package org.b3log.symphony.event;

/**
 * Event types.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Mar 5, 2013
 * @since 0.2.0
 */
public final class EventTypes {

    /**
     * Indicates a add article event.
     */
    public static final String ADD_ARTICLE = "Add Article";

    /**
     * Indicates a update article event.
     */
    public static final String UPDATE_ARTICLE = "Update Article";

    /**
     * Indicates an add comment to article event.
     */
    public static final String ADD_COMMENT_TO_ARTICLE = "Add Comment";

    /**
     * Private default constructor.
     */
    private EventTypes() {
    }
}
