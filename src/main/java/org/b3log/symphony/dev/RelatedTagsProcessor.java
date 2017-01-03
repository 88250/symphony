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
package org.b3log.symphony.dev;

import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeMode;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.Query;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.repository.TagTagRepository;
import org.b3log.symphony.service.TagMgmtService;
import org.json.JSONObject;

/**
 * Generates tag-tag relations.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, May 31, 2015
 * @since 1.3.0
 */
@RequestProcessor
public class RelatedTagsProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(RelatedTagsProcessor.class.getName());

    /**
     * Tag-Tag repository.
     */
    @Inject
    private TagTagRepository tagTagRepository;

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
     * Tag management service.
     */
    @Inject
    private TagMgmtService tagMgmtService;

    /**
     * Generates tag-tag relations by existing articles.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws IOException io exception
     */
    @RequestProcessing(value = "/dev/tag-tag/gen", method = HTTPRequestMethod.GET)
    public void genTagRelations(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        if (RuntimeMode.PRODUCTION == Latkes.getRuntimeMode()) {
            return;
        }

        try {
            final JSONObject result = articleRepository.get(new Query());
            final List<JSONObject> articles = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            for (final JSONObject article : articles) {
                final String title = article.optString(Article.ARTICLE_TITLE);
                final String tagString = article.optString(Article.ARTICLE_TAGS);

                tagMgmtService.relateTags(tagString);

                LOGGER.log(Level.INFO, "Article [title={0}, tags={1}]", title, tagString);
            }

            response.sendRedirect("/");
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Generates tag-tag relations failed", e);
            throw new IOException("Generates tag-tag relations failed", e);
        }
    }
}
