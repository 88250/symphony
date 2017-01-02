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

import org.b3log.symphony.processor.channel.ArticleChannel;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.symphony.SymphonyServletListener;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.processor.channel.ArticleListChannel;
import org.b3log.symphony.processor.channel.ChatRoomChannel;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Running status processor.
 *
 * <p>
 * <ul>
 * <li>Report running status (/cron/status), GET</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.3, Jul 19, 2016
 * @since 1.3.0
 */
@RequestProcessor
public class StatusProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(StatusProcessor.class);

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Reports running status.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/cron/status", method = HTTPRequestMethod.GET)
    public void reportStatus(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final JSONObject ret = new JSONObject();

        context.renderJSON(ret);

        ret.put(Common.ONLINE_VISITOR_CNT, optionQueryService.getOnlineVisitorCount());
        ret.put(Common.ONLINE_MEMBER_CNT, optionQueryService.getOnlineMemberCount());
        ret.put(Common.ONLINE_CHAT_CNT, ChatRoomChannel.SESSIONS.size());
        ret.put(Common.ARTICLE_CHANNEL_CNT, ArticleChannel.SESSIONS.size());
        ret.put(Common.ARTICLE_LIST_CHANNEL_CNT, ArticleListChannel.SESSIONS.size());

        final JSONObject memory = new JSONObject();
        ret.put("memory", memory);

        final int mb = 1024 * 1024;
        final Runtime runtime = Runtime.getRuntime();
        memory.put("totoal", runtime.totalMemory() / mb);
        memory.put("free", runtime.freeMemory() / mb);
        memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / mb);
        memory.put("max", runtime.maxMemory() / mb);

        LOGGER.info(ret.toString(SymphonyServletListener.JSON_PRINT_INDENT_FACTOR));
        ret.put(Keys.STATUS_CODE, true);
    }
}
