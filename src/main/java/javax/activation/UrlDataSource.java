package javax.activation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 从网络上获取图片资源并保存至本地
 * 另参见 从磁盘上获取文件 javax.activation.FileDataSource
 * 
 * @author snowflake
 * @version 1.0.0.0, Mar 9, 2016
 * @since 2.0.0
 * 
 *
 */
public class UrlDataSource implements DataSource {

	private String urlPath;
	private URL url;
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
		this.url = url;
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
