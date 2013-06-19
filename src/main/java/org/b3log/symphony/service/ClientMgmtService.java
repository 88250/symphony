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

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.symphony.repository.ClientRepository;
import org.json.JSONObject;

/**
 * Client management service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Oct 18, 2012
 * @since 0.2.0
 */
public final class ClientMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ClientMgmtService.class.getName());
    /**
     * Singleton.
     */
    private static final ClientMgmtService SINGLETON = new ClientMgmtService();
    /**
     * Client repository.
     */
    private ClientRepository clientRepository = ClientRepository.getInstance();
    /**
     * Language service.
     */
    private LangPropsService langPropsService = LangPropsService.getInstance();

    /**
     * Adds a client by the specified request json object.
     *
     * @param requestJSONObject the specified request json object (client), for example,
     * <pre>
     * {
     *     "oId": "",
     *     "clientName": "",
     *     "clientVersion": "",
     *     "clientHost": "",
     *     "clientAdminEmail": "",
     *     ....
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public void addClient(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = clientRepository.beginTransaction();

        try {
            final String oldClientId = requestJSONObject.optString(Keys.OBJECT_ID);
            final JSONObject oldClient = clientRepository.get(oldClientId);

            if (null != oldClient) {
                throw new ServiceException(langPropsService.get("updateFailLabel"));
            }

            // Add
            clientRepository.add(requestJSONObject);
            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Adds client failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates a client by the specified request json object.
     *
     * @param requestJSONObject the specified request json object (client), for example,
     * <pre>
     * {
     *     "oId": "",
     *     "clientName": "",
     *     "clientVersion": "",
     *     "clientHost": "",
     *     "clientAdminEmail": "",
     *     ....
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public void updateClient(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = clientRepository.beginTransaction();

        try {
            final String oldClientId = requestJSONObject.optString(Keys.OBJECT_ID);
            final JSONObject oldClient = clientRepository.get(oldClientId);

            if (null == oldClient) {
                throw new ServiceException(langPropsService.get("updateFailLabel"));
            }

            // Update
            clientRepository.update(oldClientId, requestJSONObject);
            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates client failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the {@link ClientMgmtService} singleton.
     *
     * @return the singleton
     */
    public static ClientMgmtService getInstance() {
        return SINGLETON;
    }

    /**
     * Private constructor.
     */
    private ClientMgmtService() {
    }
}
