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
package org.b3log.symphony.service;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Invitecode;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.repository.InvitecodeRepository;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.util.List;

/**
 * Invitecode management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.3, Aug 30, 2016
 * @since 1.4.0
 */
@Service
public class InvitecodeMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(InvitecodeMgmtService.class);

    /**
     * Invitecode repository.
     */
    @Inject
    private InvitecodeRepository invitecodeRepository;

    /**
     * Expires invitecodes.
     */
    @Transactional
    public void expireInvitecodes() {
        final long now = System.currentTimeMillis();
        final long expired = now - Symphonys.INVITECODE_EXPIRED;

        final Query query = new Query().setPage(1, Integer.MAX_VALUE).
                setFilter(CompositeFilterOperator.and(
                        new PropertyFilter(Invitecode.STATUS, FilterOperator.EQUAL, Invitecode.STATUS_C_UNUSED),
                        new PropertyFilter(Invitecode.GENERATOR_ID, FilterOperator.NOT_EQUAL, Pointtransfer.ID_C_SYS),
                        new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN_OR_EQUAL, expired)
                ));

        JSONObject result;
        try {
            result = invitecodeRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets invitecodes failed", e);
            return;
        }

        final List<JSONObject> data = (List<JSONObject>) result.opt(Keys.RESULTS);
        try {
            for (final JSONObject invitecode : data) {
                final String invitecodeId = invitecode.optString(Keys.OBJECT_ID);
                invitecodeRepository.remove(invitecodeId);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Expires invitecodes failed", e);
        }
    }

    /**
     * User generates an invitecode.
     *
     * @param userId   the specified user id
     * @param userName the specified user name
     * @return invitecode
     */
    public String userGenInvitecode(final String userId, final String userName) {
        final Transaction transaction = invitecodeRepository.beginTransaction();

        try {
            final String ret = RandomStringUtils.randomAlphanumeric(16);
            final JSONObject invitecode = new JSONObject();
            invitecode.put(Invitecode.CODE, ret);
            invitecode.put(Invitecode.MEMO, "User [" + userName + "," + userId + "] generated");
            invitecode.put(Invitecode.STATUS, Invitecode.STATUS_C_UNUSED);
            invitecode.put(Invitecode.GENERATOR_ID, userId);
            invitecode.put(Invitecode.USER_ID, "");
            invitecode.put(Invitecode.USE_TIME, 0);

            invitecodeRepository.add(invitecode);

            transaction.commit();

            return ret;
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Generates invitecode failed", e);

            return null;
        }
    }

    /**
     * Admin generates invitecodes with the specified quantity and memo.
     *
     * @param quantity the specified quantity
     * @param memo     the specified memo
     */
    public void adminGenInvitecodes(final int quantity, final String memo) {
        final Transaction transaction = invitecodeRepository.beginTransaction();

        try {
            for (int i = 0; i < quantity; i++) {
                final JSONObject invitecode = new JSONObject();
                invitecode.put(Invitecode.CODE, RandomStringUtils.randomAlphanumeric(16));
                invitecode.put(Invitecode.MEMO, memo);
                invitecode.put(Invitecode.STATUS, Invitecode.STATUS_C_UNUSED);
                invitecode.put(Invitecode.GENERATOR_ID, Pointtransfer.ID_C_SYS);
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
        }
    }

    /**
     * Updates the specified invitecode by the given invitecode id.
     *
     * @param invitecodeId the given invitecode id
     * @param invitecode   the specified invitecode
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
