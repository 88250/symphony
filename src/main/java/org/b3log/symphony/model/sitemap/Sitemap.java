/*
 * Copyright (c) 2010-2016, b3log.org & hacpai.com
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
 * @version 1.0.0.0, Sep 24, 2016
 * @since 1.6.0
 */
public final class Sitemap {

    /**
     * Start document.
     */
    private static final String START_DOCUMENT = "<?xml version='1.0' encoding='UTF-8'?>";

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
    private List<URL> urls = new ArrayList<>();

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
         * Start loc element.
         */
        private static final String START_LOC_ELEMENT = "<loc>";

        /**
         * End loc element.
         */
        private static final String END_LOC_ELEMENT = "</loc>";

        /**
         * Start last mod element.
         */
        private static final String START_LAST_MOD_ELEMENT = "<lastmod>";

        /**
         * End last mod element.
         */
        private static final String END_LAST_MOD_ELEMENT = "</lastmod>";

        /**
         * Loc.
         */
        private String loc;

        /**
         * Last mod.
         */
        private String lastMod;

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
         * Gets the loc.
         *
         * @return loc
         */
        public String getLoc() {
            return loc;
        }

        /**
         * Sets the loc with the specified loc.
         *
         * @param loc the specified loc
         */
        public void setLoc(final String loc) {
            this.loc = loc;
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

            stringBuilder.append(END_URL_ELEMENT);

            return stringBuilder.toString();
        }
    }
}
