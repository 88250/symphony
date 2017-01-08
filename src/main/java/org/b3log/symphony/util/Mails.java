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
package org.b3log.symphony.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Mail utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://blog.thinkjava.top">VirutalPier</a>
 * @version 1.1.6.6, Jan 8, 2017
 * @since 1.3.0
 */
public final class Mails {

    /**
     * Template name - verifycode.
     */
    public static final String TEMPLATE_NAME_VERIFYCODE = "sym_verifycode";
    /**
     * Template name - weekly.
     */
    public static final String TEMPLATE_NAME_WEEKLY = "sym_weekly";
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Mails.class);
    /**
     * Mail channel.
     */
    private static final String MAIL_CHANNEL = Symphonys.get("mail.channel");
    /**
     * SendCloud API user.
     */
    private static final String SENDCLOUD_API_USER = Symphonys.get("mail.sendcloud.apiUser");
    /**
     * SendCloud API key.
     */
    private static final String SENDCLOUD_API_KEY = Symphonys.get("mail.sendcloud.apiKey");
    /**
     * SendCloud from.
     */
    private static final String SENDCLOUD_FROM = Symphonys.get("mail.sendcloud.from");
    /**
     * SendCloud batch API User.
     */
    private static final String SENDCLOUD_BATCH_API_USER = Symphonys.get("mail.sendcloud.batch.apiUser");
    /**
     * SendCloud batch API key.
     */
    private static final String SENDCLOUD_BATCH_API_KEY = Symphonys.get("mail.sendcloud.batch.apiKey");
    /**
     * SendCloud batch sender email.
     */
    private static final String SENDCLOUD_BATCH_FROM = Symphonys.get("mail.sendcloud.batch.from");
    /**
     * Aliyun accesskey.
     */
    private static final String ALIYUN_ACCESSKEY = Symphonys.get("mail.aliyun.accessKey");
    /**
     * Aliyun access secret.
     */
    private static final String ALIYUN_ACCESSSECRET = Symphonys.get("mail.aliyun.accessSecret");
    /**
     * Aliyun from.
     */
    private static final String ALIYUN_FROM = Symphonys.get("mail.aliyun.from");
    /**
     * Aliyun batch from.
     */
    private static final String ALIYUN_BATCH_FROM = Symphonys.get("mail.aliyun.batch.from");
    /**
     * Template configuration.
     */
    private static final Configuration TEMPLATE_CFG = new Configuration(Configuration.VERSION_2_3_23);

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
     * Private constructor.
     */
    private Mails() {
    }

    /**
     * Sends a HTML mail.
     *
     * @param fromName     the specified from name
     * @param subject      the specified subject
     * @param toMail       the specified receiver mail
     * @param templateName the specified template name
     * @param dataModel    the specified data model
     */
    public static void sendHTML(final String fromName, final String subject, final String toMail,
                                final String templateName, final Map<String, Object> dataModel) {


        Keys.fillServer(dataModel);
        Keys.fillRuntime(dataModel);

        try {
            final Map<String, Object> formData = new HashMap<>();

            final Template template = TEMPLATE_CFG.getTemplate(templateName + ".ftl");
            final StringWriter stringWriter = new StringWriter();
            template.process(dataModel, stringWriter);
            stringWriter.close();
            final String html = stringWriter.toString();

            if ("aliyun".equals(MAIL_CHANNEL)) {
                aliSendHtml(ALIYUN_FROM, fromName, subject, toMail, html, ALIYUN_ACCESSKEY, ALIYUN_ACCESSSECRET);
                return;
            }

            if (StringUtils.isBlank(SENDCLOUD_API_USER) || StringUtils.isBlank(SENDCLOUD_API_KEY)) {
                LOGGER.warn("Please configure [#### SendCloud Mail ####] section in symphony.properties for sending mail");

                return;
            }

            formData.put("apiUser", SENDCLOUD_API_USER);
            formData.put("apiKey", SENDCLOUD_API_KEY);
            formData.put("from", SENDCLOUD_FROM);
            formData.put("fromName", fromName);
            formData.put("subject", subject);
            formData.put("to", toMail);
            formData.put("html", html);

            final HttpResponse response = HttpRequest.post("http://api.sendcloud.net/apiv2/mail/send").form(formData).send();
            LOGGER.debug(response.bodyText());
            response.close();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Send mail error", e);
        }
    }

    /**
     * Sends a HTML mail via Aliyun.
     *
     * @param fromName the specified from name
     * @param subject  the specified subject
     * @param toMail   the specified receiver mail
     * @param html     send html
     */
    private static void aliSendHtml(final String sendMail, final String fromName, final String subject, final String toMail,
                                    final String html, final String accessKey, final String accessSecret) throws Exception {
        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(accessSecret)) {
            LOGGER.warn("Please configure [#### Aliyun Mail ####] section in symphony.properties for sending mail");

            return;
        }

        final Map<String, Object> map = new HashMap<>();
        map.put("Action", "SingleSendMail");
        map.put("Format", "JSON");
        map.put("Version", "2015-11-23");
        map.put("AccessKeyId", accessKey);
        map.put("SignatureMethod", "HMAC-SHA1");
        map.put("Timestamp", getISO8601Time());
        map.put("SignatureVersion", "1.0");
        map.put("SignatureNonce", String.valueOf(System.currentTimeMillis()));
        map.put("AccountName", sendMail);
        map.put("FromAlias", fromName);
        map.put("ReplyToAddress", "true");
        map.put("AddressType", "1");
        map.put("ToAddress", toMail);
        map.put("Subject", subject);
        map.put("HtmlBody", html);

        final String[] sortedKeys = map.keySet().toArray(new String[]{});
        Arrays.sort(sortedKeys);
        final StringBuilder canonicalizedQueryString = new StringBuilder();
        try {
            for (String key : sortedKeys) {
                canonicalizedQueryString.append("&")
                        .append(Mails.percentEncode(key)).append("=")
                        .append(Mails.percentEncode(map.get(key).toString()));
            }
            final StringBuffer stringToSign = new StringBuffer();
            stringToSign.append("POST");
            stringToSign.append("&");
            stringToSign.append(Mails.percentEncode("/"));
            stringToSign.append("&");
            stringToSign.append(Mails.percentEncode(canonicalizedQueryString.toString().substring(1)));

            map.put("Signature", HmacSHA1.signString(stringToSign.toString(), accessSecret + "&"));
        } catch (UnsupportedEncodingException exp) {
            throw new RuntimeException("UTF-8 encoding is not supported.");
        }

        final HttpResponse response = HttpRequest.post("https://dm.aliyuncs.com").form(map).send();
        LOGGER.debug(response.bodyText());

        response.close();
    }

    private static String percentEncode(final String value) throws UnsupportedEncodingException {
        return value != null ? URLEncoder.encode(value, "UTF-8").replace("+", "%20")
                .replace("*", "%2A").replace("%7E", "~") : null;
    }

    /**
     * Batch send HTML mails.
     *
     * @param fromName     the specified from name
     * @param subject      the specified subject
     * @param toMails      the specified receiver mails
     * @param templateName the specified template name
     * @param dataModel    the specified data model
     */
    public static void batchSendHTML(final String fromName, final String subject, final List<String> toMails,
                                     final String templateName, final Map<String, Object> dataModel) {
        if (StringUtils.isBlank(SENDCLOUD_BATCH_API_USER) || StringUtils.isBlank(SENDCLOUD_BATCH_API_KEY)) {
            LOGGER.warn("Please configure [#### SendCloud Mail ####] section in symphony.properties form sending mail");

            return;
        }

        Keys.fillServer(dataModel);

        try {
            final Map<String, Object> formData = new HashMap<>();

            formData.put("apiUser", SENDCLOUD_BATCH_API_USER);
            formData.put("apiKey", SENDCLOUD_BATCH_API_KEY);
            formData.put("from", SENDCLOUD_BATCH_FROM);
            formData.put("fromName", fromName);
            formData.put("subject", subject);
            formData.put("templateInvokeName", templateName);

            final Template template = TEMPLATE_CFG.getTemplate(templateName + ".ftl");
            final StringWriter stringWriter = new StringWriter();
            template.process(dataModel, stringWriter);
            stringWriter.close();
            final String html = stringWriter.toString();

            // Creates or updates the SendCloud email template
            if ("sendcloud".equals(MAIL_CHANNEL)) {
                refreshWeeklyTemplate(html);
            }

            int index = 0;
            final int size = toMails.size();
            List<String> batch = new ArrayList<>();
            HttpResponse response;
            while (index < size) {
                final String mail = toMails.get(index);
                batch.add(mail);
                index++;

                if (batch.size() > 99) {
                    if ("aliyun".equals(MAIL_CHANNEL)) {
                        final String toMail = getStringToMailByList(batch);
                        aliSendHtml(ALIYUN_BATCH_FROM, fromName, subject, toMail, html, ALIYUN_ACCESSKEY, ALIYUN_ACCESSSECRET);
                        LOGGER.info("Sent [" + batch.size() + "] mails");
                    } else {
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

                    batch.clear();
                }
            }

            if (!batch.isEmpty()) { // Process remains
                if ("aliyun".equals(MAIL_CHANNEL)) {
                    final String toMail = getStringToMailByList(batch);
                    aliSendHtml(ALIYUN_BATCH_FROM, fromName, subject, toMail, html, ALIYUN_ACCESSKEY, ALIYUN_ACCESSSECRET);
                    LOGGER.info("Sent [" + batch.size() + "] mails");
                } else {
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
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Batch send mail error", e);
        }
    }

    private static String getISO8601Time() {
        final Date nowDate = new Date();
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));

        return df.format(nowDate);
    }

    private static String getStringToMailByList(final List<String> toMails) {
        final StringBuffer mails = new StringBuffer();
        for (String mail : toMails) {
            mails.append(mail + ",");
        }
        mails.deleteCharAt(mails.length() - 1);
        return mails.toString();
    }

    private static void refreshWeeklyTemplate(final String html) {
        final Map<String, Object> addData = new HashMap<>();
        addData.put("apiUser", SENDCLOUD_BATCH_API_USER);
        addData.put("apiKey", SENDCLOUD_BATCH_API_KEY);
        addData.put("invokeName", TEMPLATE_NAME_WEEKLY);
        addData.put("name", "Weekly Newsletter");
        addData.put("subject", "Weekly Newsletter");
        addData.put("templateType", "1"); // 批量邮件

        addData.put("html", html);
        HttpRequest.post("http://api.sendcloud.net/apiv2/template/add").form(addData).send();

        final Map<String, Object> updateData = new HashMap<>();
        updateData.put("apiUser", SENDCLOUD_BATCH_API_USER);
        updateData.put("apiKey", SENDCLOUD_BATCH_API_KEY);
        updateData.put("invokeName", TEMPLATE_NAME_WEEKLY);

        updateData.put("html", html);
        HttpRequest.post("http://api.sendcloud.net/apiv2/template/update").form(updateData).send();
    }
}
