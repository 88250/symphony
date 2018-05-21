/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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
package org.b3log.symphony.service;

import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Breezemoon;
import org.b3log.symphony.repository.BreezemoonRepository;
import org.b3log.symphony.repository.UserRepository;
import org.json.JSONObject;

/**
 * Breezemoon management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, May 21, 2018
 * @since 2.8.0
 */
@Service
public class BreezemoonMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(BreezemoonMgmtService.class);

    /**
     * Breezemoon repository.
     */
    @Inject
    private BreezemoonRepository breezemoonRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Adds a breezemoon with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "breezemoonContent": "",
     *                          "breezemoonAuthorId": "",
     *                          "breezemoonUA": ""
     * @throws ServiceException service exception
     */
    @Transactional
    public void addBreezemoon(final JSONObject requestJSONObject) throws ServiceException {
        final JSONObject bm = new JSONObject();
        final String content = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
        if (optionQueryService.containReservedWord(content)) {
            throw new ServiceException(langPropsService.get("contentContainReservedWordLabel"));
        }
        bm.put(Breezemoon.BREEZEMOON_CONTENT, content);
        bm.put(Breezemoon.BREEZEMOON_AUTHOR_ID, requestJSONObject.optString(Breezemoon.BREEZEMOON_AUTHOR_ID));
        bm.put(Breezemoon.BREEZEMOON_UA, requestJSONObject.optString(Breezemoon.BREEZEMOON_UA));
        final long now = System.currentTimeMillis();
        bm.put(Breezemoon.BREEZEMOON_CREATED, now);
        bm.put(Breezemoon.BREEZEMOON_UPDATED, now);

        try {
            breezemoonRepository.add(bm);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Adds a bm failed", e);

            throw new ServiceException(langPropsService.get("systemErrLabel"));
        }
    }
}
