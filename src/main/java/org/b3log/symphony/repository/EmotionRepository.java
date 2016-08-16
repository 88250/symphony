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

@Repository
public class EmotionRepository extends AbstractRepository {
    public EmotionRepository() {
		super("emotions");
	}
    
    public String getUserEmojis(final String userId) throws RepositoryException {
        final Query query = new Query();
        query.setFilter(new PropertyFilter(Emotion.EmotionUser, FilterOperator.EQUAL, userId));
        query.setFilter(new PropertyFilter(Emotion.EmotionType, FilterOperator.EQUAL, Emotion.EmotionType_Emoji));//建议用枚举
        query.addSort(Emotion.EmotionSort, SortDirection.ASCENDING);
        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);
        if (0 == array.length()) {
            return null;
        }
        String resultString="";
        try {
			
			for(int i=0;i<array.length();i++){
	        	resultString+=array.optJSONObject(i).get("emotionContent").toString();
	        	if(i!=array.length()-1)
	        		resultString+=",";
	        }
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return resultString;
    }
    
    public JSONArray getUserEmotions(final String userId) throws RepositoryException {
        final PropertyFilter pf=new PropertyFilter(Emotion.EmotionUser, FilterOperator.EQUAL, userId);
        final Query query = new Query().setFilter(pf);
        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);
        if (0 == array.length()) {
            return null;
        }
        return array;
    }
    
    @Override
    public void update(final String id, final JSONObject user) throws RepositoryException {
        super.update(id, user);
    }
}
