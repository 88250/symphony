/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
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
package org.b3log.symphony.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.b3log.latke.logging.Logger;
import org.b3log.symphony.service.DomainQueryService;
import org.json.JSONObject;

/**
 * Domain cache.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.2.0, May 15, 2016
 * @since 1.4.0
 */
@Named
@Singleton
public class DomainCache {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DomainCache.class.getName());

    /**
     * Domain query service.
     */
    @Inject
    private DomainQueryService domainQueryService;

    /**
     * Domains.
     */
    private static final List<JSONObject> DOMAINS = new ArrayList<JSONObject>();

    /**
     * Gets domains with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return domains
     */
    public List<JSONObject> getDomains(final int fetchSize) {
        if (DOMAINS.isEmpty()) {
            return Collections.emptyList();
        }

        final int end = fetchSize >= DOMAINS.size() ? DOMAINS.size() : fetchSize;

        return DOMAINS.subList(0, end);
    }

    /**
     * Loads domains.
     */
    public void loadDomains() {
        DOMAINS.clear();
        DOMAINS.addAll(domainQueryService.getMostTagDomain(Integer.MAX_VALUE));
    }
}
