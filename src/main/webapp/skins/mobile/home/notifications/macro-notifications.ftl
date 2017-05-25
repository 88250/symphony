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
    </head>
    <body>
        <#include "../../header.ftl">
        <div class="main">
            <div class="tab-current fn-clear">
                <div class="fn-hr5"></div>
                <div onclick="$(this).next().next().slideToggle()">
                    <#if type == "commented">
                    ${notificationCommentedLabel}
                    <#elseif type == "reply">
                    ${notificationReplyLabel}
                    <#elseif type == "at">
                    ${notificationAtLabel}
                    <#elseif type == "following">
                    ${notificationFollowingLabel}
                    <#elseif type == "point">
                    ${pointLabel}
                    <#elseif type == "broadcast">
                    ${sameCityLabel}
                    <#elseif type == "sysAnnounce">
                    ${systemLabel}
                    </#if>
                    <svg class="fn-right"><use xlink:href="#chevron-down"></use></svg>
                </div>
                <div class="fn-hr5"></div>
                <ul class="tab fn-clear fn-none notification-tab">
                    <li<#if type == "commented"> class="fn-none"</#if>>
                        <a href="${servePath}/notifications/commented">
                            <span>${notificationCommentedLabel}</span>
                            <#if unreadCommentedNotificationCnt &gt; 0>
                            <span class="count">${unreadCommentedNotificationCnt}</span>
                            <span class="ft-gray fn-right" onclick="Settings.makeNotificationRead('commented')" class="fn-right">
                                ${makeAsReadLabel}
                            </span>
                            </#if>
                        </a>
                    </li>
                    <li<#if type == "reply"> class="fn-none"</#if>>
                        <a href="${servePath}/notifications/reply">
                            <span>${notificationReplyLabel}</span>
                            <#if unreadReplyNotificationCnt &gt; 0>
                            <span class="count">${unreadReplyNotificationCnt}</span>
                            <span class="ft-gray fn-right" onclick="Util.makeNotificationRead('reply')" class="fn-right">
                                ${makeAsReadLabel}
                            </span>
                            </#if>
                        </a> 
                    </li>
                    <li<#if type == "at"> class="fn-none"</#if>>
                        <a href="${servePath}/notifications/at">
                            <span>${notificationAtLabel}</span>
                            <#if unreadAtNotificationCnt &gt; 0>
                            <span class="count">${unreadAtNotificationCnt}</span>
                            <span class="ft-gray fn-right" onclick="Util.makeNotificationRead('at')" class="fn-right">
                                ${makeAsReadLabel}
                            </span>
                            </#if>
                        </a>
                    </li>
                    <li<#if type == "followingUser"> class="fn-none"</#if>>
                        <a href="${servePath}/notifications/following">
                            <span>${notificationFollowingLabel}</span>
                            <#if unreadFollowingNotificationCnt &gt; 0>
                            <span class="count">${unreadFollowingNotificationCnt}</span>
                            <span class="ft-gray fn-right" onclick="Util.makeNotificationRead('following')" class="fn-right">
                                ${makeAsReadLabel}
                            </span>
                            </#if>
                        </a>
                    </li>
                    <li<#if type == "point"> class="fn-none"</#if>>
                        <a href="${servePath}/notifications/point">
                            <span>${pointLabel}</span>
                            <#if unreadPointNotificationCnt &gt; 0>
                            <span class="count">${unreadPointNotificationCnt}</span>
                            </#if>
                        </a>
                    </li>
                    <li<#if type == "broadcast"> class="fn-none"</#if>>
                        <a href="${servePath}/notifications/broadcast">
                            <span>${sameCityLabel}</span>
                            <#if unreadBroadcastNotificationCnt &gt; 0>
                            <span class="count">${unreadBroadcastNotificationCnt}</span>
                            </#if>
                        </a>
                    </li>
                    <li<#if type == "sysAnnounce"> class="fn-none"</#if>>
                        <a href="${servePath}/notifications/sys-announce">
                            <span>${systemLabel}</span>
                            <#if unreadSysAnnounceNotificationCnt &gt; 0>
                            <span class="count">${unreadSysAnnounceNotificationCnt}</span>
                            </#if>
                        </a>
                    </li>
                </ul>
            </div>
            <div class="list content">
                <#nested>
            </div>
        </div>
        <#include "../../footer.ftl">
    </body>
</html>
</#macro>
