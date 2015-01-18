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
package org.b3log.symphony.service;

import java.util.Collection;
import java.util.Set;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.Lifecycle;
import org.b3log.latke.ioc.config.Discoverer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * {@link UserQueryService} test case.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Oct 11, 2013
 * @since 0.2.5
 */
public class UserQueryServiceTestCase {

    @BeforeClass
    public void beforeClass() throws Exception {
        Latkes.initRuntimeEnv();
        final Collection<Class<?>> classes = Discoverer.discover("org.b3log");
        Lifecycle.startApplication(classes);
    }

    @Test
    public void getUserNames() throws Exception {
        final LatkeBeanManager beanManager = Lifecycle.getBeanManager();
        final UserQueryService userQueryService = beanManager.getReference(UserQueryService.class);

        Set<String> userNames = userQueryService.getUserNames("test");
        Assert.assertTrue(userNames.isEmpty());

        userNames = userQueryService.getUserNames("test @88250");
        Assert.assertEquals(userNames.iterator().next(), "88250");

        userNames = userQueryService.getUserNames("test @88250 aaa");
        Assert.assertEquals(userNames.iterator().next(), "88250");

        userNames = userQueryService.getUserNames("test @88250 aaa @Vanessa");
        Assert.assertEquals(userNames.size(), 2);

        userNames = userQueryService.getUserNames("test @88250\naaa @Vanessa");
        Assert.assertEquals(userNames.size(), 2);

        userNames = userQueryService.getUserNames("test @88250\naaa @Vanessa\n");
        Assert.assertEquals(userNames.size(), 2);

        userNames = userQueryService.getUserNames("test @88250@Vanessa");
        Assert.assertEquals(userNames.size(), 1);
        
        userNames = userQueryService.getUserNames("test @88250@");
        Assert.assertEquals(userNames.size(), 0);
    }
}
