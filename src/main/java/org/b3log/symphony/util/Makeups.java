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

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang.StringUtils;

/**
 * Makeup utilities.
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Nov 26, 2012
 * @since 0.2.0
 */
public final class Makeups {

    /**
     * Makeups links for the specified content.
     * 
     * <p>
     * The string in the specified content like {@literal "http://b3log.org"} will be convert to
     * {@literal "<a href='http://b3log.org' target='_blank'>http://b3log.org</a>"}.
     * </p>
     * 
     * @param scheme the specified scheme, for example "http"
     * @param content the specified content
     * @return content
     */
    public static String link(final String scheme, final String content) {
        String ret = content;
        final String[] urls = StringUtils.substringsBetween(ret, scheme + "://", " ");
        
        if (null == urls) {
            return ret;
        }

        for (int i = 0; i < urls.length; i++) {
            final String url = scheme + "://" + urls[i];

            try {
                new URL(url);
                ret = ret.replace(url, "<a href='" + url + "' target='_blank'>" + url + "</a>");
            } catch (final MalformedURLException e) {
                e.getMessage();
                // It's not a valid URL, do not makeup it
            }
        }

        return ret;
    }

    /**
     * Private constructor.
     */
    private Makeups() {
    }
}
