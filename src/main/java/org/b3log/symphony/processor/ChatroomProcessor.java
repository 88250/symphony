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
import org.b3log.latke.http.Dispatcher;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.util.Locales;
import org.b3log.latke.util.Times;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.channel.ChatroomChannel;
import org.b3log.symphony.processor.middleware.AnonymousViewCheckMidware;
import org.b3log.symphony.processor.middleware.LoginCheckMidware;
import org.b3log.symphony.processor.middleware.validate.ChatMsgAddValidationMidware;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.*;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.b3log.symphony.processor.channel.ChatroomChannel.SESSIONS;

/**
 * Chatroom processor.
 * <ul>
 * <li>Shows chatroom (/cr), GET</li>
 * <li>Sends chat message (/chat-room/send), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 1.4.0
 */
@Singleton
public class ChatroomProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ChatroomProcessor.class);

    /**
     * Chat messages.
     */
    public static LinkedList<JSONObject> messages = new LinkedList<>();

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

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
     * Register request handlers.
     */
    public static void register() {
        final BeanManager beanManager = BeanManager.getInstance();
        final LoginCheckMidware loginCheck = beanManager.getReference(LoginCheckMidware.class);
        final AnonymousViewCheckMidware anonymousViewCheckMidware = beanManager.getReference(AnonymousViewCheckMidware.class);
        final ChatMsgAddValidationMidware chatMsgAddValidationMidware = beanManager.getReference(ChatMsgAddValidationMidware.class);

        final ChatroomProcessor chatroomProcessor = beanManager.getReference(ChatroomProcessor.class);
        Dispatcher.post("/chat-room/send", chatroomProcessor::addChatRoomMsg, loginCheck::handle, chatMsgAddValidationMidware::handle);
        Dispatcher.get("/cr", chatroomProcessor::showChatRoom, anonymousViewCheckMidware::handle);
    }

    /**
     * Adds a chat message.
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
     */
    public synchronized void addChatRoomMsg(final RequestContext context) {
        final JSONObject requestJSONObject = (JSONObject) context.attr(Keys.REQUEST);
        String content = requestJSONObject.optString(Common.CONTENT);

        content = shortLinkQueryService.linkArticle(content);
        content = Emotions.convert(content);
        content = Markdowns.toHTML(content);
        content = Markdowns.clean(content, "");

        final JSONObject currentUser = Sessions.getUser();
        final String userName = currentUser.optString(User.USER_NAME);

        final JSONObject msg = new JSONObject();
        msg.put(User.USER_NAME, userName);
        msg.put(UserExt.USER_AVATAR_URL, currentUser.optString(UserExt.USER_AVATAR_URL));
        msg.put(Common.CONTENT, content);
        msg.put(Common.TIME, System.currentTimeMillis());

        messages.addFirst(msg);
        final int maxCnt = Symphonys.CHATROOMMSGS_CNT;
        if (messages.size() > maxCnt) {
            messages.remove(maxCnt);
        }

        final JSONObject pushMsg = JSONs.clone(msg);
        pushMsg.put(Common.TIME, Times.getTimeAgo(msg.optLong(Common.TIME), Locales.getLocale()));
        ChatroomChannel.notifyChat(pushMsg);

        context.renderJSON(StatusCodes.SUCC);

        try {
            final String userId = currentUser.optString(Keys.OBJECT_ID);
            final JSONObject user = userQueryService.getUser(userId);
            user.put(UserExt.USER_LATEST_CMT_TIME, System.currentTimeMillis());
            userMgmtService.updateUser(userId, user);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Update user latest comment time failed", e);
        }
    }

    /**
     * Shows chatroom.
     *
     * @param context the specified context
     */
    public void showChatRoom(final RequestContext context) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "chat-room.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final List<JSONObject> msgs = messages.stream().
                map(msg -> JSONs.clone(msg).put(Common.TIME, Times.getTimeAgo(msg.optLong(Common.TIME), Locales.getLocale()))).collect(Collectors.toList());
        dataModel.put(Common.MESSAGES, msgs);
        dataModel.put("chatRoomMsgCnt", Symphonys.CHATROOMMSGS_CNT);
        dataModel.put(Common.ONLINE_CHAT_CNT, SESSIONS.size());

        dataModelService.fillHeaderAndFooter(context, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);
    }
}
