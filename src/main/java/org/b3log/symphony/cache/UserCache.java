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
package org.b3log.symphony.cache;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Named;
import org.b3log.latke.Keys;
import org.json.JSONObject;

/**
 * User cache.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Apr 13, 2016
 * @since 1.4.0
 */
@Named
public class UserCache {

    /**
     * Users.
     */
    private static final Map<String, JSONObject> USERS = new HashMap<String, JSONObject>();

    /**
     * Gets a user by the specified user id.
     *
     * @param userId the specified user id
     * @return user, returns {@code null} if not found
     */
    public JSONObject getUser(final String userId) {
        return USERS.get(userId);
    }

    /**
     * Adds or updates the specified user.
     *
     * @param user the specified user
     */
    public void putUser(final JSONObject user) {
        USERS.put(user.optString(Keys.OBJECT_ID), user);
    }
}
