/*
 * Copyright (c) 2012, B3log Team
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
 * This class defines all statistic model relevant keys.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Oct 6, 2012
 * @since 0.2.0
 */
public final class Statistic {

    /**
     * Statistic.
     */
    public static final String STATISTIC = "statistic";
    /**
     * Key of member count.
     */
    public static final String STATISTIC_MEMBER_COUNT = "statisticMemberCount";
    /**
     * Key of article count.
     */
    public static final String STATISTIC_ARTICLE_COUNT = "statisticArticleCount";
    /**
     * Key of tag count.
     */
    public static final String STATISTIC_TAG_COUNT = "statisticTagCount";
    /**
     * Key of comment count.
     */
    public static final String STATISTIC_CMT_COUNT = "statisticCmtCount";

    /**
     * Private default constructor.
     */
    private Statistic() {
    }
}
