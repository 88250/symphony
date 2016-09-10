package org.b3log.symphony.service;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.model.Tag;
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
            final List<JSONObject> iconTags = tagCache.getIconTags(Integer.SIZE);
            for (final JSONObject iconTag : iconTags) {
                final JSONObject tag = new JSONObject();
                final String tagId = tag.optString(Keys.OBJECT_ID);

                tag.put(Tag.TAG_TITLE, iconTag.optString(Tag.TAG_TITLE));

                final JSONArray links = tagLinkRepository.getByTagId(tagId, 1, LINK_MAX_COUNT).
                        optJSONArray(Keys.RESULTS);
                tag.put(Tag.TAG_T_LINKS, links);

                ret.add(tag);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets forged links failed", e);
        }

        return ret;
    }
}
