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

import org.b3log.latke.Keys;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.repository.LinkRepository;
import org.b3log.symphony.repository.TagUserLinkRepository;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Link forge query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.4, Apr 21, 2017
 * @since 1.6.0
 */
@Service
public class LinkForgeQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LinkForgeQueryService.class);

    /**
     * Max tag count.
     */
    private static final int TAG_MAX_COUNT = Symphonys.getInt("forge.link.maxTagCnt");

    /**
     * Max link count.
     */
    private static final int LINK_MAX_COUNT = Symphonys.getInt("forge.link.maxCnt");

    /**
     * Tag reference count threshold.
     */
    private static final int TAG_REF_COUNT = Symphonys.getInt("forge.link.tagRefCnt");

    /**
     * Tag-User-Link repository.
     */
    @Inject
    private TagUserLinkRepository tagUserLinkRepository;

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
     * Gets user's nice links.
     *
     * @param userId the specified user id
     * @return a list of tags with links, each of tag like this,      <pre>
     * {
     *     "tagTitle": "",
     *     "tagIconPath": "",
     *     "tagLinks: [{
     *         "linkAddr": "",
     *         "linkTitle": "",
     *         ....
     *     }, ....]
     * }
     * </pre>
     */
    public List<JSONObject> getUserForgedLinks(final String userId) {
        final List<JSONObject> ret = new ArrayList<>();

        try {
            List<JSONObject> cachedTags = tagCache.getTags();
            Collections.sort(cachedTags, (o1, o2) -> o2.optInt(Tag.TAG_LINK_CNT) - o1.optInt(Tag.TAG_LINK_CNT));

            for (final JSONObject cachedTag : cachedTags) {
                cachedTags = cachedTags.size() > TAG_MAX_COUNT ? cachedTags.subList(0, TAG_MAX_COUNT) : cachedTags;

                if (cachedTag.optInt(Tag.TAG_LINK_CNT) < 1
                        || cachedTag.optInt(Tag.TAG_REFERENCE_CNT) < TAG_REF_COUNT) {
                    continue; // XXX: optimize, reduce queries
                }

                final String tagId = cachedTag.optString(Keys.OBJECT_ID);

                final JSONObject tag = new JSONObject();
                tag.put(Tag.TAG_TITLE, cachedTag.optString(Tag.TAG_TITLE));
                tag.put(Tag.TAG_URI, cachedTag.optString(Tag.TAG_URI));
                tag.put(Tag.TAG_ICON_PATH, cachedTag.optString(Tag.TAG_ICON_PATH));

                // query link id
                final List<String> linkIds = tagUserLinkRepository.getByTagIdAndUserId(tagId, userId, LINK_MAX_COUNT);
                if (linkIds.isEmpty()) {
                    continue;
                }

                // get link by id
                final List<JSONObject> links = new ArrayList<>();
                for (final String linkId : linkIds) {
                    links.add(linkRepository.get(linkId));
                }

                tag.put(Tag.TAG_T_LINKS, (Object) links);
                tag.put(Tag.TAG_T_LINKS_CNT, links.size());

                ret.add(tag);
            }

            Collections.sort(ret, (tag1, tag2) -> tag2.optInt(Tag.TAG_T_LINKS_CNT) - tag1.optInt(Tag.TAG_T_LINKS_CNT));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets forged links failed", e);
        }

        return ret;
    }

    /**
     * Gets nice links.
     *
     * @return a list of tags with links, each of tag like this,      <pre>
     * {
     *     "tagTitle": "",
     *     "tagIconPath": "",
     *     "tagLinks: [{
     *         "linkAddr": "",
     *         "linkTitle": "",
     *         ....
     *     }, ....]
     * }
     * </pre>
     */
    public List<JSONObject> getForgedLinks() {
        final List<JSONObject> ret = new ArrayList<>();

        try {
            List<JSONObject> cachedTags = tagCache.getTags();

            Collections.sort(cachedTags, (o1, o2) -> o2.optInt(Tag.TAG_LINK_CNT) - o1.optInt(Tag.TAG_LINK_CNT));

            cachedTags = cachedTags.size() > TAG_MAX_COUNT ? cachedTags.subList(0, TAG_MAX_COUNT) : cachedTags;

            for (final JSONObject cachedTag : cachedTags) {
                if (cachedTag.optInt(Tag.TAG_LINK_CNT) < 1
                        || cachedTag.optInt(Tag.TAG_REFERENCE_CNT) < TAG_REF_COUNT) {
                    continue; // XXX: optimize, reduce queries
                }

                final String tagId = cachedTag.optString(Keys.OBJECT_ID);

                final JSONObject tag = new JSONObject();
                tag.put(Tag.TAG_TITLE, cachedTag.optString(Tag.TAG_TITLE));
                tag.put(Tag.TAG_URI, cachedTag.optString(Tag.TAG_URI));
                tag.put(Tag.TAG_ICON_PATH, cachedTag.optString(Tag.TAG_ICON_PATH));

                // query link id
                final List<String> linkIds = tagUserLinkRepository.getLinkIdsByTagId(tagId, LINK_MAX_COUNT);
                if (linkIds.isEmpty()) {
                    continue;
                }

                // get link by id
                final List<JSONObject> links = new ArrayList<>();
                for (final String linkId : linkIds) {
                    links.add(linkRepository.get(linkId));
                }

                tag.put(Tag.TAG_T_LINKS, (Object) links);
                tag.put(Tag.TAG_T_LINKS_CNT, links.size());

                ret.add(tag);
            }

            Collections.sort(ret, (tag1, tag2) -> tag2.optInt(Tag.TAG_T_LINKS_CNT) - tag1.optInt(Tag.TAG_T_LINKS_CNT));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets forged links failed", e);
        }

        return ret;
    }
}
