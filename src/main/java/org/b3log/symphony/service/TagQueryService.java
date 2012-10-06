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
package org.b3log.symphony.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.repository.TagRepository;
import org.json.JSONObject;

/**
 * Tag query service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Oct 3, 2012
 * @since 0.2.0
 */
public final class TagQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TagQueryService.class.getName());
    /**
     * Singleton.
     */
    private static final TagQueryService SINGLETON = new TagQueryService();
    /**
     * Tag repository.
     */
    private TagRepository tagRepository = TagRepository.getInstance();

    /**
     * Gets the trend (sort by reference count) tags.
     * 
     * @param fetchSize the specified fetch size
     * @return trend tags, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getTrendTags(final int fetchSize) throws ServiceException {
        final Query query = new Query().addSort(Tag.TAG_REFERENCE_CNT, SortDirection.DESCENDING).
                setCurrentPageNum(1).setPageSize(fetchSize).setPageCount(1);

        try {
            final JSONObject result = tagRepository.get(query);
            return CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets trend tags failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the tags the specified fetch size.
     * 
     * @param fetchSize the specified fetch size
     * @return tags, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getTags(final int fetchSize) throws ServiceException {
        final Query query = new Query().setPageCount(1).setPageSize(fetchSize);

        try {
            final JSONObject result = tagRepository.get(query);
            return CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets tags failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the {@link TagQueryService} singleton.
     *
     * @return the singleton
     */
    public static TagQueryService getInstance() {
        return SINGLETON;
    }

    /**
     * Private constructor.
     */
    private TagQueryService() {
    }
}
