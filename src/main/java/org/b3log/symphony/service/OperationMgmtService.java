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
package org.b3log.symphony.service;

import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.repository.OperationRepository;
import org.json.JSONObject;

/**
 * Operation management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 19, 2018
 * @since 3.4.4
 */
@Service
public class OperationMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(OperationMgmtService.class);

    /**
     * Operation repository.
     */
    @Inject
    private OperationRepository operationRepository;

    /**
     * Adds the specified operation.
     *
     * @param operation the specified operation
     */
    @Transactional
    public void addOperation(final JSONObject operation) {
        try {
            operationRepository.add(operation);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Adds an operation failed", e);
        }
    }
}
