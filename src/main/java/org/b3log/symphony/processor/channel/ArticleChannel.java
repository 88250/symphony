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
package org.b3log.symphony.processor.channel;

import freemarker.template.Template;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.jdbc.JdbcRepository;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.util.Locales;
import org.b3log.symphony.model.*;
import org.b3log.symphony.service.RoleQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Skins;
import org.json.JSONObject;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Article channel.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.3.9.9, Sep 27, 2018
 * @since 1.3.0
 */
@ServerEndpoint(value = "/article-channel", configurator = Channels.WebSocketConfigurator.class)
public class ArticleChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleChannel.class);

    /**
     * Session set.
     */
    public static final Set<Session> SESSIONS = Collections.newSetFromMap(new ConcurrentHashMap());

    /**
     * Article viewing map &lt;articleId, count&gt;.
     */
    public static final Map<String, Integer> ARTICLE_VIEWS = Collections.synchronizedMap(new HashMap<>());

    /**
     * Notifies the specified article heat message to browsers.
     *
     * @param message the specified message, for example,
     *                "articleId": "",
     *                "operation": "" // "+"/"-"
     */
    public static void notifyHeat(final JSONObject message) {
        message.put(Common.TYPE, Article.ARTICLE_T_HEAT);

        final String msgStr = message.toString();

        for (final Session session : SESSIONS) {
            final String viewingArticleId = Channels.getHttpParameter(session, Article.ARTICLE_T_ID);
            if (StringUtils.isBlank(viewingArticleId)
                    || !viewingArticleId.equals(message.optString(Article.ARTICLE_T_ID))) {
                continue;
            }

            if (session.isOpen()) {
                session.getAsyncRemote().sendText(msgStr);
            }
        }
    }

    /**
     * Notifies the specified comment message to browsers.
     *
     * @param message the specified message
     */
    public static void notifyComment(final JSONObject message) {
        message.put(Common.TYPE, Comment.COMMENT);

        final BeanManager beanManager = BeanManager.getInstance();
        final UserQueryService userQueryService = beanManager.getReference(UserQueryService.class);
        final RoleQueryService roleQueryService = beanManager.getReference(RoleQueryService.class);
        final LangPropsService langPropsService = beanManager.getReference(LangPropsService.class);
        final JSONObject article = message.optJSONObject(Article.ARTICLE);

        for (final Session session : SESSIONS) {
            final String viewingArticleId = Channels.getHttpParameter(session, Article.ARTICLE_T_ID);
            if (StringUtils.isBlank(viewingArticleId)
                    || !viewingArticleId.equals(message.optString(Article.ARTICLE_T_ID))) {
                continue;
            }

            final int articleType = Integer.valueOf(Channels.getHttpParameter(session, Article.ARTICLE_TYPE));
            final JSONObject user = (JSONObject) Channels.getHttpSessionAttribute(session, User.USER);
            final boolean isLoggedIn = null != user;

            try {
                if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType) {
                    if (!isLoggedIn) {
                        continue;
                    }

                    final String userName = user.optString(User.USER_NAME);
                    final String userRole = user.optString(User.USER_ROLE);

                    final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                    final String userId = user.optString(Keys.OBJECT_ID);
                    if (!userId.equals(articleAuthorId)) {
                        final String articleContent = article.optString(Article.ARTICLE_CONTENT);
                        final Set<String> userNames = userQueryService.getUserNames(articleContent);

                        boolean invited = false;
                        for (final String inviteUserName : userNames) {
                            if (inviteUserName.equals(userName)) {
                                invited = true;

                                break;
                            }
                        }

                        if (Role.ROLE_ID_C_ADMIN.equals(userRole)) {
                            invited = true;
                        }

                        if (!invited) {
                            continue; // next session
                        }
                    }
                }

                message.put(Comment.COMMENT_T_NICE, false);
                message.put(Common.REWARED_COUNT, 0);
                message.put(Comment.COMMENT_T_VOTE, -1);
                message.put(Common.REWARDED, false);
                message.put(Comment.COMMENT_REVISION_COUNT, 1);

                final Map dataModel = new HashMap();
                dataModel.put(Common.IS_LOGGED_IN, isLoggedIn);
                dataModel.put(Common.CURRENT_USER, user);
                article.put(Common.OFFERED, false);
                dataModel.put(Article.ARTICLE, article);
                dataModel.put(Common.CSRF_TOKEN, Channels.getHttpSessionAttribute(session, Common.CSRF_TOKEN));
                Keys.fillServer(dataModel);
                dataModel.put(Comment.COMMENT, message);

                if (isLoggedIn) {
                    dataModel.putAll(langPropsService.getAll(Locales.getLocale(user.optString(UserExt.USER_LANGUAGE))));
                    final String userId = user.optString(Keys.OBJECT_ID);
                    final Map<String, JSONObject> permissions
                            = roleQueryService.getUserPermissionsGrantMap(userId);
                    dataModel.put(Permission.PERMISSIONS, permissions);
                } else {
                    dataModel.putAll(langPropsService.getAll(Locales.getLocale()));
                    final Map<String, JSONObject> permissions
                            = roleQueryService.getPermissionsGrantMap(Role.ROLE_ID_C_VISITOR);
                    dataModel.put(Permission.PERMISSIONS, permissions);
                }

                final String templateDirName = (String) session.getUserProperties().get(Keys.TEMAPLTE_DIR_NAME);
                final Template template = Skins.SKIN.getTemplate(templateDirName + "/common/comment.ftl");
                final StringWriter stringWriter = new StringWriter();
                template.process(dataModel, stringWriter);
                stringWriter.close();

                message.put("cmtTpl", stringWriter.toString());
                final String msgStr = message.toString();
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(msgStr);
                }
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Notify comment error", e);
            } finally {
                JdbcRepository.dispose();
            }
        }
    }

    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @OnOpen
    public void onConnect(final Session session) {
        final String articleId = Channels.getHttpParameter(session, Article.ARTICLE_T_ID);
        if (StringUtils.isBlank(articleId)) {
            return;
        }

        SESSIONS.add(session);

        synchronized (ARTICLE_VIEWS) {
            if (!ARTICLE_VIEWS.containsKey(articleId)) {
                ARTICLE_VIEWS.put(articleId, 1);
            } else {
                final int count = ARTICLE_VIEWS.get(articleId);
                ARTICLE_VIEWS.put(articleId, count + 1);
            }
        }

        final JSONObject message = new JSONObject();
        message.put(Article.ARTICLE_T_ID, articleId);
        message.put(Common.OPERATION, "+");

        ArticleListChannel.notifyHeat(message);
        notifyHeat(message);
    }

    /**
     * Called when the connection closed.
     *
     * @param session     session
     * @param closeReason close reason
     */
    @OnClose
    public void onClose(final Session session, final CloseReason closeReason) {
        removeSession(session);
    }

    /**
     * Called when a message received from the browser.
     *
     * @param message message
     */
    @OnMessage
    public void onMessage(final String message) {
    }

    /**
     * Called in case of an error.
     *
     * @param session session
     * @param error   error
     */
    @OnError
    public void onError(final Session session, final Throwable error) {
        removeSession(session);
    }

    /**
     * Removes the specified session.
     *
     * @param session the specified session
     */
    private void removeSession(final Session session) {
        SESSIONS.remove(session);

        final String articleId = Channels.getHttpParameter(session, Article.ARTICLE_T_ID);
        if (StringUtils.isBlank(articleId)) {
            return;
        }

        synchronized (ARTICLE_VIEWS) {
            if (!ARTICLE_VIEWS.containsKey(articleId)) {
                return;
            }

            final int count = ARTICLE_VIEWS.get(articleId);
            final int newCount = count - 1;
            if (newCount < 1) {
                ARTICLE_VIEWS.remove(articleId);
            } else {
                ARTICLE_VIEWS.put(articleId, newCount);
            }
        }

        final JSONObject message = new JSONObject();
        message.put(Article.ARTICLE_T_ID, articleId);
        message.put(Common.OPERATION, "-");

        ArticleListChannel.notifyHeat(message);
        notifyHeat(message);
    }
}
