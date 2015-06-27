/*
 * Copyright (c) 2012-2015, b3log.org
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

import org.b3log.symphony.util.Symphonys;

/**
 * This class defines all pointtransfer model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jun 27, 2015
 * @since 1.3.0
 */
public final class Pointtransfer {

    /**
     * Pointtransfer.
     */
    public static final String POINTTRANSFER = "pointtransfer";

    /**
     * Pointtransfers.
     */
    public static final String POINTTRANSFERS = "pointtransfers";

    /**
     * Key of from user id.
     */
    public static final String FROM_ID = "fromId";

    /**
     * Key of to user id.
     */
    public static final String TO_ID = "toId";

    /**
     * Key of sum.
     */
    public static final String SUM = "sum";

    /**
     * Key of from balance.
     */
    public static final String FROM_BALANCE = "fromBalance";

    /**
     * Key of to balance.
     */
    public static final String TO_BALANCE = "toBalance";

    /**
     * Key of time.
     */
    public static final String TIME = "time";

    /**
     * Key of transfer type.
     */
    public static final String TYPE = "type";

    /**
     * Key of data id.
     */
    public static final String DATE_ID = "dataId";

    // Id constants
    /**
     * System.
     */
    public static final String ID_C_SYS = "sys";

    // Transfer type and sum constants
    /**
     * Transfer type - Initialization.
     */
    public static final int TRANSFER_TYPE_C_INIT = 0;

    /**
     * Transfer sum - Initialization.
     */
    public static final int TRANSFER_SUM_C_INIT = Symphonys.getInt("pointInit");

    /**
     * Transfer type - Add Article.
     */
    public static final int TRANSFER_TYPE_C_ADD_ARTICLE = 1;

    /**
     * Transfer sum - Add Article.
     */
    public static final int TRANSFER_SUM_C_ADD_ARTICLE = Symphonys.getInt("pointAddArticle");

    /**
     * Transfer type - Update Article.
     */
    public static final int TRANSFER_TYPE_C_UPDATE_ARTICLE = 2;

    /**
     * Transfer sum - Update Article.
     */
    public static final int TRANSFER_SUM_C_UPDATE_ARTICLE = Symphonys.getInt("pointUpdateArticle");

    /**
     * Transfer type - Add Comment.
     */
    public static final int TRANSFER_TYPE_C_ADD_COMMENT = 3;

    /**
     * Transfer sum - Add Comment.
     */
    public static final int TRANSFER_SUM_C_ADD_COMMENT = Symphonys.getInt("porintAddComment");
    
    /**
     * Transfer type - Add Article Reward.
     */
    public static final int TRANSFER_TYPE_C_ADD_ARTICLE_REWARD = 4;
    
    /**
     * Transfer type - Article Reward.
     */
    public static final int TRANSFER_TYPE_C_ARTICLE_REWARD = 5;

    /**
     * Private constructor.
     */
    private Pointtransfer() {
    }
}
