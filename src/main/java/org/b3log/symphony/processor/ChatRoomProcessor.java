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
package org.b3log.symphony.processor;

import com.qiniu.util.Auth;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.AnonymousViewCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.ChatMsgAddValidation;
import org.b3log.symphony.processor.channel.ChatRoomChannel;
import static org.b3log.symphony.processor.channel.ChatRoomChannel.SESSIONS;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.CommentMgmtService;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.NotificationQueryService;
import org.b3log.symphony.service.ShortLinkQueryService;
import org.b3log.symphony.service.TuringQueryService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.service.DataModelService;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * Chat room processor.
 *
 * <ul>
 * <li>Shows char room (/cr, /chat-room, /community), GET</li>
 * <li>Sends chat message (/chat-room/send), POST</li>
 * <li>Receives <a href="https://github.com/b3log/xiaov">XiaoV</a> message (/community/push), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.5.10, Jan 21, 2017
 * @since 1.4.0
 */
@RequestProcessor
public class ChatRoomProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ChatRoomProcessor.class.getName());

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Turing query service.
     */
    @Inject
    private TuringQueryService turingQueryService;

    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Notification query service.
     */
    @Inject
    private NotificationQueryService notificationQueryService;

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Comment management service.
     */
    @Inject
    private CommentMgmtService commentMgmtService;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Chat messages.
     */
    public static LinkedList<JSONObject> messages = new LinkedList<JSONObject>();

    /**
     * XiaoV replies Stm.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws IOException io exception
     * @throws ServletException servlet exception
     */
    @RequestProcessing(value = "/cron/xiaov", method = HTTPRequestMethod.GET)
    public void xiaoVReply(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        context.renderJSON();

        try {
            final JSONObject xiaoV = userQueryService.getUserByName(TuringQueryService.ROBOT_NAME);
            if (null == xiaoV) {
                return;
            }

            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

            final String xiaoVUserId = xiaoV.optString(Keys.OBJECT_ID);
            final String xiaoVEmail = xiaoV.optString(User.USER_EMAIL);
            final JSONObject result = notificationQueryService.getAtNotifications(
                    avatarViewMode, xiaoVUserId, 1, 1); // Just get the latest one
            final List<JSONObject> notifications = (List<JSONObject>) result.get(Keys.RESULTS);

            for (final JSONObject notification : notifications) {
                if (notification.optBoolean(Notification.NOTIFICATION_HAS_READ)) {
                    continue;
                }

                String xiaoVSaid = "";

                final JSONObject comment = new JSONObject();

                String articleId = StringUtils.substringBetween(notification.optString(Common.URL),
                        "/article/", "#");

                String q = "";

                if (!StringUtils.isBlank(articleId)) {
                    final String originalCmtId = StringUtils.substringAfter(notification.optString(Common.URL), "#");
                    final JSONObject originalCmt = commentQueryService.getCommentById(avatarViewMode, originalCmtId);
                    q = originalCmt.optString(Comment.COMMENT_CONTENT);
                } else {
                    articleId = StringUtils.substringAfter(notification.optString(Common.URL),
                            "/article/");

                    final JSONObject article = articleQueryService.getArticleById(avatarViewMode, articleId);
                    q = article.optString(Article.ARTICLE_CONTENT);
                }

                if (StringUtils.isNotBlank(q)) {
                    q = Jsoup.parse(q).text();

                    if (!StringUtils.contains(q, "@" + TuringQueryService.ROBOT_NAME + " ")) {
                        continue;
                    }

                    q = StringUtils.replace(q, "@" + TuringQueryService.ROBOT_NAME + " ", "");

                    xiaoVSaid = turingQueryService.chat(articleId, q);

                    comment.put(Comment.COMMENT_CONTENT, xiaoVSaid);
                    comment.put(Comment.COMMENT_AUTHOR_ID, xiaoVUserId);
                    comment.put(Comment.COMMENT_ON_ARTICLE_ID, articleId);

                    xiaoV.remove(UserExt.USER_T_POINT_CC);
                    xiaoV.remove(UserExt.USER_T_POINT_HEX);
                    comment.put(Comment.COMMENT_T_COMMENTER, xiaoV);

                    commentMgmtService.addComment(comment);
                }

                notificationMgmtService.makeRead(notification);
            }

            context.renderTrueResult();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Update user latest comment time failed", e);
        }
    }

    /**
     * Adds a chat message.
     *
     * <p>
     * The request json object (a chat message):
     * <pre>
     * {
     *     "content": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws IOException io exception
     * @throws ServletException servlet exception
     */
    @RequestProcessing(value = "/chat-room/send", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {ChatMsgAddValidation.class})
    public synchronized void addChatRoomMsg(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        context.renderJSON();

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);
        String content = requestJSONObject.optString(Common.CONTENT);

        content = shortLinkQueryService.linkArticle(content);
        content = shortLinkQueryService.linkTag(content);
        content = Emotions.convert(content);
        content = Markdowns.toHTML(content);
        content = Markdowns.clean(content, "");

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userName = currentUser.optString(User.USER_NAME);

        final JSONObject msg = new JSONObject();
        msg.put(User.USER_NAME, userName);
        msg.put(UserExt.USER_AVATAR_URL, currentUser.optString(UserExt.USER_AVATAR_URL));
        msg.put(Common.CONTENT, content);

        ChatRoomChannel.notifyChat(msg);

        messages.addFirst(msg);
        final int maxCnt = Symphonys.getInt("chatRoom.msgCnt");
        if (messages.size() > maxCnt) {
            messages.remove(maxCnt);
        }

        if (content.contains("@" + TuringQueryService.ROBOT_NAME + " ")) {
            content = content.replaceAll("@" + TuringQueryService.ROBOT_NAME + " ", "");
            final String xiaoVSaid = turingQueryService.chat(currentUser.optString(User.USER_NAME), content);
            if (null != xiaoVSaid) {
                final JSONObject xiaoVMsg = new JSONObject();
                xiaoVMsg.put(User.USER_NAME, TuringQueryService.ROBOT_NAME);
                xiaoVMsg.put(UserExt.USER_AVATAR_URL, TuringQueryService.ROBOT_AVATAR + "?imageView2/1/w/48/h/48/interlace/0/q/100");
                xiaoVMsg.put(Common.CONTENT, "<p>@" + userName + " " + xiaoVSaid + "</p>");

                ChatRoomChannel.notifyChat(xiaoVMsg);

                messages.addFirst(xiaoVMsg);
                if (messages.size() > maxCnt) {
                    messages.remove(maxCnt);
                }
            }
        }

        context.renderTrueResult();

        currentUser.put(UserExt.USER_LATEST_CMT_TIME, System.currentTimeMillis());
        try {
            userMgmtService.updateUser(currentUser.optString(Keys.OBJECT_ID), currentUser);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Update user latest comment time failed", e);
        }
    }

    /**
     * Shows chat room.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = {"/cr", "/chat-room", "/community"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showChatRoom(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("chat-room.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModel.put(Common.MESSAGES, messages);
        dataModel.put("chatRoomMsgCnt", Symphonys.getInt("chatRoom.msgCnt"));

        // Qiniu file upload authenticate
        final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
        dataModel.put("qiniuUploadToken", auth.uploadToken(Symphonys.get("qiniu.bucket")));
        dataModel.put("qiniuDomain", Symphonys.get("qiniu.domain"));

        final long imgMaxSize = Symphonys.getLong("upload.img.maxSize");
        dataModel.put("imgMaxSize", imgMaxSize);
        final long fileMaxSize = Symphonys.getLong("upload.file.maxSize");
        dataModel.put("fileMaxSize", fileMaxSize);

        dataModel.put(Common.ONLINE_CHAT_CNT, SESSIONS.size());

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        dataModelService.fillRandomArticles(avatarViewMode, dataModel);
        dataModelService.fillSideHotArticles(avatarViewMode, dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);
    }

    /**
     * XiaoV push API.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/community/push", method = HTTPRequestMethod.POST)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public synchronized void receiveXiaoV(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final String key = Symphonys.get("xiaov.key");
//        if (!key.equals(request.getParameter("key"))) {
//            response.sendError(HttpServletResponse.SC_FORBIDDEN);
//
//            return;
//        }

        final String msg = request.getParameter("msg");
        if (StringUtils.isBlank(msg)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

            return;
        }

        String user = request.getParameter("user");
        if (StringUtils.isBlank("user")) {
            user = "V";
        }

        final JSONObject ret = new JSONObject();
        context.renderJSON(ret);

        final String defaultAvatarURL = Symphonys.get("defaultThumbnailURL");
        final JSONObject chatroomMsg = new JSONObject();
        chatroomMsg.put(User.USER_NAME, user);
        chatroomMsg.put(UserExt.USER_AVATAR_URL, defaultAvatarURL);
        chatroomMsg.put(Common.CONTENT, msg);

        ChatRoomChannel.notifyChat(chatroomMsg);
        messages.addFirst(chatroomMsg);
        final int maxCnt = Symphonys.getInt("chatRoom.msgCnt");
        if (messages.size() > maxCnt) {
            messages.remove(maxCnt);
        }

        ret.put(Keys.STATUS_CODE, true);
    }
}
