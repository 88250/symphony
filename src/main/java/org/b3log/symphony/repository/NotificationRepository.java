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
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Notification;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Notification repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Mar 19, 2016
 * @since 0.2.5
 */
@Repository
public class NotificationRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public NotificationRepository() {
        super(Notification.NOTIFICATION);
    }

    /**
     * Removes notifications by the specified data id.
     *
     * @param dataId the specified data id
     * @throws RepositoryException repository exception
     */
    public void removeByDataId(final String dataId) throws RepositoryException {
        final Query query = new Query().setFilter(
                new PropertyFilter(Notification.NOTIFICATION_DATA_ID, FilterOperator.EQUAL, dataId)).
                setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        for (int i = 0; i < array.length(); i++) {
            final JSONObject notification = array.optJSONObject(i);
            remove(notification.optString(Keys.OBJECT_ID));
        }
    }
}
