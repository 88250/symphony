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
 * This class defines all follow model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jun 3, 2015
 * @since 0.2.5
 */
public final class Follow {

    /**
     * Follow.
     */
    public static final String FOLLOW = "follow";

    /**
     * Follows.
     */
    public static final String FOLLOWS = "follows";

    /**
     * Key of follower id.
     */
    public static final String FOLLOWER_ID = "followerId";

    /**
     * Key of following id.
     */
    public static final String FOLLOWING_ID = "followingId";

    /**
     * Key of following type.
     */
    public static final String FOLLOWING_TYPE = "followingType";

    // Following type constants
    /**
     * Following type - user.
     */
    public static final int FOLLOWING_TYPE_C_USER = 0;

    /**
     * Following type - tag.
     */
    public static final int FOLLOWING_TYPE_C_TAG = 1;

    /**
     * Following type - article.
     */
    public static final int FOLLOWING_TYPE_C_ARTICLE = 2;

    /**
     * Private constructor.
     */
    private Follow() {
    }
}
