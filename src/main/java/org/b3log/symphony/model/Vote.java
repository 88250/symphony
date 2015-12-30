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
