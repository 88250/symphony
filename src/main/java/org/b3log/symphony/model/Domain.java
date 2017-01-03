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
 * This class defines domain model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Mar 14, 2016
 * @since 1.4.0
 */
public final class Domain {

    /**
     * Domain.
     */
    public static final String DOMAIN = "domain";

    /**
     * Domains.
     */
    public static final String DOMAINS = "domains";

    /**
     * Key of domain title.
     */
    public static final String DOMAIN_TITLE = "domainTitle";

    /**
     * Key of domain URI.
     */
    public static final String DOMAIN_URI = "domainURI";

    /**
     * Key of domain description.
     */
    public static final String DOMAIN_DESCRIPTION = "domainDescription";

    /**
     * Key of domain type.
     */
    public static final String DOMAIN_TYPE = "domainType";

    /**
     * Key of domain sort.
     */
    public static final String DOMAIN_SORT = "domainSort";

    /**
     * Key of domain tag count.
     */
    public static final String DOMAIN_TAG_COUNT = "domainTagCnt";

    /**
     * Key of domain icon path.
     */
    public static final String DOMAIN_ICON_PATH = "domainIconPath";

    /**
     * Key of domain CSS.
     */
    public static final String DOMAIN_CSS = "domainCSS";

    /**
     * Key of domain status.
     */
    public static final String DOMAIN_STATUS = "domainStatus";

    /**
     * Key of domain seo title.
     */
    public static final String DOMAIN_SEO_TITLE = "domainSeoTitle";

    /**
     * Key of domain seo keywords.
     */
    public static final String DOMAIN_SEO_KEYWORDS = "domainSeoKeywords";

    /**
     * Key of domain seo description.
     */
    public static final String DOMAIN_SEO_DESC = "domainSeoDesc";

    //// Transient ////
    /**
     * Key of domain count.
     */
    public static final String DOMAIN_T_COUNT = "domainCnt";

    /**
     * Key of domain tags.
     */
    public static final String DOMAIN_T_TAGS = "domainTags";

    /**
     * Key of domain id.
     */
    public static final String DOMAIN_T_ID = "domainId";

    //// Status constants
    /**
     * Domain status - valid.
     */
    public static final int DOMAIN_STATUS_C_VALID = 0;

    /**
     * Domain status - invalid.
     */
    public static final int DOMAIN_STATUS_C_INVALID = 1;

    /**
     * Private constructor.
     */
    private Domain() {
    }
}
