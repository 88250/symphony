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
import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Liveness;
import org.b3log.symphony.repository.LivenessRepository;
import org.json.JSONObject;

/**
 * Liveness management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 22, 2016
 * @since 1.4.0
 */
@Service
public class LivenessMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LivenessMgmtService.class.getName());

    /**
     * Liveness repository.
     */
    @Inject
    private LivenessRepository livenessRepository;

    /**
     * Increments a field of the specified liveness.
     *
     * @param userId the specified user id
     * @param field the specified field
     */
    @Transactional
    public void incLiveness(final String userId, final String field) {
        final String date = DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd");

        try {
            JSONObject liveness = livenessRepository.getByUserAndDate(userId, date);
            if (null == liveness) {
                liveness = new JSONObject();

                liveness.put(Liveness.LIVENESS_USER_ID, userId);
                liveness.put(Liveness.LIVENESS_DATE, date);
                liveness.put(Liveness.LIVENESS_POINT, 0);
                liveness.put(Liveness.LIVENESS_ACTIVITY, 0);
                liveness.put(Liveness.LIVENESS_ARTICLE, 0);
                liveness.put(Liveness.LIVENESS_COMMENT, 0);
                liveness.put(Liveness.LIVENESS_PV, 0);
                liveness.put(Liveness.LIVENESS_REWARD, 0);
                liveness.put(Liveness.LIVENESS_THANK, 0);
                liveness.put(Liveness.LIVENESS_VOTE, 0);
                liveness.put(Liveness.LIVENESS_VOTE, 0);

                livenessRepository.add(liveness);
            }

            liveness.put(field, liveness.optInt(field) + 1);

            livenessRepository.update(liveness.optString(Keys.OBJECT_ID), liveness);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Updates a liveness [" + date + "] field [" + field + "] failed", e);
        }
    }
}
