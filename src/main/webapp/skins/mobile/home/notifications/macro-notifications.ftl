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
                            <a href="/notifications/commented">
                                <span>${notificationCommentedLabel}</span>
                                <#if unreadCommentedNotificationCnt &gt; 0>
                                <span class="counter">${unreadCommentedNotificationCnt}</span>
                                </#if>
                            </a> 
                        </li>
                        <li<#if type == "at"> class="current"</#if>>
                            <a href="/notifications/at">
                                <span>${notificationAtLabel}</span>
                                <#if unreadAtNotificationCnt &gt; 0>
                                <span class="counter">${unreadAtNotificationCnt}</span>
                                </#if>
                            </a>
                        </li>
                        <li<#if type == "followingUser"> class="current"</#if>>
                            <a href="/notifications/following-user">
                                <span>${notificationFollowingUserLabel}</span>
                                <#if unreadFollowingUserNotificationCnt &gt; 0>
                                <span class="counter">${unreadFollowingUserNotificationCnt}</span>
                                </#if>
                            </a>
                        </li>
                        <li<#if type == "point"> class="current"</#if>>
                            <a href="/notifications/point">
                                <span>${pointLabel}</span>
                                <#if unreadPointNotificationCnt &gt; 0>
                                <span class="counter">${unreadPointNotificationCnt}</span>
                                </#if>
                            </a>
                        </li>
                        <li<#if type == "broadcast"> class="current"</#if>>
                            <a href="/notifications/broadcast">
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
                            <a href="/notifications/commented">
                                <span>${notificationCommentedLabel}</span>
                                <#if unreadCommentedNotificationCnt &gt; 0>
                                <span class="counter">${unreadCommentedNotificationCnt}</span>
                                </#if>
                            </a> 
                        </li>
                        <li<#if type == "at"> class="current"</#if>>
                            <a href="/notifications/at">
                                <span>${notificationAtLabel}</span>
                                <#if unreadAtNotificationCnt &gt; 0>
                                <span class="counter">${unreadAtNotificationCnt}</span>
                                </#if>
                            </a>
                        </li>
                        <li<#if type == "followingUser"> class="current"</#if>>
                            <a href="/notifications/following-user">
                                <span>${notificationFollowingUserLabel}</span>
                                <#if unreadFollowingUserNotificationCnt &gt; 0>
                                <span class="counter">${unreadFollowingUserNotificationCnt}</span>
                                </#if>
                            </a>
                        </li>
                        <li<#if type == "point"> class="current"</#if>>
                            <a href="/notifications/point">
                                <span>${pointLabel}</span>
                                <#if unreadPointNotificationCnt &gt; 0>
                                <span class="counter">${unreadPointNotificationCnt}</span>
                                </#if>
                            </a>
                        </li>
                        <li<#if type == "broadcast"> class="current"</#if>>
                            <a href="/notifications/broadcast">
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
