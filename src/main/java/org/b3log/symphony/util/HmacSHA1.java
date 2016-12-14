package org.b3log.symphony.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class HmacSHA1 {
	private final static String AGLORITHM_NAME = "HmacSHA1";
	public final static String URL_ENCODING = "UTF-8";

	public static String signString(String source, String accessSecret)
			throws InvalidKeyException, IllegalStateException {
		try {
			Mac mac = Mac.getInstance(AGLORITHM_NAME);
			mac.init(new SecretKeySpec(accessSecret
					.getBytes(URL_ENCODING), AGLORITHM_NAME));
			byte[] signData = mac.doFinal(source
					.getBytes(URL_ENCODING));
			return new String(Base64.encodeBase64(signData),URL_ENCODING);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("HMAC-SHA1 not supported.");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 not supported.");
		}
	}
}
