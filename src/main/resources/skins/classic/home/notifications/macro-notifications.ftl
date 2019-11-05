<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-present, b3log.org

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#macro notifications type>
<#include "../../macro-head.ftl">
<#include "../../macro-pagination.ftl">
<#include "../../common/title-icon.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${messageLabel} - ${currentUser.userName} - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/home.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="list module">
                        <#nested>
                    </div>
                </div>
                <div class="side fn-none"></div>
                <div class="side" id="side">
                    <#include '../../common/person-info.ftl'/>
                    <div class="module">
                        <div class="module-header fn-clear">
                            <#if unreadNotificationCnt &gt; 0>
                            <span onclick="Settings.makeAllNotificationsRead()"
                                  aria-label="${makeAllAsReadLabel}" class="fn-right tooltipped tooltipped-w home-side-read">
                                <svg><use xlink:href="#check"></use></svg>
                            </span>
                            </#if>
                        </div>
                        <div class="module-panel">
                            <nav class="home-menu">
                                <a href="${servePath}/notifications/commented"<#if type == "commented"> class="current"</#if>>
                                   <span>${notificationCommentedLabel}</span>
                                    <#if unreadCommentedNotificationCnt &gt; 0>
                                    <span class="count">${unreadCommentedNotificationCnt}</span>
                                    <span onclick="Util.makeNotificationRead('commented', this);return false"
                                          aria-label="${makeAsReadLabel}" class="fn-right tooltipped tooltipped-w">
                                        <svg><use xlink:href="#check"></use></svg>
                                    </span>
                                    </#if>
                                    <#if type == "commented" && commentedNotifications?size != 0>
                                    <span class="fn-right">&nbsp;</span>
                                    <span onclick="Settings.removeNotifications('commented');return false"
                                          aria-label="${removeAllLabel}"
                                          class="fn-right ft-red tooltipped tooltipped-w">
                                        <svg><use xlink:href="#remove"></use></svg>
                                    </span>
                                    </#if>
                                </a>
                                <a href="${servePath}/notifications/reply"<#if type == "reply"> class="current"</#if>>
                                    <span>${notificationReplyLabel}</span>
                                    <#if unreadReplyNotificationCnt &gt; 0>
                                    <span class="count">${unreadReplyNotificationCnt}</span>
                                    <span onclick="Util.makeNotificationRead('reply', this);return false"
                                          aria-label="${makeAsReadLabel}" class="fn-right tooltipped tooltipped-w">
                                        <svg><use xlink:href="#check"></use></svg>
                                    </span>
                                    </#if>
                                    <#if type == "reply" && replyNotifications?size != 0>
                                    <span class="fn-right">&nbsp;</span>
                                    <span onclick="Settings.removeNotifications('reply');return false"
                                          aria-label="${removeAllLabel}"
                                          class="fn-right ft-red tooltipped tooltipped-w">
                                        <svg><use xlink:href="#remove"></use></svg>
                                    </span>
                                    </#if>
                                </a>
                                <a href="${servePath}/notifications/at"<#if type == "at"> class="current"</#if>>
                                   <span>${notificationAtLabel}</span>
                                    <#if unreadAtNotificationCnt &gt; 0>
                                    <span class="count">${unreadAtNotificationCnt}</span>
                                    <span onclick="Util.makeNotificationRead('at', this);return false"
                                          aria-label="${makeAsReadLabel}" class="fn-right tooltipped tooltipped-w">
                                        <svg><use xlink:href="#check"></use></svg>
                                    </span>
                                    </#if>
                                    <#if type == "at" && atNotifications?size != 0>
                                    <span class="fn-right">&nbsp;</span>
                                    <span onclick="Settings.removeNotifications('at');return false"
                                          aria-label="${removeAllLabel}"
                                          class="fn-right ft-red tooltipped tooltipped-w">
                                        <svg><use xlink:href="#remove"></use></svg>
                                    </span>
                                    </#if>
                                </a>
                                <a href="${servePath}/notifications/following"<#if type == "following"> class="current"</#if>>
                                   <span>${notificationFollowingLabel}</span>
                                    <#if unreadFollowingNotificationCnt &gt; 0>
                                    <span class="count">${unreadFollowingNotificationCnt}</span>
                                    <span onclick="Util.makeNotificationRead('following', this);return false"
                                          aria-label="${makeAsReadLabel}" class="fn-right tooltipped tooltipped-w">
                                        <svg><use xlink:href="#check"></use></svg>
                                    </span>
                                    </#if>
                                    <#if type == "following" && followingNotifications?size != 0>
                                    <span class="fn-right">&nbsp;</span>
                                    <span onclick="Settings.removeNotifications('following');return false"
                                          aria-label="${removeAllLabel}"
                                          class="fn-right ft-red tooltipped tooltipped-w">
                                        <svg><use xlink:href="#remove"></use></svg>
                                    </span>
                                    </#if>
                                </a>
                                <a href="${servePath}/notifications/point"<#if type == "point"> class="current"</#if>>
                                   <span>${pointLabel}</span>
                                    <#if unreadPointNotificationCnt &gt; 0>
                                    <span class="count">${unreadPointNotificationCnt}</span>
                                    </#if>
                                    <#if type == "point" && pointNotifications?size != 0>
                                    <span class="fn-right">&nbsp;</span>
                                    <span onclick="Settings.removeNotifications('point');return false"
                                          aria-label="${removeAllLabel}"
                                          class="fn-right ft-red tooltipped tooltipped-w">
                                        <svg><use xlink:href="#remove"></use></svg>
                                    </span>
                                    </#if>
                                </a>
                                <a href="${servePath}/notifications/broadcast"<#if type == "broadcast"> class="current"</#if>>
                                   <span>${sameCityLabel}</span>
                                    <#if unreadBroadcastNotificationCnt &gt; 0>
                                    <span class="count">${unreadBroadcastNotificationCnt}</span>
                                    </#if>
                                    <#if type == "broadcast" && broadcastNotifications?size != 0>
                                    <span class="fn-right">&nbsp;</span>
                                    <span onclick="Settings.removeNotifications('broadcast');return false"
                                          aria-label="${removeAllLabel}"
                                          class="fn-right ft-red tooltipped tooltipped-w">
                                        <svg><use xlink:href="#remove"></use></svg>
                                    </span>
                                    </#if>
                                </a>
                                <a href="${servePath}/notifications/sys-announce"<#if type == "sysAnnounce"> class="current"</#if>>
                                   <span>${systemLabel}</span>
                                    <#if unreadSysAnnounceNotificationCnt &gt; 0>
                                    <span class="count">${unreadSysAnnounceNotificationCnt}</span>
                                    </#if>
                                </a>
                            </nav>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <#include "../../footer.ftl">
        <script src="${staticServePath}/js/settings${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            Util.parseHljs()
            Util.parseMarkdown()
            $(document).bind('keyup', 'e', function assets() {
                if (!Label.userKeyboardShortcutsStatus || Label.userKeyboardShortcutsStatus === '1') {
                    return false;
                }
                $('.home-menu .current .tooltipped').click();
                return false;
            });
        </script>
    </body>
</html>
</#macro>
