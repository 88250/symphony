/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
 *
 * 本文件属于 Sym 商业版的一部分，请仔细阅读项目根文件夹的 LICENSE 并严格遵守相关约定
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
