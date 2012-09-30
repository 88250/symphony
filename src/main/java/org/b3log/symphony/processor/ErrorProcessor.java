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
package org.b3log.symphony.processor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.user.UserService;
import org.b3log.latke.user.UserServiceFactory;
import org.b3log.latke.util.Locales;
import org.b3log.symphony.SymphonyServletListener;
import org.b3log.symphony.util.Filler;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Error processor.
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 16, 2012
 * @since 0.2.0
 */
@RequestProcessor
public final class ErrorProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ErrorProcessor.class.getName());
    /**
     * Language service.
     */
    private LangPropsService langPropsService = LangPropsService.getInstance();
    /**
     * User service.
     */
    private static UserService userService = UserServiceFactory.getUserService();

    /**
     * Shows the user template page.
     * 
     * @param context the specified context
     * @param request the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @throws IOException io exception 
     */
    @RequestProcessing(value = "/error/*", method = HTTPRequestMethod.GET)
    public void showErrorPage(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        final String requestURI = request.getRequestURI();
        final String templateName = requestURI.substring("/error/".length()) + ".ftl";
        LOGGER.log(Level.FINE, "Shows error page[requestURI={0}, templateName={1}]", new Object[]{requestURI, templateName});

        final ErrorRenderer renderer = new ErrorRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName(templateName);

        final Map<String, Object> dataModel = renderer.getDataModel();

        try {
            final Map<String, String> langs = langPropsService.getAll(Locales.getLocale(request));
            dataModel.putAll(langs);
            
            Filler.fillHeader(request, response, dataModel);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);

            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (final IOException ex) {
                LOGGER.severe(ex.getMessage());
            }
        }
    }

    /**
     * <a href="http://freemarker.org">FreeMarker</a> HTTP response 
     * renderer for error page rendering.
     *
     * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
     * @version 1.0.0.0, Aug 16, 2012
     * @since 0.2.0
     */
    private static final class ErrorRenderer extends AbstractFreeMarkerRenderer {

        /**
         * Logger.
         */
        private static final Logger LOGGER = Logger.getLogger(ErrorRenderer.class.getName());
        /**
         * FreeMarker configuration.
         */
        public static final Configuration TEMPLATE_CFG;

        static {
            TEMPLATE_CFG = new Configuration();
            TEMPLATE_CFG.setDefaultEncoding("UTF-8");
            try {
                final String webRootPath = SymphonyServletListener.getWebRoot();

                TEMPLATE_CFG.setDirectoryForTemplateLoading(new File(webRootPath + File.separatorChar + "error"));
            } catch (final IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        @Override
        protected Template getTemplate(final String templateDirName, final String templateName)
                throws IOException {
            return TEMPLATE_CFG.getTemplate(templateName);
        }

        @Override
        protected void beforeRender(final HTTPRequestContext context) throws Exception {
        }

        @Override
        protected void afterRender(final HTTPRequestContext context) throws Exception {
        }
    }
}
