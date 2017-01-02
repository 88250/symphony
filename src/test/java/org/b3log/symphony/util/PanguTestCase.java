/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <a href="https://github.com/vinta/pangu.java">Pangu</a> utilities test case.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 31, 2016
 * @since 1.6.0
 */
public class PanguTestCase {

    @Test
    public void test() {
        final String text = Pangu.spacingText("Sym是一个用Java写的实时论坛，欢迎来体验！");

        Assert.assertEquals(text, "Sym 是一个用 Java 写的实时论坛，欢迎来体验！");
    }
}
