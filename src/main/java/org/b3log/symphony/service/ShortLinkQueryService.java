/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
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
package org.b3log.symphony.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
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

/**
 * Short link query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.5.1, May 10, 2016
 * @since 1.3.0
 */
@Service
public class ShortLinkQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ShortLinkQueryService.class.getName());

    /**
     * Article pattern - simple.
     */
    private static final Pattern ARTICLE_PATTERN_SIMPLE = Pattern.compile(" \\[\\d{13,15}\\] ");

    /**
     * Article pattern - full.
     */
    private static final Pattern ARTICLE_PATTERN_FULL
            = Pattern.compile("(?:^|[^\"'\\](])(" + Latkes.getServePath() + "/article/\\d{13,15}(\\b|$))");

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

            try {
                while (matcher.find()) {
                    final String linkId = StringUtils.substringAfter(matcher.group(), "/article/");

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

                    final Query query = new Query().addProjection(Tag.TAG_TITLE, String.class)
                            .setFilter(new PropertyFilter(Tag.TAG_TITLE, FilterOperator.EQUAL, linkTagTitle));
                    final JSONArray results = tagRepository.get(query).optJSONArray(Keys.RESULTS);
                    if (0 == results.length()) {
                        continue;
                    }

                    final JSONObject linkTag = results.optJSONObject(0);

                    final String linkTitle = linkTag.optString(Tag.TAG_TITLE);
                    final String link = " [" + linkTitle + "](" + Latkes.getServePath() + "/tag/" + linkTitle + ") ";

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
