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
                        <div class="comment-list">
                            <ul>
                                <#list userHomeComments as comment>
                                <li>
                                    <div class="fn-clear">
                                        <span class="fn-left">
                                            <span class="ft-small">回复了 <a target="_blank" href="${comment.commentArticleAuthorURL}">${comment.commentArticleAuthorName}</a> 创建的主题 > </span>
                                            <a rel="bookmark" href="${comment.commentSharpURL}">${comment.commentArticleTitle}</a>
                                        </span>
                                        <span class="fn-right ft-small">
                                            ${comment.commentCreateTime?string('yyyy-MM-dd HH:mm')}  
                                        </span>    
                                    </div>
                                    <div class="content-reset">
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
