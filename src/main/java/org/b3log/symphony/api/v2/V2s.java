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
package org.b3log.symphony.api.v2;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.symphony.model.*;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * V2 API related constants and utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.1, Mar 10, 2016
 * @since 2.0.0
 */
public final class V2s {

    /**
     * Pagination window size.
     */
    public static final int WINDOW_SIZE = 10;
    /**
     * Pagination page size.
     */
    public static final int PAGE_SIZE = 20;

    /**
     * Private constructor.
     */
    private V2s() {
    }

    /**
     * Cleans unused fields of the specified comments.
     *
     * @param comments the specified comments
     */
    public static void cleanComments(final List<JSONObject> comments) {
        for (final JSONObject comment : comments) {
            cleanComment(comment);
        }
    }

    /**
     * Cleans unused fields of the specified comment.
     */
    public static void cleanComment(final JSONObject comment) {
        comment.put(Comment.COMMENT_CREATE_TIME, ((Date) comment.opt(Comment.COMMENT_CREATE_TIME)).getTime());
        final String permalink = Latkes.getServePath() + comment.optString(Comment.COMMENT_T_ARTICLE_PERMALINK);
        comment.put(Comment.COMMENT_T_ARTICLE_PERMALINK, permalink);
        final String sharpURL = Latkes.getServePath() + comment.optString(Comment.COMMENT_SHARP_URL);
        comment.put(Comment.COMMENT_SHARP_URL, sharpURL);

        final JSONObject commenter = comment.optJSONObject(Comment.COMMENT_T_COMMENTER);
        cleanUser(commenter);

        comment.remove(Comment.COMMENT_T_ARTICLE_AUTHOR_URL);
        comment.remove(Comment.COMMENT_ANONYMOUS);
        comment.remove(Comment.COMMENT_T_ARTICLE_TYPE);
        comment.remove(Comment.COMMENT_IP);
        comment.remove(Comment.COMMENT_STATUS);
        comment.remove(Common.FROM_CLIENT);
        comment.remove(Comment.COMMENT_SCORE);
        comment.remove(Pagination.PAGINATION_PAGE_COUNT);
        comment.remove(Pagination.PAGINATION_RECORD_COUNT);
    }

    /**
     * Cleans unused fields of the specified articles.
     *
     * @param articles the specified articles
     */
    public static void cleanArticles(final List<JSONObject> articles) {
        for (final JSONObject article : articles) {
            cleanArticle(article);

            article.remove(Article.ARTICLE_CONTENT);
            article.remove(Article.ARTICLE_REWARD_POINT);
            article.remove(Article.ARTICLE_COMMENTABLE);
            article.remove(Article.ARTICLE_ANONYMOUS_VIEW);
            article.remove(Article.ARTICLE_REWARD_CONTENT);
        }
    }

    /**
     * Cleans unused fields of the specified article.
     *
     * @param article the specified article
     */
    public static void cleanArticle(final JSONObject article) {
        article.put(Article.ARTICLE_CREATE_TIME, ((Date) article.opt(Article.ARTICLE_CREATE_TIME)).getTime());
        article.put(Article.ARTICLE_UPDATE_TIME, ((Date) article.opt(Article.ARTICLE_UPDATE_TIME)).getTime());
        article.put(Article.ARTICLE_LATEST_CMT_TIME, ((Date) article.opt(Article.ARTICLE_LATEST_CMT_TIME)).getTime());
        final String permalink = Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK);
        article.put(Article.ARTICLE_PERMALINK, permalink);

        final List<JSONObject> comments = (List<JSONObject>) article.opt(Article.ARTICLE_T_COMMENTS);
        if (null != comments) {
            cleanComments(comments);
        }

        article.remove(Article.ARTICLE_T_LATEST_CMT);
        article.remove(Article.ARTICLE_LATEST_CMT_TIME);
        article.remove(Article.ARTICLE_LATEST_CMTER_NAME);
        article.remove(Article.ARTICLE_SYNC_TO_CLIENT);
        article.remove(Article.ARTICLE_ANONYMOUS);
        article.remove(Article.ARTICLE_STATUS);
        article.remove(Article.ARTICLE_T_PARTICIPANTS);
        article.remove(Article.REDDIT_SCORE);
        article.remove(Article.ARTICLE_CLIENT_ARTICLE_ID);
        article.remove(Article.ARTICLE_CITY);
        article.remove(Article.ARTICLE_IP);
        article.remove(Article.ARTICLE_EDITOR_TYPE);
        article.remove(Article.ARTICLE_RANDOM_DOUBLE);
        article.remove(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK);
        article.remove(Article.ARTICLE_T_HEAT);

        final JSONObject author = article.optJSONObject(Article.ARTICLE_T_AUTHOR);
        cleanUser(author);
    }

    /**
     * Cleans unused fields of the specified domains.
     *
     * @param domains the specified domains
     */
    public static void cleanDomains(final List<JSONObject> domains) {
        for (final JSONObject domain : domains) {
            cleanDomain(domain);
        }
    }

    /**
     * Cleans unused fields of the specified domain.
     *
     * @param domain the specified domain
     */
    public static void cleanDomain(final JSONObject domain) {
        final String uri = domain.optString(Domain.DOMAIN_URI);
        domain.put(Domain.DOMAIN_URI, Latkes.getServePath() + "/domain/" + uri);

        final List<JSONObject> tags = (List<JSONObject>) domain.opt(Domain.DOMAIN_T_TAGS);
        if (null != tags) {
            cleanTags(tags);
        }

        domain.remove(Domain.DOMAIN_TYPE);
        domain.remove(Domain.DOMAIN_STATUS);
        domain.remove(Domain.DOMAIN_SEO_DESC);
        domain.remove(Domain.DOMAIN_SEO_KEYWORDS);
        domain.remove(Domain.DOMAIN_SEO_TITLE);
        domain.remove(Domain.DOMAIN_CSS);
        domain.remove(Domain.DOMAIN_SORT);
    }

    /**
     * Cleans unused fields of the specified tags.
     *
     * @param tags the specified tags
     */
    public static void cleanTags(final List<JSONObject> tags) {
        for (final JSONObject tag : tags) {
            cleanTag(tag);
        }
    }

    /**
     * Cleans unused fields of the specified tag.
     *
     * @param tag the specified tag
     */
    public static void cleanTag(final JSONObject tag) {
        final String iconPath = tag.optString(Tag.TAG_ICON_PATH);
        if (StringUtils.isBlank(iconPath)) {
            tag.put(Tag.TAG_ICON_PATH, "");
        } else {
            tag.put(Tag.TAG_ICON_PATH, Latkes.getStaticServePath() + "/images/tags/" + iconPath);
        }

        final String uri = tag.optString(Tag.TAG_URI);
        tag.put(Tag.TAG_URI, Latkes.getServePath() + "/tag/" + uri);

        Tag.fillDescription(tag);

        final List<JSONObject> domains = (List<JSONObject>) tag.opt(Tag.TAG_T_DOMAINS);
        if (null != domains) {
            for (final JSONObject domain : domains) {
                cleanDomain(domain);
            }
        }

        tag.remove(Tag.TAG_STATUS);
        tag.remove(Tag.TAG_RANDOM_DOUBLE);
        tag.remove(Tag.TAG_CSS);
        tag.remove(Tag.TAG_SEO_DESC);
        tag.remove(Tag.TAG_SEO_TITLE);
        tag.remove(Tag.TAG_SEO_KEYWORDS);
        tag.remove(Tag.TAG_T_DESCRIPTION_TEXT);
        tag.remove(Tag.TAG_T_CREATE_TIME);
    }

    /**
     * Cleans unused fields of the specified users.
     *
     * @param users the specified users
     */
    public static void cleanUsers(final List<JSONObject> users) {
        for (final JSONObject user : users) {
            cleanUser(user);
        }
    }

    /**
     * Cleans unused fields of the specified user.
     *
     * @param user the specified user
     */
    public static void cleanUser(final JSONObject user) {
        user.remove(UserExt.USER_QQ);
        user.remove(UserExt.USER_B3_KEY);
        user.remove(UserExt.USER_POINT_STATUS);
        user.remove(UserExt.USER_LATEST_LOGIN_IP);
        user.remove(UserExt.USER_FOLLOWER_STATUS);
        user.remove(UserExt.USER_GUIDE_STEP);
        user.remove(UserExt.USER_ONLINE_STATUS);
        user.remove(UserExt.USER_CURRENT_CHECKIN_STREAK_START);
        user.remove(UserExt.USER_COMMENT_STATUS);
        user.remove(UserExt.USER_UA_STATUS);
        user.remove(UserExt.USER_LATEST_ARTICLE_TIME);
        user.remove(UserExt.USER_FORGE_LINK_STATUS);
        user.remove(UserExt.USER_AVATAR_TYPE);
        user.remove(UserExt.USER_SUB_MAIL_SEND_TIME);
        user.remove(UserExt.USER_UPDATE_TIME);
        user.remove(UserExt.USER_SUB_MAIL_STATUS);
        user.remove(UserExt.USER_JOIN_POINT_RANK);
        user.remove(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL);
        user.remove(UserExt.USER_LATEST_LOGIN_TIME);
        user.remove(User.USER_PASSWORD);
        user.remove(UserExt.USER_AVATAR_VIEW_MODE);
        user.remove(UserExt.USER_LONGEST_CHECKIN_STREAK_END);
        user.remove(UserExt.USER_WATCHING_ARTICLE_STATUS);
        user.remove(UserExt.USER_LATEST_CMT_TIME);
        user.remove(UserExt.USER_FOLLOWING_TAG_STATUS);
        user.remove(UserExt.USER_TIMELINE_STATUS);
        user.remove(UserExt.USER_B3_CLIENT_UPDATE_ARTICLE_URL);
        user.remove(UserExt.USER_JOIN_USED_POINT_RANK);
        user.remove(UserExt.USER_CURRENT_CHECKIN_STREAK_END);
        user.remove(UserExt.USER_FOLLOWING_ARTICLE_STATUS);
        user.remove(UserExt.USER_KEYBOARD_SHORTCUTS_STATUS);
        user.remove(User.USER_EMAIL);
        user.remove(UserExt.USER_ARTICLE_STATUS);
        user.remove(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL);
        user.remove(UserExt.USER_GEO_STATUS);
        user.remove(UserExt.USER_LONGEST_CHECKIN_STREAK_START);
        user.remove(UserExt.USER_NOTIFY_STATUS);
        user.remove(UserExt.USER_FOLLOWING_USER_STATUS);
        user.remove(UserExt.SYNC_TO_CLIENT);
        user.remove(UserExt.USER_ONLINE_FLAG);
        user.remove(UserExt.USER_TIMEZONE);
        user.remove(UserExt.USER_LIST_PAGE_SIZE);
        user.remove(UserExt.USER_MOBILE_SKIN);
        user.remove(UserExt.USER_SKIN);
        user.remove(UserExt.USER_STATUS);
        user.remove(UserExt.USER_COUNTRY);
        user.remove(UserExt.USER_PROVINCE);
        user.remove(UserExt.USER_CITY);
        user.remove(UserExt.USER_COMMENT_VIEW_MODE);
    }
}
