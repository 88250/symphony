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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Templates utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.2, Jun 20, 2020
 * @since 1.3.0
 */
public final class Templates {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(Templates.class);

    /**
     * FreeMarker template configurations.
     */
    private static final Configuration TEMPLATE_CFG;

    /**
     * Freemarker version.
     */
    public static final Version FREEMARKER_VER = Configuration.VERSION_2_3_30;

    static {
        TEMPLATE_CFG = new Configuration(FREEMARKER_VER);
        TEMPLATE_CFG.setDefaultEncoding("UTF-8");
        try {
            String path = Templates.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            LOGGER.info("Code source path: " + path);
            LOGGER.info("Initial resource path: " + path); // 添加调试信息
            if (StringUtils.contains(path, "/target/classes/") || StringUtils.contains(path, "/target/test-classes/")) {
                // 开发时使用源码目录
                path = StringUtils.replace(path, "/target/classes/", "/src/main/resources/");
                path = StringUtils.replace(path, "/target/test-classes/", "/src/main/resources/");
                LOGGER.info("Adjusted resource path for development environment: " + path); // 添加调试信息
            }
            if (StringUtils.contains(path, "/target/symphony/")) {
                // 开发时使用源码目录
                path = StringUtils.replace(path, "/target/symphony/", "/src/main/resources/");
                LOGGER.info("Adjusted resource path for development environment: " + path); // 添加调试信息
            }
            path += "skins";
            TEMPLATE_CFG.setDirectoryForTemplateLoading(new File(path));
            LOGGER.log(Level.INFO, "Loaded template from directory [" + path + "]");
        } catch (final Exception e) {
            TEMPLATE_CFG.setClassForTemplateLoading(Templates.class, "/skins");
            LOGGER.error("Failed to load template from directory, loading from classpath", e); // 记录错误信息
        }
        TEMPLATE_CFG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        TEMPLATE_CFG.setLogTemplateExceptions(false);
    }

    /**
     * Gets a template specified by the given name.
     *
     * @param name the given name
     * @return template
     * @throws Exception exception
     */
    public static Template getTemplate(final String name) throws Exception {
        return TEMPLATE_CFG.getTemplate(name);
    }

    /**
     * Private constructor.
     */
    private Templates() {
    }
}
