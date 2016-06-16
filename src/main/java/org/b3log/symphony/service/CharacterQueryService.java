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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.repository.CharacterRepository;
import org.json.JSONObject;

/**
 * Character query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Jun 16, 2016
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
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Gets total character count.
     *
     * @return total character count
     */
    public int getTotalCharacterCount() {
        return langPropsService.get("characters").length();
    }

    /**
     * Gets all written character count.
     *
     * @return all written character count
     */
    public int getWrittenCharacterCount() {
        try {
            return (int) characterRepository.count(new Query());
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Counts characters failed", e);

            return 0;
        }
    }

    /**
     * Gets all written characters.
     *
     * <p>
     * <b>Note</b>: Just for testing.
     * </p>
     *
     * @return all written characters
     */
    public Set<JSONObject> getWrittenCharacters() {
        try {
            return CollectionUtils.jsonArrayToSet(characterRepository.get(new Query()).optJSONArray(Keys.RESULTS));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets characters failed", e);

            return Collections.emptySet();
        }
    }

    /**
     * Gets written character count of a user specified by the given user id.
     *
     * @param userId the given user id
     * @return user written character count
     */
    public int getWrittenCharacterCount(final String userId) {
        final Query query = new Query().setFilter(new PropertyFilter(
                org.b3log.symphony.model.Character.CHARACTER_USER_ID, FilterOperator.EQUAL, userId));

        try {
            return (int) characterRepository.count(query);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Counts user written characters failed", e);

            return 0;
        }
    }

    /**
     * Gets an unwritten character of the specified user id.
     *
     * @param userId the specified user id
     * @return character
     */
    public String getUnwrittenCharacter(final String userId) {
        final int maxRetries = 7;
        int retries = 0;

        while (retries < maxRetries) {
            retries++;

            final String characters = langPropsService.get("characters");
            final int index = RandomUtils.nextInt(characters.length());
            final String ret = StringUtils.trim(characters.substring(index, index + 1));

            final Query query = new Query();
            query.setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(org.b3log.symphony.model.Character.CHARACTER_USER_ID, FilterOperator.EQUAL, userId),
                    new PropertyFilter(org.b3log.symphony.model.Character.CHARACTER_CONTENT, FilterOperator.EQUAL, ret)
            ));

            query.setFilter(new PropertyFilter(org.b3log.symphony.model.Character.CHARACTER_CONTENT, FilterOperator.EQUAL, ret));

            try {
                if (characterRepository.count(query) > 0) {
                    continue;
                }

                return ret;
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Gets an unwritten character for user [id=" + userId + "] failed", e);
            }
        }

        return null;
    }

    /**
     * Gets an unwritten character.
     *
     * @return character
     */
    public String getUnwrittenCharacter() {
        final int maxRetries = 7;
        int retries = 0;

        while (retries < maxRetries) {
            retries++;

            final String characters = langPropsService.get("characters");
            final int index = RandomUtils.nextInt(characters.length());
            final String ret = StringUtils.trim(characters.substring(index, index + 1));

            final Query query = new Query().setFilter(
                    new PropertyFilter(org.b3log.symphony.model.Character.CHARACTER_CONTENT, FilterOperator.EQUAL, ret));

            try {
                if (characterRepository.count(query) > 0) {
                    continue;
                }

                return ret;
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Gets an unwritten character failed", e);
            }
        }

        return null;
    }
}
