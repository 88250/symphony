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
package org.b3log.symphony.util;

import org.b3log.symphony.service.AvatarQueryService;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.SymphonyServletListener;
import org.b3log.symphony.cache.DomainCache;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Domain;
import org.b3log.symphony.model.Follow;
import org.b3log.symphony.model.Liveness;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.ActivityQueryService;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.FollowQueryService;
import org.b3log.symphony.service.LivenessQueryService;
import org.b3log.symphony.service.NotificationQueryService;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.TagQueryService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;

/**
 * Filler utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.7.1.11, Jun 2, 2016
 * @since 0.2.0
 */
@Service
public class Filler {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Filler.class.getName());

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Tag query service.
     */
    @Inject
    private TagQueryService tagQueryService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

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
     * Notification query service.
     */
    @Inject
    private NotificationQueryService notificationQueryService;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Activity query service.
     */
    @Inject
    private ActivityQueryService activityQueryService;

    /**
     * Liveness query service.
     */
    @Inject
    private LivenessQueryService livenessQueryService;

    /**
     * Domain cache.
     */
    @Inject
    private DomainCache domainCache;

    /**
     * Fills domain navigation.
     *
     * @param dataModel the specified data model
     */
    public void fillDomainNav(final Map<String, Object> dataModel) {
        Stopwatchs.start("Fills domain nav");
        try {
            dataModel.put(Domain.DOMAINS, domainCache.getDomains(Integer.MAX_VALUE));
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills relevant articles.
     *
     * @param dataModel the specified data model
     * @param article the specified article
     * @throws Exception exception
     */
    public void fillRelevantArticles(final Map<String, Object> dataModel, final JSONObject article) throws Exception {
        dataModel.put(Common.SIDE_RELEVANT_ARTICLES,
                articleQueryService.getRelevantArticles(article, Symphonys.getInt("sideRelevantArticlesCnt")));
    }

    /**
     * Fills the latest comments.
     *
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    public void fillLatestCmts(final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills latest comments");
        try {
            // dataModel.put(Common.SIDE_LATEST_CMTS, commentQueryService.getLatestComments(Symphonys.getInt("sizeLatestCmtsCnt")));
            dataModel.put(Common.SIDE_LATEST_CMTS, (Object) Collections.emptyList());
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills random articles.
     *
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    public void fillRandomArticles(final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills random articles");
        try {
            dataModel.put(Common.SIDE_RANDOM_ARTICLES, articleQueryService.getRandomArticles(Symphonys.getInt("sideRandomArticlesCnt")));
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills hot articles.
     *
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    public void fillHotArticles(final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills hot articles");
        try {
            dataModel.put(Common.SIDE_HOT_ARTICLES, articleQueryService.getHotArticles(Symphonys.getInt("sideHotArticlesCnt")));
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills tags.
     *
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    public void fillSideTags(final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills side tags");
        try {
            dataModel.put(Common.SIDE_TAGS, tagQueryService.getTags(Symphonys.getInt("sideTagsCnt")));

            if (!(Boolean) dataModel.get(Common.IS_MOBILE)) {
                fillNewTags(dataModel);
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills header.
     *
     * @param request the specified request
     * @param response the specified response
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    private void fillHeader(final HttpServletRequest request, final HttpServletResponse response,
            final Map<String, Object> dataModel) throws Exception {
        fillMinified(dataModel);
        dataModel.put(Common.STATIC_RESOURCE_VERSION, Latkes.getStaticResourceVersion());
        dataModel.put("esEnabled", Symphonys.getBoolean("es.enabled"));
        dataModel.put("algoliaEnabled", Symphonys.getBoolean("algolia.enabled"));
        dataModel.put("algoliaAppId", Symphonys.get("algolia.appId"));
        dataModel.put("algoliaSearchKey", Symphonys.get("algolia.searchKey"));
        dataModel.put("algoliaIndex", Symphonys.get("algolia.index"));
        dataModel.put("logoIcon", Symphonys.get("icon.logo"));

        // fillTrendTags(dataModel);
        fillPersonalNav(request, response, dataModel);

        fillLangs(dataModel);
        fillSideAd(dataModel);
    }

    /**
     * Fills footer.
     *
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    private void fillFooter(final Map<String, Object> dataModel) throws Exception {
        fillSysInfo(dataModel);

        dataModel.put(Common.YEAR, String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        dataModel.put(Common.SITE_VISIT_STAT_CODE, Symphonys.get("siteVisitStatCode"));
    }

    /**
     * Fills header and footer.
     *
     * @param request the specified request
     * @param response the specified response
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    public void fillHeaderAndFooter(final HttpServletRequest request, final HttpServletResponse response,
            final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills header");
        try {
            final boolean isMobile = (Boolean) request.getAttribute(Common.IS_MOBILE);
            dataModel.put(Common.IS_MOBILE, isMobile);

            fillHeader(request, response, dataModel);
        } finally {
            Stopwatchs.end();
        }

        Stopwatchs.start("Fills footer");
        try {
            fillFooter(dataModel);
        } finally {
            Stopwatchs.end();
        }

        dataModel.put(Common.WEBSOCKET_SCHEME, Symphonys.get("websocket.scheme"));
    }

    /**
     * Fills personal navigation.
     *
     * @param request the specified request
     * @param response the specified response
     * @param dataModel the specified data model
     */
    private void fillPersonalNav(final HttpServletRequest request, final HttpServletResponse response,
            final Map<String, Object> dataModel) {
        Stopwatchs.start("Fills personal nav");
        try {
            dataModel.put(Common.IS_LOGGED_IN, false);
            dataModel.put(Common.IS_ADMIN_LOGGED_IN, false);

            if (null == Sessions.currentUser(request) && !userMgmtService.tryLogInWithCookie(request, response)) {
                dataModel.put("loginLabel", langPropsService.get("loginLabel"));

                return;
            }

            JSONObject curUser = null;

            try {
                curUser = userQueryService.getCurrentUser(request);
            } catch (final ServiceException e) {
                LOGGER.log(Level.ERROR, "Gets the current user failed", e);
            }

            if (null == curUser) {
                dataModel.put("loginLabel", langPropsService.get("loginLabel"));

                return;
            }

            dataModel.put(Common.IS_LOGGED_IN, true);
            dataModel.put(Common.LOGOUT_URL, userQueryService.getLogoutURL("/"));

            dataModel.put("logoutLabel", langPropsService.get("logoutLabel"));

            final String userName = curUser.optString(User.USER_NAME);
            dataModel.put(User.USER_NAME, userName);
            final String userRole = curUser.optString(User.USER_ROLE);
            dataModel.put(User.USER_ROLE, userRole);
            dataModel.put(Common.IS_ADMIN_LOGGED_IN, Role.ADMIN_ROLE.equals(userRole));

            avatarQueryService.fillUserAvatarURL(curUser);

            final String userId = curUser.optString(Keys.OBJECT_ID);

            final long followingArticleCnt = followQueryService.getFollowingCount(userId, Follow.FOLLOWING_TYPE_C_ARTICLE);
            final long followingTagCnt = followQueryService.getFollowingCount(userId, Follow.FOLLOWING_TYPE_C_TAG);
            final long followingUserCnt = followQueryService.getFollowingCount(userId, Follow.FOLLOWING_TYPE_C_USER);

            curUser.put(Common.FOLLOWING_ARTICLE_CNT, followingArticleCnt);
            curUser.put(Common.FOLLOWING_TAG_CNT, followingTagCnt);
            curUser.put(Common.FOLLOWING_USER_CNT, followingUserCnt);
            final int point = curUser.optInt(UserExt.USER_POINT);
            final int appRole = curUser.optInt(UserExt.USER_APP_ROLE);
            if (UserExt.USER_APP_ROLE_C_HACKER == appRole) {
                curUser.put(UserExt.USER_T_POINT_HEX, Integer.toHexString(point));
            } else {
                curUser.put(UserExt.USER_T_POINT_CC, UserExt.toCCString(point));
            }

            dataModel.put(Common.CURRENT_USER, curUser);

            final int unreadNotificationCount = notificationQueryService.getUnreadNotificationCount(curUser.optString(Keys.OBJECT_ID));
            dataModel.put(Notification.NOTIFICATION_T_UNREAD_COUNT, unreadNotificationCount);

            dataModel.put(Common.IS_DAILY_CHECKIN, activityQueryService.isCheckedinToday(userId));
            dataModel.put(Common.USE_CAPTCHA_CHECKIN, Symphonys.getBoolean("geetest.enabled"));

            final int livenessMax = Symphonys.getInt("activitYesterdayLivenessReward.maxPoint");
            final int currentLiveness = livenessQueryService.getCurrentLivenessPoint(userId);
            dataModel.put(Liveness.LIVENESS, (float) currentLiveness / livenessMax * 100);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills minified directory and file postfix for static JavaScript, CSS.
     *
     * @param dataModel the specified data model
     */
    public void fillMinified(final Map<String, Object> dataModel) {
        switch (Latkes.getRuntimeMode()) {
            case DEVELOPMENT:
                dataModel.put(Common.MINI_POSTFIX, "");
                break;
            case PRODUCTION:
                dataModel.put(Common.MINI_POSTFIX, Common.MINI_POSTFIX_VALUE);
                break;
            default:
                throw new AssertionError();
        }
    }

    /**
     * Fills the all language labels.
     *
     * @param dataModel the specified data model
     */
    private void fillLangs(final Map<String, Object> dataModel) {
        Stopwatchs.start("Fills lang");
        try {
            dataModel.putAll(langPropsService.getAll(Latkes.getLocale()));
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills the side ad labels.
     *
     * @param dataModel the specified data model
     */
    private void fillSideAd(final Map<String, Object> dataModel) {
        if (Math.random() > 0.8) {
            dataModel.put("ADLabel", langPropsService.get("ADImgLabel"));
        }
    }

    /**
     * Fills trend tags.
     *
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    private void fillTrendTags(final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills trend tags");
        try {
            // dataModel.put(Common.NAV_TREND_TAGS, tagQueryService.getTrendTags(Symphonys.getInt("trendTagsCnt")));
            dataModel.put(Common.NAV_TREND_TAGS, Collections.emptyList());
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fils new tags.
     *
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    private void fillNewTags(final Map<String, Object> dataModel) throws Exception {
        dataModel.put(Common.NEW_TAGS, tagQueryService.getNewTags(Symphonys.getInt("newTagsCnt")));
    }

    /**
     * Fills system info.
     *
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    private void fillSysInfo(final Map<String, Object> dataModel) throws Exception {
        dataModel.put(Common.VERSION, SymphonyServletListener.VERSION);
        dataModel.put(Common.ONLINE_VISITOR_CNT, optionQueryService.getOnlineVisitorCount());

        if (!(Boolean) dataModel.get(Common.IS_MOBILE)) {
            final JSONObject statistic = optionQueryService.getStatistic();
            dataModel.put(Option.CATEGORY_C_STATISTIC, statistic);
        }
    }
}
