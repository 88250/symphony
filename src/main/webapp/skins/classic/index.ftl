<#include "macro-head.ftl">
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
                                        <a class="ft-noline" 
                                           href="/${latestCmtArticle.articleAuthorName}" 
                                           title="${latestCmtArticle.articleAuthorName}"><img class="avatar fn-left" src="${latestCmtArticle.articleAuthorThumbnailURL}" /></a>
                                        <div class="fn-left" style="width: 550px">
                                            <h2><a href="${latestCmtArticle.articlePermalink}">${latestCmtArticle.articleTitle}</a></h2>
                                            <span class="ft-small">
                                                <#list latestCmtArticle.articleTags?split(",") as articleTag>
                                                <a rel="tag" href="/tags/${articleTag?url('UTF-8')}">
                                                    ${articleTag}</a><#if articleTag_has_next>, </#if>
                                                </#list>
                                            </span>
                                        </div>
                                    </div>
                                    <div class="count ft-small">
                                        ${viewLabel} <a href="">${latestCmtArticle.articleViewCount}</a><br/>
                                        ${cmtLabel} <a href="">${latestCmtArticle.articleCommentCount}</a>
                                    </div>
                                    <div class="commenters">
                                        <#list latestCmtArticle.articleParticipants as comment>
                                        <a href="/${comment.articleParticipantName}" title="${comment.articleParticipantName}" class="ft-noline">
                                            <img class="avatar-small" src="${comment.articleParticipantThumbnailURL}" />
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
        <#include "footer.ftl">
    </body>
</html>
