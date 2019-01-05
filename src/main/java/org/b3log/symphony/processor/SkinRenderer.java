/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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
package org.b3log.symphony.processor;

import freemarker.template.Template;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Locales;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.util.Skins;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.TimeZone;

/**
 * Skin user-switchable FreeMarker Renderer.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.0.6, Jan 5, 2019
 * @since 1.3.0
 */
public final class SkinRenderer extends AbstractFreeMarkerRenderer {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SkinRenderer.class);

    /**
     * HTTP servlet request context.
     */
    private final RequestContext context;

    /**
     * Constructs a skin renderer with the specified request context and template name.
     *
     * @param context      the specified request context
     * @param templateName the specified template name
     */
    public SkinRenderer(final RequestContext context, final String templateName) {
        this.context = context;
        this.context.setRenderer(this);
        setTemplateName(templateName);
    }

    /**
     * Gets a template with the specified search engine bot flag and user.
     *
     * @param isSearchEngineBot the specified search engine bot flag
     * @param user              the specified user
     * @return template
     */
    public Template getTemplate(final boolean isSearchEngineBot, final JSONObject user) {
        String templateDirName = (String) context.attr(Keys.TEMAPLTE_DIR_NAME);
        final String templateName = getTemplateName();
        try {
            Template ret;
            try {
                ret = Skins.SKIN.getTemplate(templateDirName + "/" + templateName);
            } catch (final Exception e) {
                if (Symphonys.get("skinDirName").equals(templateDirName) ||
                        Symphonys.get("mobileSkinDirName").equals(templateDirName)) {
                    throw e;
                }

                // Try to load default template
                ret = Skins.SKIN.getTemplate(Symphonys.get("skinDirName") + "/" + templateName);
            }

            if (isSearchEngineBot) {
                return ret;
            }

            ret.setLocale(Locales.getLocale());

            if (null != user) {
                ret.setTimeZone(TimeZone.getTimeZone(user.optString(UserExt.USER_TIMEZONE)));
            } else {
                ret.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
            }

            return ret;
        } catch (final IOException e) {
            LOGGER.log(Level.ERROR, "Get template [dir=" + templateDirName + ", name=" + templateName + "] error", e);

            return null;
        }
    }

    @Override
    protected Template getTemplate() {
        final boolean isSearchEngineBot = (Boolean) context.attr(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT);
        final JSONObject user = (JSONObject) context.attr(Common.CURRENT_USER);

        return getTemplate(isSearchEngineBot, user);
    }

    /**
     * Processes the specified FreeMarker template with the specified request, data model, pjax hacking.
     *
     * @param request   the specified request
     * @param dataModel the specified data model
     * @param template  the specified FreeMarker template
     * @return generated HTML
     * @throws Exception exception
     */
    protected String genHTML(final HttpServletRequest request, final Map<String, Object> dataModel, final Template template)
            throws Exception {
        final boolean isPJAX = isPJAX(context);
        dataModel.put("pjax", isPJAX);
        if (!isPJAX) {
            return super.genHTML(request, dataModel, template);
        }

        final StringWriter stringWriter = new StringWriter();
        template.setOutputEncoding("UTF-8");
        template.process(dataModel, stringWriter);
        final long endTimeMillis = System.currentTimeMillis();
        final String dateString = DateFormatUtils.format(endTimeMillis, "yyyy/MM/dd HH:mm:ss");
        final long startTimeMillis = (Long) context.attr(Keys.HttpRequest.START_TIME_MILLIS);
        final String msg = String.format("\n<!-- Generated by Latke (https://github.com/b3log/latke) in %1$dms, %2$s -->", endTimeMillis - startTimeMillis, dateString);
        final String pjaxContainer = context.header("X-PJAX-Container");

        return StringUtils.substringBetween(stringWriter.toString(),
                "<!---- pjax {" + pjaxContainer + "} start ---->",
                "<!---- pjax {" + pjaxContainer + "} end ---->") + msg;
    }

    @Override
    protected void beforeRender(final RequestContext context) {
    }

    @Override
    protected void afterRender(final RequestContext context) {
    }

    /**
     * Determines whether the specified request is sending with pjax.
     *
     * @param context the specified request context
     * @return {@code true} if it is sending with pjax, otherwise returns {@code false}
     */
    private static boolean isPJAX(final RequestContext context) {
        final boolean pjax = Boolean.valueOf(context.header("X-PJAX"));
        final String pjaxContainer = context.header("X-PJAX-Container");

        return pjax && StringUtils.isNotBlank(pjaxContainer);
    }
}
