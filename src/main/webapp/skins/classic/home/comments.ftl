<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
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
                            <a href="/${user.userName}">${articleLabel}</a>
                        </li>
                        <li class="current">
                            <a href="/${user.userName}/comments">${cmtLabel}</a>
                        </li>
                    </ul>
                    <div>
                        <div class="comment-list list">
                            <ul>
                                <#list userHomeComments as comment>
                                <li>
                                    <div class="fn-clear">
                                        <div class="fn-left comment-main" style="width:670px">
                                            <span class="fn-clear">
                                                <span class="fn-left">
                                                    <h2><a href="${comment.commentArticlePermalink}">${comment.commentArticleTitle}</a></h2>
                                                </span>
                                                <span class="fn-right ft-small">
                                                    ${comment.commentCreateTime?string('yyyy-MM-dd HH:mm')}  
                                                </span>    
                                            </span>
                                            <div>
                                                ${comment.commentContent}  
                                            </div>
                                        </div>
                                    </div>
                                </li>
                                </#list>  
                            </ul>
                        </div>
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
