package org.b3log.symphony.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.UrlDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * Send Email use SMTP 
 * 功能：三种类型的邮件
 * SIMPLE  简单类型，可以发送html格式文本,遵从freemarker模板的设置
   IMAGE  html格式类型，同时会保存html里面的  image 到磁盘，并生成eml保存到服务器的配置目录 , 
   MULTI  带附件的 Attachment，TODO 
 * 
 * @author snowflake
 * @version 1.0.0.0, Mar 9, 2016
 * @since 2.0.0
 *
 */

public final class MailSender implements java.io.Serializable {

	private static final long serialVersionUID = -1000794424345267933L;
	
	private static MailSender mailSender;

	private static class MailSenderHolder {
		private static final MailSender INSTANCE = new MailSender();
	}

	public static final MailSender getInstance() {
		if(null == mailSender){
			mailSender=MailSenderHolder.INSTANCE;
		}
		return mailSender;
	}
	
	private Object readResolve() {     
        return MailSenderHolder.INSTANCE;     
	}  
	
	 

	public static enum MailType {
		SIMPLE, 
		IMAGE /* html with image */, 
		MULTI/* Attachment TODO*/
	}
	

	private static final String CHARSET = "text/html;charset=UTF-8";
	private static final String CONFIG_PATH = "/email.properties";
	private static final Logger LOGGER = Logger.getLogger(Markdowns.class);

	private static Properties prop = new Properties();

	private static boolean smtpEnable = false;
	private static boolean isdebug = false;
	private static String mail_transport_protocol = "smtp";
	private static String mail_host = "smtp.163.com";
	private static int mail_port = 25;
	private static boolean mail_smtp_auth = true;
	private static boolean mail_smtp_ssl = true;
	private static boolean mail_smtp_starttls_enable = true;
	public static String sender = "snowflake3721@163.com";
	public static String username = "snowflake3721";
	private static String password = "snowflake3721";/*换成自己的密码哦*/
	private static String saved_path = "../mail";

	private MailSender() {
		init();
	}
	
	public boolean getSmtpEnable(){
		if(isdebug){
			init();
		}
		return smtpEnable;
		
	}

	public void init() {
		InputStream fis = MailSender.class.getResourceAsStream(CONFIG_PATH);
		try {
			prop.load(fis);
			smtpEnable = Boolean.valueOf(prop.getProperty("smtp.enable"));
			isdebug = Boolean.valueOf(prop.getProperty("isdebug"));
			mail_transport_protocol = prop.getProperty("mail.transport.protocol");
			String mailPort = prop.getProperty("mail.port");
			if (StringUtils.isNotEmpty(mailPort)) {
				mail_port = Integer.valueOf(mailPort);
			}

			mail_host = prop.getProperty("mail.host");
			mail_smtp_auth = Boolean.valueOf(prop.getProperty("mail.smtp.auth"));
			mail_smtp_ssl = Boolean.valueOf(prop.getProperty("mail.smtp.ssl"));
			sender = prop.getProperty("mail.smtp.sender");
			username = prop.getProperty("mail.smtp.username");
			password = prop.getProperty("mail.smtp.passsword");
			String savedPath = prop.getProperty("saved.eml.path");
			if (StringUtils.isNotEmpty(savedPath)) {
				saved_path = savedPath;
			} else {
				saved_path = System.getProperty("user.dir");
				// String path = MailSender.class.getResource("/").getPath();
			}
			LOGGER.info("email EML stored path: " + saved_path);

		} catch (IOException e) {
			LOGGER.error("加载" + CONFIG_PATH + "出错" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				fis.close();

			} catch (IOException e) {
				LOGGER.error("关闭输入流出错:" + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	
	/**
     * Template configuration.
     */
    private static final Configuration TEMPLATE_CFG = new Configuration(Configuration.VERSION_2_3_23);

    static {
        try {
            TEMPLATE_CFG.setDirectoryForTemplateLoading(new File(MailSender.class.getResource("/mail_tpl").toURI()));
            TEMPLATE_CFG.setDefaultEncoding("UTF-8");
            TEMPLATE_CFG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            TEMPLATE_CFG.setLogTemplateExceptions(false);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Loads mail templates failed", e);
        }
    }

	public void sendMessage(String[] tos, String subject, String content, String savedEmlPath, MailType mailType)
			throws Exception {
		//MailSender mailSender = getInstance();
		if(isdebug){
			init();//debug模式下可动态加载配置
		}
		
		// 1、创建session
		Session session = Session.getInstance(prop);
		// 开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
		session.setDebug(isdebug);
		// 2、通过session得到transport对象
		Transport ts = session.getTransport();
		// 3、使用邮箱的用户名和密码连上邮件服务器，发送邮件时，发件人需要提交邮箱的用户名和密码给smtp服务器，用户名和密码都通过验证之后才能够正常发送邮件给收件人。
		ts.connect(mail_host, username, password);
		MimeMessage message = new MimeMessage(session);
		// 4、创建邮件
		// Message message = createSimpleMail(session);

		if (MailType.SIMPLE.equals(mailType)) {
			createTextMail(message, sender, tos, subject, content, savedEmlPath);
		} else if (MailType.IMAGE.equals(mailType)) {
			message = createImageMail(message, sender, tos, subject, content, savedEmlPath);
		}

		// 5、发送邮件
		ts.sendMessage(message, message.getAllRecipients());
		LOGGER.debug(tos.toString());
		LOGGER.debug(subject);
		LOGGER.debug(content);
		ts.close();
	}

	/**
	 * @Method: createTextMail
	 * @Description: 创建一封只包含文本的邮件
	 * @Anthor:snowflake
	 *
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public MimeMessage createTextMail(MimeMessage message, String from, String[] to, String subject,
			String content, String savedEmlPathName) throws Exception {

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
	 * @Method: createImageMail
	 * @Description: 生成一封邮件正文带图片的邮件
	 * @Anthor:snowflake
	 *
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public MimeMessage createImageMail(MimeMessage message, String from, String[] to, String subject,
			String content, String savedEmlPath) throws Exception {
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
			if(fos != null){
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

	public static Set<String> getImgStr(String htmlStr) {
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
	
	
	/**
     * Sends a HTML mail.
     *
     * @param fromName     the specified from name
     * @param subject      the specified subject
     * @param toMail       the specified receiver mail
     * @param templateName the specified template name
     * @param dataModel    the specified data model
     */
    @SuppressWarnings("static-access")
	public  void sendHTML(final String fromName, final String subject, final String toMail,
                                final String templateName, final Map<String, Object> dataModel) {


        Keys.fillServer(dataModel);
        Keys.fillRuntime(dataModel);

        try {
            final Template template = TEMPLATE_CFG.getTemplate(templateName + ".ftl");
            final StringWriter stringWriter = new StringWriter();
            template.process(dataModel, stringWriter);
            stringWriter.close();
            final String content = stringWriter.toString();

            getInstance().sendMessage(new String[]{toMail}, subject, content, saved_path, MailType.IMAGE);
            LOGGER.debug(content);
           
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Send mail error", e);
        }
    }
	
	public static void main(String[] args) throws Exception {

		MailSender mailSender = getInstance();

		String subject = "eml with Image";
		String content = "这是一封邮件正文带图片<img width=\"60px\" src=\"http://localhost:8080/images/logo-M301-161X105.png\" />的邮件";
		String[] tos = { "bruceyang_it@163.com" };
		mailSender.sendMessage(tos, subject, content, saved_path, MailType.IMAGE);

	}

}
