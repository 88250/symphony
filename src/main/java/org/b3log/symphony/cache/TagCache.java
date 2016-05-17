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
package org.b3log.symphony.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
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
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * Tag cache.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.1.0, May 17, 2016
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
     * Tag repository.
     */
    @Inject
    private TagRepository tagRepository;

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Icon tags.
     */
    private static final List<JSONObject> ICON_TAGS = new ArrayList<JSONObject>();

    /**
     * All tags.
     */
    private static final List<JSONObject> TAGS = new ArrayList<JSONObject>();

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

        final int end = fetchSize >= ICON_TAGS.size() ? ICON_TAGS.size() - 1 : fetchSize;

        return ICON_TAGS.subList(0, end);
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

        return TAGS;
    }

    /**
     * Loads icon tags.
     */
    public void loadIconTags() {
        ICON_TAGS.clear();

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
        TAGS.clear();

        final Query query = new Query().setFilter(
                new PropertyFilter(Tag.TAG_STATUS, FilterOperator.EQUAL, Tag.TAG_STATUS_C_VALID))
                .setCurrentPageNum(1).setPageSize(Integer.MAX_VALUE).setPageCount(1)
                .addSort(Tag.TAG_RANDOM_DOUBLE, SortDirection.ASCENDING);
        try {
            final JSONObject result = tagRepository.get(query);
            final List<JSONObject> tags = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));

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

            TAGS.addAll(tags);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Load all tags failed", e);
        }
    }
}
