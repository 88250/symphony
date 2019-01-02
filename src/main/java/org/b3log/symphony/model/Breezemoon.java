/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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
package org.b3log.symphony.model;

/**
 * This class defines all breezemoon model relevant keys. https://github.com/b3log/symphony/issues/507
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.0, Jul 20, 2018
 * @since 2.8.0
 */
public final class Breezemoon {

    /**
     * Breezemoon.
     */
    public static final String BREEZEMOON = "breezemoon";

    /**
     * Breezemoons.
     */
    public static final String BREEZEMOONS = "breezemoons";

    /**
     * Key of breezemoon content.
     */
    public static final String BREEZEMOON_CONTENT = "breezemoonContent";

    /**
     * Key of breezemoon author id.
     */
    public static final String BREEZEMOON_AUTHOR_ID = "breezemoonAuthorId";

    /**
     * Key of breezemoon created at.
     */
    public static final String BREEZEMOON_CREATED = "breezemoonCreated";

    /**
     * Key of breezemoon updated at.
     */
    public static final String BREEZEMOON_UPDATED = "breezemoonUpdated";

    /**
     * Key of breezemoon IP.
     */
    public static final String BREEZEMOON_IP = "breezemoonIP";

    /**
     * Key of breezemoon UA.
     */
    public static final String BREEZEMOON_UA = "breezemoonUA";

    /**
     * Key of breezemoon status.
     */
    public static final String BREEZEMOON_STATUS = "breezemoonStatus";

    /**
     * Key of breezemoon city.
     */
    public static final String BREEZEMOON_CITY = "breezemoonCity";

    // Status constants
    /**
     * Breezemoon status - valid.
     */
    public static final int BREEZEMOON_STATUS_C_VALID = 0;

    /**
     * Breezemoon status - invalid.
     */
    public static final int BREEZEMOON_STATUS_C_INVALID = 1;

    //// Transient ////

    /**
     * Key of breezemoon author name.
     */
    public static final String BREEZEMOON_T_AUTHOR_NAME = "breezemoonAuthorName";

    /**
     * Key of breezemoon author thumbnail URL.
     */
    public static final String BREEZEMOON_T_AUTHOR_THUMBNAIL_URL = "breezemoonAuthorThumbnailURL";

    /**
     * Key of breezemoon create time.
     */
    public static final String BREEZEMOON_T_CREATE_TIME = "breezemoonCreateTime";
}

