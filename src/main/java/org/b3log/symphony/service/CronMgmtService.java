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

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Execs;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.*;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cron management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Dec 2, 2018
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
     * Turing query service.
     */
    @Inject
    private TuringQueryService turingQueryService;

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

        final JSONObject xiaoV = userQueryService.getUserByName(TuringQueryService.ROBOT_NAME);
        if (null != xiaoV) {
            Symphonys.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
                try {
                    xiaov();
                } catch (final Exception e) {
                    LOGGER.log(Level.ERROR, "Executes cron failed", e);
                } finally {
                    Stopwatchs.release();
                }
            }, delay, 5 * 1000, TimeUnit.MILLISECONDS);
            delay += 2000;
        }

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
                publishIPFS();
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Executes cron failed", e);
            } finally {
                Stopwatchs.release();
            }
        }, delay, 2 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
        delay += 2000;
    }

    private void xiaov() {
        try {
            final JSONObject xiaoV = userQueryService.getUserByName(TuringQueryService.ROBOT_NAME);
            final int avatarViewMode = UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL;
            final String xiaoVUserId = xiaoV.optString(Keys.OBJECT_ID);
            final JSONObject atResult = notificationQueryService.getAtNotifications(
                    avatarViewMode, xiaoVUserId, 1, 1); // Just get the latest one
            final List<JSONObject> notifications = (List<JSONObject>) atResult.get(Keys.RESULTS);
            final JSONObject replyResult = notificationQueryService.getReplyNotifications(
                    avatarViewMode, xiaoVUserId, 1, 1); // Just get the latest one
            notifications.addAll((List<JSONObject>) replyResult.get(Keys.RESULTS));
            for (final JSONObject notification : notifications) {
                if (notification.optBoolean(Notification.NOTIFICATION_HAS_READ)) {
                    continue;
                }

                notificationMgmtService.makeRead(notification);

                String articleId = notification.optString(Article.ARTICLE_T_ID);
                String q = null;
                final int dataType = notification.optInt(Notification.NOTIFICATION_DATA_TYPE);
                switch (dataType) {
                    case Notification.DATA_TYPE_C_AT:
                        q = notification.optString(Common.CONTENT);
                        break;
                    case Notification.DATA_TYPE_C_REPLY:
                        q = notification.optString(Comment.COMMENT_CONTENT);
                        break;
                    default:
                        LOGGER.warn("Unknown notification data type [" + dataType + "] for XiaoV reply");
                }

                String xiaoVSaid;
                final JSONObject comment = new JSONObject();
                if (StringUtils.isNotBlank(q)) {
                    q = Jsoup.parse(q).text();
                    q = StringUtils.replace(q, "@" + TuringQueryService.ROBOT_NAME + " ", "");

                    xiaoVSaid = turingQueryService.chat(articleId, q);

                    comment.put(Comment.COMMENT_CONTENT, xiaoVSaid);
                    comment.put(Comment.COMMENT_AUTHOR_ID, xiaoVUserId);
                    comment.put(Comment.COMMENT_ON_ARTICLE_ID, articleId);
                    comment.put(Comment.COMMENT_ORIGINAL_COMMENT_ID, notification.optString(Comment.COMMENT_T_ID));

                    commentMgmtService.addComment(comment);
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "XiaoV cron execute failed", e);
        }
    }

    private void publishIPFS() {
        final String dir = Symphonys.get("ipfs.dir");
        final String bin = Symphonys.get("ipfs.bin");
        if (StringUtils.isBlank(dir) || StringUtils.isBlank(bin)) {
            return;
        }

        final long started = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Adding articles to IPFS");
        String output = Execs.exec(bin + " add -r " + dir, 1000 * 60 * 30);
        if (StringUtils.isBlank(output) || !StringUtils.containsIgnoreCase(output, "added")) {
            LOGGER.log(Level.ERROR, "Executes [ipfs add] failed: " + output);

            return;
        }
        LOGGER.log(Level.INFO, "Publishing articles to IPFS");
        final String[] lines = output.split("\n");
        final String lastLine = lines[lines.length - 1];
        final String hash = lastLine.split(" ")[1];
        output = Execs.exec(bin + " name publish " + hash, 1000 * 60 * 30);
        if (StringUtils.isBlank(output) || !StringUtils.containsIgnoreCase(output, "published")) {
            LOGGER.log(Level.ERROR, "Executes [ipfs name publish] failed: " + output);

            return;
        }
        LOGGER.log(Level.INFO, "Published articles to IPFS [" + (System.currentTimeMillis() - started) + "ms]");
    }
}
