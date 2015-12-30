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
package org.b3log.symphony.processor;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.IOException;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.symphony.util.Skins;
import org.b3log.symphony.util.Symphonys;

/**
 * Skin user-switchable FreeMarker Renderer.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 4, 2015
 * @since 1.3.0
 */
public final class SkinRenderer extends AbstractFreeMarkerRenderer {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SkinRenderer.class.getName());

    /**
     * Gets a template with the specified template directory name and template name.
     *
     * @param templateDirName the specified template directory name
     * @param templateName the specified template name
     * @return template
     */
    @Override
    protected Template getTemplate(final String templateDirName, final String templateName) {
        final Configuration cfg = Skins.TEMPLATE_HOLDER.get(templateDirName);

        try {
            if (null == cfg) {
                LOGGER.warn("Can't get template dir [" + templateDirName + "]");

                return Skins.TEMPLATE_HOLDER.get(Symphonys.get("skinDirName")).getTemplate(templateName);
            }

            return Skins.TEMPLATE_HOLDER.get(templateDirName).getTemplate(templateName);
        } catch (final IOException e) {
            LOGGER.log(Level.ERROR, "Get template [dir=" + templateDirName + ", name=" + templateName + "] error", e);

            return null;
        }
    }

    @Override
    protected void beforeRender(final HTTPRequestContext context) throws Exception {
    }

    @Override
    protected void afterRender(final HTTPRequestContext context) throws Exception {
    }

}
