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
