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

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.repository.jdbc.JdbcRepository;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.urlfetch.*;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.model.Link;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.LinkRepository;
import org.b3log.symphony.repository.OptionRepository;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.repository.TagUserLinkRepository;
import org.b3log.symphony.util.Links;
import org.b3log.symphony.util.Pangu;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Link forge management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.2.0, Nov 30, 2017
 * @since 1.6.0
 */
@Service
public class LinkForgeMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LinkForgeMgmtService.class);

    /**
     * Link repository.
     */
    @Inject
    private LinkRepository linkRepository;

    /**
     * Tag repository.
     */
    @Inject
    private TagRepository tagRepository;

    /**
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    /**
     * Tag-User-Link repository.
     */
    @Inject
    private TagUserLinkRepository tagUserLinkRepository;

    /**
     * Tag cache.
     */
    @Inject
    private TagCache tagCache;

    /**
     * URL fetch service.
     */
    private URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();

    /**
     * Forges the specified URL.
     *
     * @param url    the specified URL
     * @param userId the specified user id
     */
    public void forge(final String url, final String userId) {
        if (!StringUtils.startsWithIgnoreCase(url, "http://") && !StringUtils.startsWithIgnoreCase(url, "https://")) {
            return;
        }

        try {
            final URL u = new URL(url);
            if (StringUtils.containsIgnoreCase(Latkes.getServePath(), u.getHost())) {
                return;
            }
        } catch (final Exception e) {
            return;
        }

        String html;
        String baseURL;
        try {
            final Document doc = Jsoup.connect(url).timeout(5000).userAgent(Symphonys.USER_AGENT_BOT).get();

            doc.select("body").prepend("<a href=\"" + url + "\">" + url + "</a>"); // Add the specified URL itfself

            html = doc.html();
            baseURL = doc.baseUri();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Parses link [" + url + "] failed", e);

            return;
        }

        final List<JSONObject> links = Links.getLinks(baseURL, html);
        final List<JSONObject> cachedTags = tagCache.getTags();

        final Transaction transaction = linkRepository.beginTransaction();
        try {
            for (final JSONObject lnk : links) {
                final String addr = lnk.optString(Link.LINK_ADDR);
                if (Link.inAddrBlacklist(addr)) {
                    continue;
                }

                JSONObject link = linkRepository.getLink(addr);

                if (null == link) {
                    link = new JSONObject();
                    link.put(Link.LINK_ADDR, lnk.optString(Link.LINK_ADDR));
                    link.put(Link.LINK_BAD_CNT, 0);
                    link.put(Link.LINK_BAIDU_REF_CNT, 0);
                    link.put(Link.LINK_CLICK_CNT, 0);
                    link.put(Link.LINK_GOOD_CNT, 0);
                    link.put(Link.LINK_SCORE, 0);
                    link.put(Link.LINK_SUBMIT_CNT, 0);
                    link.put(Link.LINK_TITLE, lnk.optString(Link.LINK_TITLE));
                    link.put(Link.LINK_TYPE, Link.LINK_TYPE_C_FORGE);
                    link.put(Link.LINK_PING_CNT, 0);
                    link.put(Link.LINK_PING_ERR_CNT, 0);

                    LOGGER.info(link.optString(Link.LINK_ADDR) + "__" + link.optString(Link.LINK_TITLE));
                    linkRepository.add(link);

                    final JSONObject linkCntOption = optionRepository.get(Option.ID_C_STATISTIC_LINK_COUNT);
                    final int linkCnt = linkCntOption.optInt(Option.OPTION_VALUE);
                    linkCntOption.put(Option.OPTION_VALUE, linkCnt + 1);
                    optionRepository.update(Option.ID_C_STATISTIC_LINK_COUNT, linkCntOption);
                } else {
                    link.put(Link.LINK_BAIDU_REF_CNT, lnk.optInt(Link.LINK_BAIDU_REF_CNT));
                    link.put(Link.LINK_TITLE, lnk.optString(Link.LINK_TITLE));
                    link.put(Link.LINK_SCORE, lnk.optInt(Link.LINK_BAIDU_REF_CNT)); // XXX: Need a score algorithm

                    linkRepository.update(link.optString(Keys.OBJECT_ID), link);
                }

                final String linkId = link.optString(Keys.OBJECT_ID);
                final double linkScore = link.optDouble(Link.LINK_SCORE, 0D);
                String title = link.optString(Link.LINK_TITLE) + " " + link.optString(Link.LINK_T_KEYWORDS);
                title = Pangu.spacingText(title);
                String[] titles = title.split(" ");
                titles = Strings.trimAll(titles);

                for (final JSONObject cachedTag : cachedTags) {
                    final String tagId = cachedTag.optString(Keys.OBJECT_ID);

                    final String tagTitle = cachedTag.optString(Tag.TAG_TITLE);
                    if (!Strings.containsIgnoreCase(tagTitle, titles)) {
                        continue;
                    }

                    final JSONObject tag = tagRepository.get(tagId);

                    // clean
                    tagUserLinkRepository.removeByTagIdUserIdAndLinkId(tagId, userId, linkId);

                    // re-add
                    final JSONObject tagLinkRel = new JSONObject();
                    tagLinkRel.put(Tag.TAG_T_ID, tagId);
                    tagLinkRel.put(UserExt.USER_T_ID, userId);
                    tagLinkRel.put(Link.LINK_T_ID, linkId);
                    tagLinkRel.put(Link.LINK_SCORE, linkScore);
                    tagUserLinkRepository.add(tagLinkRel);

                    // refresh link score
                    tagUserLinkRepository.updateTagLinkScore(tagId, linkId, linkScore);

                    // re-calc tag link count
                    final int tagLinkCnt = tagUserLinkRepository.countTagLink(tagId);
                    tag.put(Tag.TAG_LINK_CNT, tagLinkCnt);
                    tagRepository.update(tagId, tag);
                }
            }

            transaction.commit();

            LOGGER.info("Forged link [" + url + "]");
        } catch (final Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Saves links failed", e);
        }
    }

    /**
     * Purges link forge.
     */
    public void purge() {
        new Thread(() -> {
            final Transaction transaction = optionRepository.beginTransaction();

            try {
                Thread.sleep(15 * 1000);

                final JSONObject linkCntOption = optionRepository.get(Option.ID_C_STATISTIC_LINK_COUNT);
                int linkCnt = linkCntOption.optInt(Option.OPTION_VALUE);

                int slags = 0;
                JSONArray links = linkRepository.get(new Query()).optJSONArray(Keys.RESULTS);
                for (int i = 0; i < links.length(); i++) {
                    final JSONObject link = links.getJSONObject(i);
                    final String linkAddr = link.optString(Link.LINK_ADDR);

                    if (!Link.inAddrBlacklist(linkAddr) && link.optInt(Link.LINK_PING_ERR_CNT) < 7) {
                        continue;
                    }

                    final String linkId = link.optString(Keys.OBJECT_ID);

                    // clean slags

                    linkRepository.remove(linkId);
                    ++slags;

                    final List<String> tagIds = tagUserLinkRepository.getTagIdsByLinkId(linkId, Integer.MAX_VALUE);
                    for (final String tagId : tagIds) {
                        final JSONObject tag = tagRepository.get(tagId);

                        tagUserLinkRepository.removeByLinkId(linkId);

                        final int tagLinkCnt = tagUserLinkRepository.countTagLink(tagId);
                        tag.put(Tag.TAG_LINK_CNT, tagLinkCnt);
                        tagRepository.update(tagId, tag);
                    }
                }

                linkCntOption.put(Option.OPTION_VALUE, linkCnt - slags);
                optionRepository.update(Option.ID_C_STATISTIC_LINK_COUNT, linkCntOption);

                transaction.commit();

                LOGGER.info("Purged link forge [slags=" + slags + "]");

                // Ping
                links = linkRepository.get(new Query()).optJSONArray(Keys.RESULTS);
                LOGGER.info("Ping links [size=" + links.length() + "]");
                final CountDownLatch countDownLatch = new CountDownLatch(links.length());
                for (int i = 0; i < links.length(); i++) {
                    final JSONObject link = links.getJSONObject(i);
                    Symphonys.EXECUTOR_SERVICE.submit(new CheckTask(link, countDownLatch));
                }
                countDownLatch.await(1, TimeUnit.HOURS);
                LOGGER.info("Pinged links [size=" + links.length()
                        + ", countDownLatch=" + countDownLatch.getCount() + "]");
            } catch (final Exception e) {
                if (null != transaction) {
                    transaction.rollback();
                }

                LOGGER.log(Level.ERROR, "Purges link forge failed", e);
            } finally {
                JdbcRepository.dispose();
            }
        }).start();
    }

    /**
     * Link accessibility check task.
     *
     * @author <a href="http://88250.b3log.org">Liang Ding</a>
     * @version 1.0.0.0, Jun 14, 2017
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

            LOGGER.debug("Checks link [url=" + linkAddr + "] accessibility");
            final long start = System.currentTimeMillis();
            int responseCode = 0;
            try {
                final HTTPRequest request = new HTTPRequest();
                request.addHeader(new HTTPHeader("User-Agent", Symphonys.USER_AGENT_BOT));
                request.setURL(new URL(linkAddr));
                request.setConnectTimeout(1000);
                request.setReadTimeout(1000 * 5);

                final HTTPResponse response = urlFetchService.fetch(request);
                responseCode = response.getResponseCode();

                LOGGER.log(Level.DEBUG, "Accesses link [url=" + linkAddr + "] response [code={0}]", responseCode);
            } catch (final Exception e) {
                LOGGER.warn("Link [url=" + linkAddr + "] accessibility check failed [msg=" + e.getMessage() + "]");
            } finally {
                countDownLatch.countDown();

                final long elapsed = System.currentTimeMillis() - start;
                LOGGER.log(Level.DEBUG, "Accesses link [url=" + linkAddr + "] response [code=" + responseCode + "], "
                        + "elapsed [" + elapsed + ']');

                link.put(Link.LINK_PING_CNT, link.optInt(Link.LINK_PING_CNT) + 1);
                if (HttpServletResponse.SC_OK != responseCode) {
                    link.put(Link.LINK_PING_ERR_CNT, link.optInt(Link.LINK_PING_ERR_CNT) + 1);
                }

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
}