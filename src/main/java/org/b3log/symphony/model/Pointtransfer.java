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

import org.b3log.symphony.util.Symphonys;

/**
 * This class defines all pointtransfer model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.14.1.0, Apr 5, 2016
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
    public static final String DATA_ID = "dataId";

    // Id constants
    /**
     * System.
     */
    public static final String ID_C_SYS = "sys";

    // Transfer type and sum constants
    /**
     * Transfer type - Initialization Income.
     */
    public static final int TRANSFER_TYPE_C_INIT = 0;

    /**
     * Transfer sum - Initialization.
     */
    public static final int TRANSFER_SUM_C_INIT = Symphonys.getInt("pointInit");

    /**
     * Transfer type - Add Article Outcome.
     */
    public static final int TRANSFER_TYPE_C_ADD_ARTICLE = 1;

    /**
     * Transfer sum - Add Article.
     */
    public static final int TRANSFER_SUM_C_ADD_ARTICLE = Symphonys.getInt("pointAddArticle");

    /**
     * Transfer type - Update Article Outcome.
     */
    public static final int TRANSFER_TYPE_C_UPDATE_ARTICLE = 2;

    /**
     * Transfer sum - Update Article.
     */
    public static final int TRANSFER_SUM_C_UPDATE_ARTICLE = Symphonys.getInt("pointUpdateArticle");

    /**
     * Transfer type - Add Comment Income/Outcome.
     */
    public static final int TRANSFER_TYPE_C_ADD_COMMENT = 3;

    /**
     * Transfer sum - Add Comment.
     */
    public static final int TRANSFER_SUM_C_ADD_COMMENT = Symphonys.getInt("porintAddComment");

    /**
     * Transfer sum - Add Self Article Comment.
     */
    public static final int TRANSFER_SUM_C_ADD_SELF_ARTICLE_COMMENT = Symphonys.getInt("pointAddSelfArticleComment");

    /**
     * Transfer type - Add Article Reward Outcome.
     */
    public static final int TRANSFER_TYPE_C_ADD_ARTICLE_REWARD = 4;

    /**
     * Transfer sum - Add Article Reward.
     */
    public static final int TRANSFER_SUM_C_ADD_ARTICLE_REWARD = Symphonys.getInt("pointAddArticleReward");

    /**
     * Transfer type - Article Reward Income/Outcome.
     */
    public static final int TRANSFER_TYPE_C_ARTICLE_REWARD = 5;

    /**
     * Transfer type - Invite Register Income.
     */
    public static final int TRANSFER_TYPE_C_INVITE_REGISTER = 6;

    /**
     * Transfer type - Invited Register Income.
     */
    public static final int TRANSFER_TYPE_C_INVITED_REGISTER = 7;

    /**
     * Transfer sum - Invite Register.
     */
    public static final int TRANSFER_SUM_C_INVITE_REGISTER = Symphonys.getInt("pointInviteRegister");

    /**
     * Transfer type - Activity - Daily Checkin Income.
     */
    public static final int TRANSFER_TYPE_C_ACTIVITY_CHECKIN = 8;

    /**
     * Transfer sum - Activity - Daily Checkin Min.
     */
    public static final int TRANSFER_SUM_C_ACTIVITY_CHECKIN_MIN = Symphonys.getInt("pointActivityCheckinMin");

    /**
     * Transfer sum - Activity - Daily Checkin Max.
     */
    public static final int TRANSFER_SUM_C_ACTIVITY_CHECKIN_MAX = Symphonys.getInt("pointActivityCheckinMax");

    /**
     * Transfer type - User Account to User Account.
     */
    public static final int TRANSFER_TYPE_C_ACCOUNT2ACCOUNT = 9;

    /**
     * Transfer type - Activity - 1A0001.
     */
    public static final int TRANSFER_TYPE_C_ACTIVITY_1A0001 = 10;

    /**
     * Transfer type - Activity - Daily Checkin Streak Income.
     */
    public static final int TRANSFER_TYPE_C_ACTIVITY_CHECKIN_STREAK = 11;

    /**
     * Transfer sum - Activity - Daily Checkin Streak.
     */
    public static final int TRANSFER_SUM_C_ACTIVITY_CHECKINT_STREAK = Symphonys.getInt("pointActivityCheckinStreak");

    /**
     * Transfer type - Activity - 1A0001 Income.
     */
    public static final int TRANSFER_TYPE_C_ACTIVITY_1A0001_COLLECT = 12;

    /**
     * Transfer type - Charge.
     */
    public static final int TRANSFER_TYPE_C_CHARGE = 13;

    /**
     * Transfer type - Comment Reward (Thank) Income/Outcome.
     */
    public static final int TRANSFER_TYPE_C_COMMENT_REWARD = 14;

    /**
     * Transfer type - Add Article Broadcast Outcome.
     */
    public static final int TRANSFER_TYPE_C_ADD_ARTICLE_BROADCAST = 15;

    /**
     * Transfer sum - Add Article.
     */
    public static final int TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST = Symphonys.getInt("pointAddArticleBroadcast");

    /**
     * Transfer type - Exchange.
     */
    public static final int TRANSFER_TYPE_C_EXCHANGE = 16;

    /**
     * Transfer type - Abuse Deduct.
     */
    public static final int TRANSFER_TYPE_C_ABUSE_DEDUCT = 17;

    /**
     * Transfer type - Activity - Yesterday Liveness Reward Income.
     */
    public static final int TRANSFER_TYPE_C_ACTIVITY_YESTERDAY_LIVENESS_REWARD = 18;

    /**
     * Transfer type - Stick Article.
     */
    public static final int TRANSFER_TYPE_C_STICK_ARTICLE = 19;

    /**
     * Transfer sum - Stick Article.
     */
    public static final int TRANSFER_SUM_C_STICK_ARTICLE = Symphonys.getInt("pointStickArticle");

    /**
     * Transfer type - At Participants.
     */
    public static final int TRANSFER_TYPE_C_AT_PARTICIPANTS = 20;

    /**
     * Transfer sum - At Participants.
     */
    public static final int TRANSFER_SUM_C_AT_PARTICIPANTS = Symphonys.getInt("pointAtParticipants");

    /**
     * Private constructor.
     */
    private Pointtransfer() {
    }
}
