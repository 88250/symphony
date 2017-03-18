package org.b3log.symphony.util;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.util.MD5;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MP3 player utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 19, 2017
 * @since 2.1.0
 */
public final class MP3Players {

    /**
     * Renders the specified content with MP3 player if need.
     *
     * @param content the specified content
     * @return rendered content
     */
    public static final String render(final String content) {
        final StringBuffer contentBuilder = new StringBuffer();
        final String MP3_URL_REGEX = "<p>( )*<a href.*\\.mp3.*</a>( )*</p>";
        final Pattern p = Pattern.compile(MP3_URL_REGEX);
        final Matcher m = p.matcher(content);

        final String id = MD5.hash(content);

        int i = 0;
        while (m.find()) {
            String mp3URL = m.group();
            String mp3Name = StringUtils.substringBetween(mp3URL, "\">", ".mp3</a>");
            mp3URL = StringUtils.substringBetween(mp3URL, "href=\"", "\" rel=");
            final String playerId = "player" + id + i++;

            m.appendReplacement(contentBuilder, "<div id=\"" + playerId + "\" class=\"aplayer\"></div>\n"
                    + "<script>\n"
                    + "new APlayer({\n"
                    + "    element: document.getElementById('" + playerId + "'),\n"
                    + "    narrow: false,\n"
                    + "    autoplay: false,\n"
                    + "    showlrc: false,\n"
                    + "    mutex: true,\n"
                    + "    theme: '#e6d0b2',\n"
                    + "    music: {\n"
                    + "        title: '" + mp3Name + "',\n"
                    + "        author: '" + mp3URL + "',\n"
                    + "        url: '" + mp3URL + "',\n"
                    + "        pic: '" + Latkes.getStaticServePath() + "/images/sym-logo300.png'\n"
                    + "    }\n"
                    + "});\n"
                    + "</script>");
        }
        m.appendTail(contentBuilder);

        return contentBuilder.toString();
    }

    private MP3Players() {}
}
