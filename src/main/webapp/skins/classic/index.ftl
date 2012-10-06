<#include "macro-head.ftl">
<#include "macro-footer.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index.css" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content">
                    <div class="article-list list">
                        <ul>
                            <#list latestCmtArticles as latestCmtArticle>
                            <li>
                                <div>
                                    <div class="fn-clear">
                                        <img class="avatar fn-left" src="https://secure.gravatar.com/avatar/22ae6b52ee5c2d024b68531bd250be5b?s=140" />
                                        <div class="fn-left" style="width: 550px">
                                            <h2><a href="${latestCmtArticle.articlePermalink}">${latestCmtArticle.articleTitle}</a></h2>
                                            <span class="ft-small">
                                                <#list latestCmtArticle.articleTags?split(",") as articleTag>
                                                <a rel="tag" href="/tags/${articleTag?url('UTF-8')}">
                                                    ${articleTag}</a><#if articleTag_has_next>, </#if>
                                                </#list>
                                                ${latestCmtArticle.articleCreateTime}
                                            </span>
                                        </div>
                                    </div>
                                    <div class="count ft-small">
                                        ${viewLabel} <a href="">${latestCmtArticle.articleViewCount}</a><br/>
                                        ${cmtLabel} <a href="">${latestCmtArticle.articleCommentCount}</a>
                                    </div>
                                    <div class="commenters">
                                        <#list 1..10 as i>
                                        <a href="" class="ft-noline">
                                            <img class="avatar-small" src="https://secure.gravatar.com/avatar/22ae6b52ee5c2d024b68531bd250be5b?s=140" />
                                        </a>
                                        </#list>
                                    </div>
                                </div>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <@footer/>
    </body>
</html>
