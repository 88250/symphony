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
