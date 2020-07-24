/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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
package org.b3log.symphony.repository;

import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Emotion;
import org.json.JSONObject;

import java.util.List;

/**
 * Emotion repository.
 *
 * @author <a href="https://hacpai.com/member/ZephyrJung">Zephyr</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.1, Mar 5, 2019
 * @since 1.5.0
 */
@Repository
public class EmotionRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public EmotionRepository() {
        super(Emotion.EMOTION);
    }

    /**
     * Gets a user's emotion (emoji with type=0).
     *
     * @param userId the specified user id
     * @return emoji string join with {@code ","}, returns {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public String getUserEmojis(final String userId) throws RepositoryException {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Emotion.EMOTION_USER_ID, FilterOperator.EQUAL, userId),
                new PropertyFilter(Emotion.EMOTION_TYPE, FilterOperator.EQUAL, Emotion.EMOTION_TYPE_C_EMOJI))).
                addSort(Emotion.EMOTION_SORT, SortDirection.ASCENDING);
        final List<JSONObject> result = getList(query);
        if (result.isEmpty()) {
            return null;
        }

        final StringBuilder retBuilder = new StringBuilder();
        for (int i = 0; i < result.size(); i++) {
            retBuilder.append(result.get(i).optString(Emotion.EMOTION_CONTENT));
            if (i != result.size() - 1) {
                retBuilder.append(",");
            }
        }
        return retBuilder.toString();
    }

    /**
     * Remove emotions by the specified user id.
     *
     * @param userId the specified user id
     * @throws RepositoryException repository exception
     */
    public void removeByUserId(final String userId) throws RepositoryException {
        remove(new Query().setFilter(new PropertyFilter(Emotion.EMOTION_USER_ID, FilterOperator.EQUAL, userId)));
    }
}
