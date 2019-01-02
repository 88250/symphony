/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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
 * This class defines all report model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.0, Jul 15, 2018
 * @since 3.1.0
 */
public final class Report {

    /**
     * Report.
     */
    public static final String REPORT = "report";

    /**
     * Reports.
     */
    public static final String REPORTS = "reports";

    /**
     * Key of report user id.
     */
    public static final String REPORT_USER_ID = "reportUserId";

    /**
     * Key of report data id.
     */
    public static final String REPORT_DATA_ID = "reportDataId";

    /**
     * Key of report data type.
     */
    public static final String REPORT_DATA_TYPE = "reportDataType";

    /**
     * Key of report type.
     */
    public static final String REPORT_TYPE = "reportType";

    /**
     * Key of report memo.
     */
    public static final String REPORT_MEMO = "reportMemo";

    /**
     * Key of report handled.
     */
    public static final String REPORT_HANDLED = "reportHandled";

    // Report data type constants
    /**
     * Report data type - Article.
     */
    public static final int REPORT_DATA_TYPE_C_ARTICLE = 0;

    /**
     * Report data type - comment.
     */
    public static final int REPORT_DATA_TYPE_C_COMMENT = 1;

    /**
     * Report data type - user.
     */
    public static final int REPORT_DATA_TYPE_C_USER = 2;

    // Report type constants
    /**
     * Report type - Spam AD.
     */
    public static final int REPORT_TYPE_C_SPAM_AD = 0;

    /**
     * Report type - Pornographic.
     */
    public static final int REPORT_TYPE_C_PORNOGRAPHIC = 1;

    /**
     * Report type - Violation of regulations.
     */
    public static final int REPORT_TYPE_C_VIOLATION_OF_REGULATIONS = 2;

    /**
     * Report type - Allegedly infringing.
     */
    public static final int REPORT_TYPE_C_ALLEGEDLY_INFRINGING = 3;

    /**
     * Report type - Personal attacks.
     */
    public static final int REPORT_TYPE_C_PERSONAL_ATTACKS = 4;

    /**
     * Report type - Posing account.
     */
    public static final int REPORT_TYPE_C_POSING_ACCOUNT = 5;

    /**
     * Report type - Spam AD account.
     */
    public static final int REPORT_TYPE_C_SPAM_AD_ACCOUNT = 6;

    /**
     * Report type - Personal Information Violation.
     */
    public static final int REPORT_TYPE_C_PERSONAL_INFO_VIOLATION = 7;

    /**
     * Report type - Other.
     */
    public static final int REPORT_TYPE_C_OTHER = 49;

    // Report handled constants
    /**
     * Report handled - not yet.
     */
    public static final int REPORT_HANDLED_C_NOT = 0;

    /**
     * Report handled - yes.
     */
    public static final int REPORT_HANDLED_C_YES = 1;

    /**
     * Report handled - ignored.
     */
    public static final int REPORT_HANDLED_C_IGNORED = 2;

    //// Transient ////
    /**
     * Key of report user name.
     */
    public static final String REPORT_T_USERNAME = "reportUserName";

    /**
     * Key of report data.
     */
    public static final String REPORT_T_DATA = "reportData";

    /**
     * Key of report data type display string.
     */
    public static final String REPORT_T_DATA_TYPE_STR = "reportDataTypeStr";

    /**
     * Key of report type display string.
     */
    public static final String REPORT_T_TYPE_STR = "reportTypeStr";

    /**
     * Key of report time.
     */
    public static final String REPORT_T_TIME = "reportTime";

    /**
     * Private constructor.
     */
    private Report() {
    }
}
