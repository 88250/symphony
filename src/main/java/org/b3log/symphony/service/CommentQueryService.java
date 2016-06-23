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
package org.b3log.symphony.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.CommentRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Symphonys;
import org.b3log.symphony.util.Times;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Comment management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.6.6.18, Jun 23, 2016
 * @since 0.2.0
 */
@Service
public class CommentQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentQueryService.class.getName());

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
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Gets comment count of the specified day.
     *
     * @param day the specified day
     * @return comment count
     */
    public int getCommentCntInDay(final Date day) {
        final long time = day.getTime();
        final long start = Times.getDayStartTime(time);
        final long end = Times.getDayEndTime(time);

        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, start),
                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, end),
                new PropertyFilter(Comment.COMMENT_STATUS, FilterOperator.EQUAL, Comment.COMMENT_STATUS_C_VALID)
        ));

        try {
            return (int) commentRepository.count(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Count day comment failed", e);

            return 1;
        }
    }

    /**
     * Gets comment count of the specified month.
     *
     * @param day the specified month
     * @return comment count
     */
    public int getCommentCntInMonth(final Date day) {
        final long time = day.getTime();
        final long start = Times.getMonthStartTime(time);
        final long end = Times.getMonthEndTime(time);

        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, start),
                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, end),
                new PropertyFilter(Comment.COMMENT_STATUS, FilterOperator.EQUAL, Comment.COMMENT_STATUS_C_VALID)
        ));

        try {
            return (int) commentRepository.count(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Count month comment failed", e);

            return 1;
        }
    }

    /**
     * Gets a comment with {@link #organizeComment(org.json.JSONObject)} by the specified comment id.
     *
     * @param commentId the specified comment id
     * @return comment, returns {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getCommentById(final String commentId) throws ServiceException {

        try {
            final JSONObject ret = commentRepository.get(commentId);
            if (null == ret) {
                return null;
            }

            organizeComment(ret);

            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, e.getMessage(), e);

            throw new ServiceException("Gets comment[id=" + commentId + "] failed");
        }
    }

    /**
     * Gets a comment by the specified id.
     *
     * @param commentId the specified id
     * @return comment, return {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getComment(final String commentId) throws ServiceException {
        try {
            final JSONObject ret = commentRepository.get(commentId);

            if (null == ret) {
                return null;
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets a comment [commentId=" + commentId + "] failed", e);
            throw new ServiceException(e);
        }
    }

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
                comment.put(Comment.COMMENT_T_ARTICLE_TITLE, Emotions.clear(article.optString(Article.ARTICLE_TITLE)));
                comment.put(Comment.COMMENT_T_ARTICLE_PERMALINK, article.optString(Article.ARTICLE_PERMALINK));

                final String commenterId = comment.optString(Comment.COMMENT_AUTHOR_ID);
                final JSONObject commenter = userRepository.get(commenterId);

                if (UserExt.USER_STATUS_C_INVALID == commenter.optInt(UserExt.USER_STATUS)
                        || Comment.COMMENT_STATUS_C_INVALID == comment.optInt(Comment.COMMENT_STATUS)) {
                    comment.put(Comment.COMMENT_CONTENT, langPropsService.get("commentContentBlockLabel"));
                }

                if (Article.ARTICLE_TYPE_C_DISCUSSION == article.optInt(Article.ARTICLE_TYPE)) {
                    comment.put(Comment.COMMENT_CONTENT, "....");
                }

                String content = comment.optString(Comment.COMMENT_CONTENT);
                content = Emotions.clear(content);
                content = Jsoup.clean(content, Whitelist.none());
                if (StringUtils.isBlank(content)) {
                    comment.put(Comment.COMMENT_CONTENT, "....");
                } else {
                    comment.put(Comment.COMMENT_CONTENT, content);
                }

                final String commenterEmail = comment.optString(Comment.COMMENT_AUTHOR_EMAIL);
                String avatarURL = Symphonys.get("defaultThumbnailURL");
                if (!UserExt.DEFAULT_CMTER_EMAIL.equals(commenterEmail)) {
                    avatarURL = avatarQueryService.getAvatarURLByUser(commenter);
                }
                commenter.put(UserExt.USER_AVATAR_URL, avatarURL);

                comment.put(Comment.COMMENT_T_COMMENTER, commenter);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets user comments failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the user comments with the specified user id, page number and page size.
     *
     * @param userId the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @param viewer the specified viewer, may be {@code null}
     * @return user comments, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getUserComments(final String userId, final int currentPageNum, final int pageSize,
            final JSONObject viewer) throws ServiceException {
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

                comment.put(Comment.COMMENT_T_ARTICLE_TITLE,
                        Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)
                        ? langPropsService.get("articleTitleBlockLabel")
                        : Emotions.convert(article.optString(Article.ARTICLE_TITLE)));
                comment.put(Comment.COMMENT_T_ARTICLE_TYPE, article.optInt(Article.ARTICLE_TYPE));
                comment.put(Comment.COMMENT_T_ARTICLE_PERMALINK, article.optString(Article.ARTICLE_PERMALINK));

                final JSONObject commenter = userRepository.get(userId);
                comment.put(Comment.COMMENT_T_COMMENTER, commenter);

                final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                final JSONObject articleAuthor = userRepository.get(articleAuthorId);
                final String articleAuthorName = articleAuthor.optString(User.USER_NAME);
                final String articleAuthorURL = "/member/" + articleAuthor.optString(User.USER_NAME);
                comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_NAME, articleAuthorName);
                comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_URL, articleAuthorURL);
                final String articleAuthorThumbnailURL = avatarQueryService.getAvatarURLByUser(articleAuthor);
                comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_THUMBNAIL_URL, articleAuthorThumbnailURL);

                if (Article.ARTICLE_TYPE_C_DISCUSSION == article.optInt(Article.ARTICLE_TYPE)) {
                    final String msgContent = langPropsService.get("articleDiscussionLabel").
                            replace("{user}", "<a href='" + Latkes.getServePath()
                                    + "/member/" + articleAuthorName + "'>" + articleAuthorName + "</a>");

                    if (null == viewer) {
                        comment.put(Comment.COMMENT_CONTENT, msgContent);
                    } else {
                        final String commenterName = commenter.optString(User.USER_NAME);
                        final String viewerUserName = viewer.optString(User.USER_NAME);
                        final String viewerRole = viewer.optString(User.USER_ROLE);

                        if (!commenterName.equals(viewerUserName) && !Role.ADMIN_ROLE.equals(viewerRole)) {
                            final String articleContent = article.optString(Article.ARTICLE_CONTENT);
                            final Set<String> userNames = userQueryService.getUserNames(articleContent);

                            boolean invited = false;
                            for (final String userName : userNames) {
                                if (userName.equals(viewerUserName)) {
                                    invited = true;

                                    break;
                                }
                            }

                            if (!invited) {
                                comment.put(Comment.COMMENT_CONTENT, msgContent);
                            }
                        }
                    }
                }

                processCommentContent(comment);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets user comments failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the article comments with the specified article id, page number and page size.
     *
     * @param articleId the specified article id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @param sortMode the specified sort mode (traditional: 0, real time: 1)
     * @return comments, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getArticleComments(final String articleId, final int currentPageNum, final int pageSize,
            final int sortMode) throws ServiceException {
        Stopwatchs.start("Get comments");

        final Query query = new Query()
                .setPageCount(1).setCurrentPageNum(currentPageNum).setPageSize(pageSize)
                .setFilter(new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId));

        if (UserExt.USER_COMMENT_VIEW_MODE_C_REALTIME == sortMode) {
            query.addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
        } else {
            query.addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);
        }

        try {
            final JSONObject result = commentRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            organizeComments(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets article [" + articleId + "] comments failed", e);
            throw new ServiceException(e);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets comments by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "paginationCurrentPageNum": 1,
     *     "paginationPageSize": 20,
     *     "paginationWindowSize": 10,
     * }, see {@link Pagination} for more details
     * </pre>
     *
     * @param commentFields the specified article fields to return
     *
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "comments": [{
     *         "oId": "",
     *         "commentContent": "",
     *         "commentCreateTime": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     * @see Pagination
     */
    public JSONObject getComments(final JSONObject requestJSONObject, final Map<String, Class<?>> commentFields)
            throws ServiceException {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize)
                .addSort(Comment.COMMENT_CREATE_TIME, SortDirection.DESCENDING);
        for (final Map.Entry<String, Class<?>> commentField : commentFields.entrySet()) {
            query.addProjection(commentField.getKey(), commentField.getValue());
        }

        JSONObject result = null;

        try {
            result = commentRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets comments failed", e);

            throw new ServiceException(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> comments = CollectionUtils.<JSONObject>jsonArrayToList(data);

        try {
            for (final JSONObject comment : comments) {
                organizeComment(comment);

                final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                final JSONObject article = articleRepository.get(articleId);

                comment.put(Comment.COMMENT_T_ARTICLE_TITLE,
                        Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)
                        ? langPropsService.get("articleTitleBlockLabel")
                        : Emotions.convert(article.optString(Article.ARTICLE_TITLE)));
                comment.put(Comment.COMMENT_T_ARTICLE_PERMALINK, article.optString(Article.ARTICLE_PERMALINK));
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Organizes comments failed", e);

            throw new ServiceException(e);
        }

        ret.put(Comment.COMMENTS, comments);

        return ret;
    }

    /**
     * Organizes the specified comments.
     *
     * <ul>
     * <li>converts comment create time (long) to date type</li>
     * <li>generates comment author thumbnail URL</li>
     * <li>generates comment author URL</li>
     * <li>generates comment author name</li>
     * <li>generates &#64;username home URL</li>
     * <li>markdowns comment content</li>
     * <li>block comment if need</li>
     * <li>generates emotion images</li>
     * <li>generates time ago text</li>
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
     * <li>converts comment create time (long) to date type</li>
     * <li>generates comment author thumbnail URL</li>
     * <li>generates comment author URL</li>
     * <li>generates comment author name</li>
     * <li>generates &#64;username home URL</li>
     * <li>markdowns comment content</li>
     * <li>block comment if need</li>
     * <li>generates emotion images</li>
     * <li>generates time ago text</li>
     * </ul>
     *
     * @param comment the specified comment
     * @throws RepositoryException repository exception
     */
    private void organizeComment(final JSONObject comment) throws RepositoryException {
        comment.put(Common.TIME_AGO, Times.getTimeAgo(comment.optLong(Comment.COMMENT_CREATE_TIME), Latkes.getLocale()));
        comment.put(Comment.COMMENT_CREATE_TIME, new Date(comment.optLong(Comment.COMMENT_CREATE_TIME)));

        final String authorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
        final JSONObject author = userRepository.get(authorId);

        final String thumbnailURL = avatarQueryService.getAvatarURLByUser(author);
        comment.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, thumbnailURL);

        comment.put(Comment.COMMENT_T_COMMENTER, author);
        comment.put(Comment.COMMENT_T_AUTHOR_NAME, author.optString(User.USER_NAME));
        comment.put(Comment.COMMENT_T_AUTHOR_URL, author.optString(User.USER_URL));

        processCommentContent(comment);
    }

    /**
     * Processes the specified comment content.
     *
     * <ul>
     * <li>Generates &#64;username home URL</li>
     * <li>Markdowns</li>
     * <li>Blocks comment if need</li>
     * <li>Generates emotion images</li>
     * <li>Generates article link with article id</li>
     * </ul>
     *
     * @param comment the specified comment, for example,      <pre>
     * {
     *     "commentContent": "",
     *     ....,
     *     "commenter": {}
     * }
     * </pre>
     */
    private void processCommentContent(final JSONObject comment) {
        final JSONObject commenter = comment.optJSONObject(Comment.COMMENT_T_COMMENTER);

        final boolean sync = StringUtils.isNotBlank(comment.optString(Comment.COMMENT_CLIENT_COMMENT_ID));
        comment.put(Common.FROM_CLIENT, sync);

        if (Comment.COMMENT_STATUS_C_INVALID == comment.optInt(Comment.COMMENT_STATUS)
                || UserExt.USER_STATUS_C_INVALID == commenter.optInt(UserExt.USER_STATUS)) {
            comment.put(Comment.COMMENT_CONTENT, langPropsService.get("commentContentBlockLabel"));

            return;
        }

        genCommentContentUserName(comment);

        String commentContent = comment.optString(Comment.COMMENT_CONTENT);

        commentContent = shortLinkQueryService.linkArticle(commentContent);
        commentContent = shortLinkQueryService.linkTag(commentContent);
        commentContent = Emotions.convert(commentContent);
        commentContent = Markdowns.toHTML(commentContent);
        commentContent = Markdowns.clean(commentContent, "");

        final String commentId = comment.optString(Keys.OBJECT_ID);
        // MP3 player render
        final StringBuffer contentBuilder = new StringBuffer();
        final String MP3_URL_REGEX = "<p><a href.*\\.mp3.*</a>( )*</p>";
        final Pattern p = Pattern.compile(MP3_URL_REGEX);
        final Matcher m = p.matcher(commentContent);

        int i = 0;
        while (m.find()) {
            String mp3URL = m.group();
            String mp3Name = StringUtils.substringBetween(mp3URL, "\">", ".mp3</a>");
            mp3URL = StringUtils.substringBetween(mp3URL, "href=\"", "\" rel=");
            final String playerId = "player" + commentId + i++;

            m.appendReplacement(contentBuilder, "<div id=\"" + playerId + "\" class=\"aplayer\"></div>\n"
                    + "<script>\n"
                    + "var " + playerId + " = new APlayer({\n"
                    + "    element: document.getElementById('" + playerId + "'),\n"
                    + "    narrow: false,\n"
                    + "    autoplay: false,\n"
                    + "    showlrc: false,\n"
                    + "    mutex: true,\n"
                    + "    theme: '#e6d0b2',\n"
                    + "    music: {\n"
                    + "        title: '" + mp3Name + "',\n"
                    + "        author: '" + mp3URL + "',\n"
                    + "        url: '" + mp3URL + "',\n"
                    + "        pic: '" + Latkes.getStaticServePath() + "/js/lib/aplayer/default.jpg'\n"
                    + "    }\n"
                    + "});\n"
                    + playerId + ".init();\n"
                    + "</script>");
        }
        m.appendTail(contentBuilder);

        commentContent = contentBuilder.toString();
        commentContent = commentContent.replaceFirst("<div id=\"player",
                "<script src=\"" + Latkes.getStaticServePath() + "/js/lib/aplayer/APlayer.min.js\"></script>\n<div id=\"player");

        if (sync) {
            // "<i class='ft-small'>by 88250</i>"
            String syncCommenterName = StringUtils.substringAfter(commentContent, "<i class=\"ft-small\">by ");
            syncCommenterName = StringUtils.substringBefore(syncCommenterName, "</i>");

            if (UserRegisterValidation.invalidUserName(syncCommenterName)) {
                syncCommenterName = "Someone";
            }

            commentContent = commentContent.replaceAll("<i class=\"ft-small\">by .*</i>", "");

            comment.put(Comment.COMMENT_T_AUTHOR_NAME, syncCommenterName);
        }

        comment.put(Comment.COMMENT_CONTENT, commentContent);
    }

    /**
     * Generates &#64;username home URL for the specified comment content.
     *
     * @param comment the specified comment
     */
    // XXX: [Performance Issue] genCommentContentUserName
    private void genCommentContentUserName(final JSONObject comment) {
        String commentContent = comment.optString(Comment.COMMENT_CONTENT);
        try {
            final Set<String> userNames = userQueryService.getUserNames(commentContent);
            for (final String userName : userNames) {
                commentContent = commentContent.replace('@' + userName,
                        "@<a href='" + Latkes.getServePath()
                        + "/member/" + userName + "'>" + userName + "</a>");
            }

            commentContent = commentContent.replace("@participants ",
                    "@<a href='https://hacpai.com/article/1458053458339' class='ft-red'>participants</a> ");
        } catch (final ServiceException e) {
            LOGGER.log(Level.ERROR, "Generates @username home URL for comment content failed", e);
        }

        comment.put(Comment.COMMENT_CONTENT, commentContent);
    }
}
