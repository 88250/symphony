/*
 * Copyright (c) 2012, B3log Team
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.ServiceException;
import org.b3log.symphony.model.Statistic;
import org.b3log.symphony.repository.StatisticRepository;
import org.json.JSONObject;

/**
 * Statistic management service.
 * 
 * <p>
 *   <b>Note</b>: The {@link #onlineVisitorCount online visitor counting} is NOT cluster-safe.
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Oct 6, 2012
 * @since 0.2.0
 */
public final class StatisticQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(StatisticQueryService.class.getName());
    /**
     * Singleton.
     */
    private static final StatisticQueryService SINGLETON = new StatisticQueryService();
    /**
     * Statistic repository.
     */
    private StatisticRepository statisticRepository = StatisticRepository.getInstance();
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
    public static int getOnlineVisitorCount() {
        removeExpiredOnlineVisitor();
        
        return ONLINE_VISITORS.size();
    }

    /**
     * Refreshes online visitor count for the specified request.
     * 
     * @param request the specified request
     */
    public static void onlineVisitorCount(final HttpServletRequest request) {
        ONLINE_VISITORS.put(request.getRemoteAddr(), System.currentTimeMillis());
        LOGGER.log(Level.FINEST, "Current online visitor count [{0}]", ONLINE_VISITORS.size());
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
                LOGGER.log(Level.FINEST, "Removed online visitor[ip={0}]", onlineVisitor.getKey());
            }
        }
    }

    /**
     * Adds the statistic.
     * 
     * @return statistic
     * @throws ServiceException service exception
     */
    public JSONObject getStatistic() throws ServiceException {
        try {
            return statisticRepository.get(Statistic.STATISTIC);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets statistic failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the {@link StatisticMgmtService} singleton.
     *
     * @return the singleton
     */
    public static StatisticQueryService getInstance() {
        return SINGLETON;
    }

    /**
     * Private constructor.
     */
    private StatisticQueryService() {
    }
}
