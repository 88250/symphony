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
