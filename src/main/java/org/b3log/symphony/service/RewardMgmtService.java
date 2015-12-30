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
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.repository.RewardRepository;
import org.json.JSONObject;

/**
 * Reward management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jun 30, 2015
 * @since 1.3.0
 */
@Service
public class RewardMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(RewardMgmtService.class.getName());

    /**
     * Reward repository.
     */
    @Inject
    private RewardRepository rewardRepository;

    /**
     * Adds a reward with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "senderId"; "",
     *     "dataId": "",
     *     "type": int
     * }
     * </pre>
     *
     * @return reward id
     * @throws ServiceException service exception
     */
    @Transactional
    public String addReward(final JSONObject requestJSONObject) throws ServiceException {
        try {
            return rewardRepository.add(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds reward failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }
}
