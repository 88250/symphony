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
package org.b3log.symphony.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Link;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Link repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 7, 2016
 * @since 1.6.0
 */
@Repository
public class LinkRepository extends AbstractRepository {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LinkRepository.class.getName());

    /**
     * Gets a link with the specified address.
     *
     * @param addr the specified address
     * @return a link, returns {@code null} if not found
     */
    public JSONObject getLink(final String addr) {
        final Query query = new Query();
        query.setFilter(new PropertyFilter(Link.LINK_ADDR, FilterOperator.EQUAL, addr)).
                setPageCount(1).setPageSize(1).setCurrentPageNum(1);

        try {
            final JSONObject result = get(query);
            final JSONArray links = result.optJSONArray(Keys.RESULTS);
            if (0 == links.length()) {
                return null;
            }

            return links.optJSONObject(0);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets link by address [" + addr + "]", e);

            return null;
        }
    }

    /**
     * Public constructor.
     */
    public LinkRepository() {
        super(Link.LINK);
    }
}
