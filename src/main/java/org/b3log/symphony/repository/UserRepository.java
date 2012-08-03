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
package org.b3log.symphony.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.Repository;
import org.b3log.latke.repository.RepositoryException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * User repository.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Aug 2, 2012
 * @since 0.2.0
 */
public final class UserRepository extends AbstractRepository implements Repository {

    /**
     * Singleton.
     */
    private static final UserRepository SINGLETON = new UserRepository(User.USER);

    /**
     * Private constructor.
     * 
     * @param name the specified name
     */
    private UserRepository(final String name) {
        super(name);
    }

    /**
     * Gets the {@link UserRepository} singleton.
     *
     * @return the singleton
     */
    public static UserRepository getInstance() {
        return SINGLETON;
    }

    /**
     * Gets a user by the specified email.
     *
     * @param email the specified email
     * @return user, returns {@code null} if not found
     * @throws RepositoryException repository exception 
     */
    public JSONObject getByEmail(final String email) throws RepositoryException {
        final Query query = new Query().setPageCount(1);
        query.setFilter(
                new PropertyFilter(User.USER_EMAIL, FilterOperator.EQUAL, email.toLowerCase().trim()));

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }

    /**
     * Gets the administrator user.
     *
     * @return administrator user, returns {@code null} if not found or error
     * @throws RepositoryException repository exception
     */
    public JSONObject getAdmin() throws RepositoryException {
        final Query query = new Query().setFilter(
                new PropertyFilter(User.USER_ROLE, FilterOperator.EQUAL, Role.ADMIN_ROLE)).setPageCount(1);
        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }

    /**
     * Determine whether the specified email is administrator's.
     *
     * @param email the specified email
     * @return {@code true} if it is administrator's email, {@code false}
     * otherwise
     * @throws RepositoryException repository exception
     */
    public boolean isAdminEmail(final String email) throws RepositoryException {
        final JSONObject user = getByEmail(email);

        if (null == user) {
            return false;
        }

        return Role.ADMIN_ROLE.equals(user.optString(User.USER_ROLE));
    }
}
