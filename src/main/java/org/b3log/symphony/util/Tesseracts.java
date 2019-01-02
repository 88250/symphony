/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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
package org.b3log.symphony.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.util.Execs;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Tesseract-OCR utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Nov 28, 2018
 * @since 1.4.0
 */
public final class Tesseracts {

    /**
     * Recognizes a single character from the specified image file path.
     *
     * @param imagePath the specified image file path
     * @return the recognized character
     */
    public static String recognizeCharacter(final String imagePath) {
        Execs.exec("tesseract " + imagePath + " " + imagePath + " -l chi_sim -psm 10", 1000 * 10);

        try {
            return StringUtils.trim(IOUtils.toString(new FileInputStream(imagePath + ".txt"), "UTF-8"));
        } catch (final IOException e) {
            return "";
        }
    }

    /**
     * Private constructor.
     */
    private Tesseracts() {
    }
}
