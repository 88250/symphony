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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.util.Symphonys;

/**
 * This class defines tag model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.10.3.4, May 3, 2016
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

    /**
     * Key of tag seo title.
     */
    public static final String TAG_SEO_TITLE = "tagSeoTitle";

    /**
     * Key of tag seo keywords.
     */
    public static final String TAG_SEO_KEYWORDS = "tagSeoKeywords";

    /**
     * Key of tag seo description.
     */
    public static final String TAG_SEO_DESC = "tagSeoDesc";

    /**
     * Key of tag random double value.
     */
    public static final String TAG_RANDOM_DOUBLE = "tagRandomDouble";

    //// Transient ////
    /**
     * Key of tag domains.
     */
    public static final String TAG_T_DOMAINS = "tagDomains";

    /**
     * Key of tag count.
     */
    public static final String TAG_T_COUNT = "tagCnt";

    /**
     * Key of tag id.
     */
    public static final String TAG_T_ID = "tagId";

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
    public static final int MAX_TAG_TITLE_LENGTH = 9;

    /**
     * Max tag count.
     */
    public static final int MAX_TAG_COUNT = 4;

    /**
     * Tag title pattern string.
     */
    public static final String TAG_TITLE_PATTERN_STR = "[\\u4e00-\\u9fa5,\\w,\\s,&,\\+,\\-,\\.]+";

    /**
     * Tag title pattern.
     */
    public static final Pattern TAG_TITLE_PATTERN = Pattern.compile(TAG_TITLE_PATTERN_STR);

    /**
     * Uses the head tags.
     *
     * @param tagStr the specified tags
     * @param num the specified used number
     * @return head tags
     */
    public static String useHead(final String tagStr, final int num) {
        final String[] tags = tagStr.split(",");
        if (tags.length <= num) {
            return tagStr;
        }

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++) {
            sb.append(tags[i]).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     * Formats the specified tags.
     *
     * <ul>
     * <li>Trims every tag</li>
     * <li>Deduplication</li>
     * </ul>
     *
     * @param tagStr the specified tags
     * @return formatted tags string
     */
    public static String formatTags(final String tagStr) {
        final String tagStr1 = tagStr.replaceAll("\\s+", "").replaceAll("，", ",").replaceAll("、", ",").
                replaceAll("；", ",").replaceAll(";", ",");
        String[] tagTitles = tagStr1.split(",");

        tagTitles = Strings.trimAll(tagTitles);

        // deduplication
        final Set<String> titles = new LinkedHashSet<String>();
        for (final String tagTitle : tagTitles) {
            if (!exists(titles, tagTitle)) {
                titles.add(tagTitle);
            }
        }

        tagTitles = titles.toArray(new String[0]);

        int count = 0;
        final StringBuilder tagsBuilder = new StringBuilder();
        for (final String tagTitle : tagTitles) {
            final String title = tagTitle.trim();
            if (StringUtils.isBlank(title)) {
                continue;
            }

            if (StringUtils.length(title) > MAX_TAG_TITLE_LENGTH) {
                continue;
            }

            if (!TAG_TITLE_PATTERN.matcher(title).matches()) {
                continue;
            }

            tagsBuilder.append(title).append(",");
            count++;

            if (count >= MAX_TAG_COUNT) {
                break;
            }
        }
        if (tagsBuilder.length() > 0) {
            tagsBuilder.deleteCharAt(tagsBuilder.length() - 1);
        }

        return tagsBuilder.toString();
    }

    /**
     * Checks the specified tag string whether contains the reserved tags.
     *
     * @param tagStr the specified tag string
     * @return {@code true} if it contains, returns {@code false} otherwise
     */
    public static boolean containsReservedTags(final String tagStr) {
        for (final String reservedTag : Symphonys.RESERVED_TAGS) {
            if (StringUtils.containsIgnoreCase(tagStr, reservedTag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks the specified title exists in the specified title set.
     *
     * @param titles the specified title set
     * @param title the specified title to check
     * @return {@code true} if exists, returns {@code false} otherwise
     */
    private static boolean exists(final Set<String> titles, final String title) {
        for (final String setTitle : titles) {
            if (setTitle.equalsIgnoreCase(title)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Private constructor.
     */
    private Tag() {
    }
}
