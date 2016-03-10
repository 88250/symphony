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

import java.util.regex.Pattern;

/**
 * This class defines tag model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.6.3.3, Mar 10, 2016
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
     * Key of tag icon path.
     */
    public static final String TAG_ICON_PATH = "tagIconPath";

    /**
     * Key of tag description.
     */
    public static final String TAG_DESCRIPTION = "tagDescription";

    /**
     * Key of tag reference count.
     */
    public static final String TAG_REFERENCE_CNT = "tagReferenceCount";

    /**
     * Key of tag comment count.
     */
    public static final String TAG_COMMENT_CNT = "tagCommentCount";

    /**
     * Key of tag follower count.
     */
    public static final String TAG_FOLLOWER_CNT = "tagFollowerCount";

    /**
     * Key of tag status.
     */
    public static final String TAG_STATUS = "tagStatus";

    /**
     * Key of tag good count.
     */
    public static final String TAG_GOOD_CNT = "tagGoodCnt";

    /**
     * Key of tag bad count.
     */
    public static final String TAG_BAD_CNT = "tagBadCnt";

    //// Transient ////
    /**
     * Key of tag description text.
     */
    public static final String TAG_T_DESCRIPTION_TEXT = "tagDescriptionText";
    
    /**
     * Key of tag create time.
     */
    public static final String TAG_T_CREATE_TIME = "tagCreateTime";

    /**
     * Key of tag creator thumbnail URL.
     */
    public static final String TAG_T_CREATOR_THUMBNAIL_URL = "tagCreatorThumbnailURL";

    /**
     * Key of tag creator thumbnail update time.
     */
    public static final String TAG_T_CREATOR_THUMBNAIL_UPDATE_TIME = "tagCreatorThumbnailUpdateTime";

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
     * Key of tag participant thumbnail update time.
     */
    public static final String TAG_T_PARTICIPANT_THUMBNAIL_UPDATE_TIME = "tagParticipantThumbnailUpdateTime";

    /**
     * Key of tag participant URL.
     */
    public static final String TAG_T_PPARTICIPANT_URL = "tagParticipantURL";

    /**
     * Key of related tags.
     */
    public static final String TAG_T_RELATED_TAGS = "tagRelatedTags";

    //// Tag type constants
    /**
     * Tag type - creator.
     */
    public static final int TAG_TYPE_C_CREATOR = 0;

    /**
     * Tag type - article.
     */
    public static final int TAG_TYPE_C_ARTICLE = 1;

    /**
     * Tag type - user self.
     */
    public static final int TAG_TYPE_C_USER_SELF = 2;

    //// Status constants
    /**
     * Tag status - valid.
     */
    public static final int TAG_STATUS_C_VALID = 0;

    /**
     * Tag status - invalid.
     */
    public static final int TAG_STATUS_C_INVALID = 1;

    /// Validation
    /**
     * Max tag title length.
     */
    public static final int MAX_TAG_TITLE_LENGTH = 50;

    /**
     * Tag title pattern.
     */
    public static final Pattern TAG_TITLE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5,\\w,\\s,&,\\+,\\-,\\.]+");

    /**
     * Private constructor.
     */
    private Tag() {
    }
}
