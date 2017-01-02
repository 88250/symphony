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

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
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
 * @version 1.1.0.0, Sep 15, 2016
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
     * &lt;skinDirName, Configuration&gt;
     * </p>
     */
    public static final Map<String, Configuration> TEMPLATE_HOLDER = new HashMap<>();

    static {
        final ServletContext servletContext = AbstractServletListener.getServletContext();
        final String skinsPath = servletContext.getRealPath("skins");
        final File skinsDir = new File(skinsPath);
        final String[] skinNames = skinsDir.list();

        for (final String skinName : skinNames) {
            final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
            TEMPLATE_HOLDER.put(skinName, cfg);

            cfg.setDefaultEncoding("UTF-8");
            cfg.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            cfg.setServletContextForTemplateLoading(servletContext, "skins/" + skinName);
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
        }
    }

    /**
     * Private constructor.
     */
    private Skins() {
    }
}
