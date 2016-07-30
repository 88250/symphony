<#macro notifications type>
<#include "../../macro-head.ftl">
<#include "../../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${messageLabel} - ${userName} - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/home${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="responsive-show">
                    <ul class="tab fn-clear">
                        <li<#if type == "commented"> class="current"</#if>>
                            <a href="${servePath}/notifications/commented">
                                <span>${notificationCommentedLabel}</span>
                                <#if unreadCommentedNotificationCnt &gt; 0>
                                <span class="counter">${unreadCommentedNotificationCnt}</span>
                                </#if>
                            </a> 
                        </li>
                        <li<#if type == "at"> class="current"</#if>>
                            <a href="${servePath}/notifications/at">
                                <span>${notificationAtLabel}</span>
                                <#if unreadAtNotificationCnt &gt; 0>
                                <span class="counter">${unreadAtNotificationCnt}</span>
                                </#if>
                            </a>
                        </li>
                        <li<#if type == "followingUser"> class="current"</#if>>
                            <a href="${servePath}/notifications/following-user">
                                <span>${notificationFollowingUserLabel}</span>
                                <#if unreadFollowingUserNotificationCnt &gt; 0>
                                <span class="counter">${unreadFollowingUserNotificationCnt}</span>
                                </#if>
                            </a>
                        </li>
                        <li<#if type == "point"> class="current"</#if>>
                            <a href="${servePath}/notifications/point">
                                <span>${pointLabel}</span>
                                <#if unreadPointNotificationCnt &gt; 0>
                                <span class="counter">${unreadPointNotificationCnt}</span>
                                </#if>
                            </a>
                        </li>
                        <li<#if type == "broadcast"> class="current"</#if>>
                            <a href="${servePath}/notifications/broadcast">
                                <span>${sameCityLabel}</span>
                                <#if unreadBroadcastNotificationCnt &gt; 0>
                                <span class="counter">${unreadBroadcastNotificationCnt}</span>
                                </#if>
                            </a>
                        </li>
                    </ul>
                    <br/>
                </div>
                <div class="list content">
                    <#nested>
                </div>
                <div class="side">
                    <#include '../../common/person-info.ftl'/>
                    <ul class="note-list responsive-hide">
                        <li<#if type == "commented"> class="current"</#if>>
                            <a href="${servePath}/notifications/commented">
                                <span>${notificationCommentedLabel}</span>
                                <#if unreadCommentedNotificationCnt &gt; 0>
                                <span class="counter">${unreadCommentedNotificationCnt}</span>
                                <span onclick="Util.makeNotificationRead('commented')" aria-label="${makeAsReadLabel}" class="fn-right tooltipped tooltipped-sw">
                                    <svg height="18" viewBox="0 0 12 16" width="12"><path d="M12 5l-8 8-4-4 1.5-1.5L4 10l6.5-6.5z"></path></svg>
                                </span>
                                </#if>
                            </a> 
                        </li>
                        <li<#if type == "at"> class="current"</#if>>
                            <a href="${servePath}/notifications/at">
                                <span>${notificationAtLabel}</span>
                                <#if unreadAtNotificationCnt &gt; 0>
                                <span class="counter">${unreadAtNotificationCnt}</span>
                                <span onclick="Util.makeNotificationRead('at')" aria-label="${makeAsReadLabel}" class="fn-right tooltipped tooltipped-sw">
                                    <svg height="18" viewBox="0 0 12 16" width="12"><path d="M12 5l-8 8-4-4 1.5-1.5L4 10l6.5-6.5z"></path></svg>
                                </span>
                                </#if>
                            </a>
                        </li>
                        <li<#if type == "followingUser"> class="current"</#if>>
                            <a href="${servePath}/notifications/following-user">
                                <span>${notificationFollowingUserLabel}</span>
                                <#if unreadFollowingUserNotificationCnt &gt; 0>
                                <span class="counter">${unreadFollowingUserNotificationCnt}</span>
                                <span onclick="Util.makeNotificationRead('followingUser')" aria-label="${makeAsReadLabel}" class="fn-right tooltipped tooltipped-sw">
                                    <svg height="18" viewBox="0 0 12 16" width="12"><path d="M12 5l-8 8-4-4 1.5-1.5L4 10l6.5-6.5z"></path></svg>
                                </span>
                                </#if>
                            </a>
                        </li>
                        <li<#if type == "point"> class="current"</#if>>
                            <a href="${servePath}/notifications/point">
                                <span>${pointLabel}</span>
                                <#if unreadPointNotificationCnt &gt; 0>
                                <span class="counter">${unreadPointNotificationCnt}</span>
                                </#if>
                            </a>
                        </li>
                        <li<#if type == "broadcast"> class="current"</#if>>
                            <a href="${servePath}/notifications/broadcast">
                                <span>${sameCityLabel}</span>
                                <#if unreadBroadcastNotificationCnt &gt; 0>
                                <span class="counter">${unreadBroadcastNotificationCnt}</span>
                                </#if>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
        <#include "../../footer.ftl">
    </body>
</html>
</#macro>
