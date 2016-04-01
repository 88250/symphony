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
 * This class defines all comment model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.8, Apr 1, 2016
 * @since 0.2.0
 */
public final class Comment {

    /**
     * Comment.
     */
    public static final String COMMENT = "comment";

    /**
     * Comments.
     */
    public static final String COMMENTS = "comments";

    /**
     * Key of comment content.
     */
    public static final String COMMENT_CONTENT = "commentContent";

    /**
     * Key of comment create time.
     */
    public static final String COMMENT_CREATE_TIME = "commentCreateTime";

    /**
     * Key of comment author email.
     */
    public static final String COMMENT_AUTHOR_EMAIL = "commentAuthorEmail";

    /**
     * Key of comment author id.
     */
    public static final String COMMENT_AUTHOR_ID = "commentAuthorId";

    /**
     * Key of comment on article id.
     */
    public static final String COMMENT_ON_ARTICLE_ID = "commentOnArticleId";

    /**
     * Key of client comment id.
     */
    public static final String COMMENT_CLIENT_COMMENT_ID = "clientCommentId";

    /**
     * Key of comment sharp URL.
     */
    public static final String COMMENT_SHARP_URL = "commentSharpURL";

    /**
     * Key of original comment id.
     */
    public static final String COMMENT_ORIGINAL_COMMENT_ID = "commentOriginalCommentId";

    /**
     * Key of comment status.
     */
    public static final String COMMENT_STATUS = "commentStatus";

    /**
     * Key of comment IP.
     */
    public static final String COMMENT_IP = "commentIP";

    /**
     * Key of comment UA.
     */
    public static final String COMMENT_UA = "commentUA";

    //// Transient ////
    /**
     * Key of commenter.
     */
    public static final String COMMENT_T_COMMENTER = "commenter";

    /**
     * Key of comment id.
     */
    public static final String COMMENT_T_ID = "commentId";

    /**
     * Key of comment on symphony article id.
     */
    public static final String COMMENT_T_SYMPHONY_ID = "commentSymphonyArticleId";

    /**
     * Key of comment author thumbnail URL.
     */
    public static final String COMMENT_T_AUTHOR_THUMBNAIL_URL = "commentAuthorThumbnailURL";

    /**
     * Key of comment author name.
     */
    public static final String COMMENT_T_AUTHOR_NAME = "commentAuthorName";

    /**
     * Key of comment author URL.
     */
    public static final String COMMENT_T_AUTHOR_URL = "commentAuthorURL";

    /**
     * Key of comment article title.
     */
    public static final String COMMENT_T_ARTICLE_TITLE = "commentArticleTitle";

    /**
     * Key of comment article type.
     */
    public static final String COMMENT_T_ARTICLE_TYPE = "commentArticleType";

    /**
     * Key of comment article author name.
     */
    public static final String COMMENT_T_ARTICLE_AUTHOR_NAME = "commentArticleAuthorName";

    /**
     * Key of comment article author URL.
     */
    public static final String COMMENT_T_ARTICLE_AUTHOR_URL = "commentArticleAuthorURL";

    /**
     * Key of comment article author thumbnail URL.
     */
    public static final String COMMENT_T_ARTICLE_AUTHOR_THUMBNAIL_URL = "commentArticleAuthorThumbnailURL";

    /**
     * Key of comment article permalink.
     */
    public static final String COMMENT_T_ARTICLE_PERMALINK = "commentArticlePermalink";

    /**
     * Key of comment thank label.
     */
    public static final String COMMENT_T_THANK_LABEL = "commentThankLabel";

    //// Status constants
    /**
     * Comment status - valid.
     */
    public static final int COMMENT_STATUS_C_VALID = 0;

    /**
     * Comment status - invalid.
     */
    public static final int COMMENT_STATUS_C_INVALID = 1;

    /**
     * Private constructor.
     */
    private Comment() {
    }
}
