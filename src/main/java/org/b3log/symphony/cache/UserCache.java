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
import javax.inject.Singleton;
import org.b3log.latke.Keys;
import org.b3log.latke.model.User;
import org.b3log.symphony.util.JSONs;
import org.json.JSONObject;

/**
 * User cache.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.2, May 15, 2016
 * @since 1.4.0
 */
@Named
@Singleton
public class UserCache {

    /**
     * Id, User.
     */
    private static final Map<String, JSONObject> ID_CACHE = new HashMap<String, JSONObject>();

    /**
     * Name, User.
     */
    private static final Map<String, JSONObject> NAME_CACHE = new HashMap<String, JSONObject>();

    /**
     * Gets a user by the specified user id.
     *
     * @param userId the specified user id
     * @return user, returns {@code null} if not found
     */
    public JSONObject getUser(final String userId) {
        final JSONObject user = ID_CACHE.get(userId);
        if (null == user) {
            return null;
        }

        return JSONs.clone(user);
    }

    /**
     * Gets a user by the specified user name.
     *
     * @param userName the specified user name
     * @return user, returns {@code null} if not found
     */
    public JSONObject getUserByName(final String userName) {
        final JSONObject user = NAME_CACHE.get(userName);
        if (null == user) {
            return null;
        }

        return JSONs.clone(user);
    }

    /**
     * Adds or updates the specified user.
     *
     * @param user the specified user
     */
    public void putUser(final JSONObject user) {
        ID_CACHE.put(user.optString(Keys.OBJECT_ID), JSONs.clone(user));
        NAME_CACHE.put(user.optString(User.USER_NAME), JSONs.clone(user));
    }
}
