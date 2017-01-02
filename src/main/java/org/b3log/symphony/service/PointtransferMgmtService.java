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
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.PointtransferRepository;
import org.b3log.symphony.repository.UserRepository;
import org.json.JSONObject;

/**
 * Pointtransfer management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.1.3, Aug 9, 2016
 * @since 1.3.0
 */
@Service
public class PointtransferMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PointtransferMgmtService.class.getName());

    /**
     * Pointtransfer repository.
     */
    @Inject
    private PointtransferRepository pointtransferRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Transfers point from the specified from id to the specified to id with type, sum, data id and time.
     *
     * @param fromId the specified from id, may be system "sys"
     * @param toId the specified to id, may be system "sys"
     * @param type the specified type
     * @param sum the specified sum
     * @param dataId the specified data id
     * @param time the specified time
     * @return transfer record id, returns {@code null} if transfer failed
     */
    public synchronized String transfer(final String fromId, final String toId, final int type, final int sum,
            final String dataId, final long time) {
        if (StringUtils.equals(fromId, toId)) { // for example the commenter is the article author
            return null;
        }

        final Transaction transaction = pointtransferRepository.beginTransaction();
        try {
            int fromBalance = 0;
            if (!Pointtransfer.ID_C_SYS.equals(fromId)) {
                final JSONObject fromUser = userRepository.get(fromId);
                if (UserExt.USER_STATUS_C_VALID != fromUser.optInt(UserExt.USER_STATUS)) {
                    throw new Exception("Invalid from user [id=" + fromId + "]");
                }

                fromBalance = fromUser.optInt(UserExt.USER_POINT) - sum;
                if (fromBalance < 0) {
                    throw new Exception("Insufficient balance");
                }

                fromUser.put(UserExt.USER_POINT, fromBalance);
                fromUser.put(UserExt.USER_USED_POINT, fromUser.optInt(UserExt.USER_USED_POINT) + sum);
                userRepository.update(fromId, fromUser);
            }

            int toBalance = 0;
            if (!Pointtransfer.ID_C_SYS.equals(toId)) {
                final JSONObject toUser = userRepository.get(toId);
                if (UserExt.USER_STATUS_C_VALID != toUser.optInt(UserExt.USER_STATUS)) {
                    throw new Exception("Invalid to user [id=" + toId + "]");
                }

                toBalance = toUser.optInt(UserExt.USER_POINT) + sum;
                toUser.put(UserExt.USER_POINT, toBalance);

                userRepository.update(toId, toUser);
            }

            final JSONObject pointtransfer = new JSONObject();
            pointtransfer.put(Pointtransfer.FROM_ID, fromId);
            pointtransfer.put(Pointtransfer.TO_ID, toId);
            pointtransfer.put(Pointtransfer.SUM, sum);
            pointtransfer.put(Pointtransfer.FROM_BALANCE, fromBalance);
            pointtransfer.put(Pointtransfer.TO_BALANCE, toBalance);
            pointtransfer.put(Pointtransfer.TIME, time);
            pointtransfer.put(Pointtransfer.TYPE, type);
            pointtransfer.put(Pointtransfer.DATA_ID, dataId);

            final String ret = pointtransferRepository.add(pointtransfer);

            transaction.commit();

            return ret;
        } catch (final Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Transfer [fromId=" + fromId + ", toId=" + toId + ", sum=" + sum + ", type=" + type
                    + ", dataId=" + dataId + "] error", e);

            return null;
        }
    }

    /**
     * Adds a pointtransfer with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "fromId"; "",
     *     "toId": "",
     *     "sum": int,
     *     "blance": int,
     *     "time": long,
     *     "type": int,
     *     "dataId": ""
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addPointtransfer(final JSONObject requestJSONObject) throws ServiceException {
        try {
            pointtransferRepository.add(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds pointtransfer failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }
}
