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
package org.b3log.symphony.repository;

import java.util.List;
import org.b3log.latke.Keys;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.model.Link;
import org.b3log.symphony.model.Tag;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Tag-Link relation repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 10, 2016
 * @since 1.6.0
 */
@Repository
public class TagLinkRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public TagLinkRepository() {
        super(Tag.TAG + "_" + Link.LINK);
    }

    /**
     * Removes tag-links relations by the specified link id and tag id.
     *
     * @param linkId the specified link id
     * @param tagId the specified tag id
     * @return removed count
     * @throws RepositoryException repository exception
     */
    public int removeByLinkIdAndTagId(final String linkId, final String tagId) throws RepositoryException {
        final Query query = new Query().setFilter(
                CompositeFilterOperator.and(
                        new PropertyFilter(Link.LINK_T_ID, FilterOperator.EQUAL, linkId),
                        new PropertyFilter(Tag.TAG_T_ID, FilterOperator.EQUAL, tagId)
                )).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray relations = result.optJSONArray(Keys.RESULTS);
        for (int i = 0; i < relations.length(); i++) {
            final JSONObject rel = relations.optJSONObject(i);

            remove(rel.optString(Keys.OBJECT_ID));
        }

        return relations.length();
    }

    /**
     * Removes tag-links relations by the specified link id.
     *
     * @param linkId the specified link id
     * @return removed count
     * @throws RepositoryException repository exception
     */
    public int removeByLinkId(final String linkId) throws RepositoryException {
        final List<JSONObject> relations = getByLinkId(linkId);
        for (final JSONObject relation : relations) {
            remove(relation.optString(Keys.OBJECT_ID));
        }

        return relations.size();
    }

    /**
     * Gets tag-link relations by the specified link id.
     *
     * @param linkId the specified link id
     * @return for example      <pre>
     * [{
     *         "oId": "",
     *         "tagId": "",
     *         "linkId": linkId
     * }, ....], returns an empty list if not found
     * </pre>
     *
     * @throws RepositoryException repository exception
     */
    public List<JSONObject> getByLinkId(final String linkId) throws RepositoryException {
        final Query query = new Query().setFilter(
                new PropertyFilter(Link.LINK_T_ID, FilterOperator.EQUAL, linkId)).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        return CollectionUtils.jsonArrayToList(array);
    }

    /**
     * Gets tag-link relations by the specified tag id.
     *
     * @param tagId the specified tag id
     * @param currentPageNum the specified current page number, MUST greater then {@code 0}
     * @param pageSize the specified page size(count of a page contains objects), MUST greater then {@code 0}
     * @return for example      <pre>
     * {
     *     "pagination": {
     *       "paginationPageCount": 88250
     *     },
     *     "rslts": [{
     *         "oId": "",
     *         "tagId": tagId,
     *         "linkId": "",
     *         ....
     *     }, ....]
     * }
     * </pre>
     *
     * @throws RepositoryException repository exception
     */
    public JSONObject getByTagId(final String tagId, final int currentPageNum, final int pageSize)
            throws RepositoryException {
        final Query query = new Query().setFilter(
                new PropertyFilter(Tag.TAG_T_ID, FilterOperator.EQUAL, tagId)).
                addSort(Link.LINK_SCORE, SortDirection.DESCENDING).
                setCurrentPageNum(currentPageNum).
                setPageSize(pageSize).
                setPageCount(1);

        return get(query);
    }
}
