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
                        <li class="current">
                            <a href="/${user.userName}">${articleLabel}</a>
                        </li>
                        <li>
                            <a href="/comments/${user.userName}">${cmtLabel}</a>
                        </li>
                    </ul>
                    <div>
                        <div class="article-list list">
                            <ul>
                                <#list userHomeArticles as userHomeArticle>
                                <li>
                                    <div>
                                        <h2><a href="${userHomeArticle.articlePermalink}">${userHomeArticle.articleTitle}</a></h2>
                                        <span class="ft-small">
                                            <#list userHomeArticle.articleTags?split(",") as articleTag>
                                            <a rel="tag" href="/tags/${articleTag?url('UTF-8')}">
                                                ${articleTag}</a><#if articleTag_has_next>, </#if>
                                            </#list>
                                            ${userHomeArticle.articleCreateTime?string('yyyy-MM-dd HH:mm:ss')}
                                        </span>
                                        <div class="count ft-small">
                                            ${viewLabel} <a href="">${userHomeArticle.articleViewCount}</a><br/>
                                            ${cmtLabel} <a href="">${userHomeArticle.articleCommentCount}</a>
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
        <#include "footer.ftl">
    </body>
</html>
