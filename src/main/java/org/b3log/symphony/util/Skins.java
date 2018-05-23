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

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import org.b3log.latke.servlet.AbstractServletListener;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Skin utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.1, May 14, 2018
 * @since 1.3.0
 */
public final class Skins {

    /**
     * FreeMarker template configurations holder.
     * <p>
     * &lt;skinDirName, Configuration&gt;
     * </p>
     */
    public static final Map<String, Configuration> TEMPLATE_HOLDER = new HashMap<>();

    /**
     * Freemarker version.
     */
    public static final Version FREEMARKER_VER = Configuration.VERSION_2_3_28;

    static {
        final ServletContext servletContext = AbstractServletListener.getServletContext();
        final String skinsPath = servletContext.getRealPath("skins");
        final File skinsDir = new File(skinsPath);
        final String[] skinNames = skinsDir.list();

        for (final String skinName : skinNames) {
            final Configuration cfg = new Configuration(FREEMARKER_VER);
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
