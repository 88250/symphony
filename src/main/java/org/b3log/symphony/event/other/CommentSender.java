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
package org.b3log.symphony.event.other;

import java.net.URL;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeMode;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.Lifecycle;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.HTTPHeader;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.URLFetchService;
import org.b3log.latke.urlfetch.URLFetchServiceFactory;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.SymphonyServletListener;
import org.b3log.symphony.event.EventTypes;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Client;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.ClientQueryService;
import org.b3log.symphony.service.ShortLinkQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Networks;
import org.json.JSONObject;

/**
 * Sends comment to client.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.2.0, Apr 7, 2016
 * @since 1.4.0
 */
public final class CommentSender extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentSender.class.getName());

    /**
     * URL fetch service.
     */
    private URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.DEBUG, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                new Object[]{event.getType(), data, CommentSender.class.getName()});

        if (Latkes.getServePath().contains("localhost") || Networks.isIPv4(Latkes.getServerHost())
                || RuntimeMode.DEVELOPMENT == Latkes.getRuntimeMode()) {
            LOGGER.log(Level.DEBUG, "Do not sync in DEV env");
            return;
        }

        try {
            if (data.optBoolean(Common.FROM_CLIENT)) {
                return;
            }

            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            if (!originalArticle.optBoolean(Article.ARTICLE_SYNC_TO_CLIENT)) {
                return;
            }

            if (Article.ARTICLE_TYPE_C_DISCUSSION == originalArticle.optInt(Article.ARTICLE_TYPE)
                    || Article.ARTICLE_TYPE_C_THOUGHT == originalArticle.optInt(Article.ARTICLE_TYPE)) {
                return;
            }

            final LatkeBeanManager beanManager = Lifecycle.getBeanManager();
            final UserQueryService userQueryService = beanManager.getReference(UserQueryService.class);
            final ShortLinkQueryService shortLinkQueryService = beanManager.getReference(ShortLinkQueryService.class);

            final String authorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject author = userQueryService.getUser(authorId);
            final String clientURL = author.optString(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL);
            if (!Strings.isURL(clientURL)) {
                LOGGER.warn("Invalid client URL [" + clientURL + "]");

                return;
            }

            final ClientQueryService clientQueryService = beanManager.getReference(ClientQueryService.class);
            final JSONObject userClient = clientQueryService.getClientByAdminEmail(author.optString(User.USER_EMAIL));
            if (null == userClient) {
                LOGGER.warn("Not found client [email=" + author.optString(User.USER_EMAIL) + "]");

                return;
            }

            if ("B3log Solo".equals(userClient.optString(Client.CLIENT_NAME))) {
                return;
            }

            final JSONObject originalComment = data.getJSONObject(Comment.COMMENT);
            final String commenterId = originalComment.optString(Comment.COMMENT_AUTHOR_ID);
            final JSONObject commenter = userQueryService.getUser(commenterId);

            final HTTPRequest httpRequest = new HTTPRequest();
            httpRequest.setURL(new URL(clientURL));
            httpRequest.setRequestMethod(HTTPRequestMethod.POST);
            httpRequest.addHeader(new HTTPHeader("User-Agent", "B3log Symphony/" + SymphonyServletListener.VERSION));
            final JSONObject requestJSONObject = new JSONObject();
            final JSONObject comment = new JSONObject();

            final String commentContent = originalComment.optString(Comment.COMMENT_CONTENT);
            String cc = shortLinkQueryService.linkArticle(commentContent);
            cc = shortLinkQueryService.linkTag(cc);
            cc = Emotions.convert(cc);
            comment.put(Common.CONTENT, cc); // Markdown format
            cc = Markdowns.toHTML(cc);
            cc = Markdowns.clean(cc, "");
            comment.put(Common.CONTENT_HTML, cc); // HTML format
            comment.put(Common.IP, originalComment.optString(Comment.COMMENT_IP));
            comment.put(Common.UA, originalComment.optString(Comment.COMMENT_UA));
            comment.put(Article.ARTICLE_T_ID, originalArticle.optString(Article.ARTICLE_CLIENT_ARTICLE_ID));
            comment.put(Common.AUTHOR_NAME, commenter.optString(User.USER_NAME));
            comment.put(Common.AUTHOR_EMAIL, commenter.optString(User.USER_EMAIL));
            comment.put(Common.AUTHOR_URL, commenter.optString(User.USER_URL));
            comment.put(Common.IS_ARTICLE_AUTHOR,
                    originalArticle.optString(Article.ARTICLE_AUTHOR_ID)
                    .equals(commenter.optString(Keys.OBJECT_ID)));
            comment.put(Common.TIME, originalComment.optLong(Comment.COMMENT_CREATE_TIME));
            requestJSONObject.put(Comment.COMMENT, comment);

            final JSONObject client = new JSONObject();
            client.put(Common.KEY, author.optString(UserExt.USER_B3_KEY));
            requestJSONObject.put(Client.CLIENT, client);

            httpRequest.setPayload(requestJSONObject.toString().getBytes("UTF-8"));

            urlFetchService.fetchAsync(httpRequest);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends a comment to client error: {0}", e.getMessage());
        }

        LOGGER.log(Level.DEBUG, "Sent a comment to client");
    }

    /**
     * Gets the event type {@linkplain EventTypes#ADD_COMMENT_TO_ARTICLE}.
     *
     * @return event type
     */
    @Override
    public String getEventType() {
        return EventTypes.ADD_COMMENT_TO_ARTICLE;
    }
}
