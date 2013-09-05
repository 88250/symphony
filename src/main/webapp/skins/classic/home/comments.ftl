<#include "../macro-head.ftl">
<#include "../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${user.userName} - ${cmtLabel}">
        <meta name="keywords" content="${user.userName},${cmtLabel}"/>
        <meta name="description" content="${user.userName}${deLabel}${cmtLabel},${cmtLabel} by ${user.userName}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/home.css" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content">
                    <ul class="tab fn-clear">
                        <li>
                            <a href="/member/${user.userName}">${articleLabel}</a>
                        </li>
                        <li class="current">
                            <a href="/member/${user.userName}/comments">${cmtLabel}</a>
                        </li>
                    </ul>
                    <div class="fn-clear">
                        <div class="list">
                            <ul>
                                <#list userHomeComments as comment>
                                <li class="fn-clear">
                                    <img class="avatar fn-left" src="${comment.commentArticleAuthorThumbnailURL}"/>
                                    <a target="_blank" rel="nofollow" href="${comment.commentArticleAuthorURL}">${comment.commentArticleAuthorName}</a>
                                    <span class="ft-small">${creatThemeLabel}</span>
                                    <a rel="bookmark" href="${comment.commentSharpURL}">${comment.commentArticleTitle}</a>
                                    <span class="fn-right ft-small ico-date">
                                        ${comment.commentCreateTime?string('yyyy-MM-dd HH:mm')}  
                                    </span>    
                                    <div class="content-reset content-reset-p">
                                        ${comment.commentContent}
                                    </div>
                                </li>
                                </#list>  
                            </ul>
                        </div>
                        <@pagination url="/member/${user.userName}/comments"/>
                    </div>
                </div>
                <div class="side">
                    <#include "home-side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>
