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
 * This class defines tag model relevant keys.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Oct 11, 2012
 * @since 0.2.0
 */
public final class Tag {

    /**
     * Tag.
     */
    public static final String TAG = "tag";
    /**
     * Tags.
     */
    public static final String TAGS = "tags";
    /**
     * Key of tag title.
     */
    public static final String TAG_TITLE = "tagTitle";
    /**
     * Key of tag reference count.
     */
    public static final String TAG_REFERENCE_CNT = "tagReferenceCount";
    /**
     * Key of tag comment count.
     */
    public static final String TAG_COMMENT_CNT = "tagCommentCount";
    /**
     * Key of tag status.
     */
    public static final String TAG_STATUS = "tagStatus";
    //// Transient ////
    /**
     * Key of tag creator thumbnail URL.
     */
    public static final String TAG_T_CREATOR_THUMBNAIL_URL = "tagCreatorThumbnailURL";
    /**
     * Key of tag creator name.
     */
    public static final String TAG_T_CREATOR_NAME = "tagCreatorName";
    /**
     * Key of tag participants.
     */
    public static final String TAG_T_PARTICIPANTS = "tagParticipants";
    /**
     * Key of tag participant name.
     */
    public static final String TAG_T_PARTICIPANT_NAME = "tagParticipantName";
    /**
     * Key of tag participant thumbnail URL.
     */
    public static final String TAG_T_PARTICIPANT_THUMBNAIL_URL = "tagParticipantThumbnailURL";

    /**
     * Private constructor.
     */
    private Tag() {
    }
}
