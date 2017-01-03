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
 * This class defines all revision model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Apr 20, 2016
 * @since 1.4.0
 */
public final class Revision {

    /**
     * Revision.
     */
    public static final String REVISION = "revision";

    /**
     * Revisions.
     */
    public static final String REVISIONS = "revisions";

    /**
     * Key of revision data type.
     */
    public static final String REVISION_DATA_TYPE = "revisionDataType";

    /**
     * Key of revision data id.
     */
    public static final String REVISION_DATA_ID = "revisionDataId";

    /**
     * Key of revision data.
     */
    public static final String REVISION_DATA = "revisionData";

    /**
     * Key of revision author id.
     */
    public static final String REVISION_AUTHOR_ID = "revisionAuthorId";

    // Data type constants
    /**
     * Data type - article.
     */
    public static final int DATA_TYPE_C_ARTICLE = 0;
}
