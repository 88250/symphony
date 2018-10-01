/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.model;

/**
 * This class defines all link model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.4.0.0, Oct 1, 2018
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
     * Key of link address hash.
     */
    public static final String LINK_ADDR_HASH = "linkAddrHash";

    /**
     * Key of link address.
     */
    public static final String LINK_ADDR = "linkAddr";

    /**
     * Key of link title.
     */
    public static final String LINK_TITLE = "linkTitle";

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

    /**
     * Key of link ping count.
     */
    public static final String LINK_PING_CNT = "linkPingCnt";

    /**
     * Key of link ping error count.
     */
    public static final String LINK_PING_ERR_CNT = "linkPingErrCnt";

    /**
     * Key of link ping time.
     */
    public static final String LINK_PING_TIME = "linkPingTime";

    /**
     * Key of link card HTML.
     */
    public static final String LINK_CARD_HTML = "linkCardHTML";

    //// Transient ////
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

    /**
     * Key of link description.
     */
    public static final String LINK_T_DESCRIPTION = "linkDescription";

    /**
     * Key of link image.
     */
    public static final String LINK_T_IMAGE = "linkImage";

    /**
     * Key of link site.
     */
    public static final String LINK_T_SITE = "linkSite";

    /**
     * Key of link site domain.
     */
    public static final String LINK_T_SITE_DOMAIN = "linkSiteDomain";

    /**
     * Key of link site address.
     */
    public static final String LINK_T_SITE_ADDR = "linkSiteAddr";

    /**
     * Key of link site icon.
     */
    public static final String LINK_T_SITE_ICON = "linkSiteIcon";

    /**
     * Private constructor.
     */
    private Link() {
    }
}
