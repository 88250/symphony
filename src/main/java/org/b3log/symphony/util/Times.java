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
package org.b3log.symphony.util;

import java.util.Calendar;
import java.util.Date;
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
 * @version 1.1.0.0, Apr 21, 2016
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
        long r;

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
     * Determines whether the specified date1 is the same day with the specified date2.
     *
     * @param date1 the specified date1
     * @param date2 the specified date2
     * @return {@code true} if it is the same day, returns {@code false} otherwise
     */
    public static boolean isSameDay(final Date date1, final Date date2) {
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        final Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE);
    }

    /**
     * Determines whether the specified date1 is the same week with the specified date2.
     *
     * @param date1 the specified date1
     * @param date2 the specified date2
     * @return {@code true} if it is the same week, returns {@code false} otherwise
     */
    public static boolean isSameWeek(final Date date1, final Date date2) {
        final Calendar cal1 = Calendar.getInstance();
        cal1.setFirstDayOfWeek(Calendar.MONDAY);
        cal1.setTime(date1);

        final Calendar cal2 = Calendar.getInstance();
        cal2.setFirstDayOfWeek(Calendar.MONDAY);
        cal2.setTime(date2);

        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Determines whether the specified date1 is the same month with the specified date2.
     *
     * @param date1 the specified date1
     * @param date2 the specified date2
     * @return {@code true} if it is the same month, returns {@code false} otherwise
     */
    public static boolean isSameMonth(final Date date1, final Date date2) {
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        final Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }

    /**
     * Gets the day start time with the specified time.
     *
     * @param time the specified time
     * @return day start time
     */
    public static long getDayStartTime(final long time) {
        final Calendar start = Calendar.getInstance();

        start.setTimeInMillis(time);
        int year = start.get(Calendar.YEAR);
        int month = start.get(Calendar.MONTH);
        int day = start.get(Calendar.DATE);
        start.set(year, month, day, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);

        return start.getTime().getTime();
    }

    /**
     * Gets the day end time with the specified time.
     *
     * @param time the specified time
     * @return day end time
     */
    public static long getDayEndTime(final long time) {
        final Calendar end = Calendar.getInstance();

        end.setTimeInMillis(time);
        int year = end.get(Calendar.YEAR);
        int month = end.get(Calendar.MONTH);
        int day = end.get(Calendar.DATE);
        end.set(year, month, day, 23, 59, 59);
        end.set(Calendar.MILLISECOND, 999);

        return end.getTime().getTime();
    }

    /**
     * Gets the week day with the specified time.
     *
     * @param time the specified time
     * @return week day
     */
    public static int getWeekDay(final long time) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int ret = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (ret <= 0) {
            ret = 7;
        }

        return ret;
    }

    /**
     * Gets the week start time with the specified time.
     *
     * @param time the specified time
     * @return week start time
     */
    public static long getWeekStartTime(final long time) {
        final Calendar start = Calendar.getInstance();

        start.setFirstDayOfWeek(Calendar.MONDAY);

        start.setTimeInMillis(time);
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        start.set(Calendar.HOUR, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        return start.getTime().getTime();
    }

    /**
     * Gets the week end time with the specified time.
     *
     * @param time the specified time
     * @return week end time
     */
    public static long getWeekEndTime(final long time) {
        final Calendar end = Calendar.getInstance();

        end.setFirstDayOfWeek(Calendar.MONDAY);

        end.setTimeInMillis(time);
        end.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        end.set(Calendar.HOUR, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        return end.getTime().getTime();
    }

    /**
     * Gets the month start time with the specified time.
     *
     * @param time the specified time
     * @return month start time
     */
    public static long getMonthStartTime(final long time) {
        final Calendar start = Calendar.getInstance();

        start.setTimeInMillis(time);
        int year = start.get(Calendar.YEAR);
        int month = start.get(Calendar.MONTH);
        start.set(year, month, 1, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);

        return start.getTime().getTime();
    }

    /**
     * Gets the month end time with the specified time.
     *
     * @param time the specified time
     * @return month end time
     */
    public static long getMonthEndTime(final long time) {
        final Calendar end = Calendar.getInstance();

        end.setTimeInMillis(time);
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
        end.set(Calendar.HOUR, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        return end.getTime().getTime();
    }

    /**
     * Private constructor.
     */
    private Times() {
    }
}
