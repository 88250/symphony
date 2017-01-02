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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.symphony.util.GeetestLib;
import javax.servlet.annotation.WebServlet;
import org.b3log.latke.Keys;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * <a href="http://www.geetest.com">极验验证</a>.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 23, 2016
 * @since 1.4.0
 */
@WebServlet(urlPatterns = "/geetest-captcha", loadOnStartup = 2)
public class GeetestCaptchaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException, IOException {
        final JSONObject currentUser = Sessions.currentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final GeetestLib gtSdk = new GeetestLib(Symphonys.get("geetest.id"), Symphonys.get("geetest.key"));
        String resStr = "{}";
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int gtServerStatus = gtSdk.preProcess(userId);
        request.getSession().setAttribute(gtSdk.gtServerStatusSessionKey, gtServerStatus);
        resStr = gtSdk.getResponseStr();

        final PrintWriter out = response.getWriter();
        out.println(resStr);
    }
}
