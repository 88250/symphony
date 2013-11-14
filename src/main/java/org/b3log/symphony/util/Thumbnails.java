/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
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


import org.b3log.latke.util.MD5;


/**
 * Thumbnail utilities.
 * 
 * <p>
 * By using <a href="http://gravatar.com">Gravatar</a> for user thumbnail.
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 14, 2013
 * @since 0.2.5
 */
public final class Thumbnails {

    /**
     * Gravatar address.
     */
    public static final String GRAVATAR = "http://secure.gravatar.com/avatar/";

    /**
     * Gets the Gravatar URL for the specified email with the specified size.
     * 
     * @param email the specified email
     * @param size the specified size
     * @return the Gravatar URL
     */
    public static String getGravatarURL(final String email, final String size) {
        return Thumbnails.GRAVATAR + MD5.hash(email) + "?s=" + size + "&d=" + Symphonys.get("defaultThumbnailURL");
    }

    /**
     * Private constructor.
     */
    private Thumbnails() {}
}
