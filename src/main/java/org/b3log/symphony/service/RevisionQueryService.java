/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.service;

import org.b3log.latke.Keys;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Revision;
import org.b3log.symphony.repository.RevisionRepository;
import org.b3log.symphony.util.Markdowns;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Revision query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, May 6, 2017
 * @since 2.1.0
 */
@Service
public class RevisionQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(RevisionQueryService.class);

    /**
     * Revision repository.
     */
    @Inject
    private RevisionRepository revisionRepository;

    /**
     * Gets a comment's revisions.
     *
     * @param commentId the specified comment id
     * @return comment revisions, returns an empty list if not found
     */
    public List<JSONObject> getCommentRevisions(final String commentId) {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Revision.REVISION_DATA_ID, FilterOperator.EQUAL, commentId),
                new PropertyFilter(Revision.REVISION_DATA_TYPE, FilterOperator.EQUAL, Revision.DATA_TYPE_C_COMMENT)
        )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);

        try {
            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(revisionRepository.get(query).optJSONArray(Keys.RESULTS));
            for (final JSONObject rev : ret) {
                final JSONObject data = new JSONObject(rev.optString(Revision.REVISION_DATA));
                String commentContent = data.optString(Comment.COMMENT_CONTENT);
                commentContent = commentContent.replace("\n", "_esc_br_");
                commentContent = Markdowns.clean(commentContent, "");
                commentContent = commentContent.replace("_esc_br_", "\n");
                data.put(Comment.COMMENT_CONTENT, commentContent);

                rev.put(Revision.REVISION_DATA, data);
            }

            return ret;
        } catch (final RepositoryException | JSONException e) {
            LOGGER.log(Level.ERROR, "Gets comment revisions failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets an article's revisions.
     *
     * @param articleId the specified article id
     * @return article revisions, returns an empty list if not found
     */
    public List<JSONObject> getArticleRevisions(final String articleId) {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Revision.REVISION_DATA_ID, FilterOperator.EQUAL, articleId),
                new PropertyFilter(Revision.REVISION_DATA_TYPE, FilterOperator.EQUAL, Revision.DATA_TYPE_C_ARTICLE)
        )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);

        try {
            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(revisionRepository.get(query).optJSONArray(Keys.RESULTS));
            for (final JSONObject rev : ret) {
                final JSONObject data = new JSONObject(rev.optString(Revision.REVISION_DATA));
                String articleTitle = data.optString(Article.ARTICLE_TITLE);
                articleTitle = articleTitle.replace("<", "&lt;").replace(">", "&gt;");
                articleTitle = Markdowns.clean(articleTitle, "");
                data.put(Article.ARTICLE_TITLE, articleTitle);

                String articleContent = data.optString(Article.ARTICLE_CONTENT);
                // articleContent = Markdowns.toHTML(articleContent); https://hacpai.com/article/1490233597586
                articleContent = articleContent.replace("\n", "_esc_br_");
                articleContent = Markdowns.clean(articleContent, "");
                articleContent = articleContent.replace("_esc_br_", "\n");
                data.put(Article.ARTICLE_CONTENT, articleContent);

                rev.put(Revision.REVISION_DATA, data);
            }

            return ret;
        } catch (final RepositoryException | JSONException e) {
            LOGGER.log(Level.ERROR, "Gets article revisions failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Counts revision specified by the given data id and data type.
     *
     * @param dataId   the given data id
     * @param dataType the given data type
     * @return count result
     */
    public int count(final String dataId, final int dataType) {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Revision.REVISION_DATA_ID, FilterOperator.EQUAL, dataId),
                new PropertyFilter(Revision.REVISION_DATA_TYPE, FilterOperator.EQUAL, dataType)
        ));

        try {
            return (int) revisionRepository.count(query);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Counts revisions failed", e);

            return 0;
        }
    }
}
