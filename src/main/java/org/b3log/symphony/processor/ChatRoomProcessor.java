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
package org.b3log.symphony.processor;

import java.io.IOException;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.ChatMsgAddValidation;
import org.b3log.symphony.processor.channel.ChatRoomChannel;
import org.b3log.symphony.service.TuringQueryService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Chat room processor.
 *
 * <ul>
 * <li>Shows char room (/chat-room), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Apr 11, 2016
 * @since 1.4.0
 */
@RequestProcessor
public class ChatRoomProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ChatRoomProcessor.class.getName());

    /**
     * Filler.
     */
    @Inject
    private Filler filler;

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
    public void addChatRoomMsg(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        context.renderJSON();

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);
        String content = requestJSONObject.optString(Common.CONTENT);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userName = currentUser.optString(User.USER_NAME);

        final JSONObject msg = new JSONObject();
        msg.put(User.USER_NAME, userName);
        msg.put(Common.CONTENT, content);

        ChatRoomChannel.notifyChat(msg);

        if (content.contains("@" + TuringQueryService.ROBOT_NAME)) {
            content = content.replaceAll("@" + TuringQueryService.ROBOT_NAME, "");
            final String xiaoVSaid = turingQueryService.chat(currentUser.optString(Keys.OBJECT_ID), content);
            if (null != xiaoVSaid) {
                final JSONObject xiaoVMsg = new JSONObject();
                xiaoVMsg.put(User.USER_NAME, TuringQueryService.ROBOT_NAME);
                xiaoVMsg.put(Common.CONTENT, "@" + userName + ", " + xiaoVSaid);

                ChatRoomChannel.notifyChat(xiaoVMsg);
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
    @RequestProcessing(value = "/chat-room", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showChatRoom(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        request.setAttribute(Keys.TEMAPLTE_DIR_NAME, Symphonys.get("skinDirName"));
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("chat-room.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);
        filler.fillRandomArticles(dataModel);
        filler.fillHotArticles(dataModel);
        filler.fillSideTags(dataModel);
        filler.fillLatestCmts(dataModel);
    }
}
