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
package org.b3log.symphony.processor.channel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.LatkeBeanManagerImpl;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.jdbc.JdbcRepository;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.LangPropsServiceImpl;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.service.TimelineMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Emotions;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * Article channel.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.2.7.5, Apr 25, 2016
 * @since 1.3.0
 */
@ServerEndpoint(value = "/article-channel", configurator = Channels.WebSocketConfigurator.class)
public class ArticleChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleChannel.class.getName());

    /**
     * Session set.
     */
    public static final Set<Session> SESSIONS = Collections.newSetFromMap(new ConcurrentHashMap());

    /**
     * Article viewing map &lt;articleId, count&gt;.
     */
    public static final Map<String, Integer> ARTICLE_VIEWS
            = Collections.synchronizedMap(new HashMap<String, Integer>());

    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @OnOpen
    public void onConnect(final Session session) {
        final String articleId = (String) Channels.getHttpParameter(session, Article.ARTICLE_T_ID);
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

        final JSONObject user = (JSONObject) Channels.getHttpSessionAttribute(session, User.USER);
        if (null == user) {
            return;
        }

        final String userName = user.optString(User.USER_NAME);

        // Timeline
        final LatkeBeanManager beanManager = LatkeBeanManagerImpl.getInstance();
        final ArticleRepository articleRepository = beanManager.getReference(ArticleRepository.class);
        final LangPropsService langPropsService = beanManager.getReference(LangPropsServiceImpl.class);
        final TimelineMgmtService timelineMgmtService = beanManager.getReference(TimelineMgmtService.class);

        try {
            final JSONObject article = articleRepository.get(articleId);
            if (null == article) {
                return;
            }

            String articleTitle = Jsoup.parse(article.optString(Article.ARTICLE_TITLE)).text();
            articleTitle = Emotions.convert(articleTitle);

            final String articlePermalink = Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK);

            final JSONObject timeline = new JSONObject();
            timeline.put(Common.TYPE, Article.ARTICLE);
            String content = langPropsService.get("timelineInArticleLabel");
            content = content.replace("{user}", "<a target='_blank' rel='nofollow' href='" + Latkes.getServePath()
                    + "/member/" + userName + "'>" + userName + "</a>")
                    .replace("{article}", "<a target='_blank' rel='nofollow' href='" + articlePermalink
                            + "'>" + articleTitle + "</a>");
            timeline.put(Common.CONTENT, content);

            timelineMgmtService.addTimeline(timeline);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Timeline error", e);
        } finally {
            JdbcRepository.dispose();
        }
    }

    /**
     * Called when the connection closed.
     *
     * @param session session
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
     * @param error error
     */
    @OnError
    public void onError(final Session session, final Throwable error) {
        removeSession(session);
    }

    /**
     * Notifies the specified article heat message to browsers.
     *
     * @param message the specified message, for example      <pre>
     * {
     *     "articleId": "",
     *     "operation": "" // "+"/"-"
     * }
     * </pre>
     */
    public static void notifyHeat(final JSONObject message) {
        message.put(Common.TYPE, Article.ARTICLE_T_HEAT);

        final String msgStr = message.toString();

        for (final Session session : SESSIONS) {
            final String viewingArticleId = (String) Channels.getHttpParameter(session, Article.ARTICLE_T_ID);
            if (Strings.isEmptyOrNull(viewingArticleId)
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
     * @param message the specified message, for example      <pre>
     * {
     *     "articleId": "",
     *     "commentId": "",
     *     "commentAuthorName": "",
     *     "commentAuthorThumbnailURL": "",
     *     "commentCreateTime": "", // yyyy-MM-dd HH:mm
     *     "commentContent": "",
     *     “commentThankLabel": "",
     *     "thankLabel": "",
     *     "thankedLabel": "",
     *     "timeAgo": "",
     *     "commentUA": ""
     * }
     * </pre>
     */
    public static void notifyComment(final JSONObject message) {
        message.put(Common.TYPE, Comment.COMMENT);

        final String msgStr = message.toString();

        final LatkeBeanManager beanManager = LatkeBeanManagerImpl.getInstance();
        final UserQueryService userQueryService = beanManager.getReference(UserQueryService.class);
        final ArticleRepository articleRepository = beanManager.getReference(ArticleRepository.class);

        for (final Session session : SESSIONS) {
            final String viewingArticleId = (String) Channels.getHttpParameter(session, Article.ARTICLE_T_ID);
            if (Strings.isEmptyOrNull(viewingArticleId)
                    || !viewingArticleId.equals(message.optString(Article.ARTICLE_T_ID))) {
                continue;
            }

            final int articleType = Integer.valueOf(Channels.getHttpParameter(session, Article.ARTICLE_TYPE));

            try {
                if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType) {
                    final JSONObject user = (JSONObject) Channels.getHttpSessionAttribute(session, User.USER);
                    if (null == user) {
                        continue;
                    }

                    final String userName = user.optString(User.USER_NAME);
                    final String userId = user.optString(Keys.OBJECT_ID);
                    final String userRole = user.optString(User.USER_ROLE);

                    final JSONObject article = articleRepository.get(viewingArticleId);
                    if (null == article) {
                        continue;
                    }

                    final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
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

                        if (Role.ADMIN_ROLE.equals(userRole)) {
                            invited = true;
                        }

                        if (!invited) {
                            continue; // next session
                        }
                    }
                }

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
     * Removes the specified session.
     *
     * @param session the specified session
     */
    private void removeSession(final Session session) {
        SESSIONS.remove(session);

        final String articleId = (String) Channels.getHttpParameter(session, Article.ARTICLE_T_ID);
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

        final JSONObject user = (JSONObject) Channels.getHttpSessionAttribute(session, User.USER);
        if (null == user) {
            return;
        }

        final String userName = user.optString(User.USER_NAME);

        // Timeline
        final LatkeBeanManager beanManager = LatkeBeanManagerImpl.getInstance();
        final ArticleRepository articleRepository = beanManager.getReference(ArticleRepository.class);
        final LangPropsService langPropsService = beanManager.getReference(LangPropsServiceImpl.class);
        final TimelineMgmtService timelineMgmtService = beanManager.getReference(TimelineMgmtService.class);

        try {
            final JSONObject article = articleRepository.get(articleId);
            if (null == article) {
                return;
            }

            String articleTitle = Jsoup.parse(article.optString(Article.ARTICLE_TITLE)).text();
            articleTitle = Emotions.convert(articleTitle);
            final String articlePermalink = Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK);

            final JSONObject timeline = new JSONObject();
            timeline.put(Common.TYPE, Article.ARTICLE);
            String content = langPropsService.get("timelineOutArticleLabel");
            content = content.replace("{user}", "<a target='_blank' rel='nofollow' href='" + Latkes.getServePath()
                    + "/member/" + userName + "'>" + userName + "</a>")
                    .replace("{article}", "<a target='_blank' rel='nofollow' href='" + articlePermalink
                            + "'>" + articleTitle + "</a>");
            timeline.put(Common.CONTENT, content);

            timelineMgmtService.addTimeline(timeline);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Timeline error", e);
        } finally {
            JdbcRepository.dispose();
        }
    }
}
