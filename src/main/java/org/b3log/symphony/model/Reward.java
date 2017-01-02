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
 * This class defines all reward model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.0, Jun 29, 2016
 * @since 0.2.5
 */
public final class Reward {

    /**
     * Reward.
     */
    public static final String REWARD = "reward";

    /**
     * Rewards.
     */
    public static final String REWARDS = "rewards";

    /**
     * Key of sender id.
     */
    public static final String SENDER_ID = "senderId";

    /**
     * Key of data id.
     */
    public static final String DATA_ID = "dataId";

    /**
     * Key of type.
     */
    public static final String TYPE = "type";

    // Reward type constants
    /**
     * Reward type - reward article.
     */
    public static final int TYPE_C_ARTICLE = 0;

    /**
     * Reward type - comment.
     */
    public static final int TYPE_C_COMMENT = 1;

    /**
     * Reward type - user.
     */
    public static final int TYPE_C_USER = 2;
    
    /**
     * Reward type - thank article.
     */
    public static final int TYPE_C_THANK_ARTICLE = 3;

    /**
     * Private constructor.
     */
    private Reward() {
    }
}
