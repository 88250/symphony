/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
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
package org.b3log.symphony.service;

import javax.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.repository.NotificationRepository;
import org.json.JSONObject;

/**
 * Notification query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 1, 2013
 * @since 0.2.5
 */
@Service
public class NotificationQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(NotificationQueryService.class.getName());

    /**
     * Notification repository.
     */
    @Inject
    private NotificationRepository notificationRepository;

    /**
     * Adds a 'comment' type notification with the specified request json object.
     * 
     * @param requestJSONObject the specified request json object, for example,
     * <pre>
     * {
     *     "userId"; "",
     *     "dataId": ""
     * }
     * </pre>
     * @throws ServiceException 
     */
    @Transactional
    public void getCommentedNotifications(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_COMMENT);

            throw new RepositoryException();
            // addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=comment] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }
}
