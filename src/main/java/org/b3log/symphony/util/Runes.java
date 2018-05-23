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
package org.b3log.symphony.util;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rune utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 19, 2017
 * @since 2.1.0
 */
public final class Runes {


    private Runes() {
    }

//    public static void main(final String[] args) {
//        System.out.println(getChinesePercent("123abc这个中文cde123abc也要提取123ab"));
//    }

    /**
     * Gets chinese percentage of the specified string.
     *
     * @param str the specified string
     * @return percentage
     */
    public static int getChinesePercent(final String str) {
        if (StringUtils.isBlank(str)) {
            return 0;
        }

        final Pattern p = Pattern.compile("([\u4e00-\u9fa5]+)");
        final Matcher m = p.matcher(str);
        final StringBuilder chineseBuilder = new StringBuilder();
        while (m.find()) {
            chineseBuilder.append(m.group(0));
        }

        return (int) Math.floor(StringUtils.length(chineseBuilder.toString()) / (double) StringUtils.length(str) * 100);
    }
}
