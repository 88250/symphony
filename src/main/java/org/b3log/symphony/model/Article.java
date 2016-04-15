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
package org.b3log.symphony.model;

/**
 * This class defines all article model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.13.0.11, Apr 15, 2016
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
     * Key of article author email.
     */
    public static final String ARTICLE_AUTHOR_EMAIL = "articleAuthorEmail";

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

    //// Transient ////
    /**
     * Key of article stick check.
     */
    public static final String ARTICLE_T_STICK_CHECK = "articleStickCheck";

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
     * Checks the specified article type is whether invalid.
     *
     * @param articleType the specified article type
     * @return {@code true} if it is invalid, otherwise returns {@code false}
     */
    public static boolean isInvalidArticleType(final int articleType) {
        return articleType < 0 || articleType > Article.ARTICLE_TYPE_C_THOUGHT;
    }

    /**
     * Private constructor.
     */
    private Article() {
    }
}
