/*
 * Copyright (c) 2012, B3log Team
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
package org.b3log.symphony.processor;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.b3log.latke.image.Image;
import org.b3log.latke.image.ImageService;
import org.b3log.latke.image.ImageServiceFactory;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.PNGRenderer;
import org.b3log.symphony.SymphonyServletListener;

/**
 * Captcha processor.
 * 
 * <p>
 *   See <a href="http://isend-blog.appspot.com/2010/03/25/captcha_on_GAE.html">
 *  在GAE上拼接生成图形验证码</a> for philosophy. Checkout
 *    <a href="http://toy-code.googlecode.com/svn/trunk/CaptchaGenerator">
 *    the sample captcha generator</a> for mor details.
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 2, 2012
 * @since 0.2.0
 */
@RequestProcessor
public final class CaptchaProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CaptchaProcessor.class.getName());
    /**
     * Length of captcha.
     */
    private static final int LENGTH = 4;
    /**
     * Images service.
     */
    private static final ImageService IMAGE_SERVICE = ImageServiceFactory.getImageService();
    /**
     * Random.
     */
    private static final Random RANDOM = new Random();
    /**
     * Key of captcha.
     */
    public static final String CAPTCHA = "captcha";
    /**
     * Maximum captcha row.
     */
    public static final int MAX_CAPTCHA_ROW = 10;
    /**
     * Maximum captcha column.
     */
    public static final int MAX_CAPTCHA_COLUM = 10;
    /**
     * Width of a captcha character.
     */
    public static final int WIDTH_CAPTCHA_CHAR = 13;
    /**
     * Height of a captcha character.
     */
    public static final int HEIGHT_CAPTCHA_CHAR = 20;
    /**
     * Captcha &lt;"imageName", Image&gt;.
     * For example &lt;"0/5.png", Image&gt;.
     */
    private static final Map<String, Image> CAPTCHAS = new HashMap<String, Image>();

    /**
     * Gets captcha.
     * 
     * @param context the specified context
     */
    @RequestProcessing(value = "/captcha", method = HTTPRequestMethod.GET)
    public void get(final HTTPRequestContext context) {
        final PNGRenderer renderer = new PNGRenderer();
        context.setRenderer(renderer);

        if (CAPTCHAS.isEmpty()) {
            loadCaptchas();
        }

        try {
            final String row = String.valueOf(RANDOM.nextInt(MAX_CAPTCHA_ROW));
            String captcha = "";
            final List<Image> images = new ArrayList<Image>();
            for (int i = 0; i < LENGTH; i++) {
                final String column = String.valueOf(RANDOM.nextInt(MAX_CAPTCHA_COLUM));
                captcha += column;
                final String imageName = row + "/" + column + ".png";
                final Image captchaChar = CAPTCHAS.get(imageName);

                images.add(captchaChar);
            }

            final Image captchaImage = IMAGE_SERVICE.makeImage(images);

            final HttpServletRequest request = context.getRequest();
            final HttpServletResponse response = context.getResponse();

            final HttpSession httpSession = request.getSession(false);
            if (null != httpSession) {
                LOGGER.log(Level.FINER, "Captcha[{0}] for session[id={1}]", new Object[]{captcha, httpSession.getId()});
                httpSession.setAttribute(CAPTCHA, captcha);
            }

            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);

            renderer.setImage(captchaImage);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Loads captcha.
     */
    private static void loadCaptchas() {
        LOGGER.info("Loading captchas....");
        try {
            final URL captchaURL = SymphonyServletListener.class.getClassLoader().getResource("captcha.zip");
            final ZipFile zipFile = new ZipFile(captchaURL.getFile());
            final Set<String> imageNames = new HashSet<String>();
            for (int row = 0; row < MAX_CAPTCHA_ROW; row++) {
                for (int column = 0; column < MAX_CAPTCHA_COLUM; column++) {
                    imageNames.add(row + "/" + column + ".png");
                }

            }

            final ImageService imageService = ImageServiceFactory.getImageService();

            final Iterator<String> i = imageNames.iterator();
            while (i.hasNext()) {
                final String imageName = i.next();
                final ZipEntry zipEntry = zipFile.getEntry(imageName);

                final BufferedInputStream bufferedInputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                final byte[] captchaCharData = new byte[bufferedInputStream.available()];
                bufferedInputStream.read(captchaCharData);
                bufferedInputStream.close();

                final Image captchaChar = imageService.makeImage(captchaCharData);

                CAPTCHAS.put(imageName, captchaChar);
            }

            zipFile.close();
        } catch (final Exception e) {
            LOGGER.severe("Can not load captchs!");

            throw new IllegalStateException(e);
        }

        LOGGER.info("Loaded captch images");
    }
}
