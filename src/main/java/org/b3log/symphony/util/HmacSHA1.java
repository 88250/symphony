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

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * HmacSHA1 utilities.
 *
 * @author <a href="http://blog.thinkjava.top">VirutalPier</a>
 * @version 1.0.0.0, Dec 15, 2016
 * @since 1.8.0
 */
public final class HmacSHA1 {
    private final static String AGLORITHM_NAME = "HmacSHA1";
    public final static String URL_ENCODING = "UTF-8";

    public static String signString(String source, String accessSecret)
            throws InvalidKeyException, IllegalStateException {
        try {
            Mac mac = Mac.getInstance(AGLORITHM_NAME);
            mac.init(new SecretKeySpec(accessSecret.getBytes(URL_ENCODING), AGLORITHM_NAME));
            byte[] signData = mac.doFinal(source.getBytes(URL_ENCODING));

            return new String(Base64.encodeBase64(signData), URL_ENCODING);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("HMAC-SHA1 not supported.");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported.");
        }
    }

    private HmacSHA1() {
    }
}
