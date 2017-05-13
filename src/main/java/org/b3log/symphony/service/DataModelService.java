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
package org.b3log.symphony.service;

import org.apache.commons.lang.math.RandomUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Locales;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.SymphonyServletListener;
import org.b3log.symphony.cache.DomainCache;
import org.b3log.symphony.model.*;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Data model service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.12.2.28, May 13, 2017
 * @since 0.2.0
 */
@Service
public class DataModelService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DataModelService.class);

    /**
     * Icon configuration.
     */
    private static final ResourceBundle ICON_CONF = ResourceBundle.getBundle("icon");

    /**
     * Icons.
     */
    private static Map<String, String> ICONS;

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
     * Role query service.
     */
    @Inject
    private RoleQueryService roleQueryService;

    /**
     * Domain cache.
     */
    @Inject
    private DomainCache domainCache;

    /**
     * Fills relevant articles.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param dataModel      the specified data model
     * @param article        the specified article
     * @throws Exception exception
     */
    public void fillRelevantArticles(final int avatarViewMode,
                                     final Map<String, Object> dataModel, final JSONObject article) throws Exception {
        final int articleStatus = article.optInt(Article.ARTICLE_STATUS);
        if (Article.ARTICLE_STATUS_C_VALID != articleStatus) {
            dataModel.put(Common.SIDE_RELEVANT_ARTICLES, Collections.emptyList());

            return;
        }

        Stopwatchs.start("Fills relevant articles");
        try {
            dataModel.put(Common.SIDE_RELEVANT_ARTICLES,
                    articleQueryService.getRelevantArticles(
                            avatarViewMode, article, Symphonys.getInt("sideRelevantArticlesCnt")));
        } finally {
            Stopwatchs.end();
        }
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
     * @param avatarViewMode the specified avatar view mode
     * @param dataModel      the specified data model
     * @throws Exception exception
     */
    public void fillRandomArticles(final int avatarViewMode, final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills random articles");
        try {
            final int fetchSize = Symphonys.getInt("sideRandomArticlesCnt");
            if (fetchSize > 0) {
                dataModel.put(Common.SIDE_RANDOM_ARTICLES, articleQueryService.getRandomArticles(avatarViewMode, fetchSize));
            } else {
                dataModel.put(Common.SIDE_RANDOM_ARTICLES, Collections.emptyList());
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills side hot articles.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param dataModel      the specified data model
     * @throws Exception exception
     */
    public void fillSideHotArticles(final int avatarViewMode, final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills hot articles");
        try {
            dataModel.put(Common.SIDE_HOT_ARTICLES,
                    articleQueryService.getSideHotArticles(avatarViewMode, Symphonys.getInt("sideHotArticlesCnt")));
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
     * Fills index tags.
     *
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    public void fillIndexTags(final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills index tags");
        try {
            for (int i = 0; i < 9; i++) {
                final JSONObject tag = new JSONObject();
                tag.put(Tag.TAG_URI, "Sym");
                tag.put(Tag.TAG_ICON_PATH, "sym.png");
                tag.put(Tag.TAG_TITLE, "Sym");

                dataModel.put(Tag.TAG + i, tag);
            }

            final List<JSONObject> tags = tagQueryService.getTags(Symphonys.getInt("sideTagsCnt"));
            for (int i = 0; i < tags.size(); i++) {
                dataModel.put(Tag.TAG + i, tags.get(i));
            }

            dataModel.put(Tag.TAGS, tags);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills header.
     *
     * @param request   the specified request
     * @param response  the specified response
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

        // fillTrendTags(dataModel);
        fillPersonalNav(request, response, dataModel);

        fillLangs(dataModel);
        fillIcons(dataModel);
        fillSideAd(dataModel);
        fillHeaderBanner(dataModel);
        fillSideTips(dataModel);

        fillDomainNav(dataModel);
    }

    /**
     * Fills domain navigation.
     *
     * @param dataModel the specified data model
     */
    private void fillDomainNav(final Map<String, Object> dataModel) {
        Stopwatchs.start("Fills domain nav");
        try {
            dataModel.put(Domain.DOMAINS, domainCache.getDomains(Integer.MAX_VALUE));
        } finally {
            Stopwatchs.end();
        }
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
        dataModel.put(Common.MOUSE_EFFECTS, RandomUtils.nextDouble() > 0.95);
    }

    /**
     * Fills header and footer.
     *
     * @param request   the specified request
     * @param response  the specified response
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
     * @param request   the specified request
     * @param response  the specified response
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

            final String userRole = curUser.optString(User.USER_ROLE);
            dataModel.put(User.USER_ROLE, userRole);

            dataModel.put(Common.IS_ADMIN_LOGGED_IN, Role.ROLE_ID_C_ADMIN.equals(userRole));

            avatarQueryService.fillUserAvatarURL(curUser.optInt(UserExt.USER_AVATAR_VIEW_MODE), curUser);

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

            final JSONObject role = roleQueryService.getRole(userRole);
            curUser.put(Role.ROLE_NAME, role.optString(Role.ROLE_NAME));

            // final int unreadNotificationCount = notificationQueryService.getUnreadNotificationCount(curUser.optString(Keys.OBJECT_ID));
            dataModel.put(Notification.NOTIFICATION_T_UNREAD_COUNT, 0); // AJAX polling 

            dataModel.put(Common.IS_DAILY_CHECKIN, activityQueryService.isCheckedinToday(userId));
            dataModel.put(Common.USE_CAPTCHA_CHECKIN, Symphonys.getBoolean("geetest.enabled"));

            final int livenessMax = Symphonys.getInt("activitYesterdayLivenessReward.maxPoint");
            final int currentLiveness = livenessQueryService.getCurrentLivenessPoint(userId);
            dataModel.put(Liveness.LIVENESS, (float) (Math.round((float) currentLiveness / livenessMax * 100 * 100)) / 100);
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
            dataModel.putAll(langPropsService.getAll(Locales.getLocale()));
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills the all icons.
     *
     * @param dataModel the specified data model
     */
    private void fillIcons(final Map<String, Object> dataModel) {
        Stopwatchs.start("Fills icons");
        try {
            if (null == ICONS) {
                ICONS = new HashMap<>();

                final Enumeration<String> keys = ICON_CONF.getKeys();
                while (keys.hasMoreElements()) {
                    final String key = keys.nextElement();
                    String value = ICON_CONF.getString(key);

                    if ("logoIcon".equals(key)) {
                        value = value.replace("${servePath}", Latkes.getServePath());
                    }

                    ICONS.put(key, value);
                }
            }

            dataModel.putAll(ICONS);
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
        final JSONObject adOption = optionQueryService.getOption(Option.ID_C_SIDE_FULL_AD);
        if (null == adOption) {
            dataModel.put("ADLabel", "");
        } else {
            dataModel.put("ADLabel", adOption.optString(Option.OPTION_VALUE));
        }
    }

    /**
     * Fills the side tips.
     *
     * @param dataModel the specified data model
     */
    private void fillSideTips(final Map<String, Object> dataModel) {
        if (RandomUtils.nextFloat() < 0.8) {
            return;
        }

        final Map<String, String> labels = langPropsService.getAll(Locales.getLocale());
        final List<String> tipsLabels = new ArrayList<>();

        for (final Map.Entry<String, String> entry : labels.entrySet()) {
            final String key = entry.getKey();
            if (key.startsWith("tips")) {
                tipsLabels.add(entry.getValue());
            }
        }

        dataModel.put("tipsLabel", tipsLabels.get(RandomUtils.nextInt(tipsLabels.size())));
    }

    /**
     * Fills the header banner.
     *
     * @param dataModel the specified data model
     */
    private void fillHeaderBanner(final Map<String, Object> dataModel) {
        final JSONObject adOption = optionQueryService.getOption(Option.ID_C_HEADER_BANNER);
        if (null == adOption) {
            dataModel.put("HeaderBannerLabel", "");
        } else {
            dataModel.put("HeaderBannerLabel", adOption.optString(Option.OPTION_VALUE));
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
        dataModel.put(Common.NEW_TAGS, tagQueryService.getNewTags());
    }

    /**
     * Fills system info.
     *
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    private void fillSysInfo(final Map<String, Object> dataModel) throws Exception {
        dataModel.put(Common.VERSION, SymphonyServletListener.VERSION);
    }
}
