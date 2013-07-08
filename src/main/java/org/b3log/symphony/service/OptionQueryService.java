/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.repository.OptionRepository;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Option query service.
 * 
 * <p>
 *   <b>Note</b>: The {@link #onlineVisitorCount online visitor counting} is NOT cluster-safe.
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.4, Dec 21, 2012
 * @since 0.2.0
 */
@Service
public class OptionQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(OptionQueryService.class.getName());

    /**
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    /**
     * Online visitor cache.
     * 
     * <p>
     * &lt;ip, recentTime&gt;
     * </p>
     */
    private static final Map<String, Long> ONLINE_VISITORS = new HashMap<String, Long>();

    /**
     * Online visitor expiration in 5 minutes.
     */
    private static final int ONLINE_VISITOR_EXPIRATION = 300000;

    /**
     * Gets the online visitor count.
     * 
     * @return online visitor count
     */
    public int getOnlineVisitorCount() {
        removeExpiredOnlineVisitor();

        final int ret = ONLINE_VISITORS.size();

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
     * Refreshes online visitor count for the specified request.
     * 
     * @param request the specified request
     */
    public static void onlineVisitorCount(final HttpServletRequest request) {
        ONLINE_VISITORS.put(Requests.getRemoteAddr(request), System.currentTimeMillis());
        LOGGER.log(Level.TRACE, "Current online visitor count [{0}]", ONLINE_VISITORS.size());
    }

    /**
     * Removes the expired online visitor.
     */
    private static void removeExpiredOnlineVisitor() {
        final long currentTimeMillis = System.currentTimeMillis();

        final Iterator<Map.Entry<String, Long>> iterator = ONLINE_VISITORS.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, Long> onlineVisitor = iterator.next();

            if (currentTimeMillis > (onlineVisitor.getValue() + ONLINE_VISITOR_EXPIRATION)) {
                iterator.remove();
                LOGGER.log(Level.TRACE, "Removed online visitor[ip={0}]", onlineVisitor.getKey());
            }
        }
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
                setFilter(new PropertyFilter(Option.OPTION_CATEGORY, FilterOperator.EQUAL, Option.CATEGORY_C_STATISTIC));
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
}
