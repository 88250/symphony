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
package org.b3log.symphony.processor.advice.validate;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.b3log.latke.Keys;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Client;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;

/**
 * Validates for comment adding remotely.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.3, Apr 3, 2015
 * @since 0.2.0
 */
@Named
@Singleton
public class ClientCommentAddValidation extends BeforeRequestProcessAdvice {

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

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Max client name length.
     */
    public static final int MAX_CLIENT_NAME_LENGTH = 10;

    /**
     * Max client version length.
     */
    public static final int MAX_CLIENT_VER_LENGTH = 10;

    /**
     * Max client host length.
     */
    public static final int MAX_CLIENT_HOST_LENGTH = 50;

    /**
     * Max client runtime length.
     */
    public static final int MAX_CLIENT_RUNTIME_LENGTH = 10;

    /**
     * Max client comment id length.
     */
    public static final int MAX_CLIENT_COMMENT_ID_LENGTH = 32;

    /**
     * Max client comment article id length.
     */
    public static final int MAX_CLIENT_CMT_ARTICLE_ID_LENGTH = 32;

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

        final String userB3Key = requestJSONObject.optString(UserExt.USER_B3_KEY);
        if (Strings.isEmptyOrNull(userB3Key) || userB3Key.length() > UpdateSyncB3Validation.MAX_USER_B3_KEY_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Wrong B3 key [" + userB3Key + "]"));
        }

        final String clientAdminEmail = requestJSONObject.optString(Client.CLIENT_ADMIN_EMAIL);

        JSONObject author;
        try {
            author = userQueryService.getUserByEmail(clientAdminEmail);
            if (null == author) {
                throw new RequestProcessAdviceException(
                        new JSONObject().put(Keys.MSG, "User not exists [clientAdminEmail=" + clientAdminEmail + "]"));
            }

            if (!author.optString(UserExt.USER_B3_KEY).equals(userB3Key)) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Wrong B3 key [client=" + userB3Key + ", sym="
                        + author.optString(UserExt.USER_B3_KEY) + "]"));
            }
        } catch (final ServiceException e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Unknown Error"));
        }

        final String clientName = requestJSONObject.optString(Client.CLIENT_NAME);
        if (Strings.isEmptyOrNull(clientName) || clientName.length() > MAX_CLIENT_NAME_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Client name [" + clientName + "] too long"));
        }

        final String clientVersion = requestJSONObject.optString(Client.CLIENT_VERSION);
        if (Strings.isEmptyOrNull(clientVersion) || clientVersion.length() > MAX_CLIENT_VER_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Client version [" + clientVersion + "] too long"));
        }

        final String clientHost = requestJSONObject.optString(Client.CLIENT_HOST);
        if (Strings.isEmptyOrNull(clientHost) || clientHost.length() > MAX_CLIENT_HOST_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Client host [" + clientHost + "]too long"));
        }

        final String clientRuntimeEnv = requestJSONObject.optString(Client.CLIENT_RUNTIME_ENV);
        if (Strings.isEmptyOrNull(clientRuntimeEnv) || clientRuntimeEnv.length() > MAX_CLIENT_RUNTIME_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Client runtime [" + clientRuntimeEnv + "]too long"));
        }

        final JSONObject originalCmt = requestJSONObject.optJSONObject(Comment.COMMENT);
        if (null == originalCmt) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Comment is null"));
        }

        final String clientCommentId = originalCmt.optString(Comment.COMMENT_T_ID);
        if (Strings.isEmptyOrNull(clientCommentId) || clientCommentId.length() > MAX_CLIENT_COMMENT_ID_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Comment id length <= 32"));
        }

        final String commentContent = originalCmt.optString(Comment.COMMENT_CONTENT);
        if (Strings.isEmptyOrNull(commentContent) || commentContent.length() > CommentAddValidation.MAX_COMMENT_CONTENT_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("commentErrorLabel")));
        }

        final String commentClientArticleId = originalCmt.optString(Article.ARTICLE_T_ID);
        if (Strings.isEmptyOrNull(commentClientArticleId) || commentClientArticleId.length() > MAX_CLIENT_CMT_ARTICLE_ID_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Comment article id length <= 32"));
        }

        try {
            final JSONObject article
                    = articleQueryService.getArticleByClientArticleId(author.optString(Keys.OBJECT_ID), commentClientArticleId);
            if (null == article) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Article not found, do not sync comment"));
            }

            if (!article.optBoolean(Article.ARTICLE_COMMENTABLE)) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Article not allow comment, do not sync comment"));
            }

            request.setAttribute(Article.ARTICLE, article);
        } catch (final ServiceException e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Unknown Error"));
        }
    }
}
