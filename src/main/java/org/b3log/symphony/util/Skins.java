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

import freemarker.template.Configuration;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.AbstractServletListener;

/**
 * Skin utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 4, 2015
 * @since 1.3.0
 */
public final class Skins {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Skins.class.getName());

    /**
     * FreeMarker template configurations holder.
     *
     * <p>
     * &lt;skinDirName, Configuration&gt;</p>
     */
    public static final Map<String, Configuration> TEMPLATE_HOLDER = new HashMap<String, Configuration>();

    static {
        final ServletContext servletContext = AbstractServletListener.getServletContext();
        final String skinsPath = servletContext.getRealPath("skins");
        final File skinsDir = new File(skinsPath);
        final String[] skinNames = skinsDir.list();

        for (final String skinName : skinNames) {
            final Configuration cfg = new Configuration();
            TEMPLATE_HOLDER.put(skinName, cfg);

            cfg.setDefaultEncoding("UTF-8");
            cfg.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            cfg.setServletContextForTemplateLoading(servletContext, "skins/" + skinName);
        }
    }

    /**
     * Private constructor.
     */
    private Skins() {
    }
}
