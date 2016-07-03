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
package org.b3log.symphony.service;

import javax.inject.Inject;
import org.apache.commons.lang.RandomStringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Invitecode;
import org.b3log.symphony.repository.InvitecodeRepository;
import org.json.JSONObject;

/**
 * Invitecode management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jul 3, 2016
 * @since 1.4.0
 */
@Service
public class InvitecodeMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(InvitecodeMgmtService.class.getName());
    /**
     * Invitecode repository.
     */
    @Inject
    private InvitecodeRepository invitecodeRepository;

    /**
     * Generates invitecodes with the specified quantity.
     *
     * @param quantity the specified quantity
     * @throws ServiceException service exception
     */
    public void generateInvitecodes(final int quantity) throws ServiceException {
        final Transaction transaction = invitecodeRepository.beginTransaction();

        try {
            for (int i = 0; i < quantity; i++) {
                final JSONObject invitecode = new JSONObject();
                invitecode.put(Invitecode.CODE, RandomStringUtils.randomAlphanumeric(16));
                invitecode.put(Invitecode.MEMO, "");
                invitecode.put(Invitecode.STATUS, Invitecode.STATUS_C_UNUSED);
                invitecode.put(Invitecode.USER_ID, "");
                invitecode.put(Invitecode.USE_TIME, 0);
                
                invitecodeRepository.add(invitecode);
            }
            
            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Generates invitecodes failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates the specified invitecode by the given invitecode id.
     *
     * @param invitecodeId the given invitecode id
     * @param invitecode the specified invitecode
     * @throws ServiceException service exception
     */
    public void updateInvitecode(final String invitecodeId, final JSONObject invitecode) throws ServiceException {
        final Transaction transaction = invitecodeRepository.beginTransaction();

        try {
            invitecodeRepository.update(invitecodeId, invitecode);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates an invitecode[id=" + invitecodeId + "] failed", e);
            throw new ServiceException(e);
        }
    }
}
