<#include "../macro-head.ftl">
<#include "../macro-pagination.ftl">
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
                        <li class="current">
                            <a href="/${user.userName}">${articleLabel}</a>
                        </li>
                        <li>
                            <a href="/${user.userName}/comments">${cmtLabel}</a>
                        </li>
                    </ul>
                    <div>
                        <div class="article-list list">
                            <ul> 
                                <#list userHomeArticles as article>
                                <li>
                                    <div class="fn-clear">
                                        <div class="fn-left">
                                            <h2><a href="${article.articlePermalink}">${article.articleTitle}</a></h2>
                                            <span class="ft-small">
                                                <#list article.articleTags?split(",") as articleTag>
                                                <a rel="tag" href="/tags/${articleTag?url('UTF-8')}">
                                                    ${articleTag}</a><#if articleTag_has_next>, </#if>
                                                </#list>
                                                <span class="date-ico">${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}</span>
                                            </span>
                                        </div>
                                    </div>
                                    <div class="count ft-small">
                                        ${viewLabel} <a href="">${article.articleViewCount}</a><br/>
                                        ${cmtLabel} <a href="">${article.articleCommentCount}</a>
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
