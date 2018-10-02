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
package org.b3log.symphony.service;

import org.b3log.latke.ioc.Inject;
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
    private static final Logger LOGGER = Logger.getLogger(RewardMgmtService.class);

    /**
     * Reward repository.
     */
    @Inject
    private RewardRepository rewardRepository;

    /**
     * Adds a reward with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     *                          {
     *                              "senderId"; "",
     *                              "dataId": "",
     *                              "type": int
     *                          }
     *                          </pre>
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
