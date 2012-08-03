/*
 * Copyright (c) 2012, B3log Team
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
package org.b3log.symphony.util;

import org.b3log.latke.Keys;
import org.b3log.latke.model.Pagination;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Query result utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 2, 2012
 * @since 0.2.0
 */
public final class QueryResults {

    /**
     * Constructs a default query result.
     * 
     * @return a default query result, 
     * <pre>
     * {
     *     "sc": false
     * }
     * </pre>
     */
    public static JSONObject defaultResult() {
        return new JSONObject().put(Keys.STATUS_CODE, false);
    }

    /**
     * Constructs a default query results.
     * 
     * @return a default query results,
     * <pre>
     * {
     *     "pagination": {
     *       "paginationPageCount": 0
     *     },
     *     "rslts": []
     * }
     * </pre>
     */
    public static JSONObject defaultResults() {
        final JSONObject ret = new JSONObject();
        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, 0);

        final JSONArray results = new JSONArray();
        ret.put(Keys.RESULTS, results);

        return ret;
    }

    /**
     * Private constructor.
     */
    private QueryResults() {
    }
}
