<#include "../../macro-head.ftl">
<#include "../../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${userName} - ${messageLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/home.css" />
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
                            <a href="">username</a>
                            <span class="ft-small">在</span>
                            <a href="${notification.commentArticlePermalink}"> ${notification.commentArticleTitle}</a>
                            <span class="ft-small">中回复了你</span>
                            <span class="ico-date ft-small fn-right">2013-09-03 17:49</span>
                            <#--${notification.commentCreateTime?string('yyyy-MM-dd HH:mm')}-->
                            <div class="content-reset">
                                ${notification.commentContent}
                            </div>
                        </li>
                        </#list>
                    </ul>
                    <#else>
                    ${noMessageLabel}
                    </#if>
                </div>
                <div class="side">
                    <ul class="note-list">
                        <li class="current">
                            <a href="/notifications/commented">我收到的评论</a> 
                        </li>
                        <li>
                            <a href="#">@我的(开发ing)</a>
                        </li>
                        <li>
                            <a href="#">我关注的人(开发ing)</a>
                        </li>
                        <li>
                            <a href="#">我关注的标签(开发ing)</a>
                        </li>
                    </ul>
                    <@pagination url="/notifications/commented"/>
                </div>
            </div>
        </div>
        <#include "../../footer.ftl">
    </body>
</html>
