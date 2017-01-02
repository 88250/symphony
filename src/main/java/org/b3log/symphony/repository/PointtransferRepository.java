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

import java.util.List;

import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Pointtransfer;
import org.json.JSONObject;

/**
 * Pointtransfer repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.1.0, Dec 12, 2016
 * @since 1.3.0
 */
@Repository
public class PointtransferRepository extends AbstractRepository {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PointtransferRepository.class.getName());

    /**
     * Gets average point of activity eating snake of a user specified by the given user id.
     *
     * @param userId the given user id
     * @return average point, if the point small than {@code 1}, returns {@code pointActivityEatingSnake} which
     * configured in sym.properties
     */
    public int getActivityEatingSnakeAvg(final String userId) {
        int ret = Pointtransfer.TRANSFER_SUM_C_ACTIVITY_EATINGSNAKE;

        try {
            final List<JSONObject> result = select("SELECT\n"
                    + "	AVG(sum) AS point\n"
                    + "FROM\n"
                    + "	`" + getName() + "`\n"
                    + "WHERE\n"
                    + "	type = 27\n"
                    + "AND toId = ?\n"
                    + "", userId);
            if (!result.isEmpty()) {
                ret = result.get(0).optInt(Common.POINT, ret);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Calc avg point failed", e);
        }

        if (ret < 1) {
            ret = Pointtransfer.TRANSFER_SUM_C_ACTIVITY_EATINGSNAKE;
        }

        return ret;
    }

    /**
     * Public constructor.
     */
    public PointtransferRepository() {
        super(Pointtransfer.POINTTRANSFER);
    }
}
