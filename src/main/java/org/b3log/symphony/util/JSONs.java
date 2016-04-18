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
package org.b3log.symphony.util;

import org.b3log.latke.util.CollectionUtils;
import org.json.JSONObject;

/**
 * JSON utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Apr 18, 2016
 * @since 1.4.0
 */
public final class JSONs {

    /**
     * Private constructor.
     */
    private JSONs() {
    }

    /**
     * Clones a JSON object from the specified source object.
     *
     * @param src the specified source object
     * @return cloned object
     */
    public static JSONObject clone(final JSONObject src) {
        return new JSONObject(src, CollectionUtils.jsonArrayToArray(src.names(), String[].class));
    }
}
