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
package org.b3log.symphony.processor.advice.validate;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.b3log.latke.Keys;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.ArticleQueryService;
import org.json.JSONObject;

/**
 * Validates for show article update.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 11, 2013
 * @since 0.2.0
 */
@Named
@Singleton
public class ShowArticleUpdateValidation extends BeforeRequestProcessAdvice {

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    @Override
    public void doAdvice(final HTTPRequestContext context, final Map<String, Object> args) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        JSONObject article = null;
        try {
            final String articleId = request.getParameter("id");
            if (Strings.isEmptyOrNull(articleId)) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("updateArticleNotFoundLabel")));
            }

            article = articleQueryService.getArticleById(UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, articleId);
            if (null == article) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("updateArticleNotFoundLabel")));
            }
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }

        request.setAttribute(Article.ARTICLE, article);
    }
}
