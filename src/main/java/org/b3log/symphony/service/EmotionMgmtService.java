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
package org.b3log.symphony.service;

import javax.inject.Inject;

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Ids;
import org.b3log.symphony.model.Emotion;
import org.b3log.symphony.repository.EmotionRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Domain management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.3.2, Jul 29, 2016
 * @since 1.4.0
 */
@Service
public class EmotionMgmtService {
	
	private static final Logger LOGGER = Logger.getLogger(EmotionMgmtService.class.getName());
	 
	@Inject
	private EmotionRepository emotionRepository;
	
	@Inject
	private EmotionQueryService emtoionQueryService;
	
	public void setEmotionList(final JSONObject user, final String emotionList) throws ServiceException {
        Transaction transaction = null;
        if (null == user) {
            return;
        }
        String userId="null";
        try {
        	userId=user.getString("oId");
            JSONArray userEmotions=emotionRepository.getUserEmotions(userId);
            transaction = emotionRepository.beginTransaction();
            if(userEmotions!=null){
		    	for(int i=0;i<userEmotions.length();i++){
	    			emotionRepository.remove(userEmotions.optJSONObject(i).getString(Keys.OBJECT_ID));
	    	    }
            }
            String[] emotionArray=emotionList.split(",");	
            for(int i=0;i<emotionArray.length;i++){
            	 final JSONObject userEmotion = new JSONObject();
            	 userEmotion.put(Emotion.EmotionId, Ids.genTimeMillisId());
            	 userEmotion.put(Emotion.EmotionUser, userId);
            	 userEmotion.put(Emotion.EmotionContent, emotionArray[i]);
            	 userEmotion.put(Emotion.EmotionSort, i+1);
            	 userEmotion.put(Emotion.EmotionType, "0");//建议用枚举
                 emotionRepository.add(userEmotion);
            }
            transaction.commit();
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Updates user online status failed [id=" + userId + "]", e);
            if (null != transaction && transaction.isActive()) {
                transaction.rollback();
            }
            throw new ServiceException(e);
        } catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }

	public EmotionRepository getEmotionRepository() {
		return emotionRepository;
	}

	public void setEmotionRepository(EmotionRepository emotionRepository) {
		this.emotionRepository = emotionRepository;
	}

	public EmotionQueryService getEmtoionQueryService() {
		return emtoionQueryService;
	}

	public void setEmtoionQueryService(EmotionQueryService emtoionQueryService) {
		this.emtoionQueryService = emtoionQueryService;
	}
}
