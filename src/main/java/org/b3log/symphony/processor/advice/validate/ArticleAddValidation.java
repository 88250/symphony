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
package org.b3log.symphony.processor.advice.validate;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import org.b3log.latke.Keys;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.json.JSONObject;

/**
 * Validates for article adding locally, removes the duplicated tags.
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.4, Apr 18, 2013
 * @since 0.2.0
 */
public final class ArticleAddValidation extends BeforeRequestProcessAdvice {

    /**
     * Language service.
     */
    private static LangPropsService langPropsService = LangPropsService.getInstance();
    /**
     * Max article title length.
     */
    public static final int MAX_ARTICLE_TITLE_LENGTH = 255;
    /**
     * Max tag title length.
     */
    public static final int MAX_TAG_TITLE_LENGTH = 50;
    /**
     * Max article content length.
     */
    public static final int MAX_ARTICLE_CONTENT_LENGTH = 1048576;
    /**
     * Min article content length.
     */
    public static final int MIN_ARTICLE_CONTENT_LENGTH = 4;

    @Override
    public void doAdvice(final HTTPRequestContext context, final Map<String, Object> args) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }
        
        validateArticleFields(requestJSONObject);
    }

    /**
     * Validates article fields.
     * 
     * @param requestJSONObject the specified request object
     * @throws RequestProcessAdviceException 
     */
    public static void validateArticleFields(final JSONObject requestJSONObject) throws RequestProcessAdviceException {
        final String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        if (Strings.isEmptyOrNull(articleTitle) || articleTitle.length() > MAX_ARTICLE_TITLE_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTitleErrorLabel")));
        }

        final String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
        if (Strings.isEmptyOrNull(articleTags)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTagsErrorLabel")));
        }

        String[] tagTitles = articleTags.split(",");
        if (null == tagTitles || 0 == tagTitles.length) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTagsErrorLabel")));
        }
        
        tagTitles = new TreeSet<String>(Arrays.asList(tagTitles)).toArray(new String[0]);
        
        final StringBuilder tagBuilder = new StringBuilder();
        for (int i = 0; i < tagTitles.length; i++) {
            final String tagTitle = tagTitles[i].trim();
            if (Strings.isEmptyOrNull(tagTitle)) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTagsErrorLabel")));
            }

            if (Strings.isEmptyOrNull(tagTitle) || tagTitle.length() > MAX_TAG_TITLE_LENGTH || tagTitle.length() < 1) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTagsErrorLabel")));
            }
            
            // XXX: configured
            if ("B3log Broadcast".equals(tagTitle)) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTagReservedLabel")
                        + " [B3log Broadcast]"));
            }
            
            tagBuilder.append(tagTitle).append(",");
        }
        
        tagBuilder.deleteCharAt(tagBuilder.length() - 1);
        requestJSONObject.put(Article.ARTICLE_TAGS, tagBuilder.toString());

        final String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        if (Strings.isEmptyOrNull(articleContent) || articleContent.length() > MAX_ARTICLE_CONTENT_LENGTH
            || articleContent.length() < MIN_ARTICLE_CONTENT_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleContentErrorLabel")));
        }
    }
}
