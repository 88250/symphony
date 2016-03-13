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
 * This class defines domain model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 13, 2016
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
