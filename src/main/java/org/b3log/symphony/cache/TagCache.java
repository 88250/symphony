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
package org.b3log.symphony.cache;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.*;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.util.JSONs;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tag cache.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.5.6.5, Aug 31, 2018
 * @since 1.4.0
 */
@Singleton
public class TagCache {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TagCache.class);

    /**
     * Icon tags.
     */
    private static final List<JSONObject> ICON_TAGS = new ArrayList<>();

    /**
     * New tags.
     */
    private static final List<JSONObject> NEW_TAGS = new ArrayList<>();

    /**
     * All tags.
     */
    private static final List<JSONObject> TAGS = new ArrayList<>();

    /**
     * &lt;title, URI&gt;
     */
    private static final Map<String, String> TITLE_URIS = new ConcurrentHashMap<>();

    /**
     * &lt;id, tag&gt;
     */
    private static final Map<String, JSONObject> CACHE = new ConcurrentHashMap<>();

    /**
     * Gets a tag by the specified tag id.
     *
     * @param id the specified tag id
     * @return tag, returns {@code null} if not found
     */
    public JSONObject getTag(final String id) {
        final JSONObject tag = CACHE.get(id);
        if (null == tag) {
            return null;
        }

        final JSONObject ret = JSONs.clone(tag);

        TITLE_URIS.put(ret.optString(Tag.TAG_TITLE), ret.optString(Tag.TAG_URI));

        return ret;
    }

    /**
     * Adds or updates the specified tag.
     *
     * @param tag the specified tag
     */
    public void putTag(final JSONObject tag) {
        CACHE.put(tag.optString(Keys.OBJECT_ID), JSONs.clone(tag));

        TITLE_URIS.put(tag.optString(Tag.TAG_TITLE), tag.optString(Tag.TAG_URI));
    }

    /**
     * Removes a tag by the specified tag id.
     *
     * @param id the specified tag id
     */
    public void removeTag(final String id) {
        final JSONObject tag = CACHE.get(id);
        if (null == tag) {
            return;
        }

        CACHE.remove(id);

        TITLE_URIS.remove(tag.optString(Tag.TAG_TITLE));
    }

    /**
     * Gets a tag URI with the specified tag title.
     *
     * @param title the specified tag title
     * @return tag URI, returns {@code null} if not found
     */
    public String getURIByTitle(final String title) {
        return TITLE_URIS.get(title);
    }

    /**
     * Gets new tags with the specified fetch size.
     *
     * @return new tags
     */
    public List<JSONObject> getNewTags() {
        if (NEW_TAGS.isEmpty()) {
            return Collections.emptyList();
        }

        return JSONs.clone(NEW_TAGS);
    }

    /**
     * Gets icon tags with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return icon tags
     */
    public List<JSONObject> getIconTags(final int fetchSize) {
        if (ICON_TAGS.isEmpty()) {
            return Collections.emptyList();
        }

        final int end = fetchSize >= ICON_TAGS.size() ? ICON_TAGS.size() : fetchSize;

        return JSONs.clone(ICON_TAGS.subList(0, end));
    }

    /**
     * Gets all tags.
     *
     * @return all tags
     */
    public List<JSONObject> getTags() {
        if (TAGS.isEmpty()) {
            return Collections.emptyList();
        }

        return JSONs.clone(TAGS);
    }

    /**
     * Loads all tags.
     */
    public void loadTags() {
        loadAllTags();
        loadIconTags();
        loadNewTags();
    }

    /**
     * Loads new tags.
     */
    private void loadNewTags() {
        final BeanManager beanManager = BeanManager.getInstance();
        final TagRepository tagRepository = beanManager.getReference(TagRepository.class);

        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                setCurrentPageNum(1).setPageSize(Symphonys.getInt("newTagsCnt")).setPageCount(1);

        query.setFilter(new PropertyFilter(Tag.TAG_REFERENCE_CNT, FilterOperator.GREATER_THAN, 0));

        try {
            final JSONObject result = tagRepository.get(query);
            NEW_TAGS.clear();
            NEW_TAGS.addAll(CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS)));
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets new tags failed", e);
        }
    }

    /**
     * Loads icon tags.
     */
    private void loadIconTags() {
        final BeanManager beanManager = BeanManager.getInstance();
        final TagRepository tagRepository = beanManager.getReference(TagRepository.class);

        final Query query = new Query().setFilter(
                CompositeFilterOperator.and(
                        new PropertyFilter(Tag.TAG_ICON_PATH, FilterOperator.NOT_EQUAL, ""),
                        new PropertyFilter(Tag.TAG_STATUS, FilterOperator.EQUAL, Tag.TAG_STATUS_C_VALID)))
                .setCurrentPageNum(1).setPageSize(Integer.MAX_VALUE).setPageCount(1)
                .addSort(Tag.TAG_RANDOM_DOUBLE, SortDirection.ASCENDING);
        try {
            final JSONObject result = tagRepository.get(query);
            final List<JSONObject> tags = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            final List<JSONObject> toUpdateTags = new ArrayList<>();
            for (final JSONObject tag : tags) {
                toUpdateTags.add(JSONs.clone(tag));
            }

            for (final JSONObject tag : tags) {
                Tag.fillDescription(tag);
                tag.put(Tag.TAG_T_TITLE_LOWER_CASE, tag.optString(Tag.TAG_TITLE).toLowerCase());
            }

            ICON_TAGS.clear();
            ICON_TAGS.addAll(tags);

            // Updates random double
            final Transaction transaction = tagRepository.beginTransaction();
            for (final JSONObject tag : toUpdateTags) {
                tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

                tagRepository.update(tag.optString(Keys.OBJECT_ID), tag);
            }
            transaction.commit();
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Load icon tags failed", e);
        }
    }

    /**
     * Loads all tags.
     */
    public void loadAllTags() {
        final BeanManager beanManager = BeanManager.getInstance();
        final TagRepository tagRepository = beanManager.getReference(TagRepository.class);

        final Query query = new Query().setFilter(
                new PropertyFilter(Tag.TAG_STATUS, FilterOperator.EQUAL, Tag.TAG_STATUS_C_VALID))
                .setCurrentPageNum(1).setPageSize(Integer.MAX_VALUE).setPageCount(1);
        try {
            final JSONObject result = tagRepository.get(query);
            final List<JSONObject> tags = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            final Iterator<JSONObject> iterator = tags.iterator();
            while (iterator.hasNext()) {
                final JSONObject tag = iterator.next();

                String title = tag.optString(Tag.TAG_TITLE);
                if ("".equals(title)
                        || StringUtils.contains(title, " ")
                        || StringUtils.contains(title, "　")) { // filter legacy data
                    iterator.remove();

                    continue;
                }

                if (!Tag.containsWhiteListTags(title)) {
                    if (!Tag.TAG_TITLE_PATTERN.matcher(title).matches() || title.length() > Tag.MAX_TAG_TITLE_LENGTH) {
                        iterator.remove();

                        continue;
                    }
                }

                Tag.fillDescription(tag);
                tag.put(Tag.TAG_T_TITLE_LOWER_CASE, tag.optString(Tag.TAG_TITLE).toLowerCase());
            }

            tags.sort((t1, t2) -> {
                final String u1Title = t1.optString(Tag.TAG_T_TITLE_LOWER_CASE);
                final String u2Title = t2.optString(Tag.TAG_T_TITLE_LOWER_CASE);

                return u1Title.compareTo(u2Title);
            });

            TAGS.clear();
            TAGS.addAll(tags);

            TITLE_URIS.clear();
            for (final JSONObject tag : tags) {
                TITLE_URIS.put(tag.optString(Tag.TAG_TITLE), tag.optString(Tag.TAG_URI));
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Load all tags failed", e);
        }
    }
}
