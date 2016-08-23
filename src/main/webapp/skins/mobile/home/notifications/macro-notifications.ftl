<#macro notifications type>
<#include "../../macro-head.ftl">
<#include "../../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${messageLabel} - ${userName} - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
    </head>
    <body>
        <#include "../../header.ftl">
        <div class="main">
            <div class="tab-current fn-clear">
                <div onclick="$(this).next().next().toggle()">
                    <#if type == "commented">
                    ${notificationCommentedLabel}
                    <#elseif type == "at">
                    ${notificationAtLabel}
                    <#elseif type == "followingUser">
                    ${notificationFollowingUserLabel}
                    <#elseif type == "point">
                    ${pointLabel}
                    <#elseif type == "broadcast">
                    ${sameCityLabel}
                    </#if>
                    <span class="icon-chevron-down fn-right"></span>
                </div>
                <div class="fn-hr5"></div>
                <ul class="tab fn-clear fn-none notification-tab">
                    <li<#if type == "commented"> class="fn-none"</#if>>
                        <a href="${servePath}/notifications/commented">
                            <span>${notificationCommentedLabel}</span>
                            <#if unreadCommentedNotificationCnt &gt; 0>
                            <span class="counter">${unreadCommentedNotificationCnt}</span>
                            <span class="ft-gray fn-right" onclick="Util.makeNotificationRead('commented')" class="fn-right">
                                ${makeAsReadLabel}
                            </span>
                            </#if>
                        </a> 
                    </li>
                    <li<#if type == "at"> class="fn-none"</#if>>
                        <a href="${servePath}/notifications/at">
                            <span>${notificationAtLabel}</span>
                            <#if unreadAtNotificationCnt &gt; 0>
                            <span class="counter">${unreadAtNotificationCnt}</span>
                            <span class="ft-gray fn-right" onclick="Util.makeNotificationRead('at')" class="fn-right">
                                ${makeAsReadLabel}
                            </span>
                            </#if>
                        </a>
                    </li>
                    <li<#if type == "followingUser"> class="fn-none"</#if>>
                        <a href="${servePath}/notifications/following-user">
                            <span>${notificationFollowingUserLabel}</span>
                            <#if unreadFollowingUserNotificationCnt &gt; 0>
                            <span class="counter">${unreadFollowingUserNotificationCnt}</span>
                            <span class="ft-gray fn-right" onclick="Util.makeNotificationRead('followingUser')" class="fn-right">
                                ${makeAsReadLabel}
                            </span>
                            </#if>
                        </a>
                    </li>
                    <li<#if type == "point"> class="fn-none"</#if>>
                        <a href="${servePath}/notifications/point">
                            <span>${pointLabel}</span>
                            <#if unreadPointNotificationCnt &gt; 0>
                            <span class="counter">${unreadPointNotificationCnt}</span>
                            </#if>
                        </a>
                    </li>
                    <li<#if type == "broadcast"> class="fn-none"</#if>>
                        <a href="${servePath}/notifications/broadcast">
                            <span>${sameCityLabel}</span>
                            <#if unreadBroadcastNotificationCnt &gt; 0>
                            <span class="counter">${unreadBroadcastNotificationCnt}</span>
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
