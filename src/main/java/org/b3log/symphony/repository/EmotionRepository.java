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
package org.b3log.symphony.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Emotion;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Emotion repository.
 *
 * @author Zephyr
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.0, Aug 18, 2016
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
     *
     * @param userId the specified user id
     * @return emoji string join with {@code ","}, returns {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public String getUserEmojis(final String userId) throws RepositoryException {
        final Query query = new Query();
        query.setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Emotion.EMOTION_USER_ID, FilterOperator.EQUAL, userId),
                new PropertyFilter(Emotion.EMOTION_TYPE, FilterOperator.EQUAL, Emotion.EMOTION_TYPE_C_EMOJI)
        ));

        query.addSort(Emotion.EMOTION_SORT, SortDirection.ASCENDING);

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);
        if (0 == array.length()) {
            return null;
        }

        final StringBuilder retBuilder = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            retBuilder.append(array.optJSONObject(i).optString(Emotion.EMOTION_CONTENT));
            if (i != array.length() - 1) {
                retBuilder.append(",");
            }
        }

        return retBuilder.toString();
    }

    /**
     * Clears a user's emotions.
     *
     * @param userId the specified user id
     * @throws RepositoryException repository exception
     */
    public void removeUserEmotions(final String userId) throws RepositoryException {
        final PropertyFilter pf = new PropertyFilter(Emotion.EMOTION_USER_ID, FilterOperator.EQUAL, userId);
        final Query query = new Query().setFilter(pf);
        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);
        if (0 == array.length()) {
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            remove(array.optJSONObject(i).optString(Keys.OBJECT_ID));
        }
    }
}
