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
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.model.Link;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.repository.LinkRepository;
import org.b3log.symphony.repository.TagLinkRepository;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Link query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 10, 2016
 * @since 1.6.0
 */
@Service
public class LinkForgeQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LinkForgeQueryService.class.getName());

    /**
     * Tag-Link repository.
     */
    @Inject
    private TagLinkRepository tagLinkRepository;

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
        final int LINK_MAX_COUNT = 50;

        final List<JSONObject> ret = new ArrayList<>();

        try {
            final List<JSONObject> iconTags = tagCache.getIconTags(Integer.MAX_VALUE);
            for (final JSONObject iconTag : iconTags) {
                final String tagId = iconTag.optString(Keys.OBJECT_ID);

                final JSONObject tag = new JSONObject();
                tag.put(Tag.TAG_TITLE, iconTag.optString(Tag.TAG_TITLE));
                tag.put(Tag.TAG_ICON_PATH, iconTag.optString(Tag.TAG_ICON_PATH));

                final JSONArray results = tagLinkRepository.getByTagId(tagId, 1, LINK_MAX_COUNT).
                        optJSONArray(Keys.RESULTS);
                if (results.length() < 1) {
                    continue;
                }

                final List<JSONObject> links = new ArrayList<>();
                for (int i = 0; i < results.length(); i++) {
                    final String linkId = results.optJSONObject(i).optString(Link.LINK_T_ID);
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
