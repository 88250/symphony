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
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.repository.EmotionRepository;

/**
 * Emotion query service.
 * @author Zephyr
 */
@Service
public class EmotionQueryService {

    private static final Logger LOGGER = Logger.getLogger(EmotionQueryService.class.getName());
   
    @Inject
    private EmotionRepository emotionRepository;

    public String getEmojis(final String userId) {
        try {
        	String emojis=emotionRepository.getUserEmojis(userId);
        	if(emojis!=null&&emojis.length()!=0)
        		return emojis;
        	else
        		return null;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, e.getMessage());
            return null;
        }
    }
    

	public EmotionRepository getEmotionRepository() {
		return emotionRepository;
	}

	public void setEmotionRepository(EmotionRepository emotionRepository) {
		this.emotionRepository = emotionRepository;
	}

}