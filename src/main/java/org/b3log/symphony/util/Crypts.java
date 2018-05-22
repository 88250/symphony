/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * Cryptology utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.3, May 21, 2018
 * @since 1.0.0
 */
public final class Crypts {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Crypts.class);

    /**
     * Signs the specified source string using the specified secret.
     *
     * @param source the specified source string
     * @param secret the specified secret
     * @return signed string
     */
    public static String signHmacSHA1(final String source, final String secret) {
        try {
            final Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA1"));
            final byte[] signData = mac.doFinal(source.getBytes("UTF-8"));

            return new String(Base64.encodeBase64(signData), "UTF-8");
        } catch (final Exception e) {
            throw new RuntimeException("HMAC-SHA1 sign failed", e);
        }
    }

    /**
     * Encrypts by AES.
     *
     * @param content the specified content to encrypt
     * @param key     the specified key
     * @return encrypted content
     * @see #decryptByAES(java.lang.String, java.lang.String)
     */
    public static String encryptByAES(final String content, final String key) {
        try {
            final KeyGenerator kgen = KeyGenerator.getInstance("AES");
            final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(key.getBytes());
            kgen.init(128, secureRandom);
            final SecretKey secretKey = kgen.generateKey();
            final byte[] enCodeFormat = secretKey.getEncoded();
            final SecretKeySpec keySpec = new SecretKeySpec(enCodeFormat, "AES");
            final Cipher cipher = Cipher.getInstance("AES");
            final byte[] byteContent = content.getBytes("UTF-8");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            final byte[] result = cipher.doFinal(byteContent);

            return Hex.encodeHexString(result);
        } catch (final Exception e) {
            LOGGER.log(Level.WARN, "Encrypt failed", e);

            return null;
        }
    }

    /**
     * Decrypts by AES.
     *
     * @param content the specified content to decrypt
     * @param key     the specified key
     * @return original content
     * @see #encryptByAES(java.lang.String, java.lang.String)
     */
    public static String decryptByAES(final String content, final String key) {
        try {
            final byte[] data = Hex.decodeHex(content.toCharArray());
            final KeyGenerator kgen = KeyGenerator.getInstance("AES");
            final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(key.getBytes());
            kgen.init(128, secureRandom);
            final SecretKey secretKey = kgen.generateKey();
            final byte[] enCodeFormat = secretKey.getEncoded();
            final SecretKeySpec keySpec = new SecretKeySpec(enCodeFormat, "AES");
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            final byte[] result = cipher.doFinal(data);

            return new String(result, "UTF-8");
        } catch (final Exception e) {
            LOGGER.log(Level.WARN, "Decrypt failed");

            return null;
        }
    }

    private Crypts() {
    }
}
