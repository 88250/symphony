/*
 * Copyright (c) 2012-2015, b3log.org
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

import org.b3log.latke.Latkes;

/**
 * Emotions utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 26, 2012
 * @since 0.2.0
 */
public final class Emotions {

    /**
     * Emotion count.
     */
    private static final int EMOTION_CNT = 15;

    /**
     * Ten.
     */
    private static final int TEN = 10;

    /**
     * Clears the emotions ({@literal [em00]}) with specified content.
     *
     * @param content the specified content
     * @return cleared content
     */
    public static String clear(final String content) {
        return content.replaceAll("\\[em\\d+]", "");
    }

    /**
     * Converts the specified content with {@literal [em00]} into content with {@literal <img src='em00.png'/>}.
     *
     * @param content the specified content
     * @return converted content
     */
    public static String convert(final String content) {
        String ret = content;

        String emotionName;
        for (int i = 0; i < EMOTION_CNT; i++) {
            if (i < TEN) {
                emotionName = "em0" + i;
            } else {
                emotionName = "em" + i;
            }

            ret = ret.replace('[' + emotionName + ']',
                              "<img src='" + Latkes.getStaticServePath() + "/images/emotions/ease/" + emotionName + ".png" + "' />");
        }

        return ret;
    }

    /**
     * Private constructor.
     */
    private Emotions() {
    }
}
