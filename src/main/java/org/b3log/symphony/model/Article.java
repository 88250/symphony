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
package org.b3log.symphony.model;

/**
 * This class defines all article model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.23.0.13, Jan 21, 2017
 * @since 0.2.0
 */
public final class Article {

    /**
     * Article.
     */
    public static final String ARTICLE = "article";

    /**
     * Articles.
     */
    public static final String ARTICLES = "articles";

    /**
     * Key of article title.
     */
    public static final String ARTICLE_TITLE = "articleTitle";

    /**
     * Key of article content.
     */
    public static final String ARTICLE_CONTENT = "articleContent";

    /**
     * Key of article reward content.
     */
    public static final String ARTICLE_REWARD_CONTENT = "articleRewardContent";

    /**
     * Key of article reward point.
     */
    public static final String ARTICLE_REWARD_POINT = "articleRewardPoint";

    /**
     * Key of article tags.
     */
    public static final String ARTICLE_TAGS = "articleTags";

    /**
     * Key of article author id.
     */
    public static final String ARTICLE_AUTHOR_ID = "articleAuthorId";

    /**
     * Key of article comment count.
     */
    public static final String ARTICLE_COMMENT_CNT = "articleCommentCount";

    /**
     * Key of article view count.
     */
    public static final String ARTICLE_VIEW_CNT = "articleViewCount";

    /**
     * Key of article permalink.
     */
    public static final String ARTICLE_PERMALINK = "articlePermalink";

    /**
     * Key of article create time.
     */
    public static final String ARTICLE_CREATE_TIME = "articleCreateTime";

    /**
     * Key of article update time.
     */
    public static final String ARTICLE_UPDATE_TIME = "articleUpdateTime";

    /**
     * Key of article latest comment time.
     */
    public static final String ARTICLE_LATEST_CMT_TIME = "articleLatestCmtTime";

    /**
     * Key of article latest commenter name.
     */
    public static final String ARTICLE_LATEST_CMTER_NAME = "articleLatestCmterName";

    /**
     * Key of article random double value.
     */
    public static final String ARTICLE_RANDOM_DOUBLE = "articleRandomDouble";

    /**
     * Key of article commentable.
     */
    public static final String ARTICLE_COMMENTABLE = "articleCommentable";

    /**
     * Key of article sync to client.
     */
    public static final String ARTICLE_SYNC_TO_CLIENT = "syncWithSymphonyClient";

    /**
     * Key of client article id.
     */
    public static final String ARTICLE_CLIENT_ARTICLE_ID = "clientArticleId";

    /**
     * Key of client article permalink.
     */
    public static final String ARTICLE_CLIENT_ARTICLE_PERMALINK = "clientArticlePermalink";

    /**
     * Key of article editor type.
     */
    public static final String ARTICLE_EDITOR_TYPE = "articleEditorType";

    /**
     * Key of article status.
     */
    public static final String ARTICLE_STATUS = "articleStatus";

    /**
     * Key of article type.
     */
    public static final String ARTICLE_TYPE = "articleType";

    /**
     * Key of article good count.
     */
    public static final String ARTICLE_GOOD_CNT = "articleGoodCnt";

    /**
     * Key of article bad count.
     */
    public static final String ARTICLE_BAD_CNT = "articleBadCnt";

    /**
     * Key of article collection count.
     */
    public static final String ARTICLE_COLLECT_CNT = "articleCollectCnt";

    /**
     * Key of article watch count.
     */
    public static final String ARTICLE_WATCH_CNT = "articleWatchCnt";

    /**
     * Key of reddit score.
     */
    public static final String REDDIT_SCORE = "redditScore";

    /**
     * Key of article city.
     */
    public static final String ARTICLE_CITY = "articleCity";

    /**
     * Key of article IP.
     */
    public static final String ARTICLE_IP = "articleIP";

    /**
     * Key of article UA.
     */
    public static final String ARTICLE_UA = "articleUA";

    /**
     * Key of article stick.
     */
    public static final String ARTICLE_STICK = "articleStick";

    /**
     * Key of article anonymous.
     */
    public static final String ARTICLE_ANONYMOUS = "articleAnonymous";

    /**
     * Key of article perfect.
     */
    public static final String ARTICLE_PERFECT = "articlePerfect";

    /**
     * Key of article anonymous view.
     */
    public static final String ARTICLE_ANONYMOUS_VIEW = "articleAnonymousView";

    //// Transient ////
    /**
     * Key of article latest comment.
     */
    public static final String ARTICLE_T_LATEST_CMT = "articleLatestCmt";

    /**
     * Key of previous article.
     */
    public static final String ARTICLE_T_PREVIOUS = "articlePrevious";

    /**
     * Key of next article.
     */
    public static final String ARTICLE_T_NEXT = "articleNext";

    /**
     * Key of article tag objects.
     */
    public static final String ARTICLE_T_TAG_OBJS = "articleTagObjs";

    /**
     * Key of article vote.
     */
    public static final String ARTICLE_T_VOTE = "articleVote";

    /**
     * Key of article stick flag.
     */
    public static final String ARTICLE_T_IS_STICK = "articleIsStick";

    /**
     * Key of article stick remains.
     */
    public static final String ARTICLE_T_STICK_REMAINS = "articleStickRemains";

    /**
     * Key of article preview content.
     */
    public static final String ARTICLE_T_PREVIEW_CONTENT = "articlePreviewContent";

    /**
     * Key of article view count display format.
     */
    public static final String ARTICLE_T_VIEW_CNT_DISPLAY_FORMAT = "articleViewCntDisplayFormat";

    /**
     * Key of article id.
     */
    public static final String ARTICLE_T_ID = "articleId";

    /**
     * Key of article ids.
     */
    public static final String ARTICLE_T_IDS = "articleIds";

    /**
     * Key of article author.
     */
    public static final String ARTICLE_T_AUTHOR = "articleAuthor";

    /**
     * Key of article author thumbnail URL.
     */
    public static final String ARTICLE_T_AUTHOR_THUMBNAIL_URL = "articleAuthorThumbnailURL";

    /**
     * Key of article author name.
     */
    public static final String ARTICLE_T_AUTHOR_NAME = "articleAuthorName";

    /**
     * Key of article author URL.
     */
    public static final String ARTICLE_T_AUTHOR_URL = "articleAuthorURL";

    /**
     * Key of article author intro.
     */
    public static final String ARTICLE_T_AUTHOR_INTRO = "articleAuthorIntro";

    /**
     * Key of article comments.
     */
    public static final String ARTICLE_T_COMMENTS = "articleComments";

    /**
     * Key of article nice comments.
     */
    public static final String ARTICLE_T_NICE_COMMENTS = "articleNiceComments";

    /**
     * Key of article participants.
     */
    public static final String ARTICLE_T_PARTICIPANTS = "articleParticipants";

    /**
     * Key of article participant name.
     */
    public static final String ARTICLE_T_PARTICIPANT_NAME = "articleParticipantName";

    /**
     * Key of article participant thumbnail URL.
     */
    public static final String ARTICLE_T_PARTICIPANT_THUMBNAIL_URL = "articleParticipantThumbnailURL";

    /**
     * Key of article participant thumbnail update time.
     */
    public static final String ARTICLE_T_PARTICIPANT_THUMBNAIL_UPDATE_TIME = "articleParticipantThumbnailUpdateTime";

    /**
     * Key of article participant URL.
     */
    public static final String ARTICLE_T_PARTICIPANT_URL = "articleParticipantURL";

    /**
     * Key of is broadcast.
     */
    public static final String ARTICLE_T_IS_BROADCAST = "articleIsBroadcast";

    /**
     * Key of article title with Emoj.
     */
    public static final String ARTICLE_T_TITLE_EMOJI = "articleTitleEmoj";

    /**
     * Key of article heat.
     */
    public static final String ARTICLE_T_HEAT = "articleHeat";

    /**
     * Key of article ToC.
     */
    public static final String ARTICLE_T_TOC = "articleToC";

    // Anonymous constants
    /**
     * Article anonymous - public.
     */
    public static final int ARTICLE_ANONYMOUS_C_PUBLIC = 0;

    /**
     * Article anonymous - anonymous.
     */
    public static final int ARTICLE_ANONYMOUS_C_ANONYMOUS = 1;

    // Perfect constants
    /**
     * Article perfect - not perfect.
     */
    public static final int ARTICLE_PERFECT_C_NOT_PERFECT = 0;

    /**
     * Article perfect - perfect.
     */
    public static final int ARTICLE_PERFECT_C_PERFECT = 1;

    // Anonymous view constants
    /**
     * Article anonymous view - use global.
     */
    public static final int ARTICLE_ANONYMOUS_VIEW_C_USE_GLOBAL = 0;

    /**
     * Article anonymous view - not allow.
     */
    public static final int ARTICLE_ANONYMOUS_VIEW_C_NOT_ALLOW = 1;

    /**
     * Article anonymous view - allow.
     */
    public static final int ARTICLE_ANONYMOUS_VIEW_C_ALLOW = 2;

    // Status constants
    /**
     * Article status - valid.
     */
    public static final int ARTICLE_STATUS_C_VALID = 0;

    /**
     * Article status - invalid.
     */
    public static final int ARTICLE_STATUS_C_INVALID = 1;

    // Type constants
    /**
     * Article type - normal.
     */
    public static final int ARTICLE_TYPE_C_NORMAL = 0;

    /**
     * Article type - discussion.
     */
    public static final int ARTICLE_TYPE_C_DISCUSSION = 1;

    /**
     * Article type - city broadcast.
     */
    public static final int ARTICLE_TYPE_C_CITY_BROADCAST = 2;

    /**
     * Article type - <a href="https://hacpai.com/article/1441942422856">thought</a>.
     */
    public static final int ARTICLE_TYPE_C_THOUGHT = 3;

    /**
     * Article type - <a href="https://hacpai.com/article/1483240295087">book</a>。
     */
    public static final int ARTICLE_TYPE_C_BOOK = 4;

    /**
     * Checks the specified article type is whether invalid.
     *
     * @param articleType the specified article type
     * @return {@code true} if it is invalid, otherwise returns {@code false}
     */
    public static boolean isInvalidArticleType(final int articleType) {
        return articleType < 0 || articleType > Article.ARTICLE_TYPE_C_BOOK;
    }

    /**
     * Private constructor.
     */
    private Article() {
    }
}
