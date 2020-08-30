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
package org.b3log.symphony.processor;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.renderer.RssRenderer;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.util.Locales;
import org.b3log.symphony.Server;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.feed.RSSCategory;
import org.b3log.symphony.model.feed.RSSChannel;
import org.b3log.symphony.model.feed.RSSItem;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.DomainQueryService;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.ShortLinkQueryService;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Feed RSS processor.
 *
 * <ul>
 * <li>Generates recent articles' RSS (/rss/recent.xml), GET/HEAD</li>
 * <li>Generates domain articles' RSS (/rss/domain/{domainURL}.xml), GET/HEAD</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 3.1.0
 */
@Singleton
public class FeedProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(FeedProcessor.class);

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Domain query service.
     */
    @Inject
    private DomainQueryService domainQueryService;

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Generates recent articles' RSS.
     *
     * @param context the specified context
     */
    public void genRecentRSS(final RequestContext context) {
        final RssRenderer renderer = new RssRenderer();
        context.setRenderer(renderer);

        try {
            final RSSChannel channel = new RSSChannel();
            final JSONObject result = articleQueryService.getRecentArticles(0, 1, Symphonys.ARTICLE_LIST_CNT);
            final List<JSONObject> articles = (List<JSONObject>) result.get(Article.ARTICLES);
            for (int i = 0; i < articles.size(); i++) {
                RSSItem item = getItem(articles, i);
                channel.addItem(item);
            }
            channel.setTitle(langPropsService.get("symphonyLabel"));
            channel.setLastBuildDate(new Date());
            channel.setLink(Latkes.getServePath());
            channel.setAtomLink(Latkes.getServePath() + "/rss/recent.xml");
            channel.setGenerator("Symphony v" + Server.VERSION + ", https://b3log.org/sym");
            final String localeString = optionQueryService.getOption("miscLanguage").optString(Option.OPTION_VALUE);
            final String country = Locales.getCountry(localeString).toLowerCase();
            final String language = Locales.getLanguage(localeString).toLowerCase();
            channel.setLanguage(language + '-' + country);
            channel.setDescription(langPropsService.get("symDescriptionLabel"));

            renderer.setContent(channel.toString());
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Generates recent articles' RSS failed", e);
            context.getResponse().sendError(500);
        }
    }

    /**
     * Generates domain articles' RSS.
     *
     * @param context the specified context
     */
    public void genDomainRSS(final RequestContext context) {
        final String domainURI = context.pathVar("domainURI");
        final RssRenderer renderer = new RssRenderer();
        context.setRenderer(renderer);

        try {
            final JSONObject domain = domainQueryService.getByURI(domainURI);
            if (null == domain) {
                context.getResponse().sendError(404);
                return;
            }

            final RSSChannel channel = new RSSChannel();
            final String domainId = domain.optString(Keys.OBJECT_ID);
            final JSONObject result = articleQueryService.getDomainArticles(domainId, 1, Symphonys.ARTICLE_LIST_CNT);
            final List<JSONObject> articles = (List<JSONObject>) result.get(Article.ARTICLES);
            for (int i = 0; i < articles.size(); i++) {
                RSSItem item = getItem(articles, i);
                channel.addItem(item);
            }
            channel.setTitle(langPropsService.get("symphonyLabel"));
            channel.setLastBuildDate(new Date());
            channel.setLink(Latkes.getServePath());
            channel.setAtomLink(Latkes.getServePath() + "/rss/" + domainURI + ".xml");
            channel.setGenerator("Symphony v" + Server.VERSION + ", https://b3log.org/sym");
            final String localeString = optionQueryService.getOption("miscLanguage").optString(Option.OPTION_VALUE);
            final String country = Locales.getCountry(localeString).toLowerCase();
            final String language = Locales.getLanguage(localeString).toLowerCase();
            channel.setLanguage(language + '-' + country);
            channel.setDescription(langPropsService.get("symDescriptionLabel"));

            renderer.setContent(channel.toString());
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Generates recent articles' RSS failed", e);

            context.getResponse().sendError(500);
        }
    }

    private RSSItem getItem(final List<JSONObject> articles, int i) throws org.json.JSONException {
        final JSONObject article = articles.get(i);
        final RSSItem ret = new RSSItem();
        String title = article.getString(Article.ARTICLE_TITLE);
        title = Emotions.toAliases(title);
        ret.setTitle(title);
        String description = article.getString(Article.ARTICLE_CONTENT);
        description = shortLinkQueryService.linkArticle(description);
        description = Emotions.toAliases(description);
        description = Emotions.convert(description);
        description = Markdowns.toHTML(description);
        ret.setDescription(description);
        final Date pubDate = (Date) article.get(Article.ARTICLE_UPDATE_TIME);
        ret.setPubDate(pubDate);
        final String link = Latkes.getServePath() + article.getString(Article.ARTICLE_PERMALINK);
        ret.setLink(link);
        ret.setGUID(link);
        ret.setAuthor(article.optString(Article.ARTICLE_T_AUTHOR_NAME));
        final String tagsString = article.getString(Article.ARTICLE_TAGS);
        final String[] tagStrings = tagsString.split(",");
        for (final String tagString : tagStrings) {
            final RSSCategory catetory = new RSSCategory();
            ret.addCatetory(catetory);
            catetory.setTerm(tagString);
        }

        return ret;
    }
}
