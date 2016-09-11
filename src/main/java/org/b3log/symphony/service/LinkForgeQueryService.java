package org.b3log.symphony.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.repository.LinkRepository;
import org.b3log.symphony.repository.TagUserLinkRepository;
import org.json.JSONObject;

/**
 * Link query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Sep 11, 2016
 * @since 1.6.0
 */
@Service
public class LinkForgeQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LinkForgeQueryService.class.getName());

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
        final int LINK_MAX_COUNT = 50;

        final List<JSONObject> ret = new ArrayList<>();

        try {
            final List<JSONObject> cachedTags = tagCache.getTags();
            for (final JSONObject cachedTag : cachedTags) {
                if (cachedTag.optInt(Tag.TAG_LINK_CNT) < 1) {
                    continue; // XXX: optimize, reduce queries
                }

                final String tagId = cachedTag.optString(Keys.OBJECT_ID);

                final JSONObject tag = new JSONObject();
                tag.put(Tag.TAG_TITLE, cachedTag.optString(Tag.TAG_TITLE));
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

            Collections.sort(ret, new Comparator<JSONObject>() {
                @Override
                public int compare(final JSONObject tag1, final JSONObject tag2) {
                    return tag2.optInt(Tag.TAG_T_LINKS_CNT) - tag1.optInt(Tag.TAG_T_LINKS_CNT);
                }
            });
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
        final int TAG_MAX_COUNT = 50;
        final int LINK_MAX_COUNT = 20;

        final List<JSONObject> ret = new ArrayList<>();

        try {
            List<JSONObject> cachedTags = tagCache.getTags();

            Collections.sort(cachedTags, new Comparator<JSONObject>() {
                @Override
                public int compare(final JSONObject o1, final JSONObject o2) {
                    return o2.optInt(Tag.TAG_LINK_CNT) - o1.optInt(Tag.TAG_LINK_CNT);
                }
            });

            cachedTags = cachedTags.size() > TAG_MAX_COUNT ? cachedTags.subList(0, TAG_MAX_COUNT) : cachedTags;

            for (final JSONObject cachedTag : cachedTags) {
                if (cachedTag.optInt(Tag.TAG_LINK_CNT) < 1) {
                    continue; // XXX: optimize, reduce queries
                }

                final String tagId = cachedTag.optString(Keys.OBJECT_ID);

                final JSONObject tag = new JSONObject();
                tag.put(Tag.TAG_TITLE, cachedTag.optString(Tag.TAG_TITLE));
                tag.put(Tag.TAG_ICON_PATH, cachedTag.optString(Tag.TAG_ICON_PATH));

                // query link id
                final List<String> linkIds = tagUserLinkRepository.getByTagId(tagId, LINK_MAX_COUNT);
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

            Collections.sort(ret, new Comparator<JSONObject>() {
                @Override
                public int compare(final JSONObject tag1, final JSONObject tag2) {
                    return tag2.optInt(Tag.TAG_T_LINKS_CNT) - tag1.optInt(Tag.TAG_T_LINKS_CNT);
                }
            });
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets forged links failed", e);
        }

        return ret;
    }
}
