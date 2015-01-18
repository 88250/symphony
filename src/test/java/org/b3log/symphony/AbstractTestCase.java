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
package org.b3log.symphony;

import org.b3log.latke.Latkes;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.service.UserMgmtService;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Abstract test case.
 * 
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 2, 2012
 * @see #beforeClass() 
 * @see #afterClass() 
 */
public abstract class AbstractTestCase {

    /**
     * User repository.
     */
    private UserRepository userRepository;
    /**
     * User management service.
     */
    private UserMgmtService userMgmtService;

    /**
     * Before class.
     * 
     * <ol>
     *   <li>Sets up GAE unit test runtime environment</li>
     *   <li>Initializes Latke runtime</li>
     *   <li>Instantiates repositories</li>
     * </ol>
     */
    @BeforeClass
    public void beforeClass() {
        Latkes.initRuntimeEnv();

        // Repositories
        userRepository = new UserRepository();

        // Services
        userMgmtService = new UserMgmtService();
    }

    /**
     * After class.
     * 
     * <ol>
     *   <li>Shutdowns Latke runtime</li>
     * </ol>
     */
    @AfterClass
    public void afterClass() {
        Latkes.shutdown();
    }

    /**
     * Gets user repository.
     * 
     * @return user repository
     */
    public UserRepository getUserRepository() {
        return userRepository;
    }
}
