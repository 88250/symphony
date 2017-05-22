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

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.LatkeBeanManagerImpl;
import org.b3log.latke.ioc.Lifecycle;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.service.ShortLinkQueryService;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.*;
import java.util.regex.Pattern;

/**
 * This class defines tag model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author Bill Ho
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.17.0.0, May 20, 2017
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
     * Key of tag URI.
     */
    public static final String TAG_URI = "tagURI";

    /**
     * Key of tag icon path.
     */
    public static final String TAG_ICON_PATH = "tagIconPath";

    /**
     * Key of tag CSS.
     */
    public static final String TAG_CSS = "tagCSS";

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
     * Key of link count.
     */
    public static final String TAG_LINK_CNT = "tagLinkCount";

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

    /**
     * Key of tag title lower case.
     */
    public static final String TAG_T_TITLE_LOWER_CASE = "tagTitleLowerCase";

    /**
     * Key of tag links.
     */
    public static final String TAG_T_LINKS = "tagLinks";

    /**
     * Key of tag links count.
     */
    public static final String TAG_T_LINKS_CNT = "tagLinksCnt";

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

    // Status constants
    /**
     * Tag status - valid.
     */
    public static final int TAG_STATUS_C_VALID = 0;

    /**
     * Tag status - invalid.
     */
    public static final int TAG_STATUS_C_INVALID = 1;

    // Tag title constants
    /**
     * Title - Sandbox.
     */
    public static final String TAG_TITLE_C_SANDBOX = "Sandbox";

    // Validation
    /**
     * Max tag title length.
     */
    public static final int MAX_TAG_TITLE_LENGTH = (null == Symphonys.getInt("tag.maxTagTitleLength"))
            ? 9 : Symphonys.getInt("tag.maxTagTitleLength");

    /**
     * Max tag count.
     */
    public static final int MAX_TAG_COUNT = 4;

    /**
     * Tag title pattern string.
     */
    public static final String TAG_TITLE_PATTERN_STR = "[\\u4e00-\\u9fa5,\\w,&,\\+,\\-,\\.]+";

    /**
     * Tag title pattern.
     */
    public static final Pattern TAG_TITLE_PATTERN = Pattern.compile(TAG_TITLE_PATTERN_STR);

    /**
     * Normalized tag title mappings.
     */
    private static final Map<String, Set<String>> NORMALIZE_MAPPINGS = new HashMap<>();

    static {
        NORMALIZE_MAPPINGS.put("JavaScript", new HashSet<>(Arrays.asList("JS")));
        NORMALIZE_MAPPINGS.put("Elasticsearch", new HashSet<>(Arrays.asList("ES搜索引擎", "ES搜索", "ES")));
        NORMALIZE_MAPPINGS.put("golang", new HashSet<>(Arrays.asList("Go", "Go语言")));
        NORMALIZE_MAPPINGS.put("线程", new HashSet<>(Arrays.asList("多线程", "Thread")));
        NORMALIZE_MAPPINGS.put("Vue.js", new HashSet<>(Arrays.asList("Vue")));
        NORMALIZE_MAPPINGS.put("Node.js", new HashSet<>(Arrays.asList("NodeJS")));
    }

    /**
     * Private constructor.
     */
    private Tag() {
    }

    /**
     * Uses the head tags.
     *
     * @param tagStr the specified tags
     * @param num    the specified used number
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
     * <p>
     * <ul>
     * <li>Trims every tag</li>
     * <li>Deduplication</li>
     * </ul>
     * </p>
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
        final Set<String> titles = new LinkedHashSet<>();
        for (final String tagTitle : tagTitles) {
            if (!exists(titles, tagTitle)) {
                titles.add(tagTitle);
            }
        }

        tagTitles = titles.toArray(new String[0]);

        int count = 0;
        final StringBuilder tagsBuilder = new StringBuilder();
        for (final String tagTitle : tagTitles) {
            String title = tagTitle.trim();
            if (StringUtils.isBlank(title)) {
                continue;
            }

            if (containsWhiteListTags(title)) {
                tagsBuilder.append(title).append(",");
                count++;

                if (count >= MAX_TAG_COUNT) {
                    break;
                }

                continue;
            }

            if (StringUtils.length(title) > MAX_TAG_TITLE_LENGTH) {
                continue;
            }

            if (!TAG_TITLE_PATTERN.matcher(title).matches()) {
                continue;
            }

            title = normalize(title);
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
     * Checks the specified tag string whether contains the white list tags.
     *
     * @param tagStr the specified tag string
     * @return {@code true} if it contains, returns {@code false} otherwise
     */
    public static boolean containsWhiteListTags(final String tagStr) {
        for (final String whiteListTag : Symphonys.WHITE_LIST_TAGS) {
            if (StringUtils.equalsIgnoreCase(tagStr, whiteListTag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks the specified title exists in the specified title set.
     *
     * @param titles the specified title set
     * @param title  the specified title to check
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
     * Normalizes the specified title. For example, Normalizes "JS" to "JavaScript.
     *
     * @param title the specified title
     * @return normalized title
     */
    private static String normalize(final String title) {
        final TagCache cache = LatkeBeanManagerImpl.getInstance().getReference(TagCache.class);
        final List<JSONObject> iconTags = cache.getIconTags(Integer.MAX_VALUE);
        Collections.sort(iconTags, (t1, t2) -> {
            final String u1Title = t1.optString(Tag.TAG_T_TITLE_LOWER_CASE);
            final String u2Title = t2.optString(Tag.TAG_T_TITLE_LOWER_CASE);

            return u2Title.length() - u1Title.length();
        });

        for (final JSONObject iconTag : iconTags) {
            final String iconTagTitle = iconTag.optString(Tag.TAG_TITLE);
            if (iconTagTitle.length() < 2) {
                break;
            }

            if (StringUtils.containsIgnoreCase(title, iconTagTitle)) {
                return iconTagTitle;
            }
        }

        final List<JSONObject> allTags = cache.getTags();
        Collections.sort(allTags, (t1, t2) -> {
            final String u1Title = t1.optString(Tag.TAG_T_TITLE_LOWER_CASE);
            final String u2Title = t2.optString(Tag.TAG_T_TITLE_LOWER_CASE);

            return u2Title.length() - u1Title.length();
        });

        for (final JSONObject tag : allTags) {
            final String tagURI = tag.optString(Tag.TAG_URI);
            final String tagTitle = tag.optString(Tag.TAG_TITLE);
            if (tagURI.equals(tagTitle)) {
                continue;
            }

            if (StringUtils.equals(title, tagURI)) {
                return tag.optString(Tag.TAG_TITLE);
            }
        }

        for (final Map.Entry<String, Set<String>> entry : NORMALIZE_MAPPINGS.entrySet()) {
            final Set<String> oddTitles = entry.getValue();
            for (final String oddTitle : oddTitles) {
                if (StringUtils.equalsIgnoreCase(title, oddTitle)) {
                    return entry.getKey();
                }
            }
        }

        return title;
    }

    /**
     * Fills the description for the specified tag.
     *
     * @param tag the specified tag
     */
    public static void fillDescription(final JSONObject tag) {
        String description = tag.optString(Tag.TAG_DESCRIPTION);
        String descriptionText = tag.optString(Tag.TAG_TITLE);
        if (StringUtils.isNotBlank(description)) {
            final LatkeBeanManager beanManager = Lifecycle.getBeanManager();
            final ShortLinkQueryService shortLinkQueryService = beanManager.getReference(ShortLinkQueryService.class);

            description = shortLinkQueryService.linkTag(description);
            description = Markdowns.toHTML(description);

            tag.put(Tag.TAG_DESCRIPTION, description);
            descriptionText = Jsoup.parse(description).text();
        }
        tag.put(Tag.TAG_T_DESCRIPTION_TEXT, descriptionText);
    }
}
