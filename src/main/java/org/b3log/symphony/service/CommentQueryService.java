/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Locales;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Times;
import org.b3log.symphony.model.*;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.CommentRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.*;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * Comment management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.12.2.8, Jun 23, 2020
 * @since 0.2.0
 */
@Service
public class CommentQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(CommentQueryService.class);

    /**
     * Revision query service.
     */
    @Inject
    private RevisionQueryService revisionQueryService;

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
     * Gets the URL of a comment.
     *
     * @param commentId the specified comment id
     * @param sortMode  the specified sort mode
     * @param pageSize  the specified comment page size
     * @return comment URL, return {@code null} if not found
     */
    public String getCommentURL(final String commentId, final int sortMode, final int pageSize) {
        try {
            final JSONObject comment = commentRepository.get(commentId);
            if (null == comment) {
                return null;
            }

            final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
            final JSONObject article = articleRepository.get(articleId);
            if (null == article) {
                return null;
            }
            String title = Escapes.escapeHTML(article.optString(Article.ARTICLE_TITLE));
            title = Emotions.convert(title);
            final int commentPage = getCommentPage(articleId, commentId, sortMode, pageSize);

            return "<a href=\"" + Latkes.getServePath() + "/article/" + articleId + "?p=" + commentPage
                    + "&m=" + sortMode + "#" + commentId + "\" target=\"_blank\">" + title + "</a>";
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets comment URL failed", e);

            return null;
        }
    }

    /**
     * Gets the offered (accepted) comment of an article specified by the given article id.
     *
     * @param commentViewMode the specified comment view mode
     * @param articleId       the given article id
     * @return accepted comment, return {@code null} if not found
     */
    public JSONObject getOfferedComment(final int commentViewMode, final String articleId) {
        Stopwatchs.start("Gets accepted comment");
        try {
            final Query query = new Query().addSort(Comment.COMMENT_SCORE, SortDirection.DESCENDING).setPage(1, 1).setPageCount(1)
                    .setFilter(CompositeFilterOperator.and(
                            new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId),
                            new PropertyFilter(Comment.COMMENT_QNA_OFFERED, FilterOperator.EQUAL, Comment.COMMENT_QNA_OFFERED_C_YES),
                            new PropertyFilter(Comment.COMMENT_STATUS, FilterOperator.EQUAL, Comment.COMMENT_STATUS_C_VALID)));
            try {
                final List<JSONObject> comments = commentRepository.getList(query);
                if (comments.isEmpty()) {
                    return null;
                }

                final JSONObject ret = comments.get(0);
                organizeComment(ret);

                final int pageSize = Symphonys.ARTICLE_COMMENTS_CNT;
                ret.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, getCommentPage(
                        articleId, ret.optString(Keys.OBJECT_ID), commentViewMode, pageSize));
                return ret;
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Gets accepted comment failed", e);
                return null;
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the page number of a comment.
     *
     * @param articleId the specified article id
     * @param commentId the specified comment id
     * @param sortMode  the specified sort mode
     * @param pageSize  the specified comment page size
     * @return page number, return {@code 1} if occurs exception
     */
    public int getCommentPage(final String articleId, final String commentId, final int sortMode, final int pageSize) {
        final Query numQuery = new Query().setPage(1, Integer.MAX_VALUE).setPageCount(1);

        switch (sortMode) {
            case UserExt.USER_COMMENT_VIEW_MODE_C_TRADITIONAL:
                numQuery.setFilter(CompositeFilterOperator.and(
                        new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId),
                        new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, commentId)
                )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);
                break;
            case UserExt.USER_COMMENT_VIEW_MODE_C_REALTIME:
                numQuery.setFilter(CompositeFilterOperator.and(
                        new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId),
                        new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN, commentId)
                )).addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
                break;
        }

        Stopwatchs.start("Get comment page");
        try {
            final long num = commentRepository.count(numQuery);
            return (int) ((num / pageSize) + 1);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets comment page failed", e);

            return 1;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets original comment of a comment specified by the given comment id.
     *
     * @param currentUserId   the specified current user id, may be null
     * @param commentViewMode the specified comment view mode
     * @param commentId       the given comment id
     * @return original comment, return {@code null} if not found
     */
    public JSONObject getOriginalComment(final String currentUserId, final int commentViewMode, final String commentId) {
        try {
            final JSONObject comment = commentRepository.get(commentId);

            organizeComment(comment);

            final int pageSize = Symphonys.ARTICLE_COMMENTS_CNT;

            final JSONObject ret = new JSONObject();

            final JSONObject commentAuthor = comment.optJSONObject(Comment.COMMENT_T_COMMENTER);
            if (UserExt.USER_XXX_STATUS_C_DISABLED == commentAuthor.optInt(UserExt.USER_UA_STATUS)) {
                ret.put(Comment.COMMENT_UA, "");
            }

            ret.put(Comment.COMMENT_T_AUTHOR_NAME, comment.optString(Comment.COMMENT_T_AUTHOR_NAME));
            ret.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, comment.optString(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL));
            ret.put(Common.TIME_AGO, comment.optString(Common.TIME_AGO));
            ret.put(Comment.COMMENT_CREATE_TIME_STR, comment.optString(Comment.COMMENT_CREATE_TIME_STR));
            ret.put(Common.REWARED_COUNT, comment.optInt(Comment.COMMENT_THANK_CNT));
            ret.put(Common.REWARDED, comment.optBoolean(Common.REWARDED));
            ret.put(Keys.OBJECT_ID, commentId);
            ret.put(Comment.COMMENT_CONTENT, comment.optString(Comment.COMMENT_CONTENT));
            ret.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, getCommentPage(
                    comment.optString(Comment.COMMENT_ON_ARTICLE_ID), commentId,
                    commentViewMode, pageSize));

            // https://github.com/b3log/symphony/issues/682
            if (Comment.COMMENT_VISIBLE_C_AUTHOR == comment.optInt(Comment.COMMENT_VISIBLE)) {
                final String commentAuthorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
                final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                final JSONObject article = articleRepository.get(articleId);
                final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                if (StringUtils.isBlank(currentUserId) ||
                        (!StringUtils.equals(currentUserId, commentAuthorId) && !StringUtils.equals(currentUserId, articleAuthorId))) {
                    ret.put(Comment.COMMENT_CONTENT, langPropsService.get("onlySelfAndArticleAuthorVisibleLabel"));
                }
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Get replies failed", e);

            return null;
        }
    }

    /**
     * Gets replies of a comment specified by the given comment id.
     *
     * @param currentUserId   the specified current user id, may be null
     * @param commentViewMode the specified comment view mode
     * @param commentId       the given comment id
     * @return a list of replies, return an empty list if not found
     */
    public List<JSONObject> getReplies(final String currentUserId, final int commentViewMode, final String commentId) {
        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                setPage(1, Integer.MAX_VALUE).setPageCount(1).
                setFilter(CompositeFilterOperator.and(
                        new PropertyFilter(Comment.COMMENT_ORIGINAL_COMMENT_ID, FilterOperator.EQUAL, commentId),
                        new PropertyFilter(Comment.COMMENT_STATUS, FilterOperator.EQUAL, Comment.COMMENT_STATUS_C_VALID)));
        try {
            final List<JSONObject> comments = commentRepository.getList(query);
            organizeComments(comments);
            final int pageSize = Symphonys.ARTICLE_COMMENTS_CNT;
            final List<JSONObject> ret = new ArrayList<>();
            for (final JSONObject comment : comments) {
                final JSONObject reply = new JSONObject();
                ret.add(reply);

                final JSONObject commentAuthor = comment.optJSONObject(Comment.COMMENT_T_COMMENTER);
                if (UserExt.USER_XXX_STATUS_C_DISABLED == commentAuthor.optInt(UserExt.USER_UA_STATUS)) {
                    reply.put(Comment.COMMENT_UA, "");
                }

                reply.put(Comment.COMMENT_T_AUTHOR_NAME, comment.optString(Comment.COMMENT_T_AUTHOR_NAME));
                reply.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, comment.optString(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL));
                reply.put(Common.TIME_AGO, comment.optString(Common.TIME_AGO));
                reply.put(Comment.COMMENT_CREATE_TIME_STR, comment.optString(Comment.COMMENT_CREATE_TIME_STR));
                reply.put(Common.REWARED_COUNT, comment.optInt(Comment.COMMENT_THANK_CNT));
                reply.put(Common.REWARDED, comment.optBoolean(Common.REWARDED));
                reply.put(Keys.OBJECT_ID, comment.optString(Keys.OBJECT_ID));
                reply.put(Comment.COMMENT_CONTENT, comment.optString(Comment.COMMENT_CONTENT));
                reply.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, getCommentPage(
                        comment.optString(Comment.COMMENT_ON_ARTICLE_ID), reply.optString(Keys.OBJECT_ID),
                        commentViewMode, pageSize));
                reply.put(Comment.COMMENT_VISIBLE, comment.optInt(Comment.COMMENT_VISIBLE));

                // https://github.com/b3log/symphony/issues/682
                if (Comment.COMMENT_VISIBLE_C_AUTHOR == comment.optInt(Comment.COMMENT_VISIBLE)) {
                    final String commentAuthorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
                    final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                    final JSONObject article = articleRepository.get(articleId);
                    final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                    if (StringUtils.isBlank(currentUserId) ||
                            (!StringUtils.equals(currentUserId, commentAuthorId) && !StringUtils.equals(currentUserId, articleAuthorId))) {
                        reply.put(Comment.COMMENT_CONTENT, langPropsService.get("onlySelfAndArticleAuthorVisibleLabel"));
                    }
                }
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Get replies failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets nice comments of an article specified by the given article id.
     *
     * @param commentViewMode the specified comment view mode
     * @param articleId       the given article id
     * @param fetchSize       the specified fetch size
     * @return a list of nice comments, return an empty list if not found
     */
    public List<JSONObject> getNiceComments(final int commentViewMode,
                                            final String articleId, final int fetchSize) {
        Stopwatchs.start("Gets nice comments");
        try {
            final Query query = new Query().addSort(Comment.COMMENT_SCORE, SortDirection.DESCENDING).
                    setPage(1, fetchSize).setPageCount(1).
                    setFilter(CompositeFilterOperator.and(
                            new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId),
                            new PropertyFilter(Comment.COMMENT_SCORE, FilterOperator.GREATER_THAN, 0D),
                            new PropertyFilter(Comment.COMMENT_STATUS, FilterOperator.EQUAL, Comment.COMMENT_STATUS_C_VALID)));
            try {
                final List<JSONObject> ret = commentRepository.getList(query);
                organizeComments(ret);
                final int pageSize = Symphonys.ARTICLE_COMMENTS_CNT;
                for (final JSONObject comment : ret) {
                    comment.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, getCommentPage(
                            articleId, comment.optString(Keys.OBJECT_ID),
                            commentViewMode, pageSize));
                }

                return ret;
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Get nice comments failed", e);
                return Collections.emptyList();
            }
        } finally {
            Stopwatchs.end();
        }
    }

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
     * Gets a comment with {@link #organizeComment(JSONObject)} by the specified comment id.
     *
     * @param commentId the specified comment id
     * @return comment, returns {@code null} if not found
     */
    public JSONObject getCommentById(final String commentId) {

        try {
            final JSONObject ret = commentRepository.get(commentId);
            if (null == ret) {
                return null;
            }

            organizeComment(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, e.getMessage(), e);

            return null;
        }
    }

    /**
     * Gets a comment by the specified id.
     *
     * @param commentId the specified id
     * @return comment, return {@code null} if not found
     */
    public JSONObject getComment(final String commentId) {
        try {
            final JSONObject ret = commentRepository.get(commentId);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets a comment [commentId=" + commentId + "] failed", e);

            return null;
        }
    }

    /**
     * Gets the user comments with the specified user id, page number and page size.
     *
     * @param userId         the specified user id
     * @param anonymous      the specified comment anonymous
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @param viewer         the specified viewer, may be {@code null}
     * @return user comments, return an empty list if not found
     */
    public List<JSONObject> getUserComments(final String userId, final int anonymous,
                                            final int currentPageNum, final int pageSize, final JSONObject viewer) {
        final Query query = new Query().addSort(Comment.COMMENT_CREATE_TIME, SortDirection.DESCENDING).
                setPage(currentPageNum, pageSize).
                setFilter(CompositeFilterOperator.and(
                        new PropertyFilter(Comment.COMMENT_AUTHOR_ID, FilterOperator.EQUAL, userId),
                        new PropertyFilter(Comment.COMMENT_ANONYMOUS, FilterOperator.EQUAL, anonymous),
                        new PropertyFilter(Comment.COMMENT_STATUS, FilterOperator.EQUAL, Comment.COMMENT_STATUS_C_VALID)));
        try {
            final JSONObject result = commentRepository.get(query);
            final List<JSONObject> ret = (List<JSONObject>) result.opt(Keys.RESULTS);
            if (ret.isEmpty()) {
                return ret;
            }

            String currentUserId = null;
            if (null != viewer) {
                currentUserId = viewer.optString(Keys.OBJECT_ID);
            }

            final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
            final int recordCount = pagination.optInt(Pagination.PAGINATION_RECORD_COUNT);
            final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);

            final JSONObject first = ret.get(0);
            first.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);
            first.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);

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
                comment.put(Comment.COMMENT_T_ARTICLE_PERFECT, article.optInt(Article.ARTICLE_PERFECT));

                final JSONObject commenter = userRepository.get(userId);
                comment.put(Comment.COMMENT_T_COMMENTER, commenter);

                final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                JSONObject articleAuthor;
                if (Article.ARTICLE_ANONYMOUS_C_PUBLIC == article.optInt(Article.ARTICLE_ANONYMOUS)) {
                    articleAuthor = userRepository.get(articleAuthorId);
                } else {
                    articleAuthor = userRepository.getAnonymousUser();
                }
                final String articleAuthorName = articleAuthor.optString(User.USER_NAME);
                comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_NAME, articleAuthorName);
                comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_URL, "/member/" + articleAuthor.optString(User.USER_NAME));
                final String articleAuthorThumbnailURL = avatarQueryService.getAvatarURLByUser(articleAuthor, "48");
                comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_THUMBNAIL_URL, articleAuthorThumbnailURL);

                final String commentId = comment.optString(Keys.OBJECT_ID);
                final int cmtViewMode = UserExt.USER_COMMENT_VIEW_MODE_C_TRADITIONAL;
                final int cmtPage = getCommentPage(articleId, commentId, cmtViewMode, Symphonys.ARTICLE_COMMENTS_CNT);
                comment.put(Comment.COMMENT_SHARP_URL, "/article/" + articleId + "?p=" + cmtPage + "&m=" + cmtViewMode + "#" + commentId);

                if (Article.ARTICLE_TYPE_C_DISCUSSION == article.optInt(Article.ARTICLE_TYPE)
                        && Article.ARTICLE_ANONYMOUS_C_PUBLIC == article.optInt(Article.ARTICLE_ANONYMOUS)) {
                    final String msgContent = langPropsService.get("articleDiscussionLabel").
                            replace("{user}", UserExt.getUserLink(articleAuthorName));

                    if (null == viewer) {
                        comment.put(Comment.COMMENT_CONTENT, msgContent);
                    } else {
                        final String commenterName = commenter.optString(User.USER_NAME);
                        final String viewerUserName = viewer.optString(User.USER_NAME);
                        final String viewerRole = viewer.optString(User.USER_ROLE);

                        if (!commenterName.equals(viewerUserName) && !Role.ROLE_ID_C_ADMIN.equals(viewerRole)) {
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

                // https://github.com/b3log/symphony/issues/682
                if (Comment.COMMENT_VISIBLE_C_AUTHOR == comment.optInt(Comment.COMMENT_VISIBLE)) {
                    if (StringUtils.isBlank(currentUserId) || (!StringUtils.equals(currentUserId, userId) && !StringUtils.equals(currentUserId, articleAuthorId))) {
                        comment.put(Comment.COMMENT_CONTENT, langPropsService.get("onlySelfAndArticleAuthorVisibleLabel"));
                    }
                }
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets user comments failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets the article comments with the specified article id, page number and page size.
     *
     * @param articleId      the specified article id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @param sortMode       the specified sort mode (traditional: 0, real time: 1)
     * @return comments, return an empty list if not found
     */
    public List<JSONObject> getArticleComments(final String articleId, final int currentPageNum, final int pageSize, final int sortMode) {
        Stopwatchs.start("Get comments");

        final Query query = new Query().setPageCount(1).setPage(currentPageNum, pageSize).
                setFilter(new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId));

        if (UserExt.USER_COMMENT_VIEW_MODE_C_REALTIME == sortMode) {
            query.addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
        } else {
            query.addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);
        }

        try {
            Stopwatchs.start("Query comments");
            JSONObject result;
            try {
                result = commentRepository.get(query);
            } finally {
                Stopwatchs.end();
            }
            final List<JSONObject> ret = (List<JSONObject>) result.opt(Keys.RESULTS);
            organizeComments(ret);

            Stopwatchs.start("Revision, paging, original");
            try {
                for (final JSONObject comment : ret) {
                    final String commentId = comment.optString(Keys.OBJECT_ID);

                    // Fill revision count
                    comment.put(Comment.COMMENT_REVISION_COUNT, 0);
                    if (Comment.COMMENT_STATUS_C_VALID == comment.optInt(Comment.COMMENT_STATUS)) {
                        comment.put(Comment.COMMENT_REVISION_COUNT,
                                revisionQueryService.count(commentId, Revision.DATA_TYPE_C_COMMENT));
                    }

                    final String originalCmtId = comment.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID);
                    if (StringUtils.isBlank(originalCmtId)) {
                        continue;
                    }

                    // Fill page number
                    comment.put(Pagination.PAGINATION_CURRENT_PAGE_NUM,
                            getCommentPage(articleId, originalCmtId, sortMode, pageSize));

                    // Fill original comment
                    final JSONObject originalCmt = commentRepository.get(originalCmtId);
                    if (null != originalCmt) {
                        organizeComment(originalCmt);
                        comment.put(Comment.COMMENT_T_ORIGINAL_AUTHOR_THUMBNAIL_URL,
                                originalCmt.optString(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL));
                    } else {
                        comment.put(Comment.COMMENT_ORIGINAL_COMMENT_ID, "");
                    }
                }
            } finally {
                Stopwatchs.end();
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets article [" + articleId + "] comments failed", e);

            return Collections.emptyList();
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets comments by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10,
     *                          , see {@link Pagination} for more details
     * @param commentFields     the specified article fields to return
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
     * @see Pagination
     */

    public JSONObject getComments(final JSONObject requestJSONObject, final List<String> commentFields) {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setPage(currentPageNum, pageSize).
                addSort(Comment.COMMENT_CREATE_TIME, SortDirection.DESCENDING);
        for (final String commentField : commentFields) {
            query.select(commentField);
        }

        JSONObject result;
        try {
            result = commentRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets comments failed", e);
            return null;
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final List<JSONObject> comments = (List<JSONObject>) result.opt(Keys.RESULTS);
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
            return null;
        }

        ret.put(Comment.COMMENTS, (Object) comments);
        return ret;
    }

    /**
     * Organizes the specified comments.
     *
     * @param comments the specified comments
     * @see #organizeComment(JSONObject)
     */
    private void organizeComments(final List<JSONObject> comments) {
        if (comments.isEmpty()) {
            return;
        }

        Stopwatchs.start("Organizes comments");
        try {
            final ForkJoinPool pool = new ForkJoinPool(Symphonys.PROCESSORS);
            pool.submit(() -> comments.parallelStream().forEach(comment -> {
                try {
                    organizeComment(comment);
                } catch (final Exception e) {
                    LOGGER.log(Level.ERROR, "Organizes comment [" + comment.optString(Keys.OBJECT_ID) + "] failed", e);
                } finally {
                    // LOGGER.log(Level.INFO, "Stopwatch: {}{}", Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat());
                    Stopwatchs.release();
                }
            }));
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Organizes comments failed", e);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Organizes the specified comment.
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
     * <li>anonymous process</li>
     * </ul>
     *
     * @param comment the specified comment
     * @throws RepositoryException repository exception
     */
    private void organizeComment(final JSONObject comment) throws RepositoryException {
        Stopwatchs.start("Organize comment");

        try {
            comment.put(Common.TIME_AGO, Times.getTimeAgo(comment.optLong(Comment.COMMENT_CREATE_TIME), Locales.getLocale()));
            final Date createDate = new Date(comment.optLong(Comment.COMMENT_CREATE_TIME));
            comment.put(Comment.COMMENT_CREATE_TIME, createDate);
            comment.put(Comment.COMMENT_CREATE_TIME_STR, DateFormatUtils.format(createDate, "yyyy-MM-dd HH:mm:ss"));

            JSONObject author;
            if (Comment.COMMENT_ANONYMOUS_C_PUBLIC == comment.optInt(Comment.COMMENT_ANONYMOUS)) {
                final String authorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
                author = userRepository.get(authorId);
            } else {
                author = userRepository.getAnonymousUser();
            }
            comment.put(Comment.COMMENT_T_COMMENTER, author);
            comment.put(Comment.COMMENT_T_AUTHOR_NAME, author.optString(User.USER_NAME));
            comment.put(Comment.COMMENT_T_AUTHOR_URL, author.optString(User.USER_URL));
            final String thumbnailURL = avatarQueryService.getAvatarURLByUser(author, "48");
            comment.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, thumbnailURL);

            processCommentContent(comment);
        } finally {
            Stopwatchs.end();
        }
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
     * @param comment the specified comment, for example,
     *                "commentContent": "",
     *                ....,
     *                "commenter": {}
     */
    private void processCommentContent(final JSONObject comment) {
        final JSONObject commenter = comment.optJSONObject(Comment.COMMENT_T_COMMENTER);

        if (Comment.COMMENT_STATUS_C_INVALID == comment.optInt(Comment.COMMENT_STATUS)
                || UserExt.USER_STATUS_C_INVALID == commenter.optInt(UserExt.USER_STATUS)) {
            comment.put(Comment.COMMENT_CONTENT, langPropsService.get("commentContentBlockLabel"));
            return;
        }

        String commentContent = comment.optString(Comment.COMMENT_CONTENT);

        commentContent = shortLinkQueryService.linkArticle(commentContent);
        commentContent = Emotions.convert(commentContent);
        commentContent = Markdowns.toHTML(commentContent);
        commentContent = Markdowns.clean(commentContent, "");
        commentContent = MediaPlayers.renderAudio(commentContent);
        commentContent = MediaPlayers.renderVideo(commentContent);
        comment.put(Comment.COMMENT_CONTENT, commentContent);
    }
}
