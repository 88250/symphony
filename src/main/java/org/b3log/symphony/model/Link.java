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
