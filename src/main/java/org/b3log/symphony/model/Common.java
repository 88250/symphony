/*
 * Copyright (c) 2012-2015, b3log.org
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
package org.b3log.symphony.model;

/**
 * This class defines all common model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.15.1.10, Jul 23, 2015
 * @since 0.2.0
 */
public final class Common {
    
    /**
     * Key of closed 1A0001.
     */
    public static final String CLOSED_1A0001 = "closed1A0001";

    /**
     * Key of closed.
     */
    public static final String CLOSED = "closed";
    
    /**
     * Key of end.
     */
    public static final String END = "end";

    /**
     * Key of hour.
     */
    public static final String HOUR = "hour";

    /**
     * Key of collected.
     */
    public static final String COLLECTED = "collected";

    /**
     * Key of participated.
     */
    public static final String PARTICIPATED = "participated";

    /**
     * Key of to user.
     */
    public static final String TO_USER = "toUser";

    /**
     * Key of amount.
     */
    public static final String AMOUNT = "amount";

    /**
     * Key of small or large.
     */
    public static final String SMALL_OR_LARGE = "smallOrLarge";

    /**
     * Key of is daily checkin.
     */
    public static final String IS_DAILY_CHECKIN = "isDailyCheckin";

    /**
     * Key of mini postfix.
     */
    public static final String MINI_POSTFIX = "miniPostfix";

    /**
     * Value of mini postfix.
     */
    public static final String MINI_POSTFIX_VALUE = ".min";

    /**
     * Static resource version.
     */
    public static final String STATIC_RESOURCE_VERSION = "staticResourceVersion";

    /**
     * Key of go to.
     */
    public static final String GOTO = "goto";

    /**
     * Key of current user.
     */
    public static final String CURRENT_USER = "currentUser";

    /**
     * Key of is logged in.
     */
    public static final String IS_LOGGED_IN = "isLoggedIn";

    /**
     * Key of is admin.
     */
    public static final String IS_ADMIN_LOGGED_IN = "isAdminLoggedIn";

    /**
     * Key of is following.
     */
    public static final String IS_FOLLOWING = "isFollowing";

    /**
     * Key of is my article.
     */
    public static final String IS_MY_ARTICLE = "isMyArticle";

    /**
     * Key of logout URL.
     */
    public static final String LOGOUT_URL = "logoutURL";

    /**
     * Key of type.
     */
    public static final String TYPE = "type";

    /**
     * Key of recent articles.
     */
    public static final String RECENT_ARTICLES = "recentArticles";

    /**
     * Key of side tags.
     */
    public static final String SIDE_TAGS = "sideTags";

    /**
     * Key of navigation trend tags.
     */
    public static final String NAV_TREND_TAGS = "navTrendTags";

    /**
     * Key of trend tags.
     */
    public static final String TREND_TAGS = "trendTags";

    /**
     * Key of cold tags.
     */
    public static final String COLD_TAGS = "coldTags";

    /**
     * Key of side hot articles.
     */
    public static final String SIDE_HOT_ARTICLES = "sideHotArticles";

    /**
     * Key of side random articles.
     */
    public static final String SIDE_RANDOM_ARTICLES = "sideRandomArticles";

    /**
     * Key of side relevant articles.
     */
    public static final String SIDE_RELEVANT_ARTICLES = "sideRelevantArticles";

    /**
     * Key of side latest comments.
     */
    public static final String SIDE_LATEST_CMTS = "sideLatestCmts";

    /**
     * Key of latest comment articles.
     */
    public static final String LATEST_CMT_ARTICLES = "latestCmtArticles";

    /**
     * Key of user id.
     */
    public static final String USER_ID = "userId";

    /**
     * Key of user home articles.
     */
    public static final String USER_HOME_ARTICLES = "userHomeArticles";

    /**
     * Key of user home comments.
     */
    public static final String USER_HOME_COMMENTS = "userHomeComments";

    /**
     * Key of user home following users.
     */
    public static final String USER_HOME_FOLLOWING_USERS = "userHomeFollowingUsers";

    /**
     * Key of user home following tags.
     */
    public static final String USER_HOME_FOLLOWING_TAGS = "userHomeFollowingTags";

    /**
     * Key of user home following articles.
     */
    public static final String USER_HOME_FOLLOWING_ARTICLES = "userHomeFollowingArticles";

    /**
     * Key of user home follower users.
     */
    public static final String USER_HOME_FOLLOWER_USERS = "userHomeFollowerUsers";

    /**
     * Key of user home points.
     */
    public static final String USER_HOME_POINTS = "userHomePoints";

    /**
     * Key of 'commented' notifications.
     */
    public static final String COMMENTED_NOTIFICATIONS = "commentedNotifications";

    /**
     * Key of unread 'commented' notifications count.
     */
    public static final String UNREAD_COMMENTED_NOTIFICATION_CNT = "unreadCommentedNotificationCnt";

    /**
     * Key of 'at' notifications.
     */
    public static final String AT_NOTIFICATIONS = "atNotifications";

    /**
     * Key of unread 'at' notifications count.
     */
    public static final String UNREAD_AT_NOTIFICATION_CNT = "unreadAtNotificationCnt";

    /**
     * Key of 'followingUser' notifications.
     */
    public static final String FOLLOWING_USER_NOTIFICATIONS = "followingUserNotifications";

    /**
     * Key of follower user count.
     */
    public static final String FOLLOWER_USER_CNT = "followerUserCnt";

    /**
     * Key of following user count.
     */
    public static final String FOLLOWING_USER_CNT = "followingUserCnt";

    /**
     * Key of following article count.
     */
    public static final String FOLLOWING_ARTICLE_CNT = "followingArticleCnt";

    /**
     * Key of following tag count.
     */
    public static final String FOLLOWING_TAG_CNT = "followingTagCnt";

    /**
     * Key of unread 'followingUser' notifications count.
     */
    public static final String UNREAD_FOLLOWING_USER_NOTIFICATION_CNT = "unreadFollowingUserNotificationCnt";

    /**
     * Key of author name.
     */
    public static final String AUTHOR_NAME = "authorName";

    /**
     * Key of content.
     */
    public static final String CONTENT = "content";

    /**
     * Key of thumbnail URL.
     */
    public static final String THUMBNAIL_URL = "thumbnailURL";

    /**
     * Key of URL.
     */
    public static final String URL = "url";

    /**
     * Create time.
     */
    public static final String CREATE_TIME = "createTime";

    /**
     * Key of version.
     */
    public static final String VERSION = "version";

    /**
     * Key of year.
     */
    public static final String YEAR = "year";

    /**
     * Key of site visit statistic code.
     */
    public static final String SITE_VISIT_STAT_CODE = "siteVisitStatCode";

    /**
     * Key of online visitor count.
     */
    public static final String ONLINE_VISITOR_CNT = "onlineVisitorCnt";

    /**
     * Key of article channel count.
     */
    public static final String ARTICLE_CHANNEL_CNT = "articleChannelCnt";

    /**
     * Key of article list channel count.
     */
    public static final String ARTICLE_LIST_CHANNEL_CNT = "articleListChannelCnt";

    /**
     * Key of symphony key.
     */
    public static final String SYMPHONY_KEY = "symphonyKey";

    /**
     * Key of from client.
     */
    public static final String FROM_CLIENT = "fromClient";

    /**
     * Key of article comments page size.
     */
    public static final String ARTICLE_COMMENTS_PAGE_SIZE = "articleCommentsPageSize";

    /**
     * Key of weight.
     */
    public static final String WEIGHT = "weight";

    /**
     * Key of viewable.
     */
    public static final String DISCUSSION_VIEWABLE = "discussionViewable";

    /**
     * Key of usernames.
     */
    public static final String USER_NAMES = "userNames";

    /**
     * Key of username or email.
     */
    public static final String USER_NAME_OR_EMAIL = "userNameOrEmail";

    /**
     * Key of operation.
     */
    public static final String OPERATION = "operation";

    /**
     * Key of rewarded.
     */
    public static final String REWARDED = "rewarded";

    /**
     * Key of display type.
     */
    public static final String DISPLAY_TYPE = "displayType";

    /**
     * Key of description.
     */
    public static final String DESCRIPTION = "description";

    /**
     * Key of balance.
     */
    public static final String BALANCE = "balance";

    /**
     * Key of plus.
     */
    public static final String PLAUS = "plus";

    /**
     * Key of referral.
     */
    public static final String REFERRAL = "referral";

    /**
     * Key of top balance users.
     */
    public static final String TOP_BALANCE_USERS = "topBalanceUsers";

    /**
     * Private constructor.
     */
    private Common() {
    }
}
