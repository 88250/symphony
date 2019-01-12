/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.service;

import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Revision;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.CommentRepository;
import org.b3log.symphony.repository.RevisionRepository;
import org.b3log.symphony.util.Escapes;
import org.b3log.symphony.util.Markdowns;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Revision query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.3, Jan 12, 2019
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
     * Comment repository.
     */
    @Inject
    private CommentRepository commentRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * Gets a comment's revisions.
     *
     * @param viewer    the specified viewer
     * @param commentId the specified comment id
     * @return comment revisions, returns an empty list if not found
     */
    public List<JSONObject> getCommentRevisions(final JSONObject viewer, final String commentId) {
        List<JSONObject> ret = new ArrayList<>();

        try {
            final JSONObject comment = commentRepository.get(commentId);
            if (null == comment || Comment.COMMENT_STATUS_C_VALID != comment.optInt(Comment.COMMENT_STATUS)) {
                return ret;
            }

            if (Comment.COMMENT_VISIBLE_C_AUTHOR == comment.optInt(Comment.COMMENT_VISIBLE)) {
                final JSONObject article = articleRepository.get(comment.optString(Comment.COMMENT_ON_ARTICLE_ID));
                if (null == article) {
                    return ret;
                }

                final String viewerId = viewer.optString(Keys.OBJECT_ID);
                final String commentAuthorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
                final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                if (!viewerId.equals(commentAuthorId) && !viewerId.equals(articleAuthorId)) {
                    return ret;
                }
            }

            final Query query = new Query().setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(Revision.REVISION_DATA_ID, FilterOperator.EQUAL, commentId),
                    new PropertyFilter(Revision.REVISION_DATA_TYPE, FilterOperator.EQUAL, Revision.DATA_TYPE_C_COMMENT)
            )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);

            ret = revisionRepository.getList(query);
            if (ret.isEmpty()) {
                return ret;
            }

            for (final JSONObject rev : ret) {
                final JSONObject data = new JSONObject(rev.optString(Revision.REVISION_DATA));
                String commentContent = data.optString(Comment.COMMENT_CONTENT);
                commentContent = commentContent.replace("\n", "_esc_br_");
                commentContent = Markdowns.clean(commentContent, "");
                commentContent = commentContent.replace("_esc_br_", "\n");
                data.put(Comment.COMMENT_CONTENT, commentContent);
                rev.put(Revision.REVISION_DATA, data);
            }

            final JSONObject latestRev = ret.get(ret.size() - 1);
            final JSONObject latestRevData = latestRev.optJSONObject(Revision.REVISION_DATA);
            final String latestRevContent = latestRevData.optString(Comment.COMMENT_CONTENT);
            String currentContent = comment.optString(Comment.COMMENT_CONTENT);

            final boolean contentChanged = !latestRevContent.replaceAll("\\s+", "").equals(currentContent.replaceAll("\\s+", ""));
            if (contentChanged) {
                final JSONObject appendRev = new JSONObject();
                final JSONObject appendRevData = new JSONObject();
                appendRev.put(Revision.REVISION_DATA, appendRevData);
                currentContent = currentContent.replace("\n", "_esc_br_");
                currentContent = Markdowns.clean(currentContent, "");
                currentContent = currentContent.replace("_esc_br_", "\n");
                appendRevData.put(Comment.COMMENT_CONTENT, currentContent);
                ret.add(appendRev);
            }

            return ret;
        } catch (final Exception e) {
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
            final List<JSONObject> ret = revisionRepository.getList(query);
            if (ret.isEmpty()) {
                return ret;
            }

            for (final JSONObject rev : ret) {
                final JSONObject data = new JSONObject(rev.optString(Revision.REVISION_DATA));
                final String articleTitle = Escapes.escapeHTML(data.optString(Article.ARTICLE_TITLE));
                data.put(Article.ARTICLE_TITLE, articleTitle);

                String articleContent = data.optString(Article.ARTICLE_CONTENT);
                // articleContent = Markdowns.toHTML(articleContent); https://hacpai.com/article/1490233597586
                articleContent = articleContent.replace("\n", "_esc_br_");
                articleContent = Markdowns.clean(articleContent, "");
                articleContent = articleContent.replace("_esc_br_", "\n");
                data.put(Article.ARTICLE_CONTENT, articleContent);
                rev.put(Revision.REVISION_DATA, data);
            }

            final JSONObject latestRev = ret.get(ret.size() - 1);
            final JSONObject latestRevData = latestRev.optJSONObject(Revision.REVISION_DATA);
            final String latestRevTitle = latestRevData.optString(Article.ARTICLE_TITLE);
            final String latestRevContent = latestRevData.optString(Article.ARTICLE_CONTENT);
            final JSONObject article = articleRepository.get(articleId);
            final String currentTitle = article.optString(Article.ARTICLE_TITLE);
            String currentContent = article.optString(Article.ARTICLE_CONTENT);

            final boolean titleChanged = !latestRevTitle.replaceAll("\\s+", "").equals(currentTitle.replaceAll("\\s+", ""));
            final boolean contentChanged = !latestRevContent.replaceAll("\\s+", "").equals(currentContent.replaceAll("\\s+", ""));
            if (titleChanged || contentChanged) {
                final JSONObject appendRev = new JSONObject();
                final JSONObject appendRevData = new JSONObject();
                appendRev.put(Revision.REVISION_DATA, appendRevData);
                appendRevData.put(Article.ARTICLE_TITLE, Escapes.escapeHTML(currentTitle));

                currentContent = currentContent.replace("\n", "_esc_br_");
                currentContent = Markdowns.clean(currentContent, "");
                currentContent = currentContent.replace("_esc_br_", "\n");
                appendRevData.put(Article.ARTICLE_CONTENT, currentContent);
                ret.add(appendRev);
            }

            return ret;
        } catch (final Exception e) {
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

        Stopwatchs.start("Revision count");
        try {
            return (int) revisionRepository.count(query);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Counts revisions failed", e);

            return 0;
        } finally {
            Stopwatchs.end();
        }
    }
}
