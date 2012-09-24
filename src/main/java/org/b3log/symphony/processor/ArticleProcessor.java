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

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.annotation.RequestProcessing;
import org.b3log.latke.annotation.RequestProcessor;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;

/**
 * Article processor.
 * 
 * <p>
 * For article
 *   <ul>
 *     <li>Adding (/article) <em>locally</em>, PUT</li>
 *     <li>Adding (/rhythm/article) <em>remotely</em>, PUT</li>
 *   </ul>
 * </p>
 * 
 * <p>
 * The '<em>locally</em>' means user post an article on Symphony directly rather than receiving an article from 
 * externally (for example Rhythm).
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 23, 2012
 * @since 0.2.0
 */
@RequestProcessor
public class ArticleProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleProcessor.class.getName());
    /**
     * User management service.
     */
    private UserMgmtService userMgmtService = UserMgmtService.getInstance();
    /**
     * User query service.
     */
    private UserQueryService userQueryService = UserQueryService.getInstance();
    /**
     * Language service.
     */
    private LangPropsService langPropsService = LangPropsService.getInstance();

    /**
     * Adds an article locally.
     * 
     * <p>
     * The request json object (a post): 
     * <pre>
     * {
     *   "articleTitle": "",
     *   "articleTags": "", // Tags spliting by ','
     *   "articleContent": "",
     *   "articleEditorType": int
     * }
     * </pre>
     * </p>
     * 
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws IOException io exception 
     */
    @RequestProcessing(value = "/article", method = HTTPRequestMethod.PUT)
    public void addArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);

        final String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        final String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
        final String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        final int articleEditorType = requestJSONObject.optInt(Article.ARTICLE_EDITOR_TYPE);
        
        // TODO: add article validate

        final JSONObject article = new JSONObject();
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_TAGS, articleTags);
        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_EDITOR_TYPE, articleEditorType);
        
        final JSONObject currentUser = LoginProcessor.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        final String authorEmail = currentUser.optString(User.USER_EMAIL);
        article.put(Article.ARTICLE_AUTHOR_EMAIL, authorEmail);
        
        final long currentTimeMillis = System.currentTimeMillis();
        
        article.put(Article.ARTICLE_COMMENT_CNT, 0);
        article.put(Article.ARTICLE_VIEW_CNT, 0);
        article.put(Article.ARTICLE_COMMENTABLE, true);
        article.put(Article.ARTICLE_CREATE_TIME, currentTimeMillis);
        article.put(Article.ARTICLE_PERMALINK, "TODO permalink");
        article.put(Article.ARTICLE_RANDOM_DOUBLE, Math.random());
        article.put(Article.ARTICLE_STATUS, 0);
        article.put(Article.ARTICLE_UPDATE_TIME, 0);
        article.put(Article.ARTICLE_UPDATE_TIME, currentTimeMillis);
        

    }

    /**
     * Adds an article remotely.
     * 
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws IOException io exception 
     */
    @RequestProcessing(value = "/rhythm/article", method = HTTPRequestMethod.PUT)
    public void addArticleFromRhythm(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        final String requestURI = request.getRequestURI();

        final String userName = requestURI.substring("/home/".length());

    }
}
