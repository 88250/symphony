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
