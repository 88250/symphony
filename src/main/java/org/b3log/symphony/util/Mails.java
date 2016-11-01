/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2016,  b3log.org & hacpai.com
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
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.json.JSONObject;

/**
 * Mail utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.4.5, Oct 31, 2016
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
     * @param fromName the specified from name
     * @param subject the specified subject
     * @param toMail the specified receiver mail
     * @param templateName the specified template name
     * @param dataModel the specified data model
     */
    public static void sendHTML(final String fromName, final String subject, final String toMail,
            final String templateName, final Map<String, Object> dataModel) {
        if (StringUtils.isBlank(API_USER) || StringUtils.isBlank(API_KEY)) {
            LOGGER.warn("Please configure [#### SendCloud Mail ####] section in symphony.properties for sending mail");

            return;
        }

        Keys.fillServer(dataModel);
        Keys.fillRuntime(dataModel);

        try {
            final Map<String, Object> formData = new HashMap<>();

            formData.put("apiUser", API_USER);
            formData.put("apiKey", API_KEY);
            formData.put("from", FROM);
            formData.put("fromName", fromName);
            formData.put("subject", subject);
            formData.put("to", toMail);

            final Template template = TEMPLATE_CFG.getTemplate(templateName + ".ftl");
            final StringWriter stringWriter = new StringWriter();
            template.process(dataModel, stringWriter);
            stringWriter.close();
            final String html = stringWriter.toString();
            formData.put("html", html);

            final HttpResponse response = HttpRequest.post("http://api.sendcloud.net/apiv2/mail/send").form(formData).send();
            LOGGER.debug(response.bodyText());
            response.close();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Send mail error", e);
        }
    }

    /**
     * Batch send HTML mails.
     *
     * @param fromName the specified from name
     * @param subject the specified subject
     * @param toMails the specified receiver mails
     * @param templateName the specified template name
     * @param dataModel the specified data model
     */
    public static void batchSendHTML(final String fromName, final String subject, final List<String> toMails,
            final String templateName, final Map<String, Object> dataModel) {
        if (StringUtils.isBlank(BATCH_API_USER) || StringUtils.isBlank(BATCH_API_KEY)) {
            LOGGER.warn("Please configure [#### SendCloud Mail ####] section in symphony.properties form sending mail");

            return;
        }

        Keys.fillServer(dataModel);

        try {
            final Map<String, Object> formData = new HashMap<>();

            formData.put("apiUser", BATCH_API_USER);
            formData.put("apiKey", BATCH_API_KEY);
            formData.put("from", BATCH_FROM);
            formData.put("fromName", fromName);
            formData.put("subject", subject);
            formData.put("templateInvokeName", templateName);

            final Template template = TEMPLATE_CFG.getTemplate(templateName + ".ftl");
            final StringWriter stringWriter = new StringWriter();
            template.process(dataModel, stringWriter);
            stringWriter.close();
            final String html = stringWriter.toString();

            // Creates or updates the SendCloud email template
            refreshWeeklyTemplate(html);

            int index = 0;
            final int size = toMails.size();
            List<String> batch = new ArrayList<>();
            HttpResponse response;
            while (index < size) {
                final String mail = toMails.get(index);
                batch.add(mail);
                index++;

                if (batch.size() > 99) {
                    try {
                        final JSONObject xsmtpapi = new JSONObject();
                        xsmtpapi.put("to", batch);
                        xsmtpapi.put("sub", new JSONObject());
                        formData.put("xsmtpapi", xsmtpapi.toString());

                        response = HttpRequest.post("http://api.sendcloud.net/apiv2/mail/sendtemplate").form(formData).send();
                        LOGGER.debug(response.bodyText());
                        response.close();

                        LOGGER.info("Sent [" + batch.size() + "] mails");
                    } catch (final Exception e) {
                        LOGGER.log(Level.ERROR, "Send mail error", e);
                    }

                    batch.clear();
                }
            }

            if (!batch.isEmpty()) { // Process remains
                try {
                    final JSONObject xsmtpapi = new JSONObject();
                    xsmtpapi.put("to", batch);
                    xsmtpapi.put("sub", new JSONObject());
                    formData.put("xsmtpapi", xsmtpapi.toString());

                    response = HttpRequest.post("http://api.sendcloud.net/apiv2/mail/sendtemplate").form(formData).send();
                    LOGGER.debug(response.bodyText());
                    response.close();

                    LOGGER.info("Sent [" + batch.size() + "] mails");
                } catch (final Exception e) {
                    LOGGER.log(Level.ERROR, "Send mail error", e);
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Batch send mail error", e);
        }
    }

    private static void refreshWeeklyTemplate(final String html) {
        final Map<String, Object> addData = new HashMap<>();
        addData.put("apiUser", BATCH_API_USER);
        addData.put("apiKey", BATCH_API_KEY);
        addData.put("invokeName", TEMPLATE_NAME_WEEKLY);
        addData.put("name", "Weekly Newsletter");
        addData.put("subject", "Weekly Newsletter");
        addData.put("templateType", "1"); // 批量邮件

        addData.put("html", html);
        HttpRequest.post("http://api.sendcloud.net/apiv2/template/add").form(addData).send();

        final Map<String, Object> updateData = new HashMap<>();
        updateData.put("apiUser", BATCH_API_USER);
        updateData.put("apiKey", BATCH_API_KEY);
        updateData.put("invokeName", TEMPLATE_NAME_WEEKLY);

        updateData.put("html", html);
        HttpRequest.post("http://api.sendcloud.net/apiv2/template/update").form(updateData).send();
    }

    /**
     * Private constructor.
     */
    private Mails() {
    }
}
