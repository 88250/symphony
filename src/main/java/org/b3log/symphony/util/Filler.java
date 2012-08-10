/*
 * Copyright (c) 2012, B3log Team
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

import java.util.Map;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.symphony.model.Common;

/**
 * Filler utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 10, 2012
 * @since 0.2.0
 */
public final class Filler {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Filler.class.getName());

    /**
     * Fills header.ftl.
     * 
     * @param datModel the specified data model
     */
    public static void fillHeader(final Map<String, Object> datModel) {
        fillMinified(datModel);
        Keys.fillServer(datModel);
    }

    /**
     * Fills minified directory and file postfix for static JavaScript, CSS.
     * 
     * @param dataModel the specified data model
     */
    private static void fillMinified(final Map<String, Object> dataModel) {
        switch (Latkes.getRuntimeMode()) {
            case DEVELOPMENT:
                dataModel.put(Common.MINI_POSTFIX, "");
                break;
            case PRODUCTION:
                dataModel.put(Common.MINI_POSTFIX, Common.MINI_POSTFIX_VALUE);
                break;
            default:
                throw new AssertionError();
        }
    }
}
