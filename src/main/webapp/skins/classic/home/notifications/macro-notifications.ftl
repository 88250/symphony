<#macro notifications type>
<#include "../../macro-head.ftl">
<#include "../../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${userName} - ${messageLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/home${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../../header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="list content">
                    <#nested>
                </div>
                <div class="side">
                    <ul class="note-list">
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
                    </ul>
                </div>
            </div>
        </div>
        <#include "../../footer.ftl">
    </body>
</html>
</#macro>
