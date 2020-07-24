/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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
package org.b3log.symphony.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Tag;
import org.json.JSONObject;

/**
 * User-Tag relation repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Apr 17, 2017
 * @since 0.2.0
 */
@Repository
public class UserTagRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public UserTagRepository() {
        super(User.USER + "_" + Tag.TAG);
    }

    /**
     * Removes user-tag relations by the specified user id and tag id.
     *
     * @param userId the specified user id
     * @param tagId  the specified tag id
     * @param type   the specified type
     * @throws RepositoryException repository exception
     */
    public void removeByUserIdAndTagId(final String userId, final String tagId, final int type) throws RepositoryException {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(User.USER + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, userId),
                new PropertyFilter(Tag.TAG + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, tagId),
                new PropertyFilter(Common.TYPE, FilterOperator.EQUAL, type))).setPage(1, Integer.MAX_VALUE).setPageCount(1);
        remove(query);
    }

    /**
     * Gets user-tag relations by the specified user id.
     *
     * @param userId         the specified user id
     * @param currentPageNum the specified current page number, MUST greater then {@code 0}
     * @param pageSize       the specified page size(count of a page contains objects), MUST greater then {@code 0}
     * @return for example      <pre>
     * {
     *     "pagination": {
     *       "paginationPageCount": 88250
     *     },
     *     "rslts": [{
     *         "oId": "",
     *         "tag_oId": "",
     *         "user_oId": userId,
     *         "type": "" // "creator"/"article"/"comment", a tag 'creator' is also an 'article' quoter
     *     }, ....]
     * }
     * </pre>
     * @throws RepositoryException repository exception
     */
    public JSONObject getByUserId(final String userId, final int currentPageNum, final int pageSize) throws RepositoryException {
        final Query query = new Query().setFilter(new PropertyFilter(User.USER + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, userId)).
                setPage(currentPageNum, pageSize).setPageCount(1);

        return get(query);
    }

    /**
     * Gets user-tag relations by the specified tag id.
     *
     * @param tagId          the specified tag id
     * @param currentPageNum the specified current page number, MUST greater then {@code 0}
     * @param pageSize       the specified page size(count of a page contains objects), MUST greater then {@code 0}
     * @return for example      <pre>
     * {
     *     "pagination": {
     *       "paginationPageCount": 88250
     *     },
     *     "rslts": [{
     *         "oId": "",
     *         "tag_oId": "",
     *         "user_oId": userId,
     *         "type": "" // "creator"/"article"/"comment", a tag 'creator' is also an 'article' quoter
     *     }, ....]
     * }
     * </pre>
     * @throws RepositoryException repository exception
     */
    public JSONObject getByTagId(final String tagId, final int currentPageNum, final int pageSize) throws RepositoryException {
        final Query query = new Query().setFilter(new PropertyFilter(Tag.TAG + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, tagId)).
                setPage(currentPageNum, pageSize).setPageCount(1);

        return get(query);
    }
}
