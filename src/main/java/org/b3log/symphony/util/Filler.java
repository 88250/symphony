/*
 * Copyright (c) 2012, B3log Team
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

import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.user.UserService;
import org.b3log.latke.user.UserServiceFactory;
import org.b3log.latke.util.Sessions;
import org.b3log.symphony.SymphonyServletListener;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.processor.LoginProcessor;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.TagQueryService;
import org.json.JSONObject;

/**
 * Filler utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.7, Oct 25, 2012
 * @since 0.2.0
 */
public final class Filler {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Filler.class.getName());
    /**
     * User service.
     */
    private static UserService userService = UserServiceFactory.getUserService();
    /**
     * Article query service.
     */
    private static ArticleQueryService articleQueryService = ArticleQueryService.getInstance();
    /**
     * Tag query service.
     */
    private static TagQueryService tagQueryService = TagQueryService.getInstance();
    /**
     * Option query service.
     */
    private static OptionQueryService optionQueryService = OptionQueryService.getInstance();
    /**
     * Language service.
     */
    private static LangPropsService langPropsService = LangPropsService.getInstance();

    /**
     * Fills relevant articles.
     * 
     * @param dataModel the specified data model
     * @param article the specified article
     * @throws Exception exception
     */
    public static void fillRelevantArticles(final Map<String, Object> dataModel, final JSONObject article) throws Exception {
        dataModel.put(Common.SIDE_RELEVANT_ARTICLES,
                      articleQueryService.getRelevantArticles(article, Symphonys.getInt("sideRelevantArticlesCnt")));
    }

    /**
     * Fills random articles.
     * 
     * @param dataModel the specified data model
     * @throws Exception exception 
     */
    public static void fillRandomArticles(final Map<String, Object> dataModel) throws Exception {
        dataModel.put(Common.SIDE_RANDOM_ARTICLES, articleQueryService.getRandomArticles(Symphonys.getInt("sideRandomArticlesCnt")));
    }

    /**
     * Fills tags.
     * 
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    public static void fillSideTags(final Map<String, Object> dataModel) throws Exception {
        dataModel.put(Common.SIDE_TAGS, tagQueryService.getTags(Symphonys.getInt("sideTagsCnt")));
    }

    /**
     * Fills header.
     * 
     * @param request the specified request
     * @param response the specified response
     * @param dataModel the specified data model
     * @throws Exception exception 
     */
    public static void fillHeader(final HttpServletRequest request, final HttpServletResponse response,
                                  final Map<String, Object> dataModel) throws Exception {
        fillMinified(dataModel);
        Keys.fillServer(dataModel);
        dataModel.put(Common.STATIC_RESOURCE_VERSION, Latkes.getStaticResourceVersion());

        fillTrendTags(dataModel);
        fillPersonalNav(request, response, dataModel);

        fillLangs(dataModel);
    }

    /**
     * Fills footer.
     * 
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    public static void fillFooter(final Map<String, Object> dataModel) throws Exception {
        fillSysInfo(dataModel);
    }

    /**
     * Fills personal navigation.
     * 
     * @param request the specified request
     * @param response the specified response
     * @param dataModel the specified data model
     */
    private static void fillPersonalNav(final HttpServletRequest request, final HttpServletResponse response,
                                        final Map<String, Object> dataModel) {
        dataModel.put(Common.IS_LOGGED_IN, false);

        JSONObject currentUser = Sessions.currentUser(request);
        if (null == currentUser && !LoginProcessor.tryLogInWithCookie(request, response)) {
            dataModel.put("loginLabel", langPropsService.get("loginLabel"));
            
            return;
        }

        currentUser = Sessions.currentUser(request);
        
        dataModel.put(Common.IS_LOGGED_IN, true);
        dataModel.put(Common.LOGOUT_URL, userService.createLogoutURL("/"));

        dataModel.put("logoutLabel", langPropsService.get("logoutLabel"));

        final String userName = currentUser.optString(User.USER_NAME);
        dataModel.put(User.USER_NAME, userName);
    }

    /**
     * Fills minified directory and file postfix for static JavaScript, CSS.
     * 
     * @param dataModel the specified data model
     */
    public static void fillMinified(final Map<String, Object> dataModel) {
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
    private static void fillLangs(final Map<String, Object> dataModel) {
        dataModel.putAll(langPropsService.getAll(Latkes.getLocale()));
    }

    /**
     * Fills trend tags.
     * 
     * @param dataModel the specified data model
     * @throws Exception exception
     */
    private static void fillTrendTags(final Map<String, Object> dataModel) throws Exception {
        dataModel.put(Common.TREND_TAGS, tagQueryService.getTrendTags(Symphonys.getInt("trendTagsCnt")));
    }

    /**
     * Fills system info.
     * 
     * @param dataModel the specified data model
     * @throws Exception exception 
     */
    private static void fillSysInfo(final Map<String, Object> dataModel) throws Exception {
        dataModel.put(Common.VERSION, SymphonyServletListener.VERSION);
        dataModel.put(Common.ONLINE_VISITOR_CNT, OptionQueryService.getInstance().getOnlineVisitorCount());

        final JSONObject statistic = optionQueryService.getStatistic();
        dataModel.put(Option.CATEGORY_C_STATISTIC, statistic);
    }

    /**
     * Private constructor.
     */
    private Filler() {
    }
}
