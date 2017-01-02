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
 * This class defines option model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.8.0.2, Dec 24, 2016
 * @since 0.2.0
 */
public final class Option {

    /**
     * Option.
     */
    public static final String OPTION = "option";

    /**
     * Options.
     */
    public static final String OPTIONS = "options";

    /**
     * Key of option value.
     */
    public static final String OPTION_VALUE = "optionValue";

    /**
     * Key of option category.
     */
    public static final String OPTION_CATEGORY = "optionCategory";

    // oId constants
    /**
     * Key of member count.
     */
    public static final String ID_C_STATISTIC_MEMBER_COUNT = "statisticMemberCount";

    /**
     * Key of article count.
     */
    public static final String ID_C_STATISTIC_ARTICLE_COUNT = "statisticArticleCount";

    /**
     * Key of domain count.
     */
    public static final String ID_C_STATISTIC_DOMAIN_COUNT = "statisticDomainCount";

    /**
     * Key of tag count.
     */
    public static final String ID_C_STATISTIC_TAG_COUNT = "statisticTagCount";

    /**
     * Key of link count.
     */
    public static final String ID_C_STATISTIC_LINK_COUNT = "statisticLinkCount";

    /**
     * Key of comment count.
     */
    public static final String ID_C_STATISTIC_CMT_COUNT = "statisticCmtCount";

    /**
     * Key of max online visitor count.
     */
    public static final String ID_C_STATISTIC_MAX_ONLINE_VISITOR_COUNT = "statisticMaxOnlineVisitorCount";

    /**
     * Key of allow register.
     */
    public static final String ID_C_MISC_ALLOW_REGISTER = "miscAllowRegister";

    /**
     * Key of allow anonymous view.
     */
    public static final String ID_C_MISC_ALLOW_ANONYMOUS_VIEW = "miscAllowAnonymousView";

    /**
     * Key of allow add article.
     */
    public static final String ID_C_MISC_ALLOW_ADD_ARTICLE = "miscAllowAddArticle";

    /**
     * Key of allow add comment.
     */
    public static final String ID_C_MISC_ALLOW_ADD_COMMENT = "miscAllowAddComment";

    /**
     * Key of language.
     */
    public static final String ID_C_MISC_LANGUAGE = "miscLanguage";

    /**
     * Key of side full ad.
     */
    public static final String ID_C_SIDE_FULL_AD = "adSideFull";

    /**
     * Key of header banner.
     */
    public static final String ID_C_HEADER_BANNER = "headerBanner";

    // Category constants
    /**
     * Statistic.
     */
    public static final String CATEGORY_C_STATISTIC = "statistic";

    /**
     * Miscellaneous.
     */
    public static final String CATEGORY_C_MISC = "misc";

    /**
     * Reserved words.
     */
    public static final String CATEGORY_C_RESERVED_WORDS = "reserved-words";

    /**
     * Ad.
     */
    public static final String CATEGORY_C_AD = "ad";

    /**
     * Private constructor.
     */
    private Option() {
    }
}
