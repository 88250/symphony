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

import java.util.ResourceBundle;
import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeMode;

/**
 * Symphony utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.0.3, Mar 22, 2016
 * @since 0.1.0
 */
public final class Symphonys {

    /**
     * Configurations.
     */
    private static final ResourceBundle CFG = ResourceBundle.getBundle("symphony");

    /**
     * Reserved tags.
     */
    public static final String[] RESERVED_TAGS;

    /**
     * Reserved user names.
     */
    public static final String[] RESERVED_USER_NAMES;

    static {
        final String reservedTags = CFG.getString("reservedTags");
        final String[] tags = reservedTags.split(",");
        RESERVED_TAGS = new String[tags.length];

        for (int i = 0; i < tags.length; i++) {
            final String tag = tags[i];

            RESERVED_TAGS[i] = tag.trim();
        }

        final String reservedUserNames = CFG.getString("reservedUserNames");
        final String[] userNames = reservedUserNames.split(",");
        RESERVED_USER_NAMES = new String[userNames.length];

        for (int i = 0; i < userNames.length; i++) {
            final String userName = userNames[i];

            RESERVED_USER_NAMES[i] = userName.trim();
        }
    }

    /**
     * Does Symphony runs on development environment?
     *
     * @return {@code true} if it runs on development environment, {@code false} otherwise
     */
    public static boolean runsOnDevEnv() {
        return RuntimeMode.DEVELOPMENT == Latkes.getRuntimeMode();
    }

    /**
     * Gets a configuration string property with the specified key.
     *
     * @param key the specified key
     * @return string property value corresponding to the specified key, returns {@code null} if not found
     */
    public static String get(final String key) {
        return CFG.getString(key);
    }

    /**
     * Gets a configuration boolean property with the specified key.
     *
     * @param key the specified key
     * @return boolean property value corresponding to the specified key, returns {@code null} if not found
     */
    public static Boolean getBoolean(final String key) {
        final String stringValue = get(key);

        if (null == stringValue) {
            return null;
        }

        return Boolean.valueOf(stringValue);
    }

    /**
     * Gets a configuration float property with the specified key.
     *
     * @param key the specified key
     * @return float property value corresponding to the specified key, returns {@code null} if not found
     */
    public static Float getFloat(final String key) {
        final String stringValue = get(key);
        if (null == stringValue) {
            return null;
        }

        return Float.valueOf(stringValue);
    }

    /**
     * Gets a configuration integer property with the specified key.
     *
     * @param key the specified key
     * @return integer property value corresponding to the specified key, returns {@code null} if not found
     */
    public static Integer getInt(final String key) {
        final String stringValue = get(key);
        if (null == stringValue) {
            return null;
        }

        return Integer.valueOf(stringValue);
    }

    /**
     * Gets a configuration long property with the specified key.
     *
     * @param key the specified key
     * @return long property value corresponding to the specified key, returns {@code null} if not found
     */
    public static Long getLong(final String key) {
        final String stringValue = get(key);
        if (null == stringValue) {
            return null;
        }

        return Long.valueOf(stringValue);
    }

    /**
     * Private default constructor.
     */
    private Symphonys() {
    }
}
