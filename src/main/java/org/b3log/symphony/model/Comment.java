/*
 * Copyright (c) 2012, B3log Team
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
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Oct 9, 2012
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
    //// Transient ////
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
     * Key of comment article permalink.
     */
    public static final String COMMENT_T_ARTICLE_PERMALINK = "commentArticlePermalink";
}
