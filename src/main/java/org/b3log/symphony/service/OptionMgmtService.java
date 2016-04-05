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
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.repository.OptionRepository;
import org.json.JSONObject;

/**
 * Option management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Apr 5, 2016
 * @since 1.1.0
 */
@Service
public class OptionMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(OptionMgmtService.class.getName());

    /**
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    /**
     * Removes an option.
     *
     * @param id the specified option id
     */
    public void removeOption(final String id) {
        final Transaction transaction = optionRepository.beginTransaction();

        try {
            optionRepository.remove(id);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Removes an option failed", e);
        }
    }

    /**
     * Adds the specified option.
     *
     * @param option the specified option
     */
    public void addOption(final JSONObject option) {
        final Transaction transaction = optionRepository.beginTransaction();

        try {
            optionRepository.add(option);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Adds an option failed", e);
        }
    }

    /**
     * Updates the specified option by the given option id.
     *
     * @param optionId the given option id
     * @param option the specified option
     * @throws ServiceException service exception
     */
    public void updateOption(final String optionId, final JSONObject option) throws ServiceException {
        final Transaction transaction = optionRepository.beginTransaction();

        try {
            optionRepository.update(optionId, option);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates an option[id=" + optionId + "] failed", e);
            throw new ServiceException(e);
        }
    }
}
