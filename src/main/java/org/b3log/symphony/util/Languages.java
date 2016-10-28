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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Languages utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Oct 26, 2016
 * @since 1.7.0
 */
public final class Languages {

    /**
     * Available languages.
     */
    private static final List<String> LANGUAGES = Arrays.asList(
            Locale.US.toString(),
            Locale.SIMPLIFIED_CHINESE.toString()
    );

    /**
     * Gets available languages.
     *
     * @return languages
     */
    public static List<String> getAvailableLanguages() {
        return LANGUAGES;
    }

    /**
     * Private constructor.
     */
    private Languages() {
    }
}
