/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
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

import org.b3log.latke.repository.AbstractRepository;
import org.b3log.symphony.model.Client;

/**
 * Client repository.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.1.0, Oct 18, 2012
 * @since 0.2.0
 */
public final class ClientRepository extends AbstractRepository {

    /**
     * Singleton.
     */
    private static final ClientRepository SINGLETON = new ClientRepository(Client.CLIENT);

    /**
     * Private constructor.
     * 
     * @param name the specified name
     */
    private ClientRepository(final String name) {
        super(name);
    }

    /**
     * Gets the {@link ClientRepository} singleton.
     *
     * @return the singleton
     */
    public static ClientRepository getInstance() {
        return SINGLETON;
    }
}
