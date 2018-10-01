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
package org.b3log.symphony.repository;

import org.apache.commons.codec.digest.DigestUtils;
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
 * @version 1.0.0.1, Oct 1, 2018
 * @since 1.6.0
 */
@Repository
public class LinkRepository extends AbstractRepository {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LinkRepository.class);

    /**
     * Gets a link with the specified address.
     *
     * @param addr the specified address
     * @return a link, returns {@code null} if not found
     */
    public JSONObject getLink(final String addr) {
        final String hash = DigestUtils.sha1Hex(addr);
        final Query query = new Query().setFilter(new PropertyFilter(Link.LINK_ADDR_HASH, FilterOperator.EQUAL, hash)).
                setPageCount(1).setPageSize(1).setCurrentPageNum(1);
        try {
            final JSONObject result = get(query);
            final JSONArray links = result.optJSONArray(Keys.RESULTS);
            if (0 == links.length()) {
                return null;
            }

            return links.optJSONObject(0);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets link by address [addr=" + addr + ", hash=" + hash + "] failed", e);

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
