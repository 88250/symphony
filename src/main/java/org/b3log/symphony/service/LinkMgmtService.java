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
package org.b3log.symphony.service;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Link;
import org.b3log.symphony.repository.LinkRepository;
import org.b3log.symphony.util.Links;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Link management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Oct 4, 2018
 * @since 3.2.0
 */
@Service
public class LinkMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LinkMgmtService.class);

    /**
     * Link repository.
     */
    @Inject
    private LinkRepository linkRepository;

    /**
     * Adds a link with the specified URL.
     *
     * @param url the specified URL
     */
    public void addLink(final String url) {
        JSONObject link = linkRepository.getLink(url);
        final int clickCnt = null != link ? link.optInt(Link.LINK_CLICK_CNT) + 1 : 0;
        if (null != link) {
            link.put(Link.LINK_CLICK_CNT, clickCnt);
            final Transaction transaction = linkRepository.beginTransaction();
            try {
                linkRepository.update(link.optString(Keys.OBJECT_ID), link);

                transaction.commit();
            } catch (final Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                LOGGER.log(Level.ERROR, "Updates link clicks [addr=" + url + "] failed", e);
            }
        }

        if (null != link && 1000 * 60 * 5 > System.currentTimeMillis() - link.optLong(Link.LINK_PING_TIME)) {
            return;
        }

        JSONObject lnk = Links.getLink(url);
        if (null == lnk) {
            lnk = new JSONObject();
            lnk.put(Link.LINK_ADDR, url);
            lnk.put(Link.LINK_TITLE, "");
        }

        link = new JSONObject();
        final String addr = lnk.optString(Link.LINK_ADDR);
        link.put(Link.LINK_ADDR_HASH, DigestUtils.sha1Hex(addr));
        link.put(Link.LINK_ADDR, addr);
        link.put(Link.LINK_BAD_CNT, 0);
        link.put(Link.LINK_BAIDU_REF_CNT, 0);
        link.put(Link.LINK_CLICK_CNT, clickCnt);
        link.put(Link.LINK_GOOD_CNT, 0);
        link.put(Link.LINK_SCORE, 0);
        link.put(Link.LINK_SUBMIT_CNT, 0);
        link.put(Link.LINK_TITLE, lnk.optString(Link.LINK_TITLE));
        link.put(Link.LINK_PING_CNT, 0);
        link.put(Link.LINK_PING_ERR_CNT, 0);
        link.put(Link.LINK_PING_TIME, 0);
        link.put(Link.LINK_CARD_HTML, "");

        addLink(link);
    }

    /**
     * Adds the specified link.
     *
     * @param link the specified link
     */
    private void addLink(final JSONObject link) {
        final String linkAddr = link.optString(Link.LINK_ADDR);
        final JSONObject old = linkRepository.getLink(linkAddr);
        if (null != old) {
            old.put(Link.LINK_CLICK_CNT, link.optInt(Link.LINK_CLICK_CNT));

            singlePing(old);

            return;
        }

        final Transaction transaction = linkRepository.beginTransaction();
        try {
            linkRepository.add(link);

            transaction.commit();
        } catch (final Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Adds link [addr=" + linkAddr + "] failed", e);
        }

        singlePing(link);
    }

    /**
     * Pings the specified link with the specified count down latch.
     *
     * @param link           the specified link
     * @param countDownLatch the specified count down latch
     */
    public void pingLink(final JSONObject link, CountDownLatch countDownLatch) {
        Symphonys.EXECUTOR_SERVICE.submit(new CheckTask(link, countDownLatch));
    }

    /**
     * Link accessibility check task.
     *
     * @author <a href="http://88250.b3log.org">Liang Ding</a>
     * @version 2.0.0.0, Jan 1, 2018
     * @since 2.2.0
     */
    private class CheckTask implements Runnable {

        /**
         * Link to check.
         */
        private final JSONObject link;

        /**
         * Count down latch.
         */
        private final CountDownLatch countDownLatch;

        /**
         * Constructs a check task with the specified link.
         *
         * @param link           the specified link
         * @param countDownLatch the specified count down latch
         */
        public CheckTask(final JSONObject link, final CountDownLatch countDownLatch) {
            this.link = link;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            final String linkAddr = link.optString(Link.LINK_ADDR);
            final long start = System.currentTimeMillis();
            int responseCode = 0;
            try {
                final int TIMEOUT = 5000;
                final HttpResponse response = HttpRequest.get(linkAddr).timeout(TIMEOUT).followRedirects(true).header("User-Agent", Symphonys.USER_AGENT_BOT).send();
                responseCode = response.statusCode();
            } catch (final Exception e) {
                LOGGER.trace("Link [url=" + linkAddr + "] accessibility check failed [msg=" + e.getMessage() + "]");
            } finally {
                countDownLatch.countDown();

                final long elapsed = System.currentTimeMillis() - start;
                LOGGER.log(Level.TRACE, "Accesses link [url=" + linkAddr + "] response [code=" + responseCode + "], "
                        + "elapsed [" + elapsed + ']');

                link.put(Link.LINK_PING_CNT, link.optInt(Link.LINK_PING_CNT) + 1);
                if (HttpServletResponse.SC_OK != responseCode) {
                    link.put(Link.LINK_PING_ERR_CNT, link.optInt(Link.LINK_PING_ERR_CNT) + 1);
                }
                link.put(Link.LINK_PING_TIME, System.currentTimeMillis());

                final Transaction transaction = linkRepository.beginTransaction();
                try {
                    linkRepository.update(link.optString(Keys.OBJECT_ID), link);

                    transaction.commit();
                } catch (final RepositoryException e) {
                    if (null != transaction && transaction.isActive()) {
                        transaction.rollback();
                    }

                    LOGGER.log(Level.ERROR, "Updates link failed", e);
                }
            }
        }
    }

    private void singlePing(final JSONObject link) {
        try {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            pingLink(link, countDownLatch);
            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Pings link [addr=" + link.optString(Link.LINK_ADDR) + "] failed", e);
        }
    }
}
