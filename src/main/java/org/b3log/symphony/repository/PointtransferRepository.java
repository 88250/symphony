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
 * @version 1.1.0.0, Sep 23, 2015
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
                    + "	`symphony_pointtransfer`\n"
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
