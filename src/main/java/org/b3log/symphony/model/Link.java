/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2016,  b3log.org & hacpai.com
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
 * This class defines all link model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 7, 2016
 * @since 1.6.0
 */
public final class Link {

    /**
     * Link.
     */
    public static final String LINK = "link";

    /**
     * Links.
     */
    public static final String LINKS = "links";

    /**
     * Key of link address.
     */
    public static final String LINK_ADDR = "linkAddr";

    /**
     * Key of link title.
     */
    public static final String LINK_TITLE = "linkTitle";

    /**
     * Key of link type.
     */
    public static final String LINK_TYPE = "linkType";

    /**
     * Key of link submit count.
     */
    public static final String LINK_SUBMIT_CNT = "linkSubmitCnt";

    /**
     * Key of link click count.
     */
    public static final String LINK_CLICK_CNT = "linkClickCnt";

    /**
     * Key of link good count.
     */
    public static final String LINK_GOOD_CNT = "linkGoodCnt";

    /**
     * Key of link bad count.
     */
    public static final String LINK_BAD_CNT = "linkBadCnt";

    /**
     * Key of link Baidu reference count.
     */
    public static final String LINK_BAIDU_REF_CNT = "linkBaiduRefCnt";

    /**
     * Key of link score.
     */
    public static final String LINK_SCORE = "linkScore";

    //// Transient ////
    /**
     * Key of link id.
     */
    public static final String LINK_T_ID = "linkId";

    /**
     * Key of link keywords.
     */
    public static final String LINK_T_KEYWORDS = "linkKeywords";

    /**
     * Key of link HTML.
     */
    public static final String LINK_T_HTML = "linkHTML";

    /**
     * Key of link text.
     */
    public static final String LINK_T_TEXT = "linkText";

    /**
     * Key of link count.
     */
    public static final String LINK_T_COUNT = "linkCnt";

    //// Type constants
    /**
     * Link type - forge.
     */
    public static final int LINK_TYPE_C_FORGE = 0;

    /**
     * Private constructor.
     */
    private Link() {
    }
}
