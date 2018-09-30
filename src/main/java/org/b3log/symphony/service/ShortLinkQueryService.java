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
package org.b3log.symphony.service;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.TagRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Short link query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.2.3.0, Sep 30, 2018
 * @since 1.3.0
 */
@Service
public class ShortLinkQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ShortLinkQueryService.class);

    /**
     * Article pattern - simple.
     */
    private static final Pattern ARTICLE_PATTERN_SIMPLE = Pattern.compile(" \\[\\d{13,15}\\] ");

    /**
     * Article pattern - full.
     */
    private static final Pattern ARTICLE_PATTERN_FULL = Pattern.compile(Latkes.getServePath() + "/article/\\d{13,15}[?\\w&-_=#%:]*(\\b|$)");

    /**
     * Tag title pattern.
     */
    private static final Pattern TAG_PATTERN = Pattern.compile(" \\[" + Tag.TAG_TITLE_PATTERN_STR + "\\](?!\\(.+\\)) ");

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * Tag repository.
     */
    @Inject
    private TagRepository tagRepository;

    /**
     * Processes article short link (article id).
     *
     * @param content the specified content
     * @return processed content
     */
    public String linkArticle(final String content) {
        Stopwatchs.start("Link article");

        StringBuffer contentBuilder = new StringBuffer();
        try {
            Matcher matcher = ARTICLE_PATTERN_FULL.matcher(content);
            final String[] codeBlocks = StringUtils.substringsBetween(content, "```", "```");
            String codes = "";
            if (null != codeBlocks) {
                codes = String.join("", codeBlocks);
            }
            try {
                while (matcher.find()) {
                    final String url = StringUtils.trim(matcher.group());
                    if (0 < matcher.start()) {
                        final char c = content.charAt(matcher.start() - 1); // look back one char
                        if ('(' == c || ']' == c || '\'' == c || '"' == c) {
                            continue;
                        }
                    }

                    if (StringUtils.containsIgnoreCase(codes, url)) {
                        continue;
                    }
                    String linkId;
                    String queryStr = null;
                    String anchor = null;
                    if (StringUtils.contains(url, "?")) {
                        linkId = StringUtils.substringBetween(url, "/article/", "?");
                        queryStr = StringUtils.substringAfter(url, "?");
                    } else {
                        linkId = StringUtils.substringAfter(url, "/article/");
                    }
                    if (StringUtils.contains(url, "#")) {
                        linkId = StringUtils.substringBefore(linkId, "#");
                        anchor = StringUtils.substringAfter(url, "#");
                    }

                    final Query query = new Query().addProjection(Article.ARTICLE_TITLE, String.class)
                            .setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, linkId));
                    final JSONArray results = articleRepository.get(query).optJSONArray(Keys.RESULTS);
                    if (0 == results.length()) {
                        continue;
                    }

                    final JSONObject linkArticle = results.optJSONObject(0);
                    final String linkTitle = linkArticle.optString(Article.ARTICLE_TITLE);
                    String link = " [" + linkTitle + "](" + Latkes.getServePath() + "/article/" + linkId;
                    if (StringUtils.isNotBlank(queryStr)) {
                        link += "?" + queryStr;
                    }
                    if (StringUtils.isNotBlank(anchor)) {
                        link += "#" + anchor;
                    }
                    link += ") ";

                    matcher.appendReplacement(contentBuilder, link);
                }

                matcher.appendTail(contentBuilder);
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Generates article link error", e);
            }

            matcher = ARTICLE_PATTERN_SIMPLE.matcher(contentBuilder.toString());
            contentBuilder = new StringBuffer();

            try {
                while (matcher.find()) {
                    final String linkId = StringUtils.substringBetween(matcher.group(), "[", "]");

                    final Query query = new Query().addProjection(Article.ARTICLE_TITLE, String.class)
                            .setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, linkId));
                    final JSONArray results = articleRepository.get(query).optJSONArray(Keys.RESULTS);
                    if (0 == results.length()) {
                        continue;
                    }

                    final JSONObject linkArticle = results.optJSONObject(0);
                    final String linkTitle = linkArticle.optString(Article.ARTICLE_TITLE);
                    final String link = " [" + linkTitle + "](" + Latkes.getServePath() + "/article/" + linkId + ") ";

                    matcher.appendReplacement(contentBuilder, link);
                }

                matcher.appendTail(contentBuilder);
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Generates article link error", e);
            }

            return contentBuilder.toString();
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Processes tag short link (tag id).
     *
     * @param content the specified content
     * @return processed content
     */
    public String linkTag(final String content) {
        Stopwatchs.start("Link tag");

        try {
            final Matcher matcher = TAG_PATTERN.matcher(content);
            final StringBuffer contentBuilder = new StringBuffer();

            try {
                while (matcher.find()) {
                    final String linkTagTitle = StringUtils.substringBetween(matcher.group(), "[", "]");

                    if (StringUtils.equals(linkTagTitle, "x")) { // [x] => <input checked>
                        continue;
                    }

                    final Query query = new Query().addProjection(Tag.TAG_TITLE, String.class)
                            .addProjection(Tag.TAG_URI, String.class)
                            .setFilter(new PropertyFilter(Tag.TAG_TITLE, FilterOperator.EQUAL, linkTagTitle));
                    final JSONArray results = tagRepository.get(query).optJSONArray(Keys.RESULTS);
                    if (0 == results.length()) {
                        continue;
                    }

                    final JSONObject linkTag = results.optJSONObject(0);

                    final String linkTitle = linkTag.optString(Tag.TAG_TITLE);
                    final String linkURI = linkTag.optString(Tag.TAG_URI);
                    final String link = " [" + linkTitle + "](" + Latkes.getServePath() + "/tag/" + linkURI + ") ";

                    matcher.appendReplacement(contentBuilder, link);
                }
                matcher.appendTail(contentBuilder);
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Generates tag link error", e);
            }

            return contentBuilder.toString();
        } finally {
            Stopwatchs.end();
        }
    }
}
