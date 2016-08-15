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
 * This class defines all solo model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Nov 19, 2012
 * @since 0.2.0
 */
public final class Emotion {

	public static final String EmotionId="oId";
	public static final String EmotionUser = "user_oId";
    public static final String EmotionContent = "emotionContent";
    public static final String EmotionSort = "emotionSort";
    public static final String EmotionType = "emotionType";
    
    public static final String EmotionList = "emotionList";
    private Emotion() {
    }
}
