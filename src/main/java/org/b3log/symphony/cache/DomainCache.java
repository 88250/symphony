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
package org.b3log.symphony.cache;

import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.logging.Logger;
import org.b3log.symphony.service.DomainQueryService;
import org.b3log.symphony.util.JSONs;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Domain cache.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.2.3, Aug 31, 2018
 * @since 1.4.0
 */
@Singleton
public class DomainCache {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DomainCache.class);

    /**
     * Domains.
     */
    private static final List<JSONObject> DOMAINS = new ArrayList<>();

    /**
     * Lock.
     */
    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    /**
     * Domain query service.
     */
    @Inject
    private DomainQueryService domainQueryService;

    /**
     * Gets domains with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return domains
     */
    public List<JSONObject> getDomains(final int fetchSize) {
        LOCK.readLock().lock();
        try {
            if (DOMAINS.isEmpty()) {
                return Collections.emptyList();
            }

            final int end = fetchSize >= DOMAINS.size() ? DOMAINS.size() : fetchSize;

            return JSONs.clone(DOMAINS.subList(0, end));
        } finally {
            LOCK.readLock().unlock();
        }
    }

    /**
     * Loads domains.
     */
    public void loadDomains() {
        LOCK.writeLock().lock();
        try {
            DOMAINS.clear();
            DOMAINS.addAll(domainQueryService.getMostTagNaviDomains(Integer.MAX_VALUE));
        } finally {
            LOCK.writeLock().unlock();
        }
    }
}
