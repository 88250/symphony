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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import org.b3log.latke.ioc.Lifecycle;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.LangPropsServiceImpl;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Mail utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.2.1, Apr 13, 2016
 * @since 1.3.0
 */
public final class Mails {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Mails.class.getName());

    /**
     * API User.
     */
    private static final String API_USER = Symphonys.get("sendcloud.apiUser");

    /**
     * API Key.
     */
    private static final String API_KEY = Symphonys.get("sendcloud.apiKey");

    /**
     * Sender email.
     */
    private static final String FROM = Symphonys.get("sendcloud.from");

    /**
     * Sends mail.
     *
     * @param toMails to mails
     * @param templateName template name
     * @param subject subject
     * @param variables template variables
     */
    public static void send(final String subject, final String templateName,
            final List<String> toMails, final Map<String, List<String>> variables) {
        if (null == toMails || toMails.isEmpty()) {
            return;
        }

        try {
            final Map<String, Object> formData = new HashMap<String, Object>();

            final LangPropsService langPropsService = Lifecycle.getBeanManager().getReference(LangPropsServiceImpl.class);

            formData.put("api_user", API_USER);
            formData.put("api_key", API_KEY);
            formData.put("from", FROM);
            formData.put("fromname", langPropsService.get("symphonyLabel"));
            formData.put("subject", subject);
            formData.put("template_invoke_name", templateName);

            final JSONObject args = new JSONObject();
            args.put("to", new JSONArray(toMails));
            final JSONObject sub = new JSONObject();
            args.put("sub", sub);
            for (final Map.Entry<String, List<String>> var : variables.entrySet()) {
                final JSONArray value = new JSONArray(var.getValue());
                sub.put(var.getKey(), value);
            }
            formData.put("substitution_vars", args.toString());
            formData.put("resp_email_id", "true");

            final HttpResponse response = HttpRequest.post("http://sendcloud.sohu.com/webapi/mail.send_template.json")
                    .form(formData).send();

            LOGGER.log(Level.DEBUG, response.bodyText());
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Send mail error", e);
        }
    }

    /**
     * Test.
     *
     * @param args args
     * @throws IOException exception
     */
    public static void main(final String[] args) throws IOException {
        final List<String> var1 = new ArrayList<String>();
        var1.add("88250");
        final List<String> var2 = new ArrayList<String>();
        var2.add("https://hacpai.com");

        final Map<String, List<String>> vars = new HashMap<String, List<String>>();
        vars.put("%1%", var1);
        vars.put("%2%", var2);

        final List<String> toMails = new ArrayList<String>();
        toMails.add("845765@qq.com");

        send("测试邮件", "sym_register", toMails, vars);
    }

    /**
     * Private constructor.
     */
    private Mails() {
    }
}
