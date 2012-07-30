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
package org.b3log.symphony.util;

import java.util.Date;
import java.util.TimeZone;
import org.b3log.latke.util.freemarker.Templates;

/**
 * Time zone utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Jul 30, 2012
 * @since 0.2.0
 */
public final class TimeZones {

    /**
     * Gets the current date with the specified time zone id.
     *
     * @param timeZoneId the specified time zone id
     * @return date
     */
    public static Date getTime(final String timeZoneId) {
        final TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
        final TimeZone defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(timeZone);
        final Date ret = new Date();
        TimeZone.setDefault(defaultTimeZone);

        return ret;
    }

    /**
     * Sets time zone by the specified time zone id.
     *
     * <p>
     * This method will call {@linkplain TimeZone#setDefault(java.util.TimeZone)},
     * and set time zone for all date formats and template configuration.
     * </p>
     *
     * @param timeZoneId the specified time zone id
     */
    public static void setTimeZone(final String timeZoneId) {
        final TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);

        TimeZone.setDefault(timeZone);
        System.setProperty("user.timezone", timeZoneId);
        Templates.MAIN_CFG.setTimeZone(timeZone);
    }
}
