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
package org.b3log.symphony.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.MD5;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.CommentRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Markdowns;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Comment management service.
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.14, Jan 7, 2013
 * @since 0.2.0
 */
public final class CommentQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentQueryService.class.getName());

    /**
     * Singleton.
     */
    private static final CommentQueryService SINGLETON = new CommentQueryService();

    /**
     * Comment repository.
     */
    private CommentRepository commentRepository = CommentRepository.getInstance();

    /**
     * Article repository.
     */
    private ArticleRepository articleRepository = ArticleRepository.getInstance();

    /**
     * User repository.
     */
    private UserRepository userRepository = UserRepository.getInstance();

    /**
     * User query service.
     */
    private UserQueryService userQueryService = UserQueryService.getInstance();

    /**
     * Language service.
     */
    private LangPropsService langPropsService = LangPropsService.getInstance();

    /**
     * Gets the latest comments with the specified fetch size.
     * 
     * <p>
     * The returned comments content is plain text.
     * </p>
     * 
     * @param fetchSize the specified fetch size
     * @return the latest comments, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getLatestComments(final int fetchSize) throws ServiceException {
        final Query query = new Query().addSort(Comment.COMMENT_CREATE_TIME, SortDirection.DESCENDING)
                .setCurrentPageNum(1).setPageSize(fetchSize).setPageCount(1);
        try {
            final JSONObject result = commentRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final JSONObject comment : ret) {
                comment.put(Comment.COMMENT_CREATE_TIME, comment.optLong(Comment.COMMENT_CREATE_TIME));
                final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                final JSONObject article = articleRepository.get(articleId);
                comment.put(Comment.COMMENT_T_ARTICLE_TITLE, article.optString(Article.ARTICLE_TITLE));
                comment.put(Comment.COMMENT_T_ARTICLE_PERMALINK, article.optString(Article.ARTICLE_PERMALINK));

                final String commenterId = comment.optString(Comment.COMMENT_AUTHOR_ID);
                final JSONObject commenter = userRepository.get(commenterId);

                if (UserExt.USER_STATUS_C_INVALID == commenter.optInt(UserExt.USER_STATUS)
                        || Comment.COMMENT_STATUS_C_INVALID == comment.optInt(Comment.COMMENT_STATUS)) {
                    comment.put(Comment.COMMENT_CONTENT, langPropsService.get("commentContentBlockLabel"));
                }

                String content = Emotions.clear(comment.optString(Comment.COMMENT_CONTENT));
                content = Jsoup.clean(content, Whitelist.none());
                comment.put(Comment.COMMENT_CONTENT, content);

                final String commenterEmail = comment.optString(Comment.COMMENT_AUTHOR_EMAIL);
                String thumbnailURL = Latkes.getStaticServePath() + "/images/user-thumbnail.png";
                if (!UserExt.DEFAULT_CMTER_EMAIL.equals(commenterEmail)) {
                    final String hashedEmail = MD5.hash(commenterEmail);
                    thumbnailURL = "http://secure.gravatar.com/avatar/" + hashedEmail + "?s=140&d="
                            + Latkes.getStaticServePath() + "/images/user-thumbnail.png";
                }
                commenter.put(UserExt.USER_T_THUMBNAIL_URL, thumbnailURL);

                comment.put(Comment.COMMENT_T_COMMENTER, commenter);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets user comments failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the user comments with the specified user id, page number and page size.
     * 
     * @param userId the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return user comments, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getUserComments(final String userId, final int currentPageNum, final int pageSize) throws ServiceException {
        final Query query = new Query().addSort(Comment.COMMENT_CREATE_TIME, SortDirection.DESCENDING)
                .setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                setFilter(new PropertyFilter(Comment.COMMENT_AUTHOR_ID, FilterOperator.EQUAL, userId));
        try {
            final JSONObject result = commentRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final JSONObject comment : ret) {
                comment.put(Comment.COMMENT_CREATE_TIME, new Date(comment.optLong(Comment.COMMENT_CREATE_TIME)));

                final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                final JSONObject article = articleRepository.get(articleId);
                
                
                comment.put(Comment.COMMENT_T_ARTICLE_TITLE, Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)
                        ? langPropsService.get("articleTitleBlockLabel") : article.optString(Article.ARTICLE_TITLE));
                comment.put(Comment.COMMENT_T_ARTICLE_PERMALINK, article.optString(Article.ARTICLE_PERMALINK));

                final JSONObject commenter = userRepository.get(userId);
                comment.put(Comment.COMMENT_T_COMMENTER, commenter);
                
                final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                final JSONObject articleAuthor = userRepository.get(articleAuthorId);
                final String articleAuthorName = articleAuthor.optString(User.USER_NAME);
                final String articleAuthorURL = "/member/" + articleAuthor.optString(User.USER_NAME);
                comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_NAME, articleAuthorName);
                comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_URL, articleAuthorURL);

                processCommentContent(comment);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets user comments failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the article participants (commenters) with the specified article article id and fetch size.
     * 
     * @param articleId the specified article id
     * @param fetchSize the specified fetch size
     * @return article participants, for example,
     * <pre>
     * [
     *     {
     *         "articleParticipantName": "",
     *         "articleParticipantThumbnailURL": "",
     *         "commentId": ""
     *     }, ....
     * ]
     * </pre>, returns an empty list if not found
     * @throws ServiceException service exception 
     */
    public List<JSONObject> getArticleLatestParticipants(final String articleId, final int fetchSize) throws ServiceException {
        final Query query = new Query().addSort(Comment.COMMENT_CREATE_TIME, SortDirection.DESCENDING)
                .setFilter(new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId))
                .addProjection(Comment.COMMENT_AUTHOR_EMAIL, String.class).addProjection(Keys.OBJECT_ID, String.class)
                .setPageCount(1).setCurrentPageNum(1).setPageSize(fetchSize);
        final List<JSONObject> ret = new ArrayList<JSONObject>();

        try {
            final JSONObject result = commentRepository.get(query);
            final List<JSONObject> comments = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final JSONObject comment : comments) {
                final String email = comment.optString(Comment.COMMENT_AUTHOR_EMAIL);
                final JSONObject commenter = userRepository.getByEmail(email);

                String thumbnailURL = Latkes.getStaticServePath() + "/images/user-thumbnail.png";
                if (!UserExt.DEFAULT_CMTER_EMAIL.equals(email)) {
                    final String hashedEmail = MD5.hash(email);
                    thumbnailURL = "http://secure.gravatar.com/avatar/" + hashedEmail + "?s=140&d="
                            + Latkes.getStaticServePath() + "/images/user-thumbnail.png";
                }

                final JSONObject participant = new JSONObject();
                participant.put(Article.ARTICLE_T_PARTICIPANT_NAME, commenter.optString(User.USER_NAME));
                participant.put(Article.ARTICLE_T_PARTICIPANT_THUMBNAIL_URL, thumbnailURL);
                participant.put(Article.ARTICLE_T_PARTICIPANT_URL, commenter.optString(User.USER_URL));
                participant.put(Comment.COMMENT_T_ID, comment.optString(Keys.OBJECT_ID));

                ret.add(participant);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets article [" + articleId + "] participants failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the article comments with the specified article id, page number and page size.
     * 
     * @param articleId the specified article id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return comments, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getArticleComments(final String articleId, final int currentPageNum, final int pageSize)
            throws ServiceException {
        final Query query = new Query().addSort(Comment.COMMENT_CREATE_TIME, SortDirection.ASCENDING)
                .setPageCount(1).setCurrentPageNum(currentPageNum).setPageSize(pageSize)
                .setFilter(new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId));
        try {
            final JSONObject result = commentRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            organizeComments(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets article [" + articleId + "] comments failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Organizes the specified comments.
     * 
     * <ul>
     *   <li>converts comment create time (long) to date type</li>
     *   <li>generates comment author thumbnail URL</li>
     *   <li>generates comment author URL</li>
     *   <li>generates comment author name</li>
     *   <li>generates &#64;username home URL</li>
     *   <li>markdowns comment content</li>
     *   <li>blockl comment if need</li>
     *   <li>generates emotion images</li>
     * </ul>
     * 
     * @param comments the specified comments
     * @throws RepositoryException repository exception 
     */
    private void organizeComments(final List<JSONObject> comments) throws RepositoryException {
        for (final JSONObject comment : comments) {
            organizeComment(comment);
        }
    }

    /**
     * Organizes the specified comment.
     * 
     * <ul>
     *   <li>converts comment create time (long) to date type</li>
     *   <li>generates comment author thumbnail URL</li>
     *   <li>generates comment author URL</li>
     *   <li>generates comment author name</li>
     *   <li>generates &#64;username home URL</li>
     *   <li>markdowns comment content</li>
     *   <li>blockl comment if need</li>
     *   <li>generates emotion images</li>
     * </ul>
     * 
     * @param comment the specified comment
     * @throws RepositoryException repository exception
     */
    private void organizeComment(final JSONObject comment) throws RepositoryException {
        comment.put(Comment.COMMENT_CREATE_TIME, new Date(comment.optLong(Comment.COMMENT_CREATE_TIME)));

        final String email = comment.optString(Comment.COMMENT_AUTHOR_EMAIL);
        String thumbnailURL = Latkes.getStaticServePath() + "/images/user-thumbnail.png";
        if (!UserExt.DEFAULT_CMTER_EMAIL.equals(email)) {
            final String hashedEmail = MD5.hash(email);
            thumbnailURL = "http://secure.gravatar.com/avatar/" + hashedEmail + "?s=140&d="
                    + Latkes.getStaticServePath() + "/images/user-thumbnail.png";
        }

        comment.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, thumbnailURL);

        final String authorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
        final JSONObject author = userRepository.get(authorId);
        comment.put(Comment.COMMENT_T_COMMENTER, author);
        comment.put(Comment.COMMENT_T_AUTHOR_NAME, author.optString(User.USER_NAME));
        comment.put(Comment.COMMENT_T_AUTHOR_URL, author.optString(User.USER_URL));

        processCommentContent(comment);
    }

    /**
     * Processes the specified comment content.
     * 
     * <ul>
     *   <li>Generates &#64;username home URL</li>
     *   <li>Markdowns</li>
     *   <li>Blocks comment if need</li>
     *   <li>Generates emotion images</li>
     * </ul>
     * 
     * @param comment the specified comment, for example, 
     * <pre>
     * {
     *     "commentContent": "",
     *     ...., 
     *     "commenter": {}
     * }
     * </pre>
     */
    private void processCommentContent(final JSONObject comment) {
        final JSONObject commenter = comment.optJSONObject(Comment.COMMENT_T_COMMENTER);

        if (Comment.COMMENT_STATUS_C_INVALID == comment.optInt(Comment.COMMENT_STATUS)
                || UserExt.USER_STATUS_C_INVALID == commenter.optInt(UserExt.USER_STATUS)) {
            comment.put(Comment.COMMENT_CONTENT, langPropsService.get("commentContentBlockLabel"));

            return;
        }

        genCommentContentUserName(comment);

        String commentContent = comment.optString(Comment.COMMENT_CONTENT);

        commentContent = Emotions.convert(commentContent);

        commentContent = Markdowns.toHTML(commentContent);

        comment.put(Comment.COMMENT_CONTENT, commentContent);
    }

    /**
     * Generates &#64;username home URL for the specified comment content.
     * 
     * @param comment the specified comment
     */
    private void genCommentContentUserName(final JSONObject comment) {
        String commentContent = comment.optString(Comment.COMMENT_CONTENT);
        try {
            final Set<String> userNames = userQueryService.getUserNames(commentContent);
            for (final String userName : userNames) {
                commentContent = commentContent.replace('@' + userName,
                        "@<a href='/member/" + userName + "'>" + userName + "</a>");
            }
        } catch (final ServiceException e) {
            LOGGER.log(Level.SEVERE, "Generates @username home URL for comment content failed", e);
        }

        comment.put(Comment.COMMENT_CONTENT, commentContent);
    }

    /**
     * Gets the {@link CommentQueryService} singleton.
     *
     * @return the singleton
     */
    public static CommentQueryService getInstance() {
        return SINGLETON;
    }

    /**
     * Private constructor.
     */
    private CommentQueryService() {
    }
}
