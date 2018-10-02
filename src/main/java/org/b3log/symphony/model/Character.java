/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.model;

import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;

/**
 * This class defines all character model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.0, Jul 8, 2016
 * @since 1.4.0
 */
public final class Character {

    /**
     * Character.
     */
    public static final String CHARACTER = "character";

    /**
     * Characters.
     */
    public static final String CHARACTERS = "characters";

    /**
     * Key of character user id.
     */
    public static final String CHARACTER_USER_ID = "characterUserId";

    /**
     * Key of character image.
     */
    public static final String CHARACTER_IMG = "characterImg";

    /**
     * Key of character content.
     */
    public static final String CHARACTER_CONTENT = "characterContent";

    /**
     * Character font.
     */
    private static final Font FONT = new Font("宋体", Font.PLAIN, 40);

    /**
     * Gets a character by the specified character content in the specified characters.
     *
     * @param content    the specified character content
     * @param characters the specified characters
     * @return character, returns {@code null} if not found
     */
    public static JSONObject getCharacter(final String content, final Set<JSONObject> characters) {
        for (final JSONObject character : characters) {
            if (character.optString(CHARACTER_CONTENT).equals(content)) {
                return character;
            }
        }

        return null;
    }

    /**
     * Creates an image with the specified content (a character).
     *
     * @param content the specified content
     * @return image
     */
    public static BufferedImage createImage(final String content) {
        final BufferedImage ret = new BufferedImage(500, 500, Transparency.TRANSLUCENT);
        final Graphics g = ret.getGraphics();
        g.setClip(0, 0, 50, 50);
        g.fillRect(0, 0, 50, 50);
        g.setFont(new Font(null, Font.PLAIN, 40));
        g.setColor(Color.BLACK);
        g.drawString(content, 5, 40);
        g.dispose();

        return ret;
    }
}
