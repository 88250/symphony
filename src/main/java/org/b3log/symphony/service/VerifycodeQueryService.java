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
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Verifycode;
import org.b3log.symphony.repository.VerifycodeRepository;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Verifycode query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jul 3, 2015
 * @since 1.3.0
 */
@Service
public class VerifycodeQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(VerifycodeQueryService.class.getName());

    /**
     * Verifycode repository.
     */
    @Inject
    private VerifycodeRepository verifycodeRepository;

    /**
     * Gets a verifycode with the specified code.
     *
     * @param code the specified code
     * @return verifycode, returns {@code null} if not found
     */
    public JSONObject getVerifycode(final String code) {
        final Query query = new Query().setFilter(new PropertyFilter(Verifycode.CODE, FilterOperator.EQUAL, code));

        try {
            final JSONObject result = verifycodeRepository.get(query);
            final JSONArray codes = result.optJSONArray(Keys.RESULTS);
            if (0 == codes.length()) {
                return null;
            }

            return codes.optJSONObject(0);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets verifycode error", e);

            return null;
        }
    }
}
