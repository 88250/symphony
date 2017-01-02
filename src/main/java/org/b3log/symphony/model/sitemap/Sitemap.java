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
package org.b3log.symphony.model.sitemap;

import java.util.ArrayList;
import java.util.List;
import org.b3log.latke.util.Strings;

/**
 * Sitemap.
 *
 * <p>
 * See <a href="http://www.sitemaps.org/protocol.php">Sitemap XML format</a>
 * for more details.
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.0, Nov 15, 2016
 * @since 1.6.0
 */
public final class Sitemap {

    /**
     * Start document.
     */
    private static final String START_DOCUMENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /**
     * Start URL set element.
     */
    private static final String START_URL_SET_ELEMENT = "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">";

    /**
     * End URL set element.
     */
    private static final String END_URL_SET_ELEMENT = "</urlset>";

    /**
     * URLs.
     */
    private final List<URL> urls = new ArrayList<>();

    /**
     * Adds the specified url.
     *
     * @param url the specified url
     */
    public void addURL(final URL url) {
        urls.add(url);
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(START_DOCUMENT);
        stringBuilder.append(START_URL_SET_ELEMENT);

        for (final URL url : urls) {
            stringBuilder.append(url.toString());
        }

        stringBuilder.append(END_URL_SET_ELEMENT);

        return stringBuilder.toString();
    }

    /**
     * Sitemap URL.
     *
     * @author <a href="http://88250.b3log.org">Liang Ding</a>
     * @version 1.0.0.0, Sep 24, 2016
     * @since 1.6.0
     */
    public static final class URL {

        /**
         * Start URL element.
         */
        private static final String START_URL_ELEMENT = "<url>";

        /**
         * End URL element.
         */
        private static final String END_URL_ELEMENT = "</url>";

        /**
         * Start location element.
         */
        private static final String START_LOC_ELEMENT = "<loc>";

        /**
         * End location element.
         */
        private static final String END_LOC_ELEMENT = "</loc>";

        /**
         * Start last modified element.
         */
        private static final String START_LAST_MOD_ELEMENT = "<lastmod>";

        /**
         * End last modified element.
         */
        private static final String END_LAST_MOD_ELEMENT = "</lastmod>";

        /**
         * Start change frequency element.
         */
        private static final String START_CHANGE_REQ_ELEMENT = "<changefreq>";

        /**
         * End change frequency element.
         */
        private static final String END_CHANGE_REQ_ELEMENT = "</changefreq>";

        /**
         * Start priority element.
         */
        private static final String START_PRIORITY_ELEMENT = "<priority>";

        /**
         * End priority element.
         */
        private static final String END_PRIORITY_ELEMENT = "</priority>";

        /**
         * Location.
         */
        private String loc;

        /**
         * Last modified.
         */
        private String lastMod;

        /**
         * Change frequency.
         */
        private String changeFreq;

        /**
         * Priority.
         */
        private String priority;

        /**
         * Gets the last modified.
         *
         * @return last modified
         */
        public String getLastMod() {
            return lastMod;
        }

        /**
         * Sets the last modified with the specified last modified.
         *
         * @param lastMod the specified modified
         */
        public void setLastMod(final String lastMod) {
            this.lastMod = lastMod;
        }

        /**
         * Gets the location.
         *
         * @return location
         */
        public String getLoc() {
            return loc;
        }

        /**
         * Sets the location with the specified location.
         *
         * @param loc the specified location
         */
        public void setLoc(final String loc) {
            this.loc = loc;
        }

        /**
         * Gets the change frequency.
         *
         * @return change frequency
         */
        public String getChangeFreq() {
            return changeFreq;
        }

        /**
         * Sets the change frequency with the specified change frequency.
         *
         * @param changeFreq the specified change frequency
         */
        public void setChangeFreq(final String changeFreq) {
            this.changeFreq = changeFreq;
        }

        /**
         * Gets the priority.
         *
         * @return priority
         */
        public String getPriority() {
            return priority;
        }

        /**
         * Sets the priority with the specified priority.
         *
         * @param priority the specified priority
         */
        public void setPriority(final String priority) {
            this.priority = priority;
        }

        @Override
        public String toString() {
            final StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(START_URL_ELEMENT);

            stringBuilder.append(START_LOC_ELEMENT);
            stringBuilder.append(loc);
            stringBuilder.append(END_LOC_ELEMENT);

            if (!Strings.isEmptyOrNull(lastMod)) {
                stringBuilder.append(START_LAST_MOD_ELEMENT);
                stringBuilder.append(lastMod);
                stringBuilder.append(END_LAST_MOD_ELEMENT);
            }

            if (!Strings.isEmptyOrNull(changeFreq)) {
                stringBuilder.append(START_CHANGE_REQ_ELEMENT);
                stringBuilder.append(changeFreq);
                stringBuilder.append(END_CHANGE_REQ_ELEMENT);
            }

            if (!Strings.isEmptyOrNull(priority)) {
                stringBuilder.append(START_PRIORITY_ELEMENT);
                stringBuilder.append(priority);
                stringBuilder.append(END_PRIORITY_ELEMENT);
            }

            stringBuilder.append(END_URL_ELEMENT);

            return stringBuilder.toString();
        }
    }
}
