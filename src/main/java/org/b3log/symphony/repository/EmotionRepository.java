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
package org.b3log.symphony.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Emotion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Emotion repository.
 *
 * @author Zephyr
 * @version 1.0.0.0, Aug 16, 2016
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
        query.setFilter(new PropertyFilter(Emotion.EMOTION_USER_ID, FilterOperator.EQUAL, userId));
        query.setFilter(new PropertyFilter(Emotion.EMOTION_TYPE, FilterOperator.EQUAL, Emotion.EMOTION_TYPE_C_EMOJI));
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
