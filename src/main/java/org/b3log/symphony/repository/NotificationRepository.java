/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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

import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Notification;

/**
 * Notification repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.1.0.0, Nov 17, 2018
 * @since 0.2.5
 */
@Repository
public class NotificationRepository extends AbstractRepository {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(NotificationRepository.class);

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
        remove(new Query().setFilter(new PropertyFilter(Notification.NOTIFICATION_DATA_ID, FilterOperator.EQUAL, dataId)));
    }

    /**
     * Checks whether has sent a notification to a user specified by the given user id with the specified data id and data type.
     *
     * @param userId               the given user id
     * @param dataId               the specified the specified data id
     * @param notificationDataType the specified notification data type
     * @return {@code ture} if sent, returns {@code false} otherwise
     */
    public boolean hasSentByDataIdAndType(final String userId, final String dataId, final int notificationDataType) {
        try {
            return 0 < count(new Query().setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId),
                    new PropertyFilter(Notification.NOTIFICATION_DATA_ID, FilterOperator.EQUAL, dataId),
                    new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, notificationDataType))));
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Checks [" + notificationDataType + "] notification sent failed [userId=" + userId + ", dataId=" + dataId + "]", e);

            return false;
        }
    }
}
