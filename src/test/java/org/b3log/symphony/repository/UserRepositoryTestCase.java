/*
 * Copyright (c) 2012-2015, b3log.org
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
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.Transaction;
import org.b3log.symphony.AbstractTestCase;
import org.b3log.symphony.model.UserExt;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * {@link UserRepository} test case.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 2, 2012
 * @since 0.2.0
 */
@Test(suiteName = "repository")
public final class UserRepositoryTestCase extends AbstractTestCase {

    /**
     * Tests.
     * 
     * @throws Exception exception
     */
    @Test
    public void add() throws Exception {
        final UserRepository userRepository = getUserRepository();

        final JSONObject another = new JSONObject();
        another.put(User.USER_NAME, "test1");
        another.put(User.USER_EMAIL, "test1@gmail.com");
        another.put(User.USER_PASSWORD, "pass1");
        another.put(User.USER_ROLE, Role.DEFAULT_ROLE);
        another.put(UserExt.USER_ARTICLE_COUNT, 0);
        another.put(UserExt.USER_COMMENT_COUNT, 0);
        another.put(UserExt.USER_STATUS, "0");

        Transaction transaction = userRepository.beginTransaction();
        userRepository.add(another);
        transaction.commit();

        Assert.assertNull(userRepository.getAdmin());

        JSONObject admin = new JSONObject();
        admin.put(User.USER_NAME, "test");
        admin.put(User.USER_EMAIL, "test@gmail.com");
        admin.put(User.USER_PASSWORD, "pass");
        admin.put(User.USER_ROLE, Role.ADMIN_ROLE);
        admin.put(UserExt.USER_ARTICLE_COUNT, 1);
        admin.put(UserExt.USER_COMMENT_COUNT, 0);
        admin.put(UserExt.USER_STATUS, "0");

        transaction = userRepository.beginTransaction();
        userRepository.add(admin);
        transaction.commit();

        Assert.assertTrue(userRepository.isAdminEmail("test@gmail.com"));
        Assert.assertFalse(userRepository.isAdminEmail("notFound@gmail.com"));

        admin = userRepository.getAdmin();

        Assert.assertNotNull(admin);
        Assert.assertEquals(admin.optString(User.USER_NAME), "test");
        Assert.assertEquals(admin.optInt(UserExt.USER_ARTICLE_COUNT), 1);

        final JSONObject result = userRepository.get(new Query().setFilter(
                new PropertyFilter(User.USER_NAME, FilterOperator.EQUAL, "test1")));

        final JSONArray users = result.getJSONArray(Keys.RESULTS);
        Assert.assertEquals(users.length(), 1);
        Assert.assertEquals(users.getJSONObject(0).getString(User.USER_EMAIL), "test1@gmail.com");

        final JSONObject notFound = userRepository.getByEmail("not.found@gmail.com");
        Assert.assertNull(notFound);

        final JSONObject found = userRepository.getByEmail("test1@gmail.com");
        Assert.assertNotNull(found);
        Assert.assertEquals(found.getString(User.USER_PASSWORD), "pass1");
    }
}
