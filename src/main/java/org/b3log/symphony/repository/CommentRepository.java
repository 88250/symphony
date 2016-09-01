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
package org.b3log.symphony.repository;

import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.cache.CommentCache;
import org.b3log.symphony.model.Comment;
import org.json.JSONObject;

/**
 * Comment repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 1, 2016
 * @since 0.2.0
 */
@Repository
public class CommentRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public CommentRepository() {
        super(Comment.COMMENT);
    }

    /**
     * Comment cache.
     */
    @Inject
    private CommentCache commentCache;

    @Override
    public void remove(final String id) throws RepositoryException {
        super.remove(id);

        commentCache.removeComment(id);
    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {
        JSONObject ret = commentCache.getComment(id);
        if (null != ret) {
            return ret;
        }

        ret = super.get(id);

        if (null == ret) {
            return null;
        }

        commentCache.putComment(ret);

        return ret;
    }

    @Override
    public void update(final String id, final JSONObject comment) throws RepositoryException {
        super.update(id, comment);

        comment.put(Keys.OBJECT_ID, id);
        commentCache.putComment(comment);
    }
}
