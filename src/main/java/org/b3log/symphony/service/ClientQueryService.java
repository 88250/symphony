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
package org.b3log.symphony.service;

import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Client;
import org.b3log.symphony.repository.ClientRepository;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Client query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Oct 18, 2012
 * @since 0.2.0
 */
@Service
public class ClientQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ClientQueryService.class.getName());

    /**
     * Client repository.
     */
    @Inject
    private ClientRepository clientRepository;

    /**
     * Adds a client by the specified administrator email.
     *
     * @param adminEmail the specified administrator email
     * @return client,returns {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getClientByAdminEmail(final String adminEmail) throws ServiceException {
        final Query query = new Query().setFilter(new PropertyFilter(Client.CLIENT_ADMIN_EMAIL, FilterOperator.EQUAL, adminEmail));
        try {
            final JSONObject result = clientRepository.get(query);
            final JSONArray array = result.optJSONArray(Keys.RESULTS);

            if (0 == array.length()) {
                return null;
            }

            return array.optJSONObject(0);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets client failed", e);
            throw new ServiceException(e);
        }
    }
}
