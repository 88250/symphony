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
package org.b3log.symphony.processor;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Locales;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.util.Skins;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.TimeZone;

/**
 * Skin user-switchable FreeMarker Renderer.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.0, Dec 10, 2016
 * @since 1.3.0
 */
public final class SkinRenderer extends AbstractFreeMarkerRenderer {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SkinRenderer.class.getName());

    /**
     * HTTP servlet request.
     */
    private final HttpServletRequest request;

    /**
     * Constructs a skin renderer with the specified HTTP servlet request.
     *
     * @param request the specified HTTP servlet request
     */
    public SkinRenderer(final HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Gets a templat with the specified template directory name, template name, search engine bot flag and user.
     *
     * @param templateDirName  the specified template directory name
     * @param templateName     the specified template name
     * @param isSearchEnginBot the specified search engine bot flag
     * @param user             the specified user
     * @return
     */
    public static Template getTemplate(final String templateDirName, final String templateName,
                                       final boolean isSearchEnginBot, final JSONObject user) {
        Configuration cfg = Skins.TEMPLATE_HOLDER.get(templateDirName);

        try {
            if (null == cfg) {
                LOGGER.warn("Can't get template dir [" + templateDirName + "]");

                cfg = Skins.TEMPLATE_HOLDER.get(Symphonys.get("skinDirName"));
            }

            final Template ret = cfg.getTemplate(templateName);

            if (isSearchEnginBot) {
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

    /**
     * Gets a template with the specified template directory name and template name.
     *
     * @param templateDirName the specified template directory name
     * @param templateName    the specified template name
     * @return template
     */
    @Override
    protected Template getTemplate(final String templateDirName, final String templateName) {
        final boolean isSearchEngineBot = (Boolean) request.getAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT);
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        return getTemplate(templateDirName, templateName, isSearchEngineBot, user);
    }

    @Override
    protected void beforeRender(final HTTPRequestContext context) throws Exception {
    }

    @Override
    protected void afterRender(final HTTPRequestContext context) throws Exception {
    }
}
