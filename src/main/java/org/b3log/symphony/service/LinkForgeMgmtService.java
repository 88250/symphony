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
package org.b3log.symphony.service;

import java.util.List;
import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.model.Link;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.LinkRepository;
import org.b3log.symphony.repository.OptionRepository;
import org.b3log.symphony.repository.TagUserLinkRepository;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.util.Links;
import org.b3log.symphony.util.Pangu;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Link utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.3, Sep 11, 2016
 * @since 1.6.0
 */
@Service
public class LinkForgeMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LinkForgeMgmtService.class.getName());

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
     * Forges the specified URL.
     *
     * @param url the specified URL
     * @param userId the specified user id
     */
    public void forge(final String url, final String userId) {
        String html;
        String baseURL;
        try {
            final Document doc = Jsoup.connect(url).timeout(5000).userAgent(Symphonys.USER_AGENT_BOT).get();

            doc.select("body").append("<a href=\"" + url + "\">" + url + "</a>"); // Append the specified URL itfself

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
                final double linkScore = link.optDouble(Link.LINK_SCORE);
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
}
