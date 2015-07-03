/*
 * Copyright (c) 2012-2015, b3log.org
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
import java.util.ResourceBundle;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Mail utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jul 2, 2015
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
    private static final String API_USER = Symphonys.get("apiUser");

    /**
     * API Key.
     */
    private static final String API_KEY = Symphonys.get("apiKey");

    /**
     * Mail from name.
     */
    private static final String FROM_NAME;

    static {
        final ResourceBundle lang = ResourceBundle.getBundle("lang");

        FROM_NAME = lang.getString("symphonyLabel") + " - " + lang.getString("visionLabel");
    }

    /**
     * Sends mail.
     *
     * @param toMails to mails
     * @param subject subject
     * @param variables template variables
     */
    public static void send(final String subject, final List<String> toMails, final Map<String, List<String>> variables) {
        try {
            final String url = "http://sendcloud.sohu.com/webapi/mail.send_template.json";
            final HttpPost httpost = new HttpPost(url);
            final CloseableHttpClient httpclient = HttpClientBuilder.create().build();

            final List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
            params.add(new BasicNameValuePair("api_user", API_USER));
            params.add(new BasicNameValuePair("api_key", API_KEY));
            params.add(new BasicNameValuePair("from", "sym@b3log.org"));
            params.add(new BasicNameValuePair("fromname", FROM_NAME));
            params.add(new BasicNameValuePair("subject", subject));
            params.add(new BasicNameValuePair("template_invoke_name", "sym_register"));

            final JSONObject args = new JSONObject();
            args.put("to", new JSONArray(toMails));
            final JSONObject sub = new JSONObject();
            args.put("sub", sub);
            for (final Map.Entry<String, List<String>> var : variables.entrySet()) {
                final JSONArray value = new JSONArray(var.getValue());
                sub.put(var.getKey(), value);
            }
            params.add(new BasicNameValuePair("substitution_vars", args.toString()));
            params.add(new BasicNameValuePair("resp_email_id", "true"));

            httpost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            final HttpResponse response = httpclient.execute(httpost);

            if (HttpStatus.OK_200 != response.getStatusLine().getStatusCode()) {
                LOGGER.log(Level.ERROR, "Send mail return error", EntityUtils.toString(response.getEntity()));
            }

            httpost.releaseConnection();
            httpclient.close();
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
        var2.add("http://symphony.b3log.org");

        final Map<String, List<String>> vars = new HashMap<String, List<String>>();
        vars.put("%1%", var1);
        vars.put("%2%", var2);

        final List<String> toMails = new ArrayList<String>();
        toMails.add("dl88250@gmail.com");
        
        send("测试邮件", toMails, vars);
    }

    /**
     * Private constructor.
     */
    private Mails() {
    }
}
