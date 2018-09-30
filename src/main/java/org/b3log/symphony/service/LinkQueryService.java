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

import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.repository.LinkRepository;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Link query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 21, 2018
 * @since 3.3.0
 */
@Service
public class LinkQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LinkQueryService.class);

    /**
     * Link repository.
     */
    @Inject
    private LinkRepository linkRepository;

    /**
     * Tag cache.
     */
    @Inject
    private TagCache tagCache;

    /**
     * Get top links with the specified size.
     *
     * @param size the specified size
     * @return links, returns an empty list if not found
     */
    public List<JSONObject> getTopLink(final int size) {
        final List<JSONObject> ret = new ArrayList<>();

        try {
            final List<JSONObject> links = linkRepository.select("SELECT\n" +
                    "\t*\n" +
                    "FROM\n" +
                    "\t`symphony_link`\n" +
                    "WHERE\n" +
                    "\tlinkPingErrCnt / linkPingCnt < 0.1\n" +
                    "AND linkTitle != \"\"\n" +
                    "AND linkAddr NOT LIKE \"%baidu.com%\" \n" +
                    "AND linkAddr NOT LIKE \"%weiyun.com%\"\n" +
                    "ORDER BY\n" +
                    "\tlinkClickCnt DESC\n" +
                    "LIMIT ?", size);
            ret.addAll(links);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Get top links failed", e);
        }

        return ret;
    }
}
