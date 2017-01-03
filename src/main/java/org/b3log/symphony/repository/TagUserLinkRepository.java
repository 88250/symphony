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
package org.b3log.symphony.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Link;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Tag-User-Link relation repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.2, Dec 18, 2016
 * @since 1.6.0
 */
@Repository
public class TagUserLinkRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public TagUserLinkRepository() {
        super(Tag.TAG + "_" + User.USER + "_" + Link.LINK);
    }

    /**
     * Counts link (distinct(linkId)) with the specified tag id.
     *
     * @param tagId the specified tag id
     * @return count
     * @throws RepositoryException repository exception
     */
    public int countTagLink(final String tagId) throws RepositoryException {
        final List<JSONObject> result = select("SELECT count(DISTINCT(linkId)) AS `ret` FROM `" + getName()
                + "` WHERE `tagId` = ?", tagId);

        return result.get(0).optInt("ret");
    }

    /**
     * Updates link score with the specified tag id, link id and score.
     *
     * @param tagId  the specified tag id
     * @param linkId the specified link id
     * @param score  the specified score
     * @throws RepositoryException repository exception
     */
    public void updateTagLinkScore(final String tagId, final String linkId, final double score)
            throws RepositoryException {
        final Query query = new Query().setFilter(
                CompositeFilterOperator.and(
                        new PropertyFilter(Tag.TAG_T_ID, FilterOperator.EQUAL, tagId),
                        new PropertyFilter(Link.LINK_T_ID, FilterOperator.EQUAL, linkId)
                )).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray relations = result.optJSONArray(Keys.RESULTS);
        for (int i = 0; i < relations.length(); i++) {
            final JSONObject rel = relations.optJSONObject(i);
            rel.put(Link.LINK_SCORE, score);

            update(rel.optString(Keys.OBJECT_ID), rel);
        }
    }

    /**
     * Removes tag-user-link relations by the specified tag id, user id and link id.
     *
     * @param tagId  the specified tag id
     * @param userId the specified user id
     * @param linkId the specified link id
     * @throws RepositoryException repository exception
     */
    public void removeByTagIdUserIdAndLinkId(final String tagId, final String userId, final String linkId)
            throws RepositoryException {
        final Query query = new Query().setFilter(
                CompositeFilterOperator.and(
                        new PropertyFilter(Tag.TAG_T_ID, FilterOperator.EQUAL, tagId),
                        new PropertyFilter(UserExt.USER_T_ID, FilterOperator.EQUAL, userId),
                        new PropertyFilter(Link.LINK_T_ID, FilterOperator.EQUAL, linkId)
                )).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray relations = result.optJSONArray(Keys.RESULTS);
        for (int i = 0; i < relations.length(); i++) {
            final JSONObject rel = relations.optJSONObject(i);

            remove(rel.optString(Keys.OBJECT_ID));
        }
    }

    /**
     * Removes tag-user-link relations by the specified link id.
     *
     * @param linkId the specified link id
     * @throws RepositoryException repository exception
     */
    public void removeByLinkId(final String linkId) throws RepositoryException {
        final Query query = new Query().setFilter(
                new PropertyFilter(Link.LINK_T_ID, FilterOperator.EQUAL, linkId)).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray relations = result.optJSONArray(Keys.RESULTS);
        for (int i = 0; i < relations.length(); i++) {
            final JSONObject rel = relations.optJSONObject(i);

            remove(rel.optString(Keys.OBJECT_ID));
        }
    }

    /**
     * Gets link ids by the specified tag id and fetch size (distinct(linkId), order by score).
     *
     * @param tagId     the specified tag id
     * @param fetchSize the specified fetch size
     * @return a list of link id
     * @throws RepositoryException repository exception
     */
    public List<String> getLinkIdsByTagId(final String tagId, final int fetchSize) throws RepositoryException {
        final List<JSONObject> results = select("SELECT DISTINCT(`linkId`), `linkScore` FROM `" + getName()
                + "` WHERE `tagId` = ? ORDER BY `linkScore` DESC LIMIT ?", tagId, fetchSize);

        final List<String> ret = new ArrayList<>();
        for (final JSONObject result : results) {
            ret.add(result.optString(Link.LINK_T_ID));
        }

        return ret;
    }

    /**
     * Gets tag ids by the specified link id and fetch size (distinct(tagId), order by score).
     *
     * @param linkId    the specified link id
     * @param fetchSize the specified fetch size
     * @return a list of tag id
     * @throws RepositoryException repository exception
     */
    public List<String> getTagIdsByLinkId(final String linkId, final int fetchSize) throws RepositoryException {
        final List<JSONObject> results = select("SELECT DISTINCT(`tagId`), `linkScore` FROM `" + getName()
                + "` WHERE `linkId` = ? ORDER BY `linkScore` DESC LIMIT ?", linkId, fetchSize);

        final List<String> ret = new ArrayList<>();
        for (final JSONObject result : results) {
            ret.add(result.optString(Tag.TAG_T_ID));
        }

        return ret;
    }

    /**
     * Gets tag-link relations by the specified tag id, user id and fetch size (distinct(linkId), order by score).
     *
     * @param tagId     the specified tag id
     * @param userId    the specified user id
     * @param fetchSize the specified fetch size
     * @return a list of link id
     * @throws RepositoryException repository exception
     */
    public List<String> getByTagIdAndUserId(final String tagId, final String userId, final int fetchSize)
            throws RepositoryException {
        final List<JSONObject> results = select("SELECT `linkId` FROM `" + getName()
                + "` WHERE `tagId` = ? AND `userId` = ? ORDER BY `linkScore` DESC LIMIT ?", tagId, userId, fetchSize);

        final List<String> ret = new ArrayList<>();
        for (final JSONObject result : results) {
            ret.add(result.optString(Link.LINK_T_ID));
        }

        return ret;
    }
}
