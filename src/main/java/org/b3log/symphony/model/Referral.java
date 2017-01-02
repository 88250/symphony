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
