<#include "../macro-head.ftl">
<#include "../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${userName} - ${cmtLabel}">
        <meta name="keywords" content="${userName},${cmtLabel}"/>
        <meta name="description" content="${userName}${deLabel}${cmtLabel},${cmtLabel} by ${userName}"/>
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
                        <div class="comment-list list">
                            <ul>
                                <#list userHomeComments as comment>
                                <li>
                                    <div class="fn-clear">
                                        <div class="fn-left comment-main" style="width:670px">
                                            <div class="fn-clear">
                                                <span class="fn-left">
                                                    <h2><a rel="bookmark" href="${comment.commentSharpURL}">${comment.commentArticleTitle}</a></h2>
                                                </span>
                                                <span class="fn-right ft-small">
                                                    ${comment.commentCreateTime?string('yyyy-MM-dd HH:mm')}  
                                                </span>    
                                            </div>
                                            <div class="content-reset">
                                                ${comment.commentContent}  
                                            </div>
                                        </div>
                                    </div>
                                </li>
                                </#list>  
                            </ul>
                        </div>
                        <@pagination url="/member/${userName}/comments"/>
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
