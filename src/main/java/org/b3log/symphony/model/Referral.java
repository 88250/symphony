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
 * This class defines referral model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Apr 28, 2016
 * @since 1.4.0
 */
public final class Referral {

    /**
     * Referral.
     */
    public static final String REFERRAL = "referral";

    /**
     * Referrals.
     */
    public static final String REFERRALS = "referrals";

    /**
     * Key of referral user.
     */
    public static final String REFERRAL_USER = "referralUser";

    /**
     * Key of referral data id.
     */
    public static final String REFERRAL_DATA_ID = "referralDataId";

    /**
     * Key of referral type.
     */
    public static final String REFERRAL_TYPE = "referralType";

    /**
     * Key of source IP.
     */
    public static final String REFERRAL_IP = "referralIP";

    /**
     * Key of click.
     */
    public static final String REFERRAL_CLICK = "referralClick";

    /**
     * Key of referral user has point.
     */
    public static final String REFERRAL_USER_HAS_POINT = "referralUserHasPoint";

    /**
     * Key of referral author has point.
     */
    public static final String REFERRAL_AUTHOR_HAS_POINT = "referralAuthorHasPoint";

    // Type constants
    /**
     * Type - Article.
     */
    public static final int REFERRAL_TYPE_C_ARTICLE = 0;
}
