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

import java.util.Collections;
import java.util.Set;
import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.repository.CharacterRepository;
import org.json.JSONObject;

/**
 * Character query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jun 15, 2016
 * @since 1.4.0
 */
@Service
public class CharacterQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CharacterQueryService.class.getName());

    /**
     * Character repository.
     */
    @Inject
    private CharacterRepository characterRepository;

    /**
     * Gets all characters.
     *
     * <p>
     * <b>Note</b>: just for testing.
     * </p>
     *
     * @return characters
     */
    public Set<JSONObject> getCharacters() {
        final Query query = new Query();

        try {
            final JSONObject result = characterRepository.get(query);

            return CollectionUtils.jsonArrayToSet(result.optJSONArray(Keys.RESULTS));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Query characters failed", e);

            return Collections.emptySet();
        }
    }

    /**
     * Gets characters of a user specified by the given user id.
     *
     * @param userId the given user id
     * @return a set of characters, returns an empty set if not found
     */
    public Set<JSONObject> getUserCharacters(final String userId) {
        final Query query = new Query().setFilter(new PropertyFilter(
                org.b3log.symphony.model.Character.CHARACTER_USER_ID, FilterOperator.EQUAL, userId));

        try {
            final JSONObject result = characterRepository.get(query);

            return CollectionUtils.jsonArrayToSet(result.optJSONArray(Keys.RESULTS));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Query user characters failed", e);

            return Collections.emptySet();
        }
    }
}
