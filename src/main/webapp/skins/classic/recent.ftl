<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${latestLabel} - ${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <div class="module">
                        <div class="article-list list">
                            <ul class="stick">
                                <#list stickArticles as article>
                                <li>
                                    <h2>
                                        <#if 1 == article.articlePerfect>
                                        <span class="tooltipped tooltipped-w" aria-label="${perfectLabel}"><svg height="20" width="14" viewBox="3 3 11 12">${perfectIcon}</svg></span>
                                        </#if>
                                        <#if 1 == article.articleType>
                                        <span class="tooltipped tooltipped-w" aria-label="${discussionLabel}"><span class="icon-locked"></span></span>
                                        <#elseif 2 == article.articleType>
                                        <span class="tooltipped tooltipped-w" aria-label="${cityBroadcastLabel}"><span class="icon-feed"></span></span>
                                        <#elseif 3 == article.articleType>
                                        <span class="tooltipped tooltipped-w" aria-label="${thoughtLabel}"><span class="icon-video"></span></span>
                                        </#if>
                                        <a data-id="${article.oId}" data-type="${article.articleType}" rel="bookmark" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}
                                        </a>
                                    </h2>
                                    <div class="abstract">
                                        ${article.articlePreviewContent}
                                    </div>
                                    <div class="ft-smaller ft-fade fn-clear list-info">
                                        <span class="author"> 
                                            <#if article.articleAnonymous == 0>
                                            <a rel="nofollow" 
                                               href="${servePath}/member/${article.articleAuthorName}"></#if><div
                                                    class="avatar-small"
                                                    style="background-image:url('${article.articleAuthorThumbnailURL48}')"></div><#if article.articleAnonymous == 0></a></#if>&nbsp;
                                            <#if article.articleAnonymous == 0>
                                            <a rel="nofollow" class="user-name"
                                               href="${servePath}/member/${article.articleAuthorName}"></#if>
                                                ${article.articleAuthorName}
                                                <#if article.articleAnonymous == 0></a></#if>
                                            • ${article.timeAgo}
                                        </span>

                                        <span class="fn-left">
                                            <#list article.articleTagObjs as articleTag>
                                            <a rel="tag" class="ft-fade" href="${servePath}/tag/${articleTag.tagURI}">${articleTag.tagTitle}</a> &nbsp; 
                                            </#list>
                                        </span>

                                        <span class="fn-right">
                                            <#if article.articleCommentCount != 0>
                                            <a class="ft-fade" href="${servePath}${article.articlePermalink}#comments"><span class="level<#if article.articleCommentCount lt 80>${(article.articleCommentCount/20)?int}<#else>4</#if>">${article.articleCommentCount}</span> ${cmtLabel}</a> &nbsp;•&nbsp;
                                            </#if>   

                                            <#if article.articleViewCount != 0> 
                                            <a class="ft-fade" href="${servePath}${article.articlePermalink}"><span class="level<#if article.articleViewCount lt 800>${(article.articleViewCount/200)?int}<#else>4</#if>">${article.articleViewCount} ${viewLabel}</span></a>&nbsp;
                                            </#if>   

                                            <#if "" != article.articleLatestCmterName>
                                            •&nbsp; ${article.cmtTimeAgo} 
                                            <span class="ft-gray">
                                                <#if article.syncWithSymphonyClient>
                                                ${article.articleLatestCmterName}
                                                <#else>
                                                <#if article.articleLatestCmterName != 'someone'>
                                                <a rel="nofollow" class="user-name" href="${servePath}/member/${article.articleLatestCmterName}">
                                                    </#if>
                                                    ${article.articleLatestCmterName}
                                                    <#if article.articleLatestCmterName != 'someone'>
                                                </a>
                                                </#if>
                                                </#if> 
                                            </span>
                                            </#if>
                                        </span>
                                    </div>

                                    <span class="heat tooltipped tooltipped-n" aria-label="${postActivityLabel}" style="width:${article.articleHeat*3}px"></span>
                                </li>
                                </#list>
                            </ul>
                        </div>
                    </div>
                    <div class="module">
                        <div class="module-header fn-clear">
                            <span class="fn-right ft-fade">
                                <a class="<#if "" == current>ft-fade<#else>ft-gray</#if>" href="${servePath}/recent">${defaultLabel}</a>
                                /
                                <a class="<#if "/hot" == current>ft-fade<#else>ft-gray</#if>" href="${servePath}/recent/hot">${hotArticlesLabel}</a>
                                /
                                <a class="<#if "/good" == current>ft-fade<#else>ft-gray</#if>" href="${servePath}/recent/good">${goodCmtsLabel}</a>
                                /
                                <a class="<#if "/reply" == current>ft-fade<#else>ft-gray</#if>" href="${servePath}/recent/reply">${recentCommentLabel}</a>
                            </span>
                        </div>
                        <@list listData=latestArticles/>
                        <@pagination url="${servePath}/recent${current}"/>
                    </div>
                    <#include "common/domains.ftl">
                </div>

                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <@listScript/>
    </body>
</html>
