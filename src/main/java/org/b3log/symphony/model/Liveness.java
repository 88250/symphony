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
import org.json.JSONObject;

/**
 * This class defines all liveness model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 22, 2016
 * @since 1.4.0
 */
public final class Liveness {

    /**
     * Liveness.
     */
    public static final String LIVENESS = "liveness";

    /**
     * Key of user id.
     */
    public static final String LIVENESS_USER_ID = "livenessUserId";

    /**
     * Key of liveness date.
     */
    public static final String LIVENESS_DATE = "livenessDate";

    /**
     * Key of liveness point.
     */
    public static final String LIVENESS_POINT = "livenessPoint";

    /**
     * Key of liveness article.
     */
    public static final String LIVENESS_ARTICLE = "livenessArticle";

    /**
     * Key of liveness comment.
     */
    public static final String LIVENESS_COMMENT = "livenessComment";

    /**
     * Key of liveness activity.
     */
    public static final String LIVENESS_ACTIVITY = "livenessActivity";

    /**
     * Key of liveness thank.
     */
    public static final String LIVENESS_THANK = "livenessThank";

    /**
     * Key of liveness vote.
     */
    public static final String LIVENESS_VOTE = "livenessVote";

    /**
     * Key of liveness reward.
     */
    public static final String LIVENESS_REWARD = "livenessReward";

    /**
     * Key of liveness PV.
     */
    public static final String LIVENESS_PV = "livenessPV";

    /**
     * Calculates point of the specified liveness.
     *
     * @param liveness the specified liveness
     * @return point
     */
    public static int calcPoint(final JSONObject liveness) {
        final float activityPer = Symphonys.getFloat("activitYesterdayLivenessReward.activity.perPoint");
        final float articlePer = Symphonys.getFloat("activitYesterdayLivenessReward.article.perPoint");
        final float commentPer = Symphonys.getFloat("activitYesterdayLivenessReward.comment.perPoint");
        final float pvPer = Symphonys.getFloat("activitYesterdayLivenessReward.pv.perPoint");
        final float rewardPer = Symphonys.getFloat("activitYesterdayLivenessReward.reward.perPoint");
        final float thankPer = Symphonys.getFloat("activitYesterdayLivenessReward.thank.perPoint");
        final float votePer = Symphonys.getFloat("activitYesterdayLivenessReward.vote.perPoint");

        final int activity = liveness.optInt(Liveness.LIVENESS_ACTIVITY);
        final int article = liveness.optInt(Liveness.LIVENESS_ARTICLE);
        final int comment = liveness.optInt(Liveness.LIVENESS_COMMENT);
        int pv = liveness.optInt(Liveness.LIVENESS_PV);
        if (pv > 500) {
            pv = 500;
        }
        final int reward = liveness.optInt(Liveness.LIVENESS_REWARD);
        final int thank = liveness.optInt(Liveness.LIVENESS_THANK);
        int vote = liveness.optInt(Liveness.LIVENESS_VOTE);
        if (vote > 10) {
            vote = 10;
        }

        final int activityPoint = (int) (activity * activityPer);
        final int articlePoint = (int) (article * articlePer);
        final int commentPoint = (int) (comment * commentPer);
        final int pvPoint = (int) (pv * pvPer);
        final int rewardPoint = (int) (reward * rewardPer);
        final int thankPoint = (int) (thank * thankPer);
        final int votePoint = (int) (vote * votePer);

        int ret = activityPoint + articlePoint + commentPoint + pvPoint + rewardPoint + thankPoint + votePoint;

        final int max = Symphonys.getInt("activitYesterdayLivenessReward.maxPoint");
        if (ret > max) {
            ret = max;
        }

        return ret;
    }
}
