/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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

import org.b3log.symphony.util.Symphonys;

/**
 * This class defines all pointtransfer model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.27.0.0, Oct 1, 2018
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

    /**
     * Key of memo.
     */
    public static final String MEMO = "memo";

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
    public static final int TRANSFER_SUM_C_ADD_COMMENT = Symphonys.getInt("pointAddComment");

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
     * Transfer type - Activity - Character.
     */
    public static final int TRANSFER_TYPE_C_ACTIVITY_CHARACTER = 21;

    /**
     * Transfer sum - Activity - Character.
     */
    public static final int TRANSFER_SUM_C_ACTIVITY_CHARACTER = Symphonys.getInt("pointActivityCharacter");

    /**
     * Transfer type - Article Thank Income/Outcome.
     */
    public static final int TRANSFER_TYPE_C_ARTICLE_THANK = 22;

    /**
     * Transfer sum - Article Thank.
     */
    public static final int TRANSFER_SUM_C_ARTICLE_THANK = Symphonys.getInt("pointThankArticle");

    /**
     * Transfer type - Data Export.
     */
    public static final int TRANSFER_TYPE_C_DATA_EXPORT = 23;

    /**
     * Transfer sum - Data Export.
     */
    public static final int TRANSFER_SUM_C_DATA_EXPORT = Symphonys.getInt("pointDataExport");

    /**
     * Transfer type - Buy Invitecode.
     */
    public static final int TRANSFER_TYPE_C_BUY_INVITECODE = 24;

    /**
     * Transfer sum - Buy Invitecode.
     */
    public static final int TRANSFER_SUM_C_BUY_INVITECODE = Symphonys.getInt("pointInvitecode");

    /**
     * Transfer type - Invitecode Used.
     */
    public static final int TRANSFER_TYPE_C_INVITECODE_USED = 25;

    /**
     * Transfer sum - Invitecode Used.
     */
    public static final int TRANSFER_SUM_C_INVITECODE_USED = Symphonys.getInt("pointInvitecodeUsed");

    /**
     * Transfer type - Activity - Eating Snake.
     */
    public static final int TRANSFER_TYPE_C_ACTIVITY_EATINGSNAKE = 26;

    /**
     * Transfer sum - Activity - Eating Snake.
     */
    public static final int TRANSFER_SUM_C_ACTIVITY_EATINGSNAKE = Symphonys.getInt("pointActivityEatingSnake");

    /**
     * Transfer type - Activity - Eating Snake Income.
     */
    public static final int TRANSFER_TYPE_C_ACTIVITY_EATINGSNAKE_COLLECT = 27;

    /**
     * Transfer type - Invitation link Used.
     */
    public static final int TRANSFER_TYPE_C_INVITATION_LINK_USED = 28; // just a placeholder at present

    /**
     * Transfer type - Perfect Article.
     */
    public static final int TRANSFER_TYPE_C_PERFECT_ARTICLE = 29;

    /**
     * Transfer sum - Perfect Article.
     */
    public static final int TRANSFER_SUM_C_PERFECT_ARTICLE = Symphonys.getInt("pointPerfectArticle");

    /**
     * Transfer type - Activity - Gobang.
     */
    public static final int TRANSFER_TYPE_C_ACTIVITY_GOBANG = 30;

    /**
     * Transfer type - Activity - Gobang Income.
     */
    public static final int TRANSFER_TYPE_C_ACTIVITY_GOBANG_COLLECT = 31;

    /**
     * Transfer sum - Activity - Start Gobang.
     */
    public static final int TRANSFER_SUM_C_ACTIVITY_GOBANG_START = Symphonys.getInt("pointActivityGobang");

    /**
     * Transfer type - Update Comment Outcome.
     */
    public static final int TRANSFER_TYPE_C_UPDATE_COMMENT = 32;

    /**
     * Transfer sum - Update Comment.
     */
    public static final int TRANSFER_SUM_C_UPDATE_COMMENT = Symphonys.getInt("pointUpdateComment");

    /**
     * Transfer type - QnA Income/Outcome.
     */
    public static final int TRANSFER_TYPE_C_QNA_OFFER = 34;

    /**
     * Transfer type - Report Handled.
     */
    public static final int TRANSFER_TYPE_C_REPORT_HANDLED = 35;

    /**
     * Transfer sum - Report Handled.
     */
    public static final int TRANSFER_SUM_C_REPORT_HANDLED = Symphonys.getInt("pointReportHandled");

    /**
     * Transfer type - Change Username.
     */
    public static final int TRANSFER_TYPE_C_CHANGE_USERNAME = 36;

    /**
     * Transfer sum - Change Username.
     */
    public static final int TRANSFER_SUM_C_CHANGE_USERNAME = Symphonys.getInt("pointChangeUsername");

    /**
     * Private constructor.
     */
    private Pointtransfer() {
    }
}
