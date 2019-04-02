/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.AbstractServletListener;

import javax.servlet.ServletContext;
import java.util.TimeZone;

/**
 * Templates utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Jan 17, 2018
 * @since 1.3.0
 */
public final class Templates {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Templates.class);

    /**
     * FreeMarker template configurations.
     */
    private static final Configuration TEMPLATES;

    /**
     * Freemarker version.
     */
    public static final Version FREEMARKER_VER = Configuration.VERSION_2_3_28;

    static {
        final ServletContext servletContext = AbstractServletListener.getServletContext();
        TEMPLATES = new Configuration(FREEMARKER_VER);
        TEMPLATES.setDefaultEncoding("UTF-8");
        TEMPLATES.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        TEMPLATES.setServletContextForTemplateLoading(servletContext, "skins");
        TEMPLATES.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        TEMPLATES.setLogTemplateExceptions(false);
    }

    /**
     * Gets a template specified by the given name.
     *
     * @param name the given name
     * @return template
     * @throws Exception exception
     */
    public static Template getTemplate(final String name) throws Exception {
        return TEMPLATES.getTemplate(name);
    }

    /**
     * Private constructor.
     */
    private Templates() {
    }
}
