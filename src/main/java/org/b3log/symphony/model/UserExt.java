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
package org.b3log.symphony.model;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.model.User;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * This class defines ext of user model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author Bill Ho
 * @version 2.15.0.0, Aug 29, 2018
 * @see org.b3log.latke.model.User
 * @since 0.2.0
 */
public final class UserExt {

    /**
     * Key of status of using forward page.
     */
    public static final String USER_FORWARD_PAGE_STATUS = "userForwardPageStatus";

    /**
     * Key of user guide step.
     */
    public static final String USER_GUIDE_STEP = "userGuideStep";

    /**
     * Key of user language.
     */
    public static final String USER_LANGUAGE = "userLanguage";

    /**
     * Key of user timezone.
     */
    public static final String USER_TIMEZONE = "userTimezone";

    /**
     * Key of user keyboard shortcuts status.
     */
    public static final String USER_KEYBOARD_SHORTCUTS_STATUS = "userKeyboardShortcutsStatus";

    /**
     * Key of user subscription mail status.
     */
    public static final String USER_SUB_MAIL_STATUS = "userSubMailStatus";

    /**
     * Key of user auto watch article after reply status.
     */
    public static final String USER_REPLY_WATCH_ARTICLE_STATUS = "userReplyWatchArticleStatus";

    /**
     * Key of user subscription mail send time.
     */
    public static final String USER_SUB_MAIL_SEND_TIME = "userSubMailSendTime";

    /**
     * Key of user avatar view mode.
     */
    public static final String USER_AVATAR_VIEW_MODE = "userAvatarViewMode";

    /**
     * Key of user list page size.
     */
    public static final String USER_LIST_PAGE_SIZE = "userListPageSize";

    /**
     * Key of user list view mode.
     */
    public static final String USER_LIST_VIEW_MODE = "userListViewMode";

    /**
     * Key of user breezemoons status
     */
    public static final String USER_BREEZEMOON_STATUS = "userBreezemoonStatus";

    /**
     * Key of user point status.
     */
    public static final String USER_POINT_STATUS = "userPointStatus";

    /**
     * Key of user follower status.
     */
    public static final String USER_FOLLOWER_STATUS = "userFollowerStatus";

    /**
     * Key of user following article status.
     */
    public static final String USER_FOLLOWING_ARTICLE_STATUS = "userFollowingArticleStatus";

    /**
     * Key of user watching article status.
     */
    public static final String USER_WATCHING_ARTICLE_STATUS = "userWatchingArticleStatus";

    /**
     * Key of user following tag status.
     */
    public static final String USER_FOLLOWING_TAG_STATUS = "userFollowingTagStatus";

    /**
     * Key of user following user status.
     */
    public static final String USER_FOLLOWING_USER_STATUS = "userFollowingUserStatus";

    /**
     * Key of user comment status.
     */
    public static final String USER_COMMENT_STATUS = "userCommentStatus";

    /**
     * Key of user article status.
     */
    public static final String USER_ARTICLE_STATUS = "userArticleStatus";

    /**
     * Key of user online status.
     */
    public static final String USER_ONLINE_STATUS = "userOnlineStatus";

    /**
     * Key of user User-Agent status.
     */
    public static final String USER_UA_STATUS = "userUAStatus";

    /**
     * Key of user notify status.
     */
    public static final String USER_NOTIFY_STATUS = "userNotifyStatus";

    /**
     * Key of user nickname.
     */
    public static final String USER_NICKNAME = "userNickname";

    /**
     * Key of user comment view mode.
     */
    public static final String USER_COMMENT_VIEW_MODE = "userCommentViewMode";

    /**
     * Key of user geo status.
     */
    public static final String USER_GEO_STATUS = "userGeoStatus";

    /**
     * Key of user update time.
     */
    public static final String USER_UPDATE_TIME = "userUpdateTime";

    /**
     * Key of user city.
     */
    public static final String USER_CITY = "userCity";

    /**
     * Key of user country.
     */
    public static final String USER_COUNTRY = "userCountry";

    /**
     * Key of user province.
     */
    public static final String USER_PROVINCE = "userProvince";

    /**
     * Key of user skin.
     */
    public static final String USER_SKIN = "userSkin";

    /**
     * Key of mobile user skin.
     */
    public static final String USER_MOBILE_SKIN = "userMobileSkin";

    /**
     * Key of user checkin time.
     */
    public static final String USER_CHECKIN_TIME = "userCheckinTime";

    /**
     * Key of user longest checkin streak start.
     */
    public static final String USER_LONGEST_CHECKIN_STREAK_START = "userLongestCheckinStreakStart";

    /**
     * Key of user longest checkin streak end.
     */
    public static final String USER_LONGEST_CHECKIN_STREAK_END = "userLongestCheckinStreakEnd";

    /**
     * Key of user current checkin streak start.
     */
    public static final String USER_CURRENT_CHECKIN_STREAK_START = "userCurrentCheckinStreakStart";

    /**
     * Key of user current checkin streak start end.
     */
    public static final String USER_CURRENT_CHECKIN_STREAK_END = "userCurrentCheckinStreakEnd";

    /**
     * Key of user longest checkin streak.
     */
    public static final String USER_LONGEST_CHECKIN_STREAK = "userLongestCheckinStreak";

    /**
     * Key of user current checkin streak.
     */
    public static final String USER_CURRENT_CHECKIN_STREAK = "userCurrentCheckinStreak";

    /**
     * Key of user article count.
     */
    public static final String USER_ARTICLE_COUNT = "userArticleCount";

    /**
     * Key of user comment count.
     */
    public static final String USER_COMMENT_COUNT = "userCommentCount";

    /**
     * Key of new tag count.
     */
    public static final String USER_TAG_COUNT = "userTagCount";

    /**
     * Key of user status.
     */
    public static final String USER_STATUS = "userStatus";

    /**
     * Key of user point.
     */
    public static final String USER_POINT = "userPoint";

    /**
     * Key of user used point.
     */
    public static final String USER_USED_POINT = "userUsedPoint";

    /**
     * Key of user join point rank.
     */
    public static final String USER_JOIN_POINT_RANK = "userJoinPointRank";

    /**
     * Key of user join used point rank.
     */
    public static final String USER_JOIN_USED_POINT_RANK = "userJoinUsedPointRank";

    /**
     * Key of user tags.
     */
    public static final String USER_TAGS = "userTags";

    /**
     * Key of user QQ.
     */
    public static final String USER_QQ = "userQQ";

    /**
     * Key of user number.
     */
    public static final String USER_NO = "userNo";

    /**
     * Key of user intro.
     */
    public static final String USER_INTRO = "userIntro";

    /**
     * Key of user avatar type.
     */
    public static final String USER_AVATAR_TYPE = "userAvatarType";

    /**
     * Key of user avatar URL.
     */
    public static final String USER_AVATAR_URL = "userAvatarURL";

    /**
     * Key of online flag.
     */
    public static final String USER_ONLINE_FLAG = "userOnlineFlag";

    /**
     * Key of latest post article time.
     */
    public static final String USER_LATEST_ARTICLE_TIME = "userLatestArticleTime";

    /**
     * Key of latest comment time.
     */
    public static final String USER_LATEST_CMT_TIME = "userLatestCmtTime";

    /**
     * Key of latest login time.
     */
    public static final String USER_LATEST_LOGIN_TIME = "userLatestLoginTime";

    /**
     * Key of latest login IP.
     */
    public static final String USER_LATEST_LOGIN_IP = "userLatestLoginIP";

    /**
     * Key of app role.
     */
    public static final String USER_APP_ROLE = "userAppRole";

    //// Transient ////
    /**
     * Key of user create time.
     */
    public static final String USER_T_CREATE_TIME = "userCreateTime";

    /**
     * Key of user point in Hex.
     */
    public static final String USER_T_POINT_HEX = "userPointHex";

    /**
     * Key of user point in Color Code.
     */
    public static final String USER_T_POINT_CC = "userPointCC";

    /**
     * Key of user name lower case.
     */
    public static final String USER_T_NAME_LOWER_CASE = "userNameLowerCase";

    /**
     * Key of user id.
     */
    public static final String USER_T_ID = "userId";

    //// User subscription mail status constants
    /**
     * User subscription mail status - enabled.
     */
    public static final int USER_SUB_MAIL_STATUS_ENABLED = 0;

    /**
     * User subscription mail status - disabled.
     */
    public static final int USER_SUB_MAIL_STATUS_DISABLED = 1;

    //// User guide step constants
    /**
     * User guide step - finish.
     */
    public static final int USER_GUIDE_STEP_FIN = 0;

    /**
     * User guide step - upload avatar.
     */
    public static final int USER_GUIDE_STEP_UPLOAD_AVATAR = 1;

    /**
     * User guide step - follow tags.
     */
    public static final int USER_GUIDE_STEP_FOLLOW_TAGS = 2;

    /**
     * User guide step - follow users.
     */
    public static final int USER_GUIDE_STEP_FOLLOW_USERS = 3;

    /**
     * User guide step - star project.
     */
    public static final int USER_GUIDE_STEP_STAR_PROJECT = 4;

    //// Email constant
    /**
     * Builtin email suffix.
     */
    public static final String USER_BUILTIN_EMAIL_SUFFIX = "@sym.b3log.org";

    //// Community Bot constants
    /**
     * Bot name.
     */
    public static final String COM_BOT_NAME = "ComBot";

    /**
     * Bot email.
     */
    public static final String COM_BOT_EMAIL = "combot" + USER_BUILTIN_EMAIL_SUFFIX;

    //// Null user
    /**
     * Null user name.
     */
    public static final String NULL_USER_NAME = "_";

    //// Anonymous user.
    /**
     * Anonymous user name.
     */
    public static final String ANONYMOUS_USER_NAME = "someone";

    /**
     * Anonymous user id.
     */
    public static final String ANONYMOUS_USER_ID = "0";

    //// Status constants
    /**
     * User status - valid.
     */
    public static final int USER_STATUS_C_VALID = 0;

    /**
     * User status - invalid.
     */
    public static final int USER_STATUS_C_INVALID = 1;

    /**
     * User status - registered but not verified.
     */
    public static final int USER_STATUS_C_NOT_VERIFIED = 2;

    /**
     * User status - invalid login.
     */
    public static final int USER_STATUS_C_INVALID_LOGIN = 3;

    /**
     * User status - deactivated.
     */
    public static final int USER_STATUS_C_DEACTIVATED = 4;

    //// Join point rank constants
    /**
     * User join point rank - join.
     */
    public static final int USER_JOIN_POINT_RANK_C_JOIN = 0;

    /**
     * User join point rank - not join.
     */
    public static final int USER_JOIN_POINT_RANK_C_NOT_JOIN = 1;

    /**
     * User join used point rank - join.
     */
    public static final int USER_JOIN_USED_POINT_RANK_C_JOIN = 0;

    /**
     * User join used point rank - not join.
     */
    public static final int USER_JOIN_USED_POINT_RANK_C_NOT_JOIN = 1;

    //// User XXX Status constants
    /**
     * User XXX (notify/point/follower/following article/following tag/following user/comment/article/breezemoon) status - public.
     */
    public static final int USER_XXX_STATUS_C_PUBLIC = 0;

    /**
     * User XXX (notify/point/follower/following article/watching article/following tag/following user/comment/article/breezemoon) status - private.
     */
    public static final int USER_XXX_STATUS_C_PRIVATE = 1;

    /**
     * User XXX status - enabled.
     */
    public static final int USER_XXX_STATUS_C_ENABLED = 0;

    /**
     * User XXX status - disabled.
     */
    public static final int USER_XXX_STATUS_C_DISABLED = 1;

    //// Avatar View Mode constants
    /**
     * User avatar view mode - original.
     */
    public static final int USER_AVATAR_VIEW_MODE_C_ORIGINAL = 0;

    /**
     * User avatar view mode - static.
     */
    public static final int USER_AVATAR_VIEW_MODE_C_STATIC = 1;

    //// Comment View Mode constants
    /**
     * User comment view mode - traditional.
     */
    public static final int USER_COMMENT_VIEW_MODE_C_TRADITIONAL = 0;

    /**
     * User comment view mode - real time.
     */
    public static final int USER_COMMENT_VIEW_MODE_C_REALTIME = 1;

    //// Geo Status constants
    /**
     * User geo status - public.
     */
    public static final int USER_GEO_STATUS_C_PUBLIC = 0;

    /**
     * User geo status - private.
     */
    public static final int USER_GEO_STATUS_C_PRIVATE = 1;

    //// Avatar type constants
    /**
     * User avatar type - Gravatar.
     *
     * @deprecated only upload allowed since 1.3.0
     */
    public static final int USER_AVATAR_TYPE_C_GRAVATAR = 0;

    /**
     * User avatar type - External Link.
     *
     * @deprecated only upload allowed since 1.3.0
     */
    public static final int USER_AVATAR_TYPE_C_EXTERNAL_LINK = 1;

    /**
     * User avatar type - Upload.
     */
    public static final int USER_AVATAR_TYPE_C_UPLOAD = 2;

    //// App role constants
    /**
     * User app role - Hacker.
     */
    public static final int USER_APP_ROLE_C_HACKER = 0;

    /**
     * User app role - Painter.
     */
    public static final int USER_APP_ROLE_C_PAINTER = 1;

    //// List view mode constants
    /**
     * List view mode - Only title.
     */
    public static final int USER_LIST_VIEW_MODE_TITLE = 0;

    /**
     * List view mode - Title & Abstract.
     */
    public static final int USER_LIST_VIEW_MODE_TITLE_ABSTRACT = 1;

    /**
     * Private constructor.
     */
    private UserExt() {
    }

    /**
     * Gets color code of the specified point.
     *
     * @param point the specified point
     * @return color code
     */
    public static String toCCString(final int point) {
        final String hex = Integer.toHexString(point);

        if (1 == hex.length()) {
            return hex + hex + hex + hex + hex + hex;
        }

        if (2 == hex.length()) {
            final String a1 = hex.substring(0, 1);
            final String a2 = hex.substring(1);

            return a1 + a1 + a1 + a2 + a2 + a2;
        }

        if (3 == hex.length()) {
            final String a1 = hex.substring(0, 1);
            final String a2 = hex.substring(1, 2);
            final String a3 = hex.substring(2);

            return a1 + a1 + a2 + a2 + a3 + a3;
        }

        if (4 == hex.length()) {
            final String a1 = hex.substring(0, 1);
            final String a2 = hex.substring(1, 2);
            final String a3 = hex.substring(2, 3);
            final String a4 = hex.substring(3);

            return a1 + a2 + a3 + a4 + a3 + a4;
        }

        if (5 == hex.length()) {
            final String a1 = hex.substring(0, 1);
            final String a2 = hex.substring(1, 2);
            final String a3 = hex.substring(2, 3);
            final String a4 = hex.substring(3, 4);
            final String a5 = hex.substring(4);

            return a1 + a2 + a3 + a4 + a5 + a5;
        }

        if (6 == hex.length()) {
            return hex;
        }

        return hex.substring(0, 6);
    }


    /**
     * Checks the specified email whether in a whitelist mail domain.
     *
     * @param email the specified email
     * @return {@code true} if it is, returns {@code false} otherwise
     */
    public static boolean isWhitelistMailDomain(final String email) {
        final String whitelistMailDomains = Symphonys.get("whitelist.mailDomains");
        if (StringUtils.isBlank(whitelistMailDomains)) {
            return true;
        }

        final String domain = StringUtils.substringAfter(email, "@");

        return StringUtils.containsIgnoreCase(whitelistMailDomains, domain);
    }

    /**
     * Checks the specified user name whether is a reserved user name.
     *
     * @param userName the specified username
     * @return {@code true} if it is, returns {@code false} otherwise
     */
    public static boolean isReservedUserName(final String userName) {
        for (final String reservedUserName : Symphonys.RESERVED_USER_NAMES) {
            if (StringUtils.equalsIgnoreCase(userName, reservedUserName)) {
                return true;
            }
        }

        return StringUtils.containsIgnoreCase(userName, UserExt.ANONYMOUS_USER_NAME);

    }

    /**
     * Checks whether the specified user updated avatar.
     *
     * @param user the specified user
     * @return {@code true} if the specified user updated avatar, returns {@code false} otherwise
     */
    public static boolean updatedAvatar(final JSONObject user) {
        return user.optString(UserExt.USER_AVATAR_URL).contains("_");
    }

    /**
     * Checks whether the specified user finshed guide.
     *
     * @param user the specified user
     * @return {@code true} if the specified user finshed guide, returns {@code false} otherwise
     */
    public static boolean finshedGuide(final JSONObject user) {
        return UserExt.USER_GUIDE_STEP_FIN == user.optInt(UserExt.USER_GUIDE_STEP);
    }

    /**
     * Gets user link with the specified user.
     *
     * @param user the specified user
     * @return user link
     */
    public static String getUserLink(final JSONObject user) {
        return getUserLink(user.optString(User.USER_NAME));
    }

    /**
     * Gets user link with the specified user name.
     *
     * @param userName the specified user name
     * @return user link
     */
    public static String getUserLink(final String userName) {
        return "<a href=\"" + Latkes.getServePath() + "/member/" + userName + "\">" + userName + "</a> ";
    }
}
