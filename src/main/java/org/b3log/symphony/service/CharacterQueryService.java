/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.repository.CharacterRepository;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Character query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.2.2, Sep 20, 2016
 * @since 1.4.0
 */
@Service
public class CharacterQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(CharacterQueryService.class);

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
            final List<JSONObject> result = characterRepository.select("select count(DISTINCT characterContent) from "
                    + characterRepository.getName());
            if (null == result || result.isEmpty()) {
                return 0;
            }

            return result.get(0).optInt("count(DISTINCT characterContent)");
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
            return new HashSet<>(characterRepository.getList(new Query()));
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
     * Gets an unwritten character.
     *
     * @param userId the specified user id
     * @return character
     */
    public String getUnwrittenCharacter(final String userId) {
        final String ret = getUnwrittenCharacterRandom(userId);
        if (StringUtils.isNotBlank(ret)) {
            return ret;
        }

        return getUnwrittenCharacterOneByOne(userId);
    }

    /**
     * Gets an unwritten character (strategy: One By One).
     *
     * @param userId the specified user id
     * @return character
     */
    private String getUnwrittenCharacterOneByOne(final String userId) {
        final String characters = langPropsService.get("characters");

        int index = 0;
        while (true) {
            if (index > characters.length()) {
                return null; // All done
            }

            final String ret = StringUtils.trim(characters.substring(index, index + 1));

            index++;

            final Query query = new Query();
            query.setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(org.b3log.symphony.model.Character.CHARACTER_USER_ID, FilterOperator.EQUAL, userId),
                    new PropertyFilter(org.b3log.symphony.model.Character.CHARACTER_CONTENT, FilterOperator.EQUAL, ret)
            ));

            try {
                if (characterRepository.count(query) > 0) {
                    continue;
                }

                return ret;
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Gets an unwritten character for user [id=" + userId + "] failed", e);
            }
        }
    }

    /**
     * Gets an unwritten character (strategy: Random).
     *
     * @param userId the specified user id
     * @return character
     */
    private String getUnwrittenCharacterRandom(final String userId) {
        final String characters = langPropsService.get("characters");

        final int maxRetries = 7;
        int retries = 0;

        while (retries < maxRetries) {
            retries++;

            final int index = RandomUtils.nextInt(characters.length());
            final String ret = StringUtils.trim(characters.substring(index, index + 1));

            final Query query = new Query();
            query.setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(org.b3log.symphony.model.Character.CHARACTER_USER_ID, FilterOperator.EQUAL, userId),
                    new PropertyFilter(org.b3log.symphony.model.Character.CHARACTER_CONTENT, FilterOperator.EQUAL, ret)
            ));

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

    /**
     * Gets an unwritten character.
     *
     * @return character
     */
    public String getUnwrittenCharacter() {
        final String ret = getUnwrittenCharacterRandom();
        if (StringUtils.isNotBlank(ret)) {
            return ret;
        }

        return getUnwrittenCharacterOneByOne();
    }

    /**
     * Gets an unwritten character (strategy: Random).
     *
     * @return character
     */
    private String getUnwrittenCharacterRandom() {
        final String characters = langPropsService.get("characters");

        final int maxRetries = 7;
        int retries = 0;

        while (retries < maxRetries) {
            retries++;

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

    /**
     * Gets an unwritten character (strategy: One By One).
     *
     * @return character
     */
    private String getUnwrittenCharacterOneByOne() {
        final String characters = langPropsService.get("characters");

        int index = 0;
        while (true) {
            if (index > characters.length()) {
                return null; // All done
            }

            final String ret = StringUtils.trim(characters.substring(index, index + 1));

            index++;

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
    }
}
