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
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jodd.http.HttpRequest;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Lifecycle;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.LangPropsServiceImpl;

/**
 * Mail utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.3.3, Sep 15, 2016
 * @since 1.3.0
 */
public final class Mails {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Mails.class.getName());

    /**
     * API user.
     */
    private static final String API_USER = Symphonys.get("sendcloud.apiUser");

    /**
     * API key.
     */
    private static final String API_KEY = Symphonys.get("sendcloud.apiKey");

    /**
     * Sender email.
     */
    private static final String FROM = Symphonys.get("sendcloud.from");

    /**
     * Batch API User.
     */
    private static final String BATCH_API_USER = Symphonys.get("sendcloud.batch.apiUser");

    /**
     * Batch API key.
     */
    private static final String BATCH_API_KEY = Symphonys.get("sendcloud.batch.apiKey");

    /**
     * Batch sender email.
     */
    private static final String BATCH_FROM = Symphonys.get("sendcloud.batch.from");

    /**
     * Template configuration.
     */
    private static final Configuration TEMPLATE_CFG = new Configuration(Configuration.VERSION_2_3_23);

    /**
     * Template name - verifycode.
     */
    public static final String TEMPLATE_NAME_VERIFYCODE = "sym_verifycode";

    /**
     * Template name - weekly.
     */
    public static final String TEMPLATE_NAME_WEEKLY = "sym_weekly";

    static {
        try {
            TEMPLATE_CFG.setDirectoryForTemplateLoading(new File(Mails.class.getResource("/mail_tpl").toURI()));
            TEMPLATE_CFG.setDefaultEncoding("UTF-8");
            TEMPLATE_CFG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            TEMPLATE_CFG.setLogTemplateExceptions(false);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Loads mail templates failed", e);
        }
    }

    /**
     * Sends a HTML mail.
     *
     * @param subject the specified subject
     * @param toMail the specified receiver mail
     * @param templateName the specified template name
     * @param dataModel the specified data model
     */
    public static void sendHTML(final String subject, final String toMail,
            final String templateName, final Map<String, Object> dataModel) {
        if (StringUtils.isBlank(BATCH_API_USER) || StringUtils.isBlank(BATCH_API_KEY)) {
            LOGGER.warn("Please configure [#### SendCloud Mail ####] section in symphony.properties");

            return;
        }

        Keys.fillServer(dataModel);
        Keys.fillRuntime(dataModel);

        try {
            final Map<String, Object> formData = new HashMap<>();

            final LangPropsService langPropsService = Lifecycle.getBeanManager().getReference(LangPropsServiceImpl.class);

            formData.put("apiUser", API_USER);
            formData.put("apiKey", API_KEY);
            formData.put("from", FROM);
            formData.put("fromName", langPropsService.get("visionLabel"));
            formData.put("subject", subject);

            final Template template = TEMPLATE_CFG.getTemplate(templateName + ".ftl");
            final StringWriter stringWriter = new StringWriter();
            template.process(dataModel, stringWriter);
            stringWriter.close();
            final String html = stringWriter.toString();
            formData.put("html", html);

            HttpRequest.post("http://api.sendcloud.net/apiv2/mail/send").form(formData).send();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Send mail error", e);
        }
    }

    /**
     * Batch send HTML mails.
     *
     * @param subject the specified subject
     * @param toMails the specified receiver mails
     * @param templateName the specified template name
     * @param dataModel the specified data model
     */
    public static void batchSendHTML(final String subject, final List<String> toMails,
            final String templateName, final Map<String, Object> dataModel) {
        if (StringUtils.isBlank(BATCH_API_USER) || StringUtils.isBlank(BATCH_API_KEY)) {
            LOGGER.warn("Please configure [#### SendCloud Mail ####] section in symphony.properties");

            return;
        }

        Keys.fillServer(dataModel);

        try {
            final Map<String, Object> formData = new HashMap<>();
            final LangPropsService langPropsService = Lifecycle.getBeanManager().getReference(LangPropsServiceImpl.class);

            formData.put("apiUser", BATCH_API_USER);
            formData.put("apiKey", BATCH_API_KEY);
            formData.put("from", BATCH_FROM);
            formData.put("fromName", langPropsService.get("visionLabel"));
            formData.put("subject", subject);
            final Template template = TEMPLATE_CFG.getTemplate(templateName + ".ftl");
            final StringWriter stringWriter = new StringWriter();
            template.process(dataModel, stringWriter);
            stringWriter.close();
            final String html = stringWriter.toString();
            formData.put("html", html);

            int index = 0;
            final int size = toMails.size();
            List<String> batch = new ArrayList<>();
            while (index < size) {
                final String mail = toMails.get(index);
                batch.add(mail);
                index++;

                if (batch.size() > 99) {
                    try {
                        formData.put("to", StringUtils.join(batch, ";"));

                        HttpRequest.post("http://api.sendcloud.net/apiv2/mail/send").form(formData).send();

                        LOGGER.info("Sent [" + batch.size() + "] mails");
                    } catch (final Exception e) {
                        LOGGER.log(Level.ERROR, "Send mail error", e);
                    }

                    batch.clear();
                }
            }

            if (!batch.isEmpty()) { // Process remains
                try {
                    formData.put("to", StringUtils.join(batch, ";"));

                    HttpRequest.post("http://api.sendcloud.net/apiv2/mail/send").form(formData).send();

                    LOGGER.info("Sent [" + batch.size() + "] mails");
                } catch (final Exception e) {
                    LOGGER.log(Level.ERROR, "Send mail error", e);
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Batch send mail error", e);
        }
    }

    /**
     * Private constructor.
     */
    private Mails() {
    }
}
