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
package org.b3log.symphony.model;

/**
 * This class defines all emotion model relevant keys.
 *
 * @author Zephyr
 * @version 1.0.0.0, Aug 16, 2016
 * @since 1.5.0
 */
public final class Emotion {

    /**
     * Emotion.
     */
    public static final String EMOTION = "emotion";

    /**
     * Emotions.
     */
    public static final String EMOTIONS = "emotions";

    /**
     * Key of emotion user id.
     */
    public static final String EMOTION_USER_ID = "emotionUserId";

    /**
     * Key of emotion content.
     */
    public static final String EMOTION_CONTENT = "emotionContent";

    /**
     * Key of emotion sort.
     */
    public static final String EMOTION_SORT = "emotionSort";

    /**
     * Key of emotion type.
     */
    public static final String EMOTION_TYPE = "emotionType";

    // Type constants
    /**
     * Emotion type - Emoji.
     */
    public static final int EMOTION_TYPE_C_EMOJI = 0;

    private Emotion() {
    }
}
