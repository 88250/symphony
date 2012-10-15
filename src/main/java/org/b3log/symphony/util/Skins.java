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
package org.b3log.symphony.util;

import freemarker.cache.NullCacheStorage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.util.freemarker.Templates;
import org.b3log.symphony.SymphonyServletListener;
import org.b3log.symphony.model.Skin;

/**
 * Skin utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Aug 6, 2012
 * @since 0.2.0
 */
public final class Skins {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Skins.class.getName());

    /**
     * Loads skin.
     */
    public static void loadSkin() {
        LOGGER.config("Loading skin....");

        final String skinDirName = Symphonys.get("skinDirName");
        final String skinName = getSkinName(skinDirName);
        LOGGER.log(Level.INFO, "Current skin[name={0}]", skinName);

        try {
            final String webRootPath = SymphonyServletListener.getWebRoot();
            final String skinPath = webRootPath + Skin.SKINS + "/" + skinDirName;
            Templates.MAIN_CFG.setDirectoryForTemplateLoading(new File(skinPath));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final boolean enablePageCache = Symphonys.getBoolean("enablePageCache");
        Templates.enableCache(enablePageCache);
        LOGGER.log(Level.INFO, "{0} template caching", enablePageCache ? "Enabled" : "Disabled");
        if (!enablePageCache) {
            Templates.MAIN_CFG.setCacheStorage(new NullCacheStorage());
            Templates.MOBILE_CFG.setCacheStorage(new NullCacheStorage());
        }

        TimeZones.setTimeZone("Asia/Shanghai");

        LOGGER.info("Loaded skins....");
    }

    /**
     * Gets the skin name for the specified skin directory name. The skin name
     * was configured in skin.properties file({@code name} as the key) under
     * skin directory specified by the given skin directory name.
     *
     * @param skinDirName the given skin directory name
     * @return skin name, returns {@code null} if not found or error occurs
     * @see #getSkinDirNames()
     */
    private static String getSkinName(final String skinDirName) {
        final String webRootPath = SymphonyServletListener.getWebRoot();
        final File skins = new File(webRootPath + "skins/");
        final File[] skinDirs = skins.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pathname.isDirectory() && pathname.getName().equals(skinDirName) ? true : false;
            }
        });

        if (null == skinDirs) {
            LOGGER.severe("Skin directory is null");

            return null;
        }

        if (1 != skinDirs.length) {
            LOGGER.log(Level.SEVERE, "Skin directory count[{0}]", skinDirs.length);

            return null;
        }

        try {
            final Properties ret = new Properties();
            final String skinPropsPath = skinDirs[0].getPath() + "/" + "skin.properties";
            ret.load(new FileReader(skinPropsPath));

            return ret.getProperty("name");
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Read skin configuration error[msg={0}]", e.getMessage());

            return null;
        }
    }

    /**
     * Private skins.
     */
    private Skins() {
    }
}
