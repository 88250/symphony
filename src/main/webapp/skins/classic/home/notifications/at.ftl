<#include "../../macro-head.ftl">
<#include "../../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${userName} - ${messageLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/home.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../../header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="list content">
                    <#if atNotifications?size != 0>
                    <ul class="notification">
                        <#list atNotifications as notification>
                        <li class="fn-clear<#if notification.hasRead> read</#if>">
                            <img class="avatar fn-left" src="${notification.commentAuthorThumbnailURL}"/>
                            <a target="_blank" rel="nofollow" href="/member/${notification.commentAuthorName}" 
                               title="${notification.commentAuthorName}">${notification.commentAuthorName}</a>
                            <span class="ft-small">${commentOnLabel}${cmtLabel}${articleLabel}</span>
                            <a href="${notification.commentSharpURL}"> ${notification.commentArticleTitle}</a>
                            <span class="ft-small">${atYouLabel}</span>
                            <span class="ico-date ft-small fn-right">${notification.commentCreateTime?string('yyyy-MM-dd HH:mm')}</span>
                            <div class="content-reset content-reset-p ">
                                ${notification.commentContent}
                            </div>
                        </li>
                        </#list>
                    </ul>
                    <#else>
                    ${noMessageLabel}
                    </#if>

                    <@pagination url="/notifications/at"/>
                </div>
                <div class="side">
                    <ul class="note-list">
                        <li>
                            <a href="/notifications/commented">
                                <#if unreadCommentedNotificationCnt &gt; 0>
                                ${unreadCommentedNotificationCnt}  </#if>${notificationCommentedLabel}</a> 
                        </li>
                        <li class="current">
                            <a href="/notifications/at">
                                <#if unreadAtNotificationCnt &gt; 0>
                                ${unreadAtNotificationCnt} </#if>${notificationAtLabel}</a>
                        </li>
                        <li>
                            <a href="#">${notificationFollowingUserLabel}</a>
                        </li>
                        <li>
                            <a href="#">${notificationFollowingTagLabel}</a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
        <#include "../../footer.ftl">
    </body>
</html>
