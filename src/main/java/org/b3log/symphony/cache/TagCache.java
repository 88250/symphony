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
package org.b3log.symphony.cache;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.LatkeBeanManagerImpl;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.service.ShortLinkQueryService;
import org.b3log.symphony.util.JSONs;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * Tag cache.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.5.5.0, Nov 24, 2016
 * @since 1.4.0
 */
@Named
@Singleton
public class TagCache {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TagCache.class.getName());

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
        final JSONObject tag = (JSONObject) CACHE.get(id);
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
        final JSONObject tag = (JSONObject) CACHE.get(id);
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

        return new ArrayList<>(NEW_TAGS);
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

        return new ArrayList<>(ICON_TAGS.subList(0, end));
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

        return new ArrayList<>(TAGS);
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
    public void loadNewTags() {
        final LatkeBeanManager beanManager = LatkeBeanManagerImpl.getInstance();
        final TagRepository tagRepository = beanManager.getReference(TagRepository.class);

        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                setCurrentPageNum(1).setPageSize(Symphonys.getInt("newTagsCnt")).setPageCount(1);

        query.setFilter(new PropertyFilter(Tag.TAG_REFERENCE_CNT, FilterOperator.GREATER_THAN, 0));

        try {
            final JSONObject result = tagRepository.get(query);
            NEW_TAGS.clear();
            NEW_TAGS.addAll(CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS)));
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets new tags failed", e);
        }
    }

    /**
     * Loads icon tags.
     */
    public void loadIconTags() {
        final LatkeBeanManager beanManager = LatkeBeanManagerImpl.getInstance();
        final TagRepository tagRepository = beanManager.getReference(TagRepository.class);
        final ShortLinkQueryService shortLinkQueryService = beanManager.getReference(ShortLinkQueryService.class);

        final Query query = new Query().setFilter(
                CompositeFilterOperator.and(
                        new PropertyFilter(Tag.TAG_ICON_PATH, FilterOperator.NOT_EQUAL, ""),
                        new PropertyFilter(Tag.TAG_STATUS, FilterOperator.EQUAL, Tag.TAG_STATUS_C_VALID)))
                .setCurrentPageNum(1).setPageSize(Integer.MAX_VALUE).setPageCount(1)
                .addSort(Tag.TAG_RANDOM_DOUBLE, SortDirection.ASCENDING);
        try {
            final JSONObject result = tagRepository.get(query);
            final List<JSONObject> tags = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            final List<JSONObject> toUpdateTags = new ArrayList();
            for (final JSONObject tag : tags) {
                toUpdateTags.add(JSONs.clone(tag));
            }

            for (final JSONObject tag : tags) {
                String description = tag.optString(Tag.TAG_DESCRIPTION);
                String descriptionText = tag.optString(Tag.TAG_TITLE);
                if (StringUtils.isNotBlank(description)) {
                    description = shortLinkQueryService.linkTag(description);
                    description = Markdowns.toHTML(description);

                    tag.put(Tag.TAG_DESCRIPTION, description);
                    descriptionText = Jsoup.parse(description).text();
                }

                tag.put(Tag.TAG_T_DESCRIPTION_TEXT, descriptionText);
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
        final LatkeBeanManager beanManager = LatkeBeanManagerImpl.getInstance();
        final TagRepository tagRepository = beanManager.getReference(TagRepository.class);
        final ShortLinkQueryService shortLinkQueryService = beanManager.getReference(ShortLinkQueryService.class);

        final Query query = new Query().setFilter(
                new PropertyFilter(Tag.TAG_STATUS, FilterOperator.EQUAL, Tag.TAG_STATUS_C_VALID))
                .setCurrentPageNum(1).setPageSize(Integer.MAX_VALUE).setPageCount(1);
        try {
            final JSONObject result = tagRepository.get(query);
            final List<JSONObject> tags = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            // for legacy data migration
            final Transaction transaction = tagRepository.beginTransaction();
            try {
                for (final JSONObject tag : tags) {
                    String uri = tag.optString(Tag.TAG_URI);
                    if (StringUtils.isBlank(uri)) {
                        final String tagTitle = tag.optString(Tag.TAG_TITLE);
                        tag.put(Tag.TAG_URI, URLEncoder.encode(tagTitle, "UTF-8"));
                        tag.put(Tag.TAG_CSS, "");

                        tagRepository.update(tag.optString(Keys.OBJECT_ID), tag);

                        LOGGER.info("Migrated tag [title=" + tagTitle + "]");
                    }
                }

                transaction.commit();
            } catch (final RepositoryException | UnsupportedEncodingException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                LOGGER.log(Level.ERROR, "Migrates tag data failed", e);
            }

            final Iterator<JSONObject> iterator = tags.iterator();
            while (iterator.hasNext()) {
                final JSONObject tag = iterator.next();

                String title = tag.optString(Tag.TAG_TITLE);
                if (StringUtils.contains(title, " ") || StringUtils.contains(title, "　")) { // filter legacy data
                    iterator.remove();

                    continue;
                }

                if (!Tag.containsWhiteListTags(title)) {
                    if (!Tag.TAG_TITLE_PATTERN.matcher(title).matches() || title.length() > Tag.MAX_TAG_TITLE_LENGTH) {
                        iterator.remove();

                        continue;
                    }
                }

                String description = tag.optString(Tag.TAG_DESCRIPTION);
                String descriptionText = title;
                if (StringUtils.isNotBlank(description)) {
                    description = shortLinkQueryService.linkTag(description);
                    description = Markdowns.toHTML(description);

                    tag.put(Tag.TAG_DESCRIPTION, description);
                    descriptionText = Jsoup.parse(description).text();
                }

                tag.put(Tag.TAG_T_DESCRIPTION_TEXT, descriptionText);
                tag.put(Tag.TAG_T_TITLE_LOWER_CASE, tag.optString(Tag.TAG_TITLE).toLowerCase());
            }

            Collections.sort(tags, new Comparator<JSONObject>() {
                @Override
                public int compare(final JSONObject t1, final JSONObject t2) {
                    final String u1Title = t1.optString(Tag.TAG_T_TITLE_LOWER_CASE);
                    final String u2Title = t2.optString(Tag.TAG_T_TITLE_LOWER_CASE);

                    return u1Title.compareTo(u2Title);
                }
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
