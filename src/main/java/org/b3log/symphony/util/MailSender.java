package org.b3log.symphony.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;
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
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;

/**
 * Send Email use SMTP 功能：三种类型的邮件 SIMPLE 简单类型，可以发送html格式文本,遵从freemarker模板的设置
 * IMAGE html格式类型，同时会保存html里面的 image 到磁盘，并生成eml保存到服务器的配置目录 , 
 * MULTI 带附件的  Attachment，TODO
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
		if (null == mailSender) {
			mailSender = MailSenderHolder.INSTANCE;
		}
		return mailSender;
	}

	private Object readResolve() {
		return MailSenderHolder.INSTANCE;
	}

	public static enum MailType {
		SIMPLE, IMAGE /* html with image */, MULTI/* Attachment TODO */
	}

	private static final String CHARSET = "text/html;charset=UTF-8";
	private static final Logger LOGGER = Logger.getLogger(Markdowns.class);

	private static final boolean is_debug = Boolean.valueOf(Symphonys.get("isdebug"));
	private static final String mail_transport_protocol = Symphonys.get("mail.transport.protocol");
	private static final String mail_host = Symphonys.get("mail.host");
	private static final String mail_port = Symphonys.get("mail.port");
	private static final boolean mail_smtp_auth = Boolean.valueOf(Symphonys.get("mail.smtp.auth"));
	private static final boolean mail_smtp_ssl = Boolean.valueOf(Symphonys.get("mail.smtp.ssl"));
	public static final String sender = Symphonys.get("mail.smtp.sender");
	public static final String username = Symphonys.get("mail.smtp.username");
	private static final String password = Symphonys.get("mail.smtp.passsword");/* 换成自己的密码哦 */
	private static final String saved_path = Symphonys.get("saved.eml.path");
	private static Properties prop = new Properties();

	private MailSender() {
		prop.setProperty("mail.transport.protocol", mail_transport_protocol);
		prop.setProperty("mail.host", mail_host);
		prop.setProperty("mail.port", mail_port);
		prop.setProperty("mail.smtp.auth", Symphonys.get("mail.smtp.auth"));
		prop.setProperty("mail.smtp.ssl", Symphonys.get("mail.smtp.ssl"));
		prop.setProperty("mail.smtp.sender", sender);
		prop.setProperty("mail.smtp.username", username);
		prop.setProperty("mail.smtp.passsword", password);
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
		ts.connect(mail_host, username, password);
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
	 * @Method: createTextMail
	 * @Description: 创建一封只包含文本的邮件
	 * @Anthor:snowflake
	 *
	 * @param session
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
	 * @Method: createImageMail
	 * @Description: 生成一封邮件正文带图片的邮件
	 * @Anthor:snowflake
	 *
	 * @param session
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

		sendHTML(fromName, subject, new String[] { toMailSingle }, html);

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

}

/**
 *加载网络资源图片保存到本地
 * 
 * @author snowflake
 * @version 1.0.0.0, Mar 9, 2016
 * @since 2.0.0
 *
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
	 * @param map
	 *            The FileTypeMap for this object.
	 */
	public void setFileTypeMap(FileTypeMap map) {
		typeMap = map;
	}

}
