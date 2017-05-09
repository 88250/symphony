<#macro notifications type>
<#include "../../macro-head.ftl">
<#include "../../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${messageLabel} - ${currentUser.userName} - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/home.css?${staticResourceVersion}" />
        <link rel="stylesheet" href="${staticServePath}/js/lib/highlight.js-9.6.0/styles/github.css">
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
                                <svg height="18" viewBox="0 0 12 16" width="12">${checkIcon}</svg>
                            </span>
                            </#if>
                        </div>
                        <div class="module-panel">
                            <nav class="home-menu">
                                <a href="${servePath}/notifications/commented"<#if type == "commented"> class="current"</#if>>
                                   <span>${notificationCommentedLabel}</span>
                                    <#if unreadCommentedNotificationCnt &gt; 0>
                                    <span class="count">${unreadCommentedNotificationCnt}</span>
                                    <span onclick="Util.makeNotificationRead('commented')"
                                          aria-label="${makeAsReadLabel}" class="fn-right tooltipped tooltipped-w">
                                        <svg height="18" viewBox="0 0 12 16" width="12">${checkIcon}</svg>
                                    </span>
                                    </#if>
                                </a> 
                                <a href="${servePath}/notifications/reply"<#if type == "reply"> class="current"</#if>>
                                   <span>${notificationReplyLabel}</span>
                                    <#if unreadReplyNotificationCnt &gt; 0>
                                    <span class="count">${unreadReplyNotificationCnt}</span>
                                    <span onclick="Util.makeNotificationRead('reply')"
                                          aria-label="${makeAsReadLabel}" class="fn-right tooltipped tooltipped-w">
                                        <svg height="18" viewBox="0 0 12 16" width="12">${checkIcon}</svg>
                                    </span>
                                    </#if>
                                </a> 
                                <a href="${servePath}/notifications/at"<#if type == "at"> class="current"</#if>>
                                   <span>${notificationAtLabel}</span>
                                    <#if unreadAtNotificationCnt &gt; 0>
                                    <span class="count">${unreadAtNotificationCnt}</span>
                                    <span onclick="Util.makeNotificationRead('at')"
                                          aria-label="${makeAsReadLabel}" class="fn-right tooltipped tooltipped-w">
                                        <svg height="18" viewBox="0 0 12 16" width="12">${checkIcon}</svg>
                                    </span>
                                    </#if>
                                </a>
                                <a href="${servePath}/notifications/following"<#if type == "following"> class="current"</#if>>
                                   <span>${notificationFollowingLabel}</span>
                                    <#if unreadFollowingNotificationCnt &gt; 0>
                                    <span class="count">${unreadFollowingNotificationCnt}</span>
                                    <span onclick="Util.makeNotificationRead('following')"
                                          aria-label="${makeAsReadLabel}" class="fn-right tooltipped tooltipped-w">
                                        <svg height="18" viewBox="0 0 12 16" width="12">${checkIcon}</svg>
                                    </span>
                                    </#if>
                                </a>
                                <a href="${servePath}/notifications/point"<#if type == "point"> class="current"</#if>>
                                   <span>${pointLabel}</span>
                                    <#if unreadPointNotificationCnt &gt; 0>
                                    <span class="count">${unreadPointNotificationCnt}</span>
                                    </#if>
                                </a>
                                <a href="${servePath}/notifications/broadcast"<#if type == "broadcast"> class="current"</#if>>
                                   <span>${sameCityLabel}</span>
                                    <#if unreadBroadcastNotificationCnt &gt; 0>
                                    <span class="count">${unreadBroadcastNotificationCnt}</span>
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
            Settings.initHljs();
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
