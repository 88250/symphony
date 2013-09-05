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
                    <#if commentedNotifications?size != 0>
                    <ul class="notification">
                        <#list commentedNotifications as notification>
                        <li class="fn-clear<#if notification.hasRead> read</#if>">
                            <img class="avatar fn-left" src="${notification.commentAuthorThumbnailURL}"/>
                            <a target="_blank" rel="nofollow" href="/member/${notification.commentAuthorName}" 
                               title="${notification.commentAuthorName}">${notification.commentAuthorName}</a>
                            <span class="ft-small">${commentOnLabel}</span>
                            <a href="${notification.commentSharpURL}"> ${notification.commentArticleTitle}</a>
                            <span class="ft-small">${replyYouLabel}</span>
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
                    
                    <@pagination url="/notifications/commented"/>
                </div>
                <div class="side">
                    <ul class="note-list">
                        <li class="current">
                            <a href="/notifications/commented">${notificationCommentedLabel}</a> 
                        </li>
                        <li>
                            <a href="#">${notificationAtLabel}</a>
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
