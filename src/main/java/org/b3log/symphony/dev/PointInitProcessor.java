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
package org.b3log.symphony.dev;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeMode;
import org.b3log.latke.repository.CompositeFilter;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.Filter;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.repository.PointtransferRepository;
import org.b3log.symphony.repository.UserRepository;
import org.json.JSONObject;

/**
 * Generates init pointtransfer record for existing users.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jun 30, 2015
 * @since 1.3.0
 */
@RequestProcessor
public class PointInitProcessor {

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Pointtransfer repository.
     */
    @Inject
    private PointtransferRepository pointtransferRepository;

    /**
     * Generates init pointtransfer record for existing users.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/dev/pointtransfer/gen", method = HTTPRequestMethod.GET)
    public void genInitPointtransferRecords(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        if (RuntimeMode.PRODUCTION == Latkes.getRuntimeMode()) {
            return;
        }

        final JSONObject result = userRepository.get(new Query());
        final List<JSONObject> users = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

        final Transaction transaction = pointtransferRepository.beginTransaction();
        try {
            for (final JSONObject user : users) {
                final String userId = user.optString(Keys.OBJECT_ID);

                final List<Filter> filters = new ArrayList<Filter>();
                filters.add(new PropertyFilter(Pointtransfer.TO_ID, FilterOperator.EQUAL, userId));
                filters.add(new PropertyFilter(Pointtransfer.TYPE, FilterOperator.EQUAL, Pointtransfer.TRANSFER_TYPE_C_INIT));

                final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
                if (pointtransferRepository.count(query) > 0) {
                    continue;
                }

                final JSONObject pointtransfer = new JSONObject();
                pointtransfer.put(Keys.OBJECT_ID, userId);
                pointtransfer.put(Pointtransfer.FROM_ID, Pointtransfer.ID_C_SYS);
                pointtransfer.put(Pointtransfer.TO_ID, userId);
                pointtransfer.put(Pointtransfer.SUM, Pointtransfer.TRANSFER_SUM_C_INIT);
                pointtransfer.put(Pointtransfer.FROM_BALANCE, Pointtransfer.TRANSFER_SUM_C_INIT);
                pointtransfer.put(Pointtransfer.TO_BALANCE, Pointtransfer.TRANSFER_SUM_C_INIT);
                pointtransfer.put(Pointtransfer.TIME, Long.valueOf(userId));
                pointtransfer.put(Pointtransfer.TYPE, Pointtransfer.TRANSFER_TYPE_C_INIT);
                pointtransfer.put(Pointtransfer.DATA_ID, userId);

                pointtransferRepository.add(pointtransfer);
            }

            transaction.commit();
        } catch (final Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }

        response.sendRedirect("/");
    }
}
