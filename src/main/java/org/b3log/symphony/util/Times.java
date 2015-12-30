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
package org.b3log.symphony.util;

import java.util.Locale;
import java.util.Map;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.LatkeBeanManagerImpl;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.LangPropsServiceImpl;

/**
 * Time utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 1, 2015
 * @since 1.3.0
 */
public final class Times {

    /**
     * Minute unit.
     */
    private static final long MINUTE_UNIT = 60 * 1000;

    /**
     * Hour unit.
     */
    private static final long HOUR_UNIT = 60 * MINUTE_UNIT;

    /**
     * Day unit.
     */
    private static final long DAY_UNIT = 24 * HOUR_UNIT;

    /**
     * Week unit.
     */
    private static final long WEEK_UNIT = 7 * DAY_UNIT;

    /**
     * Month unit.
     */
    private static final long MONTH_UNIT = 31 * DAY_UNIT;

    /**
     * Year unit.
     */
    private static final long YEAR_UNIT = 12 * MONTH_UNIT;

    /**
     * Gets time ago format text.
     *
     * @param time the specified time.
     * @param locale the specified locale
     * @return time ago format text
     */
    public static String getTimeAgo(final long time, final Locale locale) {
        final LatkeBeanManager beanManager = LatkeBeanManagerImpl.getInstance();
        final LangPropsService langService = beanManager.getReference(LangPropsServiceImpl.class);
        final Map<String, String> langs = langService.getAll(locale);

        final long diff = System.currentTimeMillis() - time;
        long r = 0;

        if (diff > YEAR_UNIT) {
            r = diff / YEAR_UNIT;

            return r + " " + langs.get("yearsAgoLabel");
        }

        if (diff > MONTH_UNIT) {
            r = diff / MONTH_UNIT;

            return r + " " + langs.get("monthsAgoLabel");
        }

        if (diff > WEEK_UNIT) {
            r = diff / WEEK_UNIT;

            return r + " " + langs.get("weeksAgoLabel");
        }

        if (diff > DAY_UNIT) {
            r = diff / DAY_UNIT;

            return r + " " + langs.get("daysAgoLabel");
        }

        if (diff > HOUR_UNIT) {
            r = diff / HOUR_UNIT;

            return r + " " + langs.get("hoursAgoLabel");
        }

        if (diff > MINUTE_UNIT) {
            r = diff / MINUTE_UNIT;

            return r + " " + langs.get("minutesAgoLabel");
        }

        return langs.get("justNowLabel");
    }

    /**
     * Private constructor.
     */
    private Times() {
    }
}
