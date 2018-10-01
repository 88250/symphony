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
package org.b3log.symphony.processor;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.Execs;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.util.Symphonys;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * IPFS(https://ipfs.io) processor.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.3, Oct 1, 2018
 * @since 2.3.0
 */
@RequestProcessor
public class IPFSProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(IPFSProcessor.class);

    /**
     * Publishes article markdown files to IPFS.
     *
     * @param request  the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @param context  the specified HTTP request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/cron/ipfs/articles/publish", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void publishArticles(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        context.renderJSON().renderTrueResult();

        final String dir = Symphonys.get("ipfs.dir");
        final String bin = Symphonys.get("ipfs.bin");
        if (StringUtils.isBlank(dir) || StringUtils.isBlank(bin)) {
            return;
        }

        final long started = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Adding articles to IPFS");
        String output = Execs.exec(bin + " add -r " + dir);
        if (StringUtils.isBlank(output) || !StringUtils.containsIgnoreCase(output, "added")) {
            LOGGER.log(Level.ERROR, "Executes [ipfs add] failed: " + output);

            return;
        }
        LOGGER.log(Level.INFO, "Publishing articles to IPFS");
        final String[] lines = output.split("\n");
        final String lastLine = lines[lines.length - 1];
        final String hash = lastLine.split(" ")[1];
        output = Execs.exec(bin + " name publish " + hash);
        if (StringUtils.isBlank(output) || !StringUtils.containsIgnoreCase(output, "published")) {
            LOGGER.log(Level.ERROR, "Executes [ipfs name publish] failed: " + output);

            return;
        }
        LOGGER.log(Level.INFO, "Published articles to IPFS [" + (System.currentTimeMillis() - started) + "ms]");
    }
}
