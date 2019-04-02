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
package org.b3log.symphony.service;

import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.util.Symphonys;

import java.util.concurrent.TimeUnit;

/**
 * Cron management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Feb 27, 2019
 * @since 3.4.5
 */
@Service
public class CronMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CronMgmtService.class);

    /**
     * Article management service.
     */
    @Inject
    private ArticleMgmtService articleMgmtService;

    /**
     * Verifycode management service.
     */
    @Inject
    private VerifycodeMgmtService verifycodeMgmtService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Notification query service.
     */
    @Inject
    private NotificationQueryService notificationQueryService;

    /**
     * Comment management service.
     */
    @Inject
    private CommentMgmtService commentMgmtService;

    /**
     * Invitecode management service.
     */
    @Inject
    private InvitecodeMgmtService invitecodeMgmtService;

    /**
     * Mail management service.
     */
    @Inject
    private MailMgmtService mailMgmtService;

    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    /**
     * Cache management service.
     */
    @Inject
    private CacheMgmtService cacheMgmtService;

    /**
     * Start all cron tasks.
     */
    public void start() {
        long delay = 10000;

        Symphonys.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            try {
                articleMgmtService.expireStick();
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Executes cron failed", e);
            } finally {
                Stopwatchs.release();
            }
        }, delay, 60 * 1000, TimeUnit.MILLISECONDS);
        delay += 2000;

        Symphonys.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            try {
                verifycodeMgmtService.sendEmailVerifycode();
                verifycodeMgmtService.removeExpiredVerifycodes();
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Executes cron failed", e);
            } finally {
                Stopwatchs.release();
            }
        }, delay, 5 * 1000, TimeUnit.MILLISECONDS);
        delay += 2000;

        Symphonys.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            try {
                cacheMgmtService.refreshCache();
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Executes cron failed", e);
            } finally {
                Stopwatchs.release();
            }
        }, delay, 30 * 60 * 1000, TimeUnit.MILLISECONDS);
        delay += 2000;

        Symphonys.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            try {
                invitecodeMgmtService.expireInvitecodes();
                mailMgmtService.sendWeeklyNewsletter();
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Executes cron failed", e);
            } finally {
                Stopwatchs.release();
            }
        }, delay, 5 * 60 * 1000, TimeUnit.MILLISECONDS);
        delay += 2000;

        Symphonys.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            try {
                userMgmtService.resetUnverifiedUsers();
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Executes cron failed", e);
            } finally {
                Stopwatchs.release();
            }
        }, delay, 2 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
        delay += 2000;
    }
}
