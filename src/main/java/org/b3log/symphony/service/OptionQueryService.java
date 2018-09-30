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

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.processor.channel.ArticleChannel;
import org.b3log.symphony.processor.channel.ArticleListChannel;
import org.b3log.symphony.processor.channel.ChatRoomChannel;
import org.b3log.symphony.processor.channel.UserChannel;
import org.b3log.symphony.repository.OptionRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.websocket.Session;
import java.util.List;
import java.util.Set;

/**
 * Option query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.4.0.12, Jan 30, 2018
 * @since 0.2.0
 */
@Service
public class OptionQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(OptionQueryService.class);

    /**
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Gets the online member count.
     *
     * @return online member count
     */
    public int getOnlineMemberCount() {
        int ret = 0;
        for (final Set<Session> value : UserChannel.SESSIONS.values()) {
            ret += value.size();
        }

        return ret;
    }

    /**
     * Gets the online visitor count.
     *
     * @return online visitor count
     */
    public int getOnlineVisitorCount() {
        final int ret = ArticleChannel.SESSIONS.size() + ArticleListChannel.SESSIONS.size() + ChatRoomChannel.SESSIONS.size() + getOnlineMemberCount();

        try {
            final JSONObject maxOnlineMemberCntRecord = optionRepository.get(Option.ID_C_STATISTIC_MAX_ONLINE_VISITOR_COUNT);
            final int maxOnlineVisitorCnt = maxOnlineMemberCntRecord.optInt(Option.OPTION_VALUE);

            if (maxOnlineVisitorCnt < ret) {
                // Updates the max online visitor count

                final Transaction transaction = optionRepository.beginTransaction();

                try {
                    maxOnlineMemberCntRecord.put(Option.OPTION_VALUE, String.valueOf(ret));
                    optionRepository.update(maxOnlineMemberCntRecord.optString(Keys.OBJECT_ID), maxOnlineMemberCntRecord);

                    transaction.commit();
                } catch (final RepositoryException e) {
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }

                    LOGGER.log(Level.ERROR, "Updates the max online visitor count failed", e);
                }
            }
        } catch (final RepositoryException ex) {
            LOGGER.log(Level.ERROR, "Gets online visitor count failed", ex);
        }

        return ret;
    }

    /**
     * Gets the statistic.
     *
     * @return statistic
     * @throws ServiceException service exception
     */
    public JSONObject getStatistic() throws ServiceException {
        final JSONObject ret = new JSONObject();

        final Query query = new Query().
                setFilter(new PropertyFilter(Option.OPTION_CATEGORY, FilterOperator.EQUAL, Option.CATEGORY_C_STATISTIC))
                .setPageCount(1);
        try {
            final JSONObject result = optionRepository.get(query);
            final JSONArray options = result.optJSONArray(Keys.RESULTS);

            for (int i = 0; i < options.length(); i++) {
                final JSONObject option = options.optJSONObject(i);
                ret.put(option.optString(Keys.OBJECT_ID), option.optInt(Option.OPTION_VALUE));
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets statistic failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Checks whether the specified content contains reserved words.
     *
     * @param content the specified content
     * @return {@code true} if it contains reserved words, returns {@code false} otherwise
     */
    public boolean containReservedWord(final String content) {
        if (StringUtils.isBlank(content)) {
            return false;
        }

        try {
            final List<JSONObject> reservedWords = getReservedWords();

            for (final JSONObject reservedWord : reservedWords) {
                if (content.contains(reservedWord.optString(Option.OPTION_VALUE))) {
                    return true;
                }
            }

            return false;
        } catch (final Exception e) {
            return true;
        }
    }

    /**
     * Gets the reserved words.
     *
     * @return reserved words
     * @throws ServiceException service exception
     */
    public List<JSONObject> getReservedWords() throws ServiceException {
        final Query query = new Query().
                setFilter(new PropertyFilter(Option.OPTION_CATEGORY, FilterOperator.EQUAL, Option.CATEGORY_C_RESERVED_WORDS));
        try {
            final JSONObject result = optionRepository.get(query);
            final JSONArray options = result.optJSONArray(Keys.RESULTS);

            return CollectionUtils.jsonArrayToList(options);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets reserved words failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Checks whether the specified word is a reserved word.
     *
     * @param word the specified word
     * @return {@code true} if it is a reserved word, returns {@code false} otherwise
     */
    public boolean isReservedWord(final String word) {
        final Query query = new Query().
                setFilter(CompositeFilterOperator.and(
                        new PropertyFilter(Option.OPTION_VALUE, FilterOperator.EQUAL, word),
                        new PropertyFilter(Option.OPTION_CATEGORY, FilterOperator.EQUAL, Option.CATEGORY_C_RESERVED_WORDS)
                ));
        try {
            return optionRepository.count(query) > 0;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Checks reserved word failed", e);

            return true;
        }
    }

    /**
     * Gets allow register option value.
     *
     * @return allow register option value, return {@code null} if not found
     */
    public String getAllowRegister() {
        try {
            final JSONObject result = optionRepository.get(Option.ID_C_MISC_ALLOW_REGISTER);

            return result.optString(Option.OPTION_VALUE);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets option [allow register] value failed", e);

            return null;
        }
    }

    /**
     * Gets the miscellaneous.
     *
     * @return misc
     * @throws ServiceException service exception
     */
    public List<JSONObject> getMisc() throws ServiceException {
        final Query query = new Query().
                setFilter(new PropertyFilter(Option.OPTION_CATEGORY, FilterOperator.EQUAL, Option.CATEGORY_C_MISC));
        try {
            final JSONObject result = optionRepository.get(query);
            final JSONArray options = result.optJSONArray(Keys.RESULTS);

            for (int i = 0; i < options.length(); i++) {
                final JSONObject option = options.optJSONObject(i);

                option.put("label", langPropsService.get(option.optString(Keys.OBJECT_ID) + "Label"));
            }

            return CollectionUtils.jsonArrayToList(options);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets misc failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets an option by the specified id.
     *
     * @param optionId the specified id
     * @return option, return {@code null} if not found
     */
    public JSONObject getOption(final String optionId) {
        try {
            final JSONObject ret = optionRepository.get(optionId);

            if (null == ret) {
                return null;
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets an option [optionId=" + optionId + "] failed", e);

            return null;
        }
    }
}
