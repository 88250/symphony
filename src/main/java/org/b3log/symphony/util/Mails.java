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

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mail utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://blog.thinkjava.top">VirutalPier</a>
 * @author <a href="https://github.com/snowflake3721">snowflake</a>
 * @version 1.2.6.6, Mar 10, 2017
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
        if ("sendcloud".equals(MAIL_CHANNEL)) {
            if (StringUtils.isBlank(SENDCLOUD_API_USER) || StringUtils.isBlank(SENDCLOUD_API_KEY)) {
                LOGGER.warn("Please configure [#### SendCloud Mail channel ####] section in symphony.properties for sending mail");

                return;
            }
        } else if ("aliyun".equals(MAIL_CHANNEL)) {
            if (StringUtils.isBlank(ALIYUN_ACCESSKEY) || StringUtils.isBlank(ALIYUN_ACCESSSECRET)) {
                LOGGER.warn("Please configure [#### Aliyun Mail channel ####] section in symphony.properties for sending mail");

                return;
            }
        } else {
            if (StringUtils.isBlank(MailSender.username) || StringUtils.isBlank(MailSender.password)) {
                LOGGER.warn("Please configure [#### Local Mail channel ####] section in symphony.properties for sending mail");

                return;
            }
        }

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
            } else if ("local".equals(MAIL_CHANNEL.toLowerCase())) {
                MailSender.getInstance().sendHTML(fromName, subject, toMail, html);

                return;
            }

            // SendCloud
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
        if ("sendcloud".equals(MAIL_CHANNEL)) {
            if (StringUtils.isBlank(SENDCLOUD_BATCH_API_USER) || StringUtils.isBlank(SENDCLOUD_BATCH_API_KEY)) {
                LOGGER.warn("Please configure [#### SendCloud Mail channel ####] section in symphony.properties for sending mail");

                return;
            }
        } else if ("aliyun".equals(MAIL_CHANNEL)) {
            if (StringUtils.isBlank(ALIYUN_ACCESSKEY) || StringUtils.isBlank(ALIYUN_ACCESSSECRET)) {
                LOGGER.warn("Please configure [#### Aliyun Mail channel ####] section in symphony.properties for sending mail");

                return;
            }
        } else {
            if (StringUtils.isBlank(MailSender.username) || StringUtils.isBlank(MailSender.password)) {
                LOGGER.warn("Please configure [#### Local Mail channel ####] section in symphony.properties for sending mail");

                return;
            }
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
                    } else if ("local".equals(MAIL_CHANNEL.toLowerCase())) {
                        MailSender.getInstance().sendHTML(fromName, subject, batch, html);
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
                } else if ("local".equals(MAIL_CHANNEL.toLowerCase())) {
                    MailSender.getInstance().sendHTML(fromName, subject, batch, html);
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

/**
 * 通过 JavaMail SMTP 发送邮件.
 * <p>
 * 支持三种类型的邮件：
 * <ul>
 * <li>SIMPLE 简单类型，可以发送 HTML 格式文本，遵从 FreeMarker 模板的设置</li>
 * <li>IMAGE HTML 格式类型，同时会保存 HTML 里面的 image 到磁盘，并生成 eml 保存到服务器的配置目录</li>
 * <li>MULTI 带附件的 Attachment，TODO</li>
 * </ul>
 * </p>
 *
 * @author <a href="https://github.com/snowflake3721">snowflake</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.2.1, May 23, 2017
 * @since 2.1.0
 */
final class MailSender implements java.io.Serializable {

    public static final String sender = Symphonys.get("mail.local.smtp.sender");
    public static final String username = Symphonys.get("mail.local.smtp.username");
    public static final String password = Symphonys.get("mail.local.smtp.passsword"); /* 换成自己的密码哦 */

    private static final long serialVersionUID = -1000794424345267933L;
    private static final String CHARSET = "text/html;charset=UTF-8";
    private static final Logger LOGGER = Logger.getLogger(Markdowns.class);
    private static final boolean is_debug = Boolean.valueOf(Symphonys.get("mail.local.isdebug"));
    private static final String mail_transport_protocol = Symphonys.get("mail.local.transport.protocol");
    private static final String mail_host = Symphonys.get("mail.local.host");
    private static final String mail_port = Symphonys.get("mail.local.port");
    private static final String mail_smtp_auth = Symphonys.get("mail.local.smtp.auth");
    private static final String mail_smtp_ssl = Symphonys.get("mail.local.smtp.ssl");
    private static final String mail_smtp_starttls_enable = Symphonys.get("mail.local.smtp.starttls.enable");
    private static final String saved_path = Symphonys.get("mail.local.saved.eml.path");
    private static MailSender mailSender;
    private static Properties prop = new Properties();

    private MailSender() {
        prop.setProperty("mail.transport.protocol", mail_transport_protocol);
        prop.setProperty("mail.host", mail_host);
        prop.setProperty("mail.port", mail_port);
        prop.setProperty("mail.smtp.auth", mail_smtp_auth);
        prop.setProperty("mail.smtp.ssl", mail_smtp_ssl);
        prop.setProperty("mail.smtp.starttls.enable", mail_smtp_starttls_enable);
        prop.setProperty("mail.smtp.sender", sender);
        prop.setProperty("mail.smtp.username", username);
        prop.setProperty("mail.smtp.passsword", password);
    }

    public static final MailSender getInstance() {
        if (null == mailSender) {
            mailSender = MailSenderHolder.INSTANCE;
        }
        return mailSender;
    }

    private static Set<String> getImgStr(String htmlStr) {
        Set<String> pics = new HashSet<>();
        String img = "";
        Pattern p_image;
        Matcher m_image;
        // String regEx_img = "<img.*src=(.*?)[^>]*?>"; //图片链接地址
        String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
        m_image = p_image.matcher(htmlStr);
        while (m_image.find()) {
            // 得到<img />数据
            img = m_image.group();
            // 匹配<img>中的src数据
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
            while (m.find()) {
                pics.add(m.group(1));
            }
        }
        return pics;
    }

    private static void setTo(String[] to, MimeMessage message) throws MessagingException, AddressException {
        // 指明邮件的收件人，现在发件人和收件人是一样的，那就是自己给自己发
        if (null != to && to.length > 0) {
            if (to.length == 1) {
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(to[0]));
            } else {

                List<InternetAddress> iaList = new ArrayList<InternetAddress>();
                for (String t : to) {
                    InternetAddress ia = new InternetAddress(t);
                    iaList.add(ia);
                }
                InternetAddress[] iaArray = new InternetAddress[to.length];
                message.setRecipients(Message.RecipientType.TO, iaList.toArray(iaArray));
            }
        }

    }

    public static void main(String[] args) throws Exception {

        System.out.println(CHARSET.toLowerCase());

		/*
         * MailSender mailSender = getInstance();
		 *
		 * String subject = "eml with Image"; String content =
		 * "这是一封邮件正文带图片<img width=\"60px\" src=\"http://localhost:8080/images/logo-M301-161X105.png\" />的邮件"
		 * ; String[] tos = { "bruceyang_it@163.com" };
		 * mailSender.sendMessage(tos, subject, content, saved_path,
		 * MailType.IMAGE);
		 */

    }

    private Object readResolve() {
        return MailSenderHolder.INSTANCE;
    }

    public void sendMessage(String[] tos, String subject, String content, String savedEmlPath, MailType mailType)
            throws Exception {

        // 1、创建session
        Session session = Session.getInstance(prop);
        // 开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
        session.setDebug(is_debug);
        // 2、通过session得到transport对象
        Transport ts = session.getTransport();
        // 3、使用邮箱的用户名和密码连上邮件服务器，发送邮件时，发件人需要提交邮箱的用户名和密码给smtp服务器，用户名和密码都通过验证之后才能够正常发送邮件给收件人。
        ts.connect(mail_host, Integer.valueOf(mail_port), username, password);
        MimeMessage message = new MimeMessage(session);
        // 4、创建邮件
        // Message message = createSimpleMail(session);

        if (MailType.SIMPLE.equals(mailType)) {
            createTextMail(message, sender, tos, subject, content, savedEmlPath);
        } else if (MailType.IMAGE.equals(mailType)) {
            message = createImageMail(message, sender, tos, subject, content, savedEmlPath);
        } else {
            // TODO MULTI
            createTextMail(message, sender, tos, subject, content, savedEmlPath);
        }

        // 5、发送邮件
        ts.sendMessage(message, message.getAllRecipients());
        LOGGER.debug(tos.toString());
        LOGGER.debug(subject);
        LOGGER.debug(content);
        ts.close();
    }

    /**
     * 创建一封只包含文本的邮件.
     *
     * @param message
     * @param from
     * @param to
     * @param subject
     * @param content
     * @param savedEmlPathName
     * @return
     * @throws Exception
     */
    public MimeMessage createTextMail(MimeMessage message, String from, String[] to, String subject, String content,
                                      String savedEmlPathName) throws Exception {

        long currentTime = System.currentTimeMillis();
        if (!savedEmlPathName.endsWith("/")) {
            savedEmlPathName = savedEmlPathName + "/";
        }

        // 构造日期目录
        String datePath = buildDatePath();

        String savedEmlPathNameDate = savedEmlPathName + datePath;
        File savedEmlPathNameDateFile = new File(savedEmlPathNameDate);
        if (!savedEmlPathNameDateFile.exists()) {
            savedEmlPathNameDateFile.mkdirs();
        }

        // 构造存储文件绝对路径
        String fileName = savedEmlPathNameDate + currentTime + ".eml";

        // 指明邮件的发件人
        message.setFrom(new InternetAddress(from));
        setTo(to, message);

        // 邮件的标题,只包含文本的简单邮件

        if (StringUtils.isEmpty(subject)) {
            subject = "email from the system";
        }
        message.setSubject(StringUtils.trimToEmpty(subject));
        // 邮件的文本内容
        message.setContent(content, CHARSET);

        saveMessageFile(message, fileName);
        // 返回创建好的邮件对象
        return message;
    }

    /**
     * 生成一封邮件正文带图片的邮件.
     *
     * @param message
     * @param from
     * @param to
     * @param subject
     * @param content
     * @param savedEmlPath
     * @return
     * @throws Exception
     */
    public MimeMessage createImageMail(MimeMessage message, String from, String[] to, String subject, String content,
                                       String savedEmlPath) throws Exception {
        // 创建邮件
        // MimeMessage message = new MimeMessage(session);
        // 设置邮件的基本信息
        // 发件人
        message.setFrom(new InternetAddress(from));
        setTo(to, message);
        // 邮件标题
        if (StringUtils.isEmpty(subject)) {
            subject = "Email from the system [Dromala special community for study]";
        }
        message.setSubject(StringUtils.trimToEmpty(subject));

        // 准备邮件数据
        message = resolveContentForImage(message, content, savedEmlPath);

        // 返回创建好的邮件
        return message;
    }

    public MimeMessage resolveContentForImage(MimeMessage message, String content, String savedEmlPathName)
            throws Exception {
        long currentTime = System.currentTimeMillis();
        if (!savedEmlPathName.endsWith("/")) {
            savedEmlPathName = savedEmlPathName + "/";
        }

        // 构造日期目录
        String datePath = buildDatePath();

        String savedEmlPathNameDate = savedEmlPathName + datePath;
        File savedEmlPathNameDateFile = new File(savedEmlPathNameDate);
        if (!savedEmlPathNameDateFile.exists()) {
            savedEmlPathNameDateFile.mkdirs();
        }

        // 构造存储文件绝对路径
        String fileName = savedEmlPathNameDate + currentTime + ".eml";

        Set<String> imageStrSet = getImgStr(content);
        // 准备邮件正文数据
        MimeBodyPart text = new MimeBodyPart();

        if (null != imageStrSet && imageStrSet.size() > 0) {
            int i = 0;
            for (String imageStr : imageStrSet) {

                // 准备图片数据
                MimeBodyPart image = new MimeBodyPart();
                String imagenew = currentTime + "/" + i + imageStr.substring(imageStr.lastIndexOf("."));
                // imagenew = imagenew.replaceAll("/", "_");
                String savedPathAndFileName = savedEmlPathNameDate + imagenew;

                DataHandler dh = new DataHandler(new UrlDataSource(imageStr, savedPathAndFileName, true));
                image.setDataHandler(dh);
                String imagenewcid = "cid:" + imagenew;
                image.setContentID(imagenew);

                // 描述数据关系
                MimeMultipart mm = new MimeMultipart();
                mm.addBodyPart(text);
                mm.addBodyPart(image);
                mm.setSubType("related");

                message.setContent(mm);
                content = content.replaceAll(imageStr, imagenewcid);
                text.setContent(content, CHARSET);
                message.saveChanges();
                i++;
            }

        }
        text.setContent(content, CHARSET);
        saveMessageFile(message, fileName);

        return message;
    }

    private void saveMessageFile(MimeMessage message, String fileName)
            throws MessagingException, FileNotFoundException, IOException {
        message.saveChanges();
        // 将创建好的邮件写入到E盘以文件的形式进行保存
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            message.writeTo(fos);
            fos.flush();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    private String buildDatePath() {
        StringBuilder sb = new StringBuilder();
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);// 获取年份
        int month = ca.get(Calendar.MONTH);// 获取月份
        int day = ca.get(Calendar.DATE);// 获取日
        sb.append(year).append("/").append(month).append("/").append(day).append("/");
        return sb.toString();
    }

    /**
     * Sends a HTML mail for toMailList.
     *
     * @param fromName
     * @param subject
     * @param toMailList
     * @param html
     */
    public void sendHTML(final String fromName, final String subject, final List<String> toMailList, String html) {
        if (null != toMailList && toMailList.size() > 0) {
            sendHTML(fromName, subject, toMailList.toArray(new String[toMailList.size()]), html);
        }

    }

    /**
     * Sends a HTML mail.
     *
     * @param fromName
     * @param subject
     * @param toMailSingle
     * @param html
     */
    public void sendHTML(final String fromName, final String subject, final String toMailSingle, String html) {

        sendHTML(fromName, subject, new String[]{toMailSingle}, html);

    }

    public void sendHTML(final String fromName, final String subject, final String[] toMail, final String html) {

        try {
            /*
             * Keys.fillServer(dataModel); Keys.fillRuntime(dataModel);
			 *
			 *
			 * final Template template = TEMPLATE_CFG.getTemplate(templateName +
			 * ".ftl"); final StringWriter stringWriter = new StringWriter();
			 * template.process(dataModel, stringWriter); stringWriter.close();
			 * final String content = stringWriter.toString();
			 */

            getInstance().sendMessage(toMail, subject, html, saved_path, MailType.IMAGE);
            LOGGER.debug(html);

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Send mail error", e);
        }
    }

    public static enum MailType {
        SIMPLE, IMAGE /* html with image */, MULTI/* Attachment TODO */
    }

    private static class MailSenderHolder {
        private static final MailSender INSTANCE = new MailSender();
    }
}

/**
 * 加载网络资源图片保存到本地
 *
 * @author snowflake
 * @version 1.0.0.0, Mar 9, 2016
 * @since 2.0.0
 */
class UrlDataSource implements DataSource {

    private String urlPath;
    private String savedFileName;
    private boolean saved;
    private FileTypeMap typeMap = null;
    private File _file;

    public UrlDataSource(String urlPath, String savedFileName, boolean saved) {
        this.urlPath = urlPath;
        this.savedFileName = savedFileName;
        this.saved = saved;
        File imageFile = new File(this.savedFileName);
        this._file = imageFile;
    }

    public static byte[] readInputStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        // 创建一个Buffer字符串
        byte[] buffer = new byte[1024];
        // 每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len = 0;
        // 使用一个输入流从buffer里把数据读取出来
        while ((len = inStream.read(buffer)) != -1) {
            // 用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outStream.write(buffer, 0, len);
        }
        // 关闭输入流
        inStream.close();
        // 把outStream里的数据写入内存
        return outStream.toByteArray();
    }

    @Override
    public InputStream getInputStream() throws IOException {

        return getInputStreamFromURL(this.urlPath, this.saved);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {

        if (this._file.isDirectory()) {
            if (!this._file.exists()) {
                this._file.mkdirs();
            }
        }

        String parentPath = this._file.getParent();
        File _filePath = new File(parentPath);
        _filePath.mkdirs();

        return new FileOutputStream(this._file);
    }

    @Override
    public String getContentType() {

        // check to see if the type map is null?
        if (typeMap == null)
            return FileTypeMap.getDefaultFileTypeMap().getContentType(_file);
        else
            return typeMap.getContentType(_file);
    }

    @Override
    public String getName() {
        return _file.getName();
    }

    private InputStream getInputStreamFromURL(String urlPath, boolean saved) throws IOException {
        // new一个URL对象
        URL url = new URL(urlPath);
        // 打开链接
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // 设置请求方式为"GET"
        conn.setRequestMethod("GET");
        // 超时响应时间为5秒
        conn.setConnectTimeout(5 * 1000);
        // 通过输入流获取图片数据
        InputStream inStream = conn.getInputStream();
        // 得到图片的二进制数据，以二进制封装得到数据，具有通用性
        if (saved) {
            try {
                saveFile(inStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        InputStream fis = new FileInputStream(_file);
        return fis;
    }

    // 得到图片的二进制数据，以二进制封装得到数据，具有通用性
    public void saveFile(InputStream inStream) throws IOException {
        byte[] data = readInputStream(inStream);
        // new一个文件对象用来保存图片，默认保存当前工程根目录
        // File imageFile = new File(fileName);
        // 创建输出流
        FileOutputStream outStream = (FileOutputStream) getOutputStream();
        // 写入数据
        outStream.write(data);
        // 关闭输出流
        outStream.close();
    }

    /**
     * Return the File object that corresponds to this FileDataSource.
     *
     * @return the File object for the file represented by this object.
     */
    public File getFile() {
        return _file;
    }

    /**
     * Set the FileTypeMap to use with this FileDataSource
     *
     * @param map The FileTypeMap for this object.
     */
    public void setFileTypeMap(FileTypeMap map) {
        typeMap = map;
    }

}