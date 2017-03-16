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

import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.symphony.processor.advice.AnonymousViewCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.DataModelService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;

/**
 * Chat room processor.
 *
 * <ul>
 * <li>Shows char room (/cr, /chat-room, /community), GET</li>
 * <li>Sends chat message (/chat-room/send), POST</li>
 * <li>Receives <a href="https://github.com/b3log/xiaov">XiaoV</a> message (/community/push), POST</li>
 * </ul>
 *
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.0.0.0, Mar 16, 2017
 * @since 2.0.1
 */
@RequestProcessor
public class GobangProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(GobangProcessor.class.getName());

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
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Chat messages.
     */
    public static LinkedList<JSONObject> messages = new LinkedList<JSONObject>();


    /**
     * Shows chat room.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = {"/gobang-game"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showChatRoom(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final JSONObject msg = new JSONObject();
        LOGGER.info("in gobang-game method");
        msg.put(User.USER_NAME, "Zephyr");
//        GobangChannel.notifyChat(msg);
    }

}
