/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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

import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.jdbc.util.Connections;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.cache.ArticleCache;
import org.b3log.symphony.cache.DomainCache;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.processor.StatisticProcessor;
import org.b3log.symphony.processor.channel.ArticleChannel;
import org.b3log.symphony.processor.channel.ArticleListChannel;
import org.b3log.symphony.processor.channel.ChatRoomChannel;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Cache management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Dec 2, 2018
 * @since 3.4.5
 */
@Service
public class CacheMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CacheMgmtService.class);

    /**
     * Global locks
     */
    private static Cache LOCKS = CacheFactory.getCache("locks");

    /**
     * Tag cache.
     */
    @Inject
    private TagCache tagCache;

    /**
     * Domain cache.
     */
    @Inject
    private DomainCache domainCache;

    /**
     * Article cache.
     */
    @Inject
    private ArticleCache articleCache;

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
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Refreshes all caches.
     */
    public void refreshCache() {
        final String lockName = "refreshCaches";
        if (!lock(lockName)) {
            return;
        }
        try {
            LOGGER.info("Refreshing cache");
            domainCache.loadDomains();
            articleCache.loadPerfectArticles();
            articleCache.loadSideHotArticles();
            articleCache.loadSideRandomArticles();
            tagCache.loadTags();
            final StatisticProcessor statisticProcessor = BeanManager.getInstance().getReference(StatisticProcessor.class);
            statisticProcessor.loadStatData();
            userQueryService.loadUserNames();
            statusReport();
            LOGGER.info("Refreshed cache");
        } finally {
            unlock(lockName);
        }
    }

    private void statusReport() {
        final JSONObject ret = new JSONObject();
        ret.put(Common.ONLINE_VISITOR_CNT, optionQueryService.getOnlineVisitorCount());
        ret.put(Common.ONLINE_MEMBER_CNT, optionQueryService.getOnlineMemberCount());
        ret.put(Common.ONLINE_CHAT_CNT, ChatRoomChannel.SESSIONS.size());
        ret.put(Common.ARTICLE_CHANNEL_CNT, ArticleChannel.SESSIONS.size());
        ret.put(Common.ARTICLE_LIST_CHANNEL_CNT, ArticleListChannel.SESSIONS.size());
        ret.put(Common.THREAD_CNT, Symphonys.getActiveThreadCount() + "/" + Symphonys.getMaxThreadCount());
        ret.put(Common.DB_CONN_CNT, Connections.getActiveConnectionCount() + "/" + Connections.getTotalConnectionCount() + "/" + Connections.getMaxConnectionCount());
        ret.put(Keys.Runtime.RUNTIME_CACHE, Latkes.getRuntimeCache().name());
        ret.put(Keys.Runtime.RUNTIME_DATABASE, Latkes.getRuntimeDatabase().name());
        ret.put(Keys.Runtime.RUNTIME_MODE, Latkes.getRuntimeMode().name());
        final JSONObject memory = new JSONObject();
        ret.put("memory", memory);
        final int mb = 1024 * 1024;
        final Runtime runtime = Runtime.getRuntime();
        memory.put("total", runtime.totalMemory() / mb);
        memory.put("free", runtime.freeMemory() / mb);
        memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / mb);
        memory.put("max", runtime.maxMemory() / mb);

        LOGGER.info(ret.toString(4));
    }

    /**
     * Lock.
     *
     * @param lockName the specified lock name
     * @return {@code true} if lock successfully, returns {@code false} otherwise
     */
    public synchronized static boolean lock(final String lockName) {
        JSONObject lock = LOCKS.get(lockName);
        if (null == lock) {
            lock = new JSONObject();
        }

        if (lock.optBoolean(Common.LOCK)) {
            return false;
        }

        lock.put(Common.LOCK, true);
        LOCKS.put(lockName, lock);

        return true;
    }

    /**
     * Unlock.
     *
     * @param lockName the specified lock name
     */
    public synchronized static void unlock(final String lockName) {
        JSONObject lock = LOCKS.get(lockName);
        if (null == lock) {
            lock = new JSONObject();
        }

        lock.put(Common.LOCK, false);
        LOCKS.put(lockName, lock);
    }
}
