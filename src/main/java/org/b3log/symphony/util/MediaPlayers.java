/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.service.LangPropsService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Media (audio, video) player utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, May 27, 2020
 * @since 3.6.3
 */
public final class MediaPlayers {

    /**
     * Video suffix.
     */
    private static final String VIDEO_SUFFIX = "rm|rmvb|3gp|avi|mpeg|mp4|wmv|mkv|dat|asf|flv|mov|webm";

    /**
     * Video URL regex.
     */
    private static final String VIDEO_URL_REGEX = "<p>( )*<a href.*\\.(" + VIDEO_SUFFIX + ").*</a>( )*</p>";

    /**
     * Video URL regex pattern.
     */
    private static final Pattern VIDEO_PATTERN = Pattern.compile(VIDEO_URL_REGEX, Pattern.CASE_INSENSITIVE);

    /**
     * Audio suffix.
     */
    private static final String AUDIO_SUFFIX = "mp3|flac";

    /**
     * Audio (.mp3, .flac) URL regex.
     */
    private static final String AUDIO_URL_REGEX = "<p>( )*<a href.*\\.(" + AUDIO_SUFFIX + ").*</a>( )*</p>";

    /**
     * Audio URL regex pattern.
     */
    private static final Pattern AUDIO_PATTERN = Pattern.compile(AUDIO_URL_REGEX, Pattern.CASE_INSENSITIVE);

    /**
     * Media suffix.
     */
    public static final String MEDIA_SUFFIX = VIDEO_SUFFIX + "|" + AUDIO_SUFFIX;

    /**
     * Checks whether the specified src is a media resource.
     *
     * @param src the specified src
     * @return {@code true} if it is a media resource, returns {@code false} otherwise
     */
    public static boolean isMedia(final String src) {
        final String[] suffixes = StringUtils.split(MEDIA_SUFFIX, "|");
        for (final String suffix : suffixes) {
            if (StringUtils.endsWithIgnoreCase(src, "." + suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the specified a tag could be rendered by Vditor.
     *
     * @param a the specified a tag
     * @return {@code true} if could be, returns {@code false} otherwise
     */
    public static boolean isVditorRender(final String a) {
        return StringUtils.containsIgnoreCase(a, "v.qq.com") ||
                StringUtils.containsIgnoreCase(a, "youtube.com") ||
                StringUtils.containsIgnoreCase(a, "youtu.be") ||
                StringUtils.containsIgnoreCase(a, "youku.com") ||
                StringUtils.containsIgnoreCase(a, "coub.com") ||
                StringUtils.containsIgnoreCase(a, "dailymotion.com") ||
                StringUtils.containsIgnoreCase(a, "facebook.com/videos/");
    }

    /**
     * Renders the specified content with audio player if need.
     *
     * @param content the specified content
     * @return rendered content
     */
    public static String renderAudio(final String content) {
        final StringBuffer contentBuilder = new StringBuffer();

        final Matcher m = AUDIO_PATTERN.matcher(content);
        while (m.find()) {
            final String g = m.group();
            String audioName = StringUtils.substringBetween(g, "\">", "</a>");
            audioName = StringUtils.substringBeforeLast(audioName, ".");
            String audioURL = StringUtils.substringBetween(g, "href=\"", "\" rel=");
            if (StringUtils.isBlank(audioURL)) {
                audioURL = StringUtils.substringBetween(g, "href=\"", "\"");
            }

            m.appendReplacement(contentBuilder, "<div class=\"aplayer content-audio\" data-title=\""
                    + audioName + "\" data-url=\"" + audioURL + "\" ></div>\n");
        }
        m.appendTail(contentBuilder);
        return contentBuilder.toString();
    }

    /**
     * Renders the specified content with video player if need.
     *
     * @param content the specified content
     * @return rendered content
     */
    public static String renderVideo(final String content) {
        final BeanManager beanManager = BeanManager.getInstance();
        final LangPropsService langPropsService = beanManager.getReference(LangPropsService.class);

        final StringBuffer contentBuilder = new StringBuffer();
        final Matcher m = VIDEO_PATTERN.matcher(content);

        while (m.find()) {
            final String g = m.group();
            if (isVditorRender(g)) {
                continue;
            }

            String videoURL = StringUtils.substringBetween(g, "href=\"", "\" rel=");
            if (StringUtils.isBlank(videoURL)) {
                videoURL = StringUtils.substringBetween(g, "href=\"", "\"");
            }

            if (StringUtils.containsIgnoreCase(videoURL, "/forward")) {
                // 站外链接不渲染视频
                continue;
            }

            m.appendReplacement(contentBuilder, "<video width=\"100%\" src=\""
                    + videoURL + "\" controls=\"controls\">" + langPropsService.get("notSupportPlayLabel") + "</video>\n");
        }
        m.appendTail(contentBuilder);

        return contentBuilder.toString();
    }

    private MediaPlayers() {
    }
}
